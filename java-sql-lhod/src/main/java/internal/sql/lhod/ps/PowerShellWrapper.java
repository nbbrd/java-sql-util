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
package internal.sql.lhod.ps;

import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class PowerShellWrapper {

    public static final String COMMAND = "powershell";
    public static final short NO_TIMEOUT = -1;

    @NonNull
    public Process exec(@NonNull File script, short timeoutInSeconds, @NonNull String... args) throws IOException {
        List<String> result = new ArrayList<>();
        result.add(COMMAND);
        result.add("-NoLogo");
        result.add("-File");
        result.add("\"" + script.getAbsolutePath() + "\"");
//        if (timeoutInSeconds > 0) {
//            result.add("//T:" + timeoutInSeconds);
//        }
        result.addAll(Arrays.asList(args));
        return new ProcessBuilder(result).start();
    }
}
