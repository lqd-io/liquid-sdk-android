package io.lqd.sdk.model;

import io.lqd.sdk.factory.FactoryGirl;

import java.util.Date;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQSessionTest {

    private LQSession session;
    private JSONObject json;

    @Test
    public void testJSONCreationWithoutAttributes() {
        session = FactoryGirl.createSession();
        session.setEndDate(new Date());
        json = session.toJSON();

        assertTrue(json.has("started_at"));
        assertTrue(json.has("timeout"));
        assertTrue(json.has("unique_id"));
        assertTrue(json.has("ended_at"));
    }


}
