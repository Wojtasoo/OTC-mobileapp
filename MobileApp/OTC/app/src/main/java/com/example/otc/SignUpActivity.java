package com.example.otc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * SignUpActivity is a page when user is asked for credentials to Sign up after clicking Sign up on SignMainActivity
 * uses activity_signup.xml
 */
public class SignUpActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextPasswordRetype;
    Button buttonSignUp, buttonBackToMain;
    TextView textViewTermsLink;
    CheckBox checkBoxTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordRetype = findViewById(R.id.editTextPasswordRetype);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewTermsLink = findViewById(R.id.textViewTermsLink);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);

        // Initially disable sign-up button bec we want checkbox first
        buttonSignUp.setEnabled(false);

        // Click listener for Terms and Conditions link
        textViewTermsLink.setOnClickListener(v -> showTermsDialog());

        // Click listener for checkbox
        checkBoxTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Enable the sign-up button if the checkbox is checked
            buttonSignUp.setEnabled(isChecked);
        });

        // Click listener to the sign-up button
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the input values
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String passwordRetype = editTextPasswordRetype.getText().toString().trim();

                if(password.equals(passwordRetype) && email.contains("@")){
                    // Use the ApiClient to sign up
                    ApiService.getInstance().userRegister(email, password, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(() ->
                                    Toast.makeText(SignUpActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String responseBody = response.body().string();
                                runOnUiThread(() ->
                                        Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show()
                                );
                                // Navigate to the Login page
                                SharedPrefsManager.getInstance(SignUpActivity.this).saveEmail(email);
                                Intent intent = new Intent(SignUpActivity.this, VerificationCodeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                String responseBody = response.body().string();
                                try {
                                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                                    String message = jsonObject.get("detail").getAsString(); //get reply error
                                    runOnUiThread(() ->
                                            Toast.makeText(SignUpActivity.this, "Registration failed: " + message, Toast.LENGTH_SHORT).show()
                                    );
                                } catch (JsonSyntaxException e){
                                    runOnUiThread(() ->
                                            Toast.makeText(SignUpActivity.this, "Registration failed: " + responseBody, Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(SignUpActivity.this, "Registration unsuccessful - retype password or email", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Back to main button
        buttonBackToMain = findViewById(R.id.buttonBackToMain);
        buttonBackToMain.setOnClickListener(view -> {
            finish();
        });
    }

    /**
     * method for showing terms and conditions and it also operates checkbox
     */
    private void showTermsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms and Conditions");

        //here is our terms and conditions
        String termsAndConditions = "Terms and Conditions\n\n" +
                "Last Updated: 22.04.2024\n\n" +
                "Welcome to OTC File Transfer System! By using our app, you agree to the following terms and conditions. Please read them carefully.\n\n" +
                "1. Acceptance of Terms\n" +
                "By accessing or using our mobile application, you agree to be bound by these terms and conditions (the \"Terms\") and our Privacy Policy. If you do not agree to these terms, please do not use our application.\n\n" +
                "2. Description of Service\n" +
                "OTC File Transfer System provides users with the ability to send and manage files across various storage solutions and email services. Users can upload files, send them to designated disks or email addresses, and manage their storage directly from the app.\n\n" +
                "3. Privacy Policy\n" +
                "Your privacy is important to us. Our Privacy Policy explains how we collect, use, and protect your personal information. Please refer to our Privacy Policy for more information.\n\n" +
                "4. User Responsibilities\n" +
                "You are responsible for maintaining the confidentiality of your account and password and for restricting access to your computer, and you agree to accept responsibility for all activities that occur under your account or password. You agree to use OTC File Transfer System only for lawful purposes and in a way that does not infringe the rights of, restrict or inhibit anyone else's use and enjoyment of the app.\n\n" +
                "5. Intellectual Property Rights\n" +
                "The content, arrangement and layout of this app, including but not limited to, the text, graphics, images, logos, and videos, are the property of OTC File Transfer System and are protected by copyright and other intellectual property laws. You are not permitted to reproduce, transmit, or distribute the materials contained within the app without our prior permission.\n\n" +
                "6. Termination\n" +
                "We may terminate your access to our app, without cause or notice, which may result in the forfeiture and destruction of all information associated with your account. All provisions of the Terms that by their nature should survive termination shall survive termination, including, without limitation, ownership provisions, warranty disclaimers, indemnity, and limitations of liability.\n\n" +
                "7. Changes to Terms\n" +
                "We reserve the right, at our sole discretion, to modify or replace these Terms at any time. If the alterations constitute a material change to the Terms, we will notify you by posting an updated version on this page.\n\n" +
                "Contact Us\n" +
                "If you have any questions about these Terms, please contact us at wbronisz@student.agh.edu.pl.";


        builder.setMessage(termsAndConditions);
        builder.setPositiveButton("Accept", (dialog, which) -> {
            checkBoxTerms.setChecked(true);
        });
        builder.setNegativeButton("Decline", (dialog, which) -> {
            checkBoxTerms.setChecked(false);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
