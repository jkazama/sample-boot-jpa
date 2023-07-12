package sample.model.asset.type;

/**
 * Cash flow type. low: specific to each company. Labels, including Remarks,
 * should be cut out to messages.properties as much as possible
 */
public enum CashflowType {
    /** Direct Deposit */
    CASH_IN,
    /** Direct Withdrawal */
    CASH_OUT,
    /** Account Transfer Deposit */
    CASH_TRANSFER_IN,
    /** Account Transfer Withdrawal */
    CASH_TRANSFER_OUT
}
