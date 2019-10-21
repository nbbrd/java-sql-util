/*
 * Copyright 2015 National Bank of Belgium
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

import internal.sql.jdbc.DataSourceBasedSupplier;
import internal.sql.jdbc.DriverManagerSupplier;
import internal.sql.jdbc.JndiSupplier;
import internal.sql.jdbc.NoOpSupplier;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A class that supplies opened connections to databases.
 *
 * @author Philippe Charles
 * @see http://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html
 */
public interface SqlConnectionSupplier {

    /**
     * Opens a connection to a database. The class that uses this connection
     * must close it after use.
     *
     * @param connectionString
     * @return A new opened connection.
     * @throws SQLException
     */
    @NonNull
    Connection getConnection(@NonNull String connectionString) throws SQLException;

    @NonNull
    static SqlConnectionSupplier ofDriverManager(@NonNull String driverClassName, @NonNull SqlFunc<String, String> toUrl) {
        return new DriverManagerSupplier(driverClassName, toUrl);
    }

    @NonNull
    static SqlConnectionSupplier ofDataSource(@NonNull SqlFunc<String, javax.sql.DataSource> toDataSource) {
        return new DataSourceBasedSupplier(toDataSource);
    }

    @NonNull
    static SqlConnectionSupplier ofJndi() {
        return JndiSupplier.INSTANCE;
    }

    @NonNull
    static SqlConnectionSupplier noOp() {
        return NoOpSupplier.INSTANCE;
    }

    static boolean isDriverLoadable(@NonNull String driverClassName) {
        try {
            return Driver.class.isAssignableFrom(Class.forName(driverClassName));
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    static boolean isDriverRegistered(@NonNull String driverClassName) {
        return Collections
                .list(DriverManager.getDrivers())
                .stream()
                .map(o -> o.getClass().getName())
                .anyMatch(driverClassName::equals);
    }
}
