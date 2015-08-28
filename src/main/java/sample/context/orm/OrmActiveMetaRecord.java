package sample.context.orm;

import java.io.Serializable;
import java.time.LocalDateTime;

import sample.context.Entity;

/**
 * OrmActiveRecordに登録/変更メタ概念を付与した基底クラス。
 * 本クラスを継承して作成されたEntityは永続化時に自動的なメタ情報更新が行われます。
 * @see OrmInterceptor
 */
public abstract class OrmActiveMetaRecord<T extends Entity> extends OrmActiveRecord<T>implements Serializable, Entity {

	private static final long serialVersionUID = 1L;

	/** 登録利用者ID */
	public abstract String getCreateId();
	
	public abstract void setCreateId(String createId);
	
	/** 登録日時 */
	public abstract LocalDateTime getCreateDate();
	
	public abstract void setCreateDate(LocalDateTime createDate);
	
	/** 更新利用者ID */
	public abstract String getUpdateId();
	
	public abstract void setUpdateId(String updateId);
	
	/** 更新日時 */
	public abstract LocalDateTime getUpdateDate();
	
	public abstract void setUpdateDate(LocalDateTime updateDate);

}
