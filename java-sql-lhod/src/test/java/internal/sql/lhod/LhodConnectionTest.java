/*
 * Copyright 2016 National Bank of Belgium
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

import static internal.sql.lhod.LhodConnection.of;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class LhodConnectionTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThat(good())
                .as("Factory must return a non-null connection")
                .isNotNull();

        assertThatThrownBy(() -> of(null, CONN_STRING))
                .as("Factory must throw NullPointerException if executor is null")
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> of(Resources.goodExecutor(), null))
                .as("Factory must throw NullPointerException if connectionString is null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testClose() throws SQLException {
        AtomicBoolean isClosed = new AtomicBoolean(false);
        TabularDataExecutor executor = new TabularDataExecutor() {
            @Override
            public TabularDataReader exec(TabularDataQuery query) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void close() throws IOException {
                isClosed.set(true);
            }
        };
        of(executor, "").close();
        assertThat(isClosed.get())
                .as("Close event must be propagated to observer")
                .isEqualTo(true);

        LhodConnection conn = good();
        assertThat(conn.isClosed()).isFalse();
        conn.close();
        assertThat(conn.isClosed()).isTrue();
        conn.close(); // no-op
    }

    @Test
    public void testGetMetaData() throws SQLException {
        assertThat(good().getMetaData())
                .as("MetaData must be non-null")
                .isNotNull();

        assertThatThrownBy(closed()::getMetaData)
                .as("MetaData must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testGetCatalog() throws SQLException {
        assertThat(good().getCatalog())
                .as("Catalog must return expected value")
                .isEqualTo("master");

        assertThatThrownBy(bad()::getCatalog)
                .as("Catalog must throw SQLException if IOException is raised")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(ugly()::getCatalog)
                .as("Catalog must throw SQLException if content is invalid")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(err()::getCatalog)
                .as("Catalog must throw SQLException if underlying exception is raised")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("name not found")
                .hasNoCause();

        assertThatThrownBy(closed()::getCatalog)
                .as("Catalog must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testSchema() throws SQLException {
        assertThatThrownBy(closed()::getSchema)
                .as("Schema must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testCreateStatement() throws SQLException {
        assertThat(good().createStatement())
                .as("Statement must be non-null")
                .isNotNull();

        assertThatThrownBy(closed()::createStatement)
                .as("Statement must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testPrepareStatement() throws SQLException {
        assertThat(good().prepareStatement(""))
                .as("PreparedStatement must be non-null")
                .isNotNull();

        assertThatThrownBy(() -> closed().prepareStatement(""))
                .as("PreparedStatement must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    public void testIsReadOnly() throws SQLException {
        assertThat(good().isReadOnly())
                .as("ReadOnly must return expected value")
                .isEqualTo(true);

        assertThatThrownBy(closed()::isReadOnly)
                .as("ReadOnly must throw SQLException if called on a closed connection")
                .isInstanceOf(SQLException.class)
                .hasMessageContaining(CONN_STRING);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetProperty() throws IOException, SQLException {
        LhodConnection c = good();
        assertThat(c.getProperty(LhodConnection.DynamicProperty.CURRENT_CATALOG)).isEqualTo("master");
        assertThat(c.getProperty(LhodConnection.DynamicProperty.SPECIAL_CHARACTERS)).isNotEmpty();
        assertThat(c.getProperty(LhodConnection.DynamicProperty.IDENTIFIER_CASE_SENSITIVITY)).isEqualTo("8");
        assertThat(c.getProperty(LhodConnection.DynamicProperty.STRING_FUNCTIONS)).isEqualTo("5242879");

        assertThatThrownBy(() -> good().getProperty(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> bad().getProperty(LhodConnection.DynamicProperty.CURRENT_CATALOG)).isInstanceOf(Resources.ExecIOException.class);
        assertThatThrownBy(() -> ugly().getProperty(LhodConnection.DynamicProperty.CURRENT_CATALOG)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> err().getProperty(LhodConnection.DynamicProperty.CURRENT_CATALOG)).isInstanceOf(TabularDataError.class);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    static final String CONN_STRING = "MyDb";

    static LhodConnection good() {
        return of(Resources.goodExecutor(), CONN_STRING);
    }

    static LhodConnection bad() {
        return of(Resources.badExecutor(), CONN_STRING);
    }

    static LhodConnection ugly() {
        return of(Resources.uglyExecutor(), CONN_STRING);
    }

    static LhodConnection err() {
        return of(Resources.errExecutor(), CONN_STRING);
    }

    static LhodConnection closed() throws SQLException {
        LhodConnection result = good();
        result.close();
        return result;
    }
    //</editor-fold>
}
