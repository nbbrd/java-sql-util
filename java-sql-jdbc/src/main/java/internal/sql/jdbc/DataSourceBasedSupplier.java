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
import java.util.Objects;
import javax.sql.DataSource;
import nbbrd.sql.jdbc.SqlConnectionSupplier;
import nbbrd.sql.jdbc.SqlFunc;
import lombok.NonNull;

/**
 * A connection supplier that uses {@link javax.sql.DataSource}.
 */
@lombok.AllArgsConstructor
public final class DataSourceBasedSupplier implements SqlConnectionSupplier {

    @lombok.NonNull
    private final SqlFunc<String, javax.sql.DataSource> toDataSource;

    @Override
    public @NonNull Connection getConnection(@NonNull String connectionString) throws SQLException {
        Objects.requireNonNull(connectionString);
        return toDataSource.andThen(DataSource::getConnection).applyWithSql(connectionString);
    }
}
