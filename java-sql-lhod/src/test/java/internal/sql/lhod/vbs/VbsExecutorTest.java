package internal.sql.lhod.vbs;

import internal.sql.lhod.TabDataColumn;
import internal.sql.lhod.TabDataQuery;
import internal.sql.lhod.TabDataReader;
import internal.sys.DefaultResourceExtractor;
import internal.sys.ResourceExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class VbsExecutorTest {

    @SuppressWarnings({"resource", "DataFlowIssue"})
    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> new VbsExecutor(null));

        assertThatCode(() -> {
            //noinspection EmptyTryBlock
            try (VbsExecutor ignore = new VbsExecutor(extractor)) {
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void testExec() throws IOException {
        try (VbsExecutor x = new VbsExecutor(extractor)) {
            //noinspection DataFlowIssue
            assertThatNullPointerException()
                    .isThrownBy(() -> x.exec(null));

            TabDataQuery query = TabDataQuery
                    .builder()
                    .procedure("Print")
                    .parameter(extractor.getResourceAsFile("/internal/sql/lhod/Top5Stmt.tsv").toString())
                    .build();

            try (TabDataReader reader = x.exec(query)) {
                assertThat(reader.getColumns())
                        .containsExactly(
                                new TabDataColumn("Freq", 202),
                                new TabDataColumn("Browser", 202),
                                new TabDataColumn("Period", 135),
                                new TabDataColumn("MarketShare", 5)
                        );
                int count = 0;
                while (reader.readNextRow()) {
                    count++;
                }
                assertThat(count).isEqualTo(330);
            }

            x.close();
            assertThatIOException()
                    .isThrownBy(() -> x.exec(query));
        }
    }

    @Test
    public void testIsClosed() throws IOException {
        try (VbsExecutor x = new VbsExecutor(extractor)) {
            assertThat(x.isClosed()).isFalse();
            x.close();
            assertThat(x.isClosed()).isTrue();
        }
    }

    private final ResourceExtractor extractor = DefaultResourceExtractor.of(VbsExecutorTest.class);
}