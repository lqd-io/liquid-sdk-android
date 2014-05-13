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

import java.io.Serializable;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class LQVariable implements Serializable {

	private static final long serialVersionUID = -4922263795266095802L;
	public static final String DATE_TYPE = "datetime";
	public static final String STRING_TYPE = "string";
	public static final String INT_TYPE = "integer";
	public static final String FLOAT_TYPE = "float";
	public static final String COLOR_TYPE = "color";
	public static final String BOOLEAN_TYPE = "boolean";

	private String mId;
	private String mName;
	private String mDataType;
	private String mTargetId;

	public LQVariable(JSONObject jsonObject){
		try {
			mId = (String) jsonObject.get("id");
			mName = (String) jsonObject.get("name");
			mTargetId = jsonObject.optString("target_id", null);
			mDataType = (String) jsonObject.get("data_type");
		} catch (JSONException e) {
			LQLog.error("Parsing LQVariable: " + e.getMessage());
		}
	}

	public String getName() {
		return mName;
	}

	public String getDataType() {
		return mDataType;
	}

	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("id", mId);
			json.put("name", mName);
			json.put("data_type", mDataType);
			return json;
		} catch (JSONException e) {
			LQLog.error("LQVariable toJSON: " + e.getMessage());
		}
		return null;
	}

	private static JSONObject buildJsonObject(String variablekey, String variableType) throws JSONException{
		JSONObject variable = new JSONObject();
		variable.put("name", variablekey);
		variable.put("data_type", variableType);
		return variable;
	}

	public static JSONObject buildJsonObject(String variablekey, String variableValue, String variableType) {
		JSONObject variable = null;
		try {
			variable =  buildJsonObject(variablekey, variableType);
			variable.put("default_value",variableValue);
		} catch (JSONException e) {
			LQLog.error("LQVariable buildJSON: " + e.getMessage());
		}
		return variable;
	}

	public static JSONObject buildJsonObject(String variablekey, int variableValue, String variableType) {
		JSONObject variable = null;
		try {
			variable =  buildJsonObject(variablekey, variableType);
			variable.put("default_value",variableValue);
		} catch (JSONException e) {
			LQLog.error("LQVariable buildJSON: " +e.getMessage());
		}
		return variable;
	}

	public static JSONObject buildJsonObject(String variablekey, boolean variableValue, String variableType) {
		JSONObject variable = null;
		try {
			variable =  buildJsonObject(variablekey, variableType);
			variable.put("default_value",variableValue);
		} catch (JSONException e) {
			LQLog.error("LQVariable buildJSON: " +e.getMessage());
		}
		return variable;
	}

	public static JSONObject buildJsonObject(String variablekey, float variableValue, String variableType) {
		JSONObject variable = null;
		try {
			variable = buildJsonObject(variablekey, variableType);
			variable.put("default_value",variableValue);
		} catch (JSONException e) {
			LQLog.error("LQVariable buildJSON: " + e.getMessage());
		}
		return variable;
	}

	public static JSONObject buildJsonObject(String variablekey, Date variableValue, String variableType) {
		JSONObject variable = null;
		try {
			variable =  buildJsonObject(variablekey, variableType);
			variable.put("default_value",LiquidTools.dateToString(variableValue));
		} catch (JSONException e) {
			LQLog.error("LQVariable buildJSON: " + e.getMessage());
		}
		return variable;
	}



	@Override
	public String toString() {
		return "LQVariable [mId=" + mId + ", mName=" + mName
				+ ", mType=" + mDataType + "]";
	}

	public String getTargetId() {
		return mTargetId;
	}

}
