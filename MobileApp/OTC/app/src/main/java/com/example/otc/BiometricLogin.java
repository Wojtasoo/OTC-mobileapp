package com.example.otc;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricLogin extends AppCompatActivity {

    private static final String TAG = "BiometricLogin";
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Context context;
    private SharedPrefsManager prefsManager;
    private FragmentActivity activity;
    public static final int REQUEST_CODE_PIN = 1001;

    public BiometricLogin(){
        this.context = null; // Or optionally set a default context
        this.prefsManager = null;
        this.executor = null; // Initialize with appropriate executor
    }

    public BiometricLogin(Context context, FragmentActivity active) {
        this.context = context;
        this.activity = active;
        this.prefsManager = SharedPrefsManager.getInstance(context);
        this.executor = ContextCompat.getMainExecutor(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.context == null) {
            this.context = getApplicationContext();
            this.prefsManager = SharedPrefsManager.getInstance(context);
            this.executor = ContextCompat.getMainExecutor(context);
        }

        setContentView(R.layout.fragment_settings);
    }

    protected void showSetPinDialog() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_set_pin);

        EditText editTextPin = dialog.findViewById(R.id.editTextPin);
        EditText editTextConfirmPin = dialog.findViewById(R.id.editTextConfirmPin);
        Button buttonSetPin = dialog.findViewById(R.id.buttonSetPin);

        buttonSetPin.setOnClickListener(v -> {
            String pin = editTextPin.getText().toString();
            String confirmPin = editTextConfirmPin.getText().toString();

            if (pin.length() == 6 && pin.equals(confirmPin)) {
                savePin(pin);
                editTextPin.setText("");
                editTextConfirmPin.setText("");
                Toast.makeText(context, "PIN set successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(context, "PINs do not match or are not 6 digits", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        //finish();
    }

    private void savePin(String pin) {
        if (prefsManager != null) {
            prefsManager.savePin(pin);
            prefsManager.savePinSet(true);
        } else {
            Log.e(TAG, "SharedPrefsManager instance is null");
        }
    }

    public void authenticateWithBiometric(Button buttonBiometric) {
        if (BiometricUtils.isBiometricAuthAvailable(context)) {
            biometricPrompt = new BiometricPrompt(activity , executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(context, "Authentication succeeded", Toast.LENGTH_SHORT).show();
                    SharedPrefsManager.getInstance(context).setBiometricEnabled(true);
                    buttonBiometric.setText("Disable Biometric");
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for my app")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Use PIN")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        }
        else {
            Toast.makeText(getApplicationContext(),"No biometric method available", Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleBiometricAuthentication(Button buttonBiometric) {
        if (prefsManager != null && context != null) { // Check if prefsManager and context are not null
            if (prefsManager.isBiometricEnabled()) {
                // If biometric is enabled, disable it
                prefsManager.setBiometricEnabled(false);
                buttonBiometric.setText("Enable Biometric");
                Toast.makeText(context, "Biometric authentication disabled", Toast.LENGTH_SHORT).show();
            } else {
                // If biometric is disabled, enable it
                authenticateWithBiometric(buttonBiometric);
            }
        } else {
            Log.e(TAG, "prefsManager or context is null");
        }
    }

    private void startPinActivity() {
        Intent intent = new Intent(this, PinFragment.class);
        startActivityForResult(intent, REQUEST_CODE_PIN);
    }
}

class BiometricUtils extends AppCompatActivity {

    public static boolean isBiometricAuthAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
            default:
                return false;
        }
    }
}