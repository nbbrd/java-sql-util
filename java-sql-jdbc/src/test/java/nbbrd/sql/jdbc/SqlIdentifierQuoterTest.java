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
import java.sql.DriverManager;
import java.sql.SQLException;
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

        try (Connection conn = getHsqldbConnection()) {
            assertThat(SqlIdentifierQuoter.of(conn.getMetaData()))
                    .isEqualTo(SqlIdentifierQuoter.builder().sqlKeywords(SqlKeywords.LATEST_RESERVED_WORDS.getKeywords()).build());
        }
    }

    @Test
    public void testLoadIdentifierQuoteString() throws SQLException {
        assertThatNullPointerException().isThrownBy(() -> loadIdentifierQuoteString(null));

        try (Connection conn = getHsqldbConnection()) {
            assertThat(loadIdentifierQuoteString(conn.getMetaData()))
                    .isEqualTo("\"");
        }
    }

    @Test
    public void testLoadSqlKeywords() throws SQLException {
        assertThatNullPointerException().isThrownBy(() -> loadSqlKeywords(null));

        try (Connection conn = getHsqldbConnection()) {
            assertThat(loadSqlKeywords(conn.getMetaData()))
                    .containsAll(SqlKeywords.LATEST_RESERVED_WORDS.getKeywords())
                    .doesNotContain("");
        }
    }

    @Test
    public void testLoadExtraNameCharacters() throws SQLException {
        assertThatNullPointerException().isThrownBy(() -> loadExtraNameCharacters(null));

        try (Connection conn = getHsqldbConnection()) {
            assertThat(loadExtraNameCharacters(conn.getMetaData()))
                    .isEmpty();
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

    private static Connection getHsqldbConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:mem:test");
    }
}
