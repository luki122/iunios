/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import com.android.mms.R;
import com.android.mms.dom.AttrImpl;
import com.android.mms.dom.smil.SmilDocumentImpl;
import com.android.mms.dom.smil.SmilPlayer;
import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.android.mms.model.LayoutModel;
import com.android.mms.model.RegionModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.SmilHelper;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;

import aurora.app.AuroraActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.InputFilter;
import android.util.Config;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import aurora.widget.AuroraButton;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.MediaController.MediaPlayerControl;

import java.io.ByteArrayOutputStream;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import java.io.ByteArrayOutputStream;
import android.content.Context;
import android.content.res.Configuration;
import com.aurora.featureoption.FeatureOption;
import android.os.SystemProperties;
//gionee gaoj 2012-4-10 added for CR00555790 start
import com.gionee.mms.ui.SlidesBrowserActivity;
import com.android.mms.MmsApp;
//gionee gaoj 2012-4-10 added for CR00555790 end
//gionee wangym 2012-11-22 add for CR00735223 start
import com.android.mms.ui.ScaleDetector.OnScaleListener;
import android.view.MotionEvent;
//gionee wangym 2012-11-22 add for CR00735223 end
//gionee zhouyj 2012-12-20 add for CR00745782 start 
import java.lang.reflect.Field;
// gionee zhouyj 2012-12-20 add for CR00745782 end 
/**
 * Plays the given slideshow in full-screen mode with a common controller.
 */
public class SlideshowActivity extends AuroraActivity implements EventListener {
    private static final String TAG = "SlideshowActivity";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private static boolean bNeedResume = false;
    private MediaController mMediaController;
    private SmilPlayerController mSmilPlayerController;
    private SmilPlayer mSmilPlayer;

    private Handler mHandler;

    private SMILDocument mSmilDoc;

    private SlideView mSlideView;
    private SlideshowPresenter mPresenter;
    private int mSlideCount;
    private boolean mRotate = false;
    //MTK_OP01_PROTECT_START 
    private ImageButton mZoomIn;
    private ImageButton mZoomOut;
    private View mZoomControls;
    //MTK_OP01_PROTECT_END 
    /**
     * @return whether the Smil has MMS conformance layout.
     * Refer to MMS Conformance Document OMA-MMS-CONF-v1_2-20050301-A
     */
    private static final boolean isMMSConformance(SMILDocument smilDoc) {
        SMILElement head = smilDoc.getHead();
        if (head == null) {
            // No 'head' element
            return false;
        }
        NodeList children = head.getChildNodes();
        if (children == null || children.getLength() != 1) {
            // The 'head' element should have only one child.
            return false;
        }
        Node layout = children.item(0);
        if (layout == null || !"layout".equals(layout.getNodeName())) {
            // The child is not layout element
            return false;
        }
        NodeList layoutChildren = layout.getChildNodes();
        if (layoutChildren == null) {
            // The 'layout' element has no child.
            return false;
        }
        int num = layoutChildren.getLength();
        if (num <= 0) {
            // The 'layout' element has no child.
            return false;
        }
        for (int i = 0; i < num; i++) {
            Node layoutChild = layoutChildren.item(i);
            if (layoutChild == null) {
                // The 'layout' child is null.
                return false;
            }
            String name = layoutChild.getNodeName();
            if ("root-layout".equals(name)) {
                continue;
            } else if ("region".equals(name)) {
                NamedNodeMap map = layoutChild.getAttributes();
                for (int j = 0; j < map.getLength(); j++) {
                    Node node = map.item(j);
                    if (node == null) {
                        return false;
                    }
                    String attrName = node.getNodeName();
                    // The attr should be one of left, top, height, width, fit and id
                    if ("left".equals(attrName) || "top".equals(attrName) ||
                            "height".equals(attrName) || "width".equals(attrName) ||
                            "fit".equals(attrName)) {
                        continue;
                    } else if ("id".equals(attrName)) {
                        String value;
                        if (node instanceof AttrImpl) {
                            value = ((AttrImpl)node).getValue();
                        } else {
                            return false;
                        }
                        if ("Text".equals(value) || "Image".equals(value)) {
                            continue;
                        } else {
                            // The id attr is not 'Text' or 'Image'
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                // The 'layout' element has the child other than 'root-layout' or 'region'
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if ((mSmilPlayer != null) && (mMediaController != null)) {
            mMediaController.hide();
        }
    }
    
    @Override
    public void onCreate(Bundle icicle) {
        //gionee gaoj 2012-6-27 added for CR00628364 start
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-6-27 added for CR00628364 end
        super.onCreate(icicle);
        // gionee zhouyj 2012-05-14 annotate for CR00585826 start
        /** annotate for CR00585826 add play mms and pick up attachment
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport){
            Intent intent = new Intent(this, SlidesBrowserActivity.class);
            intent.setData(getIntent().getData());
            startActivity(intent);
            finish();
            return;
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
         */
        // gionee zhouyj 2012-05-14 annotate for CR00585826 end
        mHandler = new Handler();

        // Play slide-show in full-screen mode.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.slideshow);

        Intent intent = getIntent();
        Uri msg = intent.getData();
        final SlideshowModel model;

        try {
            model = SlideshowModel.createFromMessageUri(this, msg);
            mSlideCount = model.size();
        } catch (MmsException e) {
            Log.e(TAG, "Cannot present the slide show.", e);
            finish();
            return;
        }
        mSlideView = (SlideView) findViewById(R.id.slide_view);
       
        //MTK_OP01_PROTECT_START 
        if (MmsApp.isTelecomOperator()) {
            mZoomIn = (ImageButton) findViewById(R.id.zoomIn);
            mZoomOut = (ImageButton) findViewById(R.id.zoomOut);
            mZoomControls = findViewById(R.id.zoomControls);
            
            mZoomIn.setVisibility(View.VISIBLE);
            mZoomOut.setVisibility(View.VISIBLE);
            mZoomControls.setVisibility(View.VISIBLE);
            
            mZoomIn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    mSlideView.changeTextZoom(true);
                }
            });
            mZoomOut.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    mSlideView.changeTextZoom(false);
                }
            });
            mSlideView.setTextSizeController(mZoomControls, mZoomIn, mZoomOut);
        }
        //MTK_OP01_PROTECT_END
        //gionee wangym 2012-11-22 add for CR00735223 start
        if(MmsApp.mIsTouchModeSupport ){
            float size = MessageUtils.getTextSize(this);
            mTextSize = size;
            mScaleDetector = new ScaleDetector(this, new ScaleListener());
        }
        //gionee wangym 2012-11-22 add for CR00735223 end
        mPresenter = (SlideshowPresenter) PresenterFactory.getPresenter("SlideshowPresenter", this, mSlideView, model);

        mRotate = true;
        mSmilPlayer = SmilPlayer.getPlayer();
        initMediaController();
        mSlideView.setMediaController(mMediaController);
        mSlideView.setActivity(this);
        // Use SmilHelper.getDocument() to ensure rebuilding the
        // entire SMIL document.
        mSmilDoc = SmilHelper.getDocument(model);
        mHandler.post(new Runnable() {
            private boolean isRotating() {
                return mSmilPlayer.isPausedState()
                        || mSmilPlayer.isPlayingState()
                        || mSmilPlayer.isPlayedState();
            }

            public void run() {
                if (isMMSConformance(mSmilDoc)) {
                    int imageLeft = 0;
                    int imageTop = 0;
                    int textLeft = 0;
                    int textTop = 0;
                    LayoutModel layout = model.getLayout();
                    if (layout != null) {
                        RegionModel imageRegion = layout.getImageRegion();
                        if (imageRegion != null) {
                            imageLeft = imageRegion.getLeft();
                            imageTop = imageRegion.getTop();
                        }
                        RegionModel textRegion = layout.getTextRegion();
                        if (textRegion != null) {
                            textLeft = textRegion.getLeft();
                            textTop = textRegion.getTop();
                        }
                    }
                    mSlideView.enableMMSConformanceMode(textLeft, textTop, imageLeft, imageTop);
                }
                if (DEBUG) {
                    ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                    SmilXmlSerializer.serialize(mSmilDoc, ostream);
                    if (LOCAL_LOGV) {
                        Log.v(TAG, ostream.toString());
                    }
                }

                // Add event listener.
                ((EventTarget) mSmilDoc).addEventListener(
                        SmilDocumentImpl.SMIL_DOCUMENT_END_EVENT,
                        SlideshowActivity.this, false);

                mSmilPlayer.init(mSmilDoc);
                if (isRotating()) {
                    mSmilPlayer.reload();
                } else { 
                    //MTK_OP01_PROTECT_START
                    if (MmsApp.isTelecomOperator()) {
                        mSmilPlayer.prepareToPlay();
                        bNeedResume = true;
                        mSmilPlayerController.pause();
                    } else 
                    //MTK_OP01_PROTECT_END
                    {
                        mSmilPlayer.play();
                        bNeedResume = true;
                    }
                }
            }
        });
    }

    private void initMediaController() {
        mMediaController = new MediaController(SlideshowActivity.this, false);
        mSmilPlayerController = new SmilPlayerController(mSmilPlayer);
        mMediaController.setMediaPlayer(mSmilPlayerController);
        mMediaController.setAnchorView(findViewById(R.id.slide_view));
        mMediaController.setBackgroundColor(Color.BLACK);
        // gionee zhouyj 2012-12-20 modify for CR00745782 start 
        /**TextView currentTime = (TextView)mMediaController.findViewById(com.android.internal.R.id.time_current);
        TextView time = (TextView)mMediaController.findViewById(com.android.internal.R.id.time);
        currentTime.setTextColor(Color.WHITE);
        time.setTextColor(Color.WHITE);**/
        try {
            Field fCurrentTime = mMediaController.getClass().getDeclaredField("mCurrentTime");
            if (fCurrentTime != null) {
                fCurrentTime.setAccessible(true);
                TextView currentTime = (TextView) fCurrentTime.get(mMediaController);
                currentTime.setTextColor(Color.WHITE);
            }
            Field fTime = mMediaController.getClass().getDeclaredField("mEndTime");
            if (fTime != null) {
                fTime.setAccessible(true);
                TextView time = (TextView) fTime.get(mMediaController);
                time.setTextColor(Color.WHITE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // gionee zhouyj 2012-12-20 modify for CR00745782 end
        mMediaController.setPrevNextListeners(
            new OnClickListener() {
              public void onClick(View v) {
                  if ((mSmilPlayer != null) && (mMediaController != null)) {
                      mMediaController.show();
                  }
                  mSmilPlayer.next();
              }
            },
            new OnClickListener() {
              public void onClick(View v) {
                  if ((mSmilPlayer != null) && (mMediaController != null)) {
                      mMediaController.show();
                  }
                  if (mSmilPlayer.getCurrentSlide() <= 1){
                      return;
                  }
                  mSmilPlayer.prev();
              }
            });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        
        if ((ev.getAction() == MotionEvent.ACTION_UP) && (mSmilPlayer != null) && (mMediaController != null)) {
            mMediaController.show();
        }
        return false;
    }

    // fix exception when press email add
    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        // requestCode >= 0 means the activity in question is a sub-activity.

        if (null != intent && null != intent.getData()
                && intent.getData().getScheme().equals("mailto")) {
            try {
                super.startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Failed to startActivityForResult: " + intent);
                Intent i = new Intent().setClassName("com.android.email", "com.android.email.activity.setup.AccountSetupBasics");
                this.startActivity(i);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Failed to startActivityForResult: " + intent);
                Toast.makeText(this,getString(R.string.message_open_email_fail),
                      Toast.LENGTH_SHORT).show();
            }
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaController != null) {
            // Must do this so we don't leak a window.
            mMediaController.hide();
        }
        if (mSmilDoc != null) {
            ((EventTarget) mSmilDoc).removeEventListener(
                    SmilDocumentImpl.SMIL_DOCUMENT_END_EVENT, this, false);
        }
        if ((null != mSmilPlayer)) {
            if (mSmilPlayer.isPlayingState()) {
                mSmilPlayer.pause();
                bNeedResume = true;
            } else if (mSmilPlayer.isPausedState()) {
                bNeedResume = false;
            }
        }   
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        //Gionee <gaoj> <2013-4-11> added for CR00794990 start
        finish();
        //Gionee <gaoj> <2013-4-11> added for CR00794990 end
        if ((null != mSmilPlayer)) {
            if (isFinishing()) {
                mSmilPlayer.stop();
            } else {
                //Gionee <gaoj> <2013-4-11> modified rollback for CR00794990 start
                // gionee zhouyj 2013-04-03 remove for CR00792410 start:When it resume will playing the beginning.
                mSmilPlayer.stopWhenReload();
                // gionee zhouyj 2013-04-03 remove for CR00792410 end
                //Gionee <gaoj> <2013-4-11> modified rollback for CR00794990 end
            }
            if (mMediaController != null) {
                // Must do this so we don't leak a window.
                mMediaController.hide();
            }
        }
        //Gionee <gaoj> <2013-4-11> modified rollback for CR00794990 start
        // gionee zhouyj 2013-04-03 remove for CR00792410 start
        if (mPresenter != null) {
            mPresenter.onStop();
            mPresenter = null;
        }
        // gionee zhouyj 2013-04-03 remove for CR00792410 end
        //Gionee <gaoj> <2013-4-11> modified rollback for CR00794990 end
    }
    
    //Gionee <gaoj> <2013-4-11> modified rollback for CR00794990 start
    // gionee zhouyj 2013-03-25 add for CR00788015 start
    /*@Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mPresenter != null) {
            mPresenter.onStop();
            mPresenter = null;
        }
        super.onDestroy();
    }*/
    // gionee zhouyj 2013-03-25 add for CR00788015 end
    //Gionee <gaoj> <2013-4-11> modified rollback for CR00794990 end

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
                if ((mSmilPlayer != null) &&
                        (mSmilPlayer.isPausedState()
                        || mSmilPlayer.isPlayingState()
                        || mSmilPlayer.isPlayedState())) {
                    mSmilPlayer.stop();
                }
                break;
            default:
                if ((mSmilPlayer != null) && (mMediaController != null)) {
                    mMediaController.show();
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if(mMediaController != null){
            //mMediaController.setMdragging(false);
            mMediaController.hide();
        }
        if (mSmilDoc != null) {
            ((EventTarget) mSmilDoc).addEventListener(
                    SmilDocumentImpl.SMIL_DOCUMENT_END_EVENT,
                    SlideshowActivity.this, false);
        }
        
        if(!bNeedResume) {
            mRotate = false;
            return;
        }
        if(null == mSmilPlayer) {
            mSmilPlayer = SmilPlayer.getPlayer();
        }
        if(null != mSmilPlayer) {
            if(!isFinishing()) {
                if(mSmilPlayer.isPausedState()) {
                    if(mRotate) {
                        // if need resume the player, set the state playing.
                        mSmilPlayer.setStateStart();
                    } else {
                        mSmilPlayer.start();
                    }
                }
            }
        }
        mRotate = false;
    }
    
    private class SmilPlayerController implements MediaPlayerControl {
        private final SmilPlayer mPlayer;
        /**
         * We need to cache the playback state because when the MediaController issues a play or
         * pause command, it expects subsequent calls to {@link #isPlaying()} to return the right
         * value immediately. However, the SmilPlayer executes play and pause asynchronously, so
         * {@link #isPlaying()} will return the wrong value for some time. That's why we keep our
         * own version of the state of whether the player is playing.
         *
         * Initialized to true because we always programatically start the SmilPlayer upon creation
         */
        private boolean mCachedIsPlaying = true;

        public SmilPlayerController(SmilPlayer player) {
            mPlayer = player;
        }
        
        // Aurora xuyong 2013-12-06 added for google 4.3 adapt start
        public int getAudioSessionId() {
            return 0;
        }
        // Aurora xuyong 2013-12-06 added for google 4.3 adapt end

        public int getBufferPercentage() {
            // We don't need to buffer data, always return 100%.
            return 100;
        }

        public int getCurrentPosition() {
            if (mPlayer != null) {
                return mPlayer.getCurrentPosition();
            } else {
                return 0;
            }
        }

        public int getDuration() {
            return mPlayer.getDuration();
        }

        public boolean isPlaying() {
            return mCachedIsPlaying;
        }

        public void pause() {
            mPlayer.pause();
            mCachedIsPlaying = false;
        }

        public void seekTo(int pos) {
            // Don't need to support.
        }

        public void start() {
            mPlayer.start();
            mCachedIsPlaying = true;
        }

        public boolean canPause() {
            return true;
        }

        public boolean canSeekBackward() {
            return true;
        }

        public boolean canSeekForward() {
            return true;
        }
    }

    public void handleEvent(Event evt) {
        final Event event = evt;
        mHandler.post(new Runnable() {
            public void run() {
                String type = event.getType();
                if(type.equals(SmilDocumentImpl.SMIL_DOCUMENT_END_EVENT)) {
                    finish();
                }
            }
        });
    }

    //gionee wangym 2012-11-22 add for CR00735223 start
    @Override
    public boolean  dispatchTouchEvent(MotionEvent event){
        
        boolean ret = false;

//MTK_OP01_PROTECT_START
        if(mIsCmcc && mScaleDetector != null){
                ret = mScaleDetector.onTouchEvent(event);
        }
//MTK_OP01_PROTECT_END
        
        if(!ret){
            ret = super.dispatchTouchEvent(event); 
        }
        return ret;
    }
    
//MTK_OP01_PROTECT_START
    
    private final int DEFAULT_TEXT_SIZE = 20;
    private final int MIN_TEXT_SIZE = 10;
    private final int MAX_TEXT_SIZE = 32;
    private ScaleDetector mScaleDetector;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private float MIN_ADJUST_TEXT_SIZE = 0.2f;
    private boolean mIsCmcc = false;    
    
    // add for cmcc changTextSize by multiTouch
    private void changeTextSize(float size){
        if(mSlideView != null)
        {
            mSlideView.changeTextSize(size);
        }
    }    
    
    public class ScaleListener implements OnScaleListener{
        
        public boolean onScaleStart(ScaleDetector detector) {
            Log.i(TAG, "onScaleStart -> mTextSize = " + mTextSize);
            return true;
        }
        
        public void onScaleEnd(ScaleDetector detector) {
            Log.i(TAG, "onScaleEnd -> mTextSize = " + mTextSize);
            
            //save current value to preference
            MessageUtils.setTextSize(SlideshowActivity.this, mTextSize);
        }
        
        public boolean onScale(ScaleDetector detector) {

            float size = mTextSize * detector.getScaleFactor();
            
            if(Math.abs(size - mTextSize) < MIN_ADJUST_TEXT_SIZE){
                return false;
            }            
            if(size < MIN_TEXT_SIZE){
                size = MIN_TEXT_SIZE;
            }            
            if(size > MAX_TEXT_SIZE){
                size = MAX_TEXT_SIZE;
            }            
            if(size != mTextSize){
                changeTextSize(size);
                mTextSize = size;
            }
            return true;
        }
    };
    //gionee wangym 2012-11-22 add for CR00735223 end
}
