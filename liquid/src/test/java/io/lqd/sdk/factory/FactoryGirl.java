package io.lqd.sdk.factory;

import android.content.Context;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import io.lqd.sdk.Liquid;
import io.lqd.sdk.model.LQDataPoint;
import io.lqd.sdk.model.LQDevice;
import io.lqd.sdk.model.LQEvent;
import io.lqd.sdk.model.LQNetworkRequest;
import io.lqd.sdk.model.LQUser;
import io.lqd.sdk.model.LQValue;

public class FactoryGirl {

    private static final SecureRandom random = new SecureRandom();


    public static LQNetworkRequest createRequest() {
        return new LQNetworkRequest(randomString(), "GET", randomString());
    }

    public static LQEvent createEvent() {
        return new LQEvent(randomString(), null);
    }


    public static LQDevice createDevice(Context c) {
        return new LQDevice(c, Liquid.LIQUID_VERSION);
    }

    public static LQDataPoint createDataPoint(Context c) {
        return new LQDataPoint(createUser(), createDevice(c), createEvent(), new ArrayList<LQValue>());
    }

    public static LQEvent createEvent(HashMap<String, Object> attrs) {
        return new LQEvent(randomString(), attrs);
    }

    public static LQUser createUser() {
        return new LQUser(randomString());
    }


    private static String randomString() {
        return new BigInteger(130, random).toString(32);
    }

}
