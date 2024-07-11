package com.example.otc;

import androidx.annotation.NonNull;

public class Authentication {
    private int authID;
    private int authType;
    private String displayName;

    public Authentication(int authID, int authType, String displayName){
        this.authID = authID;
        this.authType = authType;
        this.displayName = displayName;
    }

    public int getAuthID(){
        return authID;
    }
    public int getAuthType(){
        return authType;
    }
    public String getDisplayName(){
        return displayName;
    }

    @NonNull
    @Override
    public String toString() {
        AuthType authType1 = AuthType.fromInt(authType);
        return authType1 + ": " + displayName;
    }
}
