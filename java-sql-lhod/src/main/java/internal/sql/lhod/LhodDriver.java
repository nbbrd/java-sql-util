/*
 * Copyright 2015 National Bank of Belgium
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

import java.io.IOException;
import static java.lang.String.format;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import internal.sql.lhod.vbs.VbsEngine;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;

/**
 * https://msdn.microsoft.com/en-us/library/aa478977.aspx
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@ServiceProvider(Driver.class)
public final class LhodDriver extends _Driver {

    public static final String PREFIX = "jdbc:lhod:";

    static {
        try {
            DriverManager.registerDriver(new LhodDriver());
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Cannot register AdoDriver", ex);
        }
    }

    @lombok.NonNull
    private final TabDataEngine engine;

    public LhodDriver() {
        this(new VbsEngine());
    }

    @VisibleForTesting
    public LhodDriver(@NonNull TabDataEngine engine) {
        this.engine = engine;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        try {
            return LhodConnection.of(engine.getExecutor(), getConnectionString(url));
        } catch (IOException ex) {
            throw new SQLException(format(Locale.ROOT, "Cannot instantiate executor: '%s'", url), ex);
        }
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url == null) {
            throw new SQLException("URL cannot be null");
        }
        return url.toLowerCase(Locale.ROOT).startsWith(PREFIX);
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    private String getConnectionString(String url) {
        return url.trim().substring(PREFIX.length());
    }
}
