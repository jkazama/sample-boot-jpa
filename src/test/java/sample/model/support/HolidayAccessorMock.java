package sample.model.support;

import java.time.LocalDate;
import java.util.Optional;

import sample.context.orm.OrmRepository;
import sample.model.BusinessDayHandler.HolidayAccessor;
import sample.model.master.Holiday;
import sample.model.master.Holiday.RegHoliday;

public class HolidayAccessorMock implements HolidayAccessor {

    @Override
    public Optional<Holiday> get(LocalDate holiday) {
        return Optional.empty();
    }

    @Override
    public void register(OrmRepository rep, RegHoliday param) {
        // nothing.
    }

}
