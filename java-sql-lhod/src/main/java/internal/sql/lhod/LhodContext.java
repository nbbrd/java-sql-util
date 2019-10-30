/*
 * Copyright 2016 National Bank of Belgium
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
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class LhodContext {

    @lombok.NonNull
    private final TabularDataExecutor executor;

    @lombok.Getter
    @lombok.NonNull
    private final String connectionString;

    @lombok.Getter
    @lombok.NonNull
    private final Instant creation;

    private EnumMap<DynamicProperty, String> lazyProperties = null;

    @Nullable
    public String getProperty(@NonNull DynamicProperty property) throws IOException {
        Objects.requireNonNull(property);
        return getProperties().get(property);
    }

    @NonNull
    public Map<DynamicProperty, String> getProperties() throws IOException {
        if (lazyProperties == null) {
            try (TabularDataReader reader = dbProperties()) {
                EnumMap<DynamicProperty, String> result = new EnumMap<>(DynamicProperty.class);
                String[] row = new String[reader.getHeader(0).length];
                while (reader.readNextInto(row)) {
                    DynamicProperty property = getPropertyOrNull(row[0]);
                    if (property != null) {
                        result.put(property, row[1]);
                    }
                }
                lazyProperties = result;
            }
        }
        return lazyProperties;
    }

    private static DynamicProperty getPropertyOrNull(String key) {
        for (DynamicProperty o : DynamicProperty.values()) {
            if (o.getKey().equals(key)) {
                return o;
            }
        }
        return null;
    }

    @Nullable
    public IdentifierCaseType getIdentifierCaseType() throws IOException {
        String property = getProperty(DynamicProperty.IDENTIFIER_CASE_SENSITIVITY);
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

    @NonNull
    public Stream<SqlStringFunction> getStringFunctions() throws IOException {
        String property = getProperty(DynamicProperty.STRING_FUNCTIONS);
        if (property != null) {
            try {
                int value = Integer.parseInt(property);
                return getStringFunctions(value);
            } catch (NumberFormatException ex) {
                throw new IOException("Cannot parse string functions bitmask", ex);
            }
        }
        return Stream.empty();
    }

    private static Stream<SqlStringFunction> getStringFunctions(int value) {
        return Arrays
                .stream(SqlStringFunction.values())
                .filter(func -> hasBitmask(value, func.getBitmask()));
    }

    private static boolean hasBitmask(int value, int bitmask) {
        return (value & bitmask) == bitmask;
    }

    @NonNull
    public TabularDataReader preparedStatement(@NonNull String sql, @NonNull List<String> parameters) throws IOException {
        TabularDataQuery query = TabularDataQuery
                .builder()
                .procedure("PreparedStatement.vbs")
                .parameter(connectionString)
                .parameter(sql)
                .parameters(parameters)
                .build();
        return executor.exec(query);
    }

    @NonNull
    public TabularDataReader openSchema(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws IOException {
        TabularDataQuery query = TabularDataQuery
                .builder()
                .procedure("OpenSchema.vbs")
                .parameter(connectionString)
                .parameter(catalog != null ? catalog : "\"\"")
                .parameter(schemaPattern != null && !schemaPattern.equals("%") ? schemaPattern : "\"\"")
                .parameter(tableNamePattern != null && !tableNamePattern.equals("%") ? tableNamePattern : "\"\"")
                .parameters(types != null ? Arrays.asList(types) : Collections.emptyList())
                .build();
        return executor.exec(query);
    }

    @NonNull
    public TabularDataReader dbProperties() throws IOException {
        TabularDataQuery query = TabularDataQuery
                .builder()
                .procedure("DbProperties.vbs")
                .parameter(connectionString)
                .parameters(Stream.of(DynamicProperty.values()).map(DynamicProperty::getKey).collect(Collectors.toList()))
                .build();
        return executor.exec(query);
    }

    // https://msdn.microsoft.com/en-us/library/ms676695%28v=vs.85%29.aspx
    @lombok.AllArgsConstructor
    @lombok.Getter
    public enum DynamicProperty {
        CURRENT_CATALOG("Current Catalog"),
        SPECIAL_CHARACTERS("Special Characters"),
        IDENTIFIER_CASE_SENSITIVITY("Identifier Case Sensitivity"),
        STRING_FUNCTIONS("String Functions");

        private final String key;
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    public enum IdentifierCaseType {
        LOWER(2),
        MIXED(8),
        SENSITIVE(4),
        UPPER(1);

        private final int value;
    }

    //https://msdn.microsoft.com/en-us/library/ms710249(v=vs.85).aspx
    @lombok.AllArgsConstructor
    @lombok.Getter
    public enum SqlStringFunction {
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
