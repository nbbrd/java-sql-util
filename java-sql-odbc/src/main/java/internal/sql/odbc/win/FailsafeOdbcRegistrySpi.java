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
package internal.sql.odbc.win;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcDriver;
import nbbrd.sql.odbc.OdbcRegistrySpi;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@lombok.AllArgsConstructor
public final class FailsafeOdbcRegistrySpi implements OdbcRegistrySpi {

    public static OdbcRegistrySpi wrap(OdbcRegistrySpi delegate) {
        return new FailsafeOdbcRegistrySpi(
                delegate,
                FailsafeOdbcRegistrySpi::logUnexpectedError,
                FailsafeOdbcRegistrySpi::logUnexpectedNull
        );
    }

    @lombok.NonNull
    private final OdbcRegistrySpi delegate;

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
    public List<OdbcDataSource> getDataSources(OdbcDataSource.Type[] types) throws IOException {
        List<OdbcDataSource> result;

        try {
            result = delegate.getDataSources(types);
        } catch (RuntimeException unexpected) {
            String msg = "Unexpected error while calling 'getDataSources' on '" + delegate + "'";
            onUnexpectedError.accept(msg, unexpected);
            throw new IOException(msg, unexpected);
        }

        if (result == null) {
            String msg = "Unexpected null while calling 'getDataSources' on '" + delegate + "'";
            onUnexpectedNull.accept(msg);
            throw new IOException(msg);
        }

        return result;
    }

    @Override
    public List<OdbcDriver> getDrivers() throws IOException {
        List<OdbcDriver> result;

        try {
            result = delegate.getDrivers();
        } catch (RuntimeException unexpected) {
            String msg = "Unexpected error while calling 'getDrivers' on '" + delegate + "'";
            onUnexpectedError.accept(msg, unexpected);
            throw new IOException(msg, unexpected);
        }

        if (result == null) {
            String msg = "Unexpected null while calling 'getDrivers' on '" + delegate + "'";
            onUnexpectedNull.accept(msg);
            throw new IOException(msg);
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
