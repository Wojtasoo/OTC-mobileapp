package com.example.otc;

import java.util.List;

public class TransferData {
    private String transferStartTime;
    private String transferState;
    private List<AuthenticationData> authentications;
    private List<FileData> files;

    public TransferData(String transferStartTime, String transferState, List<AuthenticationData> authentications, List<FileData> files) {
        this.transferStartTime = transferStartTime;
        this.transferState = transferState;
        this.authentications = authentications;
        this.files = files;
    }

    public String getTransferStartTime() {
        return transferStartTime;
    }

    public String getTransferState() {
        return transferState;
    }

    public List<AuthenticationData> getAuthentications() {
        return authentications;
    }

    public List<FileData> getFiles() {
        return files;
    }
}

class AuthenticationData {
    private String authType;
    private String displayName;

    public AuthenticationData(String authType, String displayName) {
        this.authType = authType;
        this.displayName = displayName;
    }

    public String getAuthType() {
        return authType;
    }

    public String getDisplayName() {
        return displayName;
    }
}
