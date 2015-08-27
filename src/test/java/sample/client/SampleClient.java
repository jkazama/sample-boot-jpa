package sample.client;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.*;

/**
 * 単純なHTTP経由の実行検証。
 * <p>SpringがサポートするWebTestSupportでの検証で良いのですが、コンテナ立ち上げた後に叩く単純確認用に作りました。
 */
public class SampleClient {
	private static final String ROOT_PATH = "http://localhost:8080/api";
	private SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
	
	@Test
	public void 振込出金依頼をする() throws Exception {
		post("振込出金依頼", "/asset/cio/withdraw?accountId=sample&currency=JPY&absAmount=200");
		get("振込出金依頼確認", "/asset/cio/unprocessedOut");
	}
	
//	@Test
//	public void proceedDay() throws Exception {
//		ClientHttpResponse res = post("/system/job/daily/processDay");
//		System.out.println(res.getStatusCode());
//	}
	
	private ClientHttpResponse get(String title, String path) throws Exception {
		title(title);
		return dump(factory.createRequest(new URI(ROOT_PATH + path), HttpMethod.GET).execute());
	}
	
	private ClientHttpResponse post(String title, String path) throws Exception {
		title(title);
		return dump(factory.createRequest(new URI(ROOT_PATH + path), HttpMethod.POST).execute());
	}
	
	private void title(String title) {
		System.out.println("------- " + title + "------- ");
	}
	
	private ClientHttpResponse dump(ClientHttpResponse res) throws Exception {
		System.out.println(String.format("status: %d, text: %s", res.getRawStatusCode(), res.getStatusText()));
		System.out.println(IOUtils.toString(res.getBody()));
		return res;
	}

}
