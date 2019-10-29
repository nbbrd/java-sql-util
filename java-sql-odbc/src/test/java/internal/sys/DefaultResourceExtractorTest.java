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
package internal.sys;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class DefaultResourceExtractorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> DefaultResourceExtractor.of(null));

        assertThat(DefaultResourceExtractor.of(DefaultResourceExtractorTest.class))
                .isEqualTo(DefaultResourceExtractor
                        .builder()
                        .anchor(DefaultResourceExtractorTest.class)
                        .persist(false)
                        .repository(new File(System.getProperty("java.io.tmpdir")))
                        .build()
                );
    }

    @Test
    public void test() throws IOException {
        DefaultResourceExtractor x = DefaultResourceExtractor
                .builder()
                .repository(folder.getRoot())
                .anchor(DefaultResourceExtractorTest.class)
                .build();

        assertThat(x.getResourceAsFile("Hello.txt"))
                .exists()
                .hasContent("World");

        assertThatIOException()
                .isThrownBy(() -> x.getResourceAsFile("Stuff.txt"))
                .isInstanceOf(FileNotFoundException.class)
                .withMessageContaining("Stuff.txt");

        assertThat(x.getResourceAsFile("Hello.txt"))
                .isNotEqualTo(x.getResourceAsFile("Hello.txt"));
        
        assertThat(x.getResourceAsFile("WithoutExt"))
                .exists()
                .hasContent("Hello");
    }
}
