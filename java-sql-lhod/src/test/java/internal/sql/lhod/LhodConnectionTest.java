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

import _test.Excel;
import ec.util.spreadsheet.helpers.ArraySheet;
import internal.sql.lhod.ps.PsEngine;
import internal.sql.lhod.vbs.VbsEngine;
import nbbrd.sql.jdbc.SqlFunc;
import nbbrd.sql.jdbc.SqlTable;
import nbbrd.sql.odbc.OdbcConnectionString;
import nbbrd.sql.odbc.OdbcDriver;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static _test.SQLExceptions.*;
import static internal.sql.lhod.LhodConnection.of;
import static internal.sql.lhod.Resources.CONN_STRING;
import static org.assertj.core.api.Assertions.*;

;

/**
 * @author Philippe Charles
 */
public class LhodConnectionTest {

    private TabDataExecutor good, bad, ugly, err, closed;

    @BeforeEach
    public void before() {
        good = Resources.goodExecutor();
        bad = Resources.badExecutor();
        ugly = Resources.uglyExecutor();
        err = Resources.errExecutor();
        closed = Resources.closedExecutor();
    }

    @AfterEach
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

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void testRealConnection() throws IOException, SQLException {
        Optional<OdbcDriver> excelDriver = Excel.getDriver();
        Assumptions.assumeThat(excelDriver).isPresent();
        testExcelConnection(excelDriver.orElseThrow(RuntimeException::new));
    }

    private void testExcelConnection(OdbcDriver excelDriver) throws IOException, SQLException {
        ArraySheet table = ArraySheet.copyOf("test", new Object[][]{{"c1", "c2"}, {"v\n1", "v\"2"}});

        OdbcConnectionString connectionString = Excel.getConnectionString(excelDriver, Excel.createTempFile(table));

        for (TabDataEngine engine : new TabDataEngine[]{new VbsEngine(), new PsEngine()}) {
            try (TabDataExecutor executor = engine.getExecutor()) {
                try (Connection conn = of(executor, connectionString.toString())) {
                    assertThat(SqlTable.allOf(conn.getMetaData()))
                            .extracting(SqlTable::getName)
                            .containsExactly("test$");

                    try (Statement statement = conn.createStatement()) {
                        try (ResultSet resultSet = statement.executeQuery("select * from [test$]")) {
                            assertThat(getRows(resultSet))
                                    .hasSize(1)
                                    .element(0, ARRAY)
                                    .containsExactly("v\n1", "v\"2");
                        }
                    }
                }
            }
        }
    }

    private static List<Object[]> getRows(ResultSet resultSet) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        List<Object[]> rows = new ArrayList<>();
        while (resultSet.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 0; i < row.length; i++) {
                row[i] = resultSet.getObject(i + 1);
            }
            rows.add(row);
        }
        return rows;
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
