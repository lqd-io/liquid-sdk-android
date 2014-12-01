package io.lqd.sdk.model;

import io.lqd.sdk.factory.FactoryGirl;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.location.Location;

import static org.junit.Assert.*;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQDeviceTest {

    LQDevice device;
    JSONObject json;

    @Test
    public void testJSONCreationWithoutAttributes() {
        device = FactoryGirl.createDevice(Robolectric.application);
        json = device.toJSON();

        assertTrue(json.has("vendor"));
        assertTrue(json.has("platform"));
        assertTrue(json.has("model"));
        assertTrue(json.has("system_version"));
        assertTrue(json.has("system_language"));
        assertTrue(json.has("screen_size"));
        assertTrue(json.has("carrier"));
        assertTrue(json.has("internet_connectivity"));
        assertTrue(json.has("unique_id"));
        assertTrue(json.has("app_bundle"));
        assertTrue(json.has("release_version"));
        assertTrue(json.has("liquid_version"));
        assertFalse(json.has("latitude"));
        assertFalse(json.has("longitude"));
        assertFalse(json.has("push_token"));

        Location l = new Location("le_test");
        l.setLatitude(123);
        l.setLongitude(31);
        device.setLocation(l);
        device.setPushId("le_id");

        json = device.toJSON();

        assertTrue(json.has("latitude"));
        assertTrue(json.has("longitude"));
        assertTrue(json.has("push_token"));

    }

}
