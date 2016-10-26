package sample.context;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ResourceBundleHandlerTest {

    @Test
    public void labels() {
        ResourceBundleHandler handler = new ResourceBundleHandler();
        assertTrue(0 < handler.labels("messages").size());
    }

}
