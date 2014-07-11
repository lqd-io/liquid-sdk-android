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

import java.util.ArrayList;

import android.content.Context;

public class LQQueue extends LQModel {

	private static final long serialVersionUID = 7456534930025458866L;
	private String mUrl;
	private String mHttpMethod;
	private String mJson;
	private int mNumberOfTries;

	// Initialization
	public LQQueue(String url, String httpMethod, String json){
		mUrl = url;
		mHttpMethod = httpMethod;
		mJson = json;
		mNumberOfTries = 0;
	}

	// Getters
	public String getUrl() {
		return mUrl;
	}
	public String getHttpMethod() {
		return mHttpMethod;
	}
	public String getJSON(){
		return mJson;
	}
	public int getNumberOfTries() {
		return mNumberOfTries;
	}

	public void incrementNumberOfTries() {
		mNumberOfTries++;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof LQQueue) &&
				((LQQueue)o).getHttpMethod().equals(this.getHttpMethod()) &&
				((LQQueue)o).getUrl().equals(this.getUrl()) &&
				((LQQueue)o).getJSON().equals(this.getJSON());
	}

	// File Management
	@SuppressWarnings("unchecked")
	public static ArrayList<LQQueue> loadQueue(Context context, String fileName) {
		Object result = LQModel.loadObject(context, fileName + ".queue");
		ArrayList<LQQueue> queue = (ArrayList<LQQueue>) result;
		if (queue == null) {
			queue = new ArrayList<LQQueue>();
		}
		LQLog.infoVerbose("Loading queue with " + queue.size() + " items from disk");
		return queue;
	}

	public static void saveQueue(Context context, ArrayList<LQQueue> queue, String fileName) {
		LQLog.data("Saving queue with " + queue.size() + " items to disk");
		LQModel.save(context, fileName + ".queue", queue);
	}
}
