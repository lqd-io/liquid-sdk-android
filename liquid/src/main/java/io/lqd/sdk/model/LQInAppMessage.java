package io.lqd.sdk.model;

import android.support.v4.util.ArrayMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LQInAppMessage {

    private String mId;
    private String mFormulaId;
    private String mTitleColor;
    private String mTitle;
    private String mBgColor;
    private String mMessage;
    private String mMessageColor;
    private String mDismissEventName;
    private ArrayMap<String, Object> mDismissAttributes;
    private String mType;
    private String mLayout;
    private ArrayList<Cta> mCtas = new ArrayList<>();

    public LQInAppMessage(JSONObject inapp){
        try {
            mLayout = inapp.optString("layout");
            if ("modal".equals(mLayout))
                try {
                    mTitle = inapp.getString("title");
                    mTitleColor = inapp.getString("title_color");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            mBgColor = inapp.getString("bg_color");
            mMessage = inapp.getString("message");
            mMessageColor = inapp.getString("message_color");
            mDismissEventName = inapp.getString("dismiss_event_name");
            mType = inapp.getString("type");

            JSONObject event_attributes = inapp.getJSONObject("event_attributes");
            mFormulaId = event_attributes.getString("formula_id");
            mId = event_attributes.getString("id");
            mDismissAttributes = new ArrayMap<>();
            mDismissAttributes.put("formula_id", mFormulaId);
            mDismissAttributes.put("id", mId);
            JSONArray ctas = inapp.getJSONArray("ctas");
            int ctas_length = ctas.length();

            for (int j = 0; j < ctas_length; j++) {
                mCtas.add(new Cta(ctas.getJSONObject(j)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getLayout(){ return mLayout; }

    public String getFormulaId(){ return mFormulaId; }

    public String getId(){ return mId; }

    public String getTitle() { return mTitle; }

    public String getTitleColor(){ return mTitleColor; };

    public String getMessage(){ return mMessage; }

    public String getMessageColor(){ return mMessageColor; }

    public String getBgColor(){ return mBgColor; }

    public String getType(){ return mType; }

    public String getDismissEventName() { return mDismissEventName; }

    public ArrayMap<String, Object> getDismissAttributes(){ return mDismissAttributes; }

    public ArrayList<Cta> getCtas() {
        return mCtas;
    }

    public static class Cta {

        private String mDeepLink;
        private String mCtaId;
        private String mCtaFormulaId;
        private String mButtonColor;
        private String mButtonText;
        private String mButtonTextColor;
        private String mCtasEventName;
        private ArrayMap<String , Object> mCtasAttributes;

        public Cta(JSONObject cta) {
            try {
                mButtonColor = cta.getString("bg_color");
                mCtasEventName = cta.getString("event_name");

                JSONObject cta_attributes = cta.getJSONObject("cta_attributes");

                mCtaFormulaId = cta_attributes.getString("formula_id");
                mCtaId = cta_attributes.getString("id");
                mCtasAttributes = new ArrayMap<>();
                mCtasAttributes.put("formula_id", mCtaFormulaId);
                mCtasAttributes.put("cta_id", mCtaId);

                mButtonText = cta.getString("title");
                mButtonTextColor = cta.getString("title_color");
                mDeepLink = cta.getString("android_url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String getDeepLink() { return mDeepLink; }

        public String getButtonText(){ return mButtonText; }

        public String getButtonTextColor(){ return mButtonTextColor; }

        public String getButtonColor(){ return mButtonColor; }

        public String getCtasEventName(){ return mCtasEventName; }

        public ArrayMap<String, Object> getCtasAttributes(){ return mCtasAttributes; }
    }
}