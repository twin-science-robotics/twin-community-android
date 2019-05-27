package com.twinscience.twin.lite.android.utils;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mertselcukdemir on 14.12.2018
 * Copyright (c) 2018 Twin Science & Robotics to present
 * All rights reserved.
 */
public class JsonUtils {
    public static String loadJSONFromAsset(Activity activity, String fileName) {
        String json = null;
        try {
            InputStream is = activity.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
