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

import io.lqd.sdk.model.LQDataPoint;
import io.lqd.sdk.model.LQDevice;
import io.lqd.sdk.model.LQEvent;
import io.lqd.sdk.model.LQLiquidPackage;
import io.lqd.sdk.model.LQModel;
import io.lqd.sdk.model.LQQueue;
import io.lqd.sdk.model.LQSession;
import io.lqd.sdk.model.LQUser;
import io.lqd.sdk.model.LQValue;
import io.lqd.sdk.model.LQVariable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

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

public class Liquid {

	static final String TAG_LIQUID = "LIQUID";

	private static final String LIQUID_SERVER_URL = "https://api.lqd.io/collect/";
	protected static final String LIQUID_VERSION = "0.7.0-beta";
	private static final int LIQUID_QUEUE_SIZE_LIMIT = 500;
	private static final int LIQUID_DEFAULT_FLUSH_INTERVAL = 15;
	private static final int LIQUID_MAX_NUMBER_OF_TRIES = 10;
	private static final int LIQUID_DEFAULT_SESSION_TIMEOUT = 10;

	private static int mSessionTimeout;
	private static String mApiToken;
	private LQUser mCurrentUser;
	private LQUser mPreviousUser;
	private LQDevice mDevice;
	private LQSession mCurrentSession;
	private Date mEnterBackgroundtime;
	private ExecutorService mQueue;
	private Timer mTimer;
	private ArrayList<LQQueue> mHttpQueue;
	private int mFlushInterval;
	private boolean mAutoLoadValues;
	private boolean mFlushOnBackground = true;
	private Context mContext;
	private static Liquid mInstance;
	private LQLiquidPackage mAppliedLiquidPackage;
	private HashMap<String, LQValue> mAppliedValues;
	private HashMap<String, Activity> mAttachedActivities = new HashMap<String, Activity>();
	private HashMap<String, LiquidOnEventListener> mListeners = new HashMap<String, LiquidOnEventListener>();
	private boolean mNeedCallbackCall = false;
	private LQNetwork mNetwork;
	private boolean isDevelopmentMode = false;

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
		if (mInstance == null) {
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
	public static Liquid initialize(Context context, String apiToken,
			boolean developmentMode) {
		if (mInstance == null) {
			mInstance = new Liquid(context, apiToken, developmentMode);
		}
		mInstance.mContext = context;
		return mInstance;
	}

	public Liquid(Context context, String apiToken, boolean developmentMode) {
		LiquidTools.checkForPermission(permission.INTERNET, context);
		if (apiToken == null || apiToken.length() == 0) {
			throw new IllegalArgumentException("Your API Token is invalid: \'"
					+ apiToken + "\'.");
		}
		mContext = context;
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			attachActivityCallbacks();
		}
		mSessionTimeout = LIQUID_DEFAULT_SESSION_TIMEOUT;
		mHttpQueue = new ArrayList<LQQueue>();
		mApiToken = apiToken;
		mDevice = new LQDevice(context, LIQUID_VERSION);
		mQueue = Executors.newSingleThreadExecutor();
		mFlushInterval = LIQUID_DEFAULT_FLUSH_INTERVAL;
		mAppliedLiquidPackage = new LQLiquidPackage();
		startFlushTimer();
		isDevelopmentMode = developmentMode;
		mNetwork = new LQNetwork(mApiToken, mSessionTimeout);

		mHttpQueue = LQQueue.loadQueue(mContext, mApiToken);

		// Get last user and init session
		mPreviousUser = LQUser.load(mContext, mApiToken);
		identifyUser(mPreviousUser.getIdentifier(),
				mPreviousUser.getAttributes(), null,
				mPreviousUser.isAutoIdentified(), false);

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
	 * Returns whether or not Liquid flushes in background
	 * 
	 * @return true if Liquid flushes in the background, otherwise false.
	 */
	public boolean canFlushOnBackground() {
		return mFlushOnBackground;
	}

	/**
	 * Set the Liquid behavior to flush in background.
	 * 
	 * @param flushOnBackground
	 *            whether or not Liquid will flush in background.
	 */
	public void setFlushOnBackground(boolean flushOnBackground) {
		mFlushOnBackground = flushOnBackground;
	}

	/**
	 * Set the flush interval
	 * 
	 * @param flushInterval
	 *            value in seconds.
	 */
	public synchronized void setFlushInterval(int flushInterval) {
		stopFlushTimer();
		mFlushInterval = flushInterval;
		startFlushTimer();
	}

	/**
	 * Get the flush interval
	 * 
	 * @return value in seconds of the flush interval.
	 */
	public synchronized int getFlushInterval() {
		return mFlushInterval;
	}

	public void setNetwork(LQNetwork net) {
		mNetwork = net;
	}

	/*
	 * *******************
	 * User Interaction
	 * *******************
	 */

	public void alias() {
		final String oldID = mPreviousUser.getIdentifier();
		final String newID = mCurrentUser.getIdentifier();
		if (!mPreviousUser.isAutoIdentified()) {
			LQLog.warning("Can't alias (" + oldID
					+ "): Isn't an anonymous user.");
			return;
		}
		LQLog.infoVerbose("Making alias between (" + oldID + ") and (" + newID
				+ ").");
		mQueue.execute(new Runnable() {
			@Override
			public void run() {
				String endPoint = LIQUID_SERVER_URL + "users/" + oldID
						+ "/alias";
				JSONObject params = new JSONObject();
				try {
					params.put("new_user_id", newID);
				} catch (JSONException e) {
				}
				addToHttpQueue(params.toString(), endPoint, "POST");
			}
		});
	}

	/**
	 * Identifies the current user with a generated UUID.
	 * 
	 */
	@Deprecated
	public void identifyUser() {
		resetUser();
	}

	/**
	 * Create a new User with a new UUID
	 */
	public void resetUser() {
		String automaticIdentifier = LQModel.newIdentifier();
		identifyUser(automaticIdentifier, null, null, true, false);
	}

	/**
	 * Identifies the current user with a custom UUID.
	 * 
	 * @param identifier
	 *            Custom UUID.
	 */
	public void identifyUser(String identifier) {
		identifyUser(identifier, null, null, false, true);
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
		identifyUser(identifier, null, null, false, alias);
	}

	/**
	 * Identifies the current user with a custom UUID and additional attributes.
	 * 
	 * @param identifier
	 *            The custom UUID.
	 * @param attributes
	 *            Additional user attributes.
	 */
	public void identifyUser(String identifier,
			HashMap<String, Object> attributes) {
		identifyUser(identifier, attributes, null, false, true);
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
	public void identifyUser(String identifier,
			HashMap<String, Object> attributes, boolean alias) {
		identifyUser(identifier, attributes, null, false, alias);
	}

	/**
	 * Identifies the current user with a custom UUID and additional attributes.
	 * 
	 * @deprecated Use {@link #setCurrentLocation(Location location)}
	 *             instead.</p>
	 * @param identifier
	 *            The custom UUID.
	 * @param location
	 *            User Location.
	 */
	@Deprecated
	public void identifyUser(String identifier, Location location) {
		identifyUser(identifier, null, location, false, true);
	}

	public void identifyUser(String identifier, Location location, boolean alias) {
		identifyUser(identifier, null, location, false, alias);
	}

	/**
	 * Identifies the current user with a custom UUID and additional attributes.
	 * 
	 * @deprecated Use {@link #setCurrentLocation(Location location)}
	 *             instead.</p>
	 * @param identifier
	 *            The custom UUID.
	 * @param attributes
	 *            Additional user attributes.
	 * @param location
	 *            User Location.
	 */
	@Deprecated
	public void identifyUser(String identifier,
			HashMap<String, Object> attributes, Location location) {
		identifyUser(identifier, attributes, location, false, true);
	}

	public void identifyUser(String identifier,
			HashMap<String, Object> attributes, Location location, boolean alias) {
		identifyUser(identifier, attributes, location, false, alias);
	}

	private void identifyUser(String identifier,
			HashMap<String, Object> attributes, Location location,
			boolean autoIdentified, boolean alias) {
		final String finalIdentifier = identifier;
		final HashMap<String, Object> finalAttributes = LQModel
				.sanitizeAttributes(attributes, isDevelopmentMode);

		final Location finalLocation = location;

		// same id -> just update attributes
		if (mCurrentUser != null
				&& mCurrentUser.getIdentifier().equals(identifier)) {
			mCurrentUser.setAttributes(finalAttributes);
			mCurrentUser.save(mContext, mApiToken);
			LQLog.infoVerbose("Already identified with user " + finalIdentifier
					+ ". Not identifying again.");
			return;
		}
		destroySession(UniqueTime.newDate());

		mPreviousUser = mCurrentUser;
		mCurrentUser = new LQUser(finalIdentifier, finalAttributes,
				finalLocation, autoIdentified);
		newSession(true);
		requestValues();
		mCurrentUser.save(mContext, mApiToken);

		if (alias) {
			alias();
		}

		LQLog.info("From now on we're identifying the User by the identifier '"
				+ finalIdentifier + "'");
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
	 * Add or update an additional attribute to the user.
	 * 
	 * @param key
	 *            Attribute key
	 * @param attribute
	 *            Attribute value
	 */
	public void setUserAttribute(String key, Object attribute) {
		if (mCurrentUser == null) {
			identifyUser();
		} else {
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
	}

	/**
	 * Add or update the user location.
	 * 
	 * @deprecated Use {@link #setCurrentLocation(Location location)} instead.
	 * @param location
	 *            User location.
	 */
	@Deprecated
	public void setUserLocation(Location location) {
		if (mCurrentUser == null) {
			identifyUser();
		} else {
			final Location finalLocation = location;
			mQueue.execute(new Runnable() {
				@Override
				public void run() {
					mCurrentUser.setLocation(finalLocation);
					mCurrentUser.save(mContext, mApiToken);
				}
			});
		}
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
				mCurrentUser.save(mContext, mApiToken);
			}
		});
	}

	/**
	 * Add or update the GCM registration ID
	 * 
	 * @param id
	 *            GCM identifier
	 */
	public void setGCMregistrationID(String id) {
		mDevice.setPushId(id);
	}

	/**
	 * Remove the GCM registration ID
	 */
	public void removeGCMregistrationID() {
		mDevice.setPushId(null);
	}

	private void newSession(boolean runInCurrentThread) {
		final Date now = UniqueTime.newDate();
		LQLog.infoVerbose("Open Session: " + now.toString());
		Runnable newSessionRunnable = new Runnable() {
			@Override
			public void run() {
				if (mCurrentUser == null) {
					identifyUser();
				}
				mCurrentSession = new LQSession(mSessionTimeout, now);
				track("_startSession", null, now);
			}
		};
		if (runInCurrentThread) {
			newSessionRunnable.run();
		} else {
			mQueue.execute(newSessionRunnable);
		}
	}

	/**
	 * Closes the current session and opens a new one
	 */
	public void destroySession() {
		destroySession(UniqueTime.newDate());
		newSession(false);
	}

	private void destroySession(Date closeDate) {
		if ((mCurrentUser != null) && (mCurrentSession != null)
				&& mCurrentSession.getEndDate() == null) {
			LQLog.infoVerbose("Close Session: " + closeDate.toString());
			mCurrentSession.setEndDate(closeDate);
			track("_endSession", null, closeDate);
		}
	}

	private void checkSessionTimeout() {
		if ((mCurrentSession != null) && (mEnterBackgroundtime != null)) {
			Date now = UniqueTime.newDate();
			long interval = (now.getTime() - mEnterBackgroundtime.getTime()) / 1000;
			if (interval >= mSessionTimeout) {
				destroySession(mEnterBackgroundtime);
				newSession(false);
			} else {
				track("_resumeSession", null, UniqueTime.newDate());
			}
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
	 */
	public void track(String eventName) {
		if (LQEvent.hasvalidName(eventName, isDevelopmentMode)) {
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
	public void track(String eventName, HashMap<String, Object> attributes) {
		if (LQEvent.hasvalidName(eventName, isDevelopmentMode)) {
			track(eventName, attributes, UniqueTime.newDate());
		}
	}

	private void track(String eventName, HashMap<String, Object> attributes,
			Date date) {
		LQLog.infoVerbose("Tracking: " + eventName);
		if ((mCurrentUser == null) || (mCurrentSession == null)) {
			identifyUser();
		}
		if ((eventName == null) || (eventName.length() == 0)) {
			eventName = "unnamedEvent";
		}
		final String finalEventName = eventName;
		final HashMap<String, Object> finalAttributes = LQModel
				.sanitizeAttributes(attributes, isDevelopmentMode);
		final LQUser finalUser = mCurrentUser;
		final LQDevice finalDevice = mDevice;
		final LQSession finalSession = mCurrentSession;
		final Date finalDate = date;
		mQueue.execute(new Runnable() {
			@Override
			public void run() {
				LQEvent event = new LQEvent(finalEventName, finalAttributes,
						finalDate);
				LQDataPoint dataPoint = new LQDataPoint(finalUser, finalDevice,
						finalSession, event, mAppliedLiquidPackage.getValues(),
						finalDate);
				LQLog.data(dataPoint.toJSON());
				String endPoint = LIQUID_SERVER_URL + "data_points";
				addToHttpQueue(dataPoint.toJSON(), endPoint, "POST");
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
		if (android.os.Build.VERSION.SDK_INT < 14) {
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
		if (android.os.Build.VERSION.SDK_INT < 14) {
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
		if (android.os.Build.VERSION.SDK_INT < 14) {
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
		if (android.os.Build.VERSION.SDK_INT < 14) {
			activityStartedCallback(activity);
		}
	}

	@SuppressLint("NewApi")
	private boolean isApplicationInBackground(Activity activity) {
		boolean configurationChanged;
		if(android.os.Build.VERSION.SDK_INT < 11) {
			configurationChanged = (activity.getChangingConfigurations() == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL);
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
		if (android.os.Build.VERSION.SDK_INT < 14) {
			activityDestroyedCallback(activity);
		}
	}

	public void activityCreated(Activity activity) {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			activityCreatedCallback(activity);
		}
	}

	private void activityDestroyedCallback(Activity activity) {
	}

	private void activityCreatedCallback(Activity activity) {
	}

	private void activityStopedCallback(Activity activity) {
		if (isApplicationInBackground(activity)) {
			track("_pauseSession", null, UniqueTime.newDate());
			mEnterBackgroundtime = UniqueTime.newDate();
			flush();
		} else {
			mEnterBackgroundtime = null;
		}
		requestValues();
	}

	private void activityStartedCallback(Activity activity) {
		mInstance.attachActivity(activity);
		if (mNeedCallbackCall) {
			mNeedCallbackCall = false;
			notifyListeners(false);
		}
		loadLiquidPackage(true);

		checkSessionTimeout();
		loadLiquidPackage(true);
	}

	private void activityResumedCallback(Activity activity) {
		mInstance.attachActivity(activity);
		startFlushTimer();
	}

	private void activityPausedCallback(Activity activity) {
		mInstance.detachActivity(activity);
		stopFlushTimer();
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
			public void onActivitySaveInstanceState(Activity activity,
					Bundle bundle) {
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
					String endPoint = LIQUID_SERVER_URL + "users/"
							+ mCurrentUser.getIdentifier() + "/devices/"
							+ mDevice.getUID() + "/liquid_package";
					String dataFromServer = mNetwork.httpConnectionTo(null,
							endPoint, "GET");
					if (dataFromServer != null) {
						try {
							JSONObject jsonObject = new JSONObject(
									dataFromServer);
							LQLiquidPackage liquidPackage = new LQLiquidPackage(
									jsonObject);
							LQLog.http(jsonObject.toString());
							liquidPackage.saveToDisk(mContext);
						} catch (JSONException e) {
							LQLog.error("Could not parse JSON "
									+ dataFromServer);
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
				LQLiquidPackage liquidPackage = LQLiquidPackage
						.loadFromDisk(mContext);

				if (liquidPackage != null) {
					mAppliedLiquidPackage = liquidPackage;
					mAppliedValues = LQValue
							.convertValuesToHashMap(liquidPackage.getValues());
				} else {
					mAppliedLiquidPackage = new LQLiquidPackage();
					mAppliedValues = new HashMap<String, LQValue>();
				}
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
			sendBundleVariable(LQVariable.buildJsonObject(variableKey,
					fallbackValue, LQVariable.DATE_TYPE));
		}
		if (!mAppliedValues.containsKey(variableKey)) {
			return fallbackValue;
		}
		if (mAppliedValues.get(variableKey).getDataType()
				.equals(LQVariable.DATE_TYPE)) {
			try {
				Object value = mAppliedValues.get(variableKey).getValue();
				return value == null ? null : LiquidTools
						.stringToDate((String) value);
			} catch (IllegalArgumentException e) {
				LQLog.error("Error parsing Date with key: \"" + variableKey
						+ "\"");
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
			sendBundleVariable(LQVariable.buildJsonObject(variableKey,
					LiquidTools.colorToHex(fallbackValue),
					LQVariable.COLOR_TYPE));
		}
		if (!mAppliedValues.containsKey(variableKey)) {
			return fallbackValue;
		}
		if (mAppliedValues.get(variableKey).getDataType()
				.equals(LQVariable.COLOR_TYPE)) {
			try {
				return Color.parseColor(mAppliedValues.get(variableKey)
						.getValue().toString());
			} catch (IllegalArgumentException e) {
				LQLog.error("Error parsing Color with key: \"" + variableKey
						+ "\"");
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
			sendBundleVariable(LQVariable.buildJsonObject(variableKey,
					fallbackValue, LQVariable.STRING_TYPE));
		}
		if (!mAppliedValues.containsKey(variableKey)) {
			return fallbackValue;
		}
		if (mAppliedValues.get(variableKey).getDataType()
				.equals(LQVariable.STRING_TYPE)) {
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
			sendBundleVariable(LQVariable.buildJsonObject(variableKey,
					fallbackValue, LQVariable.INT_TYPE));
		}
		if (!mAppliedValues.containsKey(variableKey)) {
			return fallbackValue;
		}
		if (mAppliedValues.get(variableKey).getDataType()
				.equals(LQVariable.INT_TYPE)) {
			try {
				return Integer.parseInt(mAppliedValues.get(variableKey)
						.getValue().toString());
			} catch (NumberFormatException e) {
				LQLog.error("Error parsing Integer with key: \"" + variableKey
						+ "\"");
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
			sendBundleVariable(LQVariable.buildJsonObject(variableKey,
					fallbackValue, LQVariable.FLOAT_TYPE));
		}
		if (!mAppliedValues.containsKey(variableKey)) {
			return fallbackValue;
		}
		if (mAppliedValues.get(variableKey).getDataType()
				.equals(LQVariable.FLOAT_TYPE)) {
			try {
				return Float.parseFloat(mAppliedValues.get(variableKey)
						.getValue().toString());
			} catch (NumberFormatException e) {
				LQLog.error("Error parsing Float with key: \"" + variableKey
						+ "\"");
			}
		}
		invalidateVariables(variableKey);
		return fallbackValue;
	}

	public boolean getBooleanVariable(String variableKey, boolean fallbackValue) {
		if (isDevelopmentMode) {
			sendBundleVariable(LQVariable.buildJsonObject(variableKey,
					fallbackValue, LQVariable.BOOLEAN_TYPE));
		}
		if (!mAppliedValues.containsKey(variableKey)) {
			return fallbackValue;
		}
		if (mAppliedValues.get(variableKey).getDataType()
				.equals(LQVariable.BOOLEAN_TYPE)) {
			return Boolean.parseBoolean(mAppliedValues.get(variableKey)
					.getValue().toString());
		}
		invalidateVariables(variableKey);
		return fallbackValue;
	}

	// Queueing
	private void addToHttpQueue(String json, String endPoint, String httpMethod) {
		LQQueue queuedEvent = new LQQueue(endPoint, httpMethod, json);
		mHttpQueue.add(queuedEvent);
		if (mHttpQueue.size() > LIQUID_QUEUE_SIZE_LIMIT) {
			mHttpQueue.remove(0);
		}
		LQQueue.saveQueue(mContext, mHttpQueue, mApiToken);
	}

	/**
	 * Force Liquid to send locally saved data.
	 */
	public void flush() {
		LQLog.infoVerbose("Flushing");
		mQueue.execute(new Runnable() {
			@Override
			public void run() {
				if (LiquidTools.isNetworkAvailable(mContext)) {
					ArrayList<LQQueue> failedQueue = new ArrayList<LQQueue>();
					while (mHttpQueue.size() > 0) {
						LQQueue queuedHttp = mHttpQueue.get(0);
						// LQLog.infoVerbose("Flushing " +
						// queuedHttp.getJSON());
						String result = mNetwork.httpConnectionTo(
								queuedHttp.getJSON(), queuedHttp.getUrl(),
								queuedHttp.getHttpMethod());
						mHttpQueue.remove(queuedHttp);
						if (result == null) {
							LQLog.http("Could not send data to server"
									+ queuedHttp.toString());
							if (queuedHttp.getNumberOfTries() < LIQUID_MAX_NUMBER_OF_TRIES) {
								queuedHttp.incrementNumberOfTries();
								failedQueue.add(queuedHttp);
							}
						}
					}
					mHttpQueue.addAll(failedQueue);
					LQQueue.saveQueue(mContext, mHttpQueue, mApiToken);
				}
			}
		});
	}

	private void sendBundleVariable(final JSONObject variable) {
		mQueue.execute(new Runnable() {

			@Override
			public void run() {
				String httpResult = null;
				LQLog.infoVerbose("Sending bundle variable " + variable);
				httpResult = mNetwork.httpConnectionTo(variable.toString(),
						LIQUID_SERVER_URL + "variables", "POST");

				if (httpResult == null) {
					LQLog.http("Server did not accept data from " + variable);
				}

			}
		});
	}

	// Timers
	private void startFlushTimer() {
		if (mFlushInterval <= 0) {
			return;
		}
		if (mTimer != null) {
			return;
		}
		mTimer = new Timer();
		final Liquid instance = this;
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				instance.flush();

			}
		};
		mTimer.scheduleAtFixedRate(task, 0,
				LIQUID_DEFAULT_FLUSH_INTERVAL * 1000);
		LQLog.infoVerbose("Started flush timer");
	}

	private void stopFlushTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
			LQLog.infoVerbose("Stopped flush timer");
		}
	}

	/**
	 * Reset all collected data that is stored locally.
	 * 
	 * <p>
	 * This includes, user, device, token, values
	 * </p>
	 */
	public void reset() {
		mQueue.execute(new Runnable() {
			@Override
			public void run() {
				mCurrentUser = null;
				mCurrentSession = null;
				mDevice = null;
				mApiToken = null;
				mEnterBackgroundtime = null;
				mTimer = null;
				mAppliedLiquidPackage = null;
				mAppliedValues = null;
				mHttpQueue = new ArrayList<LQQueue>();
			}
		});
	}

	private void invalidateVariables(final String variableKey) {
		final Runnable save = new Runnable() {

			@Override
			public void run() {
				mAppliedLiquidPackage.saveToDisk(mContext);

			}
		};

		mQueue.execute(new Runnable() {

			@Override
			public void run() {
				LQLog.infoVerbose("invalidating: " + variableKey);
				boolean removed = mAppliedLiquidPackage
						.invalidateTargetFromVariableKey(variableKey);
				if (removed) {
					LQLog.infoVerbose("invalidated: " + variableKey);
					mAppliedValues = LQValue
							.convertValuesToHashMap(mAppliedLiquidPackage
									.getValues());
					notifyListeners(false);
					mQueue.execute(save);
				}
			}
		});

	}

}
