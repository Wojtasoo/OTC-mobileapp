package com.example.otc;

import static java.lang.Boolean.TRUE;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SignMainActivity serves as a way for choosing users what they want to do if they are not logged in
 * Sign In, Sign Up, Sign up with google
 * user is redirected to other pages after clicking specific button
 * uses activity_sign_main.xml
 */
public class SignMainActivity extends AppCompatActivity {

    Button buttonSignIn, buttonSignUp, buttonSignUpGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_main);

        // Initialize buttons
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUpGoogle = findViewById(R.id.buttonSignUpGoogle);


        // Click listeners
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to login page
                startActivity(new Intent(SignMainActivity.this, SignInActivity.class));
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to sign-up page
                startActivity(new Intent(SignMainActivity.this, SignUpActivity.class));
            }
        });

        buttonSignUpGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Google login page
                startActivity(new Intent(SignMainActivity.this, GoogleLoginActivity.class));
            }
        });

        if (isSystemDarkModeEnabled(getApplicationContext())) {

            ThemeUtils.getInstance(getApplicationContext()).setDarkTheme(TRUE);
            ThemeUtils.getInstance(getApplicationContext()).applyTheme(this);
        }
    }

    private boolean isSystemDarkModeEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int currentNightMode = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
        return false; // System dark mode is not supported on versions prior to Android 10
    }
}
