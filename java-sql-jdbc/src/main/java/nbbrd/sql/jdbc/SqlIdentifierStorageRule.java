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
package nbbrd.sql.jdbc;

import java.util.Locale;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public enum SqlIdentifierStorageRule {

    UPPER {
        @Override
        public boolean isValid(@NonNull String identifier) {
            return identifier.toUpperCase(Locale.ROOT).equals(identifier);
        }
    }, LOWER {
        @Override
        public boolean isValid(@NonNull String identifier) {
            return identifier.toLowerCase(Locale.ROOT).equals(identifier);
        }
    }, MIXED {
        @Override
        public boolean isValid(@NonNull String identifier) {
            Objects.requireNonNull(identifier);
            return true;
        }
    };

    public abstract boolean isValid(@NonNull String identifier);
}
