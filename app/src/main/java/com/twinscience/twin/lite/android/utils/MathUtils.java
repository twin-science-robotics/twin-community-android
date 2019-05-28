package com.twinscience.twin.lite.android.utils;

import android.os.Build;

import java.util.Arrays;

import androidx.annotation.RequiresApi;

/**
 * Created by mertselcukdemir on 3.04.2019
 * Copyright (c) 2019 YGA to present
 * All rights reserved.
 */
public class MathUtils {

    public static int findSumWithoutUsingStream(int[] array) {
        int sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static int findSumUsingStream(Integer[] array) {
        return Arrays.stream(array)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public static double findAverageWithoutUsingStream(int[] array) {
        int sum = findSumWithoutUsingStream(array);
        return (double) sum / array.length;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static double findAverageUsingStream(int[] array) {
        return Arrays.stream(array).average().orElse(Double.NaN);
    }

}
