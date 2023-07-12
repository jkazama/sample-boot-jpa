package sample.context.actor;

/**
 * Thread Local Scope User Session.
 */
public class ActorSession {
    private static ThreadLocal<Actor> actorLocal = new ThreadLocal<>();

    /** Connect users to the user session. */
    public static void bind(final Actor actor) {
        actorLocal.set(actor);
    }

    /** Discard the user session. */
    public static void unbind() {
        actorLocal.remove();
    }

    /** Returns a valid user. If not associated, an anonymous user is returned. */
    public static Actor actor() {
        Actor actor = actorLocal.get();
        if (actor == null) {
            return Actor.ANONYMOUS;
        }
        return actor;
    }
}
