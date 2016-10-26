package sample.context.orm;

import java.io.Serializable;
import java.util.function.Consumer;

import sample.context.Entity;
import sample.util.Validator;

/**
 * The Entity base class which provides a concept of ActiveRecord on the basis of ORM.
 * <p>Support only simple behavior depending on a state of the own instance here.
 * The concepts such as get/find are included in real ActiveRecord model,
 *  but they are not acts to change a state of the self.
 * Please define them as a class method individually in succession
 *  to deal with an act to identify target instance.
 * <pre>
 * public static Optional&lt;Account&gt; get(final OrmRepository rep, String id) {
 *     return rep.get(Account.class, id);
 * }
 * 
 * public static Account findAll(final OrmRepository rep) {
 *     return rep.findAll(Account.class);
 * }
 * </pre>
 */
public class OrmActiveRecord<T extends Entity> implements Serializable, Entity {
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unchecked")
    protected T validate(Consumer<Validator> proc) {
        Validator.validate(proc);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T save(final OrmRepository rep) {
        return (T) rep.save(this);
    }

    @SuppressWarnings("unchecked")
    public T update(final OrmRepository rep) {
        return (T) rep.update(this);
    }

    @SuppressWarnings("unchecked")
    public T delete(final OrmRepository rep) {
        return (T) rep.delete(this);
    }

    @SuppressWarnings("unchecked")
    public T saveOrUpdate(final OrmRepository rep) {
        return (T) rep.saveOrUpdate(this);
    }

}
