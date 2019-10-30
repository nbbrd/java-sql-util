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
import static java.lang.String.format;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class LhodStatement extends _Statement {

    @lombok.NonNull
    private final LhodConnection conn;

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            return LhodResultSet.of(conn.getContext().preparedStatement(sql, Collections.emptyList()));
        } catch (IOException ex) {
            throw ex instanceof TabularDataError
                    ? new SQLException(ex.getMessage(), "", ((TabularDataError) ex).getNumber())
                    : new SQLException(format("Failed to execute query '%s'", sql), ex);
        }
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public Connection getConnection() throws SQLException {
        return conn;
    }
}
