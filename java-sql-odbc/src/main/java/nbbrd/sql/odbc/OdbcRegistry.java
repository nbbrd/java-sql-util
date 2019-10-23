/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import internal.sql.odbc.FailsafeOdbcRegistrySpi;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @see
 * http://msdn.microsoft.com/en-us/library/windows/desktop/ms715432(v=vs.85).aspx
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class OdbcRegistry {

    @NonNull
    public static Optional<OdbcRegistry> ofServiceLoader() {
        return OdbcRegistrySpiLoader.load()
                .map(FailsafeOdbcRegistrySpi::wrap)
                .map(OdbcRegistry::new);
    }

    @lombok.NonNull
    private final OdbcRegistrySpi spi;

    @NonNull
    public String getName() {
        return spi.getName();
    }

    @NonNull
    public List<OdbcDataSource> getDataSources(OdbcDataSource.@NonNull Type... types) throws IOException {
        return spi.getDataSources(types);
    }

    @NonNull
    public List<OdbcDriver> getDrivers() throws IOException {
        return spi.getDrivers();
    }
}
