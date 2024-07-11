package com.example.otc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;

public class OneDriveAPI {

    private ISingleAccountPublicClientApplication mSingleAccountApp;
    private static final String TAG = "OneDriveAPI";
    private String accessToken;

    public OneDriveAPI(Context context) {
        initializeMsal(context);
    }

    private void initializeMsal(Context context) {
        PublicClientApplication.createSingleAccountPublicClientApplication(
                context,
                R.raw.auth_config_single_account,
                new ISingleAccountPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(ISingleAccountPublicClientApplication application) {
                        mSingleAccountApp = application;
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.e(TAG, "Error creating MSAL application", exception);
                    }
                }
        );
    }

    public void signIn(Activity activity, String[] scopes, AuthenticationCallback callback) {
        if (mSingleAccountApp == null) {
            Log.e(TAG, "MSAL application is not initialized.");
            return;
        }

        mSingleAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @Override
            public void onAccountLoaded(IAccount activeAccount) {
                if (activeAccount != null) {
                    // Account is signed in, you can acquire token silently or proceed with actions
                    // that require a signed-in user.
                    acquireTokenSilent(scopes, callback);
                } else {
                    // No account signed in, you may need to start a sign-in flow
                    mSingleAccountApp.signIn(activity,null,scopes,callback);
                }
            }

            @Override
            public void onAccountChanged(IAccount priorAccount, IAccount currentAccount) {
                // Handle account change if needed
            }

            @Override
            public void onError(MsalException exception) {
                Log.e(TAG, "Failed to get current account: " + exception.getMessage());
                // Handle error
            }
        });
    }

    public void acquireTokenSilent(String[] scopes, AuthenticationCallback callback) {
        if (mSingleAccountApp == null) {
            Log.e(TAG, "MSAL application is not initialized.");
            return;
        }

        mSingleAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @Override
            public void onAccountLoaded(IAccount activeAccount) {
                if (activeAccount != null) {
                    mSingleAccountApp.acquireTokenSilentAsync(scopes, activeAccount.getAuthority(), new AuthenticationCallback() {
                        @Override
                        public void onSuccess(IAuthenticationResult authenticationResult) {
                            accessToken = authenticationResult.getAccessToken();
                            Log.d(TAG, "Silent token acquired. Access token: " + accessToken);
                            callback.onSuccess(authenticationResult);
                        }

                        @Override
                        public void onError(MsalException exception) {
                            Log.e(TAG, "Silent token acquisition failed: " + exception.getMessage());
                            callback.onError(exception);
                        }

                        @Override
                        public void onCancel() {
                            Log.d(TAG, "Silent token acquisition cancelled.");
                            callback.onCancel();
                        }
                    });
                } else {
                    Log.e(TAG, "No account signed in.");
                    callback.onError(new MsalClientException("No account signed in."));
                }
            }

            @Override
            public void onAccountChanged(IAccount priorAccount, IAccount currentAccount) {
                // Handle account change if needed
            }

            @Override
            public void onError(MsalException exception) {
                Log.e(TAG, "Failed to get current account: " + exception.getMessage());
                callback.onError(exception);
            }
        });
    }
}