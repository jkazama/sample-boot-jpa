package sample.context.orm;

import java.util.List;

import lombok.Value;
import sample.context.Dto;

/**
 * List of paging.
 */
@Value
public class PagingList<T> implements Dto {
    private static final long serialVersionUID = 1L;

    private List<T> list;
    private Pagination page;

}
