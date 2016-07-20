package sample.support;

import java.util.*;

/**
 * Spring 依存でないテスト利用時にインスタンスを静的にシェアします。
 * <p>基本的にこちらの利用は推奨しません。
 * <p>Hb8TestInterceptor のようにリフレクションベースで初期化されるコンポーネントなどで利用してください。
 */
public class MockTemporaryContext {

    private static final Map<Class<?>, Object> beans = new HashMap<>();
    
    public static void register(Object bean) {
        beans.put(bean.getClass(), bean);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<?> clazz) {
        return (T)beans.get(clazz);
    }
    
}
