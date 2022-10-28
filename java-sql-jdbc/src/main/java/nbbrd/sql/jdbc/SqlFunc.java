/*
 * Copyright 2017 National Bank of Belgium
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

import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.sql.SQLException;
import java.util.Objects;

/**
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @author Philippe Charles
 */
@FunctionalInterface
public interface SqlFunc<T, R> {

    R applyWithSql(T t) throws SQLException;

    default <V> @NonNull SqlFunc<V, R> compose(@NonNull SqlFunc<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> applyWithSql(before.applyWithSql(v));
    }

    default <V> @NonNull SqlFunc<T, V> andThen(@NonNull SqlFunc<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.applyWithSql(applyWithSql(t));
    }

    @StaticFactoryMethod
    static @NonNull <T> SqlFunc<T, T> identity() {
        return t -> t;
    }
}
