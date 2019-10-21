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

import java.sql.SQLException;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
public final class UncheckedSQLException extends RuntimeException {

    public UncheckedSQLException(String message, SQLException cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public UncheckedSQLException(SQLException cause) {
        super(Objects.requireNonNull(cause));
    }

    @Override
    public SQLException getCause() {
        return (SQLException) super.getCause();
    }
}
