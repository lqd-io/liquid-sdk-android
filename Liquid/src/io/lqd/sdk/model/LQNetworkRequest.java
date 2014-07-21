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
import io.lqd.sdk.Liquid;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import org.apache.http.entity.StringEntity;

import android.content.Context;
import android.os.Build;

public class LQNetworkRequest extends LQModel {

	public static final int HALF_HOUR = 30 * 60 * 1000;

	private static final long serialVersionUID = 7456534930025458866L;

	private static final String LOCAL = Locale.getDefault().toString().toLowerCase(Locale.ENGLISH);
	private static final String DEVICE = Build.MANUFACTURER + " " + Build.MODEL;

	private static final String USER_AGENT = "Liquid/"+ Liquid.LIQUID_VERSION + " (Android; Android " + Build.VERSION.RELEASE + "; " + LOCAL + "; " + DEVICE +")";
	private String mUrl;
	private String mHttpMethod;
	private String mJson;
	private int mNumberOfTries;
	private Date mLastTry;

	public LQNetworkRequest(String url, String httpMethod, String json){
		mUrl = url;
		mHttpMethod = httpMethod;
		mJson = json;
		mNumberOfTries = 0;
		mLastTry = null;
	}

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

	public void setLastTry(Date lastTry) {
		mLastTry = lastTry;
	}

	public Date getLastTry() {
		return mLastTry;
	}

	public boolean willFlushAndSet(Date now) {
		boolean willflush = canFlush(now);
		if(!willflush) {
			mLastTry = now;
		}
		return willflush;
	}

	public boolean canFlush(Date now) {
		return mLastTry == null ||  now.getTime() - mLastTry.getTime() >= HALF_HOUR;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof LQNetworkRequest) &&
				((LQNetworkRequest)o).getHttpMethod().equals(this.getHttpMethod()) &&
				((LQNetworkRequest)o).getUrl().equals(this.getUrl()) &&
				((LQNetworkRequest)o).getJSON().equals(this.getJSON());
	}

	// File Management
	@SuppressWarnings("unchecked")
	public static ArrayList<LQNetworkRequest> loadQueue(Context context, String fileName) {
		Object result = LQModel.loadObject(context, fileName + ".queue");
		ArrayList<LQNetworkRequest> queue = (ArrayList<LQNetworkRequest>) result;
		if (queue == null) {
			queue = new ArrayList<LQNetworkRequest>();
		}
		LQLog.infoVerbose("Loading queue with " + queue.size() + " items from disk");
		return queue;
	}

	public static void saveQueue(Context context, ArrayList<LQNetworkRequest> queue, String fileName) {
		LQLog.data("Saving queue with " + queue.size() + " items to disk");
		LQModel.save(context, fileName + ".queue", queue);
	}

	public LQNetworkResponse sendRequest(String token) {
		String response = null;
		int responseCode = -1;
		InputStream err = null;

		try {

			OutputStream out = null;
			BufferedOutputStream bout = null;
			HttpURLConnection connection = null;
			try {
				final URL url = new URL(this.getUrl());
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod(this.getHttpMethod());
				connection.setRequestProperty("Authorization", "Token " + token);
				connection.setRequestProperty("User-Agent", USER_AGENT );
				connection.setRequestProperty("Accept", "application/vnd.lqd.v1+json");
				connection.setRequestProperty("Content-Type",
						"application/json");
				connection.setRequestProperty("Accept-Encoding", "gzip");
				connection.setDoInput(true);
				if (this.getJSON() != null) {
					connection.setDoOutput(true);
					out = connection.getOutputStream();
					bout = new BufferedOutputStream(out);
					final StringEntity stringEntity = new StringEntity(this.getJSON(), "UTF-8");
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
			} catch (final EOFException e) {
				LQLog.error("Failed to connect, retrying");
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
		} catch (Exception e) {
			LQLog.http("Failed due to " + e + " responseCode "	+ responseCode);
			LQLog.http("Error " + inputStreamToString(err));
		}
		if ((response != null) || ((responseCode >= 200) && (responseCode < 300))) {
			LQLog.http("HTTP Success " + response);
			return new LQNetworkResponse(responseCode, response);
		}
		return new LQNetworkResponse(responseCode);
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




