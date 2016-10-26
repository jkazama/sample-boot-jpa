package sample.model.account.type;

/** Account status types. */
public enum AccountStatusType {
    Normal,
    Withdrawal;

    public boolean valid() {
        return this == Normal;
    }

    public boolean invalid() {
        return !valid();
    }
}
