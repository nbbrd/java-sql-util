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
package internal.sql.lhod;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.checkerframework.checker.nullness.qual.NonNull;
import static java.lang.String.format;
import java.util.function.Consumer;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class LhodConnection extends _Connection {

    @lombok.NonNull
    private final LhodContext context;

    @lombok.NonNull
    private final Consumer<LhodContext> onClose;

    private boolean closed = false;

    @Override
    public void close() throws SQLException {
        if (!closed) {
            onClose.accept(context);
            closed = true;
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        checkState();
        return LhodDatabaseMetaData.of(this);
    }

    @Override
    public String getCatalog() throws SQLException {
        checkState();
        try {
            return context.getProperty(LhodContext.DynamicProperty.CURRENT_CATALOG);
        } catch (IOException ex) {
            throw ex instanceof TabularDataError
                    ? new SQLException(ex.getMessage(), "", ((TabularDataError) ex).getNumber())
                    : new SQLException(format("Failed to get catalog name of '%s'", context.getConnectionString()), ex);
        }
    }

    @Override
    public String getSchema() throws SQLException {
        checkState();
        return null;
    }

    @Override
    public Statement createStatement() throws SQLException {
        checkState();
        return LhodStatement.of(this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkState();
        return LhodPreparedStatement.of(this, sql);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        checkState();
        return true;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    @NonNull
    LhodContext getContext() {
        return context;
    }

    private void checkState() throws SQLException {
        if (closed) {
            throw new SQLException(format("Connection '%s' closed", context.getConnectionString()));
        }
    }
}
