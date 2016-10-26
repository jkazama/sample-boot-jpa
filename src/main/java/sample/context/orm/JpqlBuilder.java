package sample.context.orm;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.hibernate.criterion.MatchMode;

/**
 * A builder to generate JPQL easily.
 * <p>Specialize in the dynamic condition generation of the condition phrase.
 */
public class JpqlBuilder {

    private final StringBuilder jpql;
    private final AtomicInteger index;
    private final MutableList<String> conditions = Lists.mutable.empty();
    private final MutableList<Object> reservedArgs = Lists.mutable.empty();
    private final MutableList<Object> args = Lists.mutable.empty();
    private Optional<String> orderBy = Optional.empty();

    public JpqlBuilder(String baseJpql, int fromIndex) {
        this.jpql = new StringBuilder(baseJpql);
        this.index = new AtomicInteger(fromIndex);
    }

    public JpqlBuilder(String baseJpql, String staticCondition, int fromIndex) {
        this(baseJpql, fromIndex);
        add(staticCondition);
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

    /** Add an "equal" condition. (a value is ignored at the time of null) */
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

    /** Add an "equalNot" condition. (a value is ignored at the time of null) */
    public JpqlBuilder equalNot(String field, Object value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s != ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** Add an "like" condition. (a value is ignored at the time of null) */
    public JpqlBuilder like(String field, String value, MatchMode mode) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s like ?%d", field, index.getAndIncrement()));
            args.add(mode.toMatchString(value));
        });
    }

    /** Add an "like" condition.[OR combination for plural fields] (a value is ignored at the time of null) */
    public JpqlBuilder like(List<String> fields, String value, MatchMode mode) {
        return ifValid(value, () -> {
            StringBuilder condition = new StringBuilder("(");
            for (String field : fields) {
                if (condition.length() != 1) {
                    condition.append(" or ");
                }
                condition.append(String.format("(%s like ?%d)", field, index.getAndIncrement()));
                args.add(mode.toMatchString(value));
            }
            condition.append(")");
            conditions.add(condition.toString());
        });
    }

    /** Add an "in" condition. (a value is ignored at the time of null) */
    public JpqlBuilder in(String field, List<Object> values) {
        return ifValid(values, () -> {
            conditions.add(String.format("%s in ?%d", field, index.getAndIncrement()));
            args.add(values);
        });
    }

    /** Add an "between" condition. (a value is ignored at the time of null) */
    public JpqlBuilder between(String field, Date from, Date to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s between ?%d and ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        }
        return this;
    }

    /** Add an "between" condition. (a value is ignored at the time of null) */
    public JpqlBuilder between(String field, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s between ?%d and ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        }
        return this;
    }

    /** Add an "between" condition. (a value is ignored at the time of null) */
    public JpqlBuilder between(String field, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            conditions.add(String.format(
                    "%s between ?%d and ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        }
        return this;
    }

    /** Add an "between" condition. (a value is ignored at the time of null) */
    public JpqlBuilder between(String field, String from, String to) {
        if (isValid(from) && isValid(to)) {
            conditions.add(String.format(
                    "%s between ?%d and ?%d", field, index.getAndIncrement(), index.getAndIncrement()));
            args.add(from);
            args.add(to);
        }
        return this;
    }

    /** Add an "field&gt;=value" condition. (a value is ignored at the time of null) */
    public <Y extends Comparable<? super Y>> JpqlBuilder gte(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s >= ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** Add an "field&gt;value" condition. (a value is ignored at the time of null) */
    public <Y extends Comparable<? super Y>> JpqlBuilder gt(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s > ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** Add an "field&lt;=value" condition. (a value is ignored at the time of null) */
    public <Y extends Comparable<? super Y>> JpqlBuilder lte(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s <= ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** Add an "field&lt;value" condition. (a value is ignored at the time of null) */
    public <Y extends Comparable<? super Y>> JpqlBuilder lt(String field, final Y value) {
        return ifValid(value, () -> {
            conditions.add(String.format("%s < ?%d", field, index.getAndIncrement()));
            args.add(value);
        });
    }

    /** Add an "orderBy" condition. */
    public JpqlBuilder orderBy(String orderBy) {
        this.orderBy = Optional.ofNullable(orderBy);
        return this;
    }

    /** Generate JPQL。 */
    public String build() {
        StringBuilder jpql = new StringBuilder(this.jpql.toString());
        if (!conditions.isEmpty()) {
            jpql.append(" where ");
            AtomicBoolean first = new AtomicBoolean(true);
            conditions.each(condition -> {
                if (!first.getAndSet(false)) {
                    jpql.append(" and ");
                }
                jpql.append(condition);
            });
        }
        orderBy.ifPresent(v -> jpql.append(" order by " + v));
        return jpql.toString();
    }

    /** Return a practice argument having a string in JPQL. */
    public Object[] args() {
        return Lists.mutable.ofAll(reservedArgs).withAll(args).toArray();
    }

    /**
     * Create Builder.
     * @param baseJpql JPQL (You do not include where / order by) which becomes the basic
     * @return Created Builder
     */
    public static JpqlBuilder of(String baseJpql) {
        return new JpqlBuilder(baseJpql, 1);
    }

    /**
     * Create Builder.
     * @param baseJpql JPQL (You do not include where / order by) which becomes the basic
     * @param fromIndex The start index (1 start) of the condition phrase to give dynamically.
     * When have already given replaced numbers in "field=?1"; a following number.
     * @param args The argument which gains relation for the replaced numbers that Your have been already given
     * @return Created Builder
     */
    public static JpqlBuilder of(String baseJpql, int fromIndex, Object... args) {
        return new JpqlBuilder(baseJpql, fromIndex).reservedArgs(args);
    }

    /**
     * Create Builder.
     * @param baseJpql JPQL (You do not include where / order by) which becomes the basic
     * @param staticCondition where condition phrase (e.g. field is null) to be settled without condition designation
     * @return Created Builder
     */
    public static JpqlBuilder of(String baseJpql, String staticCondition) {
        return new JpqlBuilder(baseJpql, staticCondition, 1);
    }

    /**
     * Create Builder.
     * @param baseJpql JPQL (You do not include where / order by) which becomes the basic
     * @param staticCondition where condition phrase (e.g. field is null) to be settled without condition designation
     * @param fromIndex The start index (1 start) of the condition phrase to give dynamically.
     * When have already given replaced numbers in "field=?1"; a following number.
     * @param args The argument which gains relation for the replaced numbers that Your have been already given
     * @return Created Builder
     */
    public static JpqlBuilder of(String baseJpql, String staticCondition, int fromIndex, Object... args) {
        return new JpqlBuilder(baseJpql, staticCondition, fromIndex).reservedArgs(args);
    }

}
