package sample.context;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sample.ApplicationProperties;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;
import sample.context.spring.ObjectProviderAccessor;
import sample.context.support.AppSettingHandler;
import sample.context.support.IdGenerator;

/**
 * Provides access to infrastructure layer components required for domain
 * processing.
 */
public interface DomainHelper {

    /** Returns the currently logged-in use case user. */
    default Actor actor() {
        return ActorSession.actor();
    }

    /** Returns date/time utility. */
    Timestamper time();

    /** Returns UID Generator */
    IdGenerator uid();

    /** Returns the application configuration utility. */
    AppSettingHandler setting();

    /** Returns application properties. */
    ApplicationProperties props();

    /**
     * DomainHelper implementation considering lazy loading
     * <p>
     * Use this for use with DI containers.
     */
    @Component
    @RequiredArgsConstructor(staticName = "of")
    public static class DomainHelperProviderImpl implements DomainHelper {
        private final ApplicationProperties props;
        private final ObjectProvider<Timestamper> time;
        private final ObjectProvider<IdGenerator> uid;
        private final ObjectProvider<AppSettingHandler> setting;
        private final ObjectProviderAccessor accessor;

        /** {@inheritDoc} */
        @Override
        public Timestamper time() {
            return this.accessor.bean(this.time, Timestamper.class);
        }

        /** {@inheritDoc} */
        @Override
        public IdGenerator uid() {
            return this.accessor.bean(uid, IdGenerator.class);
        }

        /** {@inheritDoc} */
        @Override
        public AppSettingHandler setting() {
            return this.accessor.bean(setting, AppSettingHandler.class);
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationProperties props() {
            return this.props;
        }

    }

}
