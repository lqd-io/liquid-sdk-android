package io.lqd.sdk.model;

import io.lqd.sdk.factory.FactoryGirl;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQDataPointTest {

    JSONObject json;

    @Before
    public void before() {
        json = FactoryGirl.createDataPoint(Robolectric.application).toJSON();
    }

    @Test
    public void testDataPointHaveEvent() {
        assertTrue(json.has("event"));
    }

    @Test
    public void testDatePointHaveDevice() {
        assertTrue(json.has("device"));
    }

    @Test
    public void testDatePointHaveUser() {
        assertTrue(json.has("user"));
    }

    @Test
    public void testDatePointHaveSession() {
        assertTrue(json.has("session"));
    }

    @Test
    public void testDatePointHaveTimestamp() {
        assertTrue(json.has("timestamp"));
    }

}
