package sample.controller.system;

import static org.mockito.BDDMockito.*;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import sample.*;
import sample.usecase.*;

@WebMvcTest(JobController.class)
public class JobControllerTest extends WebTestSupport {

    @MockBean
    private AssetAdminService asset;
    @MockBean
    private SystemAdminService system;
    
    @Override
    protected String prefix() {
        return "/api/system/job";
    }

    @Test
    public void processDay() throws Exception {
        willDoNothing().given(system).processDay();
        performPost("/daily/processDay", JsonExpects.success());
    }

    @Test
    public void closingCashOut() throws Exception {
        willDoNothing().given(asset).closingCashOut();
        performPost("/daily/closingCashOut", JsonExpects.success());
    }

    @Test
    public void realizeCashflow() throws Exception {
        willDoNothing().given(asset).realizeCashflow();
        performPost("/daily/realizeCashflow", JsonExpects.success());
    }

}
