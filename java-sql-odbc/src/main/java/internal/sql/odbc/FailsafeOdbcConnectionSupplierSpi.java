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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import nbbrd.sql.odbc.OdbcConnectionSupplierSpi;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@lombok.AllArgsConstructor
public final class FailsafeOdbcConnectionSupplierSpi implements OdbcConnectionSupplierSpi {

    public static OdbcConnectionSupplierSpi wrap(OdbcConnectionSupplierSpi delegate) {
        return new FailsafeOdbcConnectionSupplierSpi(
                delegate,
                FailsafeOdbcConnectionSupplierSpi::logUnexpectedError,
                FailsafeOdbcConnectionSupplierSpi::logUnexpectedNull
        );
    }

    @lombok.NonNull
    private final OdbcConnectionSupplierSpi delegate;

    @lombok.NonNull
    private final BiConsumer<? super String, ? super RuntimeException> onUnexpectedError;

    @lombok.NonNull
    private final Consumer<? super String> onUnexpectedNull;

    @Override
    public String getName() {
        String result;

        try {
            result = delegate.getName();
        } catch (RuntimeException unexpected) {
            String msg = "Unexpected error while calling 'getName' on '" + delegate + "'";
            onUnexpectedError.accept(msg, unexpected);
            return delegate.getClass().getName();
        }

        if (result == null) {
            String msg = "Unexpected null while calling 'getName' on '" + delegate + "'";
            onUnexpectedNull.accept(msg);
            return delegate.getClass().getName();
        }

        return result;
    }

    @Override
    public boolean isAvailable() {
        try {
            return delegate.isAvailable();
        } catch (RuntimeException unexpected) {
            String msg = "Unexpected error while calling 'isAvailable' on '" + delegate + "'";
            onUnexpectedError.accept(msg, unexpected);
            return false;
        }
    }

    @Override
    public int getCost() {
        try {
            return delegate.getCost();
        } catch (RuntimeException unexpected) {
            String msg = "Unexpected error while calling 'getCost' on '" + delegate + "'";
            onUnexpectedError.accept(msg, unexpected);
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public Connection getConnection(String connectionString) throws SQLException {
        Connection result;

        try {
            result = delegate.getConnection(connectionString);
        } catch (RuntimeException unexpected) {
            String msg = "Unexpected error while getting connection' for '" + connectionString + "'";
            onUnexpectedError.accept(msg, unexpected);
            throw new SQLException(msg, unexpected);
        }

        if (result == null) {
            String msg = "Unexpected null while getting connection' for '" + connectionString + "'";
            onUnexpectedNull.accept(msg);
            throw new SQLException(msg);
        }

        return result;
    }

    private static void logUnexpectedError(String msg, RuntimeException ex) {
        log.log(Level.WARNING, msg, ex);
    }

    private static void logUnexpectedNull(String msg) {
        log.log(Level.WARNING, msg);
    }
}
