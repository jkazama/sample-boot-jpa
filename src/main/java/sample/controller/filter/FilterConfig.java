package sample.controller.filter;

import java.util.*;

import javax.servlet.Filter;

import org.springframework.context.annotation.Configuration;

import sample.context.security.SecurityFilters;

/**
 * Expansion implementation of ServletFilter.
 * <p>Filter to return in "filters" is defined after ActionSessionFilter in SecurityHandler.
 */
@Configuration
public class FilterConfig implements SecurityFilters {

    @Override
    public List<Filter> filters() {
        return new ArrayList<>();
    }

}
