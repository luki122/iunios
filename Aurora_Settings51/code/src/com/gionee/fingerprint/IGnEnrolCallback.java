package com.gionee.fingerprint;

import android.os.Message;


public interface IGnEnrolCallback { 
    
      /**
     * Called when the sensor is waiting for the user to touch the sensor with the finger.
     */
    void onWaitingForInput();
    /**
     * Called when the user has put down the finger and the image capture procedure has started.
     */
    void onInput();
    /**
     * Called when the image capture procedure has completed.
     */
    void onCaptureCompleted();
    
    void onCaptureFailed(int reason);
    
    
    /**
     * Called when the enrollment procedure has finished analyzing a fingerprint image.
     * @param data see more info in GuidedData class
     */
    void onProgress(Message msg);
    
    /**
     * Called when the enrollment procedure has finished.
     * @param fingerId the identifier used to reference the newly enrolled fingerprint.
     */
    void onEnrolled(int fingerId);
    
    /**
     * Called if the enrollment failed.
     */
    void onEnrollmentFailed(int reason);
    
    void onExtEnrolMsg(Message msg, String description);
}
