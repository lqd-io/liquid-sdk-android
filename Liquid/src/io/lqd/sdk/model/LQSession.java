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

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class LQSession extends LQModel {

	private static final long serialVersionUID = -3586505654518525092L;

	private String mId;
	private Date mEnd;
	private Date mStart;
	private int mTimeout;

	public LQSession(int timeout, Date date) {
		mId = LQSession.newSessionIdentifier();
		mTimeout = timeout;
		mEnd = null;
		mStart = date;
	}

	public LQSession(int timeout) {
		this(timeout, new Date());
	}

	public String getIdentifier(){
		return mId;
	}

	public Date getStartDate(){
		return mStart;
	}

	public void setEndDate(Date end){
		mEnd = end;
	}

	public Date getEndDate() {
		return mEnd;
	}

	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("started_at", LiquidTools.dateToString(mStart));
			json.put("timeout", mTimeout);
			json.put("unique_id", mId);
			if(mEnd != null) {
				json.put("ended_at", LiquidTools.dateToString(mEnd));
			}
			return json;
		} catch (JSONException e) {
			LQLog.error("LQSession toJSON: " + e.getMessage());
		}
		return null;
	}

	// Session Identifier Generator
	public static String newSessionIdentifier(){
		UUID uid = UUID.randomUUID();
		String uidStr = uid.toString();
		uidStr = uidStr.replace("-", "");
		long timeSince1970 = Calendar.getInstance().getTimeInMillis();
		return uidStr.substring(0, 16) + "" + timeSince1970;
	}
}
