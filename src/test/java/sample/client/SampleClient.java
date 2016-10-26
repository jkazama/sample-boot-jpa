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
 * Check api via the simple HTTP.
 * <p>The inspection in WebTestSupport which Spring supported was enough,
 *  but made a container for simplicity confirmation to swat after standing, and having given it.
 * <p>you really login and handle it at the time of "extention.security.auth.enabled: true".
 * dummy-login by DummyLoginInterceptor is carried out at the time of false.
 */
public class SampleClient {
    private static final String ROOT_PATH = "http://localhost:8080/api";

    // for 「extention.security.auth.admin: false」
    @Test
    public void usecaseCustomer() throws Exception {
        SimpleTestAgent agent = new SimpleTestAgent();
        agent.login("sample", "sample");
        agent.post("withdraw", "/asset/cio/withdraw?accountId=sample&currency=JPY&absAmount=200");
        agent.get("unprocessedOut", "/asset/cio/unprocessedOut/");
    }

    // for 「extention.security.auth.admin: true」 
    @Test
    public void usecaseInternal() throws Exception {
        String day = DateUtils.dayFormat(TimePoint.now().day());
        SimpleTestAgent agent = new SimpleTestAgent();
        agent.login("admin", "admin");
        agent.get("findCashInOut", "/admin/asset/cio/?updFromDay=" + day + "&updToDay=" + day);
    }

    @Test
    public void usecaseBatch() throws Exception {
        String fromDay = DateUtils.dayFormat(TimePoint.now().day().minusDays(1));
        String toDay = DateUtils.dayFormat(TimePoint.now().day().plusDays(3));
        SimpleTestAgent agent = new SimpleTestAgent();
        agent.post("processDay", "/system/job/daily/processDay");
        agent.post("closingCashOut", "/system/job/daily/closingCashOut");
        agent.post("realizeCashflow", "/system/job/daily/realizeCashflow");
        agent.get("findAuditEvent", "/admin/system/audit/event/?fromDay=" + fromDay + "&toDay=" + toDay);
    }

    private class SimpleTestAgent {
        private SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        private Optional<String> sessionId = Optional.empty();

        public URI path(String path) throws Exception {
            return new URI(ROOT_PATH + path);
        }

        public SimpleTestAgent login(String loginId, String password) throws Exception {
            ClientHttpResponse res = post("Login", "/login?loginId=" + loginId + "&password=" + password);
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
                System.out.println(IOUtils.toString(res.getBody(), "UTF-8"));
            } catch (IOException e) {
                /* nothing. */ }
            return res;
        }

    }

}
