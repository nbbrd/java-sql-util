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

import static _test.SQLExceptions.assertThatSQLException;
import static _test.SQLExceptions.withoutErrorCode;
import java.io.IOException;
import java.sql.SQLException;
import nbbrd.sql.jdbc.SqlFunc;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class LhodResultSetTest {

    private TabDataReader good, bad, ugly, err, closed;

    @Before
    public void before() throws IOException {
        good = TabDataReader.of(Resources.Sample.TOP5_STMT.newReader());
//        bad = Resources.badExecutor().exec(Resources.GOOD_STMT_QUERY);
//        ugly = Resources.uglyExecutor().exec(Resources.GOOD_STMT_QUERY);
//        err = Resources.errExecutor().exec(Resources.GOOD_STMT_QUERY);
        closed = TabDataReader.of(Resources.Sample.TOP5_STMT.newReader());
        closed.close();
    }

//    @After
//    public void after() {
//        Stream.of(good, bad, ugly, err, closed)
//                .forEach(conn -> {
//                    try {
//                        conn.close();
//                    } catch (IOException ex) {
//                    }
//                });
//    }
    @Test
    @SuppressWarnings("null")
    public void testFactory() throws IOException {
        assertThat(LhodResultSet.of(good))
                .as("Factory must return a non-null LhodResultSet")
                .isNotNull();

        assertThatThrownBy(() -> LhodResultSet.of(null))
                .as("Factory must throw NullPointerException if reader is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testIsClosed() throws IOException, SQLException {
        try (LhodResultSet closeable = LhodResultSet.of(good)) {
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
        try (TabDataReader resource = TabDataReader.of(Resources.Sample.TOP5_STMT.newReader())) {
            try (LhodResultSet closeable = LhodResultSet.of(resource)) {
            }
            assertThat(resource.isClosed())
                    .as("Close event must be propagated to resource")
                    .isTrue();
        }

        try (LhodResultSet rs = LhodResultSet.of(good)) {
            assertThatCode(rs::close)
                    .as("First close does not throw exception")
                    .doesNotThrowAnyException();
            assertThatCode(rs::close)
                    .as("Subsequent close does not throw exception")
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testNext() throws IOException, SQLException {
        testCloseException("next", LhodResultSet::next);

        try (LhodResultSet rs = LhodResultSet.of(good)) {
            int count = 0;
            while (rs.next()) {
                count++;
            }
            assertThat(count).isEqualTo(330);
        }
    }

    @Test
    public void testGetMetaData() throws IOException {
        testCloseException("getMetaData", LhodResultSet::getMetaData);

        assertThat(LhodResultSet.of(good)).isNotNull();
    }

    private void testCloseException(String methodName, SqlFunc<LhodResultSet, ?> method) {
        assertThatSQLException()
                .as("%s must throw SQLException if called on a closed reader", methodName)
                .isThrownBy(() -> method.applyWithSql(LhodResultSet.of(closed)))
                .satisfies(withoutErrorCode());
    }
}
