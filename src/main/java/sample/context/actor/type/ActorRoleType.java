package sample.context.actor.type;

/**
 * Expresses the role of the user.
 */
public enum ActorRoleType {
    /** Anonymous users (users who do not have IDs or other specific information) */
    ANONYMOUS,
    /** Users (mainly BtoC customers, BtoB user employees) */
    USER,
    /** Internal users (mainly BtoC employees, BtoB administrator employees) */
    INTERNAL,
    /**
     * System administrator (employee in charge of IT systems or employee of system
     * management company)
     */
    ADMINISTRATOR,
    /** System (automatic processing on the system) */
    SYSTEM;

    public boolean isAnonymous() {
        return this == ANONYMOUS;
    }

    public boolean isUser() {
        return this == USER;
    }

    public boolean isSystem() {
        return this == SYSTEM;
    }

    public boolean notSystem() {
        return !isSystem();
    }

    public static boolean has(String name) {
        for (var roleType : values()) {
            if (roleType.name().equals(name.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

}
