package sample.context;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * ResourceBundleに対する簡易アクセスを提供します。
 * <p>本コンポーネントはAPI経由でのラベル一覧の提供等、i18n用途のメッセージプロパティで利用してください。
 * <p>ResourceBundleは単純な文字列変換を目的とする標準のMessageSourceとは異なる特性(リスト概念)を
 * 持つため、別インスタンスでの管理としています。
 * （spring.messageとは別に指定[extension.messages]する必要があるので注意してください）
 */
@ConfigurationProperties(prefix = "extension.messages")
public class ResourceBundleHandler {

    private String encoding = "UTF-8";
    private Map<String, ResourceBundle> bundleMap = new ConcurrentHashMap<>();

    /**
     * 指定されたメッセージソースのResourceBundleを返します。
     * <p>basenameに拡張子(.properties)を含める必要はありません。
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
     * 指定されたメッセージソースのラベルキー、値のMapを返します。
     * <p>basenameに拡張子(.properties)を含める必要はありません。
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
     * SpringのMessageSource経由でResourceBundleを取得するFactory。
     * <p>プロパティファイルのエンコーディング指定を可能にしています。
     */
    public static class ResourceBundleFactory extends ResourceBundleMessageSource {
        /** ResourceBundleを取得します。 */
        public static ResourceBundle create(String basename, Locale locale, String encoding) {
            ResourceBundleFactory factory = new ResourceBundleFactory();
            factory.setDefaultEncoding(encoding);
            return Optional.ofNullable(factory.getResourceBundle(basename, locale))
                    .orElseThrow(() -> new IllegalArgumentException("指定されたbasenameのリソースファイルは見つかりませんでした。[]"));
        }
    }

}
