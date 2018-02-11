package com.gionee.fingerprint;
import android.graphics.Bitmap;

public interface IGnCaptureImageCallback { 

    /**
     * Called when the image capture failed.
     */
    void onCaptureImageFailed(int reason);
    
    void onCaptureImageCompleted(Bitmap bitmap);

}
