package com.example.kogoproject.HomeScreen;

import java.util.List;

public class ResponseModel {
    private int resultcode;
    private String resultmsg;
    private List<Offer> offer;

    public ResponseModel(int resultcode, String resultmsg, List<Offer> offer) {
        this.resultcode = resultcode;
        this.resultmsg = resultmsg;
        this.offer = offer;
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

    public List<Offer> getOffer() {
        return offer;
    }

    public void setOffer(List<Offer> offer) {
        this.offer = offer;
    }
}
