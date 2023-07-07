package sample.context.support;

/**
 * Utility component for ID generation.
 */
public interface IdGenerator {

    String generate(String key);

    default String generate(Class<?> clazz) {
        return generate(clazz.getSimpleName());
    }
}
