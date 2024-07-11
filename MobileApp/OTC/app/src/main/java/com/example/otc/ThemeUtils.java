package com.example.otc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class ThemeUtils {
    private static final String PREFERENCES_FILE = "app_preferences";
    private static final String KEY_THEME = "app_theme";

    private SharedPreferences preferences;

    private static ThemeUtils instance;

    private ThemeUtils(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public static synchronized ThemeUtils getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeUtils(context.getApplicationContext());
        }
        return instance;
    }

    public boolean isDarkTheme() {
        return preferences.getBoolean(KEY_THEME, false);
    }

    public void setDarkTheme(boolean isDarkTheme) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_THEME, isDarkTheme);
        editor.apply();
    }

    public void applyTheme(Activity activity) {
        activity.setTheme(isDarkTheme() ? R.style.Base_Theme_OTC_Dark : R.style.Base_Theme_OTC);
    }
}
