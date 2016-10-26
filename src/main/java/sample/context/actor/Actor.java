package sample.context.actor;

import java.util.Locale;

import lombok.*;
import sample.context.Dto;

/**
 * User in the use case.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Actor implements Dto {
    private static final long serialVersionUID = 1L;

    /** Anonymous user */
    public static Actor Anonymous = new Actor("unknown", ActorRoleType.Anonymous);
    /** System user */
    public static Actor System = new Actor("system", ActorRoleType.System);

    private String id;
    private String name;
    private ActorRoleType roleType;
    private Locale locale;
    /** Connection channel name of the actor */
    private String channel;
    /** Outside information to identify a actor. (including the IP) */
    private String source;

    public Actor(String id, ActorRoleType roleType) {
        this(id, id, roleType);
    }

    public Actor(String id, String name, ActorRoleType roleType) {
        this(id, name, roleType, Locale.getDefault(), null, null);
    }

    /**
     * Role of the actor.
     */
    public static enum ActorRoleType {
        /** Anonymous user. (the actor who does not have specific information such as the ID) */
        Anonymous,
        /** User (mainly customer of BtoC, staff of BtoB) */
        User,
        /** Internal user (mainly staff of BtoC, staff manager of BtoB) */
        Internal,
        /** System administrator (an IT system charge staff or staff of the system management company) */
        Administrator,
        /** System (automatic processing on the system) */
        System;

        public boolean isAnonymous() {
            return this == Anonymous;
        }

        public boolean isSystem() {
            return this == System;
        }

        public boolean notSystem() {
            return !isSystem();
        }
    }
}
