package sample.context.report.csv;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;
import sample.context.InvocationException;

/**
 * This utility supports CSV read processing.
 */
@RequiredArgsConstructor(staticName = "of")
public class CsvReader {
    private final byte[] data;
    private final InputStream ins;
    private final CsvLayout layout;

    /** true when reading via binary resource */
    public boolean fromBinary() {
        return data != null;
    }

    /**
     * CSV reading process.
     * <p>
     * Instead of expanding all of the data in memory for mass data processing, a
     * row processing format using Iterator is used.
     */
    public void read(final CsvReadLine logic) {
        InputStream ins = null;
        try {
            ins = fromBinary() ? new ByteArrayInputStream(data) : this.ins;
            readStream(ins, logic);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationException("An exception occurred during resource processing", e);
        } finally {
            if (fromBinary()) {
                closeQuietly(ins);
            }
        }
    }

    private void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
        }
    }

    /**
     * CSV reading process.
     * <p>
     * Instead of expanding all of the data in memory for mass data processing, a
     * row processing format using Iterator is used.
     */
    public void readStream(final InputStream in, final CsvReadLine logic) throws Exception {
        var reader = new PushbackReader(new InputStreamReader(in, layout.charset()), 2);
        try {
            int lineNum = 0;
            boolean title = false;
            while (hasNext(in, reader)) {
                lineNum++;
                List<String> row = readStreamLine(reader); // Read rows even in header definitions and advance the seek
                if (lineNum == 1 && StringUtils.isNotBlank(layout.header())) {
                    title = true;
                    continue; // Skip first line if header definition exists
                }
                logic.execute(title ? lineNum - 1 : lineNum, row);
            }
        } finally {
            closeQuietly(reader);
        }
    }

    private boolean hasNext(final InputStream in, final PushbackReader reader) throws Exception {
        in.available();
        int i = reader.read();
        if (i != -1) {
            reader.unread(i);
            return true;
        }
        return false;
    }

    private List<String> readStreamLine(final PushbackReader reader) throws Exception {
        boolean inQt = false;
        char qt = layout.quote();
        String eol = layout.eolSymbols();
        var sb = new StringBuilder();
        int cp = -1;
        while ((cp = nextCodePoint(reader)) != -1) {
            sb.appendCodePoint(cp);
            if (qt == cp) {
                if (inQt) {
                    int len = 1;
                    while ((cp = nextCodePoint(reader)) != -1) {
                        if (qt == cp) {// escape
                            len++;
                            sb.appendCodePoint(cp);
                        } else { // eol
                            reader.unread(Character.toChars(cp));
                            break;
                        }
                    }
                    if (len % 2 != 0) {
                        inQt = (len != 1);
                    } else {
                        inQt = true;
                    }
                } else if (!layout.nonQuote()) {
                    inQt = true;
                }
            }
            if (!inQt && sb.toString().endsWith(eol)) { // line processing
                return parseRow(stripEol(sb));
            }
        }
        if (sb.length() > 0) {
            if (sb.toString().endsWith(eol)) {
                return parseRow(stripEol(sb));
            } else {
                return parseRow(sb.toString());
            }
        }
        return new ArrayList<>();
    }

    /** Returns the next character position considering surrogate pairs */
    private int nextCodePoint(final PushbackReader r) throws IOException {
        int i = r.read();
        if (i == -1) {
            return -1;
        }
        char ch = (char) i;
        if (Character.isHighSurrogate(ch)) {
            char lo = (char) r.read();
            if (Character.isLowSurrogate(lo)) {
                return Character.toCodePoint(ch, lo);
            } else {
                throw new IOException("An unexpected surrogate pair was detected.[" + String.valueOf(ch) + ", "
                        + String.valueOf(lo) + "]");
            }
        }
        return String.valueOf(ch).codePointAt(0);
    }

    private String stripEol(StringBuilder sb) {
        return sb.substring(0, sb.length() - layout.eolSymbols().length());
    }

    /** parses a CSV string and returns a list of columns */
    public List<String> parseRow(String row) {
        int pdelim = String.valueOf(layout.delim()).codePointAt(0);
        int pquote = String.valueOf(layout.quote()).codePointAt(0);
        List<String> columns = new ArrayList<>();
        var column = new StringBuilder();
        boolean inQuote = false;
        int max = row.codePointCount(0, row.length());
        for (int i = 0; i < max; i = row.offsetByCodePoints(i, 1)) {
            int c = row.codePointAt(i);
            if (c == pquote) {
                if (inQuote) {
                    int cnt = 1;
                    column.append(Character.toChars(c));
                    int next = row.offsetByCodePoints(i, 1);
                    for (; next < max; next = row.offsetByCodePoints(next, 1)) {
                        int c2 = row.codePointAt(next);
                        if (c2 != pquote) {
                            break;
                        } else {
                            column.append(Character.toChars(c2));
                            cnt++;
                            i = next;
                        }
                    }
                    if (cnt % 2 != 0) {
                        inQuote = false;
                    }
                } else if (!layout.nonQuote()) {
                    inQuote = true;
                    column.append(Character.toChars(c));
                } else {
                    column.append(Character.toChars(c));
                }
            } else if (c == pdelim && !inQuote) { // column switching
                columns.add(unescape(StringUtils.trimToEmpty(column.toString())));
                column = new StringBuilder();
                inQuote = false;
            } else { // postscript
                column.append(Character.toChars(c));
            }
        }
        columns.add(unescape(StringUtils.trimToEmpty(column.toString())));
        return columns;
    }

    private String unescape(String input) {
        if (StringUtils.isBlank(input) || layout.nonQuote()) {
            return input;
        }
        char delim = layout.delim();
        char quote = layout.quote();
        String quoteStr = String.valueOf(quote);
        String eolStr = layout.eolSymbols();
        List<String> eols = new ArrayList<>(eolStr.length());
        for (int i = 0, n = eolStr.codePointCount(0, eolStr.length()); i < n; i++) {
            eols.add(String.valueOf(Character.toChars(eolStr.codePointAt(i))));
        }
        if (input.charAt(0) != quote || input.charAt(input.length() - 1) != quote) {
            return input;
        }
        String quoteless = input.subSequence(1, input.length() - 1).toString();
        String unescape;
        boolean eolsAny = false;
        for (String eol : eols) {
            if (StringUtils.containsAny(quoteless, eol)) {
                eolsAny = true;
                break;
            }
        }
        if (StringUtils.containsAny(quoteless, delim, quote) || eolsAny) {
            unescape = StringUtils.replace(quoteless, quoteStr + quoteStr, quoteStr);
        } else {
            unescape = input;
        }
        int q1 = unescape.indexOf(quote);
        int q2 = unescape.lastIndexOf(quoteStr);
        if (q1 != q2 && q1 == 0 && unescape.endsWith(quoteStr)) {
            return unescape.substring(1, q2);
        }
        return unescape;
    }

    public static CsvReader of(byte[] data) {
        return CsvReader.of(data, null, CsvLayout.simple());
    }

    public static CsvReader of(byte[] data, CsvLayout layout) {
        return CsvReader.of(data, null, layout);
    }

    public static CsvReader of(InputStream ins) {
        return CsvReader.of(null, ins, CsvLayout.simple());
    }

    public static CsvReader of(InputStream ins, CsvLayout layout) {
        return CsvReader.of(null, ins, layout);
    }

    public static CsvReader of(byte[] data, InputStream ins) {
        return CsvReader.of(data, ins, CsvLayout.simple());
    }

    /** Represents row-level CSV read processing. */
    public static interface CsvReadLine {
        /**
         * @param lineNum Execution line number (1 start)
         * @param cols    List of parsed columns
         */
        void execute(int lineNum, final List<String> cols);
    }

}
