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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import io.lqd.sdk.LQLog;

public class LQTarget implements Serializable {

    private static final long serialVersionUID = -8930972810546846549L;
    private String mId;

    public LQTarget(String id) {
        mId = id;
    }

    public LQTarget(JSONObject jsonObject){
        try {
            mId = jsonObject.getString("id");
        } catch (JSONException e) {
            LQLog.error("Parsing LQTarget: " + e.getMessage());
        }
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        try {
            json.put("id", getId());
            return json;
        } catch (JSONException e) {
            LQLog.error("LQTarget toJSON: " + e.getMessage());
        }
        return null;
    }

    public String getId() {
        return mId;
    }


}
