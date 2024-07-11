package com.example.otc;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransferAdapter extends RecyclerView.Adapter<TransferAdapter.TransferViewHolder> {

    private List<TransferData> transferDataList;
    private Context context;

    public TransferAdapter(Context context, List<TransferData> transferDataList) {
        this.context = context;
        this.transferDataList = transferDataList;
    }

    @NonNull
    @Override
    public TransferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfer, parent, false);
        return new TransferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransferViewHolder holder, int position) {
        TransferData transferData = transferDataList.get(position);
        holder.tvTransferStartTime.setText(transferData.getTransferStartTime());
        holder.tvTransferState.setText(transferData.getTransferState());

        int textColor = isSystemDarkModeEnabled(context) ? R.color.white : R.color.black;
        holder.tvTransferStartTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), textColor));
        holder.tvTransferState.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), textColor));

        holder.llAuthentications.removeAllViews();
        for (AuthenticationData auth : transferData.getAuthentications()) {
            TextView authTypeTextView = new TextView(holder.itemView.getContext());
            authTypeTextView.setText(String.format("%s: %s", auth.getAuthType(), auth.getDisplayName()));
            authTypeTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), textColor));
            holder.llAuthentications.addView(authTypeTextView);
        }

        holder.llFiles.removeAllViews();
        for (FileData file : transferData.getFiles()) {
            TextView textView = new TextView(holder.itemView.getContext());
            textView.setText(file.getFilename());
            textView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), textColor));
            holder.llFiles.addView(textView);
        }
    }

    @Override
    public int getItemCount() {
        return transferDataList.size();
    }

    public static class TransferViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransferStartTime, tvTransferState;
        LinearLayout llAuthentications, llFiles;

        public TransferViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransferStartTime = itemView.findViewById(R.id.tvTransferStartTime);
            tvTransferState = itemView.findViewById(R.id.tvTransferState);
            llAuthentications = itemView.findViewById(R.id.llAuthentications);
            llFiles = itemView.findViewById(R.id.llFiles);
        }
    }

    private boolean isSystemDarkModeEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int currentNightMode = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
        return false; // System dark mode is not supported on versions prior to Android 10
    }
}