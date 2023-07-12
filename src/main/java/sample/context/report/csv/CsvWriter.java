package sample.context.report.csv;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;
import sample.context.InvocationException;

/**
 * A utility that supports the CSV export process.
 */
@RequiredArgsConstructor(staticName = "of")
public class CsvWriter {

    private final File file;
    private final OutputStream out;
    private final CsvLayout layout;

    /** true when reading via file resource */
    public boolean fromFile() {
        return file != null;
    }

    /**
     * CSV export process (overwrite).
     * <p>
     * CsvWrite#appendRow Writing is performed as needed to the file at the time of
     * the call.
     */
    public void write(final CsvWrite logic) {
        OutputStream out = null;
        try {
            out = fromFile() ? FileUtils.openOutputStream(file) : this.out;
            var stream = new CsvStream(layout, out);
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
     * CSV export process (append).
     * <p>
     * CsvWrite#appendRow Writing is performed as needed to the file at the time of
     * the call.
     * <p>
     * Available only for file output.
     */
    public void writeAppend(final CsvWrite logic) {
        if (!fromFile())
            throw new UnsupportedOperationException("CSV export process is only supported for file output");
        FileOutputStream out = null;
        try {
            out = FileUtils.openOutputStream(file, true);
            var stream = new CsvStream(layout, out);
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

        public CsvStream appendRow(List<?> cols) {
            try {
                out.write(row(cols).getBytes(layout.charset()));
                out.write(layout.eolSymbols().getBytes());
                return this;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        public String row(List<?> cols) {
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
            if (layout.nonQuote()) {
                return s;
            }
            char delim = layout.delim();
            char quote = layout.quote();
            String quoteStr = String.valueOf(quote);
            String eol = layout.eolSymbols();
            if (StringUtils.containsNone(s, delim, quote) && StringUtils.containsNone(s, eol)) {
                return quoteStr + s + quoteStr;
            } else {
                return quoteStr + StringUtils.replace(s, quoteStr, quoteStr + quoteStr) + quoteStr;
            }
        }
    }

    /** Represents a CSV output process. */
    public static interface CsvWrite {
        void execute(final CsvStream stream);
    }

    public static CsvWriter of(final File file) {
        return CsvWriter.of(file, null, CsvLayout.simple());
    }

    public static CsvWriter of(final File file, final CsvLayout layout) {
        return CsvWriter.of(file, null, layout);
    }

    public static CsvWriter of(final OutputStream out) {
        return CsvWriter.of(null, out, CsvLayout.simple());
    }

    public static CsvWriter of(final OutputStream out, final CsvLayout layout) {
        return CsvWriter.of(null, out, layout);
    }

}
