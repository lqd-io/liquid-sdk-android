package io.lqd.sdk.model;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import io.lqd.sdk.factory.FactoryGirl;

import static org.junit.Assert.assertTrue;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQSessionTest {

    private JSONObject json;

    @Before
    public void before() {
        LQSession session = FactoryGirl.createSession();
        session.setEndDate(new Date());
        json = session.toJSON();
    }

    @Test
    public void testSessionHaveStartedAt() {
        assertTrue(json.has("started_at"));
    }

    @Test
    public void testSessionHaveTimeout() {
        assertTrue(json.has("timeout"));
    }

    @Test
    public void testSessionHaveUniqueID() {
        assertTrue(json.has("unique_id"));
    }

    @Test
    public void testSessionHaveEndedAt() {
        assertTrue(json.has("ended_at"));
    }
}
