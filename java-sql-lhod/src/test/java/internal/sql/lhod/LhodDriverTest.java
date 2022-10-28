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
package internal.sql.lhod;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;;

/**
 *
 * @author Philippe Charles
 */
public class LhodDriverTest {

    @Test
    public void testConnect() throws SQLException {
        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> new LhodDriver(engine).connect(null, null))
                .withMessageContaining("URL cannot be null")
                .withNoCause();

        assertThat(new LhodDriver(engine).connect("jdbc:mysql://HOST/DATABASE", null)).isNull();

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> new LhodDriver(failingEngine).connect("jdbc:lhod:hello", null))
                .withMessageContaining("Cannot instantiate executor")
                .withCauseInstanceOf(CustomError.class);

        assertThatCode(() -> {
            try (Connection conn = new LhodDriver(engine).connect("jdbc:lhod:hello", null)) {
            }
        }).doesNotThrowAnyException();
    }

    @Test
    public void testAcceptsURL() throws SQLException {
        LhodDriver x = new LhodDriver(engine);

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> x.acceptsURL(null))
                .withMessageContaining("URL cannot be null")
                .withNoCause();

        assertThat(x.acceptsURL("jdbc:mysql://HOST/DATABASE")).isFalse();

        assertThat(x.acceptsURL("jdbc:lhod:hello")).isTrue();
    }

    private final TabDataEngine engine = () -> Resources.goodExecutor();
    private final TabDataEngine failingEngine = new Resources.FailingEngine(CustomError::new);

    private static final class CustomError extends IOException {
    }
}
