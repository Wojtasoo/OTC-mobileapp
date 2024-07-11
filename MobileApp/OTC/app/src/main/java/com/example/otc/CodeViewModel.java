package com.example.otc;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * CodeViewModel serves as a helper for HomeFragment, provides it with functionality calculating time, code etc
 */
public class CodeViewModel extends ViewModel {

    public static long EXPIRE_TIME; //15 minutes, it is provided in initialize which gives us value from HomeFragment and this from res/values/constants.xml
    public static int EXPIRE_TIME_SECONDS; //15 minutes in seconds, it is provided in initialize which gives us value from HomeFragment and this from res/values/constants.xml
    private CountDownTimer countDownTimer;
    private MutableLiveData<Long> _timeRemaining = new MutableLiveData<>();
    private MutableLiveData<String> _currentCode = new MutableLiveData<>();

    // LiveData accessors that the UI will observe
    public LiveData<Long> timeRemaining = _timeRemaining;
    public LiveData<String> currentCode = _currentCode;

    //in constructor default value is No Code before we even press button
    public CodeViewModel() {
        _currentCode.setValue("No Code");
    }

    //provides functionality of calculating time and changes code when it finishes counting
    public void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(_timeRemaining.getValue(), 1000) {
            public void onTick(long millisUntilFinished) {
                _timeRemaining.setValue(millisUntilFinished);
            }


            public void onFinish() {
                _currentCode.setValue("No Code");
            }
        };
        countDownTimer.start();
    }

    // functionality of generating new code
    // here get code from API and setValue to that code,
    //  and maybe get time from API or DB or by default ? (now is by default)
    public void generateNewCode(Context context) {
        ApiService.getInstance().codeAssign(SharedPrefsManager.getInstance(context).getSessionId(), new Callback() {
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
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    String code = jsonObject.get("code").getAsString();

                    // Post value to ensure it is set on the main thread
                    _currentCode.postValue(code);
                    _timeRemaining.postValue(EXPIRE_TIME); // Reset timer to 15 minutes

                    // Start the timer on the main thread
                    new Handler(Looper.getMainLooper()).post(CodeViewModel.this::startTimer);
                } else {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    String message = jsonObject.get("detail").getAsString(); //get reply error
                    // Use a Handler to post the Toast on the main thread
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Generate code failed: " + message, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

    }

    // to clear clock
    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    /**
     * before it was hardcoded up as EXPIRE_TIME etc, now we have method
     * initialize which gets us those parameters from HomeFragment
     * @param EXPIRE_TIME it is 15min
     * @param EXPIRE_TIME_SECONDS it is 15min in seconds
     */
    public void initialize(long EXPIRE_TIME, int EXPIRE_TIME_SECONDS) {
        com.example.otc.CodeViewModel.EXPIRE_TIME = EXPIRE_TIME;
        com.example.otc.CodeViewModel.EXPIRE_TIME_SECONDS = EXPIRE_TIME_SECONDS;
    }

}
