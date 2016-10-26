package sample.usecase;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;

import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;

import sample.UnitTestSupport;
import sample.model.BusinessDayHandler;

public class SystemAdminServiceTest extends UnitTestSupport {

    @Autowired
    private BusinessDayHandler businessDay;
    @Autowired
    private SystemAdminService service;
    
    @Before
    public void setup() {
        loginSystem();
    }
    
    @Test
    public void processDay() {
        LocalDate day = businessDay.day();
        LocalDate dayPlus1 = businessDay.day(1);
        LocalDate dayPlus2 = businessDay.day(2);
        assertThat(time.day(), is(day));
        service.processDay();
        assertThat(time.day(), is(dayPlus1));
        service.processDay();
        assertThat(time.day(), is(dayPlus2));
    }
    
}
