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

import java.io.File;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.ExceedMessageSizeException;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.ContentRestrictionException;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.ResolutionException;
import com.android.mms.TempFileProvider;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.LogTag;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.IModelChangedObserver;
import com.android.mms.model.LayoutModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.Model;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.pdu.PduBody;
import com.aurora.android.mms.pdu.PduPart;
import com.aurora.android.mms.pdu.PduPersister;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.ui.BasicSlideEditorView.OnTextChangedListener;
import com.android.mms.ui.MessageUtils.ResizeImageResultCallback;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import com.android.mms.RestrictedResolutionException;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony.Mms;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputFilter.LengthFilter;
import aurora.widget.AuroraEditText;
import android.text.InputFilter;
import com.aurora.featureoption.FeatureOption;
import android.drm.DrmManagerClient;
import gionee.drm.GnDrmStore.DrmExtra;

/**
 * This activity allows user to edit the contents of a slide.
 */
public class SlideEditorActivity extends AuroraActivity {
    private static final String TAG = "SlideEditorActivity";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    // Key for extra data.
    public static final String SLIDE_INDEX = "slide_index";

    // Menu ids.
    private final static int MENU_REMOVE_TEXT       = 0;
    private final static int MENU_ADD_PICTURE       = 1;
    private final static int MENU_TAKE_PICTURE      = 2;
    private final static int MENU_DEL_PICTURE       = 3;
    private final static int MENU_ADD_AUDIO         = 4;
    private final static int MENU_DEL_AUDIO         = 5;
    private final static int MENU_ADD_VIDEO         = 6;
    private final static int MENU_ADD_SLIDE         = 7;
    private final static int MENU_DEL_VIDEO         = 8;
    private final static int MENU_LAYOUT            = 9;
    private final static int MENU_DURATION          = 10;
    private final static int MENU_PREVIEW_SLIDESHOW = 11;
    private final static int MENU_RECORD_SOUND      = 12;
    private final static int MENU_SUB_AUDIO         = 13;
    private final static int MENU_TAKE_VIDEO        = 14;
    private final static int MENU_ADD_SD_SOUND      = 15;

    // Request code.
    private final static int REQUEST_CODE_EDIT_TEXT          = 0;
    private final static int REQUEST_CODE_CHANGE_PICTURE     = 1;
    private final static int REQUEST_CODE_TAKE_PICTURE       = 2;
    private final static int REQUEST_CODE_CHANGE_MUSIC       = 3;
    private final static int REQUEST_CODE_RECORD_SOUND       = 4;
    private final static int REQUEST_CODE_CHANGE_VIDEO       = 5;
    private final static int REQUEST_CODE_CHANGE_DURATION    = 6;
    private final static int REQUEST_CODE_TAKE_VIDEO         = 7;

    // number of items in the duration selector dialog that directly map from
    // item index to duration in seconds (duration = index + 1)
    private final static int NUM_DIRECT_DURATIONS = 10;

    private ImageButton mNextSlide;
    private ImageButton mPreSlide;
    private AuroraButton mPreview;
    private AuroraButton mReplaceImage;
    private AuroraButton mRemoveSlide;
    private AuroraEditText mTextEditor;
    private AuroraButton mDone;
    private BasicSlideEditorView mSlideView;
    private TextView mTextView;

    private SlideshowModel mSlideshowModel;
    private SlideshowEditor mSlideshowEditor;
    private SlideshowPresenter mPresenter;
    private boolean mDirty;

    private int mPosition;
    private Uri mUri;
    private int mSizeLimit;
    private ImageView mDrmImageVideoLock;
    private ImageView mDrmAudioLock;
    private final static String MESSAGE_URI = "message_uri";
    public static final int REQUEST_CODE_ATTACH_SOUND     = 15;
    public static final int REQUEST_CODE_ATTACH_RINGTONE  = 20;
    
    // gionee zhouyj 2012-09-13 add for CR00690245 start 
    private SubMenu mSubMenu;
    // gionee zhouyj 2012-09-13 add for CR00690245 end 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //gionee gaoj 2012-6-27 added for CR00628364 start
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-6-27 added for CR00628364 end
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_slide_activity);

        mSlideView = (BasicSlideEditorView) findViewById(R.id.slide_editor_view);
        mSlideView.setOnTextChangedListener(mOnTextChangedListener);

        mPreSlide = (ImageButton) findViewById(R.id.pre_slide_button);
        mPreSlide.setOnClickListener(mOnNavigateBackward);

        mNextSlide = (ImageButton) findViewById(R.id.next_slide_button);
        mNextSlide.setOnClickListener(mOnNavigateForward);

        mPreview = (AuroraButton) findViewById(R.id.preview_button);
        mPreview.setOnClickListener(mOnPreview);

        mReplaceImage = (AuroraButton) findViewById(R.id.replace_image_button);
        mReplaceImage.setOnClickListener(mOnReplaceImage);

        mRemoveSlide = (AuroraButton) findViewById(R.id.remove_slide_button);
        mRemoveSlide.setOnClickListener(mOnRemoveSlide);

        //gionee gaoj 2012-6-25 added for CR00628151 start
        if (MmsApp.mGnMessageSupport) {
            mPreview.setTextColor(Color.BLACK);
            mReplaceImage.setTextColor(Color.BLACK);
            mRemoveSlide.setTextColor(Color.BLACK);
        }
        //gionee gaoj 2012-6-25 added for CR00628151 end
        mTextEditor = (AuroraEditText) findViewById(R.id.text_message);
        mTextEditor.setFilters(new InputFilter[] {
                new LengthFilter(MmsConfig.getMaxTextLimit())});

        mDone = (AuroraButton) findViewById(R.id.done_button);
        mDone.setOnClickListener(mDoneClickListener);

        mTextView = (TextView) findViewById(R.id.media_size_info);
        mTextView.setVisibility(View.VISIBLE);
        
        mDrmImageVideoLock = (ImageView) findViewById(R.id.drm_imagevideo_lock);
        mDrmAudioLock = (ImageView) findViewById(R.id.drm_audio_lock);
        
        readLimitSize();
        mSizeLimit = MmsConfig.getUserSetMmsSizeLimit(false);
        initActivityState(savedInstanceState, getIntent());
        
        //gionee gaoj 2012-5-29 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            ActionBar actionBar = getActionBar();
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //gionee gaoj 2012-5-29 added for CR00555790 end
        // get the creation mode from preference
        WorkingMessage.updateCreationMode(this);
        try {
            mSlideshowModel = SlideshowModel.createFromMessageUri(this, mUri);
            // Confirm that we have at least 1 slide to display
            if (mSlideshowModel.size() == 0) {
                Log.e(TAG, "Loaded slideshow is empty; can't edit nothingness, exiting.");
                finish();
                return;
            }
            // Register an observer to watch whether the data model is changed.
            mSlideshowModel.registerModelChangedObserver(mModelChangedObserver);
            mSlideshowEditor = new SlideshowEditor(this, mSlideshowModel);
            mPresenter = (SlideshowPresenter) PresenterFactory.getPresenter(
                    "SlideshowPresenter", this, mSlideView, mSlideshowModel);

            // Sanitize mPosition
            if (mPosition >= mSlideshowModel.size()) {
                mPosition = Math.max(0, mSlideshowModel.size() - 1);
            } else if (mPosition < 0) {
                mPosition = 0;
            }

            showCurrentSlide();
            showSizeDisplay();
        } catch (MmsException e) {
            Log.e(TAG, "Create SlideshowModel failed!", e);
            finish();
            return;
        }
    }
    
    private void showDrmLock() {
        mDrmImageVideoLock.setVisibility(View.GONE);
        mDrmAudioLock.setVisibility(View.GONE);
        mSlideshowModel.setDrmContentFlag(false);
        if (FeatureOption.MTK_DRM_APP) {
            Resources res = getResources();
            boolean hasContent = false;
            boolean hasRight = false;
            boolean hasDrmImageOrVideo = false;
            boolean hasDrmAudio = false;
        if (mSlideshowModel.get(mPosition) != null) {
            if (hasDrmAudio = mSlideshowModel.get(mPosition).hasAudio()) {
                Log.i(TAG, "hasDrmAudio");
                String src = mSlideshowModel.get(mPosition).getAudio().getSrc();
                String extName = src.substring(src.lastIndexOf('.') + 1);
                if (extName.equals("dcf")) {      
                    hasContent = true;
                    mSlideshowModel.setDrmContentFlag(true);
                }
                //hasRight = mSlideshowModel.get(mPosition).getAudio().hasDrmRight();
                if (hasContent && hasRight) {           
                    mDrmAudioLock.setImageDrawable(res.getDrawable(-1/*R.drawable.drm_green_lock*/));
                    mDrmAudioLock.setVisibility(View.VISIBLE);
                } else if (hasContent){
                    mDrmAudioLock.setImageDrawable(res.getDrawable(-1/*R.drawable.drm_red_lock*/));
                    mDrmAudioLock.setVisibility(View.VISIBLE);
                }
            } 
            if (hasDrmImageOrVideo = mSlideshowModel.get(mPosition).hasImage()) {
                Log.i(TAG, "hasDrmImageOrVideo");
                String src = mSlideshowModel.get(mPosition).getImage().getSrc();
                String extName = src.substring(src.lastIndexOf('.') + 1);
                if (extName.equals("dcf")) {      
                    hasContent = true;
                    mSlideshowModel.setDrmContentFlag(true);
                }
                if (hasContent && hasRight) {           
                      mDrmImageVideoLock.setImageDrawable(res.getDrawable(-1/*R.drawable.drm_green_lock*/));
                      mDrmImageVideoLock.setVisibility(View.VISIBLE);
                } else if (hasContent){
                    mDrmImageVideoLock.setImageDrawable(res.getDrawable(-1/*R.drawable.drm_red_lock*/));
                    mDrmImageVideoLock.setVisibility(View.VISIBLE);
                }
            }
            if (hasDrmImageOrVideo = mSlideshowModel.get(mPosition).hasVideo()) {
                String src = mSlideshowModel.get(mPosition).getVideo().getSrc();
                String extName = src.substring(src.lastIndexOf('.') + 1);
                if (extName.equals("dcf")) {      
                    hasContent = true;
                    mSlideshowModel.setDrmContentFlag(true);
                }
                if (hasContent && hasRight) {           
                    mDrmImageVideoLock.setImageDrawable(res.getDrawable(-1/*R.drawable.drm_green_lock*/));
                    mDrmImageVideoLock.setVisibility(View.VISIBLE);
                } else if (hasContent){
                    mDrmImageVideoLock.setImageDrawable(res.getDrawable(-1/*R.drawable.drm_red_lock)*/));
                    mDrmImageVideoLock.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        
    }
    private void showSizeDisplay() {
        int showSize = (mSlideshowModel.getCurrentSlideshowSize() - 1)/1024 + 1;
        mTextView.setText(showSize + "K/" + mSizeLimit + "K");
        showDrmLock();
    }
    private void readLimitSize() {
        Context otherAppContext = null;
        SharedPreferences sp = null;
        try{
            otherAppContext = this.createPackageContext("com.android.mms", 
                    Context.CONTEXT_IGNORE_SECURITY);
        }
        
        catch(Exception e){
            Log.e(TAG, "ConversationList NotFoundContext");
        }
        if (otherAppContext != null) {
           sp = otherAppContext.
                    getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        }
        String mSizeLimitTemp = null;
        int mMmsSizeLimit = 0;
        if (sp != null) {
            mSizeLimitTemp = sp.getString("pref_key_mms_size_limit", "300");
        }
        if (0 == mSizeLimitTemp.compareTo("100")) {
            mMmsSizeLimit = 100;
        } else if (0 == mSizeLimitTemp.compareTo("200")) {
            mMmsSizeLimit = 200;
        } else {
            mMmsSizeLimit = 300;
        }
        MmsConfig.setUserSetMmsSizeLimit(mMmsSizeLimit);
    }
    private void initActivityState(Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState != null) {
            mUri = (Uri) savedInstanceState.getParcelable(MESSAGE_URI);
            mPosition = savedInstanceState.getInt(SLIDE_INDEX, 0);
            readLimitSize();
            intent.putExtra(SLIDE_INDEX, mPosition);
        } else {
            mUri = intent.getData();
            mPosition = intent.getIntExtra(SLIDE_INDEX, 0);
            readLimitSize();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SLIDE_INDEX, mPosition);
        outState.putParcelable(MESSAGE_URI, mUri);
    }

    @Override
    protected void onResume()  {
        super.onResume();
        showSizeDisplay();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onPause()  {
        super.onPause();
        // gionee zhouyj 2012-09-13 add for CR00690245 start 
        if(MmsApp.mGnMessageSupport && null != mSubMenu) {
            mSubMenu.close();
        }
        // gionee zhouyj 2012-09-13 add for CR00690245 end 
        synchronized (this) {
            if (mDirty) {
                try {
                    PduBody pb = mSlideshowModel.toPduBody();
                    PduPersister.getPduPersister(this).updateParts(mUri, pb);
                    mSlideshowModel.sync(pb);
                }  catch (MmsException e) {
                    Log.e(TAG, "Cannot update the message: " + mUri, e);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSlideshowModel != null) {
            mSlideshowModel.unregisterModelChangedObserver(
                    mModelChangedObserver);
        }
    }

    private final IModelChangedObserver mModelChangedObserver =
        new IModelChangedObserver() {
            public void onModelChanged(Model model, boolean dataChanged) {
                synchronized (SlideEditorActivity.this) {
                    mDirty = true;
                }
                setResult(RESULT_OK);
            }
        };

    private final OnClickListener mOnRemoveSlide = new OnClickListener() {
        public void onClick(View v) {
            // Validate mPosition
            if (mPosition >= 0 && mPosition < mSlideshowModel.size()) {
                mSlideshowEditor.removeSlide(mPosition);
                int size = mSlideshowModel.size();
                if (size > 0) {
                    if (mPosition >= size) {
                        mPosition--;
                    }
                    showCurrentSlide();
                    showSizeDisplay();
                } else {
                    mSlideshowEditor.changeLayout(LayoutModel.LAYOUT_BOTTOM_TEXT);
                    finish();
                    return;
                }
            }
        }
    };

    private final OnTextChangedListener mOnTextChangedListener = new OnTextChangedListener() {
        public void onTextChanged(String s) {
            if (!isFinishing()) {
                try {
                    mSlideshowEditor.changeText(mPosition, s);
                } catch (ExceedMessageSizeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.exceed_message_size_limitation),
                            getResourcesString(R.string.exceed_message_size_limitation));
                }
                showSizeDisplay();
            }
        }
    };

    private final OnClickListener mOnPreview = new OnClickListener() {
        public void onClick(View v) {
            previewSlideshow();
        }
    };

    private final OnClickListener mOnReplaceImage = new OnClickListener() {
        public void onClick(View v) {
            SlideModel slide = mSlideshowModel.get(mPosition);
            if (slide != null && slide.hasVideo()) {
                Toast.makeText(SlideEditorActivity.this, R.string.cannot_add_picture_and_video,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType(ContentType.IMAGE_UNSPECIFIED);
            if (FeatureOption.MTK_DRM_APP) {
                intent.putExtra(DrmExtra.EXTRA_DRM_LEVEL, DrmExtra.DRM_LEVEL_SD);
            }
            startActivityForResult(intent, REQUEST_CODE_CHANGE_PICTURE);
            showSizeDisplay();
        }
    };

    private final OnClickListener mOnNavigateBackward = new OnClickListener() {
        public void onClick(View v) {
            if (mPosition > 0) {
                mPosition --;
                showCurrentSlide();
                showSizeDisplay();
            }
        }
    };

    private final OnClickListener mOnNavigateForward = new OnClickListener() {
        public void onClick(View v) {
            if (mPosition < mSlideshowModel.size() - 1) {
                mPosition ++;
                showCurrentSlide();
                showSizeDisplay();
            }
        }
    };

    private void hideInputMethod() {
        InputMethodManager inputMethodManager =
            (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(this.getWindow()!=null && this.getWindow().getCurrentFocus()!=null){
            inputMethodManager.hideSoftInputFromWindow(this.getWindow().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private final OnClickListener mDoneClickListener = new OnClickListener() {
        public void onClick(View v) {
            hideInputMethod();
            Intent data = new Intent();
            data.putExtra("done", true);
            setResult(RESULT_OK, data);
            finish();
        }
    };

    private void previewSlideshow() {
        MessageUtils.viewMmsMessageAttachment(SlideEditorActivity.this, mUri, mSlideshowModel);
    }

    private void updateTitle() {
        setTitle(getString(R.string.slide_show_part, (mPosition + 1), mSlideshowModel.size()));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isFinishing()) {
            return false;
        }
        menu.clear();

        SlideModel slide = mSlideshowModel.get(mPosition);

        if (slide == null) {
            return false;
        }

        // Preview slideshow.
         // Aurora liugj 2013-10-29 added for aurora's new feature start
        /*menu.add(0, MENU_PREVIEW_SLIDESHOW, 0, R.string.preview_slideshow).setIcon(
                R.drawable.ic_menu_play_clip).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);*/
         // Aurora liugj 2013-10-29 added for aurora's new feature end

        // Text
        /*if (slide.hasText() && !TextUtils.isEmpty(slide.getText().getText())) {
            //"Change text" if text is set.
            menu.add(0, MENU_REMOVE_TEXT, 0, R.string.remove_text).setIcon(
                    R.drawable.ic_menu_remove_text).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }*/

        // Picture
        /*if (slide.hasImage()) {
            menu.add(0, MENU_DEL_PICTURE, 0, R.string.remove_picture).setIcon(
                    R.drawable.ic_menu_remove_picture).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else if (!slide.hasVideo()) {
            menu.add(0, MENU_ADD_PICTURE, 0, R.string.add_picture).setIcon(
                    R.drawable.ic_menu_picture).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, MENU_TAKE_PICTURE, 0, R.string.attach_take_photo).setIcon(
                    R.drawable.ic_menu_picture).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }*/

        // Music
        /*if (slide.hasAudio()) {
            menu.add(0, MENU_DEL_AUDIO, 0, R.string.remove_music).setIcon(
                    R.drawable.ic_menu_remove_sound).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else if (!slide.hasVideo()) {
            if (MmsConfig.getAllowAttachAudio()) {
                // gionee zhouyj 2012-09-13 modify for CR00690245 start 
                if(MmsApp.mGnMessageSupport) {
                    mSubMenu = menu.addSubMenu(0, MENU_SUB_AUDIO, 0, R.string.add_music)
                        .setIcon(R.drawable.ic_menu_add_sound);
                    mSubMenu.add(0, MENU_ADD_AUDIO, 0, R.string.attach_ringtone);
                    mSubMenu.add(0, MENU_ADD_SD_SOUND, 0, R.string.attach_sound);
                    mSubMenu.add(0, MENU_RECORD_SOUND, 0, R.string.attach_record_sound);
                } else {
                SubMenu subMenu = menu.addSubMenu(0, MENU_SUB_AUDIO, 0, R.string.add_music)
                    .setIcon(R.drawable.ic_menu_add_sound);
                subMenu.add(0, MENU_ADD_AUDIO, 0, R.string.attach_ringtone);
                subMenu.add(0, MENU_ADD_SD_SOUND, 0, R.string.attach_sound);
                subMenu.add(0, MENU_RECORD_SOUND, 0, R.string.attach_record_sound);
                }
                // gionee zhouyj 2012-09-13 modify for CR00690245 end 
            } else {
                menu.add(0, MENU_RECORD_SOUND, 0, R.string.attach_record_sound)
                    .setIcon(R.drawable.ic_menu_add_sound).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }*/

        // Video
        /*if (slide.hasVideo()) {
            menu.add(0, MENU_DEL_VIDEO, 0, R.string.remove_video).setIcon(
                    R.drawable.ic_menu_remove_video).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else if (!slide.hasAudio() && !slide.hasImage()) {
            menu.add(0, MENU_ADD_VIDEO, 0, R.string.add_video).setIcon(R.drawable.ic_menu_movie).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);;
            menu.add(0, MENU_TAKE_VIDEO, 0, R.string.attach_record_video)
                .setIcon(R.drawable.ic_menu_movie).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }*/

        // Add slide
        /*if (mSlideshowModel.size() < SlideshowEditor.MAX_SLIDE_NUM){
        menu.add(0, MENU_ADD_SLIDE, 0, R.string.add_slide).setIcon(
                R.drawable.ic_menu_add_slide).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }*/

        // Slide duration
        String duration = getResources().getString(R.string.duration_sec);
        int pos = duration.indexOf("%s");
        duration = duration.substring(0, pos + 2) + ")";
        Context context = SlideEditorActivity.this;
        // if need according media file duration , use like this :
        //int dur = slide.getPlayDuration() / 1000;
        // if need according slide setting duration , use like this:
        int dur = slide.getDuration() / 1000;
        String format = context.getResources().getQuantityString(R.plurals.slide_duration, dur, dur);
       /* menu.add(0, MENU_DURATION, 0,
                duration.replace("%s", format)).setIcon(
                        R.drawable.ic_menu_duration).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
*/
        // Slide layout
        int resId;
        if(LayoutModel.getLayoutType() == LayoutModel.LAYOUT_TOP_TEXT) {
            resId = R.string.layout_top;
        } else {
            resId = R.string.layout_bottom;
        }
        // FIXME: set correct icon when layout icon is available.
        /*menu.add(0, MENU_LAYOUT, 0, resId).setIcon(R.drawable.ic_menu_picture).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
*///        super.onPrepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        //gionee gaoj added for CR00725602 20121201 start
        case android.R.id.home:
            finish();
            break;
        //gionee gaoj added for CR00725602 20121201 end
            case MENU_PREVIEW_SLIDESHOW:
                previewSlideshow();
                break;

            case MENU_REMOVE_TEXT:
                SlideModel slide = mSlideshowModel.get(mPosition);
                if (slide != null) {
                    slide.removeText();
                }
                showSizeDisplay();
                break;

            case MENU_ADD_PICTURE:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType(ContentType.IMAGE_UNSPECIFIED);
                if (FeatureOption.MTK_DRM_APP) {
                    intent.putExtra(DrmExtra.EXTRA_DRM_LEVEL, DrmExtra.DRM_LEVEL_SD);
                }   
                startActivityForResult(intent, REQUEST_CODE_CHANGE_PICTURE);
                break;

            case MENU_TAKE_PICTURE:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // We have to pass a uri to store the picture data, otherwise the camera will return
                // a very small image bitmap.
                intent.putExtra(MediaStore.EXTRA_OUTPUT, TempFileProvider.SCRAP_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
                break;

            case MENU_DEL_PICTURE:
                mSlideshowEditor.removeImage(mPosition);
                setReplaceButtonText(R.string.add_picture);
                showSizeDisplay();
                break;
            case MENU_RECORD_SOUND:
                slide = mSlideshowModel.get(mPosition);
                int currentSlideSize = slide.getSlideSize();
                long sizeLimit = ComposeMessageActivity.computeAttachmentSizeLimit(mSlideshowModel,
                        currentSlideSize);
                MessageUtils.recordSound(this, REQUEST_CODE_RECORD_SOUND, sizeLimit);
                break;
            case MENU_ADD_AUDIO:
                MessageUtils.selectRingtone(SlideEditorActivity.this, REQUEST_CODE_ATTACH_RINGTONE);                                
                break;
            case MENU_ADD_SD_SOUND:
                if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                   Toast.makeText(SlideEditorActivity.this, getString(R.string.Insert_sdcard),
                           Toast.LENGTH_LONG).show();
                   break;
                } else {
                    MessageUtils.selectAudio(SlideEditorActivity.this, REQUEST_CODE_ATTACH_SOUND);
                }
                break;
            case MENU_DEL_AUDIO:
                mSlideshowEditor.removeAudio(mPosition);
                showSizeDisplay();
                break;

            case MENU_ADD_VIDEO:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContentType.VIDEO_UNSPECIFIED);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
 
                if (FeatureOption.MTK_DRM_APP) {                 
                    intent.putExtra(DrmExtra.EXTRA_DRM_LEVEL, DrmExtra.DRM_LEVEL_SD);
                }

                startActivityForResult(intent, REQUEST_CODE_CHANGE_VIDEO);
                break;

            case MENU_TAKE_VIDEO:
                slide = mSlideshowModel.get(mPosition);
                currentSlideSize = slide.getSlideSize();
                sizeLimit = ComposeMessageActivity.computeAttachmentSizeLimit(mSlideshowModel,
                        currentSlideSize);
                if (sizeLimit > ComposeMessageActivity.MIN_SIZE_FOR_CAPTURE_VIDEO) {
                    MessageUtils.recordVideo(this, REQUEST_CODE_TAKE_VIDEO, sizeLimit);
                } else {
                    Toast.makeText(this,
                            getString(R.string.message_too_big_for_video),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case MENU_DEL_VIDEO:
                mSlideshowEditor.removeVideo(mPosition);
                showSizeDisplay();
                break;

            case MENU_ADD_SLIDE:
                mPosition++;
                if ( mSlideshowEditor.addNewSlide(mPosition) ) {
                    // add successfully
                    showCurrentSlide();
                    showSizeDisplay();
                } else {
                    // move position back
                    mPosition--;
                    Toast.makeText(this, R.string.cannot_add_slide_anymore,
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case MENU_LAYOUT:
                showLayoutSelectorDialog();
                break;

            case MENU_DURATION:
                showDurationDialog();
                break;
        }
        showSizeDisplay();
        return super.onOptionsItemSelected(item);
    }

    private void setReplaceButtonText(int text) {
        mReplaceImage.setText(text);
    }

    private void showDurationDialog() {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
//        builder.setIcon(R.drawable.ic_mms_duration);
        String title = getResources().getString(R.string.duration_selector_title);
        builder.setTitle(title + (mPosition + 1) + "/" + mSlideshowModel.size());

        builder.setItems(R.array.select_dialog_items,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if ((which >= 0) && (which < NUM_DIRECT_DURATIONS)) {
                    mSlideshowEditor.changeDuration(
                            mPosition, (which + 1) * 1000);
                } else {
                    Intent intent = new Intent(SlideEditorActivity.this,
                            EditSlideDurationActivity.class);
                    intent.putExtra(EditSlideDurationActivity.SLIDE_INDEX, mPosition);
                    intent.putExtra(EditSlideDurationActivity.SLIDE_TOTAL,
                            mSlideshowModel.size());
                    intent.putExtra(EditSlideDurationActivity.SLIDE_DUR,
                            mSlideshowModel.get(mPosition).getDuration() / 1000); // in seconds
                    startActivityForResult(intent, REQUEST_CODE_CHANGE_DURATION);
                }
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void showLayoutSelectorDialog() {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
//        builder.setIcon(R.drawable.ic_mms_layout);

        String title = getResources().getString(R.string.layout_selector_title);
        builder.setTitle(title + (mPosition + 1) + "/" + mSlideshowModel.size());

        LayoutSelectorAdapter adapter = new LayoutSelectorAdapter(this);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Top text.
                        mSlideshowEditor.changeLayout(LayoutModel.LAYOUT_TOP_TEXT);
                        break;
                    case 1: // Bottom text.
                        mSlideshowEditor.changeLayout(LayoutModel.LAYOUT_BOTTOM_TEXT);
                        break;
                }
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch(requestCode) {
            case REQUEST_CODE_EDIT_TEXT:
                // XXX where does this come from?  Action is probably not the
                // right place to have the text...
                try {
                    mSlideshowEditor.changeText(mPosition, data.getAction());
                } catch (ExceedMessageSizeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.exceed_message_size_limitation),
                            getResourcesString(R.string.failed_to_add_media, getAudioString()));
                }
                showSizeDisplay();
                break;

            case REQUEST_CODE_TAKE_PICTURE:
                Uri pictureUri = null;
                boolean showError = false;
                try {
                    pictureUri = TempFileProvider.renameScrapFile(".jpg",
                            Integer.toString(mPosition), this);

                    if (pictureUri == null) {
                        showError = true;
                    } else {
                        mSlideshowEditor.changeImage(mPosition, pictureUri);
                        setReplaceButtonText(R.string.replace_image);
                    }
                } catch (MmsException e) {
                    Log.e(TAG, "add image failed", e);
                    notifyUser("add picture failed");
                    Toast.makeText(SlideEditorActivity.this,
                            getResourcesString(R.string.failed_to_add_media, getPictureString()),
                            Toast.LENGTH_SHORT).show();
                }catch (RestrictedResolutionException e){
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.select_different_media_type),
                            getResourcesString(R.string.image_resolution_too_large));
                } catch (UnsupportContentTypeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.unsupported_media_format, getPictureString()),
                            getResourcesString(R.string.select_different_media, getPictureString()));
                } catch (ResolutionException e) {
                    MessageUtils.resizeImageAsync(this, pictureUri, new Handler(),
                            mResizeImageCallback, false);
                } catch (ExceedMessageSizeException e) {
                    MessageUtils.resizeImageAsync(this, pictureUri, new Handler(),
                            mResizeImageCallback, false);
                } catch (ContentRestrictionException e){
                    addRestrictedMedia(pictureUri, requestCode, R.string.confirm_restricted_image);
                }
                showSizeDisplay();
                break;

            case REQUEST_CODE_CHANGE_PICTURE:
                try {
                    mSlideshowEditor.changeImage(mPosition, data.getData());
                    setReplaceButtonText(R.string.replace_image);
                } catch (MmsException e) {
                    Log.e(TAG, "add image failed", e);
                    notifyUser("add picture failed");
                    Toast.makeText(SlideEditorActivity.this,
                            getResourcesString(R.string.failed_to_add_media, getPictureString()),
                            Toast.LENGTH_SHORT).show();
                } catch (RestrictedResolutionException e){
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.select_different_media_type),
                            getResourcesString(R.string.image_resolution_too_large));
                } catch (UnsupportContentTypeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.unsupported_media_format, getPictureString()),
                            getResourcesString(R.string.select_different_media, getPictureString()));
                } catch (ResolutionException e) {
                    MessageUtils.resizeImageAsync(this, data.getData(), new Handler(),
                            mResizeImageCallback, false);
                } catch (ExceedMessageSizeException e) {
                    MessageUtils.resizeImageAsync(this, data.getData(), new Handler(),
                            mResizeImageCallback, false);
                } catch (ContentRestrictionException e){
                    addRestrictedMedia(data.getData(), requestCode, R.string.confirm_restricted_image);
                }
                showSizeDisplay();
                break;

            case REQUEST_CODE_ATTACH_RINGTONE:
                Uri uri = (Uri) data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (Settings.System.DEFAULT_RINGTONE_URI.equals(uri)) {
                    break;
                }
                try {
                    mSlideshowEditor.changeAudio(mPosition, uri);
                } catch (MmsException e) {
                    Log.e(TAG, "add audio failed", e);
                    notifyUser("add music failed");
                    Toast.makeText(SlideEditorActivity.this,
                            getResourcesString(R.string.failed_to_add_media, getAudioString()),
                            Toast.LENGTH_SHORT).show();
                } catch (UnsupportContentTypeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.unsupported_media_format, getAudioString()),
                            getResourcesString(R.string.select_different_media, getAudioString()));
                } catch (ExceedMessageSizeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.exceed_message_size_limitation),
                            getResourcesString(R.string.failed_to_add_media, getAudioString()));
                }catch (ContentRestrictionException e){
                    addRestrictedMedia(uri, requestCode, R.string.confirm_restricted_audio);
                }
                showSizeDisplay();
                break;
            case REQUEST_CODE_RECORD_SOUND:
            case REQUEST_CODE_ATTACH_SOUND:
                try {
                    mSlideshowEditor.changeAudio(mPosition, data.getData());
                } catch (MmsException e) {
                    Log.e(TAG, "add audio failed", e);
                    notifyUser("add music failed");
                    Toast.makeText(SlideEditorActivity.this,
                            getResourcesString(R.string.failed_to_add_media, getAudioString()),
                            Toast.LENGTH_SHORT).show();
                } catch (UnsupportContentTypeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.unsupported_media_format, getAudioString()),
                            getResourcesString(R.string.select_different_media, getAudioString()));
                } catch (ExceedMessageSizeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.exceed_message_size_limitation),
                            getResourcesString(R.string.failed_to_add_media, getAudioString()));
                }catch (ContentRestrictionException e){
                    addRestrictedMedia(data.getData(), requestCode, R.string.confirm_restricted_audio);
                }
                showSizeDisplay();
                break;

            case REQUEST_CODE_TAKE_VIDEO:
                try {
                    Uri videoUri = TempFileProvider.renameScrapVideoFile(".3gp",
                            Integer.toString(mPosition), this);

                    mSlideshowEditor.changeVideo(mPosition, videoUri);
                } catch (MmsException e) {
                    notifyUser("add video failed");
                    Toast.makeText(SlideEditorActivity.this,
                            getResourcesString(R.string.failed_to_add_media, getVideoString()),
                            Toast.LENGTH_SHORT).show();
                } catch (UnsupportContentTypeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.unsupported_media_format, getVideoString()),
                            getResourcesString(R.string.select_different_media, getVideoString()));
                } catch (ExceedMessageSizeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.exceed_message_size_limitation),
                            getResourcesString(R.string.failed_to_add_media, getVideoString()));
                }
                break;

            case REQUEST_CODE_CHANGE_VIDEO:
                try {
                    mSlideshowEditor.changeVideo(mPosition, data.getData());
                } catch (MmsException e) {
                    Log.e(TAG, "add video failed", e);
                    notifyUser("add video failed");
                    Toast.makeText(SlideEditorActivity.this,
                            getResourcesString(R.string.failed_to_add_media, getVideoString()),
                            Toast.LENGTH_SHORT).show();
                } catch (UnsupportContentTypeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.unsupported_media_format, getVideoString()),
                            getResourcesString(R.string.select_different_media, getVideoString()));
                } catch (ExceedMessageSizeException e) {
                    MessageUtils.showErrorDialog(SlideEditorActivity.this,
                            getResourcesString(R.string.exceed_message_size_limitation),
                            getResourcesString(R.string.failed_to_add_media, getVideoString()));
                } catch (ContentRestrictionException e){
                    addRestrictedMedia(data.getData(), requestCode, R.string.confirm_restricted_video);
                }
                showSizeDisplay();
                break;

            case REQUEST_CODE_CHANGE_DURATION:
                mSlideshowEditor.changeDuration(mPosition,
                    Integer.valueOf(data.getAction()) * 1000);
                break;
        }
        showSizeDisplay();
    }
    private Uri mRestritedUri = null;
    private int mMediaType = REQUEST_CODE_EDIT_TEXT;
    private void addRestrictedMedia(Uri mediaUri, int type, int messageId){
        mRestritedUri = mediaUri;
        mMediaType = type;
        if (WorkingMessage.sCreationMode == WorkingMessage.WARNING_TYPE){
            new AuroraAlertDialog.Builder(this)
            .setTitle(R.string.unsupport_media_type)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setMessage(messageId)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {     
                public final void onClick(DialogInterface dialog, int which) {
                    if (mRestritedUri == null || mMediaType == REQUEST_CODE_EDIT_TEXT){
                        return;
                    }
                    int createMode = WorkingMessage.sCreationMode;
                    WorkingMessage.sCreationMode = 0;
                    switch (mMediaType){
                    case REQUEST_CODE_TAKE_PICTURE:
                    case REQUEST_CODE_CHANGE_PICTURE:{
                         try {
                             mSlideshowEditor.changeImage(mPosition, mRestritedUri);
                             setReplaceButtonText(R.string.replace_image);
                         } catch (MmsException e) {
                             Log.e(TAG, "add image failed", e);
                             notifyUser("add picture failed");
                             Toast.makeText(SlideEditorActivity.this,
                                     getResourcesString(R.string.failed_to_add_media, getPictureString()),
                                     Toast.LENGTH_SHORT).show();
                         } catch (UnsupportContentTypeException e) {
                             MessageUtils.showErrorDialog(SlideEditorActivity.this,
                                     getResourcesString(R.string.unsupported_media_format, getPictureString()),
                                     getResourcesString(R.string.select_different_media, getPictureString()));
                         } catch (ResolutionException e) {
                             MessageUtils.resizeImageAsync(SlideEditorActivity.this, mRestritedUri, new Handler(),
                                     mResizeImageCallback, false);
                         } catch (ExceedMessageSizeException e) {
                             MessageUtils.resizeImageAsync(SlideEditorActivity.this, mRestritedUri, new Handler(),
                                     mResizeImageCallback, false);
                         }
                         break;
                    }
                    case REQUEST_CODE_ATTACH_RINGTONE:
                        try {
                            mSlideshowEditor.changeAudio(mPosition, mRestritedUri);
                        } catch (MmsException e) {
                            Log.e(TAG, "add audio failed", e);
                            notifyUser("add music failed");
                            Toast.makeText(SlideEditorActivity.this,
                                    getResourcesString(R.string.failed_to_add_media, getAudioString()),
                                    Toast.LENGTH_SHORT).show();
                        } catch (UnsupportContentTypeException e) {
                            MessageUtils.showErrorDialog(SlideEditorActivity.this,
                                    getResourcesString(R.string.unsupported_media_format, getAudioString()),
                                    getResourcesString(R.string.select_different_media, getAudioString()));
                        } catch (ExceedMessageSizeException e) {
                            MessageUtils.showErrorDialog(SlideEditorActivity.this,
                                    getResourcesString(R.string.exceed_message_size_limitation),
                                    getResourcesString(R.string.failed_to_add_media, getAudioString()));
                        }
                        break;
                    case REQUEST_CODE_RECORD_SOUND:
                    case REQUEST_CODE_ATTACH_SOUND: {
                        try {
                            mSlideshowEditor.changeAudio(mPosition, mRestritedUri);
                        } catch (MmsException e) {
                            Log.e(TAG, "add audio failed", e);
                            notifyUser("add music failed");
                            Toast.makeText(SlideEditorActivity.this,
                                    getResourcesString(R.string.failed_to_add_media, getAudioString()),
                                    Toast.LENGTH_SHORT).show();
                        } catch (UnsupportContentTypeException e) {
                            MessageUtils.showErrorDialog(SlideEditorActivity.this,
                                    getResourcesString(R.string.unsupported_media_format, getAudioString()),
                                    getResourcesString(R.string.select_different_media, getAudioString()));
                        } catch (ExceedMessageSizeException e) {
                            MessageUtils.showErrorDialog(SlideEditorActivity.this,
                                    getResourcesString(R.string.exceed_message_size_limitation),
                                    getResourcesString(R.string.failed_to_add_media, getAudioString()));
                        }
                        break;
                    }
                    case REQUEST_CODE_CHANGE_VIDEO:{
                        try {
                            mSlideshowEditor.changeVideo(mPosition, mRestritedUri);
                        } catch (MmsException e) {
                            Log.e(TAG, "add video failed", e);
                            notifyUser("add video failed");
                            Toast.makeText(SlideEditorActivity.this,
                                    getResourcesString(R.string.failed_to_add_media, getVideoString()),
                                    Toast.LENGTH_SHORT).show();
                        } catch (UnsupportContentTypeException e) {
                            MessageUtils.showErrorDialog(SlideEditorActivity.this,
                                    getResourcesString(R.string.unsupported_media_format, getVideoString()),
                                    getResourcesString(R.string.select_different_media, getVideoString()));
                        } catch (ExceedMessageSizeException e) {
                            MessageUtils.showErrorDialog(SlideEditorActivity.this,
                                    getResourcesString(R.string.exceed_message_size_limitation),
                                    getResourcesString(R.string.failed_to_add_media, getVideoString()));
                        }
                        break;
                    }
                    default:
                        Log.e(TAG, "error Restricted Midea: dataUri=" + mRestritedUri);
                    }

                    WorkingMessage.sCreationMode = createMode;                                              
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        }else{
            switch (mMediaType){
            case REQUEST_CODE_TAKE_PICTURE:
            case REQUEST_CODE_CHANGE_PICTURE:{                
                 MessageUtils.showErrorDialog(SlideEditorActivity.this,
                         getResourcesString(R.string.unsupported_media_format, getPictureString()),
                         getResourcesString(R.string.select_different_media, getPictureString()));
             
                 break;
            }
            case REQUEST_CODE_ATTACH_RINGTONE:
            case REQUEST_CODE_RECORD_SOUND:
            case REQUEST_CODE_ATTACH_SOUND:{                
                MessageUtils.showErrorDialog(SlideEditorActivity.this,
                        getResourcesString(R.string.unsupported_media_format, getAudioString()),
                        getResourcesString(R.string.select_different_media, getAudioString()));        
                break;
            }
            case REQUEST_CODE_CHANGE_VIDEO:{                
                MessageUtils.showErrorDialog(SlideEditorActivity.this,
                        getResourcesString(R.string.unsupported_media_format, getVideoString()),
                        getResourcesString(R.string.select_different_media, getVideoString()));                
            }
          }
        }
    }
    private final ResizeImageResultCallback mResizeImageCallback = new ResizeImageResultCallback() {
        public void onResizeResult(PduPart part, boolean append) {
            Context context = SlideEditorActivity.this;
            if (part == null) {
                Toast.makeText(SlideEditorActivity.this,
                        getResourcesString(R.string.failed_to_add_media, getPictureString()),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            int createMode = WorkingMessage.sCreationMode;
            WorkingMessage.sCreationMode = 0;
            try {
                long messageId = ContentUris.parseId(mUri);
                PduPersister persister = PduPersister.getPduPersister(context);
                Uri newUri = persister.persistPart(part, messageId);
                mSlideshowEditor.changeImage(mPosition, newUri);

                setReplaceButtonText(R.string.replace_image);
            } catch (MmsException e) {
                notifyUser("add picture failed");
                Toast.makeText(SlideEditorActivity.this,
                        getResourcesString(R.string.failed_to_add_media, getPictureString()),
                        Toast.LENGTH_SHORT).show();
            } catch (UnsupportContentTypeException e) {
                MessageUtils.showErrorDialog(SlideEditorActivity.this,
                        getResourcesString(R.string.unsupported_media_format, getPictureString()),
                        getResourcesString(R.string.select_different_media, getPictureString()));
            } catch (ResolutionException e) {
                MessageUtils.showErrorDialog(SlideEditorActivity.this,
                        getResourcesString(R.string.failed_to_resize_image),
                        getResourcesString(R.string.resize_image_error_information));
            } catch (ExceedMessageSizeException e) {
                MessageUtils.showErrorDialog(SlideEditorActivity.this,
                        getResourcesString(R.string.exceed_message_size_limitation),
                        getResourcesString(R.string.failed_to_add_media, getPictureString()));
            }finally{
                WorkingMessage.sCreationMode = createMode;
            }
            showSizeDisplay();
        }
    };

    private String getResourcesString(int id, String mediaName) {
        Resources r = getResources();
        return r.getString(id, mediaName);
    }

    private String getResourcesString(int id) {
        Resources r = getResources();
        return r.getString(id);
    }

    private String getAudioString() {
        return getResourcesString(R.string.type_audio);
    }

    private String getPictureString() {
        return getResourcesString(R.string.type_picture);
    }

    private String getVideoString() {
        return getResourcesString(R.string.type_video);
    }

    private void notifyUser(String message) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "notifyUser: message=" + message);
        }
    }

    private void showCurrentSlide() {
        mPresenter.setLocation(mPosition);
        mPresenter.present();
        updateTitle();
        showDrmLock();
        if (mSlideshowModel.get(mPosition).hasImage()) {
            setReplaceButtonText(R.string.replace_image);
        } else {
            setReplaceButtonText(R.string.add_picture);
        }
    }
}
