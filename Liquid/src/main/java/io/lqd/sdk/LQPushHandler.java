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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

/**
 * BroadcastReceiver that handles GCM intents.
 * 
 *
 */
public class LQPushHandler extends BroadcastReceiver {

	private static final String RECVD_REGID_FROM_GOOGLE = "com.google.android.c2dm.intent.REGISTRATION";
	private static final String RECVD_C2DM_MSG_FROM_GOOGLE = "com.google.android.c2dm.intent.RECEIVE";
	private static final String SEND_REGISTRATION_TO_GOOGLE = "com.google.android.c2dm.intent.REGISTER";
	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String LIQUID_MESSAGE_EXTRA = "lqd_message";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(RECVD_REGID_FROM_GOOGLE)) {
			handleRegistration(intent);
		} else if (action.equals(RECVD_C2DM_MSG_FROM_GOOGLE)) {
			handleNotification(context, intent);
		}
	}

	private void handleNotification(Context context, Intent intent) {
		String message = intent.getStringExtra(LIQUID_MESSAGE_EXTRA);
		int icon = getAppIconInt(context);
		String title = getAppName(context);
		Intent appIntent = getIntent(context);
		PendingIntent contentIntent = PendingIntent.getActivity(
				context.getApplicationContext(),
				0,
				appIntent,
				PendingIntent.FLAG_UPDATE_CURRENT
				);
		if(Build.VERSION.SDK_INT < 11) {
			sendNotification8(context, contentIntent, icon, title, message);
		} else {
			sendNotification11(context, contentIntent, icon, title, message);
		}

	}

	private void handleRegistration(Intent intent) {
		String registrationID = intent.getStringExtra(PROPERTY_REG_ID);
		if(registrationID != null && registrationID.length() > 0) {
			LQLog.infoVerbose("Push registration id received: " + registrationID);
			Liquid.getInstance().setGCMregistrationID(registrationID);
		}
	}


	static void registerDevice(Context context, String senderID) {
		Intent registrationIntent = new Intent(SEND_REGISTRATION_TO_GOOGLE);
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
		registrationIntent.putExtra("sender", senderID);
		context.startService(registrationIntent);
	}

	@SuppressWarnings("deprecation")
	@TargetApi(16)
	private void sendNotification11(Context c, PendingIntent intent, int icon, String title, String body) {
		NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(c);
		builder.setSmallIcon(icon);
		builder.setTicker(body);
		builder.setContentText(body);
		builder.setWhen(System.currentTimeMillis());
		builder.setContentTitle(title);
		builder.setContentIntent(intent);
		Notification n;
		if (Build.VERSION.SDK_INT < 16) {
			n = builder.getNotification();
		} else {
			n = builder.build();
		}
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(0, n);
	}

	@SuppressWarnings("deprecation")
	@TargetApi(8)
	private void sendNotification8(Context c, PendingIntent intent, int icon, String title, String body) {
		NotificationManager nm = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(icon, body, System.currentTimeMillis());
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.setLatestEventInfo(c, title, body, intent);
		nm.notify(0, n);
	}


	private static String getAppName(Context context) {
		PackageManager manager = context.getPackageManager();
		try {
			ApplicationInfo appinfo = manager.getApplicationInfo(context.getPackageName(), 0);
			return manager.getApplicationLabel(appinfo).toString();
		} catch (NameNotFoundException e) {
			return "";
		}
	}

	private static int getAppIconInt(Context context) {
		PackageManager manager = context.getPackageManager();
		try {
			ApplicationInfo appinfo = manager.getApplicationInfo(context.getPackageName(), 0);
			return appinfo.icon;
		} catch (NameNotFoundException e) {
			return android.R.drawable.sym_def_app_icon;
		}
	}

	private static Intent getIntent(Context context) {
		PackageManager manager = context.getPackageManager();
		return manager.getLaunchIntentForPackage(context.getPackageName());
	}

	public static boolean isLiquidPush(Intent intent) {
		return intent.getStringExtra(LIQUID_MESSAGE_EXTRA) != null;
	}

}
