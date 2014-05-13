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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class LQLiquidPackage implements Serializable{

	private static final long serialVersionUID = 2252438865270376L;
	private static final String LIQUID_PACKAGE_FILENAME = "LiquidPackage";

	private ArrayList<LQValue> mValues = new ArrayList<LQValue>();

	public LQLiquidPackage() {

	}

	public LQLiquidPackage(JSONObject jsonObject) {
		try {
			JSONArray valuesJsonArray = jsonObject.getJSONArray("values");
			for(int index = 0; index < valuesJsonArray.length(); index ++){
				JSONObject valueJson = valuesJsonArray.getJSONObject(index);
				LQValue v = new LQValue(valueJson);
				mValues.add(v);
			}
		} catch (JSONException e) {
			LQLog.error("Parsing LQLiquidPackage: " + e.getMessage());
		}

	}

	public boolean isEmpty() {
		return (mValues.size() == 0);
	}

	public ArrayList<LQValue> getValues() {
		return mValues;
	}

	public boolean invalidateTargetFromVariableKey(String variableKey) {
		boolean invalidated = false;
		for(LQValue value : mValues) {
			if(value.getVariable().getName().equals(variableKey)) {
				if(value.getTargetId() == null) {
					invalidated = invalidateValue(variableKey);
				} else {
					invalidated = invalidateValuesOfTarget(value.getTargetId());
				}
			}
		}
		return invalidated;
	}

	private boolean invalidateValuesOfTarget(String targetId) {
		int valueItems = mValues.size();
		ArrayList<LQValue> tempvar =  new ArrayList<LQValue>();

		for(LQValue value : mValues) {
			if(!targetId.equals(value.getTargetId())) {
				tempvar.add(value);
				--valueItems;
			}
		}
		mValues = tempvar;
		return valueItems > 0;
	}

	private boolean invalidateValue(String variableKey) {
		int valueItems = mValues.size();
		ArrayList<LQValue> tempvar =  new ArrayList<LQValue>();

		for(LQValue value : mValues) {
			if(!value.getVariable().getName().equals(variableKey)) {
				tempvar.add(value);
				--valueItems;
			}
		}
		mValues = tempvar;
		return valueItems > 0;
	}

	public void saveToDisk(Context context) {
		LQLog.data("Saving to local storage");
		try {
			FileOutputStream fileOutputStream = context.openFileOutput(
					LIQUID_PACKAGE_FILENAME + ".vars",
					Context.MODE_PRIVATE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					fileOutputStream);
			objectOutputStream.writeObject(this);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			LQLog.infoVerbose("Could not save liquid package to file");
		}
	}

	public static LQLiquidPackage loadFromDisk(Context context){
		LQLog.data("Loading from local storage");
		try {
			FileInputStream fileInputStream = context
					.openFileInput(LIQUID_PACKAGE_FILENAME
							+ ".vars");
			ObjectInputStream objectInputStream = new ObjectInputStream(
					fileInputStream);
			Object result = objectInputStream.readObject();
			objectInputStream.close();
			return (LQLiquidPackage) result;
		} catch (IOException e) {
			LQLog.infoVerbose("Could not load liquid package from file");
		} catch (ClassNotFoundException e) {
			LQLog.infoVerbose("Could not load liquid package from file");
		}
		return null;
	}

}
