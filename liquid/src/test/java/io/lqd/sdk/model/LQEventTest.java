package io.lqd.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.lqd.sdk.LiquidTools;
import io.lqd.sdk.factory.FactoryGirl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;


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


    // #hasValidName
    @Test
    public void testValidName() {
        assertTrue(
                "Should be valid name",
                LQEvent.hasValidName("custom_event", false)
        );
    }

    @Test
    public void testInvalidEventNameUnderscore() {
        assertFalse(
                "( _ )in the beginning of the event name",
                LQEvent.hasValidName("_custom_event", false)
         );
    }

    @Test
    public void testInvalidEventNameDollar() {
        assertFalse(
                "Event name with $",
                LQEvent.hasValidName("custo$m_event", false)
        );
    }

    @Test
    public void testInvalidEventNameDot() {
        assertFalse(
                "Event name with .",
                LQEvent.hasValidName("custo$m_event", false)
        );
    }

    @Test
    public void testInvalidEventNameNullChar() {
        assertFalse(
                "Event name with \0",
                LQEvent.hasValidName("_custom_\0event", false)
        );
    }
}
