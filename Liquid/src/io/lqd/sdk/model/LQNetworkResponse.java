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

public class LQNetworkResponse {

	private int mHttpCode;
	private String mData;

	public LQNetworkResponse() {
		this(-1);
	}

	public LQNetworkResponse(int httpCode) {
		this(httpCode, null);
	}

	public LQNetworkResponse(int httpCode, String response) {
		mHttpCode = httpCode;
		mData = response;
	}

	public int getHttpCode() {
		return mHttpCode;
	}

	public String getRequestResponse() {
		return mData;
	}

	public boolean hasSucceeded() {
		return mHttpCode >= 200 && mHttpCode < 300;
	}

	public boolean hasForbidden() {
		return mHttpCode == 401 || mHttpCode == 403;
	}

}
