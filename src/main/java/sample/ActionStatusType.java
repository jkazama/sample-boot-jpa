package sample;

import java.util.*;

/**
 * Processing status concept about some kind of acts.
 */
public enum ActionStatusType {
    Unprocessed,
    Processing,
    Processed,
    Cancelled,
    Error;

    public static final List<ActionStatusType> finishTypes = Collections.unmodifiableList(
            Arrays.asList(Processed, Cancelled));

    public static final List<ActionStatusType> unprocessingTypes = Collections.unmodifiableList(
            Arrays.asList(Unprocessed, Error));

    public static final List<ActionStatusType> unprocessedTypes = Collections.unmodifiableList(
            Arrays.asList(Unprocessed, Processing, Error));

    public boolean isFinish() {
        return finishTypes.contains(this);
    }

    public boolean isUnprocessing() {
        return unprocessingTypes.contains(this);
    }

    public boolean isUnprocessed() {
        return unprocessedTypes.contains(this);
    }
}
