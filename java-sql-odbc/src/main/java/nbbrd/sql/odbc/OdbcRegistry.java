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

import internal.sql.odbc.FailsafeOdbcRegistry;
import internal.sql.odbc.OdbcRegistrySpiLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author Philippe Charles
 * @see http://msdn.microsoft.com/en-us/library/windows/desktop/ms715432(v=vs.85).aspx
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class OdbcRegistry {

    public static @NonNull Optional<OdbcRegistry> ofServiceLoader() {
        return OdbcRegistrySpiLoader.load()
                .map(FailsafeOdbcRegistry::wrap)
                .map(OdbcRegistry::new);
    }

    @lombok.NonNull
    private final OdbcRegistrySpi spi;

    public @NonNull String getName() {
        return spi.getName();
    }

    public @NonNull List<String> getDataSourceNames(OdbcDataSource.@NonNull Type... types) throws IOException {
        return spi.getDataSourceNames(types);
    }

    public @NonNull List<OdbcDataSource> getDataSources(OdbcDataSource.@NonNull Type... types) throws IOException {
        return spi.getDataSources(types);
    }

    public @NonNull List<String> getDriverNames() throws IOException {
        return spi.getDriverNames();
    }

    public @NonNull List<OdbcDriver> getDrivers() throws IOException {
        return spi.getDrivers();
    }
}
