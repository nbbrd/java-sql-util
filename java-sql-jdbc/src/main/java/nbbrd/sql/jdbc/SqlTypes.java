/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import lombok.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SqlTypes {

    public java.util.@NonNull Date getJavaDate(java.sql.@NonNull Date date) {
        return new java.util.Date(date.getTime());
    }

    public java.util.@NonNull Date getJavaDate(java.sql.@NonNull Timestamp timestamp) {
        return new java.util.Date(timestamp.getTime() + (timestamp.getNanos() / 1000000));
    }
}
