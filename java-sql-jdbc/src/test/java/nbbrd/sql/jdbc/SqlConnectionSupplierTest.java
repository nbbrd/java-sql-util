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
package nbbrd.sql.jdbc;

import java.sql.Driver;
import static nbbrd.sql.jdbc.SqlConnectionSupplier.*;
import static org.assertj.core.api.Assertions.*;
import org.hsqldb.jdbc.JDBCDataSource;
import org.hsqldb.jdbc.JDBCDriver;
import org.junit.jupiter.api.Test;;

/**
 *
 * @author Philippe Charles
 */
public class SqlConnectionSupplierTest {

    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> ofDataSource(null));

        assertThat(ofDataSource(o -> new JDBCDataSource())).isNotNull();

        assertThatNullPointerException()
                .isThrownBy(() -> ofDriverManager(null, o -> o));

        assertThatNullPointerException()
                .isThrownBy(() -> ofDriverManager(Driver.class.getName(), null));

        assertThat(ofDriverManager(JDBCDriver.class.getName(), o -> o)).isNotNull();

        assertThat(ofJndi()).isNotNull();

        assertThat(noOp()).isNotNull();
    }

    @Test
    public void testIsDriverLoadable() {
        assertThatNullPointerException().isThrownBy(() -> isDriverLoadable(null));

        assertThat(isDriverLoadable("abc")).isFalse();
        assertThat(isDriverLoadable(String.class.getName())).isFalse();
        assertThat(isDriverLoadable(Driver.class.getName())).isTrue();
        assertThat(isDriverLoadable(JDBCDriver.class.getName())).isTrue();
    }

    @Test
    public void testIsDriverRegistered() {
        assertThatNullPointerException().isThrownBy(() -> isDriverRegistered(null));

        assertThat(isDriverRegistered("abc")).isFalse();
        assertThat(isDriverRegistered(String.class.getName())).isFalse();
        assertThat(isDriverRegistered(Driver.class.getName())).isFalse();
        assertThat(isDriverRegistered(JDBCDriver.class.getName())).isTrue();
    }
}
