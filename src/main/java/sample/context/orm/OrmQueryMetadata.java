package sample.context.orm;

import java.util.*;

import javax.persistence.LockModeType;

/**
 * Build additional meta information for Query.
 */
public class OrmQueryMetadata {

    private final Map<String, Object> hints = new HashMap<>();
    private Optional<LockModeType> lockMode = Optional.empty();
    
    private OrmQueryMetadata() {}
    
    public Map<String, Object> hints() {
        return hints;
    }
    
    public Optional<LockModeType> lockMode() {
        return lockMode;
    }
    
    public OrmQueryMetadata hint(String hintName, Object value) {
        this.hints.put(hintName, value);
        return this;
    }
    
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
