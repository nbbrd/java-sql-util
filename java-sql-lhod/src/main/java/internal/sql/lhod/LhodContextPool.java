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
package internal.sql.lhod;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class LhodContextPool {

    @lombok.NonNull
    private final Clock clock;

    @lombok.NonNull
    private final Duration ttl;

    @lombok.NonNull
    private final LinkedList<LhodContext> recycled; 

    public LhodContext getOrCreate(TabularDataExecutor executor, String connectionString) {
        Instant now = clock.instant();
        synchronized (recycled) {
            Iterator<LhodContext> iter = recycled.iterator();
            while (iter.hasNext()) {
                LhodContext result = iter.next();
                if (!isValid(result, now)) {
                    iter.remove();
                } else if (result.getConnectionString().equals(connectionString)) {
                    iter.remove();
                    return result;
                }
            }
        }
        return LhodContext.of(executor, connectionString, now);
    }

    public void recycle(LhodContext obj) {
        Instant now = clock.instant();
        if (isValid(obj, now)) {
            synchronized (recycled) {
                recycled.add(obj);
            }
        }
    }

    private boolean isValid(LhodContext context, Instant now) {
        return context.getCreation().plus(ttl).isAfter(now);
    }
}
