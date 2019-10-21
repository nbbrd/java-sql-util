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
import nbbrd.sql.jdbc.SqlConnectionSupplier;

/**
 *
 * @author Philippe Charles
 */
public enum NoOpSupplier implements SqlConnectionSupplier {
    INSTANCE;

    @Override
    public Connection getConnection(String connectionString) throws SQLException {
        Objects.requireNonNull(connectionString);
        throw new SQLException("No connection for '" + connectionString + "'");
    }
}
