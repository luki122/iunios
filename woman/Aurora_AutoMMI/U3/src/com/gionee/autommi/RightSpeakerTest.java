package com.gionee.autommi;

public class RightSpeakerTest extends SpeakerTest {
    @Override
    protected void chooseSpeaker()  {
        audioManager.setParameters("MMI_STEREO_SPEAKER=1"); 
    }

    @Override 
    protected void onPause() {
        super.onPause();
        audioManager.setParameters("MMI_STEREO_SPEAKER=0");
    }
}
