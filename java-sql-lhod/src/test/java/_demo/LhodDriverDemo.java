/*
 * Copyright 2018 National Bank of Belgium
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
package _demo;

import internal.sql.lhod.LhodDriver;
import internal.sql.lhod.TabDataEngine;
import internal.sql.lhod.ps.PsEngine;
import internal.sql.lhod.vbs.VbsEngine;
import nbbrd.sql.jdbc.SqlColumn;
import nbbrd.sql.jdbc.SqlTable;
import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcRegistry;

import java.io.IOException;
import java.sql.*;
import java.util.NoSuchElementException;

import static java.util.Comparator.comparing;

/**
 * @author Philippe Charles
 */
public class LhodDriverDemo {

    public static void main(String[] args) throws IOException, SQLException {
        OdbcRegistry registry = getRegistry();
        System.out.println("   Registry: '" + registry.getName() + "'");

        OdbcDataSource source = getFirstSource(registry);
        System.out.println("     Source: '" + source.getName() + "'");

        for (TabDataEngine engine : new TabDataEngine[]{new VbsEngine(), new PsEngine()}) {
            Driver driver = new LhodDriver(engine);
            System.out.println("     Engine: '" + engine.getId() + "'");

            SqlTable table = getFirstTable(driver, source);
            System.out.println(" FirstTable: " + table);

            SqlColumn column = getFirstColumn(driver, source, table);
            System.out.println("FirstColumn: " + column);
        }
    }

    private static OdbcRegistry getRegistry() {
        return OdbcRegistry.ofServiceLoader()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private static OdbcDataSource getFirstSource(OdbcRegistry registry) throws IOException {
        return registry.getDataSources(OdbcDataSource.Type.USER)
                .stream()
                .min(comparing(OdbcDataSource::getName))
                .orElseThrow(NoSuchElementException::new);
    }

    private static SqlTable getFirstTable(Driver driver, OdbcDataSource source) throws SQLException {
        try (Connection conn = driver.connect(LhodDriver.PREFIX + source.getName(), null)) {
            return SqlTable.allOf(conn.getMetaData())
                    .stream()
                    .filter(table -> "TABLE".equals(table.getType()))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
        }
    }

    private static SqlColumn getFirstColumn(Driver driver, OdbcDataSource source, SqlTable table) throws SQLException {
        try (Connection conn = driver.connect(LhodDriver.PREFIX + source.getName(), null)) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery("select * from " + table.getName())) {
                    return SqlColumn.allOf(rs.getMetaData())
                            .stream()
                            .findFirst()
                            .orElseThrow(NoSuchElementException::new);
                }
            }
        }
    }
}
