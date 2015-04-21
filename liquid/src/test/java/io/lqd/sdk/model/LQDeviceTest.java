package io.lqd.sdk.model;

import android.location.Location;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.lqd.sdk.factory.FactoryGirl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQDeviceTest {

    LQDevice device;
    JSONObject json;

    @Before
    public void before() {
        device = FactoryGirl.createDevice(Robolectric.application);
        json = device.toJSON();
    }

    @Test
    public void testDeviceHaveVendor() {
        assertTrue(json.has("vendor"));
    }

    @Test
    public void testDeviceHavePlatform() {
        assertTrue(json.has("platform"));
    }

    @Test
    public void testDeviceHaveModel() {
        assertTrue(json.has("model"));
    }

    @Test
    public void testDeviceHaveSystemVersion() {
        assertTrue(json.has("system_version"));
    }

    @Test
    public void testDeviceHaveSystemLanguage() {
        assertTrue(json.has("system_language"));
    }

    @Test
    public void testDeviceHaveScreenSize() {
        assertTrue(json.has("screen_size"));
    }

    @Test
    public void testDeviceHaveCarrier() {
        assertTrue(json.has("carrier"));
    }

    @Test
    public void testDeviceHaveInternetConnectivity() {
        assertTrue(json.has("internet_connectivity"));
    }

    @Test
    public void testDeviceHaveUniqueID() {
        assertTrue(json.has("unique_id"));
    }

    @Test
    public void testDeviceHaveAppBundle() {
        assertTrue(json.has("app_bundle"));
    }

    @Test
    public void testDeviceHaveReleaseVersion() {
        assertTrue(json.has("release_version"));
    }

    @Test
    public void testDeviceHaveLiquidVersion() {
        assertTrue(json.has("liquid_version"));
    }

    @Test
    public void testDeviceDoesNotHaveLatitude() {
        assertFalse(json.has("latitude"));
    }

    @Test
    public void testDeviceDoesNotHaveLongitude() {
        assertFalse(json.has("longitude"));
    }

    @Test
    public void testDeviceDoesNotHavePushToken() {
        assertFalse(json.has("push_token"));
    }

    @Test
    public void testDeviceHaveCoordinates() {
        Location l = new Location("le_test");
        l.setLatitude(123);
        l.setLongitude(31);
        device.setLocation(l);
        device.setPushId("le_id");
        json = device.toJSON();
        assertTrue(json.has("latitude"));
        assertTrue(json.has("longitude"));
    }

    @Test
    public void testDeviceHavePushToken() {
        device.setPushId("le_id");
        json = device.toJSON();
        assertTrue(json.has("push_token"));
    }

}
