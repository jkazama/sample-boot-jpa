package sample.model.asset;

/**
 * 摘要定数インターフェース。
 */
public interface Remarks {

    /** 振込入金 */
    String CashIn = "CashIn";
    /** 振込入金(調整) */
    String CashInAdjust = "CashInAdjust";
    /** 振込入金(取消) */
    String CashInCancel = "CashInCancel";
    /** 振込出金 */
    String CashOut = "CashOut";
    /** 振込出金(調整) */
    String CashOutAdjust = "CashOutAdjust";
    /** 振込出金(取消) */
    String CashOutCancel = "CashOutCancel";

}
