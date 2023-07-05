package sample.context.report.csv;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.*;
import sample.context.InvocationException;

/**
 * CSVの書き出し処理をサポートするユーティリティ。
 */
@Data
@AllArgsConstructor
public class CsvWriter {

    private File file;
    private OutputStream out;
    private CsvLayout layout = new CsvLayout();

    public static CsvWriter of(final File file) {
        return new CsvWriter(file, null, new CsvLayout());
    }

    public static CsvWriter of(final File file, final CsvLayout layout) {
        return new CsvWriter(file, null, layout);
    }

    public static CsvWriter of(final OutputStream out) {
        return new CsvWriter(null, out, new CsvLayout());
    }

    public static CsvWriter of(final OutputStream out, final CsvLayout layout) {
        return new CsvWriter(null, out, layout);
    }

    /** ファイルリソース経由での読み込み時にtrue */
    public boolean fromFile() {
        return file != null;
    }

    /**
     * CSV書出処理(上書き)を行います。
     * <p>
     * CsvWrite#appendRow 呼び出すタイミングでファイルへ随時書き出しが行われます。
     *
     * @param logic
     */
    public void write(final CsvWrite logic) {
        OutputStream out = null;
        try {
            out = fromFile() ? FileUtils.openOutputStream(file) : this.out;
            CsvStream stream = new CsvStream(layout, out);
            logic.execute(stream);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationException(e);
        } finally {
            if (fromFile()) {
                closeQuietly(out);
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
     * CSV書出処理(追記)を行います。
     * <p>
     * CsvWrite#appendRow 呼び出すタイミングでファイルへ随時書き出しが行われます。
     * <p>
     * ファイル出力時のみ利用可能です。
     *
     * @param logic
     */
    public void writeAppend(final CsvWrite logic) {
        if (!fromFile())
            throw new UnsupportedOperationException("CSV書出処理の追記はファイル出力時のみサポートされます");
        FileOutputStream out = null;
        try {
            out = FileUtils.openOutputStream(file, true);
            CsvStream stream = new CsvStream(layout, out);
            logic.execute(stream);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationException(e);
        } finally {
            closeQuietly(out);
        }
    }

    public static class CsvStream {
        private CsvLayout layout;
        private OutputStream out;

        public CsvStream(CsvLayout layout, OutputStream out) {
            this.layout = layout;
            this.out = out;
            if (layout.hasHeader()) {
                appendRow(layout.headerCols());
            }
        }

        public CsvStream appendRow(List<Object> cols) {
            try {
                out.write(row(cols).getBytes(layout.getCharset()));
                out.write(layout.getEolSymbols().getBytes());
                return this;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        public String row(List<Object> cols) {
            List<String> row = new ArrayList<>();
            for (Object col : cols) {
                if (col instanceof String) {
                    row.add(escape(col.toString()));
                } else {
                    if (col == null) {
                        row.add("");
                    } else {
                        row.add(col.toString());
                    }
                }
            }
            return StringUtils.join(row, ",");
        }

        private String escape(String s) {
            if (layout.isNonQuote()) {
                return s;
            }
            char delim = layout.getDelim();
            char quote = layout.getQuote();
            String quoteStr = String.valueOf(quote);
            String eol = layout.getEolSymbols();
            if (StringUtils.containsNone(s, delim, quote) && StringUtils.containsNone(s, eol)) {
                return quoteStr + s + quoteStr;
            } else {
                return quoteStr + StringUtils.replace(s, quoteStr, quoteStr + quoteStr) + quoteStr;
            }
        }
    }

    /** CSV出力処理を表現します。 */
    public static interface CsvWrite {
        /**
         * @param stream 出力CSVインスタンス
         */
        void execute(final CsvStream stream);
    }

}
