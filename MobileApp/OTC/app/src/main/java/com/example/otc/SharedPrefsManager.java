package com.example.otc;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * SharedPrefsManager is for managing session so login logout sessions you dont need to login every time you open app
 * Uses prefs to have sessionID saved even when someone closes app
 * used in SignInActivity to set user sessionID
 * used in MainActivity to see condition if user is logged in if yes it proceeds to homepage (HomePageActivity), if not it proceeds to main menu for logging in and create account (SignMainActivity)
 * used in HomePageActivity to logout, when you click logout button in menu it logs you out (so clears session)
 */
public class SharedPrefsManager {
    private static final String PREF_NAME = "appPrefs";
    private static String SESSION_ID = "standard_session";
    private static String EMAIL = "standard_email"; //helping variable to sometimes pass email especially to verificationCode
    private static SharedPrefsManager instance;
    private SharedPreferences prefs;
    private static final String PIN_PREF_KEY = "pin_key";
    private static final String PIN_SET_PREF_KEY = "pin_set_key";
    private static final String BIOMETRIC_ENABLED_KEY = "biometric_enabled";
    private static final String FIRST_START_KEY = "firstStart";

    private SharedPrefsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context);
        }
        return instance;
    }

    public void saveSessionId(String sessionId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SESSION_ID, sessionId);
        editor.apply();
    }
    public void saveEmail(String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(EMAIL, email);
        editor.apply();
    }

    public void savePin(String pin) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PIN_PREF_KEY, pin);
        editor.apply();
    }

    public void savePinSet(boolean pinSet) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PIN_SET_PREF_KEY, pinSet);
        editor.apply();
    }

    public boolean isPinSet() {
        return prefs.getBoolean(PIN_SET_PREF_KEY, false);
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(BIOMETRIC_ENABLED_KEY, false);
    }

    public void setLogin(String sessionId, String email) {
        saveSessionId(sessionId);
        saveEmail(email);
    }

    public void setBiometricEnabled(boolean enabled) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(BIOMETRIC_ENABLED_KEY, enabled);
        editor.apply();
    }

    public boolean isFirstStart() {
        return prefs.getBoolean(FIRST_START_KEY, true);
    }

    public void setFirstStart(boolean firstStart) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FIRST_START_KEY, firstStart);
        editor.apply();
    }


    public void deletePin() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PIN_PREF_KEY);
        editor.putBoolean(PIN_SET_PREF_KEY, false); // Reset PIN set flag
        editor.apply();
    }

    public String getSessionId() {
        return prefs.getString(SESSION_ID, null);
    }
    public String getEmail() {
        return prefs.getString(EMAIL, null);
    }

    public String getStoredPin() {
        return prefs.getString(PIN_PREF_KEY, null);
    }

    public void logOut() {
        ApiService.getInstance().sessionDelete(getSessionId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}
            @Override
            public void onResponse(Call call, Response response) throws IOException {}
        });
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}
