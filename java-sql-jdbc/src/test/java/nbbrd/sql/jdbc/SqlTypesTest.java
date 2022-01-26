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

import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;;

/**
 *
 * @author Philippe Charles
 */
public class SqlTypesTest {

    @Test
    public void testGetJavaDateSqlDate() {
        LocalDate x = LocalDate.of(2001, 1, 10);

        assertThat(SqlTypes.getJavaDate(java.sql.Date.valueOf(x))).isEqualTo(x.toString());
    }

    @Test
    public void testGetJavaDateFromSqlTimestamp() {
        LocalDateTime x = LocalDateTime.of(2001, 1, 10, 12, 30, 9);

        assertThat(SqlTypes.getJavaDate(java.sql.Timestamp.valueOf(x))).isEqualTo(x.toString());
    }
}
