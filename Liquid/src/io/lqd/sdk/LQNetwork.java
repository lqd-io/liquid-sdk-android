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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.http.entity.StringEntity;

import android.os.Build;

public class LQNetwork {

	private int mSessionTimeout;
	private String mApiToken;
	private static final String USER_AGENT = "Liquid/"+ Liquid.LIQUID_VERSION + "(Android; Android " + Build.VERSION.RELEASE + ")";

	public LQNetwork(String apiToken, int sessionTimeout) {
		this.mApiToken = apiToken;
		this.mSessionTimeout = sessionTimeout;
	}

	public String httpConnectionTo(String json, String endPoint,
			String httpMethod) {
		String response = null;
		int responseCode = -1;
		InputStream err = null;

		try {
			int retries = 0;
			boolean succeeded = false;
			while ((retries < mSessionTimeout) && !succeeded) {

				OutputStream out = null;
				BufferedOutputStream bout = null;
				HttpURLConnection connection = null;
				try {
					final URL url = new URL(endPoint);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod(httpMethod);
					connection.setRequestProperty("Authorization", "Token "
							+ mApiToken);
					connection.setRequestProperty("User-Agent", USER_AGENT );
					connection.setRequestProperty("Accept", "application/vnd.lqd.v1+json");
					connection.setRequestProperty("Content-Type",
							"application/json");
					connection.setRequestProperty("Accept-Encoding", "gzip");
					connection.setDoInput(true);
					if (json != null) {
						connection.setDoOutput(true);
						out = connection.getOutputStream();
						bout = new BufferedOutputStream(out);
						final StringEntity stringEntity = new StringEntity(json);
						stringEntity.writeTo(bout);
						bout.close();
						bout = null;
						out.close();
						out = null;
					}
					responseCode = connection.getResponseCode();
					err = connection.getErrorStream();
					BufferedReader boin;
					boin = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
					response = boin.readLine();
					boin.close();
					succeeded = true;
				} catch (final EOFException e) {
					LQLog.error("Failed to connect, retrying");
					retries++;
				} finally {
					if (null != bout) {
						try {
							bout.close();
						} catch (final IOException e) {
							;
						}
					}
					if (null != out) {
						try {
							out.close();
						} catch (final IOException e) {
							;
						}
					}
					if (null != err) {
						try {
							err.close();
						} catch (final IOException e) {
							;
						}
					}
					if (null != connection) {
						connection.disconnect();
					}
				}
			}
		} catch (Exception e) {
			LQLog.http("Failed due to " + e + " responseCode "	+ responseCode);
			LQLog.http("Error " + inputStreamToString(err));
		}
		if ((response != null) || ((responseCode >= 200) && (responseCode < 300))) {
			LQLog.http("HTTP Success " + response);
			return response;
		}
		return null;
	}

	private static String inputStreamToString(final InputStream stream) {
		if(stream == null) {
			return "";
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(stream)));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			return sb.toString();
		} catch (IOException e) {
			return "";
		}
	}

}
