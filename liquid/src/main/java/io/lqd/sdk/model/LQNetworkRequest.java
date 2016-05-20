/**
 * Copyright 2014-present Liquid Data Intelligence S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lqd.sdk.model;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import io.lqd.sdk.LQLog;
import io.lqd.sdk.Liquid;

public class LQNetworkRequest extends LQModel {

    public static final int HALF_HOUR = 30 * 60 * 1000;

    private static final long serialVersionUID = 7456534930025458866L;

    private static final String LOCAL = Locale.getDefault().toString();
    private static final String DEVICE = Build.MANUFACTURER + " " + Build.MODEL;

    private static final String USER_AGENT = "Liquid/" + Liquid.LIQUID_VERSION + " (Android; Android " + Build.VERSION.RELEASE + "; " + LOCAL + "; " + DEVICE + ")";
    private String mUrl;
    private String mHttpMethod;
    private String mJson;
    private int mNumberOfTries;
    private Date mLastTry;
    private SimpleDateFormat mDateFormat;

    public LQNetworkRequest(String url, String httpMethod, String json) {
        mUrl = url;
        mHttpMethod = httpMethod;
        mJson = json;
        mNumberOfTries = 0;
        mLastTry = null;
        mDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    }

    public String getUrl() {
        return mUrl;
    }
    public String getHttpMethod() {
        return mHttpMethod;
    }
    public String getJSON() {
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
                ((LQNetworkRequest) o).getHttpMethod().equals(this.getHttpMethod()) &&
                ((LQNetworkRequest) o).getUrl().equals(this.getUrl()) &&
                ((LQNetworkRequest) o).getJSON().equals(this.getJSON());
    }

    public LQNetworkResponse sendRequest(String token) {
        String response = null;
        String date = mDateFormat.format(Calendar.getInstance().getTime());
        int responseCode = -1;
        InputStream err = null;
        BufferedReader boin = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(this.getHttpMethod());
            connection.setRequestProperty("Date", date);
            connection.setRequestProperty("Authorization", "Token " + token);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/vnd.lqd.v1+json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setDoInput(true);
            if (this.getJSON() != null) {
                connection.setDoOutput(true);
                outputStream = new DataOutputStream(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(this.getJSON());
                writer.close();
                outputStream.close();
            }
            responseCode = connection.getResponseCode();
            err = connection.getErrorStream();
            GZIPInputStream gzip = new GZIPInputStream(connection.getInputStream());
            boin = new BufferedReader(new InputStreamReader(gzip, "UTF-8"));
            response = boin.readLine();
        } catch (IOException e) {
            LQLog.http("Failed due to " + e + " responseCode " + responseCode);
            LQLog.http("Error " + inputStreamToString(err));
        } finally {
            if(connection != null)
                connection.disconnect();
            try {
                if(outputStream != null)
                    outputStream.close();
            } catch (IOException e) {}
            try {
                if(err != null)
                    err.close();
            } catch (IOException e) {}
            try {
                if (boin != null)
                    boin.close();
            } catch (IOException e) {}
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

    public static LQModel fromJSON(JSONObject jsonObject) {
        LQNetworkRequest lqNetworkRequest = null;
        try {
            String mUrl = jsonObject.getString("mUrl");
            String mHttpMethod = jsonObject.getString("mHttpMethod");
            String mJson = jsonObject.getString("mJson");
            lqNetworkRequest = new LQNetworkRequest(mUrl, mHttpMethod, mJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lqNetworkRequest;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("mUrl", mUrl);
            json.put("mHttpMethod", mHttpMethod);
            json.put("mJson", mJson);
            json.put("mNumberOfTries", mNumberOfTries);
            json.put("mLastTry", mLastTry);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}




