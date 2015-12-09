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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import io.lqd.sdk.LQLog;
import io.lqd.sdk.Liquid;

public class LQClientManager {
    // Constants
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Member variables
    private GoogleCloudMessaging mGCM;
    private String mRegid;
    private String mSenderId;
    private Activity mActivity;

    public static abstract class RegistrationCompletedHandler {
        public abstract void onSuccess(String registrationId, boolean isNewRegistration);
        public void onFailure(String ex) {
            LQLog.error(ex);
        }
    }

    public LQClientManager(Activity activity, String senderid) {
        this.mActivity = activity;
        this.mSenderId = senderid;
        this.mGCM = GoogleCloudMessaging.getInstance(activity);
    }

    // Register if needed or fetch from shared prefs
    public void registerIfNeeded(final RegistrationCompletedHandler handler) {
        if (checkPlayServices()) {
            mRegid = getRegistrationId(getContext());

            if (mRegid.isEmpty()) {
                registerInBackground(handler);
            } else {
                LQLog.info("Got from cache: " + mRegid);
                handler.onSuccess(mRegid, false);
            }
        } else { // no play services
            LQLog.infoVerbose("No valid Google Play Services APK found.");
        }
    }

    // Registers the application with GCM servers asynchronously and stores the
    // registration ID and app versionCode in the application's shared prefs
    private void registerInBackground(final RegistrationCompletedHandler handler) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    if (mGCM == null) {
                        mGCM = GoogleCloudMessaging.getInstance(getContext());
                    }
                    InstanceID instanceID = InstanceID.getInstance(getContext());
                    String newIID = InstanceID.getInstance(getContext()).getId();
                    mRegid = instanceID.getToken(mSenderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    LQLog.info("Registration ID: " + mRegid);

                    // Persist the regID - no need to register again.
                    Liquid.getInstance().setGCMregistrationID(mRegid);
                    storeRegistrationId(getContext(), mRegid);

                } catch (IOException ex) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    handler.onFailure(ex.getMessage());
                }
                return mRegid;
            }

            @Override
            protected void onPostExecute(String regId) {
                if (regId != null) {
                    handler.onSuccess(regId, true);
                }
            }
        }.execute(null, null, null);
    }

    // Gets the registration ID from shared prefs
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            LQLog.infoVerbose("Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            LQLog.infoVerbose("App version changed.");
            return "";
        }
        return registrationId;
    }

    // Stores registration Id and app versionCode in the application's
    // shared prefs
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        LQLog.infoVerbose("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    // Returns application's version code
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getContext().getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
    }

    // Checks if the device has Google Play Services installed. If not display
    // a dialog to download the APK or enable it in system settings
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                LQLog.error("This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private Context getContext() {
        return mActivity.getApplicationContext();
    }

    private Activity getActivity() {
        return mActivity;
    }
}
