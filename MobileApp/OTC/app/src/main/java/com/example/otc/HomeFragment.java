package com.example.otc;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

/**
 * HomeFragment is responsible for keeping up to date code and time display even when we exit that fragment
 * it has clear only display purposes, uses CodeViewModel which provides backend for calculation and changing time generating code
 * uses fragment_home.xml
 */
public class HomeFragment extends Fragment {
    CodeViewModel viewModel;
    Button buttonGenerateCode;
    TextView textViewCode, textViewTimer;
    ProgressBar progressBarTimer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // Get the ViewModel from the activity context
        viewModel = new ViewModelProvider(requireActivity()).get(CodeViewModel.class);

        //it is for initializing HomeViewModel we provide time from res/values/constants.xml to not hard code it
        //we cannot use getResources() in HomeViewModel so we need to do it here and parse it to HomeViewModel
        long EXPIRE_TIME = Long.parseLong(getResources().getString(R.string.expire_time));; //15 minutes
        int EXPIRE_TIME_SECONDS = getResources().getInteger(R.integer.expire_time_seconds); //15 minutes in seconds
        viewModel.initialize(EXPIRE_TIME, EXPIRE_TIME_SECONDS);


        textViewTimer = view.findViewById(R.id.textViewTimer);
        buttonGenerateCode = view.findViewById(R.id.buttonGenerateCode);
        textViewCode = view.findViewById(R.id.textViewCode);
        progressBarTimer = view.findViewById(R.id.progressBarTimer);

        buttonGenerateCode.setOnClickListener(v -> viewModel.generateNewCode(requireContext()));

        setupObservers();

        return view;
    }

    private void setupObservers() {
        viewModel.currentCode.observe(getViewLifecycleOwner(), code -> textViewCode.setText(code));
        viewModel.timeRemaining.observe(getViewLifecycleOwner(), timeRemaining -> {
            if (timeRemaining != null) {
                updateTimerDisplay(timeRemaining);
            }
        });
    }

    private void updateTimerDisplay(long timeRemaining) {
        long minutes = (timeRemaining / 1000) / 60;
        long seconds = (timeRemaining / 1000) % 60;
        textViewTimer.setText(String.format("%02d:%02d", minutes, seconds));
        int progress = (int) (timeRemaining / 1000);
        progressBarTimer.setProgress(progress);

        // Update color based on percentage
        updateProgressBarColor(progressBarTimer, progress, com.example.otc.CodeViewModel.EXPIRE_TIME_SECONDS);
    }

    // just method which provides colors for progress bar based on % of time left
    private void updateProgressBarColor(ProgressBar progressBar, int progress, int max) {
        int percentage = (progress * 100) / max;
        int color = ContextCompat.getColor(requireContext(), R.color.circle_ring_color); // Default color for progress
        if (percentage <= 10) {
            color = Color.RED; // Less than or equal to 10%
        } else if (percentage <= 50) {
            color = Color.YELLOW; // Less than or equal to 50%
        }

        // Update the progress drawable color
        LayerDrawable drawable = (LayerDrawable) progressBar.getProgressDrawable();
        GradientDrawable progressDrawable = (GradientDrawable) ((RotateDrawable) drawable.findDrawableByLayerId(android.R.id.progress)).getDrawable();
        progressDrawable.setColor(color);
        progressBar.setProgressDrawable(drawable);
    }

}




