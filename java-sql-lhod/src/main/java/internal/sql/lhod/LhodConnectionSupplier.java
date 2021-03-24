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
package internal.sql.lhod;

import nbbrd.io.sys.OS;
import nbbrd.service.ServiceProvider;
import nbbrd.sql.odbc.OdbcConnectionSupplierSpi;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Philippe Charles
 */
@ServiceProvider(OdbcConnectionSupplierSpi.class)
public final class LhodConnectionSupplier implements OdbcConnectionSupplierSpi {

    private final LhodDriver driver = new LhodDriver();

    @Override
    public String getName() {
        return driver.getClass().getName();
    }

    @Override
    public boolean isAvailable() {
        return OS.NAME == OS.Name.WINDOWS;
    }

    @Override
    public int getCost() {
        return HIGH_COST;
    }

    @Override
    public Connection getConnection(String connectionString) throws SQLException {
        return driver.connect(LhodDriver.PREFIX + connectionString, null);
    }
}
