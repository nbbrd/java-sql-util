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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.Assumptions;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class RegWrapperTest {

    @Test
    public void testParseLeaf() throws IOException {
        Map<String, List<RegWrapper.RegValue>> data = parse("regLeaf.txt");

        assertThat(data)
                .hasSize(1)
                .containsKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit\\Capabilities\\UrlAssociations")
                .isEqualTo(parse("regLeaf.txt"));

        assertThat(data.get("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit\\Capabilities\\UrlAssociations"))
                .hasSize(5)
                .contains(new RegWrapper.RegValue("smartgit", "REG_SZ", "TortoiseGitURL"));
    }

    @Test
    public void testParseNode() throws IOException {
        Map<String, List<RegWrapper.RegValue>> data = parse("regNode.txt");

        assertThat(data)
                .hasSize(2)
                .containsKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit")
                .containsKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit\\Capabilities")
                .isEqualTo(parse("regNode.txt"));

        assertThat(data.get("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit"))
                .hasSize(5)
                .contains(new RegWrapper.RegValue("CachePath", "REG_SZ", "C:\\Program Files\\TortoiseGit\\bin\\TGitCache.exe"));

        assertThat(data.get("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit\\Capabilities"))
                .isEmpty();
    }

    @Test
    public void testParseNodeRecursive() throws IOException {
        Map<String, List<RegWrapper.RegValue>> data = parse("regNodeRecursive.txt");

        assertThat(data)
                .hasSize(3)
                .containsKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit")
                .containsKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit\\Capabilities")
                .containsKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit\\Capabilities\\UrlAssociations")
                .isEqualTo(parse("regNodeRecursive.txt"));

        assertThat(data.get("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit"))
                .hasSize(5)
                .contains(new RegWrapper.RegValue("CachePath", "REG_SZ", "C:\\Program Files\\TortoiseGit\\bin\\TGitCache.exe"));

        assertThat(data.get("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit\\Capabilities"))
                .isEmpty();

        assertThat(data.get("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit\\Capabilities\\UrlAssociations"))
                .isEqualTo(parse("regLeaf.txt").get("HKEY_LOCAL_MACHINE\\SOFTWARE\\TortoiseGit\\Capabilities\\UrlAssociations"));
    }

    @Test
    public void testQuery() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> RegWrapper.query(null, true));

        Assumptions.assumeThat(OS.NAME).isEqualTo(OS.Name.WINDOWS);

        Map<String, List<RegWrapper.RegValue>> data = RegWrapper.query("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", false);

        assertThat(data)
                .hasSizeGreaterThan(1)
                .containsKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");

        assertThat(data.get("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion"))
                .contains(new RegWrapper.RegValue("SystemRoot", "REG_SZ", System.getenv("SYSTEMROOT")));
    }

    static Map<String, List<RegWrapper.RegValue>> parse(String resourceName) throws IOException {
        try (BufferedReader reader = open(resourceName)) {
            return RegWrapper.parse(reader);
        }
    }

    static BufferedReader open(String resourceName) {
        return new BufferedReader(new InputStreamReader(RegWrapperTest.class.getResourceAsStream(resourceName)));
    }
}
