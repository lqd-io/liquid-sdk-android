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
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import io.lqd.sdk.LQLog;
import io.lqd.sdk.LiquidTools;

public abstract class LQModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PREF_FILE_NAME = "LQPrefs";

    /**
     * Generate a random unique id
     * @return
     */
    public static String newIdentifier() {
        String uid = UUID.randomUUID().toString().toUpperCase(Locale.ENGLISH);
        String epoch = LiquidTools.tenCharEpoch(Calendar.getInstance().getTimeInMillis());
        return uid + "-" + epoch;
    }

    /*
     * *******************
     * Attributes Assertion
     * *******************
     */

    /**
     * Check if the key have invalid chars: { $ . \0 } are the invalid attributes
     * @param key key that will be checked
     * @param raiseException if true will raise IllegalArgumentException, otherwise will Log
     * @return true if the key is valid, false otherwise
     */
    public static boolean validKey(String key, boolean raiseException) {
        boolean isValid = (!key.contains("$") && !key.contains(".") && !key.contains("\0"));
        if(!isValid) {
            LiquidTools.exceptionOrLog(raiseException, "Key: (" + key + ") contains invalid chars: (. $ \\0)");
        }
        return isValid;
    }

    /**
     * Check if the attribute type is valid: {null, String, Number, Boolean, Date} are the valid attributes
     * @param attribute attribute that will be checked
     * @param raiseException if true will raise IllegalArgumentException, otherwise will Log
     * @return true if the attribute is valid, false otherwise
     */
    public static boolean validValue(Object attribute, boolean raiseException) {
        boolean isValid = ((attribute == null) || (attribute instanceof String) ||
                (attribute instanceof Number) || (attribute instanceof Boolean) ||
                (attribute instanceof Date));
        if(!isValid) {
            LiquidTools.exceptionOrLog(raiseException, "Value (" + attribute + ") with unsupported type. Supported: (String, Number, Boolean, Date)");
        }
        return isValid;
    }

    public static HashMap<String, Object> sanitizeAttributes(Map<String, Object> attributes, boolean raiseException) {
        if (attributes == null) {
            return null;
        }
        HashMap<String, Object> attrs = new HashMap<String, Object>();
        for (String key : attributes.keySet()) {
            if (LQModel.validKey(key, raiseException) && LQModel.validValue(attributes.get(key), raiseException)) {
                attrs.put(key, attributes.get(key));
            }
        }
        return attrs;
    }

    public void save(Context context, String path) {
        String serializedObject = toJSON().toString();

        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = preferences.edit();

        String[] split = path.split("\\.");

        prefsEditor.putString(split[1], serializedObject);

        LQLog.infoVerbose("Saving " + split[1] + " to shared prefs");
        prefsEditor.apply();
    }

    public static JSONObject retriveFromFile(Context context, String path) {
        String retrivedData;

        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        String[] split = path.split("\\.");

        retrivedData = preferences.getString(split[1], "");

        try {
            return new JSONObject(retrivedData);
        } catch (JSONException e) {
            LQLog.infoVerbose("Couldn't retrieve " + split[1] + " from file, probably new user");
        }
        return new JSONObject();
    }

    protected static HashMap<String, Object> attributesFromJSON(JSONObject object, String[] excludedKeys) {
        HashMap<String, Object> hashMap = new HashMap<>();

        if(object != JSONObject.NULL)
            hashMap = toHashMap(object, excludedKeys);
        return hashMap;
    }


    private static HashMap<String, Object> toHashMap(JSONObject object, String[] excludedKeys) {
        HashMap<String, Object> hashmap = new HashMap<>();

        Iterator<String> keysItr = object.keys();

        while(keysItr.hasNext()) {
            String key = keysItr.next();
            if (!Arrays.asList(excludedKeys).contains(key)) {
                Object value = null;
                try {
                    value = object.get(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (value instanceof JSONArray) {
                    value = toList((JSONArray) value, excludedKeys);
                } else if (value instanceof JSONObject) {
                    value = toHashMap((JSONObject) value, excludedKeys);
                }
                hashmap.put(key, value);
            }
        }
        return hashmap;
    }

    public static List<Object> toList(JSONArray array, String[] excludedKeys) {
        List<Object> list = new ArrayList<>();

        for(int i = 0; i < array.length(); i++) {
            Object value = null;
            try {
                value = array.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value, excludedKeys);
            }

            else if(value instanceof JSONObject) {
                value = toHashMap((JSONObject) value, excludedKeys);
            }
            list.add(value);
        }
        return list;
    }


    public abstract JSONObject toJSON();
}
