package com.example.otc;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PinChange extends Fragment {

    private EditText editTextCurrentPin;
    private EditText editTextNewPin;
    private EditText editTextConfirmPin;
    private Button buttonChangePin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_change_pin, container, false);

        editTextCurrentPin = rootView.findViewById(R.id.editTextCurrentPin);
        editTextNewPin = rootView.findViewById(R.id.editTextNewPin);
        editTextConfirmPin = rootView.findViewById(R.id.editTextConfirmPin);
        buttonChangePin = rootView.findViewById(R.id.buttonChangePin);

        buttonChangePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SharedPrefsManager.getInstance(requireContext()).isPinSet())
                {
                    validateAndChangePin();
                }else{
                    Toast.makeText(requireContext(), "PIN not set", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private void validateAndChangePin() {
        String currentPin = editTextCurrentPin.getText().toString();
        String newPin = editTextNewPin.getText().toString();
        String confirmPin = editTextConfirmPin.getText().toString();

        // Check if current PIN matches
        if (!TextUtils.isEmpty(currentPin) && currentPin.equals(SharedPrefsManager.getInstance(requireContext()).getStoredPin())) {
            // Check if new PIN is different from current PIN and is exactly 6 digits long
            if (!newPin.equals(currentPin) && newPin.length() == 6) {
                // Check if new PIN matches confirmation
                if (newPin.equals(confirmPin)) {
                    SharedPrefsManager.getInstance(requireContext()).savePin(newPin);
                    SharedPrefsManager.getInstance(requireContext()).savePinSet(true);
                    Toast.makeText(requireContext(), "PIN changed successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, clear the fields after successful change
                    editTextCurrentPin.getText().clear();
                    editTextNewPin.getText().clear();
                    editTextConfirmPin.getText().clear();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), "New PINs don't match", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "New PIN must be different and 6 digits long", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Current PIN is incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}
