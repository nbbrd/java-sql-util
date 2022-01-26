package nbbrd.sql.odbc;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class OdbcDriverTest {

    @Test
    public void testRepresentableAsInt_ApiLevel() {
        for (OdbcDriver.ApiLevel o : OdbcDriver.ApiLevel.values()) {
            assertThat(OdbcDriver.ApiLevel.parse(o.toInt()))
                    .isEqualTo(o);
        }

        assertThatIllegalArgumentException()
                .isThrownBy(() -> OdbcDriver.ApiLevel.parse(666))
                .withMessageContaining("666");
    }

    @Test
    public void testRepresentableAsInt_FileUsage() {
        for (OdbcDriver.FileUsage o : OdbcDriver.FileUsage.values()) {
            assertThat(OdbcDriver.FileUsage.parse(o.toInt()))
                    .isEqualTo(o);
        }

        assertThatIllegalArgumentException()
                .isThrownBy(() -> OdbcDriver.FileUsage.parse(666))
                .withMessageContaining("666");
    }

    @Test
    public void testRepresentableAsInt_SqlLevel() {
        for (OdbcDriver.SqlLevel o : OdbcDriver.SqlLevel.values()) {
            assertThat(OdbcDriver.SqlLevel.parse(o.toInt()))
                    .isEqualTo(o);
        }

        assertThatIllegalArgumentException()
                .isThrownBy(() -> OdbcDriver.SqlLevel.parse(666))
                .withMessageContaining("666");
    }
}
