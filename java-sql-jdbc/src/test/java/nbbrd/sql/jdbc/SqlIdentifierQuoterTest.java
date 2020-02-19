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

import static nbbrd.sql.jdbc.SqlIdentifierQuoter.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SqlIdentifierQuoterTest {

    @Test
    public void testFactory() throws SQLException {
        assertThatNullPointerException().isThrownBy(() -> SqlIdentifierQuoter.of(null));

        for (InMemoryDriver driver : InMemoryDriver.values()) {
            try (Connection conn = driver.getConnection()) {
                SqlIdentifierQuoter quoter = SqlIdentifierQuoter.of(conn.getMetaData());

                assertThat(quoter.getQuoteString())
                        .isEqualTo(driver.getQuoteString());

                if (driver.isCustomKeywords()) {
                    assertThat(quoter.getSqlKeywords())
                            .containsAll(latestKeywords)
                            .hasSizeGreaterThan(latestKeywords.size())
                            .doesNotContain("");
                } else {
                    assertThat(quoter.getSqlKeywords())
                            .containsExactlyElementsOf(latestKeywords);
                }

                assertThat(quoter.getUnquotedStorageRule())
                        .isEqualTo(driver.getStorageRule());

                assertThat(quoter.getExtraNameCharacters())
                        .isEqualTo(driver.getExtraNameCharacters());
            }
        }
    }

    @Test
    public void testLoadIdentifierQuoteString() throws SQLException {
        assertThatNullPointerException().isThrownBy(() -> loadIdentifierQuoteString(null));

        for (InMemoryDriver driver : InMemoryDriver.values()) {
            try (Connection conn = driver.getConnection()) {
                assertThat(loadIdentifierQuoteString(conn.getMetaData()))
                        .isEqualTo(driver.getQuoteString());
            }
        }
    }

    @Test
    public void testLoadSqlKeywords() throws SQLException {
        assertThatNullPointerException().isThrownBy(() -> loadSqlKeywords(null));

        for (InMemoryDriver driver : InMemoryDriver.values()) {
            try (Connection conn = driver.getConnection()) {
                if (driver.isCustomKeywords()) {
                    assertThat(loadSqlKeywords(conn.getMetaData()))
                            .containsAll(latestKeywords)
                            .hasSizeGreaterThan(latestKeywords.size())
                            .doesNotContain("");
                } else {
                    assertThat(loadSqlKeywords(conn.getMetaData()))
                            .containsExactlyElementsOf(latestKeywords);
                }
            }
        }
    }

    @Test
    public void testLoadExtraNameCharacters() throws SQLException {
        assertThatNullPointerException().isThrownBy(() -> loadExtraNameCharacters(null));

        for (InMemoryDriver driver : InMemoryDriver.values()) {
            try (Connection conn = driver.getConnection()) {
                assertThat(loadExtraNameCharacters(conn.getMetaData()))
                        .isEqualTo(driver.getExtraNameCharacters());
            }
        }
    }

    @Test
    public void testGetSpecificKeywords() {
        assertThat(getSpecificKeywords("")).isEmpty();
        assertThat(getSpecificKeywords("hello")).containsExactly("HELLO");
        assertThat(getSpecificKeywords("hello,world")).containsExactly("HELLO", "WORLD");
        assertThat(getSpecificKeywords(" hello , world ")).containsExactly("HELLO", "WORLD");
        assertThat(getSpecificKeywords(" hello , ")).containsExactly("HELLO");
        assertThat(getSpecificKeywords(" , world")).containsExactly("WORLD");
    }

    @Test
    public void testQuote() throws SQLException {
        SqlIdentifierQuoter x = SqlIdentifierQuoter
                .builder()
                .quoteString("'")
                .clearSqlKeywords()
                .sqlKeyword("SELECT")
                .extraNameCharacters("+#")
                .build();

        assertThatNullPointerException().isThrownBy(() -> x.quote(null));
        assertThatNullPointerException().isThrownBy(() -> x.quote(null, true));

        assertThat(x.quote("abc")).isEqualTo("abc");
        assertThat(x.quote("abc", false)).isEqualTo("abc");
        assertThat(x.quote("abc", true)).isEqualTo("'abc'");

        assertThat(x.quote("a'bc")).isEqualTo("'a''bc'");
        assertThat(x.quote("a'bc", false)).isEqualTo("'a''bc'");
        assertThat(x.quote("a'bc", true)).isEqualTo("'a''bc'");

        assertThat(x.quote("a''bc")).isEqualTo("'a''''bc'");
        assertThat(x.quote("a''bc", false)).isEqualTo("'a''''bc'");
        assertThat(x.quote("a''bc", true)).isEqualTo("'a''''bc'");

        assertThat(x.quote("a'b'c")).isEqualTo("'a''b''c'");
        assertThat(x.quote("a'b'c", false)).isEqualTo("'a''b''c'");
        assertThat(x.quote("a'b'c", true)).isEqualTo("'a''b''c'");

        assertThat(x.quote("a bc")).isEqualTo("'a bc'");
        assertThat(x.quote("a bc", false)).isEqualTo("'a bc'");
        assertThat(x.quote("a bc", true)).isEqualTo("'a bc'");

        assertThat(x.quote("'abc'")).isEqualTo("'abc'");
        assertThat(x.quote("'abc'", false)).isEqualTo("'abc'");
        assertThat(x.quote("'abc'", true)).isEqualTo("'abc'");

        assertThat(x.quote("'a''bc'")).isEqualTo("'a''bc'");
        assertThat(x.quote("'a''bc'", false)).isEqualTo("'a''bc'");
        assertThat(x.quote("'a''bc'", true)).isEqualTo("'a''bc'");

        assertThat(x.quote("'a'bc'")).isEqualTo("'''a''bc'''");
        assertThat(x.quote("'a'bc'", false)).isEqualTo("'''a''bc'''");
        assertThat(x.quote("'a'bc'", true)).isEqualTo("'''a''bc'''");

        assertThat(x.quote("'a'b'c'")).isEqualTo("'''a''b''c'''");
        assertThat(x.quote("'a'b'c'", false)).isEqualTo("'''a''b''c'''");
        assertThat(x.quote("'a'b'c'", true)).isEqualTo("'''a''b''c'''");

        assertThat(x.quote("''''")).isEqualTo("''''");
        assertThat(x.quote("''''", false)).isEqualTo("''''");
        assertThat(x.quote("''''", true)).isEqualTo("''''");

        assertThat(x.quote("'''")).isEqualTo("''''''''");
        assertThat(x.quote("'''", false)).isEqualTo("''''''''");
        assertThat(x.quote("'''", true)).isEqualTo("''''''''");

        assertThat(x.quote("''")).isEqualTo("''");
        assertThat(x.quote("''", false)).isEqualTo("''");
        assertThat(x.quote("''", true)).isEqualTo("''");

        assertThat(x.quote("'")).isEqualTo("''''");
        assertThat(x.quote("'", false)).isEqualTo("''''");
        assertThat(x.quote("'", true)).isEqualTo("''''");

        assertThat(x.quote("")).isEqualTo("");
        assertThat(x.quote("", false)).isEqualTo("");
        assertThat(x.quote("", true)).isEqualTo("''");

        assertThat(x.quote("select")).isEqualTo("'select'");
        assertThat(x.quote("select", false)).isEqualTo("'select'");
        assertThat(x.quote("select", true)).isEqualTo("'select'");

        assertThat(x.quote("SELECT")).isEqualTo("'SELECT'");
        assertThat(x.quote("SELECT", false)).isEqualTo("'SELECT'");
        assertThat(x.quote("SELECT", true)).isEqualTo("'SELECT'");

        assertThat(x.quote("a+bc")).isEqualTo("'a+bc'");
        assertThat(x.quote("a+bc", false)).isEqualTo("'a+bc'");
        assertThat(x.quote("a+bc", true)).isEqualTo("'a+bc'");

        assertThat(x.quote("a#bc")).isEqualTo("'a#bc'");
        assertThat(x.quote("a#bc", false)).isEqualTo("'a#bc'");
        assertThat(x.quote("a#bc", true)).isEqualTo("'a#bc'");

        assertThat(x.quote("a bc")).isEqualTo("'a bc'");
        assertThat(x.quote("a bc", false)).isEqualTo("'a bc'");
        assertThat(x.quote("a bc", true)).isEqualTo("'a bc'");
    }

    private final Set<String> latestKeywords = SqlKeywords.LATEST_RESERVED_WORDS.getKeywords();
}
