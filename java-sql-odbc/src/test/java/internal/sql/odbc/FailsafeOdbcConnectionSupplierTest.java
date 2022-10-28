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

import nbbrd.sql.odbc.OdbcConnectionSupplierSpi;
import nbbrd.sql.odbc.OdbcRegistrySpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

;

/**
 * @author Philippe Charles
 */
public class FailsafeOdbcConnectionSupplierTest {

    @BeforeEach
    public void before() {
        unexpectedErrors.clear();
        unexpectedNulls.clear();
    }

    @Test
    public void testGetNameNull() throws IOException {
        FailsafeOdbcConnectionSupplier x = of(new NullImpl());

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
        FailsafeOdbcConnectionSupplier x = of(new NullImpl());

        assertThat(x.isAvailable())
                .isEqualTo(false);

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetCostNull() throws IOException {
        FailsafeOdbcConnectionSupplier x = of(new NullImpl());

        assertThat(x.getCost())
                .isEqualTo(OdbcRegistrySpi.HIGH_COST);

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    @Test
    public void testGetConnectionNull() throws IOException {
        FailsafeOdbcConnectionSupplier x = of(new NullImpl());

        assertThatNullPointerException()
                .isThrownBy(() -> x.getConnection(null));

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> x.getConnection(""))
                .withNoCause()
                .withMessageContaining("Unexpected null");

        assertThat(unexpectedErrors)
                .isEmpty();

        assertThat(unexpectedNulls)
                .hasSize(1)
                .element(0)
                .asString()
                .contains("Unexpected null", "getConnection", NullImpl.class.getName());
    }

    @Test
    public void testGetNameError() throws IOException {
        FailsafeOdbcConnectionSupplier x = of(new ErrorImpl());

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
        FailsafeOdbcConnectionSupplier x = of(new ErrorImpl());

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
        FailsafeOdbcConnectionSupplier x = of(new ErrorImpl());

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
    public void testGetConnectionError() throws IOException {
        FailsafeOdbcConnectionSupplier x = of(new ErrorImpl());

        assertThatNullPointerException()
                .isThrownBy(() -> x.getConnection(null));

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> x.getConnection(""))
                .withCauseInstanceOf(UnsupportedOperationException.class)
                .withMessageContaining("Unexpected error");

        assertThat(unexpectedErrors)
                .hasSize(1)
                .element(0)
                .extracting(UnexpectedError::getMsg)
                .asString()
                .contains("Unexpected error", "getConnection", ErrorImpl.class.getName());

        assertThat(unexpectedNulls)
                .isEmpty();
    }

    private final List<UnexpectedError> unexpectedErrors = new ArrayList<>();
    private final List<String> unexpectedNulls = new ArrayList<>();

    private FailsafeOdbcConnectionSupplier of(OdbcConnectionSupplierSpi delegate) {
        return new FailsafeOdbcConnectionSupplier(delegate, (msg, ex) -> unexpectedErrors.add(new UnexpectedError(msg, ex)), unexpectedNulls::add);
    }

    @lombok.Value
    static final class UnexpectedError {

        private String msg;
        private RuntimeException ex;
    }

    static final class NullImpl implements OdbcConnectionSupplierSpi {

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
        public Connection getConnection(String connectionString) throws SQLException {
            return null;
        }
    }

    static final class ErrorImpl implements OdbcConnectionSupplierSpi {

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
        public Connection getConnection(String connectionString) throws SQLException {
            throw new UnsupportedOperationException();
        }
    }
}
