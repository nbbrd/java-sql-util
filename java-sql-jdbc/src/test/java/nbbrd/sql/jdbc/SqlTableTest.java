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
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import static nbbrd.sql.jdbc.SqlTable.ALL_CATALOGS;
import static nbbrd.sql.jdbc.SqlTable.ALL_SCHEMAS;
import static nbbrd.sql.jdbc.SqlTable.ALL_TABLE_NAMES;
import static nbbrd.sql.jdbc.SqlTable.ALL_TYPES;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SqlTableTest {

    @Test
    public void testFactories() throws SQLException {
        assertThatNullPointerException()
                .isThrownBy(() -> SqlTable.allOf(null));

        assertThatNullPointerException()
                .isThrownBy(() -> SqlTable.allOf(null, ALL_CATALOGS, ALL_SCHEMAS, ALL_TABLE_NAMES, ALL_TYPES));

        try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:test")) {
            DatabaseMetaData metaData = conn.getMetaData();

            assertThatNullPointerException()
                    .isThrownBy(() -> SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, null, ALL_TYPES));
        }
    }

    @Test
    public void testAllOfMetaData() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:test")) {
            DatabaseMetaData metaData = conn.getMetaData();

            assertThat(SqlTable.allOf(metaData))
                    .isNotEmpty()
                    .allMatch(table -> table.getType().equals("SYSTEM TABLE"))
                    .contains(columnsTable);
        }
    }

    @Test
    public void testAllOfMetaData2() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:test")) {
            DatabaseMetaData metaData = conn.getMetaData();

            assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, "COLUMNS", ALL_TYPES))
                    .hasSize(1)
                    .element(0)
                    .isEqualTo(columnsTable);

            assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, "%COLUMN%", ALL_TYPES))
                    .isNotEmpty()
                    .allMatch(table -> table.getName().contains("COLUMN"));

            assertThat(SqlTable.allOf(metaData, "PUBLIC", ALL_SCHEMAS, ALL_TABLE_NAMES, ALL_TYPES))
                    .isNotEmpty()
                    .allMatch(table -> table.getCatalog().equals("PUBLIC"));

            assertThat(SqlTable.allOf(metaData, "ABC", ALL_SCHEMAS, ALL_TABLE_NAMES, ALL_TYPES))
                    .isEmpty();

            assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, "%SYS%", ALL_TABLE_NAMES, ALL_TYPES))
                    .isNotEmpty()
                    .allMatch(table -> table.getSchema().contains("SYS"));

            assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, "ABC", ALL_TABLE_NAMES, ALL_TYPES))
                    .isEmpty();

            assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, ALL_TABLE_NAMES, new String[]{"SYSTEM TABLE"}))
                    .isNotEmpty()
                    .allMatch(table -> table.getType().contains("SYS"));

            assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, ALL_TABLE_NAMES, new String[]{"ABC"}))
                    .isEmpty();
        }
    }

    private final SqlTable columnsTable = SqlTable
            .builder()
            .catalog("PUBLIC")
            .schema("INFORMATION_SCHEMA")
            .name("COLUMNS")
            .type("SYSTEM TABLE")
            .remarks("one row for each column of table of view")
            .build();
}
