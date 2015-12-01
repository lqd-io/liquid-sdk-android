package io.lqd.sdk.model;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.lqd.sdk.LQLog;


public class InappMessageParser {

    public static ArrayList<LQInAppMessage> parse(JSONArray array) {
        ArrayList<LQInAppMessage> list = new ArrayList<>();

        int length = array.length();
        for(int i = 0; i < length ; ++i) {
            try {
                JSONObject inapp = array.getJSONObject(i);
                list.add(new LQInAppMessage(inapp));
            } catch (JSONException e) {
                LQLog.error("Error parsing inapp message " + array);
            }
        }
        return list;
    }
}
