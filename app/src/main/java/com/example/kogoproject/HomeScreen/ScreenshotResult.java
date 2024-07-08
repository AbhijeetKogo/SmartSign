package com.example.kogoproject.HomeScreen;

public class ScreenshotResult {
    private final String screenshot_image;
    private final String offer_id;

    private final String signage_id;

    private final String androidids;

    public ScreenshotResult(String screenshot_image, String offer_id,String signage_id,String androidids) {
        this.screenshot_image = screenshot_image;
        this.offer_id = offer_id;
        this.signage_id = signage_id;
        this.androidids = androidids;
    }

    public String getScreenshot_image() {
        return screenshot_image;
    }

    public String getOffer_id() {
        return offer_id;
    }

    public String getSignage_id() {
        return signage_id;
    }

    public String getAndroidids() {
        return androidids;
    }


}
