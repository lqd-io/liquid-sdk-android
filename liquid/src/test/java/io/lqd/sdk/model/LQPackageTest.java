package io.lqd.sdk.model;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.test.AndroidTestCase;

import io.lqd.sdk.Examples;

import static org.junit.Assert.*;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQPackageTest extends AndroidTestCase {

    @Test
    public void testLoadFromNetwork() throws JSONException, IOException {

        JSONObject json = new JSONObject(Examples.liquidpackage1);

        LQLiquidPackage pack = new LQLiquidPackage(json);

        assertEquals(2, pack.getValues().size());

        LQValue v1 = pack.getValues().get(0);
        LQValue v2 = pack.getValues().get(1);

        for(LQValue v : pack.getValues()) {
            assertNotNull(v.getId());
            assertNotNull(v.isDefault());
            assertNotNull(v.getValue());
            assertNull(v.getTargetId());
        }

        assertEquals("Hello!", v1.getValue());
        assertEquals(true, v2.getValue());

    }

}
