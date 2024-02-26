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
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class LhodPreparedStatement extends _PreparedStatement {

    @lombok.NonNull
    private final LhodConnection conn;

    @lombok.NonNull
    private final String sql;

    private final Map<Integer, String> parameters = new HashMap<>();

    private boolean closed = false;

    @Override
    public ResultSet executeQuery() throws SQLException {
        checkState();

        TabDataQuery query = TabDataQuery
                .builder()
                .procedure("PreparedStatement")
                .parameter(conn.getConnectionString())
                .parameter(sql)
                .parameters(getParameterList())
                .build();

        try {
            return LhodResultSet.of(conn.exec(query));
        } catch (IOException ex) {
            throw ex instanceof TabDataRemoteError
                    ? new SQLException(ex.getMessage(), "", ((TabDataRemoteError) ex).getNumber())
                    : new SQLException(format(Locale.ROOT, "Failed to execute query '%s'", sql), ex);
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
    public void close() throws SQLException {
        closed = true;
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        checkState();
        chechParameterIndex(parameterIndex);
        parameters.put(parameterIndex, x);
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkState();
        return conn;
    }

    List<String> getParameterList() {
        return parameters
                .entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private void checkState() throws SQLException {
        conn.checkState();
        if (closed) {
            throw new SQLException("PreparedStatement closed");
        }
    }

    private void chechParameterIndex(int parameterIndex) throws SQLException {
        if (parameterIndex < 1) {
            throw new SQLException("Parameter index out of bounds: " + parameterIndex);
        }
    }
}
