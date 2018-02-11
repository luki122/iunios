package com.gionee.fingerprint;
import android.os.Message;

public interface IGnIdentifyCallback {
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
     * Called when the identification/verification procedure has succeeded to find a match.
     * @param fingerId the identifier of the matched fignerprint.
     * @param updated set if the fingerprint data acquired during identification was used to improve
     * the biometric data record for this identity.
     */
    
    void onIdentified(int fingerId, boolean updated);
    
    /**
     * Called when the identification/verification procedure has failed to find a match.
     */
    void onNoMatch(int reason);
    
    
    void onExtIdentifyMsg(Message msg, String description);
}