/*
 * Copyright 2016 National Bank of Belgium
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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
final class TabDataReader implements Closeable {

    @NonNull
    static TabDataReader of(@NonNull BufferedReader reader) throws IOException {
        List<TabDataColumn> columns = readColumnHeaders(reader);
        return new TabDataReader(reader, columns, new String[columns.size()]);
    }

    private static final String DELIMITER = "\t";

    private final BufferedReader reader;

    @lombok.Getter
    private final List<TabDataColumn> columns;

    private final String[] currentRow;

    private boolean closed = false;

    public boolean isClosed() throws IOException {
        return closed;
    }

    public boolean readNextRow() throws IOException {
        String line = readNextLine(reader);
        if (line != null) {
            splitInto(line, currentRow);
            return true;
        }
        return false;
    }

    public String get(int index) {
        return currentRow[index];
    }

    @Override
    public void close() throws IOException {
        closed = true;
        reader.close();
    }

    private static String[] split(String line) {
        return line.split(DELIMITER, -1);
    }

    private static void splitInto(String line, String[] array) {
        int start = 0;
        for (int i = 0; i < array.length - 1; i++) {
            int stop = line.indexOf(DELIMITER, start);
            array[i] = line.substring(start, stop);
            start = stop + DELIMITER.length();
        }
        array[array.length - 1] = line.substring(start);
    }

    @Nullable
    private static String readNextLine(@NonNull BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line != null && line.isEmpty()) {
            throw parseError(reader);
        }
        return line;
    }

    private static TabDataRemoteError parseError(@NonNull BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line != null && !line.isEmpty()) {
            int index = line.indexOf(DELIMITER);
            if (index != -1) {
                try {
                    return new TabDataRemoteError(line.substring(index + DELIMITER.length()), Integer.parseInt(line.substring(0, index)));
                } catch (NumberFormatException ex) {
                    throw new TabDataFormatError("Cannot parse error code", ex);
                }
            }
        }
        throw new TabDataFormatError("Expected error description on next row");
    }

    private static List<TabDataColumn> readColumnHeaders(BufferedReader reader) throws IOException {
        String[] names = readHeader(reader, "names");
        String[] types = readHeader(reader, "types");

        if (names.length != types.length) {
            throw new TabDataFormatError(format("Invalid data type length: expected '%s', found '%s'", names.length, types.length));
        }

        List<TabDataColumn> result = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            try {
                result.add(new TabDataColumn(names[i], Integer.parseInt(types[i])));
            } catch (NumberFormatException ex) {
                throw new TabDataFormatError("Cannot parse type code", ex);
            }
        }
        return result;
    }

    private static String[] readHeader(BufferedReader reader, String id) throws IOException {
        String header = readNextLine(reader);
        if (header == null) {
            throw new TabDataFormatError(format("Expected header %s", id));
        }
        if (header.isEmpty()) {
            throw parseError(reader);
        }
        return split(header);
    }
}
