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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import io.lqd.sdk.LQLog;
import io.lqd.sdk.LiquidTools;

public abstract class LQModel implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String PREF_FILE_NAME = "LQPrefsFile";
    private static JSONObject tempJsonObject;
    private static LQUser lqUserObject;
    private static String[] keys = { "mIdentifier", "mUniqueId" };


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


    /*
     * *******************
     * File Management
     * *******************
     */

    protected void save(Context context, String path) {
        LQLog.data("Saving " + this.getClass().getSimpleName());
        save(context, path, this);
    }

    protected static void save(Context context, String path, Object o) {

        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsEditor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(o);
        if (path.contains(".queue"))
            prefsEditor.putString("Queue", json);
        else
            prefsEditor.putString("User", json);
        LQLog.infoVerbose("Saving " + path + " to shared prefs");
        prefsEditor.apply();
    }

    protected static Object loadObject(Context context, String path) {
        LQLog.infoVerbose("Loading " + path + " from shared prefs");

        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        Type type = new TypeToken<ArrayList<LQNetworkRequest>>() {}.getType();
        Gson gson = new Gson();

        if (path.contains(".user")) {
            String jsonUserString = preferences.getString("User", "");

            if ("".equals(jsonUserString)){
                LQLog.infoVerbose("New user, requesting new identifier.");
                jsonUserString = gson.toJson(new LQUser(newIdentifier(), false));
            }
            try {
                tempJsonObject = new JSONObject(jsonUserString);
            } catch (JSONException e) {
                LQLog.error("Couldn't assign temp json " + e.getMessage());
                e.printStackTrace();
            }

            try {
                lqUserObject = gson.fromJson(jsonUserString, LQUser.class);
            } catch (Exception e) {
                LQLog.infoVerbose("Error from gson" + e.getMessage() + ". Clearing preferences file.");
                preferences.edit().clear().commit();
                return null;
            }
            getIdentifierKeyFromPrefs();
            return lqUserObject;
        }
        else {
            String json = preferences.getString("Queue", "");
            return gson.fromJson(json, type);
        }
    }

    protected static void getIdentifierKeyFromPrefs(){
        boolean found = false;
        int i = 0;

        while (i < keys.length && !found) {
            try {
                lqUserObject.setIdentifierForPrefs(tempJsonObject.getString(keys[i]));
                found = true;
                LQLog.info("Found the key: " + keys[i] + " in the preferences saved file");
            } catch (JSONException e) {
                LQLog.info("Did not found the key: " + keys[i] + " in the preferences, trying the next one.");
            }
            ++i;
        }
    }

    protected static LQModel load(Context context, String path) {
        return (LQModel) loadObject(context, path);
    }
}
