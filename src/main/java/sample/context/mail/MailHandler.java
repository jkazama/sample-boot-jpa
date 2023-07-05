package sample.context.mail;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sample.ApplicationProperties;

/**
 * Send and receive mail.
 * low: In the sample, only I / F for sending mail is created. In practice it
 * also supports receiving emails such as POP3 / IMAP.
 */
public interface MailHandler {

    void send(final SendMail mail);

    @Builder
    public static record SendMail(
            String address,
            String subject,
            String body,
            Map<String, String> bodyArgs) {
    }

    @Component
    @RequiredArgsConstructor(staticName = "of")
    @Slf4j
    public static class MailHandlerImpl implements MailHandler {
        @Value("${sample.mail.enabled:true}")
        private final ApplicationProperties props;

        @Override
        public void send(final SendMail mail) {
            if (!props.getMail().isEnabled()) {
                log.info("Sent a dummy email. [" + mail.subject + "]");
            }

            // low: There should be a lot of overhead in cooperation with external
            // resources, so it should be done asynchronously.
            // low: Send the contents of the substitution mapping of bodyArgs to body by
            // JavaMail etc.
        }
    }
}
