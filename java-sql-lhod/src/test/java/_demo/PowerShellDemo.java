package _demo;

import internal.sql.lhod.TabDataEngine;
import internal.sql.lhod.TabDataExecutor;
import internal.sql.lhod.TabDataQuery;
import internal.sql.lhod.TabDataReader;
import internal.sql.lhod.ps.PsEngine;
import internal.sql.lhod.vbs.VbsEngine;
import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcRegistry;

import java.io.IOException;
import java.util.NoSuchElementException;

import static java.util.Comparator.comparing;

public class PowerShellDemo {

    public static void main(String[] args) throws IOException {

        TabDataQuery query = TabDataQuery
                .builder()
                .procedure("DbProperties")
                .parameter(getFirstSource().getName())
                .build();

        for (TabDataEngine engine : new TabDataEngine[]{new VbsEngine(), new PsEngine()}) {
            System.out.println("[" + engine.getId() + "]");
            try (TabDataExecutor executor = engine.getExecutor()) {
                long current = System.currentTimeMillis();
                try (TabDataReader reader = executor.exec(query)) {
                    System.out.println(reader.getColumns());
                    System.out.println("Head: " + (System.currentTimeMillis() - current) + " ms");
                    current = System.currentTimeMillis();
                }
                System.out.println("Body: " + (System.currentTimeMillis() - current) + " ms");
            }
        }
    }

    private static OdbcDataSource getFirstSource() throws IOException {
        return OdbcRegistry.ofServiceLoader()
                .orElseThrow(UnsupportedOperationException::new)
                .getDataSources(OdbcDataSource.Type.USER)
                .stream()
                .min(comparing(OdbcDataSource::getName))
                .orElseThrow(NoSuchElementException::new);
    }
}
