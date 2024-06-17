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
package internal.sql.lhod.vbs;

import internal.sql.lhod.TabDataEngine;
import internal.sql.lhod.TabDataExecutor;
import internal.sys.CachedResourceExtractor;
import internal.sys.DefaultResourceExtractor;
import internal.sys.ResourceExtractor;
import lombok.NonNull;

import java.io.IOException;

/**
 *
 * @author Philippe Charles
 */
public final class VbsEngine implements TabDataEngine {

    private final ResourceExtractor scripts = CachedResourceExtractor.of(DefaultResourceExtractor.of(VbsEngine.class));

    @Override
    public @NonNull TabDataExecutor getExecutor() {
        return new VbsExecutor(scripts);
    }
}
