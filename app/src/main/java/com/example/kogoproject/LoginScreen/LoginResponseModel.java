package com.example.kogoproject.LoginScreen;

import java.io.Serializable;

public class LoginResponseModel implements Serializable {
    public int resultcode;
    public String resultmsg;
    public String locid;
    public String loc_uid;
    public int sync_timing;
    public String location_logo;

    public LoginResponseModel(int resultcode, String resultmsg, String locid, String loc_uid, int sync_timing, String location_logo) {
        this.resultcode = resultcode;
        this.resultmsg = resultmsg;
        this.locid = locid;
        this.loc_uid = loc_uid;
        this.sync_timing = sync_timing;
        this.location_logo = location_logo;
    }


    public int getResultcode() {
        return resultcode;
    }

    public void setResultcode(int resultcode) {
        this.resultcode = resultcode;
    }

    public String getResultmsg() {
        return resultmsg;
    }

    public void setResultmsg(String resultmsg) {
        this.resultmsg = resultmsg;
    }

    public String getLocid() {
        return locid;
    }

    public void setLocid(String locid) {
        this.locid = locid;
    }

    public String getLoc_uid() {
        return loc_uid;
    }

    public void setLoc_uid(String loc_uid) {
        this.loc_uid = loc_uid;
    }

    public int getSync_timing() {
        return sync_timing;
    }

    public void setSync_timing(int sync_timing) {
        this.sync_timing = sync_timing;
    }

    public String getLocation_logo() {
        return location_logo;
    }

    public void setLocation_logo(String location_logo) {
        this.location_logo = location_logo;
    }
}
