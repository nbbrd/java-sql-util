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
import internal.sys.ProcessReader;
import static internal.sys.win.CScriptWrapper.NO_TIMEOUT;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.Assumptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class CScriptWrapperTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testExec() throws IOException, InterruptedException {
        assertThatNullPointerException()
                .isThrownBy(() -> CScriptWrapper.exec(null, NO_TIMEOUT, ""));

        assertThatNullPointerException()
                .isThrownBy(() -> CScriptWrapper.exec(folder.newFile(), NO_TIMEOUT, (String[]) null));

        Assumptions.assumeThat(OS.NAME).isEqualTo(OS.Name.WINDOWS);

        assertThat(CScriptWrapper.exec(vbs(""), NO_TIMEOUT).waitFor())
                .isEqualTo(0);

        assertThat(CScriptWrapper.exec(vbs("WScript.Quit -123"), NO_TIMEOUT).waitFor())
                .isEqualTo(-123);

        File scriptWithArgs = vbs(
                "For Each strArg in Wscript.Arguments",
                "  WScript.Echo strArg",
                "Next"
        );

        assertThat(CScriptWrapper.exec(scriptWithArgs, NO_TIMEOUT, "a", "b", "c").waitFor())
                .isEqualTo(0);

        assertThat(readString(CScriptWrapper.exec(scriptWithArgs, NO_TIMEOUT, "a", "b", "c")))
                .isEqualTo("a" + System.lineSeparator() + "b" + System.lineSeparator() + "c");

        File infiniteLoop = vbs(
                "While (true)",
                "Wend"
        );

        assertThat(CScriptWrapper.exec(infiniteLoop, (short) 2).waitFor())
                .isEqualTo(0);

        assertThat(readString(CScriptWrapper.exec(infiniteLoop, (short) 2)))
                .contains(infiniteLoop.toString());
    }

    private String readString(Process p) throws IOException {
        try (BufferedReader reader = ProcessReader.newReader(p)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private File vbs(String... content) throws IOException {
        File script = folder.newFile(UUID.randomUUID().toString() + ".vbs");
        Files.write(script.toPath(), Arrays.asList(content), StandardCharsets.UTF_8);
        return script;
    }
}
