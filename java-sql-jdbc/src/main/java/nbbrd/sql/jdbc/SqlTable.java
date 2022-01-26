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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
public class SqlTable {

    /**
     * Creates a complete list of tables in a database.
     *
     * @param md a non-null database metadata
     * @return a non-null list of tables
     * @throws SQLException if a database access error occurs
     * @see DatabaseMetaData#getTables(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    public static @NonNull List<SqlTable> allOf(@NonNull DatabaseMetaData md) throws SQLException {
        return allOf(md, ALL_CATALOGS, ALL_SCHEMAS, ALL_TABLE_NAMES, ALL_TYPES);
    }

    public static final String ALL_CATALOGS = null;
    public static final String ALL_SCHEMAS = null;
    public static final String ALL_TABLE_NAMES = "%";
    public static final String[] ALL_TYPES = null;

    /**
     * Creates a partial list of tables in a database by using patterns.
     *
     * @param md               a non-null database metadata
     * @param catalog          a catalog name; must match the catalog name as it is
     *                         stored in the database; "" retrieves those without a catalog;
     *                         <code>null</code> means that the catalog name should not be used to
     *                         narrow the search
     * @param schemaPattern    a schema name pattern; must match the schema name as
     *                         it is stored in the database; "" retrieves those without a schema;
     *                         <code>null</code> means that the schema name should not be used to narrow
     *                         the search
     * @param tableNamePattern a table name pattern; must match the table name
     *                         as it is stored in the database
     * @param types            a list of table types, which must be from the list of table
     *                         types returned from {@link #getTableTypes},to include; <code>null</code>
     *                         returns all types
     * @return a non-null list of tables
     * @throws SQLException if a database access error occurs
     * @see DatabaseMetaData#getTables(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    public static @NonNull List<SqlTable> allOf(
            @NonNull DatabaseMetaData md,
            @Nullable String catalog,
            @Nullable String schemaPattern,
            @NonNull String tableNamePattern,
            @Nullable String[] types
    ) throws SQLException {
        Objects.requireNonNull(md, "md");
        Objects.requireNonNull(tableNamePattern, "tableNamePattern");

        try (ResultSet rs = md.getTables(catalog, schemaPattern, tableNamePattern, types)) {
            return allOf(rs);
        }
    }

    private static List<SqlTable> allOf(ResultSet tables) throws SQLException {
        // some infos are not supported by all drivers!
        String[] normalizedColumnNames = getNormalizedColumnNames(tables.getMetaData());

        List<SqlTable> result = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        while (tables.next()) {
            for (int i = 0; i < normalizedColumnNames.length; i++) {
                row.put(normalizedColumnNames[i], tables.getString(i + 1));
            }
            result.add(fromMap(row));
        }
        return result;
    }

    private static String[] getNormalizedColumnNames(ResultSetMetaData md) throws SQLException {
        String[] columnNames = new String[md.getColumnCount()];
        for (int i = 0; i < columnNames.length; i++) {
            // normalize to upper case (postgresql driver returns lower case)
            columnNames[i] = md.getColumnName(i + 1).toUpperCase(Locale.ROOT);
        }
        return columnNames;
    }

    private static @NonNull SqlTable fromMap(@NonNull Map<String, String> map) {
        return new SqlTable(
                get(map, "TABLE_CAT", "TABLE_CATALOG"),
                get(map, "TABLE_SCHEM", "TABLE_SCHEMA"),
                nullToEmpty(get(map, "TABLE_NAME")),
                nullToEmpty(get(map, "TABLE_TYPE")),
                get(map, "REMARKS"),
                get(map, "TYPE_CAT"),
                get(map, "TYPE_SCHEM"),
                get(map, "TYPE_NAME"),
                get(map, "SELF_REFERENCING_COL_NAME"),
                get(map, "REF_GENERATION"));
    }

    private static @NonNull String nullToEmpty(@Nullable String o) {
        return (o == null) ? "" : o;
    }

    private static @Nullable String get(@NonNull Map<String, String> map, String... keys) {
        for (String key : keys) {
            String result = map.get(key);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * table catalog (may be <code>null</code>)
     */
    String catalog;

    /**
     * table schema (may be <code>null</code>)
     */
    String schema;

    /**
     * table name
     */
    @lombok.NonNull
    String name;

    /**
     * table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL
     * TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     */
    @NonNull
    String type;

    /**
     * explanatory comment on the table
     */
    String remarks;

    /**
     * the types catalog (may be <code>null</code>)
     */
    String typesCatalog;

    /**
     * the types schema (may be <code>null</code>)
     */
    String typesSchema;

    /**
     * type name (may be <code>null</code>)
     */
    String typeName;

    /**
     * String => name of the designated "identifier" column of a typed table
     * (may be <code>null</code>)
     */
    String selfReferencingColumnName;

    /**
     * specifies how values in SELF_REFERENCING_COL_NAME are created. Values are
     * "SYSTEM", "USER", "DERIVED". (may be <code>null</code>)
     */
    String refGeneration;
}
