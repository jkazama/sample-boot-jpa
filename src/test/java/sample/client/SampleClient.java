package sample.client;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.http.client.*;

import sample.util.*;

/**
 * 単純なHTTP経由の実行検証。
 * <p>SpringがサポートするWebTestSupportでの検証で良いのですが、コンテナ立ち上げた後に叩く単純確認用に作りました。
 * <p>「extention.security.auth.enabled: true」の時は実際にログインして処理を行います。
 * falseの時はDummyLoginInterceptorによる擬似ログインが行われます。
 */
public class SampleClient {
    private static final String ROOT_PATH = "http://localhost:8080/api";

    // 「extention.security.auth.admin: false」の時のみ利用可能です。
    @Test
    public void 顧客向けユースケース検証() throws Exception {
        SimpleTestAgent agent = new SimpleTestAgent();
        agent.login("sample", "sample");
        agent.post("振込出金依頼", "/asset/cio/withdraw?accountId=sample&currency=JPY&absAmount=200");
        agent.get("振込出金依頼未処理検索", "/asset/cio/unprocessedOut/");
    }

    // 「extention.security.auth.admin: true」の時のみ利用可能です。
    @Test
    public void 社内向けユースケース検証() throws Exception {
        String day = DateUtils.dayFormat(TimePoint.now().day());
        SimpleTestAgent agent = new SimpleTestAgent();
        agent.login("admin", "admin");
        agent.get("振込入出金依頼検索", "/admin/asset/cio/?updFromDay=" + day + "&updToDay=" + day);
    }

    @Test
    public void バッチ向けユースケース検証() throws Exception {
        String fromDay = DateUtils.dayFormat(TimePoint.now().day().minusDays(1));
        String toDay = DateUtils.dayFormat(TimePoint.now().day().plusDays(3));
        SimpleTestAgent agent = new SimpleTestAgent();
        agent.post("営業日を進める(単純日回しのみ)", "/system/job/daily/processDay");
        agent.post("当営業日の出金依頼を締める", "/system/job/daily/closingCashOut");
        agent.post("入出金キャッシュフローを実現する(受渡日に残高へ反映)", "/system/job/daily/realizeCashflow");
        agent.get("イベントログを検索する", "/admin/system/audit/event/?fromDay=" + fromDay + "&toDay=" + toDay);
    }

    /** 単純なSession概念を持つHTTPエージェント */
    private class SimpleTestAgent {
        private SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        private Optional<String> sessionId = Optional.empty();

        public URI path(String path) throws Exception {
            return new URI(ROOT_PATH + path);
        }

        public SimpleTestAgent login(String loginId, String password) throws Exception {
            ClientHttpResponse res = post("ログイン", "/login?loginId=" + loginId + "&password=" + password);
            if (res.getStatusCode() == HttpStatus.OK) {
                String cookieStr = res.getHeaders().get("Set-Cookie").get(0);
                sessionId = Optional.of(cookieStr.substring(0, cookieStr.indexOf(';')));
            }
            return this;
        }

        public ClientHttpResponse get(String title, String path) throws Exception {
            title(title);
            return dump(request(path, HttpMethod.GET).execute());
        }

        private ClientHttpRequest request(String path, HttpMethod method) throws Exception {
            ClientHttpRequest req = factory.createRequest(path(path), method);
            sessionId.ifPresent((jsessionId) -> req.getHeaders().add("Cookie", jsessionId));
            return req;
        }

        public ClientHttpResponse post(String title, String path) throws Exception {
            title(title);
            return dump(request(path, HttpMethod.POST).execute());
        }

        public void title(String title) {
            System.out.println("------- " + title + "------- ");
        }

        public ClientHttpResponse dump(ClientHttpResponse res) throws Exception {
            System.out.println(String.format("status: %d, text: %s", res.getRawStatusCode(), res.getStatusText()));
            try {
                System.out.println(IOUtils.toString(res.getBody()));
            } catch (IOException e) {
                /* nothing. */ }
            return res;
        }

    }

}
