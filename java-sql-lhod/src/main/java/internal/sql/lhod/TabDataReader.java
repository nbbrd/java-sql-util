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

import internal.nbbrd.picocsv.Csv;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TabDataReader implements Closeable {

    private static final Csv.Format FORMAT = Csv.Format.RFC4180.toBuilder().delimiter('\t').build();

    @StaticFactoryMethod
    public static @NonNull TabDataReader of(@NonNull Reader reader) throws IOException {
        Csv.Reader tsv = Csv.Reader.of(FORMAT, Csv.ReaderOptions.DEFAULT, reader);
        List<TabDataColumn> columns = readColumnHeaders(tsv);
        return new TabDataReader(tsv, columns, new String[columns.size()]);
    }

    private final Csv.Reader reader;

    @lombok.Getter
    private final List<TabDataColumn> columns;

    private final String[] currentRow;

    private boolean closed = false;

    public boolean isClosed() throws IOException {
        return closed;
    }

    public boolean readNextRow() throws IOException {
        if (reader.readLine()) {
            if (!reader.readField()) {
                throw parseError(reader);
            }
            int idx = 0;
            do {
                currentRow[idx++] = reader.toString();
            } while (reader.readField());
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

    private static TabDataRemoteError parseError(Csv.Reader reader) throws IOException {
        if (!reader.readLine()) {
            throw new TabDataFormatError("Expected error on next row");
        }

        if (!reader.readField()) {
            throw new TabDataFormatError("Expected error code on next field");
        }
        String errorCode = reader.toString();

        if (!reader.readField()) {
            throw new TabDataFormatError("Expected error description on next field");
        }
        String errorMessage = reader.toString();

        try {
            return new TabDataRemoteError(errorMessage, Integer.parseInt(errorCode));
        } catch (NumberFormatException ex) {
            throw new TabDataFormatError("Cannot parse error code", ex);
        }
    }

    private static List<TabDataColumn> readColumnHeaders(Csv.Reader reader) throws IOException {
        List<String> names = readHeader(reader, "names");
        List<String> types = readHeader(reader, "types");

        if (names.size() != types.size()) {
            throw new TabDataFormatError(format(Locale.ROOT, "Invalid data type length: expected '%s', found '%s'", names.size(), types.size()));
        }

        List<TabDataColumn> result = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            try {
                result.add(new TabDataColumn(names.get(i), Integer.parseInt(types.get(i))));
            } catch (NumberFormatException ex) {
                throw new TabDataFormatError("Cannot parse type code", ex);
            }
        }
        return result;
    }

    private static List<String> readHeader(Csv.Reader reader, String id) throws IOException {
        if (!reader.readLine()) {
            throw new TabDataFormatError(format(Locale.ROOT, "Expected header %s", id));
        }
        if (!reader.readField()) {
            throw parseError(reader);
        }
        List<String> result = new ArrayList<>();
        result.add(reader.toString());
        while (reader.readField()) {
            result.add(reader.toString());
        }
        return result;
    }
}
