package com.example.otc;

public enum AuthType {

    Email(0),
    OneDrive(1),
    GoogleDrive(2);
    private final int value;

    AuthType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AuthType fromInt(int value) {
        for (AuthType authType : AuthType.values()) {
            if (authType.getValue() == value) {
                return authType;
            }
        }
        throw new IllegalArgumentException("No enum constant with value " + value);
    }
}
