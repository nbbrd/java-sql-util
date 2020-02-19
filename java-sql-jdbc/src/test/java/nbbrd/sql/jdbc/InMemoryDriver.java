/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nbbrd.sql.jdbc;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumSet;

/**
 *
 * @author charphi
 */
@lombok.Getter
@lombok.AllArgsConstructor
public enum InMemoryDriver {
    H2("jdbc:h2:mem:test", SqlIdentifierQuoter.DEFAULT.getQuoteString(), SqlIdentifierStorageRule.UPPER, "", true),
    HSQLDB("jdbc:hsqldb:mem:test", SqlIdentifierQuoter.DEFAULT.getQuoteString(), SqlIdentifierStorageRule.UPPER, "", false),
    DERBY("jdbc:derby:memory:test;create=true", SqlIdentifierQuoter.DEFAULT.getQuoteString(), SqlIdentifierStorageRule.UPPER, "", true),
    SQLITE("jdbc:sqlite::memory:", SqlIdentifierQuoter.DEFAULT.getQuoteString(), SqlIdentifierStorageRule.MIXED, "", true);

    private final String url;
    private final String quoteString;
    private final SqlIdentifierStorageRule storageRule;
    private final String extraNameCharacters;
    private final boolean customKeywords;

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public static final OutputStream DEV_NULL = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };

    static {
        // disable derby log file
        System.setProperty("derby.stream.error.field", InMemoryDriver.class.getName() + ".DEV_NULL");
    }

    // FIXME: should not have exceptions
    public static EnumSet<InMemoryDriver> not(InMemoryDriver... drivers) {
        return EnumSet.complementOf(EnumSet.copyOf(Arrays.asList(drivers)));
    }
}
