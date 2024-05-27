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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import nbbrd.sql.jdbc.SqlConnectionSupplier;
import nbbrd.sql.jdbc.SqlFunc;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A connection supplier that uses {@link DriverManager}.
 */
@lombok.AllArgsConstructor
public final class DriverManagerSupplier implements SqlConnectionSupplier {

    @lombok.NonNull
    private final String driverClassName;

    @lombok.NonNull
    private final SqlFunc<String, String> toUrl;

    @Override
    public @NonNull Connection getConnection(@NonNull String connectionString) throws SQLException {
        Objects.requireNonNull(connectionString);
        if (!SqlConnectionSupplier.isDriverLoadable(driverClassName)) {
            throw new SQLException("Can't load jdbc driver '" + driverClassName + "'");
        }
        return toUrl.andThen(DriverManager::getConnection).applyWithSql(connectionString);
    }
}
