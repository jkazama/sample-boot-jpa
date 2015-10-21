package sample.context;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ResourceBundleHandlerTest {

	@Test
	public void ラベル取得検証() {
		ResourceBundleHandler handler = new ResourceBundleHandler();
		assertTrue(0 < handler.labels("messages").size());
	}
	
}
