/*
 * Copyright 2019 National Bank of Belgium
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

import nbbrd.sql.jdbc.SqlFunc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static _test.SQLExceptions.*;
import static internal.sql.lhod.Resources.CONN_STRING;
import static internal.sql.lhod.Resources.SQL_PREP_STMT_QUERY;
import static org.assertj.core.api.Assertions.*;

;

/**
 * @author Philippe Charles
 */
public class LhodPreparedStatementTest {

    private LhodConnection good, bad, ugly, err, closed;

    @BeforeEach
    public void before() {
        good = LhodConnection.of(Resources.goodExecutor(), CONN_STRING);
        bad = LhodConnection.of(Resources.badExecutor(), CONN_STRING);
        ugly = LhodConnection.of(Resources.uglyExecutor(), CONN_STRING);
        err = LhodConnection.of(Resources.errExecutor(), CONN_STRING);
        closed = LhodConnection.of(Resources.closedExecutor(), CONN_STRING);
    }

    @AfterEach
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
        assertThat(LhodPreparedStatement.of(good, SQL_PREP_STMT_QUERY))
                .as("Factory must return a non-null DataBaseMetaData")
                .isNotNull();

        assertThatThrownBy(() -> LhodPreparedStatement.of(null, SQL_PREP_STMT_QUERY))
                .as("Factory must throw NullPointerException if connection is null")
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> LhodPreparedStatement.of(good, null))
                .as("Factory must throw NullPointerException if query is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testSetString() throws SQLException {
        testCloseException("setString", stmt -> {
            stmt.setString(1, "Firefox");
            return null;
        });

        try (LhodPreparedStatement stmt = LhodPreparedStatement.of(good, SQL_PREP_STMT_QUERY)) {
            assertThatSQLException()
                    .isThrownBy(() -> stmt.setString(0, "hello"));

            assertThatSQLException()
                    .isThrownBy(() -> stmt.setString(-1, "hello"));

            stmt.setString(1, "first");
            stmt.setString(2, "second");
            assertThat(stmt.getParameterList())
                    .containsExactly("first", "second");

            stmt.setString(2, "other");
            assertThat(stmt.getParameterList())
                    .containsExactly("first", "other");
        }
    }

    @Test
    public void testExecuteQuery() throws SQLException {
        testAllExceptions("executeQuery", stmt -> {
            stmt.setString(1, "Firefox");
            return stmt.executeQuery();
        });

        try (LhodPreparedStatement stmt = LhodPreparedStatement.of(good, SQL_PREP_STMT_QUERY)) {
            stmt.setString(1, "Firefox");
            try (ResultSet rs = stmt.executeQuery()) {
                int index = 0;
                while (rs.next()) {
                    switch (index++) {
                        case 0:
                            assertThat(rs.getString(3)).isEqualTo("7/1/2008");
                            break;
                        case 329:
                            assertThat(rs.getString(3)).isEqualTo("10/1/2011");
                            break;
                    }
                }
                assertThat(index).isEqualTo(55);
            }
        }
    }

    @Test
    public void testIsClosed() throws SQLException {
        try (LhodPreparedStatement closeable = LhodPreparedStatement.of(good, SQL_PREP_STMT_QUERY)) {
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
    public void testClose() throws SQLException {
        try (LhodConnection resource = LhodConnection.of(Resources.goodExecutor(), CONN_STRING)) {
            try (LhodPreparedStatement closeable = LhodPreparedStatement.of(resource, SQL_PREP_STMT_QUERY)) {
            }
            assertThat(resource.isClosed())
                    .as("Close event must not be propagated to connection")
                    .isFalse();
        }

        try (LhodPreparedStatement closeable = LhodPreparedStatement.of(good, SQL_PREP_STMT_QUERY)) {
            assertThatCode(closeable::close)
                    .as("First close does not throw exception")
                    .doesNotThrowAnyException();
            assertThatCode(closeable::close)
                    .as("Subsequent close does not throw exception")
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testGetConnection() throws SQLException {
        testCloseException("close", LhodPreparedStatement::getConnection);

        assertThat(LhodPreparedStatement.of(good, SQL_PREP_STMT_QUERY).getConnection()).isEqualTo(good);
    }

    private void testCloseException(String methodName, SqlFunc<LhodPreparedStatement, ?> method) {
        assertThatSQLException()
                .as("%s must throw SQLException if called on a closed connection", methodName)
                .isThrownBy(() -> method.applyWithSql(LhodPreparedStatement.of(closed, SQL_PREP_STMT_QUERY)))
                .withMessageContaining(closed.getConnectionString())
                .satisfies(withoutErrorCode());
    }

    private void testAllExceptions(String methodName, SqlFunc<LhodPreparedStatement, ?> method) {
        assertThatCode(() -> method.applyWithSql(LhodPreparedStatement.of(good, SQL_PREP_STMT_QUERY)))
                .doesNotThrowAnyException();

        assertThatSQLException()
                .as("%s must throw SQLException if IOException is raised", methodName)
                .isThrownBy(() -> method.applyWithSql(LhodPreparedStatement.of(bad, SQL_PREP_STMT_QUERY)))
                .withMessageContaining(bad.getConnectionString())
                .withCauseInstanceOf(Resources.ExecIOException.class)
                .satisfies(withoutErrorCode());

        assertThatSQLException()
                .as("%s must throw SQLException if content is invalid", methodName)
                .isThrownBy(() -> method.applyWithSql(LhodPreparedStatement.of(ugly, SQL_PREP_STMT_QUERY)))
                .withMessageContaining(ugly.getConnectionString())
                .withCauseInstanceOf(IOException.class)
                .satisfies(withoutErrorCode());

        assertThatSQLException()
                .as("%s must throw SQLException if underlying exception is raised", methodName)
                .isThrownBy(() -> method.applyWithSql(LhodPreparedStatement.of(err, SQL_PREP_STMT_QUERY)))
                .withMessageContaining("name not found")
                .withNoCause()
                .satisfies(withErrorCode(-2147467259));

        testCloseException(methodName, method);
    }
}
