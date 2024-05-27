/*
 * Copyright 2013 National Bank of Belgium
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
package nbbrd.sql.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * https://developer.mimer.com/wp-content/uploads/standard-sql-reserved-words-summary.pdf
 *
 * @author Philippe Charles
 */
public enum SqlKeywords {

    SQL92_RESERVED_WORDS("Sql92ReservedWords.txt"),
    SQL92_NON_RESERVED_WORDS("Sql92NonReservedWords.txt"),
    SQL99_RESERVED_WORDS("Sql99ReservedWords.txt"),
    SQL2003_RESERVED_WORDS("Sql2003ReservedWords.txt"),
    SQL2008_RESERVED_WORDS("Sql2008ReservedWords.txt"),
    SQL2011_RESERVED_WORDS("Sql2011ReservedWords.txt"),
    SQL2016_RESERVED_WORDS("Sql2016ReservedWords.txt"),
    LATEST_RESERVED_WORDS("Sql2016ReservedWords.txt");

    @lombok.Getter
    private final Set<String> keywords;

    SqlKeywords(String resourceName) {
        keywords = loadWords(resourceName);
    }

    private static Set<String> loadWords(String resourceName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(requireNonNull(SqlKeywords.class.getResourceAsStream(resourceName)), UTF_8))) {
            return reader.lines().collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
        } catch (IOException ex) {
            throw new RuntimeException("Missing resource '" + resourceName + "'", ex);
        } catch (UncheckedIOException ex) {
            throw new RuntimeException("Missing resource '" + resourceName + "'", ex.getCause());
        }
    }
}
