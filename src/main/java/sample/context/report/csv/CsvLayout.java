package sample.context.report.csv;

import java.util.*;

import lombok.Data;

/**
 * CSVレイアウトを表現します。
 */
@Data
public class CsvLayout {
    /** 区切り文字 */
    private char delim = ',';
    /** クオート文字 */
    private char quote = '"';
    /** クオート文字を付与しない時はtrue */
    private boolean nonQuote = false;
    /** 改行文字 */
    private String eolSymbols = "\r\n";
    /** ヘッダ文字列 */
    private String header = null;
    /** 文字エンコーディング */
    private String charset = "UTF-8";

    public boolean hasHeader() {
        return header != null;
    }

    public List<Object> headerCols() {
        List<Object> list = new ArrayList<>();
        for (String col : header.split(String.valueOf(delim))) {
            list.add(col.trim());
        }
        return list;
    }

}
