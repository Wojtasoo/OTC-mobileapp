package com.example.otc;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * AccountFragment shows user account details and allows password change.
 * uses fragment_account.xml
 */
public class AccountFragment extends Fragment {

    TextView textViewEmail;
    EditText editTextOldPassword, editTextNewPassword, editTextRetypeNewPassword;
    Button buttonChangePassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialize views
        textViewEmail = view.findViewById(R.id.textViewEmail);
        editTextOldPassword = view.findViewById(R.id.editTextOldPassword);
        editTextNewPassword = view.findViewById(R.id.editTextNewPassword);
        editTextRetypeNewPassword = view.findViewById(R.id.editTextRetypeNewPassword);
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword);

        // Load user email from SharedPrefsManager (stored there with session id etc)
        textViewEmail.setText(SharedPrefsManager.getInstance(requireContext()).getEmail());

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the input values
                String oldPassword = editTextOldPassword.getText().toString().trim();
                String newPassword = editTextNewPassword.getText().toString().trim();
                String retypeNewPassword = editTextRetypeNewPassword.getText().toString().trim();
                Context context = requireContext();

                if(newPassword.equals(retypeNewPassword)){
                    if(!newPassword.equals(oldPassword)) {
                        ApiService.getInstance().userChangePassword(SharedPrefsManager.getInstance(context).getSessionId(), oldPassword, newPassword, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                // Use a Handler to post the Toast on the main thread
                                new Handler(Looper.getMainLooper()).post(() ->
                                        Toast.makeText(context, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    String responseBody = response.body().string();
                                    // Use a Handler to post the Toast on the main thread
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context, "Changed password successfully", Toast.LENGTH_SHORT).show()
                                    );
                                    editTextOldPassword.setText("");
                                    editTextNewPassword.setText("");
                                    editTextRetypeNewPassword.setText("");
                                } else {
                                    String responseBody = response.body().string();
                                    Log.d("Response:",responseBody);
                                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                                    String message = jsonObject.get("detail").getAsString(); //get reply error
                                    // Use a Handler to post the Toast on the main thread
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context, "Change password unsuccessful: " + message, Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                        });
                    } else {
                        Toast.makeText(context, "Unsuccessful - new password cannot be the same as the current password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Change password unsuccessful - retype correct password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
