package sample.context.report.csv;

import java.util.*;

import lombok.Data;

/**
 * Express CSV layout.
 */
@Data
public class CsvLayout {
    /** delimiter */
    private char delim = ',';
    /** quote character */
    private char quote = '"';
    /** true if no quote character is given */
    private boolean nonQuote = false;
    /** end of line character */
    private String eolSymbols = "\r\n";
    /** header string */
    private String header = null;
    /** character encoding */
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
