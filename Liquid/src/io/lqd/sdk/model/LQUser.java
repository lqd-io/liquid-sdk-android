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

import android.location.Location;

public class LQUser {
	private String mIdentifier;
	private HashMap<String, Object> mAttributes;

	// Initializer
	public LQUser(String identifier, HashMap<String, Object> attributes,Location location) {
		mIdentifier = identifier;
		mAttributes = attributes;
		if (mAttributes == null) {
			mAttributes = new HashMap<String, Object>();
		}
		this.setLocation(location);
	}

	// Attributes
	public String getIdentifier() {
		return mIdentifier;
	}

	public void setAttribute(Object attribute, String key) {
		mAttributes.put(key, attribute);
	}

	public Object attributeForKey(String key) {
		return mAttributes.get(key);
	}

	public void setLocation(Location location) {
		if (location == null) {
			mAttributes.remove("_latitude");
			mAttributes.remove("_longitude");
		} else {
			mAttributes.put("_latitude", Double.valueOf(location.getLatitude()));
			mAttributes.put("_longitude",Double.valueOf(location.getLongitude()));
		}
	}

	// JSON
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("unique_id", mIdentifier);
			if(mAttributes != null){
				for(String key : mAttributes.keySet()) {
					if(mAttributes.get(key) instanceof Date) {
						json.put(key, LiquidTools.dateToString((Date) mAttributes.get(key)));
					} else {
						json.put(key, mAttributes.get(key));
					}
				}
			}
			return json;
		} catch (JSONException e) {
			LQLog.error("LQUser toJSON: " + e.getMessage());
		}
		return null;
	}

}
