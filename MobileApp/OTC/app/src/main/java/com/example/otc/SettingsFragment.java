package com.example.otc;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * SettingsFragment shows settings for user
 * uses fragment_settings
 */
public class SettingsFragment extends Fragment {

    Button buttonLinkEmail, buttonLinkGoogleDrive, buttonLinkOneDrive, buttonShowStorages, buttonDeleteAccount,Pinsettings,buttonBiometric;
    List<Authentication> authentications;
    private OneDriveAPI mOneDriveAPI;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mOneDriveAPI = new OneDriveAPI(requireContext());

        // Initialize views
        buttonLinkEmail = view.findViewById(R.id.buttonLinkEmail);
        buttonLinkGoogleDrive = view.findViewById(R.id.buttonLinkGoogleDrive);
        buttonLinkOneDrive = view.findViewById(R.id.buttonLinkOneDrive);
        buttonShowStorages = view.findViewById(R.id.buttonShowStorages);
        Pinsettings=view.findViewById(R.id.buttonPin);
        buttonBiometric=view.findViewById(R.id.buttonBiometric);
        buttonDeleteAccount = view.findViewById(R.id.buttonDeleteAccount);

        checkPinAndBiometricSupport();

        buttonLinkEmail.setOnClickListener(v -> linkEmail());
        buttonLinkGoogleDrive.setOnClickListener(v -> linkGoogleDrive());
        buttonLinkOneDrive.setOnClickListener(v -> {
            try {
                linkOneDrive();
            } catch (MsalClientException e) {
                Toast.makeText(requireContext(), "Failed to link OneDrive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        buttonShowStorages.setOnClickListener(v -> showStorages());
        buttonDeleteAccount.setOnClickListener(v -> deleteAccount());
        Pinsettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start NewFragment
                startNewFragment();
            }
        });
        buttonBiometric.setOnClickListener(v->{
            // Create an Intent to start your target activity
            BiometricLogin biometric= new BiometricLogin(requireContext(),requireActivity());
            biometric.toggleBiometricAuthentication(buttonBiometric);
        });
        buttonBiometric.setText(SharedPrefsManager.getInstance(requireContext()).isBiometricEnabled() ? "Disable Biometric" : "Enable Biometric");

        return view;
    }

    // Method to handle linking email functionality
    private void linkEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Link Email");
        builder.setMessage("Please enter your email address");

        // Set up the input fields in the dialog
        final EditText inputEmail = new EditText(requireContext());
        builder.setView(inputEmail);

        // Set up the buttons for entering email
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = inputEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Call API to check if email is already linked and it should send code on email
                ApiService.getInstance().authRegister(SharedPrefsManager.getInstance(requireContext()).getSessionId(), AuthType.Email, email, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            requireActivity().runOnUiThread(() -> showEnterCodeDialog(email));
                        } else {
                            String responseBody = response.body().string();
                            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                            String message = jsonObject.get("detail").getAsString();
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Failed to link email: " + message, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog to enter email
        builder.show();
    }

    private void showEnterCodeDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Verify Email");
        builder.setMessage("Please enter the verification code sent to " + email);

        // Set up the input field for code
        final EditText inputCode = new EditText(requireContext());
        builder.setView(inputCode);

        // Set up the buttons for entering code
        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String code = inputCode.getText().toString().trim();

                ApiService.getInstance().authVerifyEmail(SharedPrefsManager.getInstance(requireContext()).getSessionId(), email, code, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Email linked successfully", Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            String responseBody = response.body().string();
                            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                            String message = jsonObject.get("detail").getAsString();
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Failed to link email: " + message, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog to enter verification code
        builder.show();
    }


    private void linkGoogleDrive() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GoogleDriveService googleDriveService = new GoogleDriveService();
        fragmentTransaction.add(googleDriveService, "GoogleDriveService");
        fragmentTransaction.commit();
    }

    private void linkOneDrive() throws MsalClientException {
        String[] scopes = {"Files.ReadWrite","Files.ReadWrite.All","openid","profile","User.Read"};

        // Perform OneDrive sign-in
        mOneDriveAPI.signIn(requireActivity(), scopes, new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                // Successfully signed in, use authenticationResult.getAccessToken() to access OneDrive
                String accessToken = authenticationResult.getAccessToken();
                // Use the access token as needed

                // Optionally: Save the access token or perform further actions
                ApiService.getInstance().authRegister(SharedPrefsManager.getInstance(requireContext()).getSessionId(), AuthType.OneDrive, accessToken, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("Failed to link One Drive:", e.getMessage());
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Failed to link One Drive: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //response is unsuccessful
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            Log.d("One Drive Linked Successfully", responseBody);
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "One Drive Linked Successfully" + responseBody, Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            String responseBody = response.body().string();
                            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                            String message = jsonObject.get("detail").getAsString();
                            Log.d("Failed to link One Drive: ", responseBody);
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Failed to link One Drive: " + message, Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            }

            @Override
            public void onError(MsalException exception) {
                // Handle error
                Toast.makeText(requireContext(), "Failed to link OneDrive: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
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

            @Override
            public void onCancel() {
                // User canceled sign-in
                Toast.makeText(requireContext(), "OneDrive sign-in canceled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStorages() {
        // Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Storage Authentication");

        // Initialize list view and adapter
        ListView listView = new ListView(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_single_choice, new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Fetch storage authentications from API
        ApiService.getInstance().authGetBySession(SharedPrefsManager.getInstance(requireContext()).getSessionId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to get storage authentications: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Authentication>>(){}.getType();
                    authentications = gson.fromJson(response.body().string(), listType);

                    requireActivity().runOnUiThread(() -> {
                        adapter.clear();
                        if (!authentications.isEmpty()) {
                            for (Authentication auth : authentications) {
                                adapter.add(auth.toString()); // Adjust as per your Authentication object structure
                            }
                        } else {
                            Toast.makeText(requireContext(), "No storage authentications available", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    String responseBody = response.body().string();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to get storage authentications: " + responseBody, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        builder.setView(listView);

        // Add Delete and Cancel buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = listView.getCheckedItemPosition();
                if (position != ListView.INVALID_POSITION) {
                    Authentication selectedAuthentication = authentications.get(position);
//                    String selectedAuthentication = adapter.getItem(position);

                    // Call API to delete the selected authentication
                    ApiService.getInstance().authDelete(SharedPrefsManager.getInstance(requireContext()).getSessionId(), selectedAuthentication.getAuthID(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Failed to delete authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Authentication deleted successfully", Toast.LENGTH_SHORT).show()
                                );
                            } else {
                                String responseBody = response.body().string();
                                try {
                                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                                    String message = jsonObject.get("detail").getAsString(); //get reply error
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(), "Failed to delete: " + message, Toast.LENGTH_SHORT).show()
                                    );
                                } catch (JsonSyntaxException e){
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(), "Failed to delete: " + responseBody, Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Please select an authentication to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.show();
    }

    // Method to handle deleting account
    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Account Deletion");

        // Inflate the custom layout view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_account, null);
        builder.setView(dialogView);

        // Get references to EditText fields in the custom layout
        EditText inputConfirm = dialogView.findViewById(R.id.inputConfirm);

        // Set up the buttons for confirmation
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String confirm = inputConfirm.getText().toString().trim();
                final String CONFIRM = "CONFIRM";

                // Check if the entered confirmation matches
                if (CONFIRM.equals(confirm)) {
                    ApiService.getInstance().accountDelete(SharedPrefsManager.getInstance(requireContext()).getSessionId(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // Handle failure
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                // Handle success
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                );
                                // Redirect to Sign In Sign UP etc
                                Intent intent = new Intent(requireContext(), SignMainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                String responseBody = response.body().string();
                                try {
                                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                                    String message = jsonObject.get("detail").getAsString(); //get reply error
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(), "Account delete failed: " + message, Toast.LENGTH_SHORT).show()
                                    );
                                } catch (JsonSyntaxException e){
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(), "Account delete failed: " + responseBody, Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Misspelling \"CONFIRM\". Account not deleted.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }

    private void startNewFragment() {
        // Create new fragment instance
        PinFragment newFragment = new PinFragment();

        // Get FragmentManager and start transaction
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null); // Optional: Add transaction to back stack

        // Commit the transaction
        transaction.commit();
    }

    private void checkPinAndBiometricSupport() {
        if (SharedPrefsManager.getInstance(requireContext()).isPinSet()) {
            buttonBiometric.setVisibility(BiometricUtils.isBiometricAuthAvailable(requireContext()) ? View.VISIBLE : View.GONE);
        }
    }
}
