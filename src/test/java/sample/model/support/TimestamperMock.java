package sample.model.support;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import sample.context.Timestamper;

@AllArgsConstructor(staticName = "of")
public class TimestamperMock implements Timestamper {
    private LocalDate day;
    private LocalDateTime date;

    /** {@inheritDoc} */
    @Override
    public LocalDate day() {
        return day != null ? day : LocalDate.now();
    }

    /** {@inheritDoc} */
    @Override
    public LocalDateTime date() {
        return date != null ? date : LocalDateTime.now();
    }

    /** {@inheritDoc} */
    @Override
    public Timestamper forwardDay(LocalDate day) {
        this.day = day;
        return this;
    }

    public TimestamperMock fixed(LocalDate day, LocalDateTime date) {
        this.day = day;
        this.date = date;
        return this;
    }

    public static TimestamperMock of() {
        return of(null, null);
    }

}
