package sample.usecase.event;

import lombok.Builder;
import sample.context.Dto;
import sample.usecase.event.type.NotificationType;

/**
 * Represents an email distribution event.
 */
@Builder
public record NotificationEvent<T>(
        NotificationType notificationType,
        T value) implements Dto {

    public static <T> NotificationEvent<T> of(NotificationType notificationType, T value) {
        return NotificationEvent.<T>builder()
                .notificationType(notificationType)
                .value(value)
                .build();
    }

}
