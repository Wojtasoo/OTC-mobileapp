package com.example.otc;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.DriveScopes;
import com.google.android.gms.common.api.Scope;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GoogleDriveService extends Fragment {

    private GoogleSignInClient mGoogleSignInClient;
    public static final int RC_SIGN_IN = 1001;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureGoogleSignIn();
        signIn();
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestServerAuthCode(getString(R.string.web_client_id), true)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    retrieveTokens(account.getServerAuthCode());
                }
            } catch (ApiException e) {
                Toast.makeText(requireContext(), "Sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void retrieveTokens(String authCode) {
        new RetrieveTokensTask().execute(authCode);
    }

    private class RetrieveTokensTask extends AsyncTask<String, Void, GoogleTokenResponse> {
        @Override
        protected GoogleTokenResponse doInBackground(String... authCodes) {
            try {
                GoogleAuthorizationCodeTokenRequest tokenRequest = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        new GsonFactory(),
                        "https://oauth2.googleapis.com/token",
                        getString(R.string.web_client_id),  // Use the actual string resource
                        getString(R.string.client_secret), // Use the actual string resource
                        authCodes[0],
                        ""
                );
                return tokenRequest.execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(GoogleTokenResponse tokenResponse) {
            if (tokenResponse != null) {
                String refreshToken = tokenResponse.getRefreshToken();

                ApiService.getInstance().authRegister(SharedPrefsManager.getInstance(requireContext()).getSessionId(), AuthType.GoogleDrive, refreshToken, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("Failed to link Google Drive:", e.getMessage());
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Failed to link Google Drive: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //response is unsuccessful
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            Log.d("Google Drive Linked Successfully", responseBody);
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Google Drive Linked Successfully" + responseBody, Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            String responseBody = response.body().string();
                            Log.d("Failed to link Google Drive: ", responseBody);
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Failed to link Google Drive: " + responseBody, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });

            } else {
                Toast.makeText(requireContext(), "Failed to retrieve tokens", Toast.LENGTH_SHORT).show();
            }
        }
    }
}