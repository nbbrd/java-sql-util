/*
 * Copyright 2013 National Bank of Belgium
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
package nbbrd.sql.jdbc;

import internal.sql.jdbc.JdbcUtil;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A class that quotes identifiers in SQL queries.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
public final class SqlIdentifierQuoter {

    @NonNull
    public static SqlIdentifierQuoter of(@NonNull DatabaseMetaData metaData) throws SQLException {
        return new SqlIdentifierQuoter(
                loadIdentifierQuoteString(metaData),
                loadSqlKeywords(metaData),
                loadUnquotedIdentifierStorageRule(metaData),
                loadExtraNameCharacters(metaData)
        );
    }

    @NonNull
    public static Builder builder() {
        return new Builder()
                .quoteString(DEFAULT_IDENTIFIER_QUOTE_STRING)
                .unquotedStorageRule(SqlIdentifierStorageRule.UPPER)
                .extraNameCharacters("");
    }

    public static final SqlIdentifierQuoter DEFAULT = builder().build();

    @lombok.NonNull
    private final String quoteString;

    @lombok.Singular
    private final Set<String> sqlKeywords;

    @lombok.NonNull
    private final SqlIdentifierStorageRule unquotedStorageRule;

    @lombok.NonNull
    private final String extraNameCharacters;

    @NonNull
    public String quote(@NonNull String identifier) {
        return quote(identifier, false);
    }

    @NonNull
    public String quote(@NonNull String identifier, boolean force) {
        if (isProperlyQuoted(identifier)) {
            return identifier;
        }

        if (force
                || isSqlKeyword(identifier)
                || containsExtraCharacters(identifier)
                || containsQuoteStrings(identifier)
                || containsSpaces(identifier)
                || breaksStorageRule(identifier)) {
            return quoteIdentifier(identifier);
        }

        return identifier;
    }

    private boolean isProperlyQuoted(String identifier) {
        if (!(identifier.startsWith(quoteString)
                && identifier.endsWith(quoteString)
                && identifier.length() >= quoteString.length() * 2)) {
            return false;
        }

        int quoteLength = quoteString.length();
        int begin = quoteLength;
        int end = identifier.length() - quoteLength;
        boolean even = true;
        while (begin < end) {
            int next = identifier.indexOf(quoteString, begin);
            if (next == -1 || next == end) {
                return even;
            }
            if (even) {
                even = false;
            } else {
                if (begin == next) {
                    even = true;
                } else {
                    return false;
                }
            }
            begin = next + quoteLength;
        }
        return even;
    }

    private boolean isSqlKeyword(String identifier) {
        return sqlKeywords.contains(normalizeKeyword(identifier));
    }

    private boolean breaksStorageRule(String identifier) {
        return false;
        // FIXME: seems to follow API but fails in tests!
        //return !unquotedStorageRule.isValid(identifier);
    }

    private boolean containsExtraCharacters(String identifier) {
        for (int i = 0; i < identifier.length(); i++) {
            if (extraNameCharacters.indexOf(identifier.charAt(i)) != -1) {
                return true;
            }
        }
        return false;
    }

    private boolean containsQuoteStrings(String identifier) {
        return identifier.contains(quoteString);
    }

    private boolean containsSpaces(String identifier) {
        return identifier.contains(" ");
    }

    private String quoteIdentifier(String identifier) {
        return quoteString + identifier.replace(quoteString, quoteString + quoteString) + quoteString;
    }

    private static final String DEFAULT_IDENTIFIER_QUOTE_STRING = "\"";
    private static final String NOT_SUPPORTED_IDENTIFIER_QUOTE_STRING = " ";

    @NonNull
    static String loadIdentifierQuoteString(@NonNull DatabaseMetaData metaData) throws SQLException {
        String identifierQuoteString = JdbcUtil.unexpectedNullToBlank(metaData.getIdentifierQuoteString(), "getIdentifierQuoteString");

        if (identifierQuoteString.equals(NOT_SUPPORTED_IDENTIFIER_QUOTE_STRING)) {
            return DEFAULT_IDENTIFIER_QUOTE_STRING;
        }

        if (isUnexpectedIdentifierQuoteString(identifierQuoteString)) {
            return DEFAULT_IDENTIFIER_QUOTE_STRING;
        }

        return identifierQuoteString;
    }

    static boolean isUnexpectedIdentifierQuoteString(@NonNull String identifierQuoteString) {
        return identifierQuoteString.trim().isEmpty();
    }

    @NonNull
    static Set<String> loadSqlKeywords(@NonNull DatabaseMetaData metaData) throws SQLException {
        return Stream
                .concat(getSpecificKeywords(metaData), getStandardKeywords(metaData))
                .collect(Collectors.toSet());
    }

    @NonNull
    static Stream<String> getSpecificKeywords(@NonNull DatabaseMetaData metaData) throws SQLException {
        String specificKeywords = JdbcUtil.unexpectedNullToBlank(metaData.getSQLKeywords(), "getSQLKeywords");
        return getSpecificKeywords(specificKeywords);
    }

    @NonNull
    static Stream<String> getStandardKeywords(@NonNull DatabaseMetaData metaData) throws SQLException {
        return SqlKeywords.LATEST_RESERVED_WORDS.getKeywords().stream();
    }

    @NonNull
    static Stream<String> getSpecificKeywords(@NonNull CharSequence input) {
        return JdbcUtil.splitToStream(',', input)
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .map(SqlIdentifierQuoter::normalizeKeyword);
    }

    @NonNull
    static String normalizeKeyword(String input) {
        return input.toUpperCase(Locale.ROOT);
    }

    @NonNull
    static String loadExtraNameCharacters(@NonNull DatabaseMetaData metaData) throws SQLException {
        return JdbcUtil.unexpectedNullToBlank(metaData.getExtraNameCharacters(), "getExtraNameCharacters");
    }

    @NonNull
    static SqlIdentifierStorageRule loadUnquotedIdentifierStorageRule(@NonNull DatabaseMetaData metaData) throws SQLException {
        if (metaData.storesUpperCaseIdentifiers()) {
            return SqlIdentifierStorageRule.UPPER;
        }
        if (metaData.storesLowerCaseIdentifiers()) {
            return SqlIdentifierStorageRule.LOWER;
        }
        if (metaData.storesMixedCaseIdentifiers()) {
            return SqlIdentifierStorageRule.MIXED;
        }
        return SqlIdentifierStorageRule.UPPER;
    }
}
