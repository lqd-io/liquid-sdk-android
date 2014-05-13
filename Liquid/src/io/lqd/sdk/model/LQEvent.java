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

public class LQEvent {

	private String _name;
	private HashMap<String,Object> _attributes;
	private Date _date;

	// Initialization
	public LQEvent(String name, HashMap<String,Object> attributes){
		_name = name;
		_attributes = attributes;
		if(_attributes == null){
			_attributes = new HashMap<String,Object>();
		}
		_date = new Date();
	}

	// JSON
	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("name", _name);
			json.put("date",LiquidTools.dateToString(_date));
			if(_attributes != null){
				for(String key : _attributes.keySet()){
					if(_attributes.get(key) instanceof Date) {
						json.put(key, LiquidTools.dateToString((Date) _attributes.get(key)));
					} else {
						json.put(key, _attributes.get(key));
					}
				}
			}
			return json;
		} catch (JSONException e) {
			LQLog.error("LQEvent toJSON: " + e.getMessage());
		}

		return null;
	}
}
