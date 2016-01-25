/**
 * Copyright 2014-present Liquid Data Intelligence S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lqd.sdk.oneline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.lqd.sdk.LQLog;

public class LQUiElement {

    private String mIdentifier;
    private String mEventName;

    public static ArrayList<LQUiElement> parse(JSONArray array) {
        ArrayList<LQUiElement> list = new ArrayList<>();
        int length = array.length();
        for(int i = 0; i < length ; ++i) {
            try {
                JSONObject uielement = array.getJSONObject(i);
                list.add(new LQUiElement(uielement));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public LQUiElement(JSONObject element) {
        try {
            mIdentifier = element.getString("identifier");
            mEventName = element.getString("event_name");
        } catch (JSONException e) {
            LQLog.infoVerbose("Could not get element path or eventname");
        }
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public String getEventName() {
        return mEventName;
    }
}
