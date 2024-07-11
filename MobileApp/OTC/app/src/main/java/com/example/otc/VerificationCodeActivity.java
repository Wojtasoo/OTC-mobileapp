package com.example.otc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * VerificationCodeActivity is a page after successful sign in and he doesn't have verified email, a user needs to enter verification code from provided email
 * uses activity_verification_code.xml
 */
public class VerificationCodeActivity extends AppCompatActivity {

    EditText editTextVerificationCode;
    Button buttonVerify, buttonResendCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);

        editTextVerificationCode = findViewById(R.id.editTextVerificationCode);
        buttonVerify = findViewById(R.id.buttonVerify);
        buttonResendCode = findViewById(R.id.buttonResendCode);

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextVerificationCode.getText().toString().trim();
                verifyCode(code);
            }
        });

        buttonResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode();
            }
        });
    }

    /**
     * method for verifying code
     * @param code is a String of code got from user
     */
    private void verifyCode(String code) {
        ApiService.getInstance().userVerifyEmail(SharedPrefsManager.getInstance(this).getEmail(), code, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(VerificationCodeActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(VerificationCodeActivity.this, "Verification successful", Toast.LENGTH_SHORT).show()
                    );
                    // Navigate to the Login page
                    Intent intent = new Intent(VerificationCodeActivity.this, SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    String message = jsonObject.get("detail").getAsString(); //get reply error
                    runOnUiThread(() ->
                            Toast.makeText(VerificationCodeActivity.this, "Verification failed: " + message, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

    }

    /**
     * method for resending code on email
     */
    private void resendVerificationCode() {
        ApiService.getInstance().resend_verify_email(SharedPrefsManager.getInstance(this).getEmail(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(VerificationCodeActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(VerificationCodeActivity.this, "Verification code resent", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    String message = jsonObject.get("detail").getAsString(); //get reply error
                    runOnUiThread(() ->
                            Toast.makeText(VerificationCodeActivity.this, "Verification failed: " + message, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

}
