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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.lqd.sdk.model.LQModel;
import io.lqd.sdk.model.LQNetworkRequest;
import io.lqd.sdk.model.LQNetworkResponse;

public class LQQueuer extends LQModel {

    private static final int LIQUID_QUEUE_SIZE_LIMIT = 500;
    private static final int LIQUID_DEFAULT_FLUSH_INTERVAL = 15;
    private static final int LIQUID_MAX_NUMBER_OF_TRIES = 10;
    private static final String PREF_FILE_NAME = "LQPrefs" ;

    private int mFlushInterval;
    private Context mContext;
    private ArrayList<LQNetworkRequest> mHttpQueue;
    private Timer mTimer;
    private String mApiToken;
    private Liquid mLiquidInstance;

    public LQQueuer(Context context, String token) {
        this(context, token, new ArrayList<LQNetworkRequest>());
    }

    public LQQueuer(Context context, String token, ArrayList<LQNetworkRequest> queue) {
        mContext = context;
        mHttpQueue = queue;
        mApiToken = token;
        mFlushInterval = LIQUID_DEFAULT_FLUSH_INTERVAL;
    }

    public boolean addToHttpQueue(LQNetworkRequest queuedEvent) {
        mHttpQueue.add(queuedEvent);
        if (mHttpQueue.size() > LIQUID_QUEUE_SIZE_LIMIT) {
            mHttpQueue.remove(0);
            return true;
        }
        save(mContext, mApiToken);
        return false;
    }

    public synchronized void setFlushTimer(int seconds) {
        stopFlushTimer();
        mFlushInterval = seconds;
        startFlushTimer();
    }

    public void setLiquidInstance(Liquid instance) {
        mLiquidInstance = instance;
    }

    public synchronized int getFlushTimer() {
        return mFlushInterval;
    }

    public ArrayList<LQNetworkRequest> getQueue() {
        return mHttpQueue;
    }

    public void flush() {
        if (LiquidTools.isNetworkAvailable(mContext)) {
            Date now = Calendar.getInstance().getTime();
            ArrayList<LQNetworkRequest> failedQueue = new ArrayList<LQNetworkRequest>();
            LQNetworkResponse result = new LQNetworkResponse();
            while (mHttpQueue.size() > 0) {
                LQNetworkRequest queuedHttp = mHttpQueue.remove(0);
                if (queuedHttp.canFlush(now)) {
                    LQLog.infoVerbose("Flushing " + queuedHttp.toString());
                    result = queuedHttp.sendRequest(mApiToken);
                    if (!result.hasSucceeded()) {
                        LQLog.error("HTTP (" + result.getHttpCode() + ") " + queuedHttp.toString());
                        if (queuedHttp.getNumberOfTries() < LIQUID_MAX_NUMBER_OF_TRIES) {
                            if (!result.hasForbidden()) {
                                queuedHttp.setLastTry(now);
                            }
                            queuedHttp.incrementNumberOfTries();
                            failedQueue.add(queuedHttp);
                        }
                    }
                } else {
                    failedQueue.add(queuedHttp);
                }
            }
            SharedPreferences preferences = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
            preferences.edit().remove("queue").apply();

            mHttpQueue.addAll(failedQueue);
            save(mContext, mApiToken);
        }
    }

    public void startFlushTimer() {
        if (mFlushInterval <= 0 || mTimer != null) {
            return;
        }
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mLiquidInstance.flush();

            }
        };
        mTimer.scheduleAtFixedRate(task, 0,  mFlushInterval * 1000);
        LQLog.infoVerbose("Started flush timer");
    }

    public void stopFlushTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            LQLog.infoVerbose("Stopped flush timer");
        }
    }

    @Override
    public void save(Context context, String path) {
        super.save(context, path + ".queue");
    }

    @Override
    public JSONObject toJSON() {
        JSONArray array = new JSONArray();

        for(LQNetworkRequest request : mHttpQueue) {
            array.put(request.toJSON());
        }
        JSONObject json = new JSONObject();
        try {
            json.put("queue", array);
            return json;
        } catch (JSONException e) {
            LQLog.error("Error creating JSONObject from queue.");
        }
        return null;
    }

    public static ArrayList<LQNetworkRequest> fromJSON(JSONObject jsonObject) {
        ArrayList<LQNetworkRequest> list = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("queue");
            int length = jsonArray.length();
            for(int i = 0 ; i < length; ++i) {
                list.add((LQNetworkRequest) LQNetworkRequest.fromJSON(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            LQLog.error("Error loading queue from JSONObject.");
        }
        return list;
    }

    public static LQQueuer load(Context context, String path) {
        return new LQQueuer(context, path, fromJSON(retriveFromFile(context, path  + ".queue")));
    }

}
