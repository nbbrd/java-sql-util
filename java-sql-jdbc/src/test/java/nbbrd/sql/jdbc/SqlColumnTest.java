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
package nbbrd.sql.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SqlColumnTest {

    @Test
    public void testFactory() {
        assertThatNullPointerException()
                .isThrownBy(() -> SqlColumn.allOf(null));

        assertThatNullPointerException()
                .isThrownBy(() -> SqlColumn.of(null, 1));
    }

    @Test
    public void testAllOfMetaData() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:test")) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery("select * from INFORMATION_SCHEMA.COLUMNS")) {
                    ResultSetMetaData metaData = rs.getMetaData();

                    assertThat(SqlColumn.allOf(metaData))
                            .isNotEmpty()
                            .contains(tableNameColumn);
                }
            }
        }
    }

    private final SqlColumn tableNameColumn = SqlColumn
            .builder()
            .className("java.lang.String")
            .displaySize(128)
            .label("TABLE_NAME")
            .name("TABLE_NAME")
            .type(12)
            .typeName("VARCHAR")
            .build();
}
