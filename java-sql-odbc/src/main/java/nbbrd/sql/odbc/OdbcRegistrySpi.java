/*
 * Copyright 2018 National Bank of Belgium
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

import nbbrd.service.ServiceDefinition;
import java.io.IOException;
import java.util.List;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceFilter;
import nbbrd.service.ServiceSorter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(
        quantifier = Quantifier.OPTIONAL,
        loaderName = "internal.sql.odbc.OdbcRegistrySpiLoader"
)
public interface OdbcRegistrySpi {

    @NonNull
    String getName();

    @ServiceFilter
    boolean isAvailable();

    @ServiceSorter
    int getCost();

    @NonNull
    List<String> getDataSourceNames(OdbcDataSource./*@NonNull*/ Type[] types) throws IOException;

    @NonNull
    List<OdbcDataSource> getDataSources(OdbcDataSource./*@NonNull*/ Type[] types) throws IOException;

    @NonNull
    List<OdbcDriver> getDrivers() throws IOException;

    @NonNull
    List<String> getDriverNames() throws IOException;

    int NO_COST = 0;
    int LOW_COST = 100;
    int HIGH_COST = 1000;
}
