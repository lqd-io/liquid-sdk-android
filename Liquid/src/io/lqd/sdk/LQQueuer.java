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

import io.lqd.sdk.model.LQNetworkRequest;
import io.lqd.sdk.model.LQNetworkResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;

public class LQQueuer {

	private static final int LIQUID_QUEUE_SIZE_LIMIT = 500;
	private static final int LIQUID_DEFAULT_FLUSH_INTERVAL = 15;
	private static final int LIQUID_MAX_NUMBER_OF_TRIES = 10;

	private int mFlushInterval;
	private Context mContext;
	private ArrayList<LQNetworkRequest> mHttpQueue;
	private Timer mTimer;
	private String mApiToken;

	public LQQueuer(Context context, String token) {
		this(context, token, new ArrayList<LQNetworkRequest>());
	}

	public LQQueuer(Context context, String token,
			ArrayList<LQNetworkRequest> queue) {
		mContext = context;
		mHttpQueue = queue;
		mApiToken = token;
	}

	public boolean addToHttpQueue(LQNetworkRequest queuedEvent) {
		mHttpQueue.add(queuedEvent);
		if (mHttpQueue.size() > LIQUID_QUEUE_SIZE_LIMIT) {
			mHttpQueue.remove(0);
			return true;
		}
		return false;
	}

	public synchronized void setFlushTimer(int seconds) {
		stopFlushTimer();
		mFlushInterval = seconds;
		startFlushTimer();
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
					result = queuedHttp.httpConnectionTo(mApiToken);
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
			mHttpQueue.addAll(failedQueue);
			LQNetworkRequest.saveQueue(mContext, mHttpQueue, mApiToken);
		}
	}

	public void startFlushTimer() {
		if (mFlushInterval <= 0 || mTimer != null) {
			return;
		}
		mTimer = new Timer();
		TimerTask task = new TimerTask() {
			Liquid instance = Liquid.getInstance();
			@Override
			public void run() {
				instance.flush();

			}
		};
		mTimer.scheduleAtFixedRate(task, 0,
				LIQUID_DEFAULT_FLUSH_INTERVAL * 1000);
		LQLog.infoVerbose("Started flush timer");
	}

	public void stopFlushTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
			LQLog.infoVerbose("Stopped flush timer");
		}
	}

}
