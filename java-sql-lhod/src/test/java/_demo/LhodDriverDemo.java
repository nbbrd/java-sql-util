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
import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcRegistry;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.Optional;
import nbbrd.sql.jdbc.SqlColumn;
import nbbrd.sql.jdbc.SqlTable;

/**
 *
 * @author Philippe Charles
 */
public class LhodDriverDemo {

    public static void main(String[] args) throws IOException, SQLException {
        OdbcRegistry registry = OdbcRegistry.ofServiceLoader()
                .orElseThrow(UnsupportedOperationException::new);
        System.out.println("registry '" + registry.getName() + "'");

        OdbcDataSource source = registry.getDataSources(OdbcDataSource.Type.USER)
                .stream()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        System.out.println("source '" + source.getName() + "'");

        SqlTable table = getFirstTable(source).orElseThrow(NoSuchElementException::new);
        System.out.println(table);

        SqlColumn column = getFirstColumn(source, table).orElseThrow(NoSuchElementException::new);
        System.out.println(column);
    }

    private static Optional<SqlTable> getFirstTable(OdbcDataSource source) throws SQLException {
        try (Connection conn = DriverManager.getConnection(LhodDriver.PREFIX + source.getName())) {
            return SqlTable.allOf(conn.getMetaData())
                    .stream()
                    .filter(table -> "TABLE".equals(table.getType()))
                    .findFirst();
        }
    }

    private static Optional<SqlColumn> getFirstColumn(OdbcDataSource source, SqlTable table) throws SQLException {
        try (Connection conn = DriverManager.getConnection(LhodDriver.PREFIX + source.getName())) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery("select * from " + table.getName())) {
                    return SqlColumn.allOf(rs.getMetaData())
                            .stream()
                            .findFirst();
                }
            }
        }
    }
}
