package io.lqd.sdk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import io.lqd.sdk.model.LQSession;
import static org.junit.Assert.assertEquals;

@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LiquidTest {

    @Test
    public void testKeepSessionOnIdentify() throws NoSuchFieldException, IllegalAccessException {
        Liquid lqd = Liquid.initialize(Robolectric.application, "le_token");
        Field f = Liquid.class.getDeclaredField("mCurrentSession");
        f.setAccessible(true);
        String session_id = ((LQSession) f.get(lqd)).getIdentifier();
        lqd.identifyUser("le_user_id");
        assertEquals(session_id, ((LQSession) f.get(lqd)).getIdentifier());
    }
}
