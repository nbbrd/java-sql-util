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
import java.util.Objects;
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
public final class FailsafeOdbcConnectionSupplier implements OdbcConnectionSupplierSpi {

    public static OdbcConnectionSupplierSpi wrap(OdbcConnectionSupplierSpi delegate) {
        return new FailsafeOdbcConnectionSupplier(
                delegate,
                FailsafeOdbcConnectionSupplier::logUnexpectedError,
                FailsafeOdbcConnectionSupplier::logUnexpectedNull
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
            String msg = getUnexpectedErrorMsg("getName");
            onUnexpectedError.accept(msg, unexpected);
            return getId();
        }

        if (result == null) {
            String msg = getUnexpectedNullMsg("getName");
            onUnexpectedNull.accept(msg);
            return getId();
        }

        return result;
    }

    @Override
    public boolean isAvailable() {
        try {
            return delegate.isAvailable();
        } catch (RuntimeException unexpected) {
            String msg = getUnexpectedErrorMsg("isAvailable");
            onUnexpectedError.accept(msg, unexpected);
            return false;
        }
    }

    @Override
    public int getCost() {
        try {
            return delegate.getCost();
        } catch (RuntimeException unexpected) {
            String msg = getUnexpectedErrorMsg("getCost");
            onUnexpectedError.accept(msg, unexpected);
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public Connection getConnection(String connectionString) throws SQLException {
        Objects.requireNonNull(connectionString);

        Connection result;

        try {
            result = delegate.getConnection(connectionString);
        } catch (RuntimeException unexpected) {
            String msg = getUnexpectedErrorMsg("getConnection");
            onUnexpectedError.accept(msg, unexpected);
            throw new SQLException(msg, unexpected);
        }

        if (result == null) {
            String msg = getUnexpectedNullMsg("getConnection");
            onUnexpectedNull.accept(msg);
            throw new SQLException(msg);
        }

        return result;
    }

    private String getId() {
        return delegate.getClass().getName();
    }

    private String getUnexpectedErrorMsg(String method) {
        return "Unexpected error while calling '" + method + "' on '" + getId() + "'";
    }

    private String getUnexpectedNullMsg(String method) {
        return "Unexpected null while calling '" + method + "' on '" + getId() + "'";
    }

    private static void logUnexpectedError(String msg, RuntimeException ex) {
        log.log(Level.WARNING, msg, ex);
    }

    private static void logUnexpectedNull(String msg) {
        log.log(Level.WARNING, msg);
    }
}
