package com.chevstrap.rbx;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class CustomLocale {

    public static Context set(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        App.getChevstrapSettings().setLocale(languageCode);

        return context.createConfigurationContext(config);
    }

    public static Context applySaved(Context context) {
        String language = App.getChevstrapSettings().getLocale();
        if (language.isEmpty()) {
            language = "en";
        }
        return set(context, language);
    }
}
