package sample.usecase;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;

import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;

import sample.UnitTestSupport;
import sample.model.BusinessDayHandler;

/**
 * SystemAdminService の単体検証です。
 * <p>low: 簡易な正常系検証が中心
 */
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
    public void 営業日を進めます() {
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
