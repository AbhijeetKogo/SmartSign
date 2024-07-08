package com.example.kogoproject.HomeScreen;

public class Offer {
    private String offer_id;
    private String offer_type;
    private String offer_title;
    private String offer_name;
    private String offer_price;
    private String smart_cash_price;
    private String final_payment;
    private String expires_on;
    private String screen_time;
    private String offer_text_desc;
    private String offer_image_path;
    private String offer_video_path;

    public Offer(String offer_id, String offer_type, String offer_title, String offer_name, String offer_price, String smart_cash_price, String final_payment, String expires_on, String screen_time, String offer_text_desc, String offer_image_path, String offer_video_path) {
        this.offer_id = offer_id;
        this.offer_type = offer_type;
        this.offer_title = offer_title;
        this.offer_name = offer_name;
        this.offer_price = offer_price;
        this.smart_cash_price = smart_cash_price;
        this.final_payment = final_payment;
        this.expires_on = expires_on;
        this.screen_time = screen_time;
        this.offer_text_desc = offer_text_desc;
        this.offer_image_path = offer_image_path;
        this.offer_video_path = offer_video_path;
    }

    public String getOffer_id() {
        return offer_id;
    }

    public void setOffer_id(String offer_id) {
        this.offer_id = offer_id;
    }

    public String getOffer_type() {
        return offer_type;
    }

    public void setOffer_type(String offer_type) {
        this.offer_type = offer_type;
    }

    public String getOffer_title() {
        return offer_title;
    }

    public void setOffer_title(String offer_title) {
        this.offer_title = offer_title;
    }

    public String getOffer_name() {
        return offer_name;
    }

    public void setOffer_name(String offer_name) {
        this.offer_name = offer_name;
    }

    public String getOffer_price() {
        return offer_price;
    }

    public void setOffer_price(String offer_price) {
        this.offer_price = offer_price;
    }

    public String getSmart_cash_price() {
        return smart_cash_price;
    }

    public void setSmart_cash_price(String smart_cash_price) {
        this.smart_cash_price = smart_cash_price;
    }

    public String getFinal_payment() {
        return final_payment;
    }

    public void setFinal_payment(String final_payment) {
        this.final_payment = final_payment;
    }

    public String getExpires_on() {
        return expires_on;
    }

    public void setExpires_on(String expires_on) {
        this.expires_on = expires_on;
    }

    public String getScreen_time() {
        return screen_time;
    }

    public void setScreen_time(String screen_time) {
        this.screen_time = screen_time;
    }

    public String getOffer_text_desc() {
        return offer_text_desc;
    }

    public void setOffer_text_desc(String offer_text_desc) {
        this.offer_text_desc = offer_text_desc;
    }

    public String getOffer_image_path() {
        return offer_image_path;
    }

    public void setOffer_image_path(String offer_image_path) {
        this.offer_image_path = offer_image_path;
    }

    public String getOffer_video_path() {
        return offer_video_path;
    }

    public void setOffer_video_path(String offer_video_path) {
        this.offer_video_path = offer_video_path;
    }
}
