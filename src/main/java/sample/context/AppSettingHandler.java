package sample.context;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import sample.context.orm.SystemRepository;

/**
 * Access application setting information.
 */
public class AppSettingHandler {

    @Autowired
    @Lazy
    private SystemRepository rep;
    /** You do a fixed key / value with a mock mode to return at the time of the setting */
    private final Optional<Map<String, String>> mockMap;

    public AppSettingHandler() {
        this.mockMap = Optional.empty();
    }

    public AppSettingHandler(Map<String, String> mockMap) {
        this.mockMap = Optional.of(mockMap);
    }

    @Cacheable(cacheNames = "AppSettingHandler.appSetting", key = "#id")
    @Transactional(value = SystemRepository.BeanNameTx)
    public AppSetting setting(String id) {
        if (mockMap.isPresent())
            return mockSetting(id);
        AppSetting setting = AppSetting.load(rep, id);
        setting.hashCode(); // for loading
        return setting;
    }

    private AppSetting mockSetting(String id) {
        return new AppSetting(id, "category", "Mock information for the test", mockMap.get().get(id));
    }

    @CacheEvict(cacheNames = "AppSettingHandler.appSetting", key = "#id")
    @Transactional(value = SystemRepository.BeanNameTx)
    public AppSetting update(String id, String value) {
        return mockMap.isPresent() ? mockSetting(id) : AppSetting.load(rep, id).update(rep, value);
    }

}
