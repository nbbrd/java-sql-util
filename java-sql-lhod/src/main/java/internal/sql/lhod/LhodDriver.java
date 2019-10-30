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

import static java.lang.String.format;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
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

    private final TabularDataExecutor executor;
    private final LhodContextPool pool;

    public LhodDriver() {
        this(new VbsExecutor(), LhodContextPool.of(Clock.systemDefaultZone(), Duration.ofMinutes(10), new LinkedList<>()));
    }

//    @VisibleForTesting
    LhodDriver(TabularDataExecutor executor, LhodContextPool pool) {
        this.executor = executor;
        this.pool = pool;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            throw new SQLException(format("Invalid database url: '%s'", url));
        }
        String connectionString = url.trim().substring(PREFIX.length());
        return LhodConnection.of(pool.getOrCreate(executor, connectionString), pool::recycle);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.toLowerCase().startsWith(PREFIX);
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }
}
