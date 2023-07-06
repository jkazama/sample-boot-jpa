package sample.context.support;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * Provides simple access to ResourceBundle.
 * <p>
 * This component should be used in message properties for i18n applications,
 * such as providing a list of labels via API.
 * <p>
 * ResourceBundle has different characteristics (map concept) from the standard
 * MessageSource, which is intended for simple string conversion. Therefore, it
 * is managed in a separate instance.
 */
@Component
public class ResourceBundleHandler {
    private static final String DEFAULT_ENCODING = "UTF-8";
    private final Map<String, ResourceBundle> bundleMap = new ConcurrentHashMap<>();

    /**
     * Returns the ResourceBundle of the specified message source.
     * <p>
     * It is not necessary to include the extension (.properties) in the basename.
     */
    public ResourceBundle get(String basename) {
        return get(basename, Locale.getDefault());
    }

    public synchronized ResourceBundle get(String basename, Locale locale) {
        bundleMap.putIfAbsent(keyname(basename, locale),
                ResourceBundleFactory.create(basename, locale, DEFAULT_ENCODING));
        return bundleMap.get(keyname(basename, locale));
    }

    private String keyname(String basename, Locale locale) {
        return basename + "_" + locale.toLanguageTag();
    }

    /**
     * Returns the label key and value Map of the specified message source.
     * <p>
     * It is not necessary to include the extension (.properties) in the basename.
     */
    public Map<String, String> labels(String basename) {
        return labels(basename, Locale.getDefault());
    }

    public Map<String, String> labels(String basename, Locale locale) {
        ResourceBundle bundle = get(basename, locale);
        return bundle.keySet().stream().collect(Collectors.toMap(
                key -> key,
                key -> bundle.getString(key)));
    }

    /**
     * Factory to acquire ResourceBundle via Spring's MessageSource.
     * <p>
     * Allows specification of encoding for property files.
     */
    public static class ResourceBundleFactory extends ResourceBundleMessageSource {
        /** Return ResourceBundle. */
        public static ResourceBundle create(String basename, Locale locale, String encoding) {
            var factory = new ResourceBundleFactory();
            factory.setDefaultEncoding(encoding);
            return Optional.ofNullable(factory.getResourceBundle(basename, locale))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No resource file with the specified basename was found. [" + basename + "]"));
        }
    }

}
