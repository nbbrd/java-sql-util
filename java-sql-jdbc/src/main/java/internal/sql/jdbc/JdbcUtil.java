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
package internal.sql.jdbc;

import java.util.logging.Level;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
@lombok.extern.java.Log
public class JdbcUtil {

    @NonNull
    public Stream<String> splitToStream(char separator, @NonNull CharSequence input) {
        return Stream.of(input.toString().split(String.valueOf(separator), -1));
    }

    @NonNull
    public String unexpectedNullToBlank(@Nullable String input, @NonNull String source) {
        if (input != null) {
            return input;
        }
        log.log(Level.FINE, "Unexpected null value for ''{0}''", source);
        return "";
    }
}
