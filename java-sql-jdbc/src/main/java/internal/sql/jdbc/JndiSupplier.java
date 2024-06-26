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
import java.util.concurrent.Callable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import nbbrd.sql.jdbc.SqlConnectionSupplier;
import lombok.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class JndiSupplier implements SqlConnectionSupplier {

    @lombok.NonNull
    private final Callable<? extends Context> contextSupplier;

    @Override
    public @NonNull Connection getConnection(@NonNull String connectionString) throws SQLException {
        Objects.requireNonNull(connectionString);
        return lookupByName(connectionString).getConnection();
    }

    private DataSource lookupByName(String name) throws SQLException {
        try {
            Context ctx = getContext();
            return (DataSource) ctx.lookup(name);
        } catch (NamingException | ClassCastException ex) {
            throw new SQLException("Cannot retrieve javax.sql.DataSource for '" + name + "'", ex);
        }
    }

    private Context getContext() throws SQLException {
        try {
            return contextSupplier.call();
        } catch (Exception ex) {
            throw new SQLException("Cannot retrieve context", ex);
        }
    }
}
