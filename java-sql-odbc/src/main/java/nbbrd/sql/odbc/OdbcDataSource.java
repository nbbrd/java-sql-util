/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package nbbrd.sql.odbc;

import java.io.File;

/**
 * https://docs.microsoft.com/en-us/sql/odbc/reference/install/registry-entries-for-data-sources
 *
 * @author charphi
 */
@lombok.Value
@lombok.Builder
public class OdbcDataSource {

    public enum Type {

        SYSTEM, USER, FILE
    }

    @lombok.NonNull
    Type type;

    /**
     * The data source name (DSN).
     */
    @lombok.NonNull
    String name;

    /**
     * Optional description of the data source.
     */
    String description;

    /**
     * Name defined by the driver developer. It is usually the name of the DBMS
     * associated with the driver.
     */
    @lombok.NonNull
    String driverName;

    /**
     * Driver DLL path.
     */
    @lombok.NonNull
    File driverPath;

    /**
     * Optional server name.
     */
    String serverName;
}
