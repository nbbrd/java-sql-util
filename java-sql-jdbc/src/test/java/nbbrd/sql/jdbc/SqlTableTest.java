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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import static nbbrd.sql.jdbc.SqlTable.ALL_CATALOGS;
import static nbbrd.sql.jdbc.SqlTable.ALL_SCHEMAS;
import static nbbrd.sql.jdbc.SqlTable.ALL_TABLE_NAMES;
import static nbbrd.sql.jdbc.SqlTable.ALL_TYPES;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;;

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

        for (InMemoryDriver driver : InMemoryDriver.values()) {
            try (Connection conn = driver.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();

                assertThatNullPointerException()
                        .isThrownBy(() -> SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, null, ALL_TYPES));
            }
        }
    }

    @Test
    public void testAllOfMetaData() throws SQLException {
        for (InMemoryDriver driver : InMemoryDriver.not(InMemoryDriver.H2)) {
            try (Connection conn = driver.getConnection()) {
                conn.prepareStatement("CREATE TABLE table1( column1 varchar(10) )").execute();

                DatabaseMetaData metaData = conn.getMetaData();

                assertThat(SqlTable.allOf(metaData))
                        .isNotEmpty()
                        .filteredOn(table -> table.getName().equalsIgnoreCase("table1"))
                        .hasSize(1)
                        .first()
                        .extracting(SqlTable::getType)
                        .isEqualTo("TABLE"); // FIXME: "BASE TABLE" in H2
            }
        }
    }

    @Test
    public void testAllOfMetaData2() throws SQLException {
        for (InMemoryDriver driver : InMemoryDriver.not(InMemoryDriver.DERBY, InMemoryDriver.SQLITE, InMemoryDriver.H2)) {
            try (Connection conn = driver.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();

                assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, "COLUMNS", ALL_TYPES))
                        .hasSize(1)
                        .element(0)
                        .isEqualToComparingOnlyGivenFields(columnsTable, "schema", "name", "type");

                assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, "%COLUMN%", ALL_TYPES))
                        .isNotEmpty()
                        .allMatch(table -> table.getName().contains("COLUMN"));

                for (String catalog : getCatalogs(metaData)) {
                    assertThat(SqlTable.allOf(metaData, catalog, ALL_SCHEMAS, ALL_TABLE_NAMES, ALL_TYPES))
                            .isNotEmpty()
                            .allMatch(table -> table.getCatalog().equals(catalog));
                }

                assertThat(SqlTable.allOf(metaData, "ABC", ALL_SCHEMAS, ALL_TABLE_NAMES, ALL_TYPES))
                        .isEmpty();

                // FIXME: %SYS%
//                for (String schema : getSchemas(metaData)) {
//                    assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, schema, ALL_TABLE_NAMES, ALL_TYPES))
//                            .isNotEmpty()
//                            .allMatch(table -> table.getSchema().contains(schema));
//                }
                assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, "ABC", ALL_TABLE_NAMES, ALL_TYPES))
                        .isEmpty();

                assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, ALL_TABLE_NAMES, new String[]{"SYSTEM TABLE"}))
                        .isNotEmpty()
                        .allMatch(table -> table.getType().contains("SYS"));

                assertThat(SqlTable.allOf(metaData, ALL_CATALOGS, ALL_SCHEMAS, ALL_TABLE_NAMES, new String[]{"ABC"}))
                        .isEmpty();
            }
        }
    }

    private static List<String> getCatalogs(DatabaseMetaData metaData) throws SQLException {
        List<String> result = new ArrayList<>();
        try (ResultSet rs = metaData.getCatalogs()) {
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        }
        return result;
    }

    private static List<String> getSchemas(DatabaseMetaData metaData) throws SQLException {
        List<String> result = new ArrayList<>();
        try (ResultSet rs = metaData.getSchemas()) {
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        }
        return result;
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
