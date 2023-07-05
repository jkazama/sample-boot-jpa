package sample.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import sample.context.support.ResourceBundleHandler;

public class ResourceBundleHandlerTest {

    @Test
    public void ラベル取得検証() {
        ResourceBundleHandler handler = new ResourceBundleHandler();
        assertTrue(0 < handler.labels("messages").size());
    }

}
