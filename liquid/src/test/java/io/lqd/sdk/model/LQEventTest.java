package io.lqd.sdk.model;

import io.lqd.sdk.LiquidTools;
import io.lqd.sdk.factory.FactoryGirl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQEventTest {

    private LQEvent event;
    private JSONObject json;

    @Test
    public void testJSONCreationWithoutAttributes() {
        event = FactoryGirl.createEvent();
        json = event.toJSON();

        assertTrue(json.has("name"));
        assertTrue(json.has("date"));
    }
    @Test
    public void testJSONCreationWithAttributes() throws JSONException {
        HashMap<String, Object> attrs = new HashMap<String, Object>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 30);
        Date now = new Date();
        attrs.put("attr1", true);
        attrs.put("attr2", "le_value");
        attrs.put("attr3", now);

        // override default event methods
        attrs.put("name", "le_name");
        attrs.put("date", LiquidTools.dateToString(cal.getTime()));

        event = FactoryGirl.createEvent(attrs);
        json = event.toJSON();

        assertTrue(json.has("name"));
        assertTrue(json.has("date"));
        assertTrue(json.has("attr1"));
        assertTrue(json.has("attr2"));
        assertTrue(json.has("attr3"));
        assertEquals(true, json.getBoolean("attr1"));
        assertEquals("le_value", json.getString("attr2"));
        assertEquals(LiquidTools.dateToString(now), json.getString("attr3"));

        // can't override default event methods
        assertNotSame(LiquidTools.dateToString(cal.getTime()), json.getString("date"));
        assertNotSame("le_name", json.getString("name"));

    }



}
