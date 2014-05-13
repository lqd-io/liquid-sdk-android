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

import java.util.HashMap;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest.permission;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

public class LQDevice {
	private String _vendor;
	private String _deviceModel;
	private String _systemVersion;
	private String _deviceName;
	private String _screenSize;
	private String _carrier;
	private String _internetConnectivity;
	private String _uid;
	private String _appBundle;
	private String _appVersion;
	private String _appName;
	private String _releaseVersion;
	private String _liquidVersion;
	private HashMap<String, Object> _attributes;

	private Context _context;

	// Initialization
	public LQDevice(Context context, String liquidVersion) {
		_context = context;
		_vendor = LQDevice.getDeviceVendor();
		_deviceModel = LQDevice.getDeviceModel();
		_systemVersion = LQDevice.getSystemVersion();
		_deviceName = LQDevice.getDeviceName();
		_screenSize = LQDevice.getScreenSize(context);
		_carrier = LQDevice.getCarrier(context);
		_internetConnectivity = LQDevice.getInternetConnectivity(context);
		_uid = LQDevice.getDeviceID(context);
		_appBundle = LQDevice.getAppBundle(context);
		_appName = LQDevice.getAppName(context);
		_appVersion = LQDevice.getAppVersion(context);
		_releaseVersion = LQDevice.getReleaseVersion(context);
		_liquidVersion = liquidVersion;
	}

	// Attributes
	public String getUID() {
		return _uid;
	}

	public void setAttribute(Object attribute, String key) {
		_attributes.put(key, attribute);
	}

	public void getAttribute(String key) {
		_attributes.get(key);
	}

	// JSON
	public JSONObject toJSON() {
		// Updating to avoid callbacks
		_internetConnectivity = LQDevice.getInternetConnectivity(_context);

		HashMap<String, Object> attrs = new HashMap<String, Object>();
		if(_attributes != null) {
			attrs.putAll(_attributes);
		}
		attrs.put("_vendor", _vendor);
		attrs.put("platform", "Android");
		attrs.put("_deviceModel", _deviceModel);
		try {
			attrs.put("_systemVersion", Integer.parseInt(_systemVersion));
		} catch (NumberFormatException e) {
			attrs.put("_systemVersion", _systemVersion);
		}
		attrs.put("_deviceName", _deviceName);
		attrs.put("_screenSize", _screenSize);
		attrs.put("_carrier", _carrier);
		attrs.put("_internetConnectivity", _internetConnectivity);
		attrs.put("unique_id", _uid);
		attrs.put("_appBundle", _appBundle);
		attrs.put("_appName", _appName);
		attrs.put("_appVersion", _appVersion);
		attrs.put("_releaseVersion", _releaseVersion);
		attrs.put("_liquidVersion", _liquidVersion);

		JSONObject json = new JSONObject();
		try {
			if(attrs != null){
				for(String key : attrs.keySet()){
					json.put(key, attrs.get(key));
				}
			}
			return json;
		} catch (JSONException e) {
			LQLog.error("LQDevice toJSON: " + e.getMessage());
		}
		return null;
	}

	// Device Info
	private static String getDeviceVendor() {
		return android.os.Build.MANUFACTURER;
	}

	private static String getDeviceModel() {
		return android.os.Build.MODEL;
	}

	private static String getSystemVersion() {
		return String.valueOf(android.os.Build.VERSION.SDK_INT);
	}

	@SuppressWarnings("deprecation")
	private static String getScreenSize(Context context) {
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		int width = display.getWidth(); // deprecated
		int height = display.getHeight(); // deprecated
		return width + "x" + height;
	}

	// cannot get device name on android
	private static String getDeviceName() {
		return "";
	}

	private static String getCarrier(Context context) {
		TelephonyManager telephonyManager = ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE));
		return telephonyManager.getNetworkOperatorName();
	}

	private static String getInternetConnectivity(Context context) {
		if (LiquidTools.checkForPermission(permission.ACCESS_NETWORK_STATE, context)) {
			ConnectivityManager connManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mNetwork = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mWifi.isConnected()) {
				return "WiFi";
			} else if (mNetwork.isConnected()) {
				return "Cellular";
			} else {
				return "No Connectivity";
			}
		}else{
			return "No ACCESS_NETWORK_STATE permission";
		}
	}

	public synchronized static String getDeviceID(Context context) {
		String uid;
		SharedPreferences sharedPrefs = context.getSharedPreferences(
				"io.lqd.UUID", Context.MODE_PRIVATE);
		uid = sharedPrefs.getString("io.lqd.UUID", null);
		if (uid == null) {
			uid = UUID.randomUUID().toString();
			Editor editor = sharedPrefs.edit();
			editor.putString("io.lqd.UUID", uid);
			editor.commit();
		}

		return uid.replace("-", "");
	}

	private static String getAppBundle(Context context) {
		return context.getPackageName();
	}

	private static String getAppName(Context context) {
		int stringId = context.getApplicationInfo().labelRes;
		return context.getString(stringId);
	}

	private static String getAppVersion(Context context) {
		try {
			PackageInfo pInfo;
			pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";

	}

	private static String getReleaseVersion(Context context) {
		try {
			PackageInfo pInfo;
			pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return String.valueOf(pInfo.versionCode);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";

	}
}
