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
import static org.assertj.core.api.Assertions.*;
import org.hsqldb.jdbc.JDBCDriver;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DriverManagerSupplierTest {
    
    @Test
    public void testGetConnection() {
        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> new DriverManagerSupplier("abc", connectionString -> "jdbc:hsqldb:" + connectionString).getConnection("mem:test"))
                .withMessageContaining("abc");
        
        DriverManagerSupplier x = new DriverManagerSupplier(JDBCDriver.class.getName(), o -> "jdbc:hsqldb:" + o);
        
        assertThatNullPointerException()
                .isThrownBy(() -> x.getConnection(null));
        
        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> x.getConnection(""));
        
        assertThatCode(() -> {
            try (Connection conn = x.getConnection("mem:test")) {
            }
        }).doesNotThrowAnyException();
    }
}
