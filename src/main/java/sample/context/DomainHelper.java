package sample.context;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sample.ApplicationProperties;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;
import sample.context.spring.ObjectProviderAccessor;
import sample.context.support.AppSettingHandler;

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
        private final ObjectProvider<AppSettingHandler> settingHandler;
        private final ObjectProviderAccessor accessor;

        /** {@inheritDoc} */
        @Override
        public Timestamper time() {
            return this.accessor.bean(this.time, Timestamper.class);
        }

        /** {@inheritDoc} */
        @Override
        public AppSettingHandler setting() {
            return this.accessor.bean(settingHandler, AppSettingHandler.class);
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationProperties props() {
            return this.props;
        }

    }

    // /** Domain helpers for mock testing */
    // public static class DomainHelperMock implements DomainHelper {
    // private final ApplicationProperties props = new ApplicationProperties();
    // private final Timestamper time = new TimestamperMock();
    // private final AppSettingHandler setting = new AppSettingHandlerMock();

    // /** {@inheritDoc} */
    // @Override
    // public Timestamper time() {
    // return time;
    // }

    // /** {@inheritDoc} */
    // @Override
    // public AppSettingHandler setting() {
    // return this.setting;
    // }

    // /** {@inheritDoc} */
    // @Override
    // public ApplicationProperties props() {
    // return this.props;
    // }

    // }

}
