package sample.context.report;

import java.io.*;

import sample.InvocationException;
import sample.context.report.csv.*;
import sample.context.report.csv.CsvReader.CsvReadLine;
import sample.context.report.csv.CsvWriter.CsvWrite;

/**
 * The report client / server components.
 * low: Provide only CSV implementation.
 *  Actually, you handle the uptake / output such as Fixed Length/Excel/PDF.
 * low: As for Excel, as for POI, the PDF, the use of JasperReport is common.
 *  (it is recommended to use a commercial product)
 */
public class ReportHandler {

    /**
     * Make the form a byte array on memory.
     * <p>If there is no performance bottleneck such as mass data, please write out the report within this processing.
     */
    public byte[] convert(ReportToByte logic) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(out)) {
            logic.execute(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new InvocationException(e);
        }
    }

    /** Read CSV file and process line by line. */
    public void readCsv(byte[] data, CsvReadLine logic) {
        CsvReader.of(data).read(logic);
    }

    /** Read CSV file and process line by line. */
    public void readCsv(byte[] data, CsvLayout layout, CsvReadLine logic) {
        CsvReader.of(data, layout).read(logic);
    }

    /** Read CSV Stream and process line by line. */
    public void readCsv(InputStream ins, CsvReadLine logic) {
        CsvReader.of(ins).read(logic);
    }

    /** Read CSV Stream and process line by line. */
    public void readCsv(InputStream ins, CsvLayout layout, CsvReadLine logic) {
        CsvReader.of(ins, layout).read(logic);
    }

    /** Export the CSV file. */
    public void writeCsv(File file, CsvWrite logic) {
        CsvWriter.of(file).write(logic);
    }

    /** Export the CSV file. */
    public void writeCsv(File file, CsvLayout layout, CsvWrite logic) {
        CsvWriter.of(file, layout).write(logic);
    }

    /** Export the CSV to Stream. */
    public void writeCsv(OutputStream out, CsvWrite logic) {
        CsvWriter.of(out).write(logic);
    }

    /** Export the CSV to Stream. */
    public void writeCsv(OutputStream out, CsvLayout layout, CsvWrite logic) {
        CsvWriter.of(out, layout).write(logic);
    }

    public static interface ReportToByte {
        void execute(OutputStream out);
    }

}
