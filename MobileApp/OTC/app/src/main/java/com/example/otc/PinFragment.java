package com.example.otc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.widget.Button;
import android.widget.Toast;

public class PinFragment extends Fragment implements DeletePinFragment.PinDeleteListener {

    private Button buttonPin, buttonChangePin, buttonDeletePin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pin_settings, container, false);

        // Initialize views
        buttonPin = rootView.findViewById(R.id.buttonPin);
        buttonChangePin = rootView.findViewById(R.id.buttonChangePin);
        buttonDeletePin = rootView.findViewById(R.id.buttonDeletePin);

        buttonPin.setOnClickListener(v -> SetPIN());
        buttonChangePin.setOnClickListener(v -> {
            if (SharedPrefsManager.getInstance(requireContext()).isPinSet()) {
                replaceFragment(new PinChange());
            } else {
                Toast.makeText(requireContext(), "PIN not set", Toast.LENGTH_SHORT).show();
            }
        });
        buttonDeletePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SharedPrefsManager.getInstance(requireContext()).isPinSet())
                {
                    showDeletePinConfirmationDialog();
                }
                else {
                    Toast.makeText(requireContext(), "PIN not set", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }
    private void SetPIN()
    {
        if(SharedPrefsManager.getInstance(requireContext()).isPinSet())
        {
            Toast.makeText(requireContext(),"Pin is already set",Toast.LENGTH_SHORT).show();
        }
        else{
            BiometricLogin setthepin= new BiometricLogin(requireContext(),requireActivity());
            setthepin.showSetPinDialog();
        }
    }

    private void replaceFragment(Fragment fragment) {
        if (getActivity() == null) return;
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Optional: Add transaction to back stack
        transaction.commit();
    }

    private void showDeletePinConfirmationDialog() {
        DeletePinFragment dialog = new DeletePinFragment();
        dialog.setPinDeleteListener(this); // Set PinDeleteListener in DeletePinFragment

        // Show the dialog using getParentFragmentManager()
        dialog.show(requireActivity().getSupportFragmentManager(), "DeletePinConfirmationDialogFragment");
    }

    @Override
    public void onPinDeleteConfirmed() {
        // Handle PIN deletion using SharedPrefsManager
        SharedPrefsManager.getInstance(requireContext()).deletePin();

        // Optionally, clear UI fields or update UI as needed after deleting PIN
        Toast.makeText(requireContext(), "PIN deleted successfully", Toast.LENGTH_SHORT).show();

        // Optionally, close this fragment after PIN deletion
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}

