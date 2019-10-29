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
package internal.sql.odbc;

import internal.sql.odbc.win.RegOdbcRegistry;
import internal.sys.OS;
import java.util.ServiceLoader;
import nbbrd.sql.odbc.OdbcRegistrySpi;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class OdbcRegistrySpiLoaderTest {

    @Test
    public void testLoad() {
        if (OS.NAME == OS.Name.WINDOWS) {
            assertThat(OdbcRegistrySpiLoader.load())
                    .isPresent()
                    .get()
                    .isInstanceOf(RegOdbcRegistry.class);
        } else {
            assertThat(OdbcRegistrySpiLoader.load()).isEmpty();
        }
    }

    @Test
    public void testSource() {
        assertThat(ServiceLoader.load(OdbcRegistrySpi.class))
                .hasSize(1)
                .hasOnlyElementsOfType(RegOdbcRegistry.class);

    }
}
