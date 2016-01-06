package sample;

import java.io.Serializable;
import java.util.*;

import lombok.Value;

/**
 * 審査例外を表現します。
 * <p>ValidationExceptionは入力例外や状態遷移例外等の復旧可能な審査例外です。
 * その性質上ログ等での出力はWARNレベル(ERRORでなく)で行われます。
 * <p>審査例外はグローバル/フィールドスコープで複数保有する事が可能です。複数件の例外を取り扱う際は
 * Warnsを利用して初期化してください。
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Warns warns;

    /** フィールドに従属しないグローバルな審査例外を通知するケースで利用してください。 */
    public ValidationException(String message) {
        super(message);
        warns = Warns.init(message);
    }

    /** フィールドに従属する審査例外を通知するケースで利用してください。 */
    public ValidationException(String field, String message) {
        super(message);
        warns = Warns.init(field, message);
    }

    /** フィールドに従属する審査例外を通知するケースで利用してください。 */
    public ValidationException(String field, String message, String[] messageArgs) {
        super(message);
        warns = Warns.init(field, message, messageArgs);
    }

    /** 複数件の審査例外を通知するケースで利用してください。 */
    public ValidationException(final Warns warns) {
        super(warns.head().map((v) -> v.getMessage()).orElse(ErrorKeys.Exception));
        this.warns = warns;
    }

    /** 発生した審査例外一覧を返します。*/
    public List<Warn> list() {
        return warns.list();
    }

    @Override
    public String getMessage() {
        return warns.head().map((v) -> v.getMessage()).orElse(ErrorKeys.Exception);
    }

    /** 審査例外情報です。  */
    public static class Warns implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<Warn> list = new ArrayList<>();

        private Warns() {
        }

        public Warns add(String message) {
            list.add(new Warn(null, message, null));
            return this;
        }

        public Warns add(String field, String message) {
            list.add(new Warn(field, message, null));
            return this;
        }

        public Warns add(String field, String message, String[] messageArgs) {
            list.add(new Warn(field, message, messageArgs));
            return this;
        }

        public Optional<Warn> head() {
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        }

        public List<Warn> list() {
            return list;
        }

        public boolean nonEmpty() {
            return !list.isEmpty();
        }

        public static Warns init() {
            return new Warns();
        }

        public static Warns init(String message) {
            return init().add(message);
        }

        public static Warns init(String field, String message) {
            return init().add(field, message);
        }

        public static Warns init(String field, String message, String[] messageArgs) {
            return init().add(field, message, messageArgs);
        }

    }

    /** フィールドスコープの審査例外トークンを表現します。 */
    @Value
    public static class Warn implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 審査例外フィールドキー */
        private String field;
        /** 審査例外メッセージ */
        private String message;
        /** 審査例外メッセージ引数 */
        private String[] messageArgs;

        /** フィールドに従属しないグローバル例外時はtrue */
        public boolean global() {
            return field == null;
        }
    }

    /** 審査例外で用いるメッセージキー定数 */
    public static interface ErrorKeys {
        /** サーバー側で問題が発生した可能性があります */
        String Exception = "error.Exception";
        /** 情報が見つかりませんでした */
        String EntityNotFound = "error.EntityNotFoundException";
        /** ログイン状態が有効ではありません */
        String Authentication = "error.Authentication";
        /** 対象機能の利用が認められていません */
        String AccessDenied = "error.AccessDeniedException";

        /** ログインに失敗しました */
        String Login = "error.login";
        /** 既に登録されているIDです */
        String DuplicateId = "error.duplicateId";

        /** 既に処理済の情報です */
        String ActionUnprocessing = "error.ActionStatusType.unprocessing";
    }

}
