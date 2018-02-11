package com.mediatek.settings.ext;

import android.content.Context;
//import android.widget.Spinner;
import aurora.widget.AuroraSpinner;

public interface AuroraIWifiApDialogExt {
    /**
     * set adapter for wifi access point security spinner
     * @param context The parent context 
     */
    void setAdapter(Context context, AuroraSpinner spinner, int arrayId);
    /**
     * set adapter for wifi access point security spinner
     * @param indext The selected indext in the adapter 
     * @return selected item
     */
    int getSelection(int index);
    /**
     * Get security type
     * @param position The selected position in the adapter 
     * @return security type 
     */
    int getSecurityType(int position);
}
