package io.lqd.sdk.model;

import io.lqd.sdk.factory.FactoryGirl;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQDataPointTest {

    @Test
    public void testJSONCreationWithoutAttributes() {
        LQDataPoint dp = FactoryGirl.createDataPoint(Robolectric.application);
        JSONObject json = dp.toJSON();

        assertTrue(json.has("event"));
        assertTrue(json.has("device"));
        assertTrue(json.has("user"));
        assertTrue(json.has("session"));
        assertTrue(json.has("timestamp"));
    }

}
