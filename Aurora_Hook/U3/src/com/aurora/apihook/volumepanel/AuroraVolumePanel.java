/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.aurora.apihook.volumepanel;

import com.aurora.R;
import com.aurora.utils.DensityUtil;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnDismissListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.AudioService;
import android.media.AudioSystem;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.media.VolumeController;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.VolumePanel;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.view.View;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.util.ArrayList;
import java.util.HashMap;


//add for aurora hanping 2014.4.28
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

import java.util.Map;
//add for aurora hanping 2014.4.28

/**
 * Handle the volume up and down keys.
 *
 * This code really should be moved elsewhere.
 *
 * Seriously, it really really should be moved elsewhere.  This is used by
 * android.media.AudioService, which actually runs in the system process, to
 * show the volume dialog when the user changes the volume.  What a mess.
 *
 * @hide
 */
public class AuroraVolumePanel extends VolumePanel implements OnSeekBarChangeListener, View.OnClickListener,
        VolumeController
{
    private static final String TAG = "VolumePanel";
    private static boolean LOGD = true;

    /**
     * The delay before playing a sound. This small period exists so the user
     * can press another key (non-volume keys, too) to have it NOT be audible.
     * <p>
     * PhoneWindow will implement this part.
     */
    public static final int PLAY_SOUND_DELAY = 300;

    /**
     * The delay before vibrating. This small period exists so if the user is
     * moving to silent mode, it will not emit a short vibrate (it normally
     * would since vibrate is between normal mode and silent mode using hardware
     * keys).
     */
    public static final int VIBRATE_DELAY = 300;

    private static final int VIBRATE_DURATION = 300;
    private static final int BEEP_DURATION = 150;
    private static final int MAX_VOLUME = 100;
    private static final int FREE_DELAY = 10000;
    private static final int TIMEOUT_DELAY = 3000;

    private static final int MSG_VOLUME_CHANGED = 0;
    private static final int MSG_FREE_RESOURCES = 1;
    private static final int MSG_PLAY_SOUND = 2;
    private static final int MSG_STOP_SOUNDS = 3;
    private static final int MSG_VIBRATE = 4;
    private static final int MSG_TIMEOUT = 5;
    private static final int MSG_RINGER_MODE_CHANGED = 6;
    private static final int MSG_MUTE_CHANGED = 7;
    private static final int MSG_REMOTE_VOLUME_CHANGED = 8;
    private static final int MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN = 9;
    private static final int MSG_SLIDER_VISIBILITY_CHANGED = 10;
    private static final int MSG_DISPLAY_SAFE_VOLUME_WARNING = 11;

    // Pseudo stream type for master volume
    private static final int STREAM_MASTER = -100;
    // Pseudo stream type for remote volume is defined in AudioService.STREAM_REMOTE_MUSIC

    protected Context mContext;
    private AudioManager mAudioManager;
    protected AudioService mAudioService;
    private boolean mRingIsSilent;
    private boolean mShowCombinedVolumes;
    private boolean mVoiceCapable;

    // True if we want to play tones on the system stream when the master stream is specified.
    private  boolean mPlayMasterStreamTones;

    /** Dialog containing all the sliders */
    private  Dialog mDialog;
    /** Dialog's content view */
    private  View mView;

    /** The visible portion of the volume overlay */
    private  ViewGroup mPanel;
    /** Contains the sliders and their touchable icons */
    private  ViewGroup mSliderGroup;
    /** The button that expands the dialog to show all sliders */
    private  View mMoreButton;
    /** Dummy divider icon that needs to vanish with the more button */
    private  View mDivider;

    /** Currently active stream that shows up at the top of the list of sliders */
    private int mActiveStreamType = -1;
    /** All the slider controls mapped by stream type */
    private HashMap<Integer,StreamControl> mStreamControls;

    private Object mLock = new Object();
    private enum StreamResources {
        BluetoothSCOStream(AudioManager.STREAM_BLUETOOTH_SCO,
                R.string.volume_icon_description_bluetooth,
                R.drawable.ic_audio_bt,
                R.drawable.ic_audio_bt,
                false),
        RingerStream(AudioManager.STREAM_RING,
                R.string.volume_icon_description_ringer,
                R.drawable.ic_audio_ring_notif,
                R.drawable.ic_audio_ring_notif_mute,
                false),
        VoiceStream(AudioManager.STREAM_VOICE_CALL,
                R.string.volume_icon_description_incall,
                R.drawable.ic_audio_phone,
                R.drawable.ic_audio_phone,
                false),
        AlarmStream(AudioManager.STREAM_ALARM,
                R.string.volume_alarm,
                R.drawable.ic_audio_alarm,
                R.drawable.ic_audio_alarm_mute,
                false),
        MediaStream(AudioManager.STREAM_MUSIC,
                R.string.volume_icon_description_media,
                R.drawable.ic_audio_vol,
                R.drawable.ic_audio_vol_mute,
                true),
        NotificationStream(AudioManager.STREAM_NOTIFICATION,
                R.string.volume_icon_description_notification,
                R.drawable.ic_audio_notification,
                R.drawable.ic_audio_notification_mute,
                true),
        // for now, use media resources for master volume
        MasterStream(STREAM_MASTER,
                R.string.volume_icon_description_media, //FIXME should have its own description
                R.drawable.ic_audio_vol,
                R.drawable.ic_audio_vol_mute,
                false),
        RemoteStream(AudioService.STREAM_REMOTE_MUSIC,
                R.string.volume_icon_description_media, //FIXME should have its own description
                R.drawable.ic_media_route_on_holo_dark,
                R.drawable.ic_media_route_disabled_holo_dark,
                false);// will be dynamically updated

        int streamType;
        int descRes;
        int iconRes;
        int iconMuteRes;
        // RING, VOICE_CALL & BLUETOOTH_SCO are hidden unless explicitly requested
        boolean show;

        StreamResources(int streamType, int descRes, int iconRes, int iconMuteRes, boolean show) {
            this.streamType = streamType;
            this.descRes = descRes;
            this.iconRes = iconRes;
            this.iconMuteRes = iconMuteRes;
            this.show = show;
        }
    };

    // List of stream types and their order
    private static final StreamResources[] STREAMS = {
        StreamResources.BluetoothSCOStream,
        StreamResources.RingerStream,
        StreamResources.VoiceStream,
        StreamResources.MediaStream,
        StreamResources.NotificationStream,
        StreamResources.AlarmStream,
        StreamResources.MasterStream,
        StreamResources.RemoteStream
    };

    /** Object that contains data for each slider */
    private class StreamControl {
        int streamType;
        ViewGroup group;
        ImageView icon;
        SeekBar seekbarView;
        int iconRes;
        int iconMuteRes;
    }

    // Synchronize when accessing this
    private ToneGenerator mToneGenerators[];
    private Vibrator mVibrator;

    private static AlertDialog sConfirmSafeVolumeDialog;
    private static Object sConfirmSafeVolumeLock = new Object();

    private static class WarningDialogReceiver extends BroadcastReceiver
            implements DialogInterface.OnDismissListener {
        private final Context mContext;
        private final Dialog mDialog;
        private final AuroraVolumePanel mVolumePanel;

        WarningDialogReceiver(Context context, Dialog dialog, AuroraVolumePanel volumePanel) {
            mContext = context;
            mDialog = dialog;
            mVolumePanel = volumePanel;
            IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mDialog.cancel();
            cleanUp();
        }

        public void onDismiss(DialogInterface unused) {
            mContext.unregisterReceiver(this);
            cleanUp();
        }

        private void cleanUp() {
            synchronized (sConfirmSafeVolumeLock) {
                sConfirmSafeVolumeDialog = null;
            }
            mVolumePanel.forceTimeout();
            mVolumePanel.updateStates();
        }
    }


    public AuroraVolumePanel(final Context context, AudioService volumeService) {
    	super(context,volumeService);
       mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioService = volumeService;
      
        // For now, only show master volume if master volume is supported
        boolean useMasterVolume = context.getResources().getBoolean(
                com.android.internal.R.bool.config_useMasterVolume);
        if (useMasterVolume) {
            for (int i = 0; i < STREAMS.length; i++) {
                StreamResources streamRes = STREAMS[i];
                streamRes.show = (streamRes.streamType == STREAM_MASTER);
            }
        }
       
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mView = inflater.inflate(R.layout.volume_adjust, null);
        mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                resetTimeout();
                return false;
            }
        });
        
        mPanel = (ViewGroup) mView.findViewById(R.id.visible_panel);
        mSliderGroup = (ViewGroup) mView.findViewById(R.id.slider_group);
        mMoreButton = (ImageView) mView.findViewById(R.id.expand_button);
        mDivider = (ImageView) mView.findViewById(R.id.expand_button_divider);

        mDialog = new Dialog(context, R.style.Theme_Panel_Volume) {
            public boolean onTouchEvent(MotionEvent event) {
                if (isShowing() && event.getAction() == MotionEvent.ACTION_OUTSIDE &&
                        sConfirmSafeVolumeDialog == null) {
                    forceTimeout();
                    return true;
                }
                return false;
            }
        };
        mDialog.setTitle("Volume control"); // No need to localize
        mDialog.setContentView(mView);
        mDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mActiveStreamType = -1;
                mAudioManager.forceVolumeControlStream(mActiveStreamType);
            }
        });
        // Change some window properties
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.TOP);
        LayoutParams lp = window.getAttributes();
        lp.token = null;
        // Offset from the top
        lp.y = DensityUtil.dip2px(mContext, 120);//mContext.getResources().getDimensionPixelOffset(
                //com.android.internal.R.dimen.volume_panel_top);
        lp.type = LayoutParams.TYPE_VOLUME_OVERLAY;
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.addFlags(LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        mToneGenerators = new ToneGenerator[AudioSystem.getNumStreamTypes()];
        mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        mVoiceCapable = context.getResources().getBoolean(R.bool.config_voice_capable);
        mShowCombinedVolumes = !mVoiceCapable && !useMasterVolume;
        // If we don't want to show multiple volumes, hide the settings button and divider
        if (!mShowCombinedVolumes) {
            mMoreButton.setVisibility(View.GONE);
            mDivider.setVisibility(View.GONE);
        } else {
            mMoreButton.setOnClickListener(this);
        }

        boolean masterVolumeOnly = context.getResources().getBoolean(
                com.android.internal.R.bool.config_useMasterVolume);
        boolean masterVolumeKeySounds = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_useVolumeKeySounds);

        mPlayMasterStreamTones = masterVolumeOnly && masterVolumeKeySounds;

        listenToRingerMode();

        //add for aurora hanping 2014.4.28
        auroraInit();
        //add for aurora hanping 2014.4.28
    }

    public void setLayoutDirection(int layoutDirection) {
        mPanel.setLayoutDirection(layoutDirection);
        updateStates();
    }

    private void listenToRingerMode() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        mContext.registerReceiver(new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                    removeMessages(MSG_RINGER_MODE_CHANGED);
                    sendMessage(obtainMessage(MSG_RINGER_MODE_CHANGED));
                }
            }
        }, filter);
    }

    private boolean isMuted(int streamType) {
        if (streamType == STREAM_MASTER) {
            return mAudioManager.isMasterMute();
        } else if (streamType == AudioService.STREAM_REMOTE_MUSIC) {
            return (mAudioService.getRemoteStreamVolume() <= 0);
        } else {
            return mAudioManager.isStreamMute(streamType);
        }
    }

    private int getStreamMaxVolume(int streamType) {
        if (streamType == STREAM_MASTER) {
            return mAudioManager.getMasterMaxVolume();
        } else if (streamType == AudioService.STREAM_REMOTE_MUSIC) {
            return mAudioService.getRemoteStreamMaxVolume();
        } else {
            return mAudioManager.getStreamMaxVolume(streamType);
        }
    }

    private int getStreamVolume(int streamType) {
        if (streamType == STREAM_MASTER) {
            return mAudioManager.getMasterVolume();
        } else if (streamType == AudioService.STREAM_REMOTE_MUSIC) {
            return mAudioService.getRemoteStreamVolume();
        } else {
            return mAudioManager.getStreamVolume(streamType);
        }
    }

    private void setStreamVolume(int streamType, int index, int flags) {
        if (streamType == STREAM_MASTER) {
            mAudioManager.setMasterVolume(index, flags);
        } else if (streamType == AudioService.STREAM_REMOTE_MUSIC) {
            mAudioService.setRemoteStreamVolume(index);
        } else {
            mAudioManager.setStreamVolume(streamType, index, flags);
        }
    }

    private void createSliders() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mStreamControls = new HashMap<Integer, StreamControl>(STREAMS.length);
        Resources res = mContext.getResources();
        for (int i = 0; i < STREAMS.length; i++) {
            StreamResources streamRes = STREAMS[i];
            int streamType = streamRes.streamType;
            if (mVoiceCapable && streamRes == StreamResources.NotificationStream) {
                streamRes = StreamResources.RingerStream;
            }
            StreamControl sc = new StreamControl();
            sc.streamType = streamType;
            int resId = R.layout.volume_adjust_item;
//            String theme = SystemProperties.get("persist.sys.aurora.overlay", "");
//            boolean isWomanTheme = "/system/theme/woman/".equals(theme);
//            if(isWomanTheme){
//            	resId = R.layout.volume_adjust_item_woman;
//            }
//            Log.e("woman", "theme:"+isWomanTheme);
            sc.group = (ViewGroup) inflater.inflate(resId, null);
            sc.group.setTag(sc);
            sc.icon = (ImageView) sc.group.findViewById(R.id.stream_icon);
            sc.icon.setTag(sc);
            sc.icon.setContentDescription(res.getString(streamRes.descRes));
            sc.iconRes = streamRes.iconRes;
            sc.iconMuteRes = streamRes.iconMuteRes;
            sc.icon.setImageResource(sc.iconRes);
            sc.seekbarView = (SeekBar) sc.group.findViewById(R.id.seekbar);
            int plusOne = (streamType == AudioSystem.STREAM_BLUETOOTH_SCO ||
                    streamType == AudioSystem.STREAM_VOICE_CALL) ? 1 : 0;
            sc.seekbarView.setMax(getStreamMaxVolume(streamType) + plusOne);
            sc.seekbarView.setOnSeekBarChangeListener(this);
            sc.seekbarView.setTag(sc);
            mStreamControls.put(streamType, sc);
        }
    }

    private void reorderSliders(int activeStreamType) {
        mSliderGroup.removeAllViews();

        StreamControl active = mStreamControls.get(activeStreamType);
        if (active == null) {
            Log.e("VolumePanel", "Missing stream type! - " + activeStreamType);
            mActiveStreamType = -1;
        } else {
            mSliderGroup.addView(active.group);
            mActiveStreamType = activeStreamType;
            active.group.setVisibility(View.VISIBLE);
            updateSlider(active);
        }

        addOtherVolumes();
    }

    private void addOtherVolumes() {
        if (!mShowCombinedVolumes) return;

        for (int i = 0; i < STREAMS.length; i++) {
            // Skip the phone specific ones and the active one
            final int streamType = STREAMS[i].streamType;
            if (!STREAMS[i].show || streamType == mActiveStreamType) {
                continue;
            }
            StreamControl sc = mStreamControls.get(streamType);
            mSliderGroup.addView(sc.group);
            updateSlider(sc);
        }
    }

    /** Update the mute and progress state of a slider */
    private void updateSlider(StreamControl sc) {
    	int progress = getStreamVolume(sc.streamType);
    	Log.e("index", "index:"+progress);
        sc.seekbarView.setProgress(progress);
        final boolean muted = isMuted(sc.streamType);
        // Force reloading the image resource
        sc.icon.setImageDrawable(null);
        sc.icon.setImageResource(muted ? sc.iconMuteRes : sc.iconRes);
        if (((sc.streamType == AudioManager.STREAM_RING) ||
                (sc.streamType == AudioManager.STREAM_NOTIFICATION)) &&
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            sc.icon.setImageResource(R.drawable.ic_audio_ring_notif_vibrate);
        }
        if (sc.streamType == AudioService.STREAM_REMOTE_MUSIC) {
            // never disable touch interactions for remote playback, the muting is not tied to
            // the state of the phone.
            sc.seekbarView.setEnabled(true);
        } else if ((sc.streamType != mAudioManager.getMasterStreamType() && muted) ||
                        (sConfirmSafeVolumeDialog != null)) {
            sc.seekbarView.setEnabled(false);
        } else {
            sc.seekbarView.setEnabled(true);
        }
    }

    private boolean isExpanded() {
        return mMoreButton.getVisibility() != View.VISIBLE;
    }

    private void expand() {
        final int count = mSliderGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            mSliderGroup.getChildAt(i).setVisibility(View.VISIBLE);
        }
        mMoreButton.setVisibility(View.INVISIBLE);
        mDivider.setVisibility(View.INVISIBLE);
    }

    private void collapse() {
        mMoreButton.setVisibility(View.VISIBLE);
        mDivider.setVisibility(View.VISIBLE);
        final int count = mSliderGroup.getChildCount();
        for (int i = 1; i < count; i++) {
            mSliderGroup.getChildAt(i).setVisibility(View.GONE);
        }
    }

    public void updateStates() {
        final int count = mSliderGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            StreamControl sc = (StreamControl) mSliderGroup.getChildAt(i).getTag();
            updateSlider(sc);
        }
    }

    public void postVolumeChanged(int streamType, int flags) {
        if (hasMessages(MSG_VOLUME_CHANGED)) return;
        synchronized (mLock) {
            if (mStreamControls == null) {
            	Log.e("volume", "mStreamControls == null");
                createSliders();
            }
        }
        Log.e("volume", "mStreamControls != null  streamType:"+streamType);
        removeMessages(MSG_FREE_RESOURCES);
        obtainMessage(MSG_VOLUME_CHANGED, streamType, flags).sendToTarget();
    }

    public void postRemoteVolumeChanged(int streamType, int flags) {
        if (hasMessages(MSG_REMOTE_VOLUME_CHANGED)) return;
        synchronized (mLock) {
            if (mStreamControls == null) {
                createSliders();
            }
        }
        removeMessages(MSG_FREE_RESOURCES);
        obtainMessage(MSG_REMOTE_VOLUME_CHANGED, streamType, flags).sendToTarget();
    }

    public void postRemoteSliderVisibility(boolean visible) {
        obtainMessage(MSG_SLIDER_VISIBILITY_CHANGED,
                AudioService.STREAM_REMOTE_MUSIC, visible ? 1 : 0).sendToTarget();
    }

    /**
     * Called by AudioService when it has received new remote playback information that
     * would affect the VolumePanel display (mainly volumes). The difference with
     * {@link #postRemoteVolumeChanged(int, int)} is that the handling of the posted message
     * (MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN) will only update the volume slider if it is being
     * displayed.
     * This special code path is due to the fact that remote volume updates arrive to AudioService
     * asynchronously. So after AudioService has sent the volume update (which should be treated
     * as a request to update the volume), the application will likely set a new volume. If the UI
     * is still up, we need to refresh the display to show this new value.
     */
    public void postHasNewRemotePlaybackInfo() {
        if (hasMessages(MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN)) return;
        // don't create or prevent resources to be freed, if they disappear, this update came too
        //   late and shouldn't warrant the panel to be displayed longer
        obtainMessage(MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN).sendToTarget();
    }

    public void postMasterVolumeChanged(int flags) {
        postVolumeChanged(STREAM_MASTER, flags);
    }

    public void postMuteChanged(int streamType, int flags) {
        if (hasMessages(MSG_VOLUME_CHANGED)) return;
        synchronized (mLock) {
            if (mStreamControls == null) {
                createSliders();
            }
        }
        removeMessages(MSG_FREE_RESOURCES);
        obtainMessage(MSG_MUTE_CHANGED, streamType, flags).sendToTarget();
    }

    public void postMasterMuteChanged(int flags) {
        postMuteChanged(STREAM_MASTER, flags);
    }

    public void postDisplaySafeVolumeWarning(int flags) {
        if (hasMessages(MSG_DISPLAY_SAFE_VOLUME_WARNING)) return;
        obtainMessage(MSG_DISPLAY_SAFE_VOLUME_WARNING, flags, 0).sendToTarget();
    }
    
    public void postDisplaySafeVolumeWarning(){
    	 if (hasMessages(MSG_DISPLAY_SAFE_VOLUME_WARNING)) return;
         obtainMessage(MSG_DISPLAY_SAFE_VOLUME_WARNING, 0, 0).sendToTarget();
    }

    /**
     * Override this if you have other work to do when the volume changes (for
     * example, vibrating, playing a sound, etc.). Make sure to call through to
     * the superclass implementation.
     */
    protected void onVolumeChanged(int streamType, int flags) {
        if ((flags & AudioManager.FLAG_SHOW_UI) != 0) {
            synchronized (mLock) {
                if (mActiveStreamType != streamType) {
                    reorderSliders(streamType);
                }
                onShowVolumeChanged(streamType, flags);
            }
        }

        if ((flags & AudioManager.FLAG_PLAY_SOUND) != 0 && ! mRingIsSilent) {
            removeMessages(MSG_PLAY_SOUND);
            sendMessageDelayed(obtainMessage(MSG_PLAY_SOUND, streamType, flags), PLAY_SOUND_DELAY);
        }

        if ((flags & AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE) != 0) {
            removeMessages(MSG_PLAY_SOUND);
            removeMessages(MSG_VIBRATE);
            onStopSounds();
        }
        
        removeMessages(MSG_FREE_RESOURCES);
        sendMessageDelayed(obtainMessage(MSG_FREE_RESOURCES), FREE_DELAY);
        resetTimeout();
    }

    protected void onMuteChanged(int streamType, int flags) {

        if (LOGD) Log.d(TAG, "onMuteChanged(streamType: " + streamType + ", flags: " + flags + ")");

        StreamControl sc = mStreamControls.get(streamType);
        if (sc != null) {
            sc.icon.setImageResource(isMuted(sc.streamType) ? sc.iconMuteRes : sc.iconRes);
        }

        onVolumeChanged(streamType, flags);
    }

    protected void onShowVolumeChanged(int streamType, int flags) {
         
        int index = getStreamVolume(streamType);
        mRingIsSilent = false;

        if (LOGD) {
            Log.d(TAG, "onShowVolumeChanged(streamType: " + streamType
                    + ", flags: " + flags + "), index: " + index);
        }

        // get max volume for progress bar

        int max = getStreamMaxVolume(streamType);

        switch (streamType) {

            case AudioManager.STREAM_RING: {
//                setRingerIcon();
                Uri ringuri = RingtoneManager.getActualDefaultRingtoneUri(
                        mContext, RingtoneManager.TYPE_RINGTONE);
                if (ringuri == null) {
                    mRingIsSilent = true;
                }
                break;
            }

            case AudioManager.STREAM_MUSIC: {
                // Special case for when Bluetooth is active for music
                if ((mAudioManager.getDevicesForStream(AudioManager.STREAM_MUSIC) &
                        (AudioManager.DEVICE_OUT_BLUETOOTH_A2DP |
                        AudioManager.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES |
                        AudioManager.DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER)) != 0) {
                    setMusicIcon(R.drawable.ic_audio_bt, R.drawable.ic_audio_bt_mute);
                } else {
                    setMusicIcon(R.drawable.ic_audio_vol, R.drawable.ic_audio_vol_mute);
                }
                break;
            }

            case AudioManager.STREAM_VOICE_CALL: {
                /*
                 * For in-call voice call volume, there is no inaudible volume.
                 * Rescale the UI control so the progress bar doesn't go all
                 * the way to zero and don't show the mute icon.
                 */
                index++;
                max++;
                break;
            }

            case AudioManager.STREAM_ALARM: {
                break;
            }

            case AudioManager.STREAM_NOTIFICATION: {
                Uri ringuri = RingtoneManager.getActualDefaultRingtoneUri(
                        mContext, RingtoneManager.TYPE_NOTIFICATION);
                if (ringuri == null) {
                    mRingIsSilent = true;
                }
                break;
            }

            case AudioManager.STREAM_BLUETOOTH_SCO: {
                /*
                 * For in-call voice call volume, there is no inaudible volume.
                 * Rescale the UI control so the progress bar doesn't go all
                 * the way to zero and don't show the mute icon.
                 */
                index++;
                max++;
                break;
            }

            case AudioService.STREAM_REMOTE_MUSIC: {
                if (LOGD) { Log.d(TAG, "showing remote volume "+index+" over "+ max); }
                break;
            }
        }

        StreamControl sc = mStreamControls.get(streamType);
        if (sc != null) {
            if (sc.seekbarView.getMax() != max) {
                sc.seekbarView.setMax(max);
            }

            sc.seekbarView.setProgress(index);
            if (((flags & AudioManager.FLAG_FIXED_VOLUME) != 0) ||
                    (streamType != mAudioManager.getMasterStreamType() &&
                     streamType != AudioService.STREAM_REMOTE_MUSIC &&
                     isMuted(streamType)) ||
                     sConfirmSafeVolumeDialog != null) {
                sc.seekbarView.setEnabled(false);
            } else {
                sc.seekbarView.setEnabled(true);
            }
        }

        if(mDialog !=null){
        if (!mDialog.isShowing()) {
            int stream = (streamType == AudioService.STREAM_REMOTE_MUSIC) ? -1 : streamType;
            // when the stream is for remote playback, use -1 to reset the stream type evaluation
            mAudioManager.forceVolumeControlStream(stream);
            mDialog.setContentView(mView);
            // Showing dialog - use collapsed state
            if (mShowCombinedVolumes) {
                collapse();
            }
            mDialog.show();
        }
        }
        // Do a little vibrate if applicable (only when going into vibrate mode)
        if ((streamType != AudioService.STREAM_REMOTE_MUSIC) &&
                ((flags & AudioManager.FLAG_VIBRATE) != 0) &&
                mAudioService.isStreamAffectedByRingerMode(streamType) &&
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            sendMessageDelayed(obtainMessage(MSG_VIBRATE), VIBRATE_DELAY);
        }
    }

    protected void onPlaySound(int streamType, int flags) {
        auroraOnPlaySound(streamType, flags);

     /*   if (hasMessages(MSG_STOP_SOUNDS)) {
            removeMessages(MSG_STOP_SOUNDS);
            // Force stop right now
            onStopSounds();
        }

        synchronized (mLock) {
            ToneGenerator toneGen = getOrCreateToneGenerator(streamType);
            if (toneGen != null) {
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP);
                sendMessageDelayed(obtainMessage(MSG_STOP_SOUNDS), BEEP_DURATION);
            }
        }*/

    }

    //add for aurora hanping 2014.4.28
    protected void auroraOnPlaySound(int streamType, int flags) {

        if (hasMessages(MSG_STOP_SOUNDS)) {
            removeMessages(MSG_STOP_SOUNDS);
            // Force stop right now
            onStopSounds();
        }
        
        if(mAuroraSoundPool == null) {
        	return;
        }
        
        
        if (mAuroraIsLoadSoundPool) {
        	try {
        		int index = getStreamVolume(streamType);
        		if(index > 0) {
        			if(index > 6){
        				index = 6;
        			}
        			mSoundPoolStreamId = mAuroraSoundPool.play(mAuroraSoundPoolMap.get(index), 1.0F, 1.0F, 0, 0, 1.0F);
        		}
                sendMessageDelayed(obtainMessage(MSG_STOP_SOUNDS), BEEP_DURATION);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
        }
        
    }
    //add for aurora hanping 2014.4.28
	
    protected void onStopSounds() {
		auroraOnStopSounds();
/*
        synchronized (mLock) {
            int numStreamTypes = AudioSystem.getNumStreamTypes();
            for (int i = numStreamTypes - 1; i >= 0; i--) {
                ToneGenerator toneGen = mToneGenerators[i];
                if (toneGen != null) {
                    toneGen.stopTone();
                }
            }
        }
*/
    }

    //modify key_up sound 20140225 begin for aurora
	protected void auroraOnStopSounds() {
		if (mAuroraSoundPool != null) {
			mAuroraSoundPool.stop(mSoundPoolStreamId);
	    }
	}
    //modify key_up sound 20140225 end for aurora

    protected void onVibrate() {

        // Make sure we ended up in vibrate ringer mode
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE) {
            return;
        }

        mVibrator.vibrate(VIBRATE_DURATION);
    }

    protected void onRemoteVolumeChanged(int streamType, int flags) {
        // streamType is the real stream type being affected, but for the UI sliders, we
        // refer to AudioService.STREAM_REMOTE_MUSIC. We still play the beeps on the real
        // stream type.
        if (LOGD) Log.d(TAG, "onRemoteVolumeChanged(stream:"+streamType+", flags: " + flags + ")");
        if(mDialog !=null){
        if (((flags & AudioManager.FLAG_SHOW_UI) != 0) || mDialog.isShowing()) {
            synchronized (mLock) {
                if (mActiveStreamType != AudioService.STREAM_REMOTE_MUSIC) {
                    reorderSliders(AudioService.STREAM_REMOTE_MUSIC);
                }
                onShowVolumeChanged(AudioService.STREAM_REMOTE_MUSIC, flags);
            }
        } else {
            if (LOGD) Log.d(TAG, "not calling onShowVolumeChanged(), no FLAG_SHOW_UI or no UI");
        }
        }

        if ((flags & AudioManager.FLAG_PLAY_SOUND) != 0 && ! mRingIsSilent) {
            removeMessages(MSG_PLAY_SOUND);
            sendMessageDelayed(obtainMessage(MSG_PLAY_SOUND, streamType, flags), PLAY_SOUND_DELAY);
        }

        if ((flags & AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE) != 0) {
            removeMessages(MSG_PLAY_SOUND);
            removeMessages(MSG_VIBRATE);
            onStopSounds();
        }

        removeMessages(MSG_FREE_RESOURCES);
        sendMessageDelayed(obtainMessage(MSG_FREE_RESOURCES), FREE_DELAY);
        resetTimeout();
    }

    protected void onRemoteVolumeUpdateIfShown() {
        if (LOGD) Log.d(TAG, "onRemoteVolumeUpdateIfShown()");
        if(mDialog !=null){
        if (mDialog.isShowing()
                && (mActiveStreamType == AudioService.STREAM_REMOTE_MUSIC)
                && (mStreamControls != null)) {
            onShowVolumeChanged(AudioService.STREAM_REMOTE_MUSIC, 0);
        }
        }
    }


    /**
     * Handler for MSG_SLIDER_VISIBILITY_CHANGED
     * Hide or show a slider
     * @param streamType can be a valid stream type value, or VolumePanel.STREAM_MASTER,
     *                   or AudioService.STREAM_REMOTE_MUSIC
     * @param visible
     */
    synchronized protected void onSliderVisibilityChanged(int streamType, int visible) {
        if (LOGD) Log.d(TAG, "onSliderVisibilityChanged(stream="+streamType+", visi="+visible+")");
        boolean isVisible = (visible == 1);
        for (int i = STREAMS.length - 1 ; i >= 0 ; i--) {
            StreamResources streamRes = STREAMS[i];
            if (streamRes.streamType == streamType) {
                streamRes.show = isVisible;
                if (!isVisible && (mActiveStreamType == streamType)) {
                    mActiveStreamType = -1;
                }
                break;
            }
        }
    }

    protected void onDisplaySafeVolumeWarning(int flags) {
    	if(mDialog !=null){
        if ((flags & AudioManager.FLAG_SHOW_UI) != 0 || mDialog.isShowing()) {
            synchronized (sConfirmSafeVolumeLock) {
                if (sConfirmSafeVolumeDialog != null) {
                    return;
                }
                sConfirmSafeVolumeDialog = new AlertDialog.Builder(mContext)
                .setMessage(com.aurora.internal.R.string.safe_media_volume_warning)
                .setPositiveButton(com.aurora.internal.R.string.aurora_yes,
                                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAudioService.disableSafeMediaVolume();
                    }
                })
                .setNegativeButton(com.aurora.internal.R.string.aurora_no, null)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .create();
                final WarningDialogReceiver warning = new WarningDialogReceiver(mContext,
                        sConfirmSafeVolumeDialog, this);

                sConfirmSafeVolumeDialog.setOnDismissListener(warning);
                sConfirmSafeVolumeDialog.getWindow().setType(
                                                WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
                sConfirmSafeVolumeDialog.show();
            }
            updateStates();
        }
    	}
        resetTimeout();
    }

    /**
     * Lock on this VolumePanel instance as long as you use the returned ToneGenerator.
     */
    private ToneGenerator getOrCreateToneGenerator(int streamType) {
        if (streamType == STREAM_MASTER) {
            // For devices that use the master volume setting only but still want to
            // play a volume-changed tone, direct the master volume pseudostream to
            // the system stream's tone generator.
            if (mPlayMasterStreamTones) {
                streamType = AudioManager.STREAM_SYSTEM;
            } else {
                return null;
            }
        }
        synchronized (mLock) {
            if (mToneGenerators[streamType] == null) {
                try {
                    mToneGenerators[streamType] = new ToneGenerator(streamType, MAX_VOLUME);
                } catch (RuntimeException e) {
                    if (LOGD) {
                        Log.d(TAG, "ToneGenerator constructor failed with "
                                + "RuntimeException: " + e);
                    }
                }
            }
            return mToneGenerators[streamType];
        }
    }


    /**
     * Switch between icons because Bluetooth music is same as music volume, but with
     * different icons.
     */
    private void setMusicIcon(int resId, int resMuteId) {
        StreamControl sc = mStreamControls.get(AudioManager.STREAM_MUSIC);
        if (sc != null) {
            sc.iconRes = resId;
            sc.iconMuteRes = resMuteId;
            sc.icon.setImageResource(isMuted(sc.streamType) ? sc.iconMuteRes : sc.iconRes);
        }
    }

    protected void onFreeResources() {
        synchronized (mLock) {
            for (int i = mToneGenerators.length - 1; i >= 0; i--) {
                if (mToneGenerators[i] != null) {
                    mToneGenerators[i].release();
                }
                mToneGenerators[i] = null;
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {

            case MSG_VOLUME_CHANGED: {
                onVolumeChanged(msg.arg1, msg.arg2);
                break;
            }

            case MSG_MUTE_CHANGED: {
                onMuteChanged(msg.arg1, msg.arg2);
                break;
            }

            case MSG_FREE_RESOURCES: {
                onFreeResources();
                break;
            }

            case MSG_STOP_SOUNDS: {
                onStopSounds();
                break;
            }

            case MSG_PLAY_SOUND: {
                onPlaySound(msg.arg1, msg.arg2);
                break;
            }

            case MSG_VIBRATE: {
                onVibrate();
                break;
            }

            case MSG_TIMEOUT: {
            	if(mDialog !=null){
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                    mActiveStreamType = -1;
                }
            	}
                synchronized (sConfirmSafeVolumeLock) {
                    if (sConfirmSafeVolumeDialog != null) {
                        sConfirmSafeVolumeDialog.dismiss();
                    }
                }
                break;
            }
            case MSG_RINGER_MODE_CHANGED: {
            	if(mDialog !=null){
                if (mDialog.isShowing()) {
                    updateStates();
                }
            	}
                break;
            }

            case MSG_REMOTE_VOLUME_CHANGED: {
                onRemoteVolumeChanged(msg.arg1, msg.arg2);
                break;
            }

            case MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN:
                onRemoteVolumeUpdateIfShown();
                break;

            case MSG_SLIDER_VISIBILITY_CHANGED:
                onSliderVisibilityChanged(msg.arg1, msg.arg2);
                break;

            case MSG_DISPLAY_SAFE_VOLUME_WARNING:
                onDisplaySafeVolumeWarning(msg.arg1);
                break;
        }
    }

    private void resetTimeout() {
        removeMessages(MSG_TIMEOUT);
        sendMessageDelayed(obtainMessage(MSG_TIMEOUT), TIMEOUT_DELAY);
    }

    private void forceTimeout() {
        removeMessages(MSG_TIMEOUT);
        sendMessage(obtainMessage(MSG_TIMEOUT));
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        final Object tag = seekBar.getTag();
        if (fromUser && tag instanceof StreamControl) {
            StreamControl sc = (StreamControl) tag;
            if (getStreamVolume(sc.streamType) != progress) {
                setStreamVolume(sc.streamType, progress, 0);
            }
        }
         // Aurora <Luofu> <2014-7-9> modify for seekbar animation begin
        auroraMuteAnimationStart(seekBar,progress);
     // Aurora <Luofu> <2014-7-9> modify for seekbar animation end
        resetTimeout();
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        final Object tag = seekBar.getTag();
        if (tag instanceof StreamControl) {
            StreamControl sc = (StreamControl) tag;
            // because remote volume updates are asynchronous, AudioService might have received
            // a new remote volume value since the finger adjusted the slider. So when the
            // progress of the slider isn't being tracked anymore, adjust the slider to the last
            // "published" remote volume value, so the UI reflects the actual volume.
            if (sc.streamType == AudioService.STREAM_REMOTE_MUSIC) {
                seekBar.setProgress(getStreamVolume(AudioService.STREAM_REMOTE_MUSIC));
            }
        }
    }

    public void onClick(View v) {
        if (v == mMoreButton) {
            expand();
        }
        resetTimeout();
    }



 // Aurora <Luofu> <2014-7-9> modify for Seekbar when it's progress change to 0 begin
    
    private AnimatorSet auroraMuteAnimationSet;// shake animation
    private int auroraMuteVibrateDistanceLevel = 50;// shake distance
    private int auroraMuteVibrateDuration = 500;// shake duration
    private int auroraMuteVibrateTotal = 4;// shake repeat times
    private int auroraFirstDelta = 20;
    private int auroraSecondeDelta = 10;
    private int auroraThirdDelta = 5;
    private int[] auroraMuteVibrateArray;
    
    private SeekBar auroraSeekBar;
    private void auroraCancelAnimation()
    {
        if ((this.auroraMuteAnimationSet != null) && (this.auroraMuteAnimationSet.isRunning())) {
            this.auroraMuteAnimationSet.cancel();
        }
    }

    public void auroraMuteAnimationStart(SeekBar seekbar,int progress)
    {
        if(progress == 0){
            auroraInitMuteAnimation(seekbar);
            auroraCancelAnimation();
            this.auroraMuteAnimationSet.start();
        }
        
    }

    private  int auroraDip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    private void auroraInitMuteAnimation(final SeekBar seekbar) {
        auroraSeekBar = seekbar;
        auroraMuteVibrateDistanceLevel = auroraDip2px(mContext,10f);
        auroraFirstDelta = auroraDip2px(mContext,5f);
        auroraSecondeDelta = auroraDip2px(mContext,2.5f);
        auroraThirdDelta = auroraDip2px(mContext,1.25f);
        this.auroraMuteAnimationSet = new AnimatorSet();
        ArrayList localArrayList = new ArrayList();
        //int i = 2 * this.auroraMuteVibrateTotal;
       // int j = this.auroraMuteVibrateDuration / i;
        int k = this.auroraMuteVibrateDistanceLevel;
        auroraMuteVibrateArray = new int[]{k-auroraFirstDelta, k,
                                           k,k-auroraFirstDelta,
                                           k-auroraFirstDelta,k,
                                           k-auroraSecondeDelta,k,
                                           k-auroraSecondeDelta,k,
                                           k-auroraThirdDelta,k,
                                           k-auroraThirdDelta,k};
       // for (int times = 0; times < auroraMuteVibrateTotal-1; times++) {
            ValueAnimator offsetAnimator = ValueAnimator.ofInt(auroraMuteVibrateArray);
            offsetAnimator.setDuration(auroraMuteVibrateDuration);
            offsetAnimator.setInterpolator(new LinearInterpolator());
            offsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                public void onAnimationUpdate(ValueAnimator animation)
                {
                    if(auroraSeekBar != null){
                        auroraSeekBar.setThumbOffset(((Integer) animation.getAnimatedValue()).intValue());
                    Log.e("ll", ""+((Integer) animation.getAnimatedValue()).intValue());
                    }
                }
            });
            localArrayList.add(offsetAnimator);
            //k = (int) (0.5 * k);
        //}
        this.auroraMuteAnimationSet.playSequentially(localArrayList);
    }
    
    
 // Aurora <Luofu> <2014-7-9> modify for Seekbar when it's progress change to 0 end
    
	//add for aurora hanping 2014.4.28
    private SoundPool mAuroraSoundPool;
    private int mSoundPoolStreamId;
    static boolean mAuroraIsLoadSoundPool;
    private HashMap<Integer, Integer> mAuroraSoundPoolMap;
    
	private void auroraInit() {
		mAuroraSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
		mAuroraSoundPoolMap = new HashMap<Integer, Integer>();
		mAuroraSoundPool.setOnLoadCompleteListener(new AuroraLoadListener());

		for (int i = 1; i < 16; i++) {
			String str = "system/media/audio/ui/Key" + i + ".ogg";
			mAuroraSoundPoolMap.put(i, mAuroraSoundPool.load(str, 0));
		}
	}
    
	private class AuroraLoadListener implements SoundPool.OnLoadCompleteListener {
		private AuroraLoadListener() {
			
		}

		public void onLoadComplete(SoundPool paramSoundPool, int sampleId, int status) {
			Log.d("VolumePanel", "onLoadComplete(sampleId: " + sampleId
					+ ", status: " + status + ")");
			if (status >= 0) {
				AuroraVolumePanel.mAuroraIsLoadSoundPool = true;
				return;
			}
			AuroraVolumePanel.mAuroraIsLoadSoundPool = false;
		}
	}
	//add for aurora hanping 2014.4.28



}

