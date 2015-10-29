package io.lqd.sdk.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.lqd.sdk.Examples;

import static junit.framework.Assert.assertEquals;

public class LQInappMesssageTest {

    @Before
    public void before() throws JSONException {
        JSONArray json = new JSONArray(Examples.inapp_messages);
    }

    @Test
    public void blablatest() {
        int i = 1;
        int j = 2;

        assertEquals(i, j);
    }
}
