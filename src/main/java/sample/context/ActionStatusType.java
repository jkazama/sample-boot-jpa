package sample.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A processing status concept related to some action.
 */
public enum ActionStatusType {
    UNPROCESSED,
    PROCESSING,
    PROCESSED,
    CANCELLED,
    ERROR;

    /** Completed Status List */
    public static final List<ActionStatusType> FINISH_TYPES = Collections.unmodifiableList(
            Arrays.asList(PROCESSED, CANCELLED));

    /** List of incomplete statuses (not including those in process) */
    public static final List<ActionStatusType> UNPROCESSING_TYPES = Collections.unmodifiableList(
            Arrays.asList(UNPROCESSED, ERROR));

    /** List of incomplete statuses (including those in process) */
    public static final List<ActionStatusType> UNPROCESSED_TYPES = Collections.unmodifiableList(
            Arrays.asList(UNPROCESSED, PROCESSING, ERROR));

    /** true for completed status */
    public boolean isFinish() {
        return FINISH_TYPES.contains(this);
    }

    /** true when status is incomplete (not including in-processing) */
    public boolean isUnprocessing() {
        return UNPROCESSING_TYPES.contains(this);
    }

    /** true when status is incomplete (including in-process) */
    public boolean isUnprocessed() {
        return UNPROCESSED_TYPES.contains(this);
    }
}
