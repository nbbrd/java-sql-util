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
package nbbrd.sql.jdbc;

import java.sql.SQLException;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SqlFuncTest {

    @Test
    public void testCompose() throws SQLException {
        assertThat(upperCase.compose(simpleName).applyWithSql(Double.class))
                .isEqualTo("DOUBLE");

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> upperCase.compose(failingSimpleName).applyWithSql(Double.class))
                .withMessage("getSimpleName");

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> failingUpperCase.compose(simpleName).applyWithSql(Double.class))
                .withMessage("toUpperCase");

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> failingUpperCase.compose(failingSimpleName).applyWithSql(Double.class))
                .withMessage("getSimpleName");
    }

    @Test
    public void testAndThen() throws SQLException {
        assertThat(simpleName.andThen(upperCase).applyWithSql(Double.class))
                .isEqualTo("DOUBLE");

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> failingSimpleName.andThen(upperCase).applyWithSql(Double.class))
                .withMessage("getSimpleName");

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> simpleName.andThen(failingUpperCase).applyWithSql(Double.class))
                .withMessage("toUpperCase");

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> failingSimpleName.andThen(failingUpperCase).applyWithSql(Double.class))
                .withMessage("getSimpleName");
    }

    SqlFunc<Class, String> simpleName = Class::getSimpleName;
    SqlFunc<String, String> upperCase = String::toUpperCase;
    SqlFunc<Class, String> failingSimpleName = ofException("getSimpleName");
    SqlFunc<String, String> failingUpperCase = ofException("toUpperCase");

    static <T, R> SqlFunc<T, R> ofException(String reason) {
        return ofException(() -> new SQLException(reason));
    }

    static <T, R> SqlFunc<T, R> ofException(Supplier<SQLException> ex) {
        return o -> {
            throw ex.get();
        };
    }
}
