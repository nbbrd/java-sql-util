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

import internal.sql.lhod.AdoDriver;
import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcRegistry;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import nbbrd.sql.jdbc.SqlTable;

/**
 *
 * @author Philippe Charles
 */
public class AdoDriverDemo {

    public static void main(String[] args) throws IOException, SQLException {
        OdbcRegistry registry = OdbcRegistry.ofServiceLoader()
                .orElseThrow(UnsupportedOperationException::new);

        System.out.println("Using registry '" + registry.getName() + "'");

        OdbcDataSource source = registry.getDataSources(OdbcDataSource.Type.USER)
                .stream()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);

        System.out.println("Using source '" + source.getName() + "'");

        System.out.println("[Tables]");

        try (Connection conn = DriverManager.getConnection(AdoDriver.PREFIX + source.getName())) {
            SqlTable.allOf(conn.getMetaData())
                    .stream()
                    .filter(table -> "TABLE".equals(table.getType()))
                    .forEach(System.out::println);
        }
    }
}
