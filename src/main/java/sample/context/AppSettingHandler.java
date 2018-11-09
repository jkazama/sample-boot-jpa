package sample.context;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;

import sample.context.orm.*;

/**
 * アプリケーション設定情報に対するアクセス手段を提供します。
 */
public class AppSettingHandler {

    @Autowired
    private SystemRepository rep;
    @Autowired
    @Qualifier(SystemRepository.BeanNameTx)
    private PlatformTransactionManager txm;
    /** 設定時は固定のキー/値を返すモックモードとする */
    private final Optional<Map<String, String>> mockMap;

    public AppSettingHandler() {
        this.mockMap = Optional.empty();
    }

    public AppSettingHandler(Map<String, String> mockMap) {
        this.mockMap = Optional.of(mockMap);
    }

    /** アプリケーション設定情報を取得します。 */
    @Cacheable(cacheNames = "AppSettingHandler.appSetting", key = "#id")
    public AppSetting setting(String id) {
        if (mockMap.isPresent()) {
            return mockSetting(id);
        }
        AppSetting setting = TxTemplate.of(txm).readOnly().tx(
                () -> AppSetting.load(rep, id));
        return setting;
    }

    private AppSetting mockSetting(String id) {
        return new AppSetting(id, "category", "テスト用モック情報", mockMap.get().get(id));
    }

    /** アプリケーション設定情報を変更します。 */
    @CacheEvict(cacheNames = "AppSettingHandler.appSetting", key = "#id")
    public AppSetting update(String id, String value) {
        return mockMap.isPresent() ? mockSetting(id)
                : TxTemplate.of(txm).tx(() -> AppSetting.load(rep, id).update(rep, value));
    }

}
