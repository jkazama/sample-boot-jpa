package sample.context.orm;

import java.io.Serializable;
import java.util.function.Consumer;

import sample.context.Entity;
import sample.util.Validator;

/**
 * ORMベースでActiveRecordの概念を提供するEntity基底クラス。
 * <p>ここでは自インスタンスの状態に依存する簡易な振る舞いのみをサポートします。
 * 実際のActiveRecordモデルにはget/find等の概念も含まれますが、それらは 自己の状態を
 * 変える行為ではなく対象インスタンスを特定する行為(クラス概念)にあたるため、
 * クラスメソッドとして継承先で個別定義するようにしてください。
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

    /** 審査処理をします。 */
    @SuppressWarnings("unchecked")
    protected T validate(Consumer<Validator> proc) {
        Validator.validate(proc);
        return (T) this;
    }

    /**
     * 与えられたレポジトリを経由して自身を新規追加します。
     * @param rep 永続化の際に利用する関連{@link OrmRepository}
     * @return 自身の情報
     */
    @SuppressWarnings("unchecked")
    public T save(final OrmRepository rep) {
        return (T) rep.save(this);
    }

    /**
     * 与えられたレポジトリを経由して自身を更新します。
     * @param rep 永続化の際に利用する関連{@link OrmRepository}
     */
    @SuppressWarnings("unchecked")
    public T update(final OrmRepository rep) {
        return (T) rep.update(this);
    }

    /**
     * 与えられたレポジトリを経由して自身を物理削除します。
     * @param rep 永続化の際に利用する関連{@link OrmRepository}
     */
    @SuppressWarnings("unchecked")
    public T delete(final OrmRepository rep) {
        return (T) rep.delete(this);
    }

    /**
     * 与えられたレポジトリを経由して自身を新規追加または更新します。
     * @param rep 永続化の際に利用する関連{@link OrmRepository}
     */
    @SuppressWarnings("unchecked")
    public T saveOrUpdate(final OrmRepository rep) {
        return (T) rep.saveOrUpdate(this);
    }

}
