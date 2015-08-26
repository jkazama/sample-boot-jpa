package sample.model.asset.type;

/** キャッシュフロー種別。 low: 各社固有です。摘要含めラベルはなるべくmessages.propertiesへ切り出し */
public enum CashflowType {
	/** 振込入金 */
	CashIn,
	/** 振込出金 */
	CashOut,
	/** 振替入金 */
	CashTransferIn,
	/** 振替出金 */
	CashTransferOut
}
