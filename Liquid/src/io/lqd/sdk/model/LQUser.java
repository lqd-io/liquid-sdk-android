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
	private String _identifier;
	private HashMap<String, Object> _attributes;

	// Initializer
	public LQUser(String identifier, HashMap<String, Object> attributes,
			Location location) {
		_identifier = identifier;
		_attributes = attributes;
		if (_attributes == null) {
			_attributes = new HashMap<String, Object>();
		}
		this.setLocation(location);
	}

	// Attributes
	public String getIdentifier() {
		return _identifier;
	}

	public void setAttribute(Object attribute, String key) {
		_attributes.put(key, attribute);
	}

	public Object attributeForKey(String key) {
		return _attributes.get(key);
	}

	public void setLocation(Location location) {
		if (location == null) {
			_attributes.remove("_latitude");
			_attributes.remove("_longitude");
		} else {
			_attributes
			.put("_latitude", Double.valueOf(location.getLatitude()));
			_attributes.put("_longitude",
					Double.valueOf(location.getLongitude()));
		}
	}

	// JSON
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("unique_id", _identifier);
			if(_attributes != null){
				for(String key : _attributes.keySet()) {
					if(_attributes.get(key) instanceof Date) {
						json.put(key, LiquidTools.dateToString((Date) _attributes.get(key)));
					} else {
						json.put(key, _attributes.get(key));
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
