package com.gionee.autommi;

public class LeftSpeakerTest extends SpeakerTest {
    @Override
    protected void chooseSpeaker()  {
        audioManager.setParameters("MMI_STEREO_SPEAKER=2"); 
    }

    @Override 
    protected void onPause() {
        super.onPause();
        audioManager.setParameters("MMI_STEREO_SPEAKER=0");
    }
}
