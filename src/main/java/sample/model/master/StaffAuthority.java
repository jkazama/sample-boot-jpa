package sample.model.master;

import java.util.List;

import javax.persistence.*;

import lombok.*;
import sample.context.orm.*;
import sample.model.constraints.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StaffAuthority extends OrmActiveRecord<StaffAuthority> {
	private static final long serialVersionUID = 1l;

	/** ID */
	@Id
	@GeneratedValue
	private Long id;
	/** 社員ID */
	@IdStr
	private String staffId;
	/** 権限名称。(「プリフィックスにROLE_」を付与してください) */
	@Name
	private String authority;

	/** 口座IDに紐付く権限一覧を返します。 */
	public static List<StaffAuthority> find(final OrmRepository rep, String staffId) {
		return rep.tmpl().find("from StaffAuthority where staffId=?1", staffId);
	}
	
}
