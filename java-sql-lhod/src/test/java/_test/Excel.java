package _test;

import ec.util.spreadsheet.helpers.ArraySheet;
import nbbrd.sql.odbc.OdbcConnectionString;
import nbbrd.sql.odbc.OdbcDriver;
import nbbrd.sql.odbc.OdbcRegistry;
import spreadsheet.fastexcel.FastExcelBookFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public final class Excel {

    private Excel() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Optional<OdbcDriver> getDriver() throws IOException {
        return OdbcRegistry.ofServiceLoader()
                .orElseThrow(IOException::new)
                .getDrivers()
                .stream()
                .filter(Excel::isExcel)
                .findFirst();
    }

    private static boolean isExcel(OdbcDriver driver) {
        return driver.getName().contains("Excel") && driver.getName().contains(".xlsx");
    }

    public static File createTempFile(ArraySheet table) throws IOException {
        File excelFile = File.createTempFile("book1", ".xlsx");
        new FastExcelBookFactory().store(excelFile, table.toBook());
        return excelFile;
    }

    public static OdbcConnectionString getConnectionString(OdbcDriver driver, File file) {
        return OdbcConnectionString
                .builder()
                .with("DRIVER", driver.getName())
                .with("DBQ", file.toString())
                .build();
    }
}
