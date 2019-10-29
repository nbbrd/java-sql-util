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
package internal.sys.win;

import internal.sys.OS;
import java.io.IOException;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.Assumptions;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class WhereWrapperTest {

    @Test
    public void testIsAvailable() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> WhereWrapper.isAvailable(null));

        Assumptions.assumeThat(OS.NAME).isEqualTo(OS.Name.WINDOWS);

        assertThat(WhereWrapper.isAvailable("where"))
                .as("Should be successul")
                .isTrue();

        assertThat(WhereWrapper.isAvailable(UUID.randomUUID().toString()))
                .as("Should be unsuccessul")
                .isFalse();

        assertThatIOException()
                .isThrownBy(() -> WhereWrapper.isAvailable("//"))
                .withMessageContaining("Unexpected errors");
    }
}
