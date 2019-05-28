package com.google.blockly.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by mertselcukdemir on 27.03.2019
 * Copyright (c) 2019 YGA to present
 * All rights reserved.
 */
public class BlockViewUtils {


    public static int convertDpToPx(Context context, float dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return ((int) px);
    }

}
