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

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.lqd.sdk.gcm.LQClientManager;
import io.lqd.sdk.model.LQDataPoint;
import io.lqd.sdk.model.LQDevice;
import io.lqd.sdk.model.LQEvent;
import io.lqd.sdk.model.LQInAppMessage;
import io.lqd.sdk.model.LQLiquidPackage;
import io.lqd.sdk.model.LQModel;
import io.lqd.sdk.model.LQNetworkRequest;
import io.lqd.sdk.model.LQUser;
import io.lqd.sdk.model.LQValue;
import io.lqd.sdk.model.LQVariable;
import io.lqd.sdk.visual.InappMessage;
import io.lqd.sdk.visual.Modal;
import io.lqd.sdk.visual.SlideUp;

public class Liquid {

    protected static final String TAG_LIQUID = "LIQUID";

    public static final String LIQUID_VERSION = "2.1.0";
    private static final int LIQUID_DEFAULT_SESSION_TIMEOUT = 30;

    private int mSessionTimeout;
    private String mApiToken;
    private LQUser mCurrentUser;
    private LQUser mPreviousUser;
    private LQDevice mDevice;
    protected ExecutorService mQueue;
    private boolean mAutoLoadValues;
    private Context mContext;
    private static Liquid mInstance;
    private LQLiquidPackage mLoadedLiquidPackage;
    private HashMap<String, LQValue> mAppliedValues = new HashMap<String, LQValue>();
    private HashMap<String, Activity> mAttachedActivities = new HashMap<String, Activity>();
    private HashMap<String, LiquidOnEventListener> mListeners = new HashMap<String, LiquidOnEventListener>();
    private ArrayList<String> mBundleVariablesSended;
    private boolean mNeedCallbackCall = false;
    private LQQueuer mHttpQueuer;
    private boolean isDevelopmentMode;
    private Activity mCurrentActivity;
    private LinkedList<InappMessage> mInAppMessagesQueue;
    private boolean isStarted;


    /**
     * Retrieves the Liquid shared instance.
     * <p>
     * You can use this method across all your activities.
     * </p>
     *
     * @throws IllegalStateException
     *             if you didn't call initialize() previously.
     *
     * @return A Liquid instance.
     */
    public static Liquid getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("Can't call getInstance() before initialize(context,apiToken)");
        }
        return mInstance;
    }

    /**
     * Call this method to initialize Liquid.
     *
     * @param context
     *            The Android context of your application.
     * @param apiToken
     *            The Liquid ApiToken of your app.
     *
     * @return A Liquid instance.
     */
    public static Liquid initialize(Context context, String apiToken) {
        if (!isInitialized()) {
            mInstance = new Liquid(context, apiToken, false);
        }
        mInstance.mContext = context;
        return mInstance;
    }

    /**
     * Call this method to initialize Liquid.
     *
     * @param context
     *            The Android context of your application.
     * @param apiToken
     *            The Liquid ApiToken of your app.
     * @param developmentMode
     *            The flag to send to Liquid server the variables used in
     *            methods with <b>fallbackVariable</b> param.
     *
     * @return The Liquid instance.
     */
    public static Liquid initialize(Context context, String apiToken, boolean developmentMode) {
        if (!isInitialized()) {
            mInstance = new Liquid(context, apiToken, developmentMode);
        }
        mInstance.mContext = context;
        return mInstance;
    }

    public static boolean isInitialized() {
        return mInstance != null;
    }

    private Liquid(Context context, String apiToken, boolean developmentMode) {
        LiquidTools.checkForPermission(permission.INTERNET, context);
        if (apiToken == null || apiToken.length() == 0) {
            throw new IllegalArgumentException("Your API Token is invalid: \'" + apiToken + "\'.");
        }
        mContext = context;
        if (Build.VERSION.SDK_INT >= 14) {
            attachActivityCallbacks();
        }
        mSessionTimeout = LIQUID_DEFAULT_SESSION_TIMEOUT;
        mApiToken = apiToken;
        mDevice = new LQDevice(context, LIQUID_VERSION);
        mQueue = Executors.newSingleThreadExecutor();
        loadLiquidPackage(true);
        mInAppMessagesQueue = new LinkedList<>();
        mHttpQueuer = LQQueuer.load(mContext, mApiToken);
        mHttpQueuer.setLiquidInstance(this);
        mHttpQueuer.startFlushTimer();
        isDevelopmentMode = developmentMode;
        isStarted = false;
        if(isDevelopmentMode)
            mBundleVariablesSended = new ArrayList<String>();

        // Get last user and init session
        mPreviousUser = LQUser.load(mContext, mApiToken + ".user");
        identifyUser(mPreviousUser.getIdentifier(), mPreviousUser.getAttributes(), mPreviousUser.isIdentified(), false);

        LQLog.info("Initialized Liquid with API Token " + apiToken);
    }

    /*
     * *******************
     * Setters and Getters
     * *******************
     */

    /**
     * Attach a listener to be notified of Liquid Events
     * {@link LiquidOnEventListener}
     *
     * @see LiquidOnEventListener
     *
     * @param l Listener to be attached.
     */
    public void attachLiquidEventListener(LiquidOnEventListener l) {
        mListeners.put(l.getClass().getName(), l);
    }

    /**
     * Detach a listener to stop being notified by Liquid Events
     * {@link LiquidOnEventListener}
     *
     * @see LiquidOnEventListener
     *
     * @param l Listener to be detached.
     */
    public void detachLiquidEventListener(LiquidOnEventListener l) {
        mListeners.remove(l.getClass().getName());
    }

    private void attachActivity(Activity activity) {
        mAttachedActivities.put(activity.getClass().getName(), activity);
        if(LiquidOnEventListener.class.isInstance(activity)) {
            attachLiquidEventListener((LiquidOnEventListener) activity);
        }
    }

    private void detachActivity(Activity activity) {
        mAttachedActivities.remove(activity.getClass().getName());
        if(LiquidOnEventListener.class.isInstance(activity)) {
            detachLiquidEventListener((LiquidOnEventListener) activity);
        }
    }


    /**
     * Returns whether or not the {@link LiquidOnEventListener#onValuesLoaded()}
     * will be called after {@link LiquidOnEventListener#onValuesReceived()}.
     *
     * @see LiquidOnEventListener
     * @return true if is auto loading variables, otherwise false.
     */
    public boolean willAutoloadVariables() {
        return mAutoLoadValues;
    }

    /**
     * By default Liquid will not auto load variables.
     *
     * Set Liquid behavior to auto load variables.
     *
     * @param autoloadVariables
     *            whether or not Liquid will auto load the variables.
     */
    public void setAutoLoadVariables(boolean autoloadVariables) {
        mAutoLoadValues = autoloadVariables;
    }

    /**
     * Get the timeout value that Liquid is using to close automatically a
     * session.
     *
     * @return In seconds the value of the timeout.
     */
    public int getSessionTimeout() {
        return mSessionTimeout;
    }

    /**
     * Set the timeout value that Liquid will use to close automatically a
     * session.
     *
     * @param sessionTimeout
     *            value in seconds of the timeout
     */
    public void setSessionTimeout(int sessionTimeout) {
        mSessionTimeout = sessionTimeout;
    }

    /**
     * Set the flush interval
     *
     * @param flushInterval
     *            value in seconds.
     */
    public void setFlushInterval(int flushInterval) {
        mHttpQueuer.setFlushTimer(flushInterval);
    }

    /**
     * Get the flush interval
     *
     * @return value in seconds of the flush interval.
     */
    public int getFlushInterval() {
        return mHttpQueuer.getFlushTimer();
    }

    /*
     * *******************
     * User Interaction
     * *******************
     */

    public void setupPushNotifications(Activity activity, final String senderID) {
        LQLog.infoVerbose("Requesting device push token");
        LQClientManager pushClientManager = new LQClientManager(activity, senderID);

        pushClientManager.registerIfNeeded(new LQClientManager.RegistrationCompletedHandler() {

            @Override
            public void onSuccess(String registrationId, boolean isNewRegistration) {
                Liquid.getInstance().setGCMregistrationID(registrationId);
            }

            @Override
            public void onFailure(String ex) {
                super.onFailure(ex);
            }
        });
    }

    public void alias() {
        final String oldID = mPreviousUser.getIdentifier();
        final String newID = mCurrentUser.getIdentifier();
        if (mPreviousUser.isIdentified()) {
            LQLog.warning("Can't alias (" + oldID + "): Isn't an anonymous user.");
            return;
        }
        LQLog.infoVerbose("Making alias between (" + oldID + ") and (" + newID + ").");
        mQueue.execute(new Runnable() {
            @Override
            public void run() {
                mHttpQueuer.addToHttpQueue(LQRequestFactory.createAliasRequest(oldID, newID));
            }
        });
    }

    /**
     * Create a new User with a new UUID if the user isn't an identified one
     */
    public void resetUser() {
        if(mCurrentUser.isIdentified()) {
            identifyUser(LQModel.newIdentifier(), null, false, false);
        } else {
            mCurrentUser.clearCustomAttributes();
            mCurrentUser.save(mContext, mApiToken);
        }
    }

    /**
     * Identifies the current user with a custom UUID.
     *
     * @param identifier
     *            Custom UUID.
     */
    public void identifyUser(String identifier) {
        identifyUser(identifier, null, true, true);
    }

    /**
     * Identifies the current user with a custom UUID.
     *
     * @param identifier
     * @param alias
     *            if true, will make an alias with previous user if previous
     *            user is anonymous.
     */
    public void identifyUser(String identifier, boolean alias) {
        identifyUser(identifier, null, true, alias);
    }

    /**
     * Identifies the current user with a custom UUID and additional attributes.
     *
     * @param identifier
     *            The custom UUID.
     * @param attributes
     *            Additional user attributes.
     */
    public void identifyUser(String identifier, Map<String, Object> attributes) {
        identifyUser(identifier, attributes, true, true);
    }

    /**
     * Identifies the current user with a custom UUID and additional attributes.
     *
     * @param identifier
     *            The custom UUID.
     * @param attributes
     *            Additional user attributes.
     * @param alias
     *            if true, will make an alias with previous user if previous
     *            user is anonymous.
     */
    public void identifyUser(String identifier, Map<String, Object> attributes, boolean alias) {
        identifyUser(identifier, attributes, true, alias);
    }


    private void identifyUser(final String identifier, Map<String, Object> attributes, boolean identified, boolean alias) {
        final HashMap<String, Object> finalAttributes = LQModel.sanitizeAttributes(attributes, isDevelopmentMode);

        // invalid identifier, keeps the current user
        if (identifier == null || identifier.isEmpty()) {
            return;
        }

        // same id -> just update attributes
        if (mCurrentUser != null && mCurrentUser.getIdentifier().equals(identifier)) {
            mCurrentUser.setAttributes(finalAttributes);
            mCurrentUser.save(mContext, mApiToken);
            LQLog.infoVerbose("Already identified with user " + identifier + ". Not identifying again.");
            return;
        }

        mPreviousUser = mCurrentUser;
        mCurrentUser = new LQUser(identifier, finalAttributes, identified);
        requestValues();
        mCurrentUser.save(mContext, mApiToken);

        if (alias) {
            alias();
        }

        LQLog.info("From now on we're identifying the User by the identifier '" + identifier + "'");
    }

    /**
     * Get the user UUID
     *
     * @return the user UUID, null if the user isn't identified.
     */
    public String getUserIdentifier() {
        if (mCurrentUser == null) {
            return null;
        }
        return mCurrentUser.getIdentifier();
    }

    /**
     * Get the device UUID
     *
     * @return the device UUID in String format
     */
    public String getDeviceIdentifier() {
        return LQDevice.getDeviceID(mContext);
    }

    /**
     * Add or update an additional attribute to the user.
     *
     * @param key
     *            Attribute key
     * @param attribute
     *            Attribute value
     */
    public void setUserAttribute(String key, Object attribute) {
        if (LQModel.validKey(key, isDevelopmentMode)) {
            final String finalKey = key;
            final Object finalAttribute = attribute;
            mQueue.execute(new Runnable() {
                @Override
                public void run() {
                    mCurrentUser.setAttribute(finalKey, finalAttribute);
                    mCurrentUser.save(mContext, mApiToken);
                }
            });
        }
    }

    public void setUserAttributes(final Map<String, Object> attributes) {
        mQueue.execute(new Runnable() {
            @Override
            public void run() {
                for (String key : attributes.keySet()) {
                    if (LQModel.validKey(key, isDevelopmentMode)) {
                        mCurrentUser.setAttribute(key, attributes.get(key));
                    }
                }
                mCurrentUser.save(mContext, mApiToken);
            }
        });
    }

    /**
     * Add or update the current location.
     *
     * @param location
     *            Current location.
     */
    public void setCurrentLocation(final Location location) {
        mQueue.execute(new Runnable() {
            @Override
            public void run() {
                mDevice.setLocation(location);
            }
        });
    }

    /**
     * Add or update the GCM registration ID
     *
     * @param id
     *            GCM identifier
     */
    public void setGCMregistrationID(final String id) {
        mQueue.execute(new Runnable() {
            @Override
            public void run() {
                mDevice.setPushId(id);
            }
        });
    }

    /**
     * Remove the GCM registration ID
     */
    public void removeGCMregistrationID() {
        mQueue.execute(new Runnable() {
            @Override
            public void run() {
                mDevice.setPushId(null);
            }
        });

    }

    /**
     * Track an event.
     *
     * <p>
     * If the <b>eventName</b> is a empty string or null, the event will be
     * tracked with name <b>unnamedEvent</b>
     * </p>
     *
     * @param eventName
     *            Name of the event.
     */
    public void track(String eventName) {
        if (LQEvent.hasValidName(eventName, isDevelopmentMode)) {
            track(eventName, null, UniqueTime.newDate());
        } else {
            LQLog.warning("Event can't begin with \' _ \' character ");
        }
    }

    /**
     * Track an event.
     *
     * <p>
     * If the <b>eventName</b> is a empty string or null, the event will be
     * tracked with name <b>unnamedEvent</b>
     * </p>
     *
     * @param eventName
     *            Name of the event.
     * @param attributes
     *            Additional attributes of the event.
     */
    public void track(String eventName, Map<String, Object> attributes) {
        if (LQEvent.hasValidName(eventName, isDevelopmentMode)) {
            track(eventName, attributes, UniqueTime.newDate());
        }
    }

    /**
     * Track the dismissed action of the In-APP message
     *
     * @param inAppMessage
     *              The message itself.
     */
    public void trackDismiss(final LQInAppMessage inAppMessage) {
        mQueue.execute(new Runnable() {
            @Override
            public void run() {
               mHttpQueuer.addToHttpQueue(LQRequestFactory.inappMessagesReportRequest(mCurrentUser.getIdentifier(), inAppMessage.getFormulaId()));
            }
        });
        track(inAppMessage.getDismissEventName(), inAppMessage.getDismissAttributes(), UniqueTime.newDate());
    }

    /**
     * Track the button click action.
     *
     * @param inAppMessageCta
     *              The button itself.
     */
    public void trackCta(final LQInAppMessage.Cta inAppMessageCta){
        mQueue.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("cta_id", inAppMessageCta.getCtasAttributes().get("cta_id"));

                    mHttpQueuer.addToHttpQueue(LQRequestFactory.inappMessagesReportRequest(mCurrentUser.getIdentifier(), (String) inAppMessageCta.getCtasAttributes().get("formula_id"), payload));
                } catch (JSONException e) {

                }
            }
        });
        track(inAppMessageCta.getCtasEventName(), inAppMessageCta.getCtasAttributes(), UniqueTime.newDate());
    }

    private void track(String eventName, Map<String, Object> attributes, Date date) {
        LQEvent event = new LQEvent(eventName, LQModel.sanitizeAttributes(attributes, isDevelopmentMode), date);

        LQLog.infoVerbose("Tracking: " + event.getName());

        final String datapoint = new LQDataPoint(mCurrentUser, mDevice, event, mLoadedLiquidPackage.getValues(), date).toJSON().toString();
        LQLog.data(datapoint);

        mQueue.execute(new Runnable() {
            @Override
            public void run() {
                mHttpQueuer.addToHttpQueue(LQRequestFactory.createDataPointRequest(datapoint));
            }
        });
    }

    /*
     * *******************
     * Activity Lifecycle
     * *******************
     */

    /**
     * Override this method to the Activity onResume() You only need to do this
     * if your android minSDK is < 14
     *
     * @param activity
     *            the resumed activity
     */
    public void activityResumed(Activity activity) {
        if (Build.VERSION.SDK_INT < 14) {
            activityResumedCallback(activity);
        }
    }

    /**
     * Override this method to the Activity onPaused() You only need to do this
     * if your android minSDK is < 14
     *
     * @param activity
     *            the paused activity
     */
    public void activityPaused(Activity activity) {
        if (Build.VERSION.SDK_INT < 14) {
            activityPausedCallback(activity);
        }
    }

    /**
     * Override this method to the Activity onStopped() You only need to do this
     * if your android minSDK is < 14
     *
     * @param activity
     *            the stopped activity
     */
    public void activityStopped(Activity activity) {
        if (Build.VERSION.SDK_INT < 14) {
            activityStopedCallback(activity);
        }
    }

    /**
     * Override this method to the Activity onStart() You only need to do this
     * if your android minSDK is < 14
     *
     * @param activity
     *            the started activity
     */
    public void activityStarted(Activity activity) {
        if (Build.VERSION.SDK_INT < 14) {
            activityStartedCallback(activity);
        }
    }

    @SuppressLint("NewApi")
    private boolean isApplicationInBackground(Activity activity) {
        boolean configurationChanged;
        if(Build.VERSION.SDK_INT < 11) {
            int changingConfigs = activity.getChangingConfigurations();
            configurationChanged = (changingConfigs == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL || changingConfigs == Configuration.SCREENLAYOUT_LAYOUTDIR_LTR);
        } else {
            configurationChanged = activity.isChangingConfigurations();
        }
        return mAttachedActivities.size() == 0 && !configurationChanged;
    }

    /**
     * Override this method to the Activity onDestroy() You only need to do this
     * if your android minSDK is < 14
     *
     * @param activity
     *            the destroyed activity
     */
    public void activityDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT < 14) {
            activityDestroyedCallback(activity);
        }
    }

    public void activityCreated(Activity activity) {
        if (Build.VERSION.SDK_INT < 14) {
            activityCreatedCallback(activity);
        }
    }

    private void activityDestroyedCallback(Activity activity) {
        //mCurrentActivity = activity;
    }

    private void activityCreatedCallback(Activity activity) {
        mCurrentActivity = activity;

    }

    private void activityStopedCallback(Activity activity) {
        // mCurrentActivity = activity;

        if (isApplicationInBackground(activity)) {
            track("app background", null, UniqueTime.newDate());
            flush();
            requestValues();
            isStarted = false;
        }
    }

    private void activityStartedCallback(Activity activity) {
        mCurrentActivity = activity;

        mInstance.attachActivity(activity);
        if (mNeedCallbackCall) {
            mNeedCallbackCall = false;
            notifyListeners(false);
        }
    }

    private void activityResumedCallback(Activity activity) {
        mCurrentActivity = activity;



        mInstance.attachActivity(activity);

        if(!isApplicationInBackground(activity) && !isStarted) {
            track("app foreground", null, UniqueTime.newDate());
            isStarted = true;

            showInAppMessages();
        }

        mHttpQueuer.startFlushTimer();
    }

    private void activityPausedCallback(Activity activity) {
        mCurrentActivity = activity;

        mInstance.detachActivity(activity);
        mHttpQueuer.stopFlushTimer();
    }



    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void attachActivityCallbacks() {
        final Application app = (Application) mContext.getApplicationContext();
        app.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityStopped(Activity activity) {
                activityStopedCallback(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                activityStartedCallback(activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                activityResumedCallback(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                activityPausedCallback(activity);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                activityDestroyedCallback(activity);
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                activityCreatedCallback(activity);
            }
        });
    }

    /**
     * Request values from the server.
     */
    public void requestValues() {
        if ((mCurrentUser != null) && (mDevice != null)) {
            mQueue.execute(new Runnable() {
                @Override
                public void run() {
                    LQNetworkRequest req = LQRequestFactory.requestLiquidPackageRequest(mCurrentUser.getIdentifier(), mDevice.getUID());
                    String dataFromServer = req.sendRequest(mApiToken).getRequestResponse();
                    if (dataFromServer != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(dataFromServer);
                            LQLiquidPackage liquidPackage = new LQLiquidPackage(jsonObject);
                            LQLog.http(jsonObject.toString());
                            liquidPackage.saveToDisk(mContext);
                        } catch (JSONException e) {
                            LQLog.error("Could not parse JSON (Liquid Variables):" + dataFromServer);
                        }
                        notifyListeners(true);
                        if (mAutoLoadValues) {
                            loadLiquidPackage(false);
                        }
                    }
                }

            });
        }
    }

    public void addInapp(LQInAppMessage inapp) {
        if(mCurrentActivity == null)
            return;
        if (inapp.getLayout().equals("modal")) {
            mInAppMessagesQueue.add(new Modal(mContext, mCurrentActivity.findViewById(android.R.id.content).getRootView(), inapp));
        } else if (inapp.getLayout().equals("slide_up")) {
            mInAppMessagesQueue.add(new SlideUp(mContext, mCurrentActivity.findViewById(android.R.id.content).getRootView(), inapp));
        }
    }

    public void showInAppMessages(){
        mQueue.execute(new Runnable() {
            @Override
            public void run() {
                InappMessage in_app = mInAppMessagesQueue.poll();
                if (in_app == null) {
                    LQLog.infoVerbose("Not anymore inapp messages in the queue");
                } else {
                    in_app.show();
                }
            }
        });
    }


    private void notifyListeners(final boolean received) {
        Handler mainHandler = new Handler(mContext.getMainLooper());
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mListeners.size() == 0) {
                    mNeedCallbackCall = true;
                    return;
                } else {
                    mNeedCallbackCall = false;
                }
                for (LiquidOnEventListener listener : mListeners.values()) {
                    if (received) {
                        listener.onValuesReceived();
                    } else {
                        listener.onValuesLoaded();
                    }
                }
            }
        });
    }

    /**
     * Load a values retrieved previously from the server.
     */
    public void loadValues() {
        loadLiquidPackage(false);
    }

    private void loadLiquidPackage(boolean runInCurrentThread) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mLoadedLiquidPackage = LQLiquidPackage.loadFromDisk(mContext);
                mAppliedValues = LQValue.convertToHashMap(mLoadedLiquidPackage.getValues());
                notifyListeners(false);
            }
        };
        if (runInCurrentThread) {
            runnable.run();
        } else {
            mQueue.execute(runnable);
        }
    }

    /*
     * *******************
     * Getters for liquid Package
     * *******************
     */

    /**
     * Get a variable value.
     *
     * @param variableKey
     *            Variable Key of the Value.
     * @param fallbackValue
     *            is the value returned if the value for variableKey doesn't
     *            exist in Liquid instance.
     * @return The value in Liquid instance or fallbackValue if the variable
     *         don't exist.
     */
    public Date getDateVariable(String variableKey, Date fallbackValue) {
        if (isDevelopmentMode) {
            sendBundleVariable(LQVariable.buildJsonObject(variableKey, fallbackValue, LQVariable.DATE_TYPE));
        }
        if (!mAppliedValues.containsKey(variableKey)) {
            return fallbackValue;
        }
        if (mAppliedValues.get(variableKey).getDataType().equals(LQVariable.DATE_TYPE)) {
            try {
                Object value = mAppliedValues.get(variableKey).getValue();
                return value == null ? null : LiquidTools.stringToDate((String) value);
            } catch (IllegalArgumentException e) {
                LQLog.error("Error parsing Date with key: \"" + variableKey + "\"");
            }
        }
        invalidateVariables(variableKey);
        return fallbackValue;
    }

    /**
     * Get a variable value.
     *
     * @param variableKey
     *            Variable Key of the Value.
     * @param fallbackValue
     *            is the value returned if the value for variableKey doesn't
     *            exist in Liquid instance.
     * @return The value in Liquid instance or fallbackValue if the variable
     *         don't exist.
     */
    public int getColorVariable(String variableKey, int fallbackValue) {
        if (isDevelopmentMode) {
            sendBundleVariable(LQVariable.buildJsonObject(variableKey, LiquidTools.colorToHex(fallbackValue), LQVariable.COLOR_TYPE));
        }
        if (!mAppliedValues.containsKey(variableKey)) {
            return fallbackValue;
        }
        if (mAppliedValues.get(variableKey).getDataType()
                .equals(LQVariable.COLOR_TYPE)) {
            try {
                return Color.parseColor(mAppliedValues.get(variableKey).getValue().toString());
            } catch (IllegalArgumentException e) {
                LQLog.error("Error parsing Color with key: \"" + variableKey + "\"");
            }
        }
        invalidateVariables(variableKey);
        return fallbackValue;
    }

    /**
     * Get a variable value.
     *
     * @param variableKey
     *            Variable Key of the Value.
     * @param fallbackValue
     *            is the value returned if the value for variableKey doesn't
     *            exist in Liquid instance.
     * @return The value in Liquid instance or fallbackValue if the variable
     *         don't exist.
     */
    public String getStringVariable(String variableKey, String fallbackValue) {
        if (isDevelopmentMode) {
            sendBundleVariable(LQVariable.buildJsonObject(variableKey, fallbackValue, LQVariable.STRING_TYPE));
        }
        if (!mAppliedValues.containsKey(variableKey)) {
            return fallbackValue;
        }
        if (mAppliedValues.get(variableKey).getDataType().equals(LQVariable.STRING_TYPE)) {
            Object value = mAppliedValues.get(variableKey).getValue();
            return value == null ? null : value.toString();
        }
        invalidateVariables(variableKey);
        return fallbackValue;
    }

    /**
     * Get a variable value.
     *
     * @param variableKey
     *            Variable Key of the Value.
     * @param fallbackValue
     *            is the value returned if the value for variableKey doesn't
     *            exist in Liquid instance.
     * @return The value in Liquid instance or fallbackValue if the variable
     *         don't exist.
     */
    public int getIntVariable(String variableKey, int fallbackValue) {
        if (isDevelopmentMode) {
            sendBundleVariable(LQVariable.buildJsonObject(variableKey,fallbackValue, LQVariable.INT_TYPE));
        }
        if (!mAppliedValues.containsKey(variableKey)) {
            return fallbackValue;
        }
        if (mAppliedValues.get(variableKey).getDataType().equals(LQVariable.INT_TYPE)) {
            try {
                return Integer.parseInt(mAppliedValues.get(variableKey).getValue().toString());
            } catch (NumberFormatException e) {
                LQLog.error("Error parsing Integer with key: \"" + variableKey + "\"");
            }
        }
        invalidateVariables(variableKey);
        return fallbackValue;
    }

    /**
     * Get a variable value.
     *
     * @param variableKey
     *            Variable Key of the Value.
     * @param fallbackValue
     *            is the value returned if the value for variableKey doesn't
     *            exist in Liquid instance.
     * @return The value in Liquid instance or fallbackValue if the variable
     *         don't exist.
     */
    public float getFloatVariable(String variableKey, float fallbackValue) {
        if (isDevelopmentMode) {
            sendBundleVariable(LQVariable.buildJsonObject(variableKey, fallbackValue, LQVariable.FLOAT_TYPE));
        }
        if (!mAppliedValues.containsKey(variableKey)) {
            return fallbackValue;
        }
        if (mAppliedValues.get(variableKey).getDataType().equals(LQVariable.FLOAT_TYPE)) {
            try {
                return Float.parseFloat(mAppliedValues.get(variableKey).getValue().toString());
            } catch (NumberFormatException e) {
                LQLog.error("Error parsing Float with key: \"" + variableKey + "\"");
            }
        }
        invalidateVariables(variableKey);
        return fallbackValue;
    }

    public boolean getBooleanVariable(String variableKey, boolean fallbackValue) {
        if (isDevelopmentMode) {
            sendBundleVariable(LQVariable.buildJsonObject(variableKey, fallbackValue, LQVariable.BOOLEAN_TYPE));
        }
        if (!mAppliedValues.containsKey(variableKey)) {
            return fallbackValue;
        }
        if (mAppliedValues.get(variableKey).getDataType().equals(LQVariable.BOOLEAN_TYPE)) {
            return Boolean.parseBoolean(mAppliedValues.get(variableKey).getValue().toString());
        }
        invalidateVariables(variableKey);
        return fallbackValue;
    }


    /**
     * Force Liquid to send locally saved data.
     */
    public void flush() {
        LQLog.infoVerbose("Flushing");
        mQueue.execute(new Runnable() {
            @Override
            public void run() {
                mHttpQueuer.flush();
            }
        });
    }

    private void sendBundleVariable(final JSONObject variable) {
        if(!mBundleVariablesSended.contains(variable.optString("name"))) {
            mQueue.execute(new Runnable() {

                @Override
                public void run() {
                    LQLog.infoVerbose("Sending bundle variable " + variable);
                    LQRequestFactory.createVariableRequest(variable).sendRequest(mApiToken);
                }
            });
            mBundleVariablesSended.add(variable.optString("name"));
        }
    }

    /**
     * Reset all collected data that is stored locally.
     *
     * <p>
     * This includes, user, device, session, values, events
     * </p>
     */
    public void reset() {
        reset(false);
    }

    /**
     * Same as reset but preserves the tracked events that aren't in Liquid Server.
     */
    public void softReset() {
        reset(true);
    }

    private void reset(final boolean soft) {
        mQueue.execute(new Runnable() {

            @Override
            public void run() {
                mDevice = new LQDevice(mContext, LIQUID_VERSION);
                mLoadedLiquidPackage = new LQLiquidPackage();
                mAppliedValues = new HashMap<String, LQValue>();
                if(!soft) {
                    mHttpQueuer = new LQQueuer(mContext, mApiToken);
                }
                resetUser();
            }
        });
    }

    private void invalidateVariables(final String variableKey) {
        mQueue.execute(new Runnable() {

            @Override
            public void run() {
                LQLog.infoVerbose("invalidating: " + variableKey);
                boolean removed = mLoadedLiquidPackage.invalidateTargetFromVariableKey(variableKey);
                if (removed) {
                    LQLog.infoVerbose("invalidated: " + variableKey);
                    mAppliedValues = LQValue.convertToHashMap(mLoadedLiquidPackage.getValues());
                    mLoadedLiquidPackage.saveToDisk(mContext);
                    notifyListeners(false);
                }
            }
        });

    }
}
