package io.lqd.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import android.content.Context;

public class Tools {

    public static String readRawTextFile(Context ctx, int resId) throws IOException {
        InputStream is = ctx.getResources().openRawResource(resId);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        return writer.toString();
    }

    public static String loadJSONFromAsset(Context context, String name) throws IOException {
        String json = null;
        InputStream is = context.getAssets().open(name);

        int size = is.available();

        byte[] buffer = new byte[size];

        is.read(buffer);

        is.close();

        json = new String(buffer, "UTF-8");
        return json;

    }
}
