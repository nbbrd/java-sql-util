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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
public class DefaultResourceExtractor implements ResourceExtractor {

    @lombok.NonNull File repository;

    boolean persist;

    @lombok.NonNull Class<?> anchor;

    @Override
    public @NonNull File getResourceAsFile(@NonNull String resourceName) throws IOException {
        try (InputStream stream = anchor.getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new FileNotFoundException(resourceName);
            }
            Path result = createEmptyFile(repository.toPath(), resourceName);
            Files.copy(stream, result, StandardCopyOption.REPLACE_EXISTING);
            if (!persist) {
                result.toFile().deleteOnExit();
            }
            return result.toFile();
        }
    }

    public static Builder builder() {
        return new Builder()
                .repository(getTempFolder().toFile())
                .persist(false);
    }

    public static DefaultResourceExtractor of(Class<?> anchor) {
        return builder().anchor(anchor).build();
    }

    private static Path getTempFolder() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    private static Path createEmptyFile(Path parent, String resourceName) throws IOException {
        int idx = resourceName.lastIndexOf(".");
        return idx != -1
                ? Files.createTempFile(parent, "rsrc", resourceName.substring(idx))
                : Files.createTempFile(parent, "rsrc", resourceName);
    }

    // fix javadoc
    public static final class Builder {
    }
}
