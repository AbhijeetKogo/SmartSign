package com.example.kogoproject.HomeScreen;

public class SyncResponseModel {
    public int resultcode;
    public int sync_available;
    public int take_shot;
    public int islogout;
    public String sync_available_cond;

    public SyncResponseModel(int resultcode, int sync_available, int take_shot, int islogout, String sync_available_cond) {
        this.resultcode = resultcode;
        this.sync_available = sync_available;
        this.take_shot = take_shot;
        this.islogout = islogout;
        this.sync_available_cond = sync_available_cond;
    }

    public int getResultcode() {
        return resultcode;
    }

    public void setResultcode(int resultcode) {
        this.resultcode = resultcode;
    }

    public int getSync_available() {
        return sync_available;
    }

    public void setSync_available(int sync_available) {
        this.sync_available = sync_available;
    }

    public int getTake_shot() {
        return take_shot;
    }

    public void setTake_shot(int take_shot) {
        this.take_shot = take_shot;
    }

    public int getIslogout() {
        return islogout;
    }

    public void setIslogout(int islogout) {
        this.islogout = islogout;
    }

    public String getSync_available_cond() {
        return sync_available_cond;
    }

    public void setSync_available_cond(String sync_available_cond) {
        this.sync_available_cond = sync_available_cond;
    }
}
