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

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import io.lqd.sdk.LQLog;
import io.lqd.sdk.LiquidTools;

public class LQUser extends LQModel {

    private static final long serialVersionUID = 1582937331182018907L;
    private static final String[] RESERVED_KEYS  = {"unique_id", "identified"};

    private String mIdentifier;
    private boolean mIdentified;
    private HashMap<String, Object> mAttributes = new HashMap<String, Object>();

    public LQUser(String identifier) {
        this(identifier, new HashMap<String,Object>());
    }

    public LQUser(String identifier, boolean identified) {
        this(identifier, new HashMap<String,Object>(), identified);
    }

    public LQUser(String identifier, HashMap<String, Object> attributes) {
        this(identifier, attributes, true);
    }

    public LQUser(String identifier, HashMap<String, Object> attributes, boolean identified) {
        mIdentifier = identifier;
        mAttributes = attributes;
        this.setIdentified(identified);
        attributesCheck();

    }

    // Attributes
    public String getIdentifier() {
        return mIdentifier;
    }

    public String setIdentifierForPrefs(String identifier){
        mIdentifier = identifier;
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

    public void clearCustomAttributes() {
        setAttributes(new HashMap<String, Object>());
    }

    public HashMap<String, Object> getAttributes() {
        return new HashMap<String, Object>(mAttributes);
    }

    public void setAttributes(HashMap<String, Object> attributes) {
        mAttributes = attributes;
        attributesCheck();
    }

    public Object setAttribute(String key, Object attribute) {
        return mAttributes.put(key, attribute);
    }

    public Object attributeForKey(String key) {
        return mAttributes.get(key);
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



    public static LQModel fromJSON(JSONObject jsonObject) {
        try {
            String unique_id = jsonObject.getString("unique_id");
            boolean identified = jsonObject.getBoolean("identified");
            HashMap<String, Object> attrs = attributesFromJSON(jsonObject, RESERVED_KEYS);
            return new LQUser(unique_id, attrs, identified);
        } catch (JSONException e) {
            LQLog.infoVerbose("New user, identifying now...");
            return new LQUser(LQModel.newIdentifier(), false);
        }
    }

    protected void attributesCheck() {
        if(mAttributes == null) {
            mAttributes = new HashMap<String, Object>();
        }
    }

    @Override
    public void save(Context context, String path) {
        super.save(context, path + ".user");
    }

    public static LQUser load(Context context, String path) {
        return (LQUser) fromJSON(retriveFromFile(context, path));
    }

}
