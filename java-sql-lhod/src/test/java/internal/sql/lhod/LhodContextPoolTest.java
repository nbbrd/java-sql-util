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

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedList;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class LhodContextPoolTest {

    @Test
    public void test() {
        FakeClock clock = new FakeClock();
        LinkedList<LhodContext> list = new LinkedList<>();
        LhodContextPool pool = LhodContextPool.of(clock, Duration.ofMillis(10), list);

        LhodContext context;

        context = pool.getOrCreate(NoOpExecutor.INSTANCE, "abc");
        assertThat(list).isEmpty();

        assertThat(context).isNotSameAs(pool.getOrCreate(NoOpExecutor.INSTANCE, "abc"));
        assertThat(list).isEmpty();

        pool.recycle(context);
        assertThat(list).hasSize(1);

        assertThat(context).isSameAs(pool.getOrCreate(NoOpExecutor.INSTANCE, "abc"));
        assertThat(list).isEmpty();

        clock.epochMilli += 10;
        pool.recycle(context);
        assertThat(list).isEmpty();

        context = pool.getOrCreate(NoOpExecutor.INSTANCE, "abc");
        pool.recycle(context);
        clock.epochMilli += 10;
        assertThat(list).hasSize(1);
        assertThat(context).isNotSameAs(pool.getOrCreate(NoOpExecutor.INSTANCE, "abc"));
        assertThat(list).isEmpty();

        pool.recycle(pool.getOrCreate(NoOpExecutor.INSTANCE, "abc"));
        assertThat(list).hasSize(1);
        pool.recycle(pool.getOrCreate(NoOpExecutor.INSTANCE, "123"));
        assertThat(list).hasSize(2);
    }

    private static final class FakeClock extends Clock {

        private long epochMilli;

        @Override
        public ZoneId getZone() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(epochMilli);
        }
    }

    private enum NoOpExecutor implements TabularDataExecutor {
        INSTANCE;

        @Override
        public TabularDataReader exec(TabularDataQuery query) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
