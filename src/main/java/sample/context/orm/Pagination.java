package sample.context.orm;

import java.math.RoundingMode;

import lombok.*;
import sample.context.Dto;
import sample.context.orm.Sort.SortOrder;
import sample.util.Calculator;

/**
 * Paging information.
 */
@Data
@AllArgsConstructor
public class Pagination implements Dto {
    private static final long serialVersionUID = 1l;
    public static final int DefaultSize = 100;
    /** The number of page (1 origin) */
    private int page;
    /** The number in the page */
    private int size;
    /** The number of total */
    private Long total;
    /** Ignore the number of total calculation */
    private boolean ignoreTotal;
    /** Sort condition */
    private Sort sort;

    public Pagination() {
        this(1);
    }

    public Pagination(int page) {
        this(page, DefaultSize, null, false, new Sort());
    }

    public Pagination(int page, int size) {
        this(page, size, null, false, new Sort());
    }

    public Pagination(int page, int size, final Sort sort) {
        this(page, size, null, false, sort);
    }

    public Pagination(final Pagination req, long total) {
        this(req.getPage(), req.getSize(), total, false, req.getSort());
    }

    public Pagination ignoreTotal() {
        this.ignoreTotal = true;
        return this;
    }

    public Pagination sortIfEmpty(SortOrder... orders) {
        if (sort != null)
            sort.ifEmpty(orders);
        return this;
    }

    /** Return maximum pagination. An appropriate value is paid only at the time of total setting. */
    public int getMaxPage() {
        return (total == null) ? 0 : Calculator.of(total)
                .scale(0, RoundingMode.UP).divideBy(size).intValue();
    }

    public int getFirstResult() {
        return (page - 1) * size;
    }

}
