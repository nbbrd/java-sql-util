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

import lombok.NonNull;
import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcDataSource.Type;
import nbbrd.sql.odbc.OdbcDriver;
import nbbrd.sql.odbc.OdbcRegistrySpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

;

/**
 * @author Philippe Charles
 */
public class FailsafeOdbcRegistryTest {

    @BeforeEach
    public void before() {
        unexpectedErrors.clear();
        unexpectedNulls.clear();
    }

    @Test
    public void testGetNameNull() {
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
    public void testIsAvailableNull() {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThat(x.isAvailable())
                .isEqualTo(false);

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetCostNull() {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThat(x.getCost())
                .isEqualTo(OdbcRegistrySpi.HIGH_COST);

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetDataSourceNamesNull() {
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
    public void testGetDataSourcesNull() {
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
    public void testGetDriverNamesNull() {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThatIOException()
                .isThrownBy(x::getDriverNames)
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
    public void testGetDriversNull() {
        FailsafeOdbcRegistry x = of(new NullImpl());

        assertThatIOException()
                .isThrownBy(x::getDrivers)
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
    public void testGetNameError() {
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
    public void testIsAvailableError() {
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
    public void testGetCostError() {
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
    public void testGetDataSourceNamesError() {
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
    public void testGetDataSourcesError() {
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
    public void testGetDriverNamesError() {
        FailsafeOdbcRegistry x = of(new ErrorImpl());

        assertThatIOException()
                .isThrownBy(x::getDriverNames)
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
    public void testGetDriversError() {
        FailsafeOdbcRegistry x = of(new ErrorImpl());

        assertThatIOException()
                .isThrownBy(x::getDrivers)
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
    static class UnexpectedError {

        String msg;
        RuntimeException ex;
    }

    @SuppressWarnings("DataFlowIssue")
    static final class NullImpl implements OdbcRegistrySpi {

        @Override
        public @NonNull String getName() {
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
        public @NonNull List<String> getDataSourceNames(Type[] types) {
            return null;
        }

        @Override
        public @NonNull List<OdbcDataSource> getDataSources(OdbcDataSource.Type[] types) {
            return null;
        }

        @Override
        public @NonNull List<String> getDriverNames() {
            return null;
        }

        @Override
        public @NonNull List<OdbcDriver> getDrivers() {
            return null;
        }
    }

    static final class ErrorImpl implements OdbcRegistrySpi {

        @Override
        public @NonNull String getName() {
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
        public @NonNull List<String> getDataSourceNames(Type[] types) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull List<OdbcDataSource> getDataSources(OdbcDataSource.Type[] types) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull List<String> getDriverNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull List<OdbcDriver> getDrivers() {
            throw new UnsupportedOperationException();
        }
    }
}
