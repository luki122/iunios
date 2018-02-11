/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.ui;
// Aurora xuyong 2014-03-04 added for aurora's new feature start
// Aurora xuyong 2014-03-07 added for aurora's new feature start
// Aurora xuyong 2015-07-30 added for bug #14494 start
import java.io.File;
// Aurora xuyong 2015-07-30 added for bug #14494 end
import java.lang.ref.WeakReference;
// Aurora xuyong 2014-03-07 added for aurora's new feature end
import java.util.ArrayList;
// Aurora xuyong 2014-03-04 added for aurora's new feature end
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
// Aurora xuyong 2014-03-04 added for aurora's new feature start
import android.os.Handler;
import android.os.Message;
// Aurora xuyong 2014-03-04 added for aurora's new feature end
// Aurora xuyong 2014-09-16 added for bug #8331 start
import android.text.Spannable;
import android.text.Spanned;
// Aurora xuyong 2014-09-16 added for bug #8331 end
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
// Aurora xuyong 2014-09-16 added for bug #8331 start
import android.text.method.LinkMovementMethod;
// Aurora xuyong 2014-09-16 added for bug #8331 end
// Aurora xuyong 2014-03-04 added for aurora's new feature start
import android.text.style.URLSpan;
// Aurora xuyong 2014-03-04 added for aurora's new feature end
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
// Aurora xuyong 2013-12-30 added for aurora's ne feature start
import android.view.ViewStub;
// Aurora xuyong 2013-12-30 added for aurora's ne feature end
import android.widget.ImageView;
import android.widget.LinearLayout;
// Aurora xuyong 2013-12-30 added for aurora's ne feature start
import android.widget.RelativeLayout;
// Aurora xuyong 2013-12-30 added for aurora's ne feature end
import android.widget.TextView;
// Aurora xuyong 2013-12-30 added for aurora's ne feature start
import android.widget.ViewAnimator;
// Aurora xuyong 2013-12-30 added for aurora's ne feature end

import com.android.mms.MmsApp;
import com.android.mms.R;
// Aurora xuyong 2014-03-07 added for aurora's new feature start
import com.android.mms.model.AudioModel;
// Aurora xuyong 2014-03-07 added for aurora's new feature end
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
// Aurora xuyong 2014-03-07 added for aurora's new feature start
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.VideoModel;
// Aurora xuyong 2014-03-07 added for aurora's new feature end
// Aurora xuyong 2014-03-04 added for aurora's new feature start
// Aurora xuyong 2014-05-04 added for aurora's new feature start
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageListItem;
// Aurora xuyong 2014-05-04 added for aurora's new feature end
import com.android.mms.ui.MessageUtils;
// Aurora xuyong 2014-03-04 added for aurora's new feature end
import com.android.mms.ui.SlideViewInterface;
import android.widget.ImageView; // import com.gionee.widget.GnImageView;

import android.webkit.MimeTypeMap;
import android.view.MotionEvent;

import android.webkit.MimeTypeMap;
import android.view.MotionEvent;
// Aurora xuyong 2014-01-03 added for aurora;s new feature start
import com.android.mms.ui.VideoAttachmentView;
// Aurora xuyong 2014-01-03 added for aurora;s new feature end
// Aurora xuyong 2013-12-30 added for aurora's ne feature start
import com.aurora.mms.ui.AuroraRoundImageView;
// Aurora xuyong 2014-03-04 added for aurora's new feature start
import com.aurora.mms.ui.ClickContent;
import com.aurora.mms.util.AuroraLinkMovementMethod;
// Aurora xuyong 2014-09-16 added for bug #8331 start
import com.aurora.view.AuroraURLSpan;
// Aurora xuyong 2014-09-16 added for bug #8331 end
// Aurora xuyong 2014-03-04 added for aurora's new feature end
// Aurora xuyong 2013-12-30 added for aurora's ne feature end
/**
 * A simplified view of slide in the slides list.
 */
public class SlidesBrowserItemView extends LinearLayout implements SlideViewInterface {
    private static final String TAG = "SlideListItemView";
    // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
    private RelativeLayout mImageView;
    private TextView mTextView;
    private RelativeLayout mSlideNumberView;
    private AuroraRoundImageView mAudioView;
    private View mAudioInfoView;
    private View mVideoInfoView;
    private AuroraRoundImageView mVideoView;
    // Aurora xuyong 2014-01-03 added for aurora;s new feature start
    private TextView mSlideBgView;
    // Aurora xuyong 2014-01-03 added for aurora;s new feature end
    
    private LinearLayout mRootView;
    // Aurora xuyong 2013-12-30 modified for aurora's ne feature end

    public SlidesBrowserItemView(Context context) {
        super(context);
        this.setBackgroundColor(0x0);
        this.setSelected(false);
    }

    public SlidesBrowserItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundColor(0x0);
        this.setSelected(false);
    }

    @Override
    protected void onFinishInflate() {
    }
    // Aurora xuyong 2014-03-04 added for aurora's new feature start
    public final static int MODEL_ATTACHMENTS_PICK = 0;
    public final static int MODEL_LINK_CLICK = 1;
    public final static int MODEL_MULTI_LINKS_CLICK = 2;
    
    private Handler mHandler;
    public void setHandler(Handler handler) {
        mHandler = handler;
    }
    // Aurora xuyong 2014-03-04 added for aurora's new feature end
    public void startAudio() {
        // Playing audio is not needed in this view.
    }

    public void startVideo() {
        // Playing audio is not needed in this view.
    }

    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        if (audio == null) {
            throw new IllegalArgumentException("Audio URI may not be null.");
        }
        // Aurora xuyong 2014-03-07 modified for aurora's new feature start
        initAudioInfoView(name, audio);
        // Aurora xuyong 2014-03-07 modified for aurora's new feature end
    }

    public void setImage(String name, Bitmap bitmap) {
        if (mImageView == null) {
            // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
            ViewStub viewStub = (ViewStub) findViewById(R.id.aurora_part_imageview);
            if (viewStub != null) {
                mImageView = (RelativeLayout) viewStub.inflate();
            }
            /*mImageView = (RelativeLayout)factory.inflate(R.layout.aurora_slides_item_image, null);
            addView(mImageView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));*/
            // Aurora xuyong 2013-12-30 modified for aurora's ne feature end
        }
        try {
            /*if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }*/
            // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
            //mImageView.setVisibility(View.VISIBLE);
            //mImageView.setImageBitmap(bitmap);
            // Aurora xuyong 2013-12-30 modified for aurora's ne feature end
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
        mImageView.setVisibility(View.VISIBLE);
        AuroraRoundImageView iamge = (AuroraRoundImageView)mImageView.findViewById(R.id.aurora_item_image);
        iamge.setImageBitmap(bitmap);
        iamge.setOnClickListener(new OnClickListener() {
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature end

            @Override
            public void onClick(View v) {
                if (mSlideShowModel == null) {
                    return;
                }

                onMediaThumnailClicked(mSlideShowModel.getImage());
            }
        });
        // Aurora xuyong 2014-03-04 added for aurora's new feature start
        iamge.setOnLongClickListener(new View.OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                Message msg = Message.obtain(mHandler, MODEL_ATTACHMENTS_PICK);
                msg.obj = mSlideShowModel.getImage().getUri();
                msg.sendToTarget();
                return false;
            }
        });
        // Aurora xuyong 2014-03-04 added for aurora's new feature end
    }

    public void setImageRegionFit(String fit) {
        // TODO Auto-generated method stub
    }

    public void setImageVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }
    // Aurora xuyong 2014-03-04 added for aurora's new feature start
    private ArrayList<String> initComList(ArrayList<String> list) {
        ArrayList<String> nl = new ArrayList();
        for (String s : list) {
            if (!nl.contains(s)) {
                nl.add(s);
            }
        }
        return nl;
    }
    
    /*private void initAutoLinks(TextView textView) {
        URLSpan[] spans = textView.getUrls();
        java.util.ArrayList<String> urlsold = MessageUtils.extractUris(spans);
        final java.util.ArrayList<String> urls = initComList(urlsold);
        if (urls.size() == 0) {
            return;
        } else if (urls.size() == 1) {
            Message msg = Message.obtain(mHandler, MODEL_LINK_CLICK);
            ClickContent cc = new ClickContent();
            cc.setValue(urls.get(0));
            msg.obj = cc;
            msg.sendToTarget();    
        } else {
            Message msg = Message.obtain(mHandler, MODEL_MULTI_LINKS_CLICK);
            ClickContent cc = new ClickContent();
            cc.setValues(urls);
            msg.obj = cc;
            msg.sendToTarget();    
        }
    }*/
    // Aurora xuyong 2014-03-04 added for aurora's new feature end
    public void setText(String name, String text) {
        // Aurora xuyong 2014-09-16 modified for bug #8331 start
        mTextView.setText(rebuildTextBody(text));
        // Aurora xuyong 2014-09-16 modified for bug #8331 end
        mTextView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }
    // Aurora xuyong 2014-09-16 added for bug #8331 start
    private Spannable rebuildTextBody(String body) {
        Spannable.Factory sf = Spannable.Factory.getInstance();
        Spannable sp = sf.newSpannable(body);
        if(Linkify.addLinks(sp, Linkify.ALL)) {
            mTextView.setMovementMethod(AuroraLinkMovementMethod.getInstance());
            URLSpan[] urlSpans = sp.getSpans(0, sp.length(), URLSpan.class);
            for (URLSpan urlSpan : urlSpans) {
                int start = sp.getSpanStart(urlSpan);
                int end   = sp.getSpanEnd(urlSpan);
                sp.removeSpan(urlSpan);
                AuroraURLSpan aURLSpan = new AuroraURLSpan(urlSpan.getURL());
                // Aurora xuyong 2015-02-04 added for bug #11531 start
                aURLSpan.setHandler(mHandler);
                // Aurora xuyong 2015-02-04 added for bug #11531 end
                sp.setSpan(aURLSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return sp;
    }
    // Aurora xuyong 2014-09-16 added for bug #8331 end
    public void setTextVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }
    // Aurora xuyong 2014-03-07 added for aurora's new feature start
    // Aurora xuyong 2014-05-04 deleted for aurora's new feature start
    /*private class GetDurationRunnable implements Runnable {
        
        Context mContext;
        Uri mUri;
        boolean mIsVideo;
        
        public GetDurationRunnable(Context context, Uri video, boolean isVideo) {
            mContext =     context;
            mUri = video;
            mIsVideo = isVideo;
        }
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Bitmap bitmap = null;
            if (!mIsVideo) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.aurora_audio_thumbnail);
            } else {
                bitmap = VideoAttachmentView.createVideoThumbnail(mContext, mUri);
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.aurora_video_thumbnail);
                }
            }
            int duration = VideoAttachmentView.getMediaDuration(mContext, mUri);
            BitmapAndDurationInfo info = new BitmapAndDurationInfo(duration);
            info.setBitmap(bitmap);
            info.setIsVideo(mIsVideo);
            info.setUri(mUri);
            Message msg = Message.obtain(mDhandler);
            msg.obj = info;
            msg.sendToTarget();
        }
        
    }
    
    private class BitmapAndDurationInfo {
        
        private WeakReference<Bitmap> mBitmapReference;
        private int mDuration = -1;
        private Uri mUri;
        private boolean mIsVideo;
        
        BitmapAndDurationInfo(int duration) {
            mDuration = duration;
        }
        
        public synchronized void setBitmap(Bitmap bitmap) {
            mBitmapReference = new WeakReference<Bitmap>(bitmap);
        }
        
        public void setDuration(int duration) {
            mDuration = duration;
        }
        
        public synchronized void setUri(Uri uri) {
            mUri = uri;
        }
        
        public void setIsVideo(boolean bool) {
            mIsVideo = bool;
        }
        
        public synchronized Bitmap getBitmap() {
            return mBitmapReference.get();
        }
        
        public int getDuration() {
            return mDuration;
        }
        
        public synchronized Uri getUri() {
            return mUri;
        }
        
        public boolean isVideo() {
            return mIsVideo;
        }
    }
    
    Handler mDhandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            BitmapAndDurationInfo info = (BitmapAndDurationInfo)(msg.obj);
            if (info == null || mSlideShowModel == null) {
                return;
            }
            Bitmap bitmap = info.getBitmap();
            Uri uri = info.getUri();
            boolean isVideo = info.isVideo();
            int duration = info.getDuration();
            if (isVideo) {
                if (mVideoView != null) {
                    mVideoView.setImageResource(R.drawable.aurora_video_thumbnail);
                }
            } else {
                if (mAudioView != null) {
                    mAudioView.setImageResource(R.drawable.aurora_audio_thumbnail);
                }
            }
            if (mSlideBgView != null) {
                mSlideBgView.setText(VideoAttachmentView.initMediaDuration(duration));
            }
            boolean needReplaceVideoImage = false;
            boolean needReplaceAudioImage = false;
            if (isVideo) {
                    VideoModel vm = mSlideShowModel.getVideo();
                    if (vm != null) {
                        String videoUri = vm.getUri().toString();
                        needReplaceVideoImage = videoUri.equals(uri.toString());
                    }
            } else {
                    AudioModel am = mSlideShowModel.getAudio();
                    if (am != null) {
                        String audioUri = am.getUri().toString();
                        needReplaceAudioImage = audioUri.equals(uri.toString());
                    }
            }
            if (needReplaceVideoImage || needReplaceAudioImage) {
                
                if (isVideo) {
                    if (mVideoView != null) {
                        // Aurora xuyong 2014-03-08 modified for aurora's new feature start
                        if (bitmap != null) {
                            mVideoView.setImageBitmap(bitmap);
                        } else {
                            mVideoView.setImageResource(R.drawable.aurora_video_thumbnail);
                        }
                        // Aurora xuyong 2014-03-08 modified for aurora's new feature end
                    }
                } else {
                    if (mAudioView != null) {
                        // Aurora xuyong 2014-03-08 modified for aurora's new feature start
                        if (bitmap != null) {
                            mAudioView.setImageBitmap(bitmap);
                        } else {
                            mAudioView.setImageResource(R.drawable.aurora_audio_thumbnail);
                        }
                        // Aurora xuyong 2014-03-08 modified for aurora's new feature end
                    }
                }
            } else {
                if (isVideo) {
                    if (mVideoView != null && needReplaceVideoImage) {
                        mVideoView.setImageResource(R.drawable.aurora_video_thumbnail);
                    }
                } else {
                    if (mAudioView != null && needReplaceAudioImage) {
                        mAudioView.setImageResource(R.drawable.aurora_audio_thumbnail);
                    }
                }
            }
        }
    };*/
    // Aurora xuyong 2014-05-04 deleted for aurora's new feature end
    // Aurora xuyong 2014-03-07 added for aurora's new feature end
    public void setVideo(String name, Uri video) {
        if (null == mVideoInfoView) {
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
            ViewStub viewStub = (ViewStub) findViewById(R.id.aurora_part_vedio);
            if (viewStub != null) {
                mVideoInfoView = (View) viewStub.inflate();
            }
            //mVideoInfoView = factory.inflate(R.layout.gn_slidebrowser_media_info, null);

            /*TextView audioName = (TextView) mVideoInfoView.findViewById(R.id.media_name);
            audioName.setText(name);*/

            /*addView(mVideoInfoView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));*/
        }
        mVideoInfoView.setVisibility(View.VISIBLE);
        // Aurora xuyong 2014-01-03 modified for aurora;s new feature start
        mSlideBgView = (TextView)mVideoInfoView.findViewById(R.id.aurora_slide_bg_down);
        // Aurora xuyong 2014-03-07 added for aurora's new feature start
        // Aurora xuyong 2014-05-04 deleted for aurora's new feature start
        //Thread thread = new Thread(new GetDurationRunnable(this.getContext(), video, true));
        //thread.start();
        // Aurora xuyong 2014-05-04 deleted for aurora's new feature start
        // Aurora xuyong 2014-03-07 added for aurora's new feature end
        // Aurora xuyong 2014-03-07 deleted for aurora's new feature start
        //mSlideBgView.setText(VideoAttachmentView.initMediaDuration(VideoAttachmentView.getMediaDuration(getContext(), mSlideShowModel.getVideo().getUri())));
        // Aurora xuyong 2014-03-07 deleted for aurora's new feature end
        // Aurora xuyong 2014-01-03 modified for aurora;s new feature end
        mVideoView = (AuroraRoundImageView) mVideoInfoView.findViewById(R.id.media_icon);
        // Aurora xuyong 2014-05-04 added for aurora's new feature start
        mVideoView.bindTextView(mSlideBgView);
        // Aurora xuyong 2014-05-07 modified for bug 4693 start
        SlidesBrowserActivity.mThumbnailWorker.loadImage(video, mVideoView, MessageListItem.isVideo);
        // Aurora xuyong 2014-05-07 modified for bug 4693 end
        // Aurora xuyong 2014-05-04 added for aurora's new feature end
        // Aurora xuyong 2013-03-05 added for aurora's new feature start
        // Aurora xuyong 2014-03-07 deleted for aurora's new feature start
        /*MediaMetadataRetriever mp = new MediaMetadataRetriever();
        try {
            mp.setDataSource(this.getContext(), mSlideShowModel.getVideo().getUri());
            mVideoView.setImageBitmap(mp.getFrameAtTime(-1));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unexpected IOException.", e);
        } finally {
            mp.release();
        }*/
        // Aurora xuyong 2014-03-07 deleted for aurora's new feature end
        // Aurora xuyong 2013-03-05 added for aurora's new feature end
        // Aurora xuyong 2014-03-04 added for aurora's new feature start
        mVideoView.setOnLongClickListener(new View.OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                Message msg = Message.obtain(mHandler, MODEL_ATTACHMENTS_PICK);
                msg.obj = mSlideShowModel.getVideo().getUri();
                msg.sendToTarget();
                return false;
            }
        });
        // Aurora xuyong 2014-03-04 added for aurora's new feature end
        // Aurora xuyong 2013-03-05 deleted for aurora's new feature start
        //mVideoView.setImageResource(R.drawable.aurora_video_thumbnail);
        // Aurora xuyong 2013-03-05 deleted for aurora's new feature end

        /*mVideoView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSlideShowModel == null) {
                    return;
                }

                onMediaThumnailClicked(mSlideShowModel.getVideo());
            }
        });*/
        ImageView showView = (ImageView) mVideoInfoView.findViewById(R.id.aurora_slideshow_button);
        showView.setOnClickListener(new OnClickListener() {
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature end
            @Override
            public void onClick(View v) {
                if (mSlideShowModel == null) {
                    return;
                }

                onMediaThumnailClicked(mSlideShowModel.getVideo());
            }
        });
        // Aurora xuyong 2014-03-04 added for aurora's new feature start
        showView.setOnLongClickListener(new View.OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                Message msg = Message.obtain(mHandler, MODEL_ATTACHMENTS_PICK);
                msg.obj = mSlideShowModel.getVideo().getUri();
                msg.sendToTarget();
                return false;
            }
        });
        // Aurora xuyong 2014-03-04 added for aurora's new feature end
    }

    /**
     * get the thumbnail of a video.
     */
    private Bitmap createVideoThumbnail(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            bitmap = retriever.getFrameAtTime(1000);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            Log.d(TAG, ex.getMessage());
        } catch (RuntimeException ex) {
            Log.d(TAG, ex.getMessage());
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        return bitmap;
    }

    public void setVideoVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    public void stopAudio() {
        // Stopping audio is not needed in this view.
    }

    public void stopVideo() {
        // Stopping video is not needed in this view.
    }

    public void reset() {
        // TODO Auto-generated method stub
    }

    public void setVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    public void pauseAudio() {
        // TODO Auto-generated method stub

    }

    public void pauseVideo() {
        // TODO Auto-generated method stub

    }

    public void seekAudio(int seekTo) {
        // TODO Auto-generated method stub

    }

    public void seekVideo(int seekTo) {
        // TODO Auto-generated method stub

    }

    private class Position {
        public Position(int left, int top) {
            mTop = top;
            mLeft = left;
        }

        public int mTop;

        public int mLeft;
    }

    public void setSlideNumber(int number) {
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
        TextView numberTextView;
        if (mSlideNumberView == null) {
            ViewStub viewStub = (ViewStub) findViewById(R.id.aurora_part_num_divider);

            if (viewStub != null) {
                mSlideNumberView = (RelativeLayout) viewStub.inflate();
            }
            //mSlideNumberView = (RelativeLayout)factory.inflate(R.layout.aurora_slides_divider, null);
            numberTextView = (TextView)mSlideNumberView.findViewById(R.id.aurora_slides_pos);
            numberTextView.setText("." + String.valueOf(number + 1));
            /*addView(mSlideNumberView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));*/
        }
     // Aurora xuyong 2013-12-30 modified for aurora's ne feature end
    }

    public void createModelViews(int textLeft, int textTop, int imageLeft, int imageTop) {

        // Layout views to fit the LinearLayout from left to right, then top to
        // bottom.
        TreeMap<Position, View> viewsByPosition = new TreeMap<Position, View>(
                new Comparator<Position>() {
                    public int compare(Position p1, Position p2) {
                        int l1 = p1.mLeft;
                        int t1 = p1.mTop;
                        int l2 = p2.mLeft;
                        int t2 = p2.mTop;
                        int res = t1 - t2;
                        if (res == 0) {
                            res = l1 - l2;
                        }
                        if (res == 0) {
                            // A view will be lost if return 0.
                            return -1;
                        }
                        return res;
                    }
                });

        int slideNumberHeight = 20;
        textTop += slideNumberHeight;
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
        LayoutInflater factory = LayoutInflater.from(getContext());
        mRootView = (LinearLayout)factory.inflate(R.layout.aurora_slides_item, null);
        
        addView(mRootView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        if (textLeft >= 0 && textTop >= 0) {
            ViewStub viewStub = (ViewStub) findViewById(R.id.aurora_part_textview);
            if (viewStub != null) {
                mTextView = (TextView) viewStub.inflate();
                // Aurora xuyong 2014-03-04 added for aurora's new feature start
                /*mTextView.setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        initAutoLinks((TextView)v);
                    }
                });*/
                // Aurora xuyong 2014-03-04 added for aurora's new feature end
            }
            /*mTextView = new TextView(mContext);
            mTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mTextView.setTextSize(16);
            mTextView.setTextColor(colors)
            // mTextView.setTextColor(Color.WHITE);
            mTextView.setAutoLinkMask(Linkify.ALL);*/
            viewsByPosition.put(new Position(textLeft, textTop), mTextView);
        }

        if (imageLeft >= 0 && imageTop >= 0) {
            ViewStub viewStub = (ViewStub) findViewById(R.id.aurora_part_imageview);
            if (viewStub != null) {
                mImageView = (RelativeLayout) viewStub.inflate();
            }
            /*mImageView = (RelativeLayout)factory.inflate(R.layout.aurora_slides_item_image, null);
            mImageView.setVisibility(View.VISIBLE);*/
            //mImageView.setBackgroundColor(0xFFFFFFFF);
            viewsByPosition.put(new Position(imageLeft, imageTop), mImageView);
        }

        /*for (View view : viewsByPosition.values()) {
            addView(view, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            view.setVisibility(View.GONE);
        }*/
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature end
    }

    private void onMediaThumnailClicked(MediaModel media) {

        if (media == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        String contentType;
        if (media.isDrmProtected()) {
            contentType = media.getDrmObject().getContentType();
        } else {
            //gionee gaoj 2012-10-29 added for CR00718353 start
            if (MmsApp.mGnMessageSupport) {
                intent.putExtra("CanShare", false);
            }
            //gionee gaoj 2012-10-29 added for CR00718353 end
            String[] temp = media.getSrc().split("\\.");
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            contentType = mimeTypeMap.getMimeTypeFromExtension(temp[temp.length-1]);
        }
        // Aurora xuyong 2015-07-30 modified for bug #14494 start
        File output = MessageUtils.copyPartsToOutputFile(SlidesBrowserItemView.this.getContext(), media.getUri(), contentType);
        intent.setDataAndType(Uri.fromFile(output), contentType);
        // Aurora xuyong 2015-07-30 modified for bug #14494 end
        //gionee gaoj 2012-10-25 modified for CR00718105 start
        if (MmsApp.mGnMessageSupport) {
            SlidesBrowserItemView.this.getContext().startActivity(
                    Intent.createChooser(intent, null));
        } else {
        //gionee gaoj 2012-10-25 modified for CR00718105 end
        SlidesBrowserItemView.this.getContext().startActivity(
                Intent.createChooser(intent, "Choose"));
        //gionee gaoj 2012-10-25 modified for CR00718105 start
        }
        //gionee gaoj 2012-10-25 modified for CR00718105 end
    }
    // Aurora xuyong 2014-03-07 modified for aurora's new feature start
    private void initAudioInfoView(String name, Uri audio) {
    // Aurora xuyong 2014-03-07 modified for aurora's new feature end
        if (null == mAudioInfoView) {
            // Aurora xuyong 2013-12-30 modified for aurora's ne feature start
            ViewStub viewStub = (ViewStub) findViewById(R.id.aurora_part_audio);

            if (viewStub != null) {
                mAudioInfoView = (View) viewStub.inflate();
            }
            //mAudioInfoView = factory.inflate(R.layout.gn_slidebrowser_media_info, null);

            /*TextView audioName = (TextView) mAudioInfoView.findViewById(R.id.media_name);
            audioName.setText(name);*/

            /*addView(mAudioInfoView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));*/
        }

        mAudioInfoView.setVisibility(View.VISIBLE);
        // Aurora xuyong 2014-01-03 added for aurora;s new feature start
        // Aurora xuyong 2014-03-07 added for aurora's new feature start
        // Aurora xuyong 2014-05-04 deleted for aurora's new feature start
        //Thread thread = new Thread(new GetDurationRunnable(this.getContext(), audio, false));
        //thread.start();
        // Aurora xuyong 2014-05-04 deleted for aurora's new feature end
        // Aurora xuyong 2014-03-07 added for aurora's new feature end
        mSlideBgView = (TextView)mAudioInfoView.findViewById(R.id.aurora_slide_bg_down);
        // Aurora xuyong 2014-03-07 deleted for aurora's new feature start
        //mSlideBgView.setText(VideoAttachmentView.initMediaDuration(VideoAttachmentView.getMediaDuration(getContext(), mSlideShowModel.getAudio().getUri())));
        // Aurora xuyong 2014-03-07 deleted for aurora's new feature end
        // Aurora xuyong 2014-01-03 added for aurora;s new feature end
        mAudioView = (AuroraRoundImageView) mAudioInfoView.findViewById(R.id.media_icon);
        // Aurora xuyong 2014-05-04 added for aurora's new feature start
        mAudioView.bindTextView(mSlideBgView);
        // Aurora xuyong 2014-05-07 modified for bug 4693 start
        SlidesBrowserActivity.mThumbnailWorker.loadImage(audio, mAudioView, MessageListItem.isAudio);
        // Aurora xuyong 2014-05-07 modified for bug 4693 end
        // Aurora xuyong 2014-05-04 added for aurora's new feature end
        // Aurora xuyong 2014-03-04 added for aurora's new feature start
        mAudioView.setOnLongClickListener(new View.OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                Message msg = Message.obtain(mHandler, MODEL_ATTACHMENTS_PICK);
                msg.obj = mSlideShowModel.getAudio().getUri();
                msg.sendToTarget();
                return false;
            }
        });
        // Aurora xuyong 2014-03-04 added for aurora's new feature end
        // Aurora xuyong 2014-03-07 deleted for aurora's new feature start
        //mAudioView.setImageResource(R.drawable.aurora_audio_thumbnail);
        // Aurora xuyong 2014-03-07 deleted for aurora's new feature end

        /*mAudioView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSlideShowModel == null) {
                    return;
                }

                onMediaThumnailClicked(mSlideShowModel.getAudio());
            }
        });*/
        ImageView showView = (ImageView) mAudioInfoView.findViewById(R.id.aurora_slideshow_button);
        showView.setOnClickListener(new OnClickListener() {
        // Aurora xuyong 2013-12-30 modified for aurora's ne feature end
            @Override
            public void onClick(View v) {
                if (mSlideShowModel == null) {
                    return;
                }

                onMediaThumnailClicked(mSlideShowModel.getAudio());
            }
        });
        // Aurora xuyong 2014-03-04 added for aurora's new feature start
        showView.setOnLongClickListener(new View.OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                Message msg = Message.obtain(mHandler, MODEL_ATTACHMENTS_PICK);
                msg.obj = mSlideShowModel.getAudio().getUri();
                msg.sendToTarget();
                return false;
            }
        });
        // Aurora xuyong 2014-03-04 added for aurora's new feature end
    }

    SlideModel mSlideShowModel;

    public void setSlideShowModel(SlideModel model) {
        mSlideShowModel = model;
    }

    //gionee wangym 2012-11-22 add for CR00735223 start
     public void setMmsTextSize(float size){        
        if(mTextView != null && mTextView.getVisibility() == View.VISIBLE){
            mTextView.setTextSize(size);
        }
    }
    //gionee wangym 2012-11-22 add for CR00735223 end
   // Aurora xuyong 2014-04-25 added for bug #4301 start
    @Override
    public void setImage(String name, Uri uri) {
        // TODO Auto-generated method stub
       // Aurora xuyong 2014-05-04 added for aurora's new feature start
        if (mImageView == null) {
            ViewStub viewStub = (ViewStub) findViewById(R.id.aurora_part_imageview);
            if (viewStub != null) {
                mImageView = (RelativeLayout) viewStub.inflate();
            }
        }
        mImageView.setVisibility(View.VISIBLE);
        AuroraRoundImageView iamge = (AuroraRoundImageView)mImageView.findViewById(R.id.aurora_item_image);
        // Aurora xuyong 2014-05-07 modified for bug 4693 start
        SlidesBrowserActivity.mThumbnailWorker.loadImage(uri, iamge, MessageListItem.isImage);
        // Aurora xuyong 2014-05-07 modified for bug 4693 end
        iamge.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSlideShowModel == null) {
                    return;
                }

                onMediaThumnailClicked(mSlideShowModel.getImage());
            }
        });
        iamge.setOnLongClickListener(new View.OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                Message msg = Message.obtain(mHandler, MODEL_ATTACHMENTS_PICK);
                msg.obj = mSlideShowModel.getImage().getUri();
                msg.sendToTarget();
                return false;
            }
        });
       // Aurora xuyong 2014-05-04 added for aurora's new feature end
    }
   // Aurora xuyong 2014-04-25 added for bug #4301 end
}
