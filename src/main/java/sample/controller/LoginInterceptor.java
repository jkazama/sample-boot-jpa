package sample.controller;

import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import sample.context.actor.*;
import sample.context.actor.Actor.ActorRoleType;
import sample.context.security.SecurityConfigurer;

/**
 * AOPInterceptor relates a login user with thread local.
 */
@Aspect
@Configuration
public class LoginInterceptor {
    
    @Autowired
    private ActorSession session;

    @Before("execution(* *..controller.system.*Controller.*(..))")
    public void bindSystem() {
        session.bind(Actor.System);
    }

    @After("execution(* *..controller..*Controller.*(..))")
    public void unbind() {
        session.unbind();
    }

    /**
     * The dummy-login handling of that certification setting (extension.security.auth.enabled: false) is done.
     * <p>Use it only at the time of development.
     */
    @Aspect
    @Component
    @ConditionalOnMissingBean(SecurityConfigurer.class)
    public static class DummyLoginInterceptor {
        @Autowired
        private ActorSession session;

        @Before("execution(* *..controller.*Controller.*(..))")
        public void bindUser() {
            session.bind(new Actor("sample", ActorRoleType.User));
        }

        @Before("execution(* *..controller.admin.*Controller.*(..))")
        public void bindAdmin() {
            session.bind(new Actor("admin", ActorRoleType.Internal));
        }
    }

}
