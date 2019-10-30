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
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class CachedResourceExtractorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test() throws IOException, InterruptedException {
        CachedResourceExtractor x = CachedResourceExtractor.of(
                DefaultResourceExtractor
                        .builder()
                        .repository(folder.getRoot())
                        .anchor(CachedResourceExtractorTest.class)
                        .build()
        );

        File hello = x.getResourceAsFile("Hello.txt");

        assertThat(hello)
                .exists()
                .hasContent("World");

        assertThatIOException()
                .isThrownBy(() -> x.getResourceAsFile("Stuff.txt"))
                .isInstanceOf(FileNotFoundException.class)
                .withMessageContaining("Stuff.txt");

        assertThat(hello)
                .isEqualTo(x.getResourceAsFile("Hello.txt"));

        hello = x.getResourceAsFile("Hello.txt");
        Thread.sleep(1000);
        Files.write(hello.toPath(), Arrays.asList("abc"), StandardOpenOption.TRUNCATE_EXISTING);
        assertThat(hello)
                .isNotEqualTo(x.getResourceAsFile("Hello.txt"));

        hello = x.getResourceAsFile("Hello.txt");
        hello.delete();
        assertThat(hello)
                .isNotEqualTo(x.getResourceAsFile("Hello.txt"));
    }
}
