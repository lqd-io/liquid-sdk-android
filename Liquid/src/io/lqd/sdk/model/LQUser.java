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

import android.content.Context;
import android.location.Location;

public class LQUser extends LQModel {

	private static final long serialVersionUID = 1582937331182018907L;

	private String mIdentifier;
	private boolean mAutoIdentified;
	private HashMap<String, Object> mAttributes;


	public LQUser(String identifier) {
		this(identifier, new HashMap<String,Object>(), null);
	}

	public LQUser(String identifier, boolean autoIdentified) {
		this(identifier, new HashMap<String,Object>(), null, autoIdentified);
	}

	public LQUser(String identifier, HashMap<String, Object> attributes, Location location) {
		this(identifier, attributes, location, false);
	}

	public LQUser(String identifier, HashMap<String, Object> attributes, Location location, boolean autoIdentified) {
		mIdentifier = identifier;
		if (attributes == null) {
			mAttributes = new HashMap<String, Object>();
		}
		else {
			mAttributes = attributes;
		}
		this.setLocation(location);
		this.setAutoIdentified(autoIdentified);
	}

	// Attributes
	public String getIdentifier() {
		return mIdentifier;
	}

	public boolean isAutoIdentified() {
		return mAutoIdentified;
	}

	public void setAutoIdentified(boolean mAutoIdentified) {
		this.mAutoIdentified = mAutoIdentified;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof LQUser) && ( this.toJSON().toString().equals(((LQUser) o).toJSON().toString()));
	}

	public HashMap<String, Object> getAttributes() {
		return new HashMap<>(mAttributes);
	}

	public void setAttributes(HashMap<String, Object> attributes) {
		mAttributes = attributes;
	}

	public Object setAttribute(String key, Object attribute) {
		return mAttributes.put(key, attribute);
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
			if(mAttributes != null) {
				for(String key : mAttributes.keySet()) {
					if(mAttributes.get(key) instanceof Date) {
						json.put(key, LiquidTools.dateToString((Date) mAttributes.get(key)));
					} else {
						json.put(key, mAttributes.get(key));
					}
				}
			}
			json.put("unique_id", mIdentifier);
			json.put("auto_identified", mAutoIdentified);
			return json;
		} catch (JSONException e) {
			LQLog.error("LQUser toJSON: " + e.getMessage());
		}
		return null;
	}

	@Override
	public void save(Context context, String path) {
		super.save(context, path + ".user");
	}

	public static LQUser load(Context context, String path) {
		LQUser user = (LQUser) LQModel.load(context, path + ".user");
		if(user == null) {
			user = new LQUser(LQDevice.getDeviceID(context));
		}
		return user;
	}

}
