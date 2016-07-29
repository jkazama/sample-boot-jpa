package sample.context.orm;

import java.util.*;

import javax.persistence.LockModeType;

/**
 * Query 向けの追加メタ情報を構築します。
 */
public class OrmQueryMetadata {

    private final Map<String, Object> hints = new HashMap<>();
    private Optional<LockModeType> lockMode = Optional.empty();
    
    private OrmQueryMetadata() {}
    
    /** 内部に保持するヒント情報を返します。 */
    public Map<String, Object> hints() {
        return hints;
    }
    
    /** 内部に保持するロックモードを返します。 */
    public Optional<LockModeType> lockMode() {
        return lockMode;
    }
    
    /** ヒントを追加します。 */
    public OrmQueryMetadata hint(String hintName, Object value) {
        this.hints.put(hintName, value);
        return this;
    }
    
    /** ロックモードを設定します。 */
    public OrmQueryMetadata lockMode(LockModeType lockMode) {
        this.lockMode = Optional.ofNullable(lockMode);
        return this;
    }

    public static OrmQueryMetadata empty() {
        return new OrmQueryMetadata();
    }
    
    public static OrmQueryMetadata withLock(LockModeType lockMode) {
        return empty().lockMode(lockMode);
    }
    
    public static OrmQueryMetadata withHint(String hintName, Object value) {
        return empty().hint(hintName, value);
    }

}
