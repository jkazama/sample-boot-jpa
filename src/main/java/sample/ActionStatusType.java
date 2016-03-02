package sample;

import java.util.*;

/**
 * 何らかの行為に関わる処理ステータス概念。
 */
public enum ActionStatusType {
    /** 未処理 */
    Unprocessed,
    /** 処理中 */
    Processing,
    /** 処理済 */
    Processed,
    /** 取消 */
    Cancelled,
    /** エラー */
    Error;

    /** 完了済みのステータス一覧 */
    public static final List<ActionStatusType> finishTypes = Collections.unmodifiableList(
            Arrays.asList(Processed, Cancelled));

    /** 未完了のステータス一覧(処理中は含めない) */
    public static final List<ActionStatusType> unprocessingTypes = Collections.unmodifiableList(
            Arrays.asList(Unprocessed, Error));

    /** 未完了のステータス一覧(処理中も含める) */
    public static final List<ActionStatusType> unprocessedTypes = Collections.unmodifiableList(
            Arrays.asList(Unprocessed, Processing, Error));

    /** 完了済みのステータスの時はtrue */
    public boolean isFinish() {
        return finishTypes.contains(this);
    }

    /** 未完了のステータス(処理中は含めない)の時はtrue */
    public boolean isUnprocessing() {
        return unprocessingTypes.contains(this);
    }

    /** 未完了のステータス(処理中も含める)の時はtrue */
    public boolean isUnprocessed() {
        return unprocessedTypes.contains(this);
    }
}
