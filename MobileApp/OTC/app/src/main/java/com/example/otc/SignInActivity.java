package com.example.otc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * SignInActivity is a page when user is asked for credentials to login after clicking Sign In on SignMainActivity
 * uses activity_login.xml
 */
public class SignInActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    Button buttonLogin, buttonBackToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonBackToMain = findViewById(R.id.buttonBackToMain);

        // Click listener to the login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the input values
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Check user password so it is login functionality
                ApiService.getInstance().userPasswordCheck(email, password, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(SignInActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            //if correct credentials
                            if(Boolean.parseBoolean(responseBody)) {
                                runOnUiThread(() ->
                                    Toast.makeText(SignInActivity.this, "Login successful", Toast.LENGTH_SHORT).show()
                                );
                                //check if email is verified
                                ApiService.getInstance().isEmailVerified(email, new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        runOnUiThread(() ->
                                                Toast.makeText(SignInActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        if (response.isSuccessful()) {
                                            String responseBody = response.body().string();
                                            // if email is verified create session
                                            if (Boolean.parseBoolean(responseBody)) {
                                                ApiService.getInstance().sessionCreate(email, password, new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        runOnUiThread(() ->
                                                                Toast.makeText(SignInActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                        );
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        if (response.isSuccessful()) {
                                                            String responseBody = response.body().string();
                                                            // get sessionId from API and save it to SharedPrefsManager when i quit app to be saved
                                                            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                                                            String sessionID = jsonObject.get("sessionId").getAsString();
                                                            SharedPrefsManager.getInstance(SignInActivity.this).setLogin(sessionID, email);
                                                            // Navigate to the Home
                                                            Intent intent = new Intent(SignInActivity.this, HomePageActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                        } else {
                                                            String responseBody = response.body().string();
                                                            runOnUiThread(() ->
                                                                Toast.makeText(SignInActivity.this, "Unsuccessfully creating session: " + responseBody, Toast.LENGTH_SHORT).show()
                                                            );
                                                        }
                                                    }
                                                });
                                            } else { // if email is not verified go to verification
                                                SharedPrefsManager.getInstance(SignInActivity.this).saveEmail(email);
                                                startActivity(new Intent(SignInActivity.this, VerificationCodeActivity.class));
                                            }

                                        } else { // if email verification API fails
                                            String responseBody = response.body().string();
                                            runOnUiThread(() ->
                                                    Toast.makeText(SignInActivity.this, "Unsuccessfully email verification: " + response.message(), Toast.LENGTH_SHORT).show()
                                            );
                                        }
                                    }
                                });
                            } else { // if user enters wrong credentials
                                        runOnUiThread(() ->
                                                Toast.makeText(SignInActivity.this, "Login failed check credentials", Toast.LENGTH_SHORT).show()
                                        );
                            }


                        } else { // if login check API fails
                            String responseBody = response.body().string();
                            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                            String message = jsonObject.get("detail").getAsString(); //get reply error
                            runOnUiThread(() ->
                                    Toast.makeText(SignInActivity.this, "Login failed: " + message, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });

            }
        });

        // Back to main button
        buttonBackToMain.setOnClickListener(view -> {
            finish();
        });

    }
}
