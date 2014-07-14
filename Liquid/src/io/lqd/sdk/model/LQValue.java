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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class LQValue implements Serializable {


	private static final long serialVersionUID = 2482245220467419730L;
	private String mId;
	private Object mValue;
	private LQVariable mVariable;
	private boolean mIsDefault;
	private String mTargetId = null;

	public LQValue(JSONObject jsonObject){
		try {
			mId = jsonObject.getString("id");
			mValue = jsonObject.get("value");
			if(!jsonObject.isNull("target_id")) {
				mTargetId = jsonObject.getString("target_id");
			}
			JSONObject v = jsonObject.getJSONObject("variable");
			mVariable = new LQVariable(v);
		} catch (JSONException e) {
			LQLog.error("Parsing LQValue: " + e.getMessage());
		}
	}

	public Object getValue() {
		return mValue;
	}

	public void setValue(Object value) {
		mValue = value;
	}

	public LQVariable getVariable() {
		return mVariable;
	}

	public void setVariable(LQVariable variable) {
		mVariable = variable;
	}

	public String getDataType() {
		return mVariable.getDataType();
	}

	public boolean isDefault() {
		return mIsDefault;
	}

	public String getTargetId() {
		return mTargetId;
	}

	public String getId() {
		return mId;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof LQValue && o != null && this.getId().equals(((LQValue) o).getId());
	}

	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("id", mId);
			json.put("target_id", mTargetId);
			return json;
		} catch (JSONException e) {
			LQLog.error("LQVALUE toJSON:" + e.getMessage());
		}
		return null;
	}

	public static HashMap<String,LQValue> convertToHashMap(ArrayList<LQValue> values){
		HashMap<String,LQValue> hashMap = new HashMap<String, LQValue>();
		for(LQValue value : values) {
			if(value.getValue() != null){
				if(value.getVariable().getName() != null){
					hashMap.put(value.getVariable().getName(), value);
				}
			}
		}
		return hashMap;
	}

	@Override
	public String toString() {
		return "LQValue [mIdentifier=" + mId + ", mValue=" + mValue
				+ ", mVariable=" + mVariable + ", mIsDefault=" + mIsDefault
				+ "]";
	}

}
