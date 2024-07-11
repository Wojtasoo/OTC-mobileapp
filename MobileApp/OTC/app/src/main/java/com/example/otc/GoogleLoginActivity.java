package com.example.otc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;

public class GoogleLoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(view -> signIn());
        findViewById(R.id.buttonBackToMain).setOnClickListener(view -> finish());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed
                Log.w("GoogleLoginActivity", "Google sign in failed", e);
                Toast.makeText(GoogleLoginActivity.this, "Google sign in failed"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("GoogleLoginActivity", "firebaseAuthWithGoogle:" + account.getId());
        String token_ID=account.getIdToken();
        String email=account.getEmail();

        ApiService.getInstance().userRegister_Google(token_ID, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                runOnUiThread(() ->
                        Toast.makeText(GoogleLoginActivity.this, "Sign In Unsuccessful: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle success
                // Redirect to main page after successful registration
                if(response.isSuccessful())
                {
                    runOnUiThread(() ->
                            Toast.makeText(GoogleLoginActivity.this, "Sign In Successful: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                    JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    String sessionID = jsonObject.get("sessionId").getAsString();
                    SharedPrefsManager.getInstance(GoogleLoginActivity.this).setLogin(sessionID, email);
                    Intent intent = new Intent(GoogleLoginActivity.this, HomePageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Close the current activity
                }
                else
                {
                    Log.d("Error:",response.body().string());
                    runOnUiThread(() ->
                            Toast.makeText(GoogleLoginActivity.this, "Sign In Failed: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                    finish();
                }
            }
        });
    }
}

