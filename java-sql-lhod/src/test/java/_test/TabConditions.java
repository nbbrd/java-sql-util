package _test;

import internal.sql.lhod.TabDataReader;
import nbbrd.io.function.IOPredicate;
import org.assertj.core.api.Condition;

import java.io.IOException;


public final class TabConditions {

    private TabConditions() {
        // static class
    }

    public static Condition<TabDataReader> rowCount(int count) {
        return new Condition<>(IOPredicate.unchecked(reader -> countRows(reader) == count), "row count is %s", count);
    }

    private static int countRows(TabDataReader reader) throws IOException {
        int result = 0;
        while (reader.readNextRow()) result++;
        return result;
    }
}
