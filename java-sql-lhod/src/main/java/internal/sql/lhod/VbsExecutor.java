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
package internal.sql.lhod;

import internal.sys.ResourceExtractor;
import nbbrd.io.sys.ProcessReader;
import nbbrd.io.win.CScriptWrapper;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
final class VbsExecutor implements TabDataExecutor {

    @lombok.NonNull
    private final ResourceExtractor scripts;

    private boolean closed = false;

    @Override
    public @NonNull TabDataReader exec(@NonNull TabDataQuery query) throws IOException {
        if (closed) {
            throw new IOException("Executor closed");
        }
        return TabDataReader.of(exec(query.getProcedure() + ".vbs", query.getParameters().toArray(new String[0])));
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    @Override
    public boolean isClosed() throws IOException {
        return closed;
    }

    private BufferedReader exec(String scriptName, String[] args) throws IOException {
        File script = scripts.getResourceAsFile(scriptName);
        Process process = CScriptWrapper.exec(script, CScriptWrapper.NO_TIMEOUT, args);
        return ProcessReader.newReader(process);
    }
}
