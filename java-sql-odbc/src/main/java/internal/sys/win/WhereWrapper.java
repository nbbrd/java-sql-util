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

import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class WhereWrapper {

    public static final String COMMAND = "where";

    public boolean isAvailable(@NonNull String command) throws IOException {
        try {
            int exitCode = new ProcessBuilder(COMMAND, "/Q", command).start().waitFor();
            switch (exitCode) {
                case SUCCESSFUL_EXIT_CODE:
                    return true;
                case UNSUCCESSFUL_EXIT_CODE:
                    return false;
                case ERRORS_EXIT_CODE:
                    throw new IOException("Unexpected errors");
                default:
                    throw new IOException("Unexpected exit code: " + exitCode);
            }
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    private static final int SUCCESSFUL_EXIT_CODE = 0;
    private static final int UNSUCCESSFUL_EXIT_CODE = 1;
    private static final int ERRORS_EXIT_CODE = 2;
}
