package com.example.otc;

import static java.lang.Boolean.TRUE;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * MainActivity is a page which is executed when app starts
 * it determines if user is Signed In or not (his sessionID matches session in Database), if he is then he is redirected to HomePageActivity
 * if he is Sign Out (he has wrong sessionID or doesn't have it) he is redirected to SignMainActivity
 * uses activity_main.xml
 */
public class MainActivity extends AppCompatActivity {

    private static final int MAX_RETRY_COUNT = 5;
    private static int retryCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils themeUtils = ThemeUtils.getInstance(this); //at start of app. apply theme
        themeUtils.applyTheme(this); // Apply theme first
        super.onCreate(savedInstanceState);

        if (isSystemDarkModeEnabled(getApplicationContext())) {
            ThemeUtils.getInstance(getApplicationContext()).setDarkTheme(TRUE);
            ThemeUtils.getInstance(getApplicationContext()).applyTheme(this);
        }

        ApiService.getInstance().sessionCheck(SharedPrefsManager.getInstance(this).getSessionId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                retryCount++;
                if (retryCount < MAX_RETRY_COUNT) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "No internet connection: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                    retryCount=0;
                    finish();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if(Boolean.parseBoolean(responseBody)){
                        Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent); //if user is in session go to Homepage
                    } else {
                        Intent intent = new Intent(MainActivity.this, SignMainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } else {
                    String responseBody = response.body().string();
                    Intent intent = new Intent(MainActivity.this, SignMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });

    }

    private boolean isSystemDarkModeEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int currentNightMode = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
        return false; // System dark mode is not supported on versions prior to Android 10
    }
}