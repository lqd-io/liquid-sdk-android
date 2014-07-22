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

package io.lqd.sdk;

import io.lqd.sdk.model.LQDataPoint;
import io.lqd.sdk.model.LQNetworkRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class LQRequestFactory {

	protected static final String LIQUID_SERVER_BASE_URL = "https://api.lqd.io/collect/";
	protected static final String LIQUID_DATAPOINT_URL = LIQUID_SERVER_BASE_URL + "data_points";
	protected static final String LIQUID_ALIAS_URL = LIQUID_SERVER_BASE_URL + "aliases";
	protected static final String LIQUID_LQD_PACKAGE_URL = LIQUID_SERVER_BASE_URL + "users/%s/devices/%s/liquid_package";
	protected static final String LIQUID_VARIABLES_URL = LIQUID_SERVER_BASE_URL + "variables";


	public static LQNetworkRequest createAliasRequest(String oldId, String newId) {
		JSONObject json;
		try {
			json = new JSONObject();
			json.put("unique_id", newId);
			json.put("unique_id_alias", oldId);
			return new LQNetworkRequest(LIQUID_ALIAS_URL, "POST", json.toString());
		} catch (JSONException e) {
			return null;
		}
	}

	public static LQNetworkRequest createDataPointRequest(LQDataPoint datapoint) {
		return new LQNetworkRequest(LIQUID_DATAPOINT_URL, "POST", datapoint.toJSON().toString());
	}

	public static LQNetworkRequest requestLiquidPackageRequest(String userId, String userDevice) {
		String url = String.format(LIQUID_LQD_PACKAGE_URL, userId, userDevice);
		return new LQNetworkRequest(url, "GET", null);
	}

	public static LQNetworkRequest createVariableRequest(JSONObject variable) {
		return new LQNetworkRequest(LIQUID_VARIABLES_URL, "POST", variable.toString());
	}

}
