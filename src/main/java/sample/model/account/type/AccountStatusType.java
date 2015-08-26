package sample.model.account.type;

/** 口座状態を表現します。 */
public enum AccountStatusType {
	/** 通常 */
	NORMAL,
	/** 退会 */
	WITHDRAWAL;
	
	public boolean valid() {
		return this == NORMAL;
	}
	public boolean invalid() {
		return !valid();
	}
}
