package sample.context.support;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import sample.context.orm.repository.SystemRepository;

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
    @Slf4j
    public static class AppSettingHandlerImpl implements AppSettingHandler {
        public static final String CacheItemKey = "AppSettingHandler.appSetting";
        private static final String UIDKeyPrefix = "uid.";
        private final SystemRepository rep;

        public AppSettingHandlerImpl(SystemRepository rep) {
            this.rep = rep;
        }

        /** {@inheritDoc} */
        @Override
        @Cacheable(cacheNames = CacheItemKey, key = "#id")
        @Transactional(SystemRepository.BeanNameTx)
        public AppSetting setting(String id) {
            Optional<AppSetting> setting = rep.get(AppSetting.class, id);
            if (setting.isEmpty()) {
                log.warn("Initial registered settings do not exist [{}]", id);
                return rep.save(AppSetting.of(id, null));
            } else {
                return setting.get();
            }
        }

        /** {@inheritDoc} */
        @Override
        @CacheEvict(cacheNames = CacheItemKey, key = "#id")
        @Transactional(SystemRepository.BeanNameTx)
        public AppSetting change(String id, String value) {
            return AppSetting.load(rep, id).change(rep, value);
        }

        /** {@inheritDoc} */
        @Override
        @Transactional(transactionManager = SystemRepository.BeanNameTx, propagation = Propagation.REQUIRES_NEW)
        public synchronized long nextId(String id) {
            String uidKey = UIDKeyPrefix + id;
            if (rep.get(AppSetting.class, uidKey).isEmpty()) {
                rep.save(AppSetting.of(uidKey, "0"));
                rep.flushAndClear();
            }
            var setting = rep.loadForUpdate(AppSetting.class, uidKey);
            long nextId = setting.longValue() + 1;
            setting.setValue(String.valueOf(nextId));
            rep.update(setting);
            return nextId;
        }
    }
}
