package sample.context.report;

import lombok.*;
import sample.context.Dto;

/** ファイルイメージを表現します。 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportFile implements Dto {
	private static final long serialVersionUID = 1L;
	private String name;
	private byte[] data;
	public int size() {
		return data.length;
	}
}
