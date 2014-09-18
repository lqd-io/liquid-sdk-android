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

import io.lqd.sdk.LQLog;
import io.lqd.sdk.LiquidTools;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;

public class LQUser extends LQModel {

    private static final long serialVersionUID = 1582937331182018907L;

    private String mIdentifier;
    private boolean mIdentified;
    private HashMap<String, Object> mAttributes = new HashMap<String, Object>();


    public LQUser(String identifier) {
        this(identifier, new HashMap<String,Object>(), null);
    }

    public LQUser(String identifier, boolean identified) {
        this(identifier, new HashMap<String,Object>(), null, identified);
    }

    public LQUser(String identifier, HashMap<String, Object> attributes, Location location) {
        this(identifier, attributes, location, true);
    }

    public LQUser(String identifier, HashMap<String, Object> attributes, Location location, boolean identified) {
        mIdentifier = identifier;
        if(attributes != null)
            mAttributes = attributes;
        this.setLocation(location);
        this.setIdentified(identified);
    }

    // Attributes
    public String getIdentifier() {
        return mIdentifier;
    }

    public boolean isIdentified() {
        return mIdentified;
    }

    public void setIdentified(boolean mAutoIdentified) {
        this.mIdentified = mAutoIdentified;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof LQUser) && ( this.toJSON().toString().equals(((LQUser) o).toJSON().toString()));
    }

    public HashMap<String, Object> getAttributes() {
        return new HashMap<String, Object>(mAttributes);
    }

    public void setAttributes(HashMap<String, Object> attributes) {
        mAttributes = attributes;
    }

    public Object setAttribute(String key, Object attribute) {
        return mAttributes.put(key, attribute);
    }

    public Object attributeForKey(String key) {
        return mAttributes.get(key);
    }

    public void setLocation(Location location) {
        if (location == null) {
            mAttributes.remove("latitude");
            mAttributes.remove("longitude");
        } else {
            mAttributes.put("latitude", Double.valueOf(location.getLatitude()));
            mAttributes.put("longitude",Double.valueOf(location.getLongitude()));
        }
    }

    // JSON
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            if(mAttributes != null) {
                for(String key : mAttributes.keySet()) {
                    if(mAttributes.get(key) instanceof Date) {
                        json.put(key, LiquidTools.dateToString((Date) mAttributes.get(key)));
                    } else {
                        json.put(key, mAttributes.get(key));
                    }
                }
            }
            json.put("unique_id", mIdentifier);
            json.put("identified", mIdentified);
            return json;
        } catch (JSONException e) {
            LQLog.error("LQUser toJSON: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void save(Context context, String path) {
        super.save(context, path + ".user");
    }

    public static LQUser load(Context context, String path) {
        LQUser user = (LQUser) LQModel.load(context, path + ".user");
        if(user == null) {
            user = new LQUser(LQModel.newIdentifier(), false);
        }
        return user;
    }

}
