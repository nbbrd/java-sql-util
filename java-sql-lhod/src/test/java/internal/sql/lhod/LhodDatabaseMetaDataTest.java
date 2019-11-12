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

import static _test.SQLExceptions.*;
import static internal.sql.lhod.LhodDatabaseMetaData.of;
import static internal.sql.lhod.Resources.CONN_STRING;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;
import nbbrd.sql.jdbc.SqlFunc;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class LhodDatabaseMetaDataTest {

    private LhodConnection good, bad, ugly, err, closed;

    @Before
    public void before() {
        good = LhodConnection.of(Resources.goodExecutor(), CONN_STRING);
        bad = LhodConnection.of(Resources.badExecutor(), CONN_STRING);
        ugly = LhodConnection.of(Resources.uglyExecutor(), CONN_STRING);
        err = LhodConnection.of(Resources.errExecutor(), CONN_STRING);
        closed = LhodConnection.of(Resources.closedExecutor(), CONN_STRING);
    }

    @After
    public void after() {
        Stream.of(good, bad, ugly, err, closed)
                .forEach(conn -> {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                    }
                });
    }

    @Test
    @SuppressWarnings("null")
    public void testFactory() throws SQLException {
        assertThat(of(good))
                .as("Factory must return a non-null DataBaseMetaData")
                .isNotNull();

        assertThatThrownBy(() -> of(null))
                .as("Factory must throw NullPointerException if connection is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testStoresUpperCaseIdentifiers() throws SQLException {
        testAllExceptions("storesUpperCaseIdentifiers", LhodDatabaseMetaData::storesUpperCaseIdentifiers);

        assertThat(of(good).storesUpperCaseIdentifiers()).isFalse();
    }

    @Test
    public void testStoresLowerCaseIdentifiers() throws SQLException {
        testAllExceptions("storesLowerCaseIdentifiers", LhodDatabaseMetaData::storesLowerCaseIdentifiers);

        assertThat(of(good).storesLowerCaseIdentifiers()).isFalse();
    }

    @Test
    public void testStoresMixedCaseIdentifiers() throws SQLException {
        testAllExceptions("storesMixedCaseIdentifiers", LhodDatabaseMetaData::storesMixedCaseIdentifiers);

        assertThat(of(good).storesMixedCaseIdentifiers()).isFalse();
    }

    @Test
    public void testGetIdentifierQuoteString() throws SQLException {
        testCloseException("getIdentifierQuoteString", LhodDatabaseMetaData::getIdentifierQuoteString);

        assertThat(of(good).getIdentifierQuoteString()).isNull();
    }

    @Test
    public void testGetSQLKeywords() throws SQLException {
        testCloseException("getSQLKeywords", LhodDatabaseMetaData::getSQLKeywords);

        assertThat(of(good).getSQLKeywords()).isNotNull();
    }

    @Test
    public void testGetStringFunctions() throws SQLException {
        testAllExceptions("getStringFunctions", LhodDatabaseMetaData::getStringFunctions);

        assertThat(of(good).getStringFunctions().split(",", -1))
                .as("StringFunctions must return expected value")
                .containsOnly(
                        "ASCII",
                        "CHAR",
                        "CONCAT",
                        "LCASE",
                        "LEFT",
                        "LENGTH",
                        "LOCATE",
                        "LOCATE_2",
                        "LTRIM",
                        "RIGHT",
                        "RTRIM",
                        "SPACE",
                        "SUBSTRING",
                        "UCASE");
    }

    @Test
    public void testGetExtraNameCharacters() throws SQLException {
        testAllExceptions("getExtraNameCharacters", LhodDatabaseMetaData::getExtraNameCharacters);

        assertThat(of(good).getExtraNameCharacters())
                .as("getExtraNameCharacters must return expected value")
                .isNotEmpty();
    }

    @Test
    public void testGetTables() throws SQLException {
        testAllExceptions("getTables", md -> md.getTables(null, null, null, null));

        try (ResultSet rs = of(good).getTables(null, null, null, null)) {
            int index = 0;
            while (rs.next()) {
                switch (index++) {
                    case 0:
                        assertThat(rs.getString(3)).isEqualTo("MSysAccessStorage");
                        break;
                }
            }
            assertThat(index).isEqualTo(15);
        }
    }

    @Test
    public void testGetConnection() throws SQLException {
        testCloseException("getConnection", LhodDatabaseMetaData::getConnection);

        assertThat(of(good).getConnection()).isEqualTo(good);
    }

    @Test
    public void testIsReadOnly() throws SQLException {
        testCloseException("isReadOnly", LhodDatabaseMetaData::isReadOnly);

        assertThat(of(good).isReadOnly()).isEqualTo(good.isReadOnly());
    }

    private void testCloseException(String methodName, SqlFunc<LhodDatabaseMetaData, ?> method) {
        assertThatSQLException()
                .as("%s must throw SQLException if called on a closed connection", methodName)
                .isThrownBy(() -> method.applyWithSql(of(closed)))
                .withMessageContaining(closed.getConnectionString())
                .satisfies(withoutErrorCode());
    }

    private void testAllExceptions(String methodName, SqlFunc<LhodDatabaseMetaData, ?> method) {
        assertThatCode(() -> method.applyWithSql(of(good)))
                .doesNotThrowAnyException();

        assertThatSQLException()
                .as("%s must throw SQLException if IOException is raised", methodName)
                .isThrownBy(() -> method.applyWithSql(of(bad)))
                .withMessageContaining(bad.getConnectionString())
                .withCauseInstanceOf(Resources.ExecIOException.class)
                .satisfies(withoutErrorCode());

        assertThatSQLException()
                .as("%s must throw SQLException if content is invalid", methodName)
                .isThrownBy(() -> method.applyWithSql(of(ugly)))
                .withMessageContaining(ugly.getConnectionString())
                .withCauseInstanceOf(IOException.class)
                .satisfies(withoutErrorCode());

        assertThatSQLException()
                .as("%s must throw SQLException if underlying exception is raised", methodName)
                .isThrownBy(() -> method.applyWithSql(of(err)))
                .withMessageContaining("name not found")
                .withNoCause()
                .satisfies(withErrorCode(-2147467259));

        testCloseException(methodName, method);
    }
}
