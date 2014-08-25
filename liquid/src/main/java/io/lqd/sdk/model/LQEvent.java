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

import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class LQEvent extends LQModel {

	private static final long serialVersionUID = 4817275328778708775L;

	private String mName;
	private HashMap<String,Object> mAttributes;
	private Date mDate;

	// Initialization
	public LQEvent(String name, HashMap<String,Object> attributes, Date date){
		mName = name;
		if(attributes == null){
			mAttributes = new HashMap<String,Object>();
		} else {
			mAttributes = attributes;
		}
		mDate = date;
	}

	public LQEvent(String name, HashMap<String,Object> attributes) {
		this(name,attributes, new Date());
	}

	// JSON
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			if(mAttributes != null) {
				for(String key : mAttributes.keySet()){
					if(mAttributes.get(key) instanceof Date) {
						json.put(key, LiquidTools.dateToString((Date) mAttributes.get(key)));
					} else {
						json.put(key, mAttributes.get(key));
					}
				}
			}
			json.put("name", mName);
			json.put("date",LiquidTools.dateToString(mDate));
			return json;
		} catch (JSONException e) {
			LQLog.error("LQEvent toJSON: " + e.getMessage());
		}

		return null;
	}

	public static boolean hasvalidName(String name, boolean raiseException) {
		boolean isValid = name == null || name.length() == 0 || name.charAt(0) != '_';
		if(!isValid) {
			LiquidTools.exceptionOrLog(raiseException, "Event can't begin with \' _ \' character ");
		}
		return isValid;
	}
}
