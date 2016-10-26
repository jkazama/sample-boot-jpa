package sample.context.orm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.Value;
import sample.context.Dto;

/**
 * Sort information.
 * <p>Contain sort information (SortOrder) of plural matters.
 */
@Data
public class Sort implements Dto {
    private static final long serialVersionUID = 1L;

    private final List<SortOrder> orders = new ArrayList<SortOrder>();

    public Sort add(SortOrder order) {
        orders.add(order);
        return this;
    }

    public Sort asc(String property) {
        return add(SortOrder.asc(property));
    }

    public Sort desc(String property) {
        return add(SortOrder.desc(property));
    }

    public List<SortOrder> orders() {
        return orders;
    }

    public Sort ifEmpty(SortOrder... items) {
        if (orders.isEmpty() && items != null) {
            orders.addAll(Arrays.asList(items));
        }
        return this;
    }

    public static Sort ascBy(String property) {
        return new Sort().asc(property);
    }

    public static Sort descBy(String property) {
        return new Sort().desc(property);
    }

    /** Sort information of the field unit. */
    @Value
    public static class SortOrder implements Serializable {
        private static final long serialVersionUID = 1L;
        private String property;
        private boolean ascending;

        public static SortOrder asc(String property) {
            return new SortOrder(property, true);
        }

        public static SortOrder desc(String property) {
            return new SortOrder(property, false);
        }
    }

}
