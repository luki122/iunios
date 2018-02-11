package com.mediatek.settings.ext;

import android.content.Context;
import android.os.SystemProperties;
import android.widget.ArrayAdapter;
//import android.widget.Spinner;
import aurora.widget.AuroraSpinner;

public class AuroraDefaultWifiApDialogExt implements AuroraIWifiApDialogExt {
    private static final String TAG = "DefaultWifiApDialogExt";

    public void setAdapter(Context context, AuroraSpinner spinner, int arrayId) {
    }
    public int getSelection(int index) {
        return index;
    }
    public int getSecurityType(int position) {
        return position;
    }
}
