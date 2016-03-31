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

import android.app.Activity;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LQWebSocket {

    private final String mToken;
    private final JSONObject mIdentifier;
    private Activity mActivity;
    private LQDevelopmentMode mLQDevelopmentMode;
    private WebSocket ws;
    private boolean isSubscribed = false;

    public LQWebSocket(String token, Activity activity, LQDevelopmentMode lqDevelopmentMode) {
        mToken = token;
        mActivity = activity;
        mLQDevelopmentMode = lqDevelopmentMode;
        mIdentifier = new JSONObject();

        if(mToken == null || "".equals(mToken)) {
            LQLog.error("Missing token");
            return;
        }

        try {
            mIdentifier.put("channel", "MessageChannel");
            mIdentifier.put("token", mToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        connectToWebSocket();
    }

    private void connectToWebSocket() {
        try {
            ws = new WebSocketFactory().createSocket("wss://cable.onliquid.com/");


            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    LQLog.info("Connected to websocket");
                    subscribe();
                }

                @Override
                public void onDisconnected(WebSocket websocket,
                                           WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                           boolean closedByServer) throws Exception
                {
                    LQLog.info("Websocket disconnected");
                }

                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    if (isSubscribed && message.contains("end_development")) {
                        ws.disconnect();
                        isSubscribed = false;
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Liquid.getInstance().updateDevModeBooleans();
                                mLQDevelopmentMode.exitDevelopmentMode(mActivity);
                            }
                        });
                    }

                    if(!isSubscribed && message.contains("confirm_subscription")) {
                        LQLog.info("Subscribed to channel: " + mToken);
                        isSubscribed = true;
                        startDevelopment();
                    }
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) {
                    LQLog.error("Websocket error");
                    cause.printStackTrace();
                    try {
                        ws.recreate().connect();
                    } catch (WebSocketException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            ws.connect();

        } catch (WebSocketException | IOException e) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity.getApplicationContext(),
                            "Websocket connection error, check the log for info", Toast.LENGTH_LONG)
                            .show();
                }
            });

            e.printStackTrace();
        }

    }

    public void uiElementAdd(String identifier, String eventname) {
        JSONObject payload = new JSONObject();

        try {
            payload.put("command", "message");
            payload.put("identifier", mIdentifier.toString());
            payload.put("data", constructMessage(identifier, eventname, "add_element").toString());

            ws.sendText(payload.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void uiElementsRemove(String identifier) {
        JSONObject payload = new JSONObject();

        try {
            payload.put("command", "message");
            payload.put("identifier", mIdentifier.toString());
            payload.put("data", constructMessage(identifier, null, "remove_element").toString());

            ws.sendText(payload.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void uiElementsChange(String identifier, String eventname) {
        JSONObject payload = new JSONObject();

        try {
            payload.put("command", "message");
            payload.put("identifier", mIdentifier.toString());
            payload.put("data", constructMessage(identifier, eventname, "change_element").toString());

            ws.sendText(payload.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    private void subscribe() throws JSONException {
        JSONObject payload = new JSONObject();

        payload.put("command", "subscribe");
        payload.put("identifier", mIdentifier.toString());

        ws.sendText(payload.toString());
    }

    private void startDevelopment() throws JSONException {
        JSONObject payload = new JSONObject();
        JSONObject data = new JSONObject();

        data.put("action", "start_development");

        payload.put("command", "message");
        payload.put("identifier", mIdentifier.toString());
        payload.put("data", data.toString());

        ws.sendText(payload.toString());
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Liquid.getInstance().eventTrackingModeON();
            }
        });
    }

    private JSONObject constructMessage(String identifier, String eventname, String action) throws JSONException {
        JSONObject message = new JSONObject();

        if(eventname != null)
            message.put("event_name", eventname);

        message.put("identifier", identifier);
        message.put("platform", "android");
        message.put("action", action);

        return message;
    }

}
