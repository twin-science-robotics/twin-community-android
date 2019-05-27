package com.twinscience.twin.lite.android.utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import com.twinscience.twin.lite.android.main.MainActivity;

import java.util.Locale;


public class LocaleManager {

    public static void updateResources(MainActivity activity, String language) {
        Resources activityRes = activity.getResources();
        Configuration activityConf = activityRes.getConfiguration();
        Locale newLocale = new Locale(language);
        activityConf.setLocale(newLocale);
        activityRes.updateConfiguration(activityConf, activityRes.getDisplayMetrics());

        Resources applicationRes = activity.getApplicationContext().getResources();
        Configuration applicationConf = applicationRes.getConfiguration();
        applicationConf.setLocale(newLocale);
        applicationRes.updateConfiguration(applicationConf,
                applicationRes.getDisplayMetrics());
    }

    public static String getLocaleLanguage() {
        return Locale.getDefault().getLanguage().equals("tr") ? "tr" : "en";
    }
}