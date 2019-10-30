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

import static internal.sql.lhod.LhodContext.DynamicProperty.CURRENT_CATALOG;
import static internal.sql.lhod.LhodContext.DynamicProperty.IDENTIFIER_CASE_SENSITIVITY;
import static internal.sql.lhod.LhodContext.DynamicProperty.SPECIAL_CHARACTERS;
import static internal.sql.lhod.LhodContext.DynamicProperty.STRING_FUNCTIONS;
import static internal.sql.lhod.LhodContext.of;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class LhodContextTest {

    @Test
    @SuppressWarnings("null")
    public void testGetProperty() throws IOException, SQLException {
        LhodContext c = good();
        assertThat(c.getProperty(CURRENT_CATALOG)).isEqualTo("master");
        assertThat(c.getProperty(SPECIAL_CHARACTERS)).isNotEmpty();
        assertThat(c.getProperty(IDENTIFIER_CASE_SENSITIVITY)).isEqualTo("8");
        assertThat(c.getProperty(STRING_FUNCTIONS)).isEqualTo("5242879");

        assertThatThrownBy(() -> good().getProperty(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> bad().getProperty(CURRENT_CATALOG)).isInstanceOf(FileNotFoundException.class);
        assertThatThrownBy(() -> ugly().getProperty(CURRENT_CATALOG)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> err().getProperty(CURRENT_CATALOG)).isInstanceOf(TabularDataError.class);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    static final String CONN_STRING = "MyDb";

    static LhodContext good() {
        return of(Resources.good(), CONN_STRING, Instant.now());
    }

    static LhodContext bad() {
        return of(Resources.bad(), CONN_STRING, Instant.now());
    }

    static LhodContext ugly() {
        return of(Resources.ugly(), CONN_STRING, Instant.now());
    }

    static LhodContext err() {
        return of(Resources.err(), CONN_STRING, Instant.now());
    }
    //</editor-fold>
}
