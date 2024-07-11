package com.example.otc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DeletePinFragment extends DialogFragment {

    private PinDeleteListener pinDeleteListener;

    public interface PinDeleteListener {
        void onPinDeleteConfirmed();
    }

    public void setPinDeleteListener(PinDeleteListener listener) {
        this.pinDeleteListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Delete PIN")
                .setMessage("Are you sure you want to remove the PIN?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        pinDeleteListener.onPinDeleteConfirmed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
