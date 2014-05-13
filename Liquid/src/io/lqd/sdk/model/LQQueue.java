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

import java.io.Serializable;

public class LQQueue implements Serializable {

	private static final long serialVersionUID = 7456534930025458866L;
	private String _url;
	private String _httpMethod;
	private String _json;
	private int _numberOfTries;

	// Initialization
	public LQQueue(String url, String httpMethod, String json){
		_url = url;
		_httpMethod = httpMethod;
		_json = json;
		_numberOfTries = 0;
	}

	// Getters
	public String getUrl(){
		return _url;
	}
	public String getHttpMethod(){
		return _httpMethod;
	}
	public String getJSON(){
		return _json;
	}
	public int getNumberOfTries(){
		return _numberOfTries;
	}

	public void incrementNumberOfTries(){
		_numberOfTries++;
	}
}
