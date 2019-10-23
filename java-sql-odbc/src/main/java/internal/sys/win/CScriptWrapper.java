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

import java.io.File;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class CScriptWrapper {

    public static final String COMMAND = "cscript";

    @NonNull
    public Process exec(@NonNull File script, @NonNull String... args) throws IOException {
        // http://technet.microsoft.com/en-us/library/ff920171.aspx
        String[] result = new String[3 + args.length];
        result[0] = COMMAND;
        result[1] = "/nologo";
        result[2] = "\"" + script.getAbsolutePath() + "\"";
        System.arraycopy(args, 0, result, 3, args.length);
        return new ProcessBuilder(result).start();
    }
}
