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
import static internal.sql.lhod.LhodConnection.of;
import static internal.sql.lhod.Resources.CONN_STRING;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import java.io.IOException;
import java.util.stream.Stream;
import nbbrd.sql.jdbc.SqlFunc;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Philippe Charles
 */
public class LhodConnectionTest {

    private TabDataExecutor good, bad, ugly, err, closed;

    @Before
    public void before() {
        good = Resources.goodExecutor();
        bad = Resources.badExecutor();
        ugly = Resources.uglyExecutor();
        err = Resources.errExecutor();
        closed = Resources.closedExecutor();
    }

    @After
    public void after() {
        Stream.of(good, bad, ugly, err, closed)
                .forEach(conn -> {
                    try {
                        conn.close();
                    } catch (IOException ex) {
                    }
                });
    }

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThatNullPointerException()
                .as("Factory must throw NullPointerException if executor is null")
                .isThrownBy(() -> of(null, CONN_STRING));

        assertThatNullPointerException()
                .as("Factory must throw NullPointerException if connectionString is null")
                .isThrownBy(() -> of(Resources.goodExecutor(), null));
    }

    @Test
    public void testIsClosed() throws SQLException, IOException {
        try (LhodConnection closeable = of(good, CONN_STRING)) {
            assertThat(closeable.isClosed())
                    .as("IsClosed must return false if close() method not called")
                    .isFalse();
            closeable.close();
            assertThat(closeable.isClosed())
                    .as("IsClosed must return true if close() method has been called")
                    .isTrue();
        }
    }

    @Test
    public void testClose() throws SQLException, IOException {
        try (TabDataExecutor resource = Resources.goodExecutor()) {
            try (LhodConnection closeable = of(resource, CONN_STRING)) {
            }
            assertThat(resource.isClosed())
                    .as("Close event must be propagated to executor")
                    .isEqualTo(true);
        }

        try (LhodConnection closeable = of(good, CONN_STRING)) {
            assertThatCode(closeable::close)
                    .as("First close does not throw exception")
                    .doesNotThrowAnyException();
            assertThatCode(closeable::close)
                    .as("Subsequent close does not throw exception")
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testGetMetaData() throws SQLException {
        testCloseException("getMetaData", LhodConnection::getMetaData);

        assertThat(of(good, CONN_STRING).getMetaData())
                .as("MetaData must be non-null")
                .isNotNull();
    }

    @Test
    public void testGetCatalog() throws SQLException {
        testAllExceptions("getCatalog", LhodConnection::getCatalog);

        assertThat(of(good, CONN_STRING).getCatalog())
                .as("Catalog must return expected value")
                .isEqualTo("C:\\Temp\\Top5-Table.mdb");
    }

    @Test
    public void testGetSchema() throws SQLException {
        testCloseException("getSchema", LhodConnection::getSchema);
    }

    @Test
    public void testCreateStatement() throws SQLException {
        testCloseException("createStatement", LhodConnection::createStatement);

        assertThat(of(good, CONN_STRING).createStatement())
                .as("Statement must be non-null")
                .isNotNull();
    }

    @Test
    public void testPrepareStatement() throws SQLException {
        testCloseException("prepareStatement", conn -> conn.prepareStatement(""));

        assertThat(of(good, CONN_STRING).prepareStatement(""))
                .as("PreparedStatement must be non-null")
                .isNotNull();
    }

    @Test
    public void testIsReadOnly() throws SQLException {
        testCloseException("isReadOnly", LhodConnection::isReadOnly);

        assertThat(of(good, CONN_STRING).isReadOnly())
                .as("ReadOnly must return expected value")
                .isEqualTo(true);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetProperty() throws IOException, SQLException {
        assertThatNullPointerException()
                .isThrownBy(() -> of(good, CONN_STRING).getProperty(null));

        assertThat(of(good, CONN_STRING).getProperty(LhodConnection.DynamicProperty.CURRENT_CATALOG))
                .isEqualTo("C:\\Temp\\Top5-Table.mdb");

        assertThat(of(good, CONN_STRING).getProperty(LhodConnection.DynamicProperty.SPECIAL_CHARACTERS))
                .isNotEmpty();

        assertThat(of(good, CONN_STRING).getProperty(LhodConnection.DynamicProperty.IDENTIFIER_CASE_SENSITIVITY))
                .isEqualTo("4");

        assertThat(of(good, CONN_STRING).getProperty(LhodConnection.DynamicProperty.STRING_FUNCTIONS))
                .isEqualTo("360061");

        assertThatIOException()
                .isThrownBy(() -> of(bad, CONN_STRING).getProperty(LhodConnection.DynamicProperty.CURRENT_CATALOG))
                .isInstanceOf(Resources.ExecIOException.class);

        assertThatIOException()
                .isThrownBy(() -> of(ugly, CONN_STRING).getProperty(LhodConnection.DynamicProperty.CURRENT_CATALOG));

        assertThatIOException()
                .isThrownBy(() -> of(err, CONN_STRING).getProperty(LhodConnection.DynamicProperty.CURRENT_CATALOG))
                .isInstanceOf(TabDataRemoteError.class);
    }

    private void testCloseException(String methodName, SqlFunc<LhodConnection, ?> method) {
        assertThatSQLException()
                .as("%s must throw SQLException if called on a closed executor", methodName)
                .isThrownBy(() -> method.applyWithSql(of(closed, CONN_STRING)))
                .withMessageContaining(CONN_STRING)
                .satisfies(withoutErrorCode());
    }

    private void testAllExceptions(String methodName, SqlFunc<LhodConnection, ?> method) {
        assertThatCode(() -> method.applyWithSql(of(good, CONN_STRING)))
                .doesNotThrowAnyException();

        assertThatSQLException()
                .as("%s must throw SQLException if IOException is raised", methodName)
                .isThrownBy(() -> method.applyWithSql(of(bad, CONN_STRING)))
                .withMessageContaining(CONN_STRING)
                .withCauseInstanceOf(Resources.ExecIOException.class)
                .satisfies(withoutErrorCode());

        assertThatSQLException()
                .as("%s must throw SQLException if content is invalid", methodName)
                .isThrownBy(() -> method.applyWithSql(of(ugly, CONN_STRING)))
                .withMessageContaining(CONN_STRING)
                .withCauseInstanceOf(IOException.class)
                .satisfies(withoutErrorCode());

        assertThatSQLException()
                .as("%s must throw SQLException if underlying exception is raised", methodName)
                .isThrownBy(() -> method.applyWithSql(of(err, CONN_STRING)))
                .withMessageContaining("name not found")
                .withNoCause()
                .satisfies(withErrorCode(-2147467259));

        testCloseException(methodName, method);
    }
}
