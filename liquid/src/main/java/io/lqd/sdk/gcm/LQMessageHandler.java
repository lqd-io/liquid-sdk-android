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

package io.lqd.sdk.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import io.lqd.sdk.LQLog;

public class LQMessageHandler extends GcmListenerService {

    private static final String LIQUID_MESSAGE_EXTRA = "lqd_message";
    private static final String LIQUID_PUSH_ID_EXTRA = "lqd_id";
    private static final String LIQUID_SOUND_EXTRA = "lqd_sound";
    private static final String LIQUID_TITLE_EXTRA = "lqd_title";
    private static final String LIQUID_DEEPLINK_EXTRA = "lqd_deeplink";


    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(LIQUID_MESSAGE_EXTRA);
        int push_id = 0;
        try {
            push_id = Integer.parseInt(data.getString(LIQUID_PUSH_ID_EXTRA));
        } catch (NumberFormatException e) {
            LQLog.error("push_id is not an int: " + data.getString(LIQUID_PUSH_ID_EXTRA));
        }
        int icon = getAppIconInt(getApplicationContext());
        Uri sound = getPushSound(data, getApplicationContext());
        String title = getPushTitle(data, getApplicationContext());
        String deepLinkString = getDeepLink(data);

        Intent appIntent = getIntent(getApplicationContext());
        Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW);
        PendingIntent contentIntent;
        if (deepLinkString != null) {
            deepLinkIntent.setData(Uri.parse(deepLinkString));
            contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, deepLinkIntent, 0);
        } else {
            contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, appIntent, 0);
        }

        createNotification(getApplicationContext(), contentIntent, icon, push_id, title, message, sound);
    }

    // Creates notification based on title and body received
    private void createNotification(Context c, PendingIntent intent, int icon, int push_id, String title, String body, Uri sound) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), icon))
                .setTicker(body)
                .setContentText(body)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setAutoCancel(true)
                .setVibrate(new long[]{500, 500})
                .setContentIntent(intent);
        if (sound != null)
            builder.setSound(sound);
        nm.notify(push_id, builder.build());
    }

    private static int getAppIconInt(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            ApplicationInfo appinfo = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

            Bundle b = appinfo.metaData;
            if(b != null && b.getInt("io.lqd.sdk.notification_icon",0) > 0)
                return b.getInt("io.lqd.sdk.notification_icon");

            return appinfo.icon;
        } catch (PackageManager.NameNotFoundException e) {
            return android.R.drawable.sym_def_app_icon;
        }
    }

    private static Intent getIntent(Context context) {
        PackageManager manager = context.getPackageManager();
        return manager.getLaunchIntentForPackage(context.getPackageName());
    }

    public static boolean isLiquidPush(Intent intent) {
        return intent.getStringExtra("lqd_message") != null;
    }

    private static Uri getPushSound(Bundle data, Context context) {
        String sound = data.getString(LIQUID_SOUND_EXTRA);
        if(sound == null)
            return null;
        if("default".equals(sound))
            return Settings.System.DEFAULT_NOTIFICATION_URI;
        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + sound);
    }

    private static String getPushTitle(Bundle data, Context context) {
        String title = data.getString(LIQUID_TITLE_EXTRA);
        if(title == null)
            return getAppName(context);
        return title;
    }

    private static String getAppName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            ApplicationInfo appinfo = manager.getApplicationInfo(context.getPackageName(), 0);
            return manager.getApplicationLabel(appinfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private static String getDeepLink(Bundle data) {
        return data.getString(LIQUID_DEEPLINK_EXTRA);
    }

}
