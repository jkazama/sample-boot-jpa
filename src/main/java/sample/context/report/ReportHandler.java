package sample.context.report;

import java.io.*;

import org.apache.commons.io.IOUtils;

import sample.context.report.csv.*;
import sample.context.report.csv.CsvReader.CsvReadLine;
import sample.context.report.csv.CsvWriter.CsvWrite;

/**
 * 帳票処理を行います。
 * low: サンプルではCSVのみ提供します。実際は固定長/Excel/PDFなどの取込/出力なども取り扱う可能性があります。
 * low: ExcelはPOI、PDFはJasperReportの利用が一般的です。(商用製品を利用するのもおすすめです)
 */
public class ReportHandler {

    /**
     * 帳票をオンメモリ上でbyte配列にします。
     * <p>大量データ等、パフォーマンス上のボトルネックが無いときはこちらの処理内でレポートを書き出しするようにしてください。
     */
    public byte[] convert(ReportToByte logic) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        try {
            logic.execute(out);
            return out.toByteArray();
        } finally {
            IOUtils.closeQuietly(dos);
        }
    }

    /**
     * CSVファイルを読み込んで行単位に処理を行います。
     * @param data 読み込み対象となるバイナリ
     * @param logic 行単位の読込処理
     */
    public void readCsv(byte[] data, CsvReadLine logic) {
        CsvReader.of(data).read(logic);
    }

    public void readCsv(byte[] data, CsvLayout layout, CsvReadLine logic) {
        CsvReader.of(data, layout).read(logic);
    }

    /**
     * CSVストリームを読み込んで行単位に処理を行います。
     * @param ins 読み込み対象となるInputStream
     * @param logic 行単位の読込処理
     */
    public void readCsv(InputStream ins, CsvReadLine logic) {
        CsvReader.of(ins).read(logic);
    }

    public void readCsv(InputStream ins, CsvLayout layout, CsvReadLine logic) {
        CsvReader.of(ins, layout).read(logic);
    }

    /**
     * CSVファイルを書き出しします。
     * @param file 出力対象となるファイル
     * @param logic 書出処理
     */
    public void writeCsv(File file, CsvWrite logic) {
        CsvWriter.of(file).write(logic);
    }

    public void writeCsv(File file, CsvLayout layout, CsvWrite logic) {
        CsvWriter.of(file, layout).write(logic);
    }

    /**
     * CSVストリームに書き出しします。
     * @param out 出力Stream
     * @param logic 書出処理
     */
    public void writeCsv(OutputStream out, CsvWrite logic) {
        CsvWriter.of(out).write(logic);
    }

    public void writeCsv(OutputStream out, CsvLayout layout, CsvWrite logic) {
        CsvWriter.of(out, layout).write(logic);
    }

    /** レポートをバイナリ形式で OutputStream へ書き出します。 */
    public static interface ReportToByte {
        void execute(OutputStream out);
    }

}
