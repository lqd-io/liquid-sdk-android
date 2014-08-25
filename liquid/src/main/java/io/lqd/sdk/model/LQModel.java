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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import android.content.Context;

public abstract class LQModel implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Generate a random unique id
	 * @return
	 */
	public static String newIdentifier() {
		String uid = UUID.randomUUID().toString().toUpperCase(Locale.ENGLISH);
		long timeSince1970 = Calendar.getInstance().getTimeInMillis();
		return uid + "-" + String.valueOf(timeSince1970).substring(0,10);
	}

	/*
	 * *******************
	 * Attributes Assertion
	 * *******************
	 */

	/**
	 * Check if the key have invalid chars: { $ . \0 } are the invalid attributes
	 * @param key key that will be checked
	 * @param raiseException if true will raise IllegalArgumentException, otherwise will Log
	 * @return true if the key is valid, false otherwise
	 */
	public static boolean validKey(String key, boolean raiseException) {
		boolean isValid = (!key.contains("$") && !key.contains(".") && !key.contains("\0"));
		if(!isValid) {
			LiquidTools.exceptionOrLog(raiseException, "Key: (" + key + ") contains invalid chars: (. $ \\0)");
		}
		return isValid;
	}

	/**
	 * Check if the attribute type is valid: {null, String, Number, Boolean, Date} are the valid attributes
	 * @param attribute attribute that will be checked
	 * @param raiseException if true will raise IllegalArgumentException, otherwise will Log
	 * @return true if the attribute is valid, false otherwise
	 */
	public static boolean validValue(Object attribute, boolean raiseException) {
		boolean isValid = ((attribute == null) || (attribute instanceof String) ||
				(attribute instanceof Number) || (attribute instanceof Boolean) ||
				(attribute instanceof Date));
		if(!isValid) {
			LiquidTools.exceptionOrLog(raiseException, "Key: (" + attribute + ") contains invalid chars: (. $ \\0)");
		}
		return isValid;
	}

	public static HashMap<String, Object> sanitizeAttributes(HashMap<String, Object> attributes, boolean raiseException) {
		if (attributes == null) {
			return null;
		}
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		for (String key : attributes.keySet()) {
			if (LQModel.validKey(key, raiseException) && LQModel.validValue(attributes.get(key), raiseException)) {
				attrs.put(key, attributes.get(key));
			}
		}
		return attrs;
	}


	/*
	 * *******************
	 * File Management
	 * *******************
	 */

	protected void save(Context context, String path) {
		LQLog.data("Saving " + this.getClass().getSimpleName());
		save(context,path,this);
	}

	protected static void save(Context context, String path, Object o) {
		try {
			FileOutputStream fileOutputStream = context.openFileOutput(path, Context.MODE_PRIVATE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					fileOutputStream);
			objectOutputStream.writeObject(o);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (Exception e) {
			LQLog.infoVerbose( "Could not save to file " + path);
		}
	}

	protected static Object loadObject(Context context, String path) {
		LQLog.infoVerbose("Loading " + path + "from disk");
		try {
			FileInputStream fileInputStream = context.openFileInput(path);
			ObjectInputStream objectInputStream = new ObjectInputStream(
					fileInputStream);
			Object result = objectInputStream.readObject();
			objectInputStream.close();
			return result;
		} catch (IOException e) {
			LQLog.infoVerbose("Could not load queue from file " + path);
			return null;
		} catch (ClassNotFoundException e) {
			LQLog.infoVerbose("Could not load queue from file " + path);
			return null;
		}
    }

	protected static LQModel load(Context context, String path) {
		return (LQModel) loadObject(context, path);
	}

}
