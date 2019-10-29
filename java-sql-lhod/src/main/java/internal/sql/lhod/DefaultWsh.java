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

import internal.sys.ProcessReader;
import internal.sys.CachedResourceExtractor;
import internal.sys.DefaultResourceExtractor;
import internal.sys.ResourceExtractor;
import internal.sys.win.CScriptWrapper;
import static internal.sys.win.CScriptWrapper.NO_TIMEOUT;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Philippe Charles
 */
//@ThreadSafe
final class DefaultWsh implements Wsh {

    private final ResourceExtractor scripts = CachedResourceExtractor.of(DefaultResourceExtractor.of(Wsh.class));

    @Override
    public BufferedReader exec(String scriptName, String... args) throws IOException {
        File script = scripts.getResourceAsFile(scriptName);
        Process process = CScriptWrapper.exec(script, NO_TIMEOUT, args);
        return ProcessReader.newReader(process);
    }
}
