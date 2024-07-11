package com.example.otc;

import static java.lang.Boolean.TRUE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * HomePageActivity is most important class it the hearth of app it branches on other fragments
 * provides menu which is used to communicate through app, loads default fragment
 * uses activity_main_page.xml, drawer_menu.xml, nav_header.xml -> are for front-end of menu and background
 * to change colors etc use .xml and mainly colors.xml + themes
 */
public class HomePageActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Context context = this;
    TextView textViewName;
    private BiometricLogin biometricLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);

        textViewName = headerView.findViewById(R.id.textViewMail);
        textViewName.setText(SharedPrefsManager.getInstance(this).getEmail());

        biometricLogin = new BiometricLogin(getApplicationContext(), this);

        if (SharedPrefsManager.getInstance(getApplicationContext()).isPinSet()) {
            if (SharedPrefsManager.getInstance(getApplicationContext()).isBiometricEnabled()) {
                if (BiometricUtils.isBiometricAuthAvailable(getApplicationContext())) {
                    BiometricPrompt biometricPrompt = new BiometricPrompt(HomePageActivity.this, ContextCompat.getMainExecutor(getApplicationContext()), new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(getApplicationContext(), "Authentication Error", Toast.LENGTH_SHORT).show();
                            enterPIN();
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Toast.makeText(getApplicationContext(), "Authentication succeeded", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(getApplicationContext(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                            enterPIN();
                        }
                    });

                    BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Biometric login for my app")
                            .setSubtitle("Log in using your biometric credential")
                            .setNegativeButtonText("Use PIN")
                            .build();

                    biometricPrompt.authenticate(promptInfo);
                }
                else {
                    Toast.makeText(getApplicationContext(),"No biometric method available", Toast.LENGTH_SHORT).show();
                    enterPIN();
                }
            } else {
                enterPIN();
            }
        }

        boolean firstStart = SharedPrefsManager.getInstance(getApplicationContext()).isFirstStart();
        if (firstStart){
            if (isSystemDarkModeSupported()) {
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Dark mode is supported on this device.", Toast.LENGTH_SHORT).show()
                );
            } else {
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Dark mode is not supported on this device.", Toast.LENGTH_SHORT).show()
                );
            }
            SharedPrefsManager.getInstance(getApplicationContext()).setFirstStart(false);
        }

        ApiService.getInstance().authExistsAny(SharedPrefsManager.getInstance(getApplicationContext()).getSessionId(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("One Drive:","No One Drive account linked");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful())
                {
                    String responseBody = response.body().string();
                    // if email is verified create session
                    if (Boolean.parseBoolean(responseBody)){
                        String[] scopes = {"Files.ReadWrite","Files.ReadWrite.All","openid","profile","User.Read"};
                        OneDriveAPI one_drive=new OneDriveAPI(getApplicationContext());
                        one_drive.acquireTokenSilent(scopes, new AuthenticationCallback() {
                            @Override
                            public void onCancel() {
                                Toast.makeText(getApplicationContext(), "OneDrive silent token acquisition canceled", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(IAuthenticationResult authenticationResult) {
                                String accessToken = authenticationResult.getAccessToken();
                                // Use the access token as needed

                                // Optionally: Save the access token or perform further actions
                                ApiService.getInstance().UpdateOneDrive(SharedPrefsManager.getInstance(getApplicationContext()).getSessionId(), accessToken, new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        Log.d("Failed to update One Drive access token:", e.getMessage());
                                        Toast.makeText(getApplicationContext(), "Failed to link One Drive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        Log.d("One Drive token updated Successfully", responseBody);
                                    }
                                });
                            }

                            @Override
                            public void onError(MsalException exception) {
                                exception.printStackTrace();
                                Log.e("MSAL_CLIENT_ERROR", "Client error: " + exception.getMessage());

                                if (exception.getErrorCode().equals(MsalClientException.DEVICE_NETWORK_NOT_AVAILABLE)) {
                                    Log.e("MSAL_CLIENT_ERROR", "Device network not available.");
                                } else if (exception.getErrorCode().equals(MsalClientException.NO_CURRENT_ACCOUNT)) {
                                    Log.e("MSAL_CLIENT_ERROR", "No current account found.");
                                } else if (exception.getErrorCode().equals(MsalClientException.INVALID_PARAMETER)) {
                                    Log.e("MSAL_CLIENT_ERROR", "Invalid parameter.");
                                } else {
                                    Log.e("MSAL_CLIENT_ERROR", "Other client error: " + exception.getErrorCode());
                                }
                            }
                        });
                    }
                }else{
                    String responseBody = response.body().string();
                    Log.d("Failed to acquire assigned One Drive accounts form database: ", responseBody);
                    //Toast.makeText(getApplicationContext(), "Failed to link One Drive: " + responseBody, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (isSystemDarkModeEnabled(getApplicationContext())) {
            FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
            fragmentContainer.setBackgroundResource(R.drawable.dark_mode_background);
            toolbar.setBackgroundColor(getResources().getColor(R.color.black));

            ThemeUtils.getInstance(getApplicationContext()).setDarkTheme(TRUE);
            ThemeUtils.getInstance(getApplicationContext()).applyTheme(this);
        }

        // Here is setting the OTC color and title on main page (if we wont do this the OTC will be automatically added idk how and where and has android:textColorPrimary as its color
        // so it is better to do it by ourself in order to change color of title
        // it doesnt work when changed in .xml in toolbar settings idk why so im doing it in code
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name); // Safely set the title
        }
        int titleColor = ContextCompat.getColor(this, R.color.title_OTC_color); //get color form values/colors.xml
        toolbar.setTitleTextColor(titleColor); //set color of title


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(getApplicationContext(), R.color.iconColor));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        /**
         * here add to menu, to sidebar functionality,
         * so you need to add another func in drawer_menu.xml (name id icon)
         * next create fragment_func.xml there whole front-end or just some func like in logout just logout and navigate to other activity
         * next create java file funcFragment and there back-end it needs have same structure as other Fragments
         * at last add here another if condition and similar to other func using function replaceFragment
         */
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_account) {
                replaceFragment(new AccountFragment());
            } else if (id == R.id.nav_settings) {
                // Implement settings fragment or another activity
                replaceFragment(new SettingsFragment());
            } else if (id == R.id.nav_logout) {
                SharedPrefsManager.getInstance(context).logOut(); //clear token logout
                Intent intent = new Intent(HomePageActivity.this, SignMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (id == R.id.nav_history) {
                replaceFragment(new Transfer_History());
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Load the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private boolean isSystemDarkModeSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    private boolean isSystemDarkModeEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int currentNightMode = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
        return false; // System dark mode is not supported on versions prior to Android 10
    }

    protected void enterPIN() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_enter_pin, null);
        builder.setView(dialogView);

        EditText editTextPin = dialogView.findViewById(R.id.editTextPin);

        AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        // Listen for Enter key press event on the EditText
        editTextPin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    String enteredPin = editTextPin.getText().toString();
                    // Validate the entered PIN using SharedPrefsManager
                    if (validatePin(enteredPin)) {
                        dialog.dismiss();
                        // Proceed with app functionality
                        Toast.makeText(HomePageActivity.this, "PIN Correct", Toast.LENGTH_SHORT).show();
                    } else if (enteredPin.length() < 6) {
                        editTextPin.setError("PIN is too short");
                        editTextPin.requestFocus();
                    } else {
                        editTextPin.setError("Incorrect PIN");
                        editTextPin.requestFocus();
                    }
                    return true; // Consume the event
                }
                return false; // Return false to proceed with default handling
            }
        });

        dialog.show();
    }

    private boolean validatePin(String enteredPin) {
        // Implement your PIN validation logic here
        // Compare enteredPin with the stored PIN
        String storedPin = SharedPrefsManager.getInstance(getApplicationContext()).getStoredPin();
        return enteredPin.equals(storedPin);
    }

    // function which helps with replacing fragments through menu
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}