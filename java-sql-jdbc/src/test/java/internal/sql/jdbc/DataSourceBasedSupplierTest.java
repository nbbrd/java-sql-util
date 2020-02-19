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
package internal.sql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DataSourceBasedSupplierTest {

    @Test
    public void testGetConnection() {
        DataSourceBasedSupplier x = new DataSourceBasedSupplier(connectionString -> newDataSource("jdbc:hsqldb:" + connectionString));

        assertThatNullPointerException()
                .isThrownBy(() -> x.getConnection(null));

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> x.getConnection(""));

        assertThatCode(() -> {
            try (Connection conn = x.getConnection("mem:test")) {
            }
        }).doesNotThrowAnyException();
    }

    static JDBCDataSource newDataSource(String database) {
        JDBCDataSource result = new JDBCDataSource();
        result.setDatabase(database);
        return result;
    }
}
