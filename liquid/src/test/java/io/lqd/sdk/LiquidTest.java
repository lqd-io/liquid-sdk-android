package io.lqd.sdk;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

import io.lqd.sdk.model.LQSession;
import io.lqd.sdk.model.LQUser;

@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LiquidTest {

        private Liquid lqd;
    @Before
    public void setUp() {
        lqd = Liquid.initialize(Robolectric.application, "le_token");
    }

    @Test
    public void testResetUserIdentified() {
        lqd.identifyUser("new_id");
        assertEquals("new_id", lqd.getUserIdentifier());
        lqd.resetUser();
        assertNotEquals("new_id", lqd.getUserIdentifier()); //resets the user id
    }

    @Test
    public void testResetUserAnonymous() {
        String id = lqd.getUserIdentifier();
        lqd.resetUser();
        assertEquals(id, lqd.getUserIdentifier());
    }


    public void testKeepSessionOnIdentify() throws NoSuchFieldException, IllegalAccessException {
        Field f = Liquid.class.getDeclaredField("mCurrentSession");
        f.setAccessible(true);
        String session_id = ((LQSession) f.get(lqd)).getIdentifier();
        lqd.identifyUser("le_user_id");
        assertEquals(session_id, ((LQSession) f.get(lqd)).getIdentifier());
    }

    // public void softReset()

    @Test
    public void testnewSessionAfterReset() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        Field f = Liquid.class.getDeclaredField("mCurrentSession");
        f.setAccessible(true);
        lqd.softReset();
        assertNotNull(f.get(lqd));
    }

    // public void setUserAttributes(final HashMap<String, Object> attributes)

    @Test
    public void testSetAttributes() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        HashMap<String, Object> attrs = new HashMap<>();
        attrs.put("key", 1);
        attrs.put("key_2", "value");
        lqd.setUserAttributes(attrs);
        Field f = Liquid.class.getDeclaredField("mCurrentUser");
        f.setAccessible(true);
        LQUser user = ((LQUser) f.get(lqd));
        Thread.sleep(2000);
        assertEquals(1, user.attributeForKey("key"));
        assertEquals("value", user.attributeForKey("key_2"));
    }
}
