package sample.controller.admin;

import org.junit.Test;

import sample.WebTestSupport;
import sample.util.DateUtils;

//low: 簡易な正常系検証が中心
public class AssetAdminControllerTest extends WebTestSupport {

    @Override
    protected String prefix() {
        return "/api/admin/asset";
    }

    @Test
    public void findCashInOut() throws Exception {
        fixtures.cio("sample", "3000", true).save(rep);
        fixtures.cio("sample", "8000", true).save(rep);
        // low: JSONの値検証は省略
        String day = DateUtils.dayFormat(time.day());
        String query = "updFromDay=" + day + "&updToDay=" + day;
        logger.info(performGet("/cio/?" + query).getResponse().getContentAsString());
    }

}
