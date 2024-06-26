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

import nbbrd.design.StaticFactoryMethod;
import lombok.NonNull;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
public class SqlColumn {

    /**
     * Creates a complete list of columns in a table.
     *
     * @param md a non-null resultset metadata
     * @return a non-null list of columns
     * @throws SQLException if a database access error occurs
     */
    public static @NonNull List<SqlColumn> allOf(@NonNull ResultSetMetaData md) throws SQLException {
        SqlColumn[] result = new SqlColumn[md.getColumnCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = of(md, i + 1);
        }
        return Arrays.asList(result);
    }

    /**
     * Gets a specific column in a table.
     *
     * @param md          a non-null resultset metadata
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a non-null column
     * @throws SQLException if a database access error occurs
     */
    @StaticFactoryMethod
    public static @NonNull SqlColumn of(@NonNull ResultSetMetaData md, int columnIndex) throws SQLException {
        return new SqlColumn(
                md.getColumnClassName(columnIndex),
                md.getColumnDisplaySize(columnIndex),
                md.getColumnLabel(columnIndex),
                md.getColumnName(columnIndex),
                md.getColumnType(columnIndex),
                md.getColumnTypeName(columnIndex));
    }

    /**
     * The column's table's catalog name.
     *
     * @see ResultSetMetaData#getColumnClassName(int)
     */
    String className;

    /**
     * The column's normal maximum width in characters.
     *
     * @see ResultSetMetaData#getColumnDisplaySize(int)
     */
    int displaySize;

    /**
     * The column's suggested title for use in printouts and displays. The
     * suggested title is usually specified by the SQL <code>AS</code> clause.
     * If a SQL <code>AS</code> is not specified, the value returned from
     * <code>getColumnLabel</code> will be the same as the value returned by the
     * <code>getColumnName</code> method.
     *
     * @see ResultSetMetaData#getColumnLabel(int)
     */
    String label;

    /**
     * The column's name.
     *
     * @see ResultSetMetaData#getColumnName(int)
     */
    String name;

    /**
     * The column's SQL type.
     *
     * @see ResultSetMetaData#getColumnType(int)
     */
    int type;

    /**
     * The column's database-specific type name.
     *
     * @see ResultSetMetaData#getColumnTypeName(int)
     */
    String typeName;
}
