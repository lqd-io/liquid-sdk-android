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

package io.lqd.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LiquidTools {

    private static final TimeZone timezone = Calendar.getInstance().getTimeZone();

    public static boolean checkForPermission(String permission, Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            LQLog.warning( "Requesting permission " + permission + " but it is not on the AndroidManifest.xml");
            return false;
        }
    }

    protected static int getId(String resourceName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resourceName);
            return idField.getInt(idField);
        } catch (Exception e) {
            throw new RuntimeException("No resource ID found for: " + resourceName + " / " + c, e);
        }
    }

    protected static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager
        = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static String dateToString(Date date) {
        return ISO8601Utils.format(date, true, timezone);
    }

    public static Date stringToDate(String date) throws IllegalArgumentException {
        return ISO8601Utils.parse(date);
    }

    public static String colorToHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static void saveToDisk(Context c, String key, String value) {
        SharedPreferences sharedPrefs = c.getSharedPreferences("io.lqd."+ key, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putString("io.lqd."+ key, value);
        editor.commit();
    }

    public static String loadFromDisk(Context c, String key) {
        SharedPreferences sharedPrefs = c.getSharedPreferences("io.lqd."+ key, Context.MODE_PRIVATE);
        return sharedPrefs.getString("io.lqd."+ key, null);
    }

    public static void exceptionOrLog(boolean raiseException, String message) {
        if(raiseException) {
            throw new IllegalArgumentException(message);
        } else {
            LQLog.warning(message);
        }
    }

}
