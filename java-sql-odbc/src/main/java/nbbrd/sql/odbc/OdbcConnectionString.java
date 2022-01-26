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
package nbbrd.sql.odbc;

import lombok.AccessLevel;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * https://msdn.microsoft.com/en-us/library/system.data.odbc.odbcconnection.connectionstring.aspx
 * https://msdn.microsoft.com/en-us/library/system.data.odbc.odbcconnectionstringbuilder(v=vs.110).aspx
 * https://www.connectionstrings.com/kb/
 *
 * @author Philippe Charles
 */
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OdbcConnectionString {

    @lombok.NonNull
    Map<String, String> attributes;

    public @Nullable String getDriver() {
        return get(DRIVER_KEYWORD);
    }

    public @Nullable String get(@NonNull String key) {
        return attributes
                .entrySet()
                .stream()
                .filter(o -> o.getKey().equalsIgnoreCase(key))
                .findFirst()
                .map(Entry::getValue)
                .orElse(null);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        Iterator<Entry<String, String>> iter = attributes.entrySet().iterator();
        if (iter.hasNext()) {
            Entry<String, String> o = iter.next();
            append(result, o);
            while (iter.hasNext()) {
                o = iter.next();
                result.append(";");
                append(result, o);
            }
        }

        return result.toString();
    }

    private static void append(StringBuilder b, Entry<String, String> o) {
        if (o.getKey().equalsIgnoreCase(DRIVER_KEYWORD)) {
            b.append(o.getKey()).append("={").append(o.getValue()).append("}");
        } else {
            b.append(o.getKey()).append("=").append(o.getValue());
        }
    }

    @StaticFactoryMethod
    public static @NonNull OdbcConnectionString parse(@NonNull CharSequence input) {
        OdbcConnectionString.Builder result = OdbcConnectionString.builder();
        Matcher m = KEY_VALUE.matcher(input);
        while (m.find()) {
            result.with(m.group(1), m.group(2));
        }
        return result.build();
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private final LinkedHashSet<String> order = new LinkedHashSet<>();
        private final TreeMap<String, String> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        public @NonNull Builder with(@NonNull String key, @NonNull String value) {
            order.add(key);
            attributes.put(key, value);
            return this;
        }

        public @NonNull OdbcConnectionString build() {
            LinkedHashMap<String, String> result = new LinkedHashMap<>();
            order.forEach(o -> result.put(o, attributes.get(o)));
            return new OdbcConnectionString(Collections.unmodifiableMap(result));
        }
    }

    private static final String DRIVER_KEYWORD = "DRIVER";
    private static final Pattern KEY_VALUE = Pattern.compile(
            "(\\w+)"
                    + "="
                    + "\\{*"
                    + "((?<=\\{)[^\\{\\}]+(?=\\})|[^\\s;]+)"
                    + "\\}*"
                    + ";?"
    );
}
