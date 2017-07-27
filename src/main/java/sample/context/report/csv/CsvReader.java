package sample.context.report.csv;

import java.io.*;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import sample.InvocationException;
import lombok.*;

/**
 * This utility supports CSV loading processing.
 */
@Data
@AllArgsConstructor
public class CsvReader {

    private byte[] data;
    private InputStream ins;
    private CsvLayout layout = new CsvLayout();

    public static CsvReader of(byte[] data) {
        return new CsvReader(data, null, new CsvLayout());
    }

    public static CsvReader of(byte[] data, CsvLayout layout) {
        return new CsvReader(data, null, layout);
    }

    public static CsvReader of(InputStream ins) {
        return new CsvReader(null, ins, new CsvLayout());
    }

    public static CsvReader of(InputStream ins, CsvLayout layout) {
        return new CsvReader(null, ins, layout);
    }

    /** true when reading via binary resource */
    public boolean fromBinary() {
        return data != null;
    }

    /** Perform CSV loading processing.*/
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
                IOUtils.closeQuietly(ins);
            }
        }
    }

    /** Perform CSV loading processing.*/
    public void readStream(final InputStream in, final CsvReadLine logic) throws Exception {
        PushbackReader reader = new PushbackReader(new InputStreamReader(in, layout.getCharset()), 2);
        try {
            int lineNum = 0;
            while (hasNext(in, reader)) {
                lineNum++;
                // Also read the line in the header definition and move seek forward
                List<String> row = readStreamLine(reader);
                if (lineNum == 1 && StringUtils.isNotBlank(layout.getHeader())) {
                    continue; // Skip the first line when header definition exists
                }
                logic.execute(lineNum, row);
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /** check line presence */
    private boolean hasNext(final InputStream in, final PushbackReader reader) throws Exception {
        in.available();
        int i = reader.read();
        if (i != -1) {
            reader.unread(i);
            return true;
        }
        return false;
    }

    /** get row string from InputStream and execute parseLine */
    private List<String> readStreamLine(final PushbackReader reader) throws Exception {
        boolean inQt = false;
        char qt = layout.getQuote();
        String eol = layout.getEolSymbols();
        StringBuilder sb = new StringBuilder();
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
                        } else { // EOL
                            reader.unread(Character.toChars(cp));
                            break;
                        }
                    }
                    if (len % 2 != 0) {
                        inQt = (len != 1);
                    } else {
                        inQt = true;
                    }
                } else if (!layout.isNonQuote()) {
                    inQt = true;
                }
            }
            if (!inQt && sb.toString().endsWith(eol)) { // row processing
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

    /** returns the next character position considering the surrogate pair */
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
                throw new IOException("detected unexpected surrogate pairs. [" + String.valueOf(ch) + ", " + String.valueOf(lo) + "]");
            }
        }
        return String.valueOf(ch).codePointAt(0);
    }

    private String stripEol(StringBuilder sb) {
        return sb.substring(0, sb.length() - layout.getEolSymbols().length());
    }

    /** it parses the CSV string and returns a column list */
    public List<String> parseRow(String row) {
        int pdelim = String.valueOf(layout.getDelim()).codePointAt(0);
        int pquote = String.valueOf(layout.getQuote()).codePointAt(0);
        List<String> columns = new ArrayList<>();
        StringBuilder column = new StringBuilder();
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
                } else if (!layout.isNonQuote()) {
                    inQuote = true;
                    column.append(Character.toChars(c));
                } else {
                    column.append(Character.toChars(c));
                }
            } else if (c == pdelim && !inQuote) { // column switching
                columns.add(unescape(StringUtils.trimToEmpty(column.toString())));
                column = new StringBuilder();
                inQuote = false;
            } else { // append at the end
                column.append(Character.toChars(c));
            }
        }
        columns.add(unescape(StringUtils.trimToEmpty(column.toString())));
        return columns;
    }

    private String unescape(String input) {
        if (StringUtils.isBlank(input) || layout.isNonQuote()) {
            return input;
        }
        char delim = layout.getDelim();
        char quote = layout.getQuote();
        String quoteStr = String.valueOf(quote);
        String eolStr = layout.getEolSymbols();
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

    /**
     * It expresses row level CSV reading processing.
     */
    public static interface CsvReadLine {
        /**
         * @param lineNum line number (1 start)
         * @param cols column list
         */
        void execute(int lineNum, final List<String> cols);
    }

}
