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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
class Resources {

    @lombok.RequiredArgsConstructor
    static final class FailingEngine implements TabDataEngine {

        @lombok.NonNull
        private final Supplier<? extends IOException> onGetExecutor;

        @Override
        public TabDataExecutor getExecutor() throws IOException {
            throw onGetExecutor.get();
        }
    }

    @lombok.RequiredArgsConstructor
    static final class FailingExecutor implements TabDataExecutor {

        @lombok.NonNull
        private final Supplier<? extends IOException> onExec;

        @lombok.NonNull
        private final Supplier<? extends IOException> onClose;

        @Override
        public TabDataReader exec(TabDataQuery query) throws IOException {
            throw onExec.get();
        }

        @Override
        public void close() throws IOException {
            throw onClose.get();
        }

        @Override
        public boolean isClosed() throws IOException {
            return false;
        }
    }

    @lombok.RequiredArgsConstructor
    static final class FakeExecutor implements TabDataExecutor {

        @lombok.NonNull
        private final Function<TabDataQuery, String> queries;

        private boolean closed = false;

        @Override
        public TabDataReader exec(TabDataQuery query) throws IOException {
            if (closed) {
                throw new IOException("Executor closed");
            }

            String content = queries.apply(query);
            if (content == null) {
                throw new FileNotFoundException(query.toString());
            }

            return TabDataReader.of(new BufferedReader(new StringReader(content)));
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        @Override
        public boolean isClosed() throws IOException {
            return closed;
        }
    }

    static final String CONN_STRING = "Top5";

    static final String SQL_STMT_QUERY = "select * from Top5";
    static final String SQL_PREP_STMT_QUERY = "select * from Top5 where browser=?";

    static final TabDataQuery GOOD_PROPERTIES_QUERY = TabDataQuery
            .builder()
            .procedure("DbProperties")
            .parameter(CONN_STRING)
            .parameters(LhodConnection.DYNAMIC_PROPERTY_KEYS)
            .build();

    static final TabDataQuery GOOD_SCHEMA_QUERY = TabDataQuery
            .builder()
            .procedure("OpenSchema")
            .parameter(CONN_STRING)
            .parameter("\"\"")
            .parameter("\"\"")
            .parameter("\"\"")
            .build();

    static final TabDataQuery GOOD_STMT_QUERY = TabDataQuery
            .builder()
            .procedure("PreparedStatement")
            .parameter(CONN_STRING)
            .parameter(SQL_STMT_QUERY)
            .build();

    static final TabDataQuery GOOD_PREP_STMT_QUERY = TabDataQuery
            .builder()
            .procedure("PreparedStatement")
            .parameter(CONN_STRING)
            .parameter(SQL_PREP_STMT_QUERY)
            .parameter("Firefox")
            .build();

    static final Map<TabDataQuery, String> GOOD_QUERIES = loadGoodQueries();

    private static Map<TabDataQuery, String> loadGoodQueries() {
        Map<TabDataQuery, String> result = new HashMap<>();
        result.put(GOOD_PROPERTIES_QUERY, Sample.TOP5_PROPS.getContent());
        result.put(GOOD_SCHEMA_QUERY, Sample.TOP5_SCHEMA.getContent());
        result.put(GOOD_STMT_QUERY, Sample.TOP5_STMT.getContent());
        result.put(GOOD_PREP_STMT_QUERY, Sample.TOP5_PREP_STMT.getContent());
        return Collections.unmodifiableMap(result);
    }

    static FakeExecutor goodExecutor() {
        return new FakeExecutor(GOOD_QUERIES::get);
    }

    static TabDataExecutor badExecutor() {
        return new FailingExecutor(ExecIOException::new, CloseIOException::new);
    }

    static TabDataExecutor uglyExecutor() {
        return new FakeExecutor(query -> "helloworld");
    }

    static TabDataExecutor errExecutor() {
        return new FakeExecutor(query -> Sample.MYDB_ERR.getContent());
    }

    static TabDataExecutor closedExecutor() {
        FakeExecutor result = new FakeExecutor(query -> "");
        try {
            result.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    static final class ExecIOException extends IOException {
    }

    static final class CloseIOException extends IOException {
    }

    enum Sample {
        TOP5_PROPS("Top5Props.tsv"),
        TOP5_SCHEMA("Top5Schema.tsv"),
        TOP5_STMT("Top5Stmt.tsv"),
        TOP5_PREP_STMT("Top5PrepStmt.tsv"),
        MYDB_ERR("MyDbErr.tsv");

        @lombok.Getter
        private final String content;

        private Sample(String resourceName) {
            this.content = load(resourceName);
        }

        public BufferedReader newReader() throws IOException {
            return new BufferedReader(new StringReader(content));
        }

        private static String load(String resourceName) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Resources.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
