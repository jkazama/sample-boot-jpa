package sample.context.orm;

import java.math.RoundingMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import sample.context.Dto;
import sample.util.Calculator;

/**
 * ページング情報を表現します。
 */
@Data
@AllArgsConstructor
public class Pagination implements Dto {
	private static final long serialVersionUID = 1l;
	public static final int defaultSize = 100;
	/** ページ数(1開始) */
	private int page;
	/** ページあたりの件数 */
	private int size;
	/** トータル件数 */
	private Long total;
	/** トータル件数算出を無視するか */
	private boolean ignoreTotal;
	/** ソート条件 */
	private Sort sort;

	public Pagination() {
		this(1);
	}
	
	public Pagination(int page) {
		this(page, defaultSize, null, false, new Sort());
	}
	
	public Pagination(int page, int size) {
		this(page, size, null, false, new Sort());
	}

	public Pagination(int page, int size, final Sort sort) {
		this(page, size, null, false, sort);
	}

	public Pagination(final Pagination req, long total) {
		this(req.getPage(), req.getSize(), total, false, req.getSort());
	}

	/** カウント算出を無効化します。 */
	public Pagination ignoreTotal() {
		this.ignoreTotal = true;
		return this;
	}
	
	/**
	 * @return 最大ページ数を返します。total設定時のみ適切な値が返されます。
	 */
	public int getMaxPage() {
		return (total == null) ? 0 : Calculator.init(total)
				.scale(0, RoundingMode.UP).divideBy(size).intValue();
	}

	/**
	 * @return 開始件数を返します。
	 */
	public int getFirstResult() {
		return (page - 1) * size;
	}

}
