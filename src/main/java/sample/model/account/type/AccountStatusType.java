package sample.model.account.type;

/** Account status */
public enum AccountStatusType {
    NORMAL,
    /** withdrawal from this service */
    WITHDRAWAL;

    public boolean isValid() {
        return this == NORMAL;
    }

    public boolean isInvalid() {
        return !isValid();
    }
}
