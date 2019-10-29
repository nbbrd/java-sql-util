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
package nbbrd.sql.odbc;

import internal.sys.OS;
import java.util.Optional;
import nbbrd.sql.odbc.OdbcDataSource.Type;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.Assumptions;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class OdbcRegistryTest {

    @Test
    public void test() {
        Assumptions.assumeThat(OS.NAME).isEqualTo(OS.Name.WINDOWS);

        Optional<OdbcRegistry> odbcReg = OdbcRegistry.ofServiceLoader();

        assertThat(odbcReg).isPresent();

        assertThatCode(() -> odbcReg.get().getName()).doesNotThrowAnyException();
        assertThatCode(() -> odbcReg.get().getDataSources(Type.SYSTEM, Type.USER)).doesNotThrowAnyException();
        assertThatCode(() -> odbcReg.get().getDrivers()).doesNotThrowAnyException();

        assertThatIOException()
                .isThrownBy(() -> odbcReg.get().getDataSources(Type.FILE))
                .withCauseInstanceOf(UnsupportedOperationException.class);
    }
}