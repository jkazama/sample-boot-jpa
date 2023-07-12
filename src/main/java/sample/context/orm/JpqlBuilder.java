package sample.context.orm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

/**
 * Builder for simple JPQL generation.
 * <p>
 * Specialize in dynamic condition generation of conditional clauses.
 */
public class JpqlBuilder {
    private final StringBuilder jpql;
    private final AtomicInteger index;
    private final List<String> conditions = new ArrayList<>();
    private final List<Object> reservedArgs = new ArrayList<>();
    private final List<Object> args = new ArrayList<>();
    private Optional<String> groupBy = Optional.empty();
    private Optional<String> orderBy = Optional.empty();

    public JpqlBuilder(String baseJpql, int fromIndex) {
        this.jpql = new StringBuilder(baseJpql);
        this.index = new AtomicInteger(fromIndex);
    }

    public JpqlBuilder(String baseJpql, String staticCondition, int fromIndex) {
        this(baseJpql, fromIndex);
        add(staticCondition);
    }

    /**
     * Add a conditional clause.
     * <p>
     * When setting the value, do not assign a number to ? (It will be added
     * automatically)
     */
    public JpqlBuilder condition(String condition, Object... values) {
        if (StringUtils.isNotBlank(condition)) {
            int count = StringUtils.countMatches(condition, '?');
            Object[] indexes = new Object[count];
            for (int i = 0; i < count; i++) {
                indexes[i] = index.getAndIncrement();
            }
            String c = condition.replace("?", "?%d");
            conditions.add(String.format(c, indexes));
            if (values != null) {
                args.addAll(Arrays.asList(values));
            }
        }
        return this;
    }

    private JpqlBuilder add(String condition) {
        if (StringUtils.isNotBlank(condition)) {
            this.conditions.add(condition);
        }
        return this;
    }

    private JpqlBuilder reservedArgs(Object... args) {
        if (args != null) {
            this.reservedArgs.addAll(Arrays.asList(args));
        }
        return this;
    }

    /** Assigns a match condition. (If the value is null, it is ignored.) */
    public JpqlBuilder equal(String field, Object value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s = ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    private JpqlBuilder ifValid(Object value, Runnable command) {
        if (isValid(value)) {
            command.run();
        }
        return this;
    }

    private boolean isValid(Object value) {
        if (value instanceof String) {
            return StringUtils.isNotBlank((String) value);
        } else if (value instanceof Optional) {
            return ((Optional<?>) value).isPresent();
        } else if (value instanceof Object[]) {
            return value != null && 0 < ((Object[]) value).length;
        } else if (value instanceof Collection) {
            return value != null && 0 < ((Collection<?>) value).size();
        } else {
            return value != null;
        }
    }

    /** Assigns a mismatch condition. (If the value is null, it is ignored.) */
    public JpqlBuilder equalNot(String field, Object value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s != ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** Assigns a like condition. (If the value is null, it is ignored.) */
    public JpqlBuilder like(String field, String value, OrmMatchMode mode) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s LIKE ?%d", field, index.getAndIncrement()));
            args.add(mode.toMatchString(value));
        });
    }

    /**
     * Assigns a like condition. [OR join for multiple fields (ignored if the value
     * is null)
     */
    public JpqlBuilder like(List<String> fields, String value, OrmMatchMode mode) {
        return ifValid(value, () -> {
            StringBuilder condition = new StringBuilder("(");
            for (String field : fields) {
                if (condition.length() != 1) {
                    condition.append(" OR ");
                }
                condition.append(String.format("(%s LIKE ?%d)", field, index.getAndIncrement()));
                args.add(mode.toMatchString(value));
            }
            condition.append(")");
            conditions.add(condition.toString());
        });
    }

    /** Grant the in condition. */
    public JpqlBuilder in(String field, Collection<?> values) {
        return ifValid(values, () -> {
            conditions.add(String.format("%s IN ?%d", field, index.getAndIncrement()));
            args.add(values);
        });
    }

    /** Grant the BETWEEN condition. */
    public JpqlBuilder between(String field, Date from, Date to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Grant the BETWEEN condition. */
    public JpqlBuilder between(String field, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Grant the BETWEEN condition. */
    public JpqlBuilder between(String field, Number from, Number to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Grant the BETWEEN condition. */
    public JpqlBuilder between(String field, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Grant the BETWEEN condition. */
    public JpqlBuilder between(String field, String from, String to) {
        if (isValid(from) && isValid(to)) {
            conditions.add(String.format(
                    "%s BETWEEN ?%d AND ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        } else if (isValid(from)) {
            gte(field, from);
        } else if (isValid(to)) {
            lte(field, to);
        }
        return this;
    }

    /** [field]&gt;=[value] Assigns a condition. (Ignored if value is null) */
    public <Y> JpqlBuilder gte(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s >= ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** [field]&gt;[value] Assigns a condition. (Ignored if value is null) */
    public <Y> JpqlBuilder gt(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s > ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** [field]&lt;=[value] Assigns a condition. */
    public <Y> JpqlBuilder lte(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s <= ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** [field]&lt;=[value] Assigns a condition. */
    public <Y> JpqlBuilder lt(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s < ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** group by Grants a conditional clause. */
    public JpqlBuilder groupBy(String groupBy) {
        this.groupBy = Optional.ofNullable(groupBy);
        return this;
    }

    /** Grant an order by condition clause. */
    public JpqlBuilder orderBy(String orderBy) {
        this.orderBy = Optional.ofNullable(orderBy);
        return this;
    }

    /** Generate JPQL. */
    public String build() {
        StringBuilder jpql = new StringBuilder(this.jpql.toString());
        if (!conditions.isEmpty()) {
            jpql.append(" WHERE ");
            AtomicBoolean first = new AtomicBoolean(true);
            conditions.forEach(condition -> {
                if (!first.getAndSet(false)) {
                    jpql.append(" AND ");
                }
                jpql.append(condition);
            });
        }
        groupBy.ifPresent(v -> jpql.append(" GROUP BY " + v));
        orderBy.ifPresent(v -> jpql.append(" ORDER BY " + v));
        return jpql.toString();
    }

    /** Returns the execution arguments associated with JPQL. */
    public Object[] args() {
        List<Object> result = new ArrayList<>();
        result.addAll(this.reservedArgs);
        result.addAll(this.args);
        return result.toArray();
    }

    /**
     * Generate builder.
     *
     * @param baseJpql Base JPQL (do not include where / order by)
     * @return builder
     */
    public static JpqlBuilder of(String baseJpql) {
        return new JpqlBuilder(baseJpql, 1);
    }

    /**
     * Generate builder.
     *
     * @param baseJpql  Base JPQL (do not include where / order by)
     * @param fromIndex The start index (1 start) of the condition clause to be
     *                  dynamically assigned.
     *                  If a sequential replacement number has already been assigned
     *                  with "field=?1" etc., the next number.
     * @param args      Argument associated with an already assigned substitution
     *                  sequence number
     * @return builder
     */
    public static JpqlBuilder of(String baseJpql, int fromIndex, Object... args) {
        return new JpqlBuilder(baseJpql, fromIndex).reservedArgs(args);
    }

    /**
     * Generate builder.
     *
     * @param baseJpql        Base JPQL (do not include where / order by)
     * @param staticCondition A where conditional clause (e.g., field is null) that
     *                        is determined without any condition specified.
     * @return builder
     */
    public static JpqlBuilder of(String baseJpql, String staticCondition) {
        return new JpqlBuilder(baseJpql, staticCondition, 1);
    }

    /**
     * Generate builder.
     *
     * @param baseJpql        Base JPQL (do not include where / order by)
     * @param staticCondition A where conditional clause (e.g., field is null) that
     *                        is determined without any condition specified.
     * @param fromIndex       The start index (1 start) of the condition clause to
     *                        be dynamically assigned.
     *                        If a sequential replacement number has already been
     *                        assigned with "field=?1" etc., the next number.
     * @param args            Argument associated with an already assigned
     *                        substitution sequence number
     * @return builder
     */
    public static JpqlBuilder of(String baseJpql, String staticCondition, int fromIndex, Object... args) {
        return new JpqlBuilder(baseJpql, staticCondition, fromIndex).reservedArgs(args);
    }

}
