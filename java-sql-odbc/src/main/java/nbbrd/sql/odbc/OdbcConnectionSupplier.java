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
package nbbrd.sql.odbc;

import internal.sql.odbc.FailsafeOdbcConnectionSupplier;
import internal.sql.odbc.OdbcConnectionSupplierSpiLoader;
import nbbrd.sql.jdbc.SqlConnectionSupplier;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class OdbcConnectionSupplier implements SqlConnectionSupplier {

    public static @NonNull Optional<OdbcConnectionSupplier> ofServiceLoader() {
        return OdbcConnectionSupplierSpiLoader.load()
                .map(FailsafeOdbcConnectionSupplier::wrap)
                .map(OdbcConnectionSupplier::new);
    }

    @lombok.NonNull
    private final OdbcConnectionSupplierSpi spi;


    public @NonNull String getName() {
        return spi.getName();
    }

    @Override
    public @NonNull Connection getConnection(@NonNull String connectionString) throws SQLException {
        return spi.getConnection(connectionString);
    }

    @NonNull
    public Connection getConnection(@NonNull OdbcConnectionString connectionString) throws SQLException {
        return spi.getConnection(connectionString.toString());
    }
}
