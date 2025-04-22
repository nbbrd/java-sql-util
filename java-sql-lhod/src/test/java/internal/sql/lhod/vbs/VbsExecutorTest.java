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
import java.util.UUID;

import static _test.TabConditions.rowCount;
import static org.assertj.core.api.Assertions.*;

class VbsExecutorTest {

    @SuppressWarnings({"resource", "DataFlowIssue", "EmptyTryBlock"})
    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> new VbsExecutor(null));

        assertThatCode(() -> {
            try (VbsExecutor ignore = new VbsExecutor(extractor)) {
            }
        }).doesNotThrowAnyException();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @EnabledOnOs(value = OS.WINDOWS, architectures = "amd64")
    public void testExec() throws IOException {
        try (VbsExecutor x = new VbsExecutor(extractor)) {
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
                assertThat(reader).has(rowCount(330));
            }

//            assertThatIOException()
//                    .isThrownBy(() -> x.exec(TabDataQuery.builder().procedure("Print").parameter("boom.txt").build()))
//                    .withMessageContaining("boom.txt");

            String missingDSN = UUID.randomUUID().toString().substring(0, 32);
            for (String procedure : new String[]{"DBProperties", "OpenSchema", "PreparedStatement"}) {
                assertThatIOException()
                        .isThrownBy(() -> x.exec(TabDataQuery.builder().procedure(procedure).parameter(missingDSN).build()))
                        .withMessageContaining("Data source name not found and no default driver specified");
            }

            x.close();
            assertThatIOException()
                    .isThrownBy(() -> x.exec(query));
        }
    }

    @Test
    @EnabledOnOs(value = OS.WINDOWS, architectures = "aarch64")
    public void testExecArm64() {
        try (VbsExecutor x = new VbsExecutor(extractor)) {
            String missingDSN = UUID.randomUUID().toString().substring(0, 32);
            for (String procedure : new String[]{"DBProperties", "OpenSchema", "PreparedStatement"}) {
                assertThatIOException()
                        .isThrownBy(() -> x.exec(TabDataQuery.builder().procedure(procedure).parameter(missingDSN).build()))
                        .withMessageContaining("Provider cannot be found. It may not be properly installed.");
            }
        }
    }

    @Test
    public void testIsClosed() {
        try (VbsExecutor x = new VbsExecutor(extractor)) {
            assertThat(x.isClosed()).isFalse();
            x.close();
            assertThat(x.isClosed()).isTrue();
        }
    }

    private final ResourceExtractor extractor = DefaultResourceExtractor.of(VbsExecutorTest.class);
}