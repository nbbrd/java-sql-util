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
package _test;

import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ThrowableTypeAssert;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SQLExceptions {

    public static ThrowableTypeAssert<SQLException> assertThatSQLException() {
        return Assertions.assertThatExceptionOfType(SQLException.class);
    }

    public static Condition<SQLException> withErrorCode(int code) {
        return new Condition<>(ex -> ex.getErrorCode() == code, "SQL error code must be equal to " + code);
    }

    public static Condition<SQLException> withoutErrorCode() {
        return new Condition<>(ex -> ex.getErrorCode() == 0, "SQL error code must be equal to 0");
    }
}
