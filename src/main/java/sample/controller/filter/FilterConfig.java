package sample.controller.filter;

import java.util.*;

import javax.servlet.Filter;

import org.springframework.context.annotation.Configuration;

import sample.context.security.SecurityHandler.SecurityFilters;

/**
 * ServletFilterの拡張実装。
 * filtersで返すFilterはSecurityHandlerにおいてActionSessionFilterの後に定義されます。
 */
@Configuration
public class FilterConfig implements SecurityFilters {

	@Override
	public List<Filter> filters() {
		return new ArrayList<>();
	}
	
}
