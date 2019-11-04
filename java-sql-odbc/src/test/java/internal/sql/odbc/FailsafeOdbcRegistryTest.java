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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcDataSource.Type;
import nbbrd.sql.odbc.OdbcDriver;
import nbbrd.sql.odbc.OdbcRegistrySpi;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FailsafeOdbcRegistryTest {

    @Before
    public void before() {
        unexpectedErrors.clear();
        unexpectedNulls.clear();
    }

    @Test
    public void testGetNameNull() throws IOException {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThat(x.getName())
                .isEqualTo(NullImpl.class.getName());

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .hasSize(1)
                .element(0)
                .asString()
                .contains("Unexpected null", "getName", NullImpl.class.getName());
    }

    @Test
    public void testIsAvailableNull() throws IOException {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThat(x.isAvailable())
                .isEqualTo(false);

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetCostNull() throws IOException {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThat(x.getCost())
                .isEqualTo(OdbcRegistrySpi.HIGH_COST);

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetDataSourceNamesNull() throws IOException {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThatNullPointerException()
                .isThrownBy(() -> x.getDataSourceNames(null));

        assertThatIOException()
                .isThrownBy(() -> x.getDataSourceNames(Type.values()))
                .withNoCause()
                .withMessageContaining("Unexpected null");

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .hasSize(1)
                .element(0)
                .asString()
                .contains("Unexpected null", "getDataSourceNames", NullImpl.class.getName());
    }

    @Test
    public void testGetDataSourcesNull() throws IOException {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThatNullPointerException()
                .isThrownBy(() -> x.getDataSources(null));

        assertThatIOException()
                .isThrownBy(() -> x.getDataSources(Type.values()))
                .withNoCause()
                .withMessageContaining("Unexpected null");

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .hasSize(1)
                .element(0)
                .asString()
                .contains("Unexpected null", "getDataSources", NullImpl.class.getName());
    }

    @Test
    public void testGetDriverNamesNull() throws IOException {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThatIOException()
                .isThrownBy(() -> x.getDriverNames())
                .withNoCause()
                .withMessageContaining("Unexpected null");

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .hasSize(1)
                .element(0)
                .asString()
                .contains("Unexpected null", "getDriverNames", NullImpl.class.getName());
    }

    @Test
    public void testGetDriversNull() throws IOException {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThatIOException()
                .isThrownBy(() -> x.getDrivers())
                .withNoCause()
                .withMessageContaining("Unexpected null");

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .hasSize(1)
                .element(0)
                .asString()
                .contains("Unexpected null", "getDrivers", NullImpl.class.getName());
    }

    @Test
    public void testGetNameError() throws IOException {
        FailsafeOdbcRegistry x = of(new ErrorImpl());

        assertThat(x.getName())
                .isEqualTo(ErrorImpl.class.getName());

        assertThat(unexpectedErrors)
                .hasSize(1)
                .element(0)
                .extracting(UnexpectedError::getMsg)
                .asString()
                .contains("Unexpected error", "getName", ErrorImpl.class.getName());

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testIsAvailableError() throws IOException {
        FailsafeOdbcRegistry x = of(new ErrorImpl());

        assertThat(x.isAvailable())
                .isEqualTo(false);

        assertThat(unexpectedErrors)
                .hasSize(1)
                .element(0)
                .asString()
                .contains("Unexpected error", "isAvailable", ErrorImpl.class.getName());

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetCostError() throws IOException {
        FailsafeOdbcRegistry x = of(new ErrorImpl());

        assertThat(x.getCost())
                .isEqualTo(Integer.MAX_VALUE);

        assertThat(unexpectedErrors)
                .hasSize(1)
                .element(0)
                .asString()
                .contains("Unexpected error", "getCost", ErrorImpl.class.getName());

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetDataSourceNamesError() throws IOException {
        FailsafeOdbcRegistry x = of(new ErrorImpl());

        assertThatNullPointerException()
                .isThrownBy(() -> x.getDataSourceNames(null));

        assertThatIOException()
                .isThrownBy(() -> x.getDataSourceNames(Type.values()))
                .withCauseInstanceOf(UnsupportedOperationException.class)
                .withMessageContaining("Unexpected error");

        assertThat(unexpectedErrors)
                .hasSize(1)
                .element(0)
                .extracting(UnexpectedError::getMsg)
                .asString()
                .contains("Unexpected error", "getDataSourceNames", ErrorImpl.class.getName());

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetDataSourcesError() throws IOException {
        FailsafeOdbcRegistry x = of(new ErrorImpl());

        assertThatNullPointerException()
                .isThrownBy(() -> x.getDataSources(null));

        assertThatIOException()
                .isThrownBy(() -> x.getDataSources(Type.values()))
                .withCauseInstanceOf(UnsupportedOperationException.class)
                .withMessageContaining("Unexpected error");

        assertThat(unexpectedErrors)
                .hasSize(1)
                .element(0)
                .extracting(UnexpectedError::getMsg)
                .asString()
                .contains("Unexpected error", "getDataSources", ErrorImpl.class.getName());

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetDriverNamesError() throws IOException {
        FailsafeOdbcRegistry x = of(new ErrorImpl());

        assertThatIOException()
                .isThrownBy(() -> x.getDriverNames())
                .withCauseInstanceOf(UnsupportedOperationException.class)
                .withMessageContaining("Unexpected error");

        assertThat(unexpectedErrors)
                .hasSize(1)
                .element(0)
                .extracting(UnexpectedError::getMsg)
                .asString()
                .contains("Unexpected error", "getDriverNames", ErrorImpl.class.getName());

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetDriversError() throws IOException {
        FailsafeOdbcRegistry x = of(new ErrorImpl());

        assertThatIOException()
                .isThrownBy(() -> x.getDrivers())
                .withCauseInstanceOf(UnsupportedOperationException.class)
                .withMessageContaining("Unexpected error");

        assertThat(unexpectedErrors)
                .hasSize(1)
                .element(0)
                .extracting(UnexpectedError::getMsg)
                .asString()
                .contains("Unexpected error", "getDrivers", ErrorImpl.class.getName());

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    private final List<UnexpectedError> unexpectedErrors = new ArrayList<>();
    private final List<String> unexpectedNulls = new ArrayList<>();

    private FailsafeOdbcRegistry of(OdbcRegistrySpi delegate) {
        return new FailsafeOdbcRegistry(delegate, (msg, ex) -> unexpectedErrors.add(new UnexpectedError(msg, ex)), unexpectedNulls::add);
    }

    @lombok.Value
    static final class UnexpectedError {

        private String msg;
        private RuntimeException ex;
    }

    static final class NullImpl implements OdbcRegistrySpi {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public int getCost() {
            return HIGH_COST;
        }

        @Override
        public List<String> getDataSourceNames(Type[] types) throws IOException {
            return null;
        }

        @Override
        public List<OdbcDataSource> getDataSources(OdbcDataSource.Type[] types) throws IOException {
            return null;
        }

        @Override
        public List<String> getDriverNames() throws IOException {
            return null;
        }

        @Override
        public List<OdbcDriver> getDrivers() throws IOException {
            return null;
        }
    }

    static final class ErrorImpl implements OdbcRegistrySpi {

        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAvailable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCost() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> getDataSourceNames(Type[] types) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<OdbcDataSource> getDataSources(OdbcDataSource.Type[] types) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> getDriverNames() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<OdbcDriver> getDrivers() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
