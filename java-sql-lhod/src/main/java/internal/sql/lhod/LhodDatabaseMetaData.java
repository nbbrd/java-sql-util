/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.sql.lhod;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import static java.lang.String.format;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class LhodDatabaseMetaData extends _DatabaseMetaData {

    @lombok.NonNull
    private final LhodConnection conn;

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        conn.checkState();
        try {
            return getIdentifierCaseType() == IdentifierCaseType.UPPER;
        } catch (IOException ex) {
            throw ex instanceof TabDataRemoteError
                    ? new SQLException(ex.getMessage(), "", ((TabDataRemoteError) ex).getNumber())
                    : new SQLException(format("Failed to get identifier case type of '%s'", conn.getConnectionString()), ex);
        }
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        conn.checkState();
        try {
            return getIdentifierCaseType() == IdentifierCaseType.LOWER;
        } catch (IOException ex) {
            throw ex instanceof TabDataRemoteError
                    ? new SQLException(ex.getMessage(), "", ((TabDataRemoteError) ex).getNumber())
                    : new SQLException(format("Failed to get identifier case type of '%s'", conn.getConnectionString()), ex);
        }
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        conn.checkState();
        try {
            return getIdentifierCaseType() == IdentifierCaseType.MIXED;
        } catch (IOException ex) {
            throw ex instanceof TabDataRemoteError
                    ? new SQLException(ex.getMessage(), "", ((TabDataRemoteError) ex).getNumber())
                    : new SQLException(format("Failed to get identifier case type of '%s'", conn.getConnectionString()), ex);
        }
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        conn.checkState();
        return null;
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        conn.checkState();
        return "";
    }

    @Override
    public String getStringFunctions() throws SQLException {
        conn.checkState();
        try {
            return getStringFunctionStream()
                    .map(SqlStringFunction::getLabel)
                    .sorted()
                    .collect(Collectors.joining(","));
        } catch (IOException ex) {
            throw ex instanceof TabDataRemoteError
                    ? new SQLException(ex.getMessage(), "", ((TabDataRemoteError) ex).getNumber())
                    : new SQLException(format("Failed to get string functions of '%s'", conn.getConnectionString()), ex);
        }
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        conn.checkState();
        try {
            return conn.getProperty(LhodConnection.DynamicProperty.SPECIAL_CHARACTERS);
        } catch (IOException ex) {
            throw ex instanceof TabDataRemoteError
                    ? new SQLException(ex.getMessage(), "", ((TabDataRemoteError) ex).getNumber())
                    : new SQLException(format("Failed to get extra name chars of '%s'", conn.getConnectionString()), ex);
        }
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        conn.checkState();

        TabDataQuery query = TabDataQuery
                .builder()
                .procedure("OpenSchema")
                .parameter(conn.getConnectionString())
                .parameter(catalog != null ? catalog : "\"\"")
                .parameter(schemaPattern != null && !schemaPattern.equals("%") ? schemaPattern : "\"\"")
                .parameter(tableNamePattern != null && !tableNamePattern.equals("%") ? tableNamePattern : "\"\"")
                .parameters(types != null ? Arrays.asList(types) : Collections.emptyList())
                .build();

        try {
            return LhodResultSet.of(conn.exec(query));
        } catch (IOException ex) {
            throw ex instanceof TabDataRemoteError
                    ? new SQLException(ex.getMessage(), "", ((TabDataRemoteError) ex).getNumber())
                    : new SQLException(format("Failed to list tables with catalog='%s', schemaPattern='%s', tableNamePattern='%s', types='%s' of '%s'", catalog, schemaPattern, tableNamePattern, types != null ? Arrays.toString(types) : null, conn.getConnectionString()), ex);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        conn.checkState();
        return conn;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        conn.checkState();
        return conn.isReadOnly();
    }

    @Nullable
    private IdentifierCaseType getIdentifierCaseType() throws IOException {
        String property = conn.getProperty(LhodConnection.DynamicProperty.IDENTIFIER_CASE_SENSITIVITY);
        if (property != null) {
            try {
                int value = Integer.parseInt(property);
                return getIdentifierCaseTypeOrNull(value);
            } catch (NumberFormatException ex) {
                throw new IOException("Cannot parse identifier case type", ex);
            }
        }
        return null;
    }

    private static IdentifierCaseType getIdentifierCaseTypeOrNull(int value) {
        return Arrays
                .stream(IdentifierCaseType.values())
                .filter(type -> type.getValue() == value)
                .findFirst()
                .orElse(null);
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    private enum IdentifierCaseType {
        LOWER(2),
        MIXED(8),
        SENSITIVE(4),
        UPPER(1);

        private final int value;
    }

    @NonNull
    private Stream<SqlStringFunction> getStringFunctionStream() throws IOException {
        String property = conn.getProperty(LhodConnection.DynamicProperty.STRING_FUNCTIONS);
        if (property != null) {
            try {
                int value = Integer.parseInt(property);
                return getStringFunctionStream(value);
            } catch (NumberFormatException ex) {
                throw new IOException("Cannot parse string functions bitmask", ex);
            }
        }
        return Stream.empty();
    }

    private static Stream<SqlStringFunction> getStringFunctionStream(int value) {
        return Arrays
                .stream(SqlStringFunction.values())
                .filter(func -> hasBitmask(value, func.getBitmask()));
    }

    private static boolean hasBitmask(int value, int bitmask) {
        return (value & bitmask) == bitmask;
    }

    //https://msdn.microsoft.com/en-us/library/ms710249(v=vs.85).aspx
    @lombok.AllArgsConstructor
    @lombok.Getter
    private enum SqlStringFunction {
        SQL_FN_STR_CONCAT(0x00000001, "CONCAT"),
        SQL_FN_STR_INSERT(0x00000002, "INSERT"),
        SQL_FN_STR_LEFT(0x00000004, "LEFT"),
        SQL_FN_STR_LTRIM(0x00000008, "LTRIM"),
        SQL_FN_STR_LENGTH(0x00000010, "LENGTH"),
        SQL_FN_STR_LOCATE(0x00000020, "LOCATE"),
        SQL_FN_STR_LCASE(0x00000040, "LCASE"),
        SQL_FN_STR_REPEAT(0x00000080, "REPEAT"),
        SQL_FN_STR_REPLACE(0x00000100, "REPLACE"),
        SQL_FN_STR_RIGHT(0x00000200, "RIGHT"),
        SQL_FN_STR_RTRIM(0x00000400, "RTRIM"),
        SQL_FN_STR_SUBSTRING(0x00000800, "SUBSTRING"),
        SQL_FN_STR_UCASE(0x00001000, "UCASE"),
        SQL_FN_STR_ASCII(0x00002000, "ASCII"),
        SQL_FN_STR_CHAR(0x00004000, "CHAR"),
        SQL_FN_STR_DIFFERENCE(0x00008000, "DIFFERENCE"),
        SQL_FN_STR_LOCATE_2(0x00010000, "LOCATE_2"),
        SQL_FN_STR_SOUNDEX(0x00020000, "SOUNDEX"),
        SQL_FN_STR_SPACE(0x00040000, "SPACE"),
        SQL_FN_STR_BIT_LENGTH(0x00080000, "BIT_LENGTH"),
        SQL_FN_STR_CHAR_LENGTH(0x00100000, "CHAR_LENGTH"),
        SQL_FN_STR_CHARACTER_LENGTH(0x00200000, "CHARACTER_LENGTH"),
        SQL_FN_STR_OCTET_LENGTH(0x00400000, "OCTET_LENGTH"),
        SQL_FN_STR_POSITION(0x00800000, "POSITION");

        private final int bitmask;
        private final String label;
    }
}
