package sample.context.orm;

import java.util.List;

import lombok.Value;
import sample.context.Dto;

/**
 * ページング一覧を表現します。
 * 
 * @param <T> 結果オブジェクト(一覧の要素)
 */
@Value
public class PagingList<T> implements Dto {

	private static final long serialVersionUID = 1L;

	private List<T> list;
	private Pagination page;
	
}
