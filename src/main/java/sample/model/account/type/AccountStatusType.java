package sample.model.account.type;

/** 口座状態を表現します。 */
public enum AccountStatusType {
    /** 通常 */
    Normal,
    /** 退会 */
    Withdrawal;

    public boolean valid() {
        return this == Normal;
    }

    public boolean invalid() {
        return !valid();
    }
}
