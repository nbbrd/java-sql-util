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
package internal.sys;

import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
@lombok.Builder
public final class CachedResourceExtractor implements ResourceExtractor {

    @lombok.NonNull
    private final ResourceExtractor extractor;

    @lombok.NonNull
    private final ConcurrentMap<String, Entry> index;

    @Override
    public @NonNull File getResourceAsFile(@NonNull String resourceName) throws IOException {
        Objects.requireNonNull(resourceName);

        Entry result = index.get(resourceName);
        if (!isValid(result)) {
            result = newEntry(resourceName);
            index.put(resourceName, result);
        }
        return result.getFile();
    }

    private boolean isValid(Entry entry) throws IOException {
        return entry != null && entry.isValidFile();
    }

    private Entry newEntry(String resourceName) throws IOException {
        File file = extractor.getResourceAsFile(resourceName);
        return new Entry(file, file.length(), Files.getLastModifiedTime(file.toPath()));
    }

    public static Builder builder() {
        return new Builder()
                .index(new ConcurrentHashMap<>());
    }

    public static CachedResourceExtractor of(ResourceExtractor extractor) {
        return builder().extractor(extractor).build();
    }

    @lombok.Value
    public static class Entry {

        File file;
        long size;
        FileTime lastModified;

        public boolean isValidFile() throws IOException {
            return file.exists() && file.isFile() && file.canRead()
                    && size == file.length()
                    && lastModified.equals(Files.getLastModifiedTime(file.toPath()));
        }
    }
}
