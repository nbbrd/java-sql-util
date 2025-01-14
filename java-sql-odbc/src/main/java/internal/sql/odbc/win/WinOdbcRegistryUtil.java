/*
 * Copyright 2013 National Bank of Belgium
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

import nbbrd.sql.odbc.OdbcDataSource;
import nbbrd.sql.odbc.OdbcDriver;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class WinOdbcRegistryUtil {

    public interface Registry {

        enum Root {

            HKEY_LOCAL_MACHINE, HKEY_CURRENT_USER
        }

        boolean keyExists(@NonNull Root root, @NonNull String key) throws IOException;

        @NonNull
        Map<String, Object> getValues(@NonNull Root root, @NonNull String key) throws IOException;
    }

    public static final String DATA_SOURCES_KEY = "SOFTWARE\\ODBC\\ODBC.INI\\ODBC Data Sources";
    public static final String DATA_SOURCE_KEY = "SOFTWARE\\ODBC\\ODBC.INI";
    public static final String DRIVERS_KEY = "SOFTWARE\\ODBC\\Odbcinst.INI\\ODBC Drivers";
    public static final String DRIVER_KEY = "SOFTWARE\\ODBC\\Odbcinst.INI";

    public static final String KEY_SEPARATOR = "\\";

    public List<String> getDataSourceNames(Registry reg, OdbcDataSource.Type... types) throws IOException {
        List<String> result = new ArrayList<>();
        for (OdbcDataSource.Type o : types) {
            forEachDataSourceName(reg, o, result::add);
        }
        return result;
    }

    private void forEachDataSourceName(Registry reg, OdbcDataSource.Type type, Consumer<String> consumer) throws IOException {
        Registry.Root root = getRoot(type);
        if (reg.keyExists(root, DATA_SOURCES_KEY)) {
            for (String dataSourceName : reg.getValues(root, DATA_SOURCES_KEY).keySet()) {
                consumer.accept(dataSourceName);
            }
        }
    }

    public List<OdbcDataSource> getDataSources(Registry reg, OdbcDataSource.Type... types) throws IOException {
        List<OdbcDataSource> result = new ArrayList<>();
        for (OdbcDataSource.Type o : types) {
            forEachDataSource(reg, o, result::add);
        }
        return result;
    }

    private void forEachDataSource(Registry reg, OdbcDataSource.Type type, Consumer<OdbcDataSource> consumer) throws IOException {
        Registry.Root root = getRoot(type);
        if (reg.keyExists(root, DATA_SOURCES_KEY)) {
            for (Entry<String, Object> master : reg.getValues(root, DATA_SOURCES_KEY).entrySet()) {
                String dataSourceKey = DATA_SOURCE_KEY + KEY_SEPARATOR + master.getKey();
                if (reg.keyExists(root, dataSourceKey)) {
                    consumer.accept(dataSourceOf(type, master, reg.getValues(root, dataSourceKey)));
                }
            }
        }
    }

    private OdbcDataSource dataSourceOf(OdbcDataSource.Type type, Entry<String, Object> master, Map<String, Object> details) {
        return OdbcDataSource
                .builder()
                .type(type)
                .name(master.getKey())
                .description(toString(details.get("Description"), null))
                .driverName(toString(master.getValue(), null))
                .driverPath(toFile(details.get("Driver"), null))
                .serverName(toString(details.get("Server"), null))
                .build();
    }

    public Registry.Root getRoot(OdbcDataSource.Type type) {
        switch (type) {
            case SYSTEM:
                return Registry.Root.HKEY_LOCAL_MACHINE;
            case USER:
                return Registry.Root.HKEY_CURRENT_USER;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<String> getDriverNames(Registry reg) throws IOException {
        if (reg.keyExists(Registry.Root.HKEY_LOCAL_MACHINE, DRIVERS_KEY)) {
            return new ArrayList<>(reg.getValues(Registry.Root.HKEY_LOCAL_MACHINE, DRIVERS_KEY).keySet());
        }
        return Collections.emptyList();
    }

    public List<OdbcDriver> getDrivers(Registry reg) throws IOException {
        List<OdbcDriver> result = new ArrayList<>();
        Registry.Root localMachine = Registry.Root.HKEY_LOCAL_MACHINE;
        if (reg.keyExists(localMachine, DRIVERS_KEY)) {
            for (String driverName : reg.getValues(localMachine, DRIVERS_KEY).keySet()) {
                String driverKey = DRIVER_KEY + KEY_SEPARATOR + driverName;
                if (reg.keyExists(localMachine, driverKey)) {
                    result.add(driverOf(driverName, reg.getValues(localMachine, driverKey)));
                }
            }
        }
        return result;
    }

    private OdbcDriver driverOf(String driverName, Map<String, Object> details) {
        return OdbcDriver
                .builder()
                .name(driverName)
                .apiLevel(toEnum(details.get("APILevel"), OdbcDriver.ApiLevel::parse).orElse(OdbcDriver.ApiLevel.NONE))
                .connectFunctions(toConnectFunctions(details.get("ConnectFunctions"), null))
                .driverPath(toFile(details.get("Driver"), null))
                .driverOdbcVer(toString(details.get("DriverOdbcVer"), null))
                .fileExtensions(toFileExtensions(details.get("FileExtns")))
                .fileUsage(toEnum(details.get("FileUsage"), OdbcDriver.FileUsage::parse).orElse(OdbcDriver.FileUsage.NONE))
                .setupPath(toFile(details.get("Setup"), null))
                .sqlLevel(toEnum(details.get("SQLLevel"), OdbcDriver.SqlLevel::parse).orElse(OdbcDriver.SqlLevel.SQL_92_ENTRY))
                .usageCount(toInt(details.get("UsageCount"), -1))
                .build();
    }

    private String toString(Object obj, String defaultValue) {
        return obj instanceof String ? (String) obj : defaultValue;
    }

    private File toFile(Object obj, File defaultValue) {
        return obj instanceof String ? Paths.get((String) obj).toFile() : defaultValue;
    }

    private int toInt(Object obj, int defaultValue) {
        return obj instanceof Integer ? (Integer) obj : defaultValue;
    }

    private <Z extends Enum<Z>> Optional<Z> toEnum(Object obj, IntFunction<Z> parser) {
        if (obj == null) {
            return Optional.empty();
        }
        try {
            int value = Integer.parseInt(obj.toString());
            return Optional.of(parser.apply(value));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private List<String> toFileExtensions(Object obj) {
        return obj != null
                ? splitToStream(",", obj.toString())
                .map(WinOdbcRegistryUtil::getFileExtension)
                .filter(o -> !o.isEmpty())
                .collect(Collectors.toList())
                : Collections.emptyList();
    }

    @NonNull
    private Stream<String> splitToStream(@NonNull String separator, @NonNull CharSequence input) {
        return Stream.of(input.toString().split(separator, -1));
    }

    private String getFileExtension(String input) {
        int index = input.lastIndexOf('.');
        return index != -1 ? input.substring(index + 1) : "";
    }

    private OdbcDriver.ConnectFunctions toConnectFunctions(Object obj, OdbcDriver.ConnectFunctions defaultValue) {
        if (obj != null) {
            try {
                return OdbcDriver.ConnectFunctions.parse(obj.toString());
            } catch (IllegalArgumentException ex) {
            }
        }
        return defaultValue;
    }
}
