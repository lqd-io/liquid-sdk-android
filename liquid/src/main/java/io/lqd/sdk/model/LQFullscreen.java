package io.lqd.sdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class LQFullscreen extends LQInAppMessage {
    private String mTitle;
    private String mTitleColor;

    public LQFullscreen(JSONObject inapp) {
        super(inapp);
        try {
            mTitle = inapp.getString("title");
            mTitleColor = inapp.getString("title_color");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() { return mTitle; }

    public String getTitleColor(){ return mTitleColor; }


}
