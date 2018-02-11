package com.android.systemui.statusbar.phone;

public class AuroraDeviceSN {

    private static AuroraDeviceSN mInstance = new AuroraDeviceSN();


    public static AuroraDeviceSN getDefault() {
        return mInstance;
    }

} 
