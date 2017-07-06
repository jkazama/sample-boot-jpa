package sample.context.report.csv;

import java.io.*;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.*;
import sample.InvocationException;

/**
 * CSVの読込処理をサポートするユーティリティです。
 */
@Data
@AllArgsConstructor
public class CsvReader {

    private byte[] data;
    private InputStream ins;
    private CsvLayout layout = new CsvLayout();

    /** バイナリリソース経由での読み込み時にtrue */
    public boolean fromBinary() {
        return data != null;
    }

    /**
     * CSV読込処理を行います。
     * <p>大量データ処理を想定してメモリ内に全展開するのではなく、Iteratorを用いた
     * 行処理形式を利用しています。
     * @param logic
     */
    public void read(final CsvReadLine logic) {
        InputStream ins = null;
        try {
            ins = fromBinary() ? new ByteArrayInputStream(data) : this.ins;
            readStream(ins, logic);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationException("リソース処理中に例外が発生しました", e);
        } finally {
            if (fromBinary()) {
                IOUtils.closeQuietly(ins);
            }
        }
    }

    public void readStream(final InputStream in, final CsvReadLine logic) throws Exception {
        PushbackReader reader = new PushbackReader(new InputStreamReader(in, layout.getCharset()), 2);
        try {
            int lineNum = 0;
            boolean title = false;
            while (hasNext(in, reader)) {
                lineNum++;
                List<String> row = readStreamLine(reader); // ヘッダ定義でも行を読み込み、シークを先に進める
                if (lineNum == 1 && StringUtils.isNotBlank(layout.getHeader())) {
                    title = true;
                    continue; // ヘッダ定義存在時は最初の行をスキップ
                }
                logic.execute(title ? lineNum - 1 : lineNum, row);
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /** 行の存在判定を行います */
    private boolean hasNext(final InputStream in, final PushbackReader reader) throws Exception {
        in.available();
        int i = reader.read();
        if (i != -1) {
            reader.unread(i);
            return true;
        }
        return false;
    }

    /** InputStreamから行文字列を取得してparseLineを実行します */
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
                        if (qt == cp) {// エスケープ
                            len++;
                            sb.appendCodePoint(cp);
                        } else { // 終端
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
            if (!inQt && sb.toString().endsWith(eol)) { // 行処理
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

    /** サロゲートペアを考慮した次の文字位置を返します */
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
                throw new IOException("想定外のサロゲートペアを検出しました。[" + String.valueOf(ch) + ", " + String.valueOf(lo) + "]");
            }
        }
        return String.valueOf(ch).codePointAt(0);
    }

    private String stripEol(StringBuilder sb) {
        return sb.substring(0, sb.length() - layout.getEolSymbols().length());
    }

    /** CSV文字列を解析して列一覧を返します */
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
            } else if (c == pdelim && !inQuote) { // 列切替
                columns.add(unescape(StringUtils.trimToEmpty(column.toString())));
                column = new StringBuilder();
                inQuote = false;
            } else { // 末尾追記
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

    /** 行レベルのCSV読込処理を表現します。  */
    public static interface CsvReadLine {
        /**
         * @param lineNum 実行行番号(1開始)
         * @param cols 解析された列一覧
         */
        void execute(int lineNum, final List<String> cols);
    }

}
