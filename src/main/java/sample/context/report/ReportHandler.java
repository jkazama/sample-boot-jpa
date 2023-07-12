package sample.context.report;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sample.context.InvocationException;
import sample.context.report.csv.CsvLayout;
import sample.context.report.csv.CsvReader;
import sample.context.report.csv.CsvReader.CsvReadLine;
import sample.context.report.csv.CsvWriter;
import sample.context.report.csv.CsvWriter.CsvWrite;

/**
 * Processes ledger sheets.
 * low: Only CSV is provided in the sample. In reality, it may handle
 * import/export of fixed length/Excel/PDF, etc.
 * low: POI is commonly used for Excel and JasperReport for PDF. (It is also
 * recommended to use commercial products).
 */
public class ReportHandler {

    /**
     * Turns a ledger sheet into a byte array on-memory.
     * <p>
     * When there is no performance bottleneck, such as a large amount of data, the
     * report should be exported within this process.
     */
    public byte[] convert(ReportToByte logic) {
        var out = new ByteArrayOutputStream();
        try (var dos = new DataOutputStream(out)) {
            logic.execute(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new InvocationException(e);
        }
    }

    /** CSV files are read and processed row by row. */
    public void readCsv(byte[] data, CsvReadLine logic) {
        CsvReader.of(data).read(logic);
    }

    /** CSV files are read and processed row by row. */
    public void readCsv(byte[] data, CsvLayout layout, CsvReadLine logic) {
        CsvReader.of(data, layout).read(logic);
    }

    /** Reads CSV stream and processes row by row */
    public void readCsv(InputStream ins, CsvReadLine logic) {
        CsvReader.of(ins).read(logic);
    }

    public void readCsv(InputStream ins, CsvLayout layout, CsvReadLine logic) {
        CsvReader.of(ins, layout).read(logic);
    }

    /** Export CSV file. */
    public void writeCsv(File file, CsvWrite logic) {
        CsvWriter.of(file).write(logic);
    }

    /** Export CSV file. */
    public void writeCsv(File file, CsvLayout layout, CsvWrite logic) {
        CsvWriter.of(file, layout).write(logic);
    }

    /** Export to CSV stream. */
    public void writeCsv(OutputStream out, CsvWrite logic) {
        CsvWriter.of(out).write(logic);
    }

    /** Export to CSV stream. */
    public void writeCsv(OutputStream out, CsvLayout layout, CsvWrite logic) {
        CsvWriter.of(out, layout).write(logic);
    }

    /** Writes a report in binary format to OutputStream. */
    public static interface ReportToByte {
        void execute(OutputStream out);
    }

}
