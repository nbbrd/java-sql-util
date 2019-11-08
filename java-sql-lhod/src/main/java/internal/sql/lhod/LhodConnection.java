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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class LhodConnection extends _Connection {

    @lombok.NonNull
    private final TabularDataExecutor executor;

    @lombok.Getter
    @lombok.NonNull
    private final String connectionString;

    private EnumMap<DynamicProperty, String> lazyProperties = null;

    @Override
    public boolean isClosed() throws SQLException {
        try {
            return executor.isClosed();
        } catch (IOException ex) {
            throw new SQLException("Failed to check executor state", ex);
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            executor.close();
        } catch (IOException ex) {
            throw new SQLException("Failed to close executor", ex);
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
            return getProperty(DynamicProperty.CURRENT_CATALOG);
        } catch (IOException ex) {
            throw ex instanceof TabularDataError
                    ? new SQLException(ex.getMessage(), "", ((TabularDataError) ex).getNumber())
                    : new SQLException(format("Failed to get catalog name of '%s'", connectionString), ex);
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

    @Nullable
    String getProperty(@NonNull DynamicProperty property) throws IOException {
        Objects.requireNonNull(property);
        if (lazyProperties == null) {
            lazyProperties = loadProperties();
        }
        return lazyProperties.get(property);
    }

    @NonNull
    TabularDataReader exec(@NonNull TabularDataQuery query) throws IOException {
        return executor.exec(query);
    }

    private EnumMap<DynamicProperty, String> loadProperties() throws IOException {
        TabularDataQuery query = TabularDataQuery
                .builder()
                .procedure("DbProperties")
                .parameter(connectionString)
                .parameters(DYNAMIC_PROPERTY_KEYS)
                .build();

        try (TabularDataReader reader = exec(query)) {
            String[] row = new String[reader.getHeader(0).length];
            Map<String, String> properties = new HashMap<>();
            while (reader.readNextInto(row)) {
                properties.put(row[0], row[1]);
            }
            return getProperties(properties);
        }
    }

    private EnumMap<DynamicProperty, String> getProperties(Map<String, String> properties) {
        EnumMap<DynamicProperty, String> result = new EnumMap<>(DynamicProperty.class);
        for (DynamicProperty o : DynamicProperty.values()) {
            String value = properties.get(o.getKey());
            if (value != null) {
                result.put(o, value);
            }
        }
        return result;
    }

    void checkState() throws SQLException {
        if (isClosed()) {
            throw new SQLException(format("Connection '%s' closed", connectionString));
        }
    }

    static final List<String> DYNAMIC_PROPERTY_KEYS = Stream.of(DynamicProperty.values()).map(DynamicProperty::getKey).collect(Collectors.toList());

    // https://msdn.microsoft.com/en-us/library/ms676695%28v=vs.85%29.aspx
    @lombok.AllArgsConstructor
    @lombok.Getter
    enum DynamicProperty {
        CURRENT_CATALOG("Current Catalog"),
        SPECIAL_CHARACTERS("Special Characters"),
        IDENTIFIER_CASE_SENSITIVITY("Identifier Case Sensitivity"),
        STRING_FUNCTIONS("String Functions");

        private final String key;
    }
}
