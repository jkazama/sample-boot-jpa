package sample.context.security;

import java.util.List;

import javax.servlet.Filter;

/**
 * Filter expansion setting for Spring Security.
 * <p>When you want to add Filter, please register Bean in succession to this I/F.
 */
public interface SecurityFilters {

    /**
     * Return a list of ServletFilter.
     */
    List<Filter> filters();

}
