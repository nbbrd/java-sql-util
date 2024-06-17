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
package internal.sql.odbc.win;

import internal.sql.odbc.win.WinOdbcRegistryUtil.Registry;
import internal.sql.odbc.win.WinOdbcRegistryUtil.Registry.Root;
import nbbrd.io.sys.OS;
import nbbrd.io.win.RegWrapper;
import nbbrd.io.win.WhereWrapper;
import nbbrd.service.ServiceProvider;
import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcDriver;
import nbbrd.sql.odbc.OdbcRegistrySpi;
import lombok.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@ServiceProvider(OdbcRegistrySpi.class)
public final class RegOdbcRegistry implements OdbcRegistrySpi {

    @Override
    public @NonNull String getName() {
        return "OdbcRegistryOverRegCommand";
    }

    @Override
    public boolean isAvailable() {
        return OS.NAME == OS.Name.WINDOWS && isCommandAvailable();
    }

    @Override
    public int getCost() {
        return HIGH_COST;
    }

    @Override
    public @NonNull List<String> getDataSourceNames(OdbcDataSource.Type[] types) throws IOException {
        MapRegistry.Builder reg = MapRegistry.builder();
        for (OdbcDataSource.Type o : types) {
            reg.load(WinOdbcRegistryUtil.getRoot(o), WinOdbcRegistryUtil.DATA_SOURCES_KEY, false);
        }
        return WinOdbcRegistryUtil.getDataSourceNames(reg.build(), types);
    }

    @Override
    public @NonNull List<OdbcDataSource> getDataSources(OdbcDataSource.Type[] types) throws IOException {
        MapRegistry.Builder reg = MapRegistry.builder();
        for (OdbcDataSource.Type o : types) {
            reg.load(WinOdbcRegistryUtil.getRoot(o), WinOdbcRegistryUtil.DATA_SOURCE_KEY, true);
        }
        return WinOdbcRegistryUtil.getDataSources(reg.build(), types);
    }

    @Override
    public @NonNull List<String> getDriverNames() throws IOException {
        Registry reg = MapRegistry
                .builder()
                .load(Root.HKEY_LOCAL_MACHINE, WinOdbcRegistryUtil.DRIVERS_KEY, false)
                .build();
        return WinOdbcRegistryUtil.getDriverNames(reg);
    }

    @Override
    public @NonNull List<OdbcDriver> getDrivers() throws IOException {
        Registry reg = MapRegistry
                .builder()
                .load(Root.HKEY_LOCAL_MACHINE, WinOdbcRegistryUtil.DRIVER_KEY, true)
                .build();
        return WinOdbcRegistryUtil.getDrivers(reg);
    }

    private static boolean isCommandAvailable() {
        try {
            return WhereWrapper.isAvailable(RegWrapper.COMMAND);
        } catch (IOException ex) {
            log.log(Level.WARNING, "While checking command availability", ex);
            return false;
        }
    }

    @lombok.Builder
    private static final class MapRegistry implements WinOdbcRegistryUtil.Registry {

        @lombok.Singular
        private final Map<String, List<RegWrapper.RegValue>> keys;

        @Override
        public boolean keyExists(@NonNull Root root, @NonNull String key) {
            String target = root + WinOdbcRegistryUtil.KEY_SEPARATOR + key;
            return keys.keySet().stream().anyMatch(o -> o.startsWith(target));
        }

        @Override
        public @NonNull Map<String, Object> getValues(@NonNull Root root, @NonNull String key) {
            String target = root + WinOdbcRegistryUtil.KEY_SEPARATOR + key;
            return keys.containsKey(target)
                    ? keys.get(target).stream().collect(Collectors.toMap(RegWrapper.RegValue::getName, RegWrapper.RegValue::getValue))
                    : Collections.emptySortedMap();
        }

        public static class Builder {

            public Builder load(Root root, String subkey, boolean recursive) throws IOException {
                String keyName = root + WinOdbcRegistryUtil.KEY_SEPARATOR + subkey;
                try {
                    return keys(RegWrapper.query(keyName, recursive));
                } catch (IOException ex) {
                    // FIXME: "ERROR: The system was unable to find the specified registry key or value."
                    if (ex.getMessage().contains("Invalid exit value: 1")) {
                        return this;
                    }
                    throw ex;
                }
            }
        }
    }
}
