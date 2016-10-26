package sample.context;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Simple access for ResourceBundle.
 */
@ConfigurationProperties(prefix = "extension.messages")
public class ResourceBundleHandler {

    private String encoding = "UTF-8";
    private Map<String, ResourceBundle> bundleMap = new ConcurrentHashMap<>();

    /**
     * Return ResourceBundle of the message source.
     * <p>It is not necessary to include extension (.properties) in basename.
     */
    public ResourceBundle get(String basename) {
        return get(basename, Locale.getDefault());
    }

    public synchronized ResourceBundle get(String basename, Locale locale) {
        bundleMap.putIfAbsent(keyname(basename, locale), ResourceBundleFactory.create(basename, locale, encoding));
        return bundleMap.get(keyname(basename, locale));
    }

    private String keyname(String basename, Locale locale) {
        return basename + "_" + locale.toLanguageTag();
    }

    /**
     * Return label key and value from message source.
     * <p>It is not necessary to include extension (.properties) in basename.
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
     * Factory which acquires ResourceBundle via MessageSource of Spring.
     * <p>Enable the encoding designation of the property file.
     */
    public static class ResourceBundleFactory extends ResourceBundleMessageSource {
        public static ResourceBundle create(String basename, Locale locale, String encoding) {
            ResourceBundleFactory factory = new ResourceBundleFactory();
            factory.setDefaultEncoding(encoding);
            return Optional.ofNullable(factory.getResourceBundle(basename, locale))
                    .orElseThrow(() -> new IllegalArgumentException("The resource file of basename was not found. []"));
        }
    }

}
