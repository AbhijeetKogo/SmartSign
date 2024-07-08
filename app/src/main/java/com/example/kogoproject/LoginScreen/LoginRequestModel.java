package com.example.kogoproject.LoginScreen;

public class LoginRequestModel {
    public String signage_id;

    public LoginRequestModel(String signage_id) {
        this.signage_id = signage_id;
    }

    public String getSignage_id() {
        return signage_id;
    }

    public void setSignage_id(String signage_id) {
        this.signage_id = signage_id;
    }
}
