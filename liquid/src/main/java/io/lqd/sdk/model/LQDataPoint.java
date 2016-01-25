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

package io.lqd.sdk.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import io.lqd.sdk.LiquidTools;

public class LQDataPoint {


    private LQUser mUser;
    private LQDevice mDevice;
    private LQEvent mEvent;
    private ArrayList<LQValue> mValues;
    private Date mTimestamp;

    public LQDataPoint(LQUser user, LQDevice device, LQEvent event, ArrayList<LQValue> values, Date date) {
        mUser = user;
        mDevice = device;
        mEvent = event;
        mValues = values;
        mTimestamp = date;
    }

    public LQDataPoint(LQUser user, LQDevice device, LQEvent event, ArrayList<LQValue> values) {
        this(user,device,event,values, new Date());
    }

    // JSON
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            JSONObject userJSON = mUser.toJSON();
            if(userJSON != null){
                json.put("user", userJSON);
            }

            JSONObject deviceJSON = mDevice.toJSON();
            if(deviceJSON != null){
                json.put("device", deviceJSON);
            }

            JSONObject eventJSON = mEvent.toJSON();
            if(eventJSON != null){
                json.put("event", eventJSON);
            }

            JSONArray valuesJsonArray = new JSONArray();
            for(LQValue value : mValues){
                JSONObject valueJSON = value.toJSON();
                if(valueJSON != null){
                    valuesJsonArray.put(valueJSON);
                }
            }

            if(valuesJsonArray.length() > 0) {
                json.put("values", valuesJsonArray);
            }

            json.put("timestamp",LiquidTools.dateToString(mTimestamp));
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

}
