package io.lqd.sdk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LiquidTest {

    @Test
    public void testResetUserIdentified() {
        Liquid lqd = Liquid.initialize(Robolectric.application, "api_token");
        lqd.identifyUser("new_id");
        assertEquals("new_id", lqd.getUserIdentifier());
        lqd.resetUser();
        assertNotEquals("new_id", lqd.getUserIdentifier()); //resets the user id
    }

    @Test
    public void testResetUserAnonymous() {
        Liquid lqd = Liquid.initialize(Robolectric.application, "api_token");
        String id = lqd.getUserIdentifier();
        lqd.resetUser();
        assertEquals(id, lqd.getUserIdentifier());
    }
}
