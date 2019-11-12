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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class LhodResultSetMetaDataTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() throws SQLException {
        assertThat(sample()).isNotNull();
        assertThatThrownBy(() -> LhodResultSetMetaData.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testGetColumnCount() {
        assertThat(empty().getColumnCount()).isEqualTo(0);
        assertThat(sample().getColumnCount()).isEqualTo(2);
    }

    @Test
    public void testGetColumnName() throws SQLException {
        ResultSetMetaData md = sample();
        assertThat(md.getColumnName(1)).isEqualTo("INT_COLUMN");
        assertThat(md.getColumnName(2)).isEqualTo("DOUBLE_COLUMN");
        assertThatThrownBy(() -> md.getColumnName(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> md.getColumnName(3)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testGetColumnType() throws SQLException {
        ResultSetMetaData md = sample();
        assertThat(md.getColumnType(1)).isEqualTo(Types.INTEGER);
        assertThat(md.getColumnType(2)).isEqualTo(Types.DOUBLE);
        assertThatThrownBy(() -> md.getColumnType(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> md.getColumnType(3)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    static LhodResultSetMetaData empty() {
        return LhodResultSetMetaData.of(Collections.emptyList());
    }

    static LhodResultSetMetaData sample() {
        return LhodResultSetMetaData.of(Arrays.asList(new TabDataColumn("INT_COLUMN", 3), new TabDataColumn("DOUBLE_COLUMN", 5)));
    }
}
