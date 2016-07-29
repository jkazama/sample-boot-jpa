package sample.controller;

import static org.mockito.BDDMockito.*;

import java.util.*;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import sample.WebTestSupport;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.usecase.AssetService;

/**
 * AssetController の単体検証です。
 * <p>low: 簡易な正常系検証が中心
 */
@WebMvcTest(AssetController.class)
public class AssetControllerTest extends WebTestSupport {

    @MockBean
    private AssetService service;
    
    @Override
    protected String prefix() {
        return "/api/asset";
    }

    @Test
    public void 未処理の振込依頼情報を検索します() {
        given(service.findUnprocessedCashOut()).willReturn(resultCashOuts());
        performGet("/cio/unprocessedOut/",
            JsonExpects.success()
                .match("$[0].currency", "JPY")
                .match("$[0].absAmount", 3000)
                .match("$[1].absAmount", 4000));
    }

    private List<CashInOut> resultCashOuts() {
        return Arrays.asList(
                fixtures.cio("sample", "3000", true),
                fixtures.cio("sample", "4000", true));
    }

    @Test
    public void 振込出金依頼をします() {
        given(service.withdraw(any(RegCashOut.class))).willReturn(1L);
        performPost(
          uriBuilder("/cio/withdraw")
            .queryParam("accountId", "sample")
            .queryParam("currency", "JPY")
            .queryParam("absAmount", "1000")
            .build(),
          JsonExpects.success()
        );
    }

}
