package sample.context.support;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import sample.context.orm.OrmRepository;

/**
 * Provides access to application configuration information.
 */
public interface AppSettingHandler {

    /** Returns application configuration information. */
    AppSetting setting(String id);

    /** Change application configuration information. */
    AppSetting change(String id, String value);

    /** The following values are automatically numbered. */
    long nextId(String id);

    @Component
    public static class AppSettingHandlerImpl implements AppSettingHandler {
        private final OrmRepository rep;

        public AppSettingHandlerImpl(OrmRepository rep) {
            this.rep = rep;
        }

        /** {@inheritDoc} */
        @Override
        @Cacheable(cacheNames = "AppSettingHandler.appSetting", key = "#id")
        @Transactional
        public AppSetting setting(String id) {
            AppSetting setting = AppSetting.load(rep, id);
            setting.hashCode(); // for loading
            return setting;
        }

        /** {@inheritDoc} */
        @Override
        @CacheEvict(cacheNames = "AppSettingHandler.appSetting", key = "#id")
        @Transactional
        public AppSetting change(String id, String value) {
            return AppSetting.load(rep, id).change(rep, value);
        }

        /** {@inheritDoc} */
        @Override
        @Transactional(TxType.REQUIRES_NEW)
        public long nextId(String id) {
            AppSetting setting = rep.loadForUpdate(AppSetting.class, id);
            long nextId = setting.longValue() + 1;
            setting.setValue(String.valueOf(nextId));
            rep.update(setting);
            return nextId;
        }
    }

    // public static class AppSettingHandlerMock implements AppSettingHandler {
    // private final Map<String, AppSetting> mockMap = new HashMap<>();

    // @Override
    // public AppSetting setting(String id) {
    // return this.mockMap.get(id);
    // }

    // @Override
    // public AppSetting change(String id, String value) {
    // this.mockMap.put(id, AppSetting.of(id, value));
    // return this.mockMap.get(id);
    // }

    // @Override
    // public long nextId(String id) {
    // return setting(id).longValue() + 1;
    // }
    // }
}
