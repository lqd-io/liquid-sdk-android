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
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class LQSession {

	private String _identifier;
	private Date _end;
	private Date _start;
	private int _timeout;
	private HashMap<String, Object> _attributes;

	public LQSession(int timeout) {
		_identifier = LQSession.newSessionIdentifier();
		_timeout = timeout;
		_end = null;
		_attributes = new HashMap<String, Object>();
		_start = new Date();
	}
	public String getIdentifier(){
		return _identifier;
	}
	public Date getStart(){
		return _start;
	}
	public void setEnd(Date end){
		_end = end;
	}

	public void setAttribute(Object attribute, String key) {
		_attributes.put(key, attribute);
	}

	public void getAttribute(String key) {
		_attributes.get(key);
	}

	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try {
			if(_attributes != null){
				for(String key : _attributes.keySet()){
					json.put(key, _attributes.get(key));
				}
			}
			json.put("started_at", LiquidTools.dateToString(_start));
			json.put("timeout", _timeout);
			json.put("unique_id", _identifier);
			if(_end != null) {
				json.put("ended_at", LiquidTools.dateToString(_end));
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
