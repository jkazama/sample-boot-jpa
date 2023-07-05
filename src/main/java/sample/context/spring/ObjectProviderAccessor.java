package sample.context.spring;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Provides a caching mechanism for Spring's ObjectProvider.
 * <p>
 * ObjectProvider is optimized due to its high access cost.
 */
@Component
public class ObjectProviderAccessor {
    private final ConcurrentMap<Class<?>, Object> cache = new ConcurrentHashMap<Class<?>, Object>();

    /** Returns a bean. */
    @SuppressWarnings("unchecked")
    public <T> T bean(ObjectProvider<T> target, Class<T> clazz) {
        cache.computeIfAbsent(clazz, k -> target.getObject());
        return (T) cache.get(clazz);
    }

    /** Returns a bean. */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> beanOpt(ObjectProvider<T> target, Class<T> clazz) {
        cache.computeIfAbsent(clazz, k -> target.getIfAvailable());
        return Optional.ofNullable(cache.get(clazz)).map(v -> (T) v);
    }

}
