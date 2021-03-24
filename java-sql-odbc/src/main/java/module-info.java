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

module nbbrd.sql.odbc {

    // compile only
    requires static org.checkerframework.checker.qual;
    requires static lombok;
    requires static nbbrd.service;

    // required dependencies
    requires transitive nbbrd.sql.jdbc;
    requires transitive nbbrd.io.win;

    // public api
    exports nbbrd.sql.odbc;

    // services registration
    uses nbbrd.sql.odbc.OdbcConnectionSupplierSpi;
    provides nbbrd.sql.odbc.OdbcConnectionSupplierSpi with
            internal.sql.odbc.win.SunOdbcConnectionSupplier;

    uses nbbrd.sql.odbc.OdbcRegistrySpi;
    provides nbbrd.sql.odbc.OdbcRegistrySpi with
            internal.sql.odbc.win.RegOdbcRegistry;

    // private api
    exports internal.sys to nbbrd.sql.lhod;
}
