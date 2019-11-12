/*
 * Copyright 2015 National Bank of Belgium
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
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class LhodResultSet extends _ResultSet {

    @lombok.NonNull
    private final TabDataReader reader;

    private final DateFormat dateFormat = newDateFormat();
    private final NumberFormat numberFormat = newNumberFormat();

    @Override
    public boolean next() throws SQLException {
        checkState();
        try {
            return reader.readNextRow();
        } catch (IOException ex) {
            throw ex instanceof TabDataRemoteError
                    ? new SQLException(ex.getMessage(), "", ((TabDataRemoteError) ex).getNumber())
                    : new SQLException("While reading next row", ex);
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        try {
            return reader.isClosed();
        } catch (IOException ex) {
            throw new SQLException("Failed to check reader state", ex);
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new SQLException("While closing reader", ex);
        }
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkState();
        return LhodResultSetMetaData.of(reader.getColumns());
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return get(columnIndex);
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return get(columnIndex);
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return new Date(parseDate(columnIndex).getTime());
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return new Timestamp(parseDate(columnIndex).getTime());
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).doubleValue();
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).floatValue();
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).longValue();
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).intValue();
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return parseNumber(columnIndex).shortValue();
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return new BigDecimal(get(columnIndex));
    }

    private java.util.Date parseDate(int columnIndex) throws SQLException {
        try {
            return dateFormat.parse(get(columnIndex));
        } catch (ParseException ex) {
            throw new SQLException("While parsing date", ex);
        }
    }

    private Number parseNumber(int columnIndex) throws SQLException {
        try {
            return numberFormat.parse(get(columnIndex));
        } catch (ParseException ex) {
            throw new SQLException("While parsing number", ex);
        }
    }

    private String get(int columnIndex) {
        return reader.get(columnIndex - 1);
    }

    private void checkState() throws SQLException {
        if (isClosed()) {
            throw new SQLException("ResultSet closed");
        }
    }

    private static final Locale EN_US = new Locale("en", "us");

    private static DateFormat newDateFormat() {
        DateFormat result = new SimpleDateFormat("MM/dd/yyyy", EN_US);
        result.setLenient(false);
        return result;
    }

    private static NumberFormat newNumberFormat() {
        return NumberFormat.getInstance(EN_US);
    }
}
