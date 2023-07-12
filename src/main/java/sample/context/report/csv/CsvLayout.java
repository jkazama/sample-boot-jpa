package sample.context.report.csv;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;

/**
 * Represents a CSV layout.
 */
@Builder
public record CsvLayout(
        /** delimiter */
        char delim,
        /** quoted character */
        char quote,
        /** true when no quote character is given */
        boolean nonQuote,
        /** newline character */
        String eolSymbols,
        /** header string */
        String header,
        /** character encoding */
        String charset) {

    public boolean hasHeader() {
        return header != null;
    }

    public List<Object> headerCols() {
        var list = new ArrayList<>();
        for (String col : header.split(String.valueOf(delim))) {
            list.add(col.trim());
        }
        return list;
    }

    public static CsvLayoutBuilder builderDefault() {
        return CsvLayout.builder()
                .delim(',')
                .quote('"')
                .nonQuote(false)
                .eolSymbols("\n")
                .header(null)
                .charset("UTF-8");
    }

    public static CsvLayout simple() {
        return CsvLayout.builderDefault().build();
    }

}
