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
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.FileAttachmentModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import aurora.widget.AuroraButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
// a0
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.MmsConfig;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.telephony.SmsMessage;
import android.widget.ImageView;
import android.widget.TextView;
import com.aurora.featureoption.FeatureOption;
import gionee.drm.GnDrmManagerClient;

import java.util.List;
// a1
//gionee gaoj 2012-4-10 added for CR00555790 start
import android.graphics.Color;
import com.gionee.mms.slide.ViewToolbar;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import com.android.mms.MmsApp;
//gionee gaoj 2012-4-10 added for CR00555790 end
/**
 * This is an embedded editor/view to add photos and sound/video clips
 * into a multimedia message.
 */
public class AttachmentEditor extends LinearLayout {
    private static final String TAG = "AttachmentEditor";

    static final int MSG_EDIT_SLIDESHOW   = 1;
    static final int MSG_SEND_SLIDESHOW   = 2;
    static final int MSG_PLAY_SLIDESHOW   = 3;
    static final int MSG_REPLACE_IMAGE    = 4;
    static final int MSG_REPLACE_VIDEO    = 5;
    static final int MSG_REPLACE_AUDIO    = 6;
    static final int MSG_PLAY_VIDEO       = 7;
    static final int MSG_PLAY_AUDIO       = 8;
    static final int MSG_VIEW_IMAGE       = 9;
    static final int MSG_REMOVE_ATTACHMENT = 10;

    private final Context mContext;
    private Handler mHandler;

    private SlideViewInterface mView;
    // add for vCard
    private View mFileAttachmentView;
    private SlideshowModel mSlideshow;
    private Presenter mPresenter;
    private boolean mCanSend;
    private AuroraButton mSendButton;

    //gionee gaoj 2012-4-10 added for CR00555790 start
    private TextChangeForOneSlideListener mListener;
    //gionee gaoj 2012-4-10 added for CR00555790 end
    public AttachmentEditor(Context context, AttributeSet attr) {
        super(context, attr);
        mContext = context;
        //gionee gaoj 2012-4-16 added for CR00555790 start
        if (MmsApp.mGnMessageSupport == true) {
            mToolbar = new AttachmentViewToolBar(this);
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (isToolbarShowing()) {
                                // Aurora xuyong 2013-11-20 deleted for aurora's new feature start
                                //hideRecipientToolbar();
                                // Aurora xuyong 2013-11-20 deleted for aurora's new feature end
                            } else {
                                // Aurora xuyong 2013-11-20 deleted for aurora's new feature start
                                //showRecipientToolbar();
                                // Aurora xuyong 2013-11-20 deleted for aurora's new feature end
                            }
                        }
                    });
                }
            });
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    /**
     * Returns true if the attachment editor has an attachment to show.
     */
    public boolean update(WorkingMessage msg) {
        hideView();
        mView = null;
        // add for vcard
        mFileAttachmentView = null;
// a0
        mWorkingMessage = msg;
// a1
        // If there's no attachment, we have nothing to do.
        if (!msg.hasAttachment()) {
            return false;
        }

        // Get the slideshow from the message.
        mSlideshow = msg.getSlideshow();
// e0
//        mView = createView();
        try {
            // for vcard: file attachment view and other views are exclusive to each other
            if (mSlideshow.sizeOfFilesAttach() > 0) {
                mFileAttachmentView = createFileAttachmentView(msg);
                return true;
            }
            //gionee gaoj 2012-4-10 added for CR00555790 start
            if (MmsApp.mGnMessageSupport) {
                mView = createGnView(msg);
            } else {
            mView = createView(msg);
            }
            //gionee gaoj 2012-4-10 added for CR00555790 end
        } catch(IllegalArgumentException e) {
            return false;
        }
// e1

        if ((mPresenter == null) || !mSlideshow.equals(mPresenter.getModel())) {
            mPresenter = PresenterFactory.getPresenter(
                    "MmsThumbnailPresenter", mContext, mView, mSlideshow);
        } else {
            mPresenter.setView(mView);
        }

        mPresenter.present();
        return true;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void setCanSend(boolean enable) {
        if (mCanSend != enable) {
            mCanSend = enable;
            updateSendButton();
        }
    }

    private void updateSendButton() {
        if (null != mSendButton) {
            mSendButton.setEnabled(mCanSend);
            mSendButton.setFocusable(mCanSend);
        }
    }

    public void hideView() {
        if (mView != null) {
            ((View)mView).setVisibility(View.GONE);
        }
        // add for vcard
        if (mFileAttachmentView != null) {
            mFileAttachmentView.setVisibility(View.GONE);
        }
    }

    private View getStubView(int stubId, int viewId) {
        View view = findViewById(viewId);
        if (view == null) {
            ViewStub stub = (ViewStub) findViewById(stubId);
            view = stub.inflate();
        }

        return view;
    }

    private class MessageOnClick implements OnClickListener {
        private int mWhat;

        public MessageOnClick(int what) {
            mWhat = what;
        }

        public void onClick(View v) {
            //gionee gaoj 2012-5-8 added for CR00588986 start
            if(MmsApp.mGnMessageSupport) {
                // gionee zhouyj 2012-05-24 modify for CR00607785 start 
                //Aurora xuyong 2013-10-11 deleted for aurora's new feature start
                //if (isShowingToolbar() || mWhat == MSG_REMOVE_ATTACHMENT){
                //Aurora xuyong 2013-10-11 deleted for aurora's new feature end
                // gionee zhouyj 2012-05-24 modify for CR00607785 end 
                    Message msg = Message.obtain(mHandler, mWhat);
                    msg.sendToTarget();
                    hideToolbar();
                //Aurora xuyong 2013-10-11 deleted for aurora's new feature start
                //}
                //Aurora xuyong 2013-10-11 deleted for aurora's new feature end
            } else {
            //gionee gaoj 2012-5-8 added for CR00588986 end
            Message msg = Message.obtain(mHandler, mWhat);
            msg.sendToTarget();
//Gionee guoyx 20121023 by CR00705464 for MTK ALPS00283177 REMOVED BEGIN
//            if (mWhat == MSG_EDIT_SLIDESHOW) {
//                v.setEnabled(false);
//            }
//Gionee guoyx 20121023 by CR00705464 for MTK ALPS00283177 REMOVED END
            //gionee gaoj 2012-5-8 added for CR00588986 start
            }
            //gionee gaoj 2012-5-8 added for CR00588986 end
            //gionee gaoj 2012-5-8 modified for CR00600582 start
//            if (mWhat == MSG_EDIT_SLIDESHOW) {
//                v.setEnabled(false);
//            }
            //gionee gaoj 2012-5-8 modified for CR00600582 end
        }
    }
// mo
//    private SlideViewInterface createView() {
    private SlideViewInterface createView(WorkingMessage msg) {
// m1
        boolean inPortrait = inPortraitMode();
        if (mSlideshow.size() > 1) {
// m0
//            return createSlideshowView(inPortrait);
            return createSlideshowView(inPortrait, msg);
// m1
        }

        SlideModel slide = mSlideshow.get(0);
        if (slide.hasImage()) {
            return createMediaView(
                    R.id.image_attachment_view_stub,
                    R.id.image_attachment_view,
                    R.id.view_image_button, R.id.replace_image_button, R.id.remove_image_button,
// m0
//                    MSG_VIEW_IMAGE, MSG_REPLACE_IMAGE, MSG_REMOVE_ATTACHMENT);
                    R.id.media_size_info, msg.getCurrentMessageSize(),
                        MSG_VIEW_IMAGE, MSG_REPLACE_IMAGE, MSG_REMOVE_ATTACHMENT, msg);           
// m1
        } else if (slide.hasVideo()) {
            return createMediaView(
                    R.id.video_attachment_view_stub,
                    R.id.video_attachment_view,
                    R.id.view_video_button, R.id.replace_video_button, R.id.remove_video_button,
// m0
//                    MSG_PLAY_VIDEO, MSG_REPLACE_VIDEO, MSG_REMOVE_ATTACHMENT);
                    R.id.media_size_info, msg.getCurrentMessageSize(),
                        MSG_PLAY_VIDEO, MSG_REPLACE_VIDEO, MSG_REMOVE_ATTACHMENT, msg);                
// m1
        } else if (slide.hasAudio()) {
            return createMediaView(
                    R.id.audio_attachment_view_stub,
                    R.id.audio_attachment_view,
                    R.id.play_audio_button, R.id.replace_audio_button, R.id.remove_audio_button,
// mo
//                    MSG_PLAY_AUDIO, MSG_REPLACE_AUDIO, MSG_REMOVE_ATTACHMENT);
                    R.id.media_size_info, msg.getCurrentMessageSize(),
                        MSG_PLAY_AUDIO, MSG_REPLACE_AUDIO, MSG_REMOVE_ATTACHMENT, msg);    
// m1
        } else {
            throw new IllegalArgumentException();
        }
    }

    // add for vcard
    private View createFileAttachmentView(WorkingMessage msg) {
        List<FileAttachmentModel> attachFiles = mSlideshow.getAttachFiles();
        if (attachFiles == null || attachFiles.size() != 1) {
            Log.e(TAG, "createFileAttachmentView, oops no attach files found.");
            return null;
        }
        FileAttachmentModel attach = attachFiles.get(0);
        Log.i(TAG, "createFileAttachmentView, attach " + attach.toString());
        final View view = getStubView(R.id.file_attachment_view_stub, R.id.file_attachment_view);
        view.setVisibility(View.VISIBLE);
        final ImageView thumb = (ImageView) view.findViewById(R.id.file_attachment_thumbnail);
        final TextView name = (TextView) view.findViewById(R.id.file_attachment_name_info);
        String nameText = null;
        int thumbResId = -1;
        if (attach.isVCard()) {
            nameText = mContext.getString(R.string.file_attachment_vcard_name, attach.getSrc());
            thumbResId = -1;//R.drawable.ic_vcard_attach;
        }
        name.setText(nameText);
        thumb.setImageResource(thumbResId);
        final TextView size = (TextView) view.findViewById(R.id.file_attachment_size_info);
        size.setText(MessageUtils.getHumanReadableSize(attach.getAttachSize())
                +"/"+MmsConfig.getUserSetMmsSizeLimit(false) + "K");
        final ImageView remove = (ImageView) view.findViewById(R.id.file_attachment_button_remove);
        //final ImageView divider = (ImageView) view.findViewById(R.id.file_attachment_divider);
        //divider.setVisibility(View.VISIBLE);
        remove.setVisibility(View.VISIBLE);
        remove.setOnClickListener(new MessageOnClick(MSG_REMOVE_ATTACHMENT));
        return view;
    }

    /**
     * What is the current orientation?
     */
    private boolean inPortraitMode() {
        final Configuration configuration = mContext.getResources().getConfiguration();
        return configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private SlideViewInterface createMediaView(
            int stub_view_id, int real_view_id,
            int view_button_id, int replace_button_id, int remove_button_id,
// m0
//            int view_message, int replace_message, int remove_message) {
            int size_view_id, int msgSize,
            int view_message, int replace_message, int remove_message, WorkingMessage msg) {
// m1
        LinearLayout view = (LinearLayout)getStubView(stub_view_id, real_view_id);
        view.setVisibility(View.VISIBLE);

        AuroraButton viewButton = (AuroraButton) view.findViewById(view_button_id);
        AuroraButton replaceButton = (AuroraButton) view.findViewById(replace_button_id);
        AuroraButton removeButton = (AuroraButton) view.findViewById(remove_button_id);

// a0
        // show Mms Size  
        mMediaSize = (TextView) view.findViewById(size_view_id); 
        int sizeShow = (msgSize - 1)/1024 + 1;
        String info = sizeShow + "K/" + MmsConfig.getUserSetMmsSizeLimit(false) + "K";
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            attachmentCallBack(info);
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        mMediaSize.setText(info); 
// a1

        viewButton.setOnClickListener(new MessageOnClick(view_message));
        replaceButton.setOnClickListener(new MessageOnClick(replace_message));
        removeButton.setOnClickListener(new MessageOnClick(remove_message));

// a0
        if (mFlagMini) {
            replaceButton.setVisibility(View.GONE);
        }
// a1
        return (SlideViewInterface) view;
    }

    //gionee gaoj 2012-4-10 added for CR00555790 start
    private SlideViewInterface createGnView(WorkingMessage msg){
        boolean inPortrait = inPortraitMode();
        if (mSlideshow.size() > 1) {
            return createGnSlideshowView(inPortrait, msg.getCurrentMessageSize());
        }

        SlideModel slide = mSlideshow.get(0);

        if (slide.hasImage()) {
            return createGnMediaView(
                    inPortrait ? R.id.gn_image_attachment_view_portrait_stub :
                        R.id.gn_image_attachment_view_landscape_stub,
                    inPortrait ? R.id.gn_image_attachment_view_portrait :
                        R.id.gn_image_attachment_view_landscape,
                    R.id.media_size_info, msg.getCurrentMessageSize(),
                    MSG_VIEW_IMAGE, MSG_REPLACE_IMAGE, MSG_REMOVE_ATTACHMENT,
                    R.id.image_content);
        } else if (slide.hasVideo()) {
            return createGnMediaView(
                    inPortrait ? R.id.gn_video_attachment_view_portrait_stub :
                        R.id.gn_video_attachment_view_landscape_stub,
                    inPortrait ? R.id.gn_video_attachment_view_portrait :
                        R.id.gn_video_attachment_view_landscape,
                    R.id.media_size_info, msg.getCurrentMessageSize(),
                    MSG_PLAY_VIDEO, MSG_REPLACE_VIDEO, MSG_REMOVE_ATTACHMENT,
                    R.id.video_thumbnail);
        } else if (slide.hasAudio()) {
            return createGnMediaView(
                    inPortrait ? R.id.gn_audio_attachment_view_portrait_stub :
                        R.id.gn_audio_attachment_view_landscape_stub,
                    inPortrait ? R.id.gn_audio_attachment_view_portrait :
                        R.id.gn_audio_attachment_view_landscape,
                    R.id.media_size_info, msg.getCurrentMessageSize(),
                    MSG_PLAY_AUDIO, MSG_REPLACE_AUDIO, MSG_REMOVE_ATTACHMENT,
                    R.id.audio_image_content);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private SlideViewInterface createGnMediaView(int stub_view_id, int real_view_id,
            int size_view_id, int msgSize, int view_message, int replace_message,
            int remove_message, int image_view_id) {

        LinearLayout view = (LinearLayout) getStubView(stub_view_id, real_view_id);
        view.setVisibility(View.VISIBLE);

        // show Mms Size
        mMediaSize = (TextView) view.findViewById(size_view_id);
        int sizeShow = (msgSize - 1)/1024 + 1;
        String info = sizeShow + "K/" + MmsConfig.getUserSetMmsSizeLimit(false) + "K";
        if (MmsApp.mGnMessageSupport) {
            attachmentCallBack(info);
        }
        mMediaSize.setText(info);

        getToolbar().setToolBarClickListener(view_message, replace_message, remove_message, -1);
        ImageView contentView = (ImageView)findViewById(image_view_id);
        if (contentView != null) {
            if (R.id.image_content == image_view_id) {
                //Aurora xuyong 2013-10-11 added for aurora's new feature start
                contentView.setOnClickListener(new MessageOnClick(view_message));
                //Aurora xuyong 2013-10-11 added for aurora's new feature end
                getToolbar().setHostView(contentView, AttachmentViewToolBar.ID_ATTACHMENT_IMAGE);
            } else if (R.id.video_thumbnail == image_view_id) {
                //Aurora xuyong 2013-10-11 added for aurora's new feature start
                contentView.setOnClickListener(null);
                ImageView playVideoButton = (ImageView)findViewById(R.id.aurora_video_play);
                if (view_message != -1) {
                    playVideoButton.setOnClickListener(new MessageOnClick(view_message));
                }
                //Aurora xuyong 2013-10-11 added for aurora's new feature end
                getToolbar().setHostView(contentView, AttachmentViewToolBar.ID_ATTACHMENT_VIDEO);
            } else if (R.id.audio_image_content == image_view_id) {
                //Aurora xuyong 2013-10-11 added for aurora's new feature start
                contentView.setOnClickListener(null);
                ImageView playAudioButton = (ImageView)findViewById(R.id.audio_name);
                if (view_message != -1) {
                    playAudioButton.setOnClickListener(new MessageOnClick(view_message));
                }
                //Aurora xuyong 2013-10-11 added for aurora's new feature end
                getToolbar().setHostView(contentView, AttachmentViewToolBar.ID_ATTACHMENT_AUDIO);
            }
        }

        return (SlideViewInterface) view;
    }

    private SlideViewInterface createGnSlideshowView(boolean inPortrait, int msgSize) {
        LinearLayout view;
        view =(LinearLayout) getStubView(inPortrait ?
                R.id.gn_slideshow_attachment_view_portrait_stub :
                    R.id.gn_slideshow_attachment_view_landscape_stub,
                    inPortrait ? R.id.gn_slideshow_attachment_view_portrait :
                        R.id.gn_slideshow_attachment_view_landscape);

        view.setVisibility(View.VISIBLE);
        // show Mms Size
        mMediaSize = (TextView) view.findViewById(R.id.media_size_info);
        int sizeShow = (msgSize - 1)/1024 + 1;
        String info = sizeShow + "K/" + MmsConfig.getUserSetMmsSizeLimit(false) + "K";
        if (MmsApp.mGnMessageSupport) {
            attachmentCallBack(info);
        }
        mMediaSize.setText(info);

        getToolbar().setToolBarClickListener(MSG_PLAY_SLIDESHOW, -1, MSG_REMOVE_ATTACHMENT,
                MSG_EDIT_SLIDESHOW);
        ImageView contentView = (ImageView)findViewById(R.id.slideshow_image);
        // Aurora xuyong 2014-04-18 added for bug #4385 start
        contentView.setOnClickListener(new MessageOnClick(MSG_PLAY_SLIDESHOW));
        // Aurora xuyong 2014-04-18 added for bug #4385 end
        if (contentView != null) {
            getToolbar().setHostView(contentView, AttachmentViewToolBar.ID_ATTACHMENT_SLIDE);
        }
        if (MmsApp.mGnMessageSupport) {
            ImageView playBtn = (ImageView) findViewById(R.id.play_slideshow_button);
            if (playBtn != null) {
                getToolbar().setHostView(playBtn, AttachmentViewToolBar.ID_ATTACHMENT_SLIDE);
            }
        }

        return (SlideViewInterface) view;
    }
    //gionee gaoj 2012-4-10 added for CR00555790 end

// m0
//    private SlideViewInterface createSlideshowView(boolean inPortrait) {
    private SlideViewInterface createSlideshowView(boolean inPortrait, WorkingMessage msg) {
// m1
        LinearLayout view =(LinearLayout) getStubView(
                R.id.slideshow_attachment_view_stub,
                R.id.slideshow_attachment_view);
        view.setVisibility(View.VISIBLE);

        AuroraButton editBtn = (AuroraButton) view.findViewById(R.id.edit_slideshow_button);
        mSendButton = (AuroraButton) view.findViewById(R.id.send_slideshow_button);
// a0
        mSendButton.setOnClickListener(new MessageOnClick(MSG_SEND_SLIDESHOW));
// a1

        updateSendButton();
        final ImageButton playBtn = (ImageButton) view.findViewById(
                R.id.play_slideshow_button);
// a0
        if (FeatureOption.MTK_DRM_APP) {
            if (msg.mHasDrmPart) {
                Log.i(TAG, "mHasDrmPart");
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), -1/*R.drawable.mms_play_btn*/);        
                Drawable front = mContext.getResources().getDrawable(-1/*R.drawable.drm_red_lock*/);
                GnDrmManagerClient drmManager= new GnDrmManagerClient(mContext);
                Bitmap drmBitmap = drmManager.overlayBitmap(bitmap, front);
                playBtn.setImageBitmap(drmBitmap);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        }

        // show Mms Size  
        mMediaSize = (TextView) view.findViewById(R.id.media_size_info); 
        int sizeShow = (msg.getCurrentMessageSize() - 1)/1024 + 1;
        String info = sizeShow + "K/" + MmsConfig.getUserSetMmsSizeLimit(false) + "K";
        
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            attachmentCallBack(info);
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        mMediaSize.setText(info);
// a1

        editBtn.setEnabled(true);
        editBtn.setOnClickListener(new MessageOnClick(MSG_EDIT_SLIDESHOW));
        mSendButton.setOnClickListener(new MessageOnClick(MSG_SEND_SLIDESHOW));
        playBtn.setOnClickListener(new MessageOnClick(MSG_PLAY_SLIDESHOW));

        AuroraButton removeButton = (AuroraButton) view.findViewById(R.id.remove_slideshow_button);
        removeButton.setOnClickListener(new MessageOnClick(MSG_REMOVE_ATTACHMENT));

        return (SlideViewInterface) view;
    }

// a0
    private WorkingMessage mWorkingMessage;
    private boolean mTextIncludedInMms;
    private TextView mMediaSize;
    private ImageView mDrmLock;
    private boolean mFlagMini = false;

    public void update(WorkingMessage msg, boolean isMini) {
        mFlagMini = isMini;
        update(msg);
    }

    public void onTextChangeForOneSlide(CharSequence s) throws ExceedMessageSizeException {

        if (null == mMediaSize || (mWorkingMessage.hasSlideshow() && mWorkingMessage.getSlideshow().size() >1)) {
            return;
        }

        // borrow this method to get the encoding type
        int[] params = SmsMessage.calculateLength(s, false);
        int type = params[3];
        int totalSize = 0;
        if (mWorkingMessage.hasAttachment()) {
            totalSize = mWorkingMessage.getCurrentMessageSize();
        }
        // show
        int sizeShow = (totalSize - 1)/1024 + 1;
        String info = sizeShow + "K/" + MmsConfig.getUserSetMmsSizeLimit(false) + "K";
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            attachmentCallBack(info);
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        mMediaSize.setText(info);
    }
// a1

    //gionee gaoj 2012-4-10 added for CR00555790 start
    AttachmentViewToolBar mToolbar;
    private static final int AUTO_HIDETOOLBAR_TIMEOUT = 3000;

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        hideRecipientToolbar();
    }

    private boolean isToolbarShowing() {
        if (mToolbar != null) {
            return mToolbar.isShowing();
        }
        return false;
    }

    private Runnable mAutoHideToolbarRunnable = new Runnable() {
        public void run() {
            hideToolbar();
        }
    };

    private void hideToolbar() {
        mHandler.removeCallbacks(mAutoHideToolbarRunnable);
        if (mToolbar != null) {
            mToolbar.hide();
        }
    }
    private boolean isShowingToolbar(){
        if(mToolbar != null){
            return mToolbar.isShowing();
        }
        return false;
    }
    
    private synchronized AttachmentViewToolBar getToolbar() {
        if (mToolbar == null) {
            mToolbar = new AttachmentViewToolBar(this);
        }
        return mToolbar;
    }

    private void hideRecipientToolbar() {
        mHandler.removeCallbacks(mAutoHideToolbarRunnable);
        if (mToolbar != null) {
            mToolbar.hide();
        }
    }

    private void showRecipientToolbar() {
        if (isToolbarShowing()) {
            hideRecipientToolbar();
        }
        getToolbar().show();
        mHandler.postDelayed(mAutoHideToolbarRunnable,  AUTO_HIDETOOLBAR_TIMEOUT);
    }

    private  class AttachmentViewToolBar extends ViewToolbar{
        private static final int ID_VIEW = 0x06;
        private static final int ID_REPLACE = 0x07;
        private static final int ID_DEL = 0x08;
        private static final int ID_EDIT = 0x09;

        private TextView mView;
        private TextView mReplace;
        private TextView mDelete;
        private TextView mEdit;

        public static final int ID_ATTACHMENT_SLIDE = 1;
        public static final int ID_ATTACHMENT_VIDEO = 2;
        public static final int ID_ATTACHMENT_IMAGE = 3;
        public static final int ID_ATTACHMENT_AUDIO = 4;

        private int mAttachmentType = 0;

        public AttachmentViewToolBar(View hostView) {
            super(hostView);
            mView = initToolbarItem(ID_VIEW, R.string.view);
            mReplace = initToolbarItem(ID_REPLACE, R.string.replace);
            mDelete = initToolbarItem(ID_DEL, R.string.delete);
            mEdit = initToolbarItem(ID_EDIT, R.string.edit);
        }

        public void setHostView(View hostView, int attachmentType) {
            mHostView = hostView;
            mAttachmentType = attachmentType;
        }

        @Override
        protected void updateToolbarItems() {
            mToolbarGroup.removeAllViews();

            switch(mAttachmentType) {
                case ID_ATTACHMENT_SLIDE:
                    mView.setText(R.string.view);
                    mToolbarGroup.addView(mView);
                    mToolbarGroup.addView(mEdit);
                    mToolbarGroup.addView(mDelete);
                    break;
                case ID_ATTACHMENT_VIDEO:
                    mView.setText(R.string.play);
                    mToolbarGroup.addView(mView);
                    mToolbarGroup.addView(mReplace);
                    mToolbarGroup.addView(mDelete);
                    break;
                case ID_ATTACHMENT_IMAGE:
                    mView.setText(R.string.view);
                    mToolbarGroup.addView(mView);
                    mToolbarGroup.addView(mReplace);
                    mToolbarGroup.addView(mDelete);
                    break;
                case ID_ATTACHMENT_AUDIO:
                    mView.setText(R.string.play);
                    mToolbarGroup.addView(mView);
                    mToolbarGroup.addView(mReplace);
                    mToolbarGroup.addView(mDelete);
                    break;
                default:
                    break;
            }
        }

        protected TextView initToolbarItem(int id, int textResId) {
            TextView textView = new TextView(mContext);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.WHITE);
            textView.setId(id);
            textView.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight, 0);
            textView.setText(textResId);
            return textView;
        }

        void show() {
            if (!mShowing) {

                int[] location = new int[2];
                mHostView.getLocationOnScreen(location);
                if (location[1] < 128) {
                    location[1] = 128;
                }
                showInternal(location[0] + mHostView.getWidth()/ 2 , location[1], 0, false);
            }
        }

        public void setToolBarClickListener(int view_message, int replace_message,
                int remove_message, int edit_message) {

            if (view_message != -1) {
                mView.setOnClickListener(new MessageOnClick(view_message));
            }

            if (replace_message != -1) {
                mReplace.setOnClickListener(new MessageOnClick(replace_message));
            }

            if (remove_message != -1) {
                mDelete.setOnClickListener(new MessageOnClick(remove_message));
            }

            if (edit_message != -1) {
                mEdit.setOnClickListener(new MessageOnClick(edit_message));
            }

            if (mFlagMini) {
                mReplace.setVisibility(View.GONE);
            }
        }
    }
    public boolean dispatchKeyEvent(KeyEvent arg0) {

        if (isToolbarShowing() && arg0.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            hideRecipientToolbar();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        hideRecipientToolbar();
        super.onDetachedFromWindow();
    }

    public interface TextChangeForOneSlideListener {
        public void onTextChangeForOneSlide(String text);
    }

    public void setTextChangeForOneSlideListener(TextChangeForOneSlideListener listener) {
        mListener = listener;
    }

    public void attachmentCallBack(String info) {
        if (null != mListener) {
            mListener.onTextChangeForOneSlide(info);
            mMediaSize.setVisibility(View.GONE);
        }
    }
    //gionee gaoj 2012-4-10 added for CR00555790 end
}
