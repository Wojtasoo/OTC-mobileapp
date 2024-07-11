package com.example.otc;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Transfer_History extends Fragment {

    private RecyclerView recyclerView;
    private TransferAdapter adapter;
    private List<TransferData> transferDataList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transfer_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        transferDataList = new ArrayList<>();
        adapter = new TransferAdapter(requireContext(),transferDataList);
        recyclerView.setAdapter(adapter);

        Context context = requireContext();
        ApiService.getInstance().getTransferHistory(SharedPrefsManager.getInstance(context).getSessionId(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Error loading history: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if(responseBody.isEmpty())
                    {
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Error: " + "Null object returned. Empty response from server", Toast.LENGTH_SHORT).show()
                        );
                        response.close();
                    }
                    Log.d("JSON: ", responseBody);
                    try {
                        parseTransferData(responseBody);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            adapter.notifyDataSetChanged();
                        });
                    } catch (JSONException e) {
                        Log.d("Error parsing JSON: ", e.getMessage());
                    }
                } else {
                    String responseBody = response.body().string();
                    //Log.d("JSON: ", responseBody);
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    String message = jsonObject.get("detail").getAsString(); //get reply error
                    // Use a Handler to post the Toast on the main thread
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Error: " + message, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        return view;
    }

    public void parseTransferData(String jsonString) throws JSONException {
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject transferObject = jsonArray.getJSONObject(i);

                String transferStartTimeUtc = transferObject.getString("transferStartTime");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date utcDateTime = dateFormat.parse(transferStartTimeUtc);
                dateFormat.setTimeZone(TimeZone.getDefault()); // Use system default time zone
                String transferStartTime = dateFormat.format(utcDateTime);
                String transferState = transferObject.getString("transferState");

                JSONArray authsArr = transferObject.getJSONArray("authentications");
                List<AuthenticationData> authList = new ArrayList<>();
                for (int j = 0; j < authsArr.length(); j++) {
                    JSONObject authObject = authsArr.getJSONObject(j);
                    int authTypeValue = authObject.getInt("authType");
                    String authType;
                    if (authTypeValue == AuthType.Email.getValue()) {
                        authType = AuthType.Email.name();
                    } else if (authTypeValue == AuthType.OneDrive.getValue()) {
                        authType = AuthType.OneDrive.name();
                    } else if (authTypeValue == AuthType.GoogleDrive.getValue()) {
                        authType = AuthType.GoogleDrive.name();
                    } else {
                        authType = String.valueOf(authTypeValue);
                    }
                    String displayName = authObject.getString("displayName");
                    authList.add(new AuthenticationData(authType, displayName));
                }

                JSONArray filesArray = transferObject.getJSONArray("files");
                List<FileData> fileList = new ArrayList<>();
                for (int j = 0; j < filesArray.length(); j++) {
                    String filename = filesArray.getJSONObject(j).getString("filename");
                    fileList.add(new FileData(filename));
                }

                transferDataList.add(new TransferData(transferStartTime, transferState, authList, fileList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}