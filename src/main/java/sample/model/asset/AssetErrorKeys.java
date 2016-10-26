package sample.model.asset;

/**
 * asset-domain error key constants.
 */
public interface AssetErrorKeys {

    /** 受渡日を迎えていないため実現できません */
    String CashflowRealizeDay = "error.Cashflow.realizeDay";
    /** 既に受渡日を迎えています */
    String CashflowBeforeEqualsDay = "error.Cashflow.beforeEqualsDay";

    /** 未到来の受渡日です */
    String CashInOutAfterEqualsDay = "error.CashInOut.afterEqualsDay";
    /** 既に発生日を迎えています */
    String CashInOutBeforeEqualsDay = "error.CashInOut.beforeEqualsDay";
    /** 出金可能額を超えています */
    String CashInOutWithdrawAmount = "error.CashInOut.withdrawAmount";
}
