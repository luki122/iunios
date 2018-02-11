/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.incallui;

import com.android.incallui.CallList;

import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.telecom.AudioState;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.os.storage.StorageManager;

/**
 * Fragment for call control buttons
 */
public class AuroraCallButtonFragment
        extends BaseFragment<AuroraCallButtonPresenter, AuroraCallButtonPresenter.CallButtonUi>
        implements AuroraCallButtonPresenter.CallButtonUi, OnMenuItemClickListener, OnDismissListener,
        View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private CompoundButton mAudioButton;
    private CompoundButton mMuteButton;
    private CompoundButton mShowDialpadButton;
    private CompoundButton mHoldButton;
    private CompoundButton mSwapButton;
    private CompoundButton mAddCallButton;
    private CompoundButton mMergeButton;
    phoneCompoundButton mRecordButton;
    TextView mRecordTime;
    private ImageView mEndButton;
	private ImageView mEndButton2;
	private ImageView mHideDialpad;
	
    private AuroraInCallTouchUiAnimationController mAnimController;

    private PopupMenu mAudioModePopup;
    private boolean mAudioModePopupVisible;

    private int mPrevAudioMode = 0;

    // Constants for Drawable.setAlpha()
    private static final int HIDDEN = 0;
    private static final int VISIBLE = 255;

    private boolean mIsEnabled;

    @Override
    AuroraCallButtonPresenter createPresenter() {
        // TODO: find a cleaner way to include audio mode provider than having a singleton instance.
        return new AuroraCallButtonPresenter();
    }

    @Override
    AuroraCallButtonPresenter.CallButtonUi getUi() {
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.aurora_call_button_fragment, container, false);

        mAudioButton = (CompoundButton) parent.findViewById(R.id.audioButton);
        mAudioButton.setOnClickListener(this);
        mMuteButton = (CompoundButton) parent.findViewById(R.id.muteButton);
        mMuteButton.setOnClickListener(this);
        mShowDialpadButton = (CompoundButton) parent.findViewById(R.id.dialpadButton);
        mShowDialpadButton.setOnClickListener(this);
        mHoldButton = (CompoundButton) parent.findViewById(R.id.holdButton);
        mHoldButton.setOnClickListener(this);
        mSwapButton = (CompoundButton) parent.findViewById(R.id.swapButton);
        mSwapButton.setOnClickListener(this);
        mAddCallButton = (CompoundButton) parent.findViewById(R.id.addButton);
        mAddCallButton.setOnClickListener(this);
        mMergeButton = (CompoundButton) parent.findViewById(R.id.mergeButton);
        mMergeButton.setOnClickListener(this);
        mRecordButton = (phoneCompoundButton) parent.findViewById(R.id.recordButton);
        mRecordButton.setOnClickListener(this);
        mRecordTime = (TextView) parent.findViewById(R.id.record_time);
        
		View end_bottom_button = parent.findViewById(R.id.end_bottom_button);
		mEndButton = (ImageView) end_bottom_button.findViewById(R.id.endButton);
		mEndButton.setOnClickListener(this);
		mEndButton2 = (ImageView) end_bottom_button.findViewById(R.id.end_keypad);
		mEndButton2.setOnClickListener(this);
		mHideDialpad = (ImageView) end_bottom_button.findViewById(R.id.hide_keypad);
		mHideDialpad.setOnClickListener(this);
        mAnimController = ((InCallActivity) getActivity()).mInCallTouchUiAnimationController;
        
        mAnimController.setInCallControlsView(parent);

        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set the buttons
        updateAudioButtons(getPresenter().getSupportedAudio());
        
    }

    @Override
    public void onResume() {
        if (getPresenter() != null) {
            getPresenter().refreshMuteState();
        }
        super.onResume();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d(this, "onClick(View " + view + ", id " + id + ")...");

        switch(id) {
            case R.id.audioButton:
                onAudioButtonClicked();
                break;
            case R.id.addButton:
                getPresenter().addCallClicked();
                break;
            case R.id.muteButton: {
                final CompoundButton button = (CompoundButton) view;
                getPresenter().muteClicked(!button.isChecked());
                break;
            }
            case R.id.mergeButton:
                getPresenter().mergeClicked();
                break;
            case R.id.holdButton: {
                final CompoundButton button = (CompoundButton) view;
                getPresenter().holdClicked(!button.isChecked());
                break;
            }
            case R.id.swapButton:
                getPresenter().swapClicked();
                break;
            case R.id.dialpadButton:
                getPresenter().showDialpadClicked(!mShowDialpadButton.isChecked());
                break;
            case R.id.endButton:
            case R.id.end_keypad:
        		InCallActivity activity = (InCallActivity) getActivity();
        		if (activity != null) {
        			activity.getCallCardFragment().getPresenter().endCallClicked();
        		}
            	break;
            case R.id.hide_keypad:
                getPresenter().showDialpadClicked(false);
            	mAnimController.setKeypadEnable(false);
            	break;
            case R.id.recordButton:
            	AuroraCallMonitor.getInstance().mManageRecord.onRecordClick();
            	break;
            default:
                Log.wtf(this, "onClick: unexpected");
                break;
        }
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        mIsEnabled = isEnabled;
//        View view = getView();
//        if (view.getVisibility() != View.VISIBLE) {
//            view.setVisibility(View.VISIBLE);
//        }

        mAudioButton.setEnabled(isEnabled);
        mMuteButton.setEnabled(isEnabled);
        mShowDialpadButton.setEnabled(isEnabled);
        mHoldButton.setEnabled(isEnabled);
        mSwapButton.setEnabled(isEnabled);
        mAddCallButton.setEnabled(isEnabled);
        mMergeButton.setEnabled(isEnabled);
        mRecordButton.setEnabled(isEnabled);
        
//        mEndButton.setEnabled(isEnabled);
//        mAnimController.updateEndButtonText(isEnabled);
//        mEndButton2.setEnabled(isEnabled);
//        mHideDialpad.setEnabled(isEnabled);
        
    }

    @Override
    public void setMute(boolean value) {
        if (mMuteButton.isChecked() != value) {
            mMuteButton.setChecked(value);
            maybeSendAccessibilityEvent(mMuteButton, value ? R.string.accessibility_call_muted
                    : R.string.accessibility_call_unmuted);
        }
    }



    @Override
    public void enableMute(boolean enabled) {
        mMuteButton.setEnabled(enabled);
    }


    @Override
    public void setHold(boolean value) {
        if (mHoldButton.isChecked() != value) {
            mHoldButton.setChecked(value);
            maybeSendAccessibilityEvent(mHoldButton,
                    value ? R.string.accessibility_call_put_on_hold :
                            R.string.accessibility_call_removed_from_hold);
        }
    }


    @Override
    public void enableHold(boolean enabled) {
        mHoldButton.setEnabled(enabled);
    }

    @Override
    public void showSwapButton(boolean show) {
        mSwapButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mHoldButton.setVisibility(!show ? View.VISIBLE : View.GONE);
    }


    @Override
    public void showMergeButton(boolean show) {
        mMergeButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mAddCallButton.setVisibility(!show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setAudio(int mode) {
        updateAudioButtons(getPresenter().getSupportedAudio());
        refreshAudioModePopup();

        if (mPrevAudioMode != mode) {
            if (mPrevAudioMode != 0) {
                int stringId = 0;
                switch (mode) {
                    case AudioState.ROUTE_EARPIECE:
                        stringId = R.string.accessibility_earpiece_selected;
                        break;
                    case AudioState.ROUTE_BLUETOOTH:
                        stringId = R.string.accessibility_bluetooth_headset_selected;
                        break;
                    case AudioState.ROUTE_WIRED_HEADSET:
                        stringId = R.string.accessibility_wired_headset_selected;
                        break;
                    case AudioState.ROUTE_SPEAKER:
                        stringId = R.string.accessibility_speakerphone_selected;
                        break;
                }
                if (stringId != 0) {
                    maybeSendAccessibilityEvent(mAudioButton, stringId);
                }
            }
            mPrevAudioMode = mode;
        }
    }

    @Override
    public void setSupportedAudio(int modeMask) {
        updateAudioButtons(modeMask);
        refreshAudioModePopup();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Log.d(this, "- onMenuItemClick: " + item);
        Log.d(this, "  id: " + item.getItemId());
        Log.d(this, "  title: '" + item.getTitle() + "'");

        int mode = AudioState.ROUTE_WIRED_OR_EARPIECE;

        switch (item.getItemId()) {
            case R.id.audio_mode_speaker:
                mode = AudioState.ROUTE_SPEAKER;
                break;
            case R.id.audio_mode_earpiece:
            case R.id.audio_mode_wired_headset:
                // InCallAudioState.ROUTE_EARPIECE means either the handset earpiece,
                // or the wired headset (if connected.)
                mode = AudioState.ROUTE_WIRED_OR_EARPIECE;
                break;
            case R.id.audio_mode_bluetooth:
                mode = AudioState.ROUTE_BLUETOOTH;
                break;
            default:
                Log.e(this, "onMenuItemClick:  unexpected View ID " + item.getItemId()
                        + " (MenuItem = '" + item + "')");
                break;
        }

        getPresenter().setAudioMode(mode);

        return true;
    }

    // PopupMenu.OnDismissListener implementation; see showAudioModePopup().
    // This gets called when the PopupMenu gets dismissed for *any* reason, like
    // the user tapping outside its bounds, or pressing Back, or selecting one
    // of the menu items.
    @Override
    public void onDismiss(PopupMenu menu) {
        Log.d(this, "- onDismiss: " + menu);
        mAudioModePopupVisible = false;
    }

    /**
     * Checks for supporting modes.  If bluetooth is supported, it uses the audio
     * pop up menu.  Otherwise, it toggles the speakerphone.
     */
    private void onAudioButtonClicked() {
        Log.d(this, "onAudioButtonClicked: " +
                AudioState.audioRouteToString(getPresenter().getSupportedAudio()));

        if (isSupported(AudioState.ROUTE_BLUETOOTH)) {
            showAudioModePopup();
        } else {
            getPresenter().toggleSpeakerphone();
        }
    }

    /**
     * Refreshes the "Audio mode" popup if it's visible.  This is useful
     * (for example) when a wired headset is plugged or unplugged,
     * since we need to switch back and forth between the "earpiece"
     * and "wired headset" items.
     *
     * This is safe to call even if the popup is already dismissed, or even if
     * you never called showAudioModePopup() in the first place.
     */
    public void refreshAudioModePopup() {
        if (mAudioModePopup != null && mAudioModePopupVisible) {
            // Dismiss the previous one
            mAudioModePopup.dismiss();  // safe even if already dismissed
            // And bring up a fresh PopupMenu
            showAudioModePopup();
        }
    }

    /**
     * Updates the audio button so that the appriopriate visual layers
     * are visible based on the supported audio formats.
     */
    private void updateAudioButtons(int supportedModes) {
        final boolean bluetoothSupported = isSupported(AudioState.ROUTE_BLUETOOTH);
        final boolean speakerSupported = isSupported(AudioState.ROUTE_SPEAKER);

        boolean audioButtonEnabled = false;
        boolean audioButtonChecked = false;
        boolean showMoreIndicator = false;

        boolean showBluetoothIcon = false;
        boolean showSpeakerphoneIcon = false;
        boolean showHandsetIcon = false;

        boolean showToggleIndicator = false;

        if (bluetoothSupported) {
            Log.d(this, "updateAudioButtons - popup menu mode");

            audioButtonEnabled = true;
            showMoreIndicator = true;
            // The audio button is NOT a toggle in this state.  (And its
            // setChecked() state is irrelevant since we completely hide the
            // btn_compound_background layer anyway.)

            // Update desired layers:
            if (isAudio(AudioState.ROUTE_BLUETOOTH)) {
                showBluetoothIcon = true;
            } else if (isAudio(AudioState.ROUTE_SPEAKER)) {
                showSpeakerphoneIcon = true;
            } else {
                showHandsetIcon = true;
                // TODO: if a wired headset is plugged in, that takes precedence
                // over the handset earpiece.  If so, maybe we should show some
                // sort of "wired headset" icon here instead of the "handset
                // earpiece" icon.  (Still need an asset for that, though.)
            }
        } else if (speakerSupported) {
            Log.d(this, "updateAudioButtons - speaker toggle mode");

            audioButtonEnabled = true;

            // The audio button *is* a toggle in this state, and indicated the
            // current state of the speakerphone.
            audioButtonChecked = isAudio(AudioState.ROUTE_SPEAKER);

            // update desired layers:
            showToggleIndicator = true;
            showSpeakerphoneIcon = true;
        } else {
            Log.d(this, "updateAudioButtons - disabled...");

            // The audio button is a toggle in this state, but that's mostly
            // irrelevant since it's always disabled and unchecked.
            audioButtonEnabled = false;
            audioButtonChecked = false;

            // update desired layers:
            showToggleIndicator = true;
            showSpeakerphoneIcon = true;
        }

        // Finally, update it all!

        Log.v(this, "audioButtonEnabled: " + audioButtonEnabled);
        Log.v(this, "audioButtonChecked: " + audioButtonChecked);
        Log.v(this, "showMoreIndicator: " + showMoreIndicator);
        Log.v(this, "showBluetoothIcon: " + showBluetoothIcon);
        Log.v(this, "showSpeakerphoneIcon: " + showSpeakerphoneIcon);
        Log.v(this, "showHandsetIcon: " + showHandsetIcon);

        // Only enable the audio button if the fragment is enabled.
        mAudioButton.setEnabled(audioButtonEnabled && mIsEnabled);
        mAudioButton.setChecked(audioButtonChecked);

    }

    private void showAudioModePopup() {
        Log.d(this, "showAudioPopup()...");

        final ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(),
                R.style.InCallPopupMenuStyle);
        mAudioModePopup = new PopupMenu(contextWrapper, mAudioButton /* anchorView */);
        mAudioModePopup.getMenuInflater().inflate(R.menu.incall_audio_mode_menu,
                mAudioModePopup.getMenu());
        mAudioModePopup.setOnMenuItemClickListener(this);
        mAudioModePopup.setOnDismissListener(this);

        final Menu menu = mAudioModePopup.getMenu();

        // TODO: Still need to have the "currently active" audio mode come
        // up pre-selected (or focused?) with a blue highlight.  Still
        // need exact visual design, and possibly framework support for this.
        // See comments below for the exact logic.

        final MenuItem speakerItem = menu.findItem(R.id.audio_mode_speaker);
        speakerItem.setEnabled(isSupported(AudioState.ROUTE_SPEAKER));
        // TODO: Show speakerItem as initially "selected" if
        // speaker is on.

        // We display *either* "earpiece" or "wired headset", never both,
        // depending on whether a wired headset is physically plugged in.
        final MenuItem earpieceItem = menu.findItem(R.id.audio_mode_earpiece);
        final MenuItem wiredHeadsetItem = menu.findItem(R.id.audio_mode_wired_headset);

        final boolean usingHeadset = isSupported(AudioState.ROUTE_WIRED_HEADSET);
        earpieceItem.setVisible(!usingHeadset);
        earpieceItem.setEnabled(!usingHeadset);
        wiredHeadsetItem.setVisible(usingHeadset);
        wiredHeadsetItem.setEnabled(usingHeadset);
        // TODO: Show the above item (either earpieceItem or wiredHeadsetItem)
        // as initially "selected" if speakerOn and
        // bluetoothIndicatorOn are both false.

        final MenuItem bluetoothItem = menu.findItem(R.id.audio_mode_bluetooth);
        bluetoothItem.setEnabled(isSupported(AudioState.ROUTE_BLUETOOTH));
        // TODO: Show bluetoothItem as initially "selected" if
        // bluetoothIndicatorOn is true.

        mAudioModePopup.show();

        // Unfortunately we need to manually keep track of the popup menu's
        // visiblity, since PopupMenu doesn't have an isShowing() method like
        // Dialogs do.
        mAudioModePopupVisible = true;
    }

    private boolean isSupported(int mode) {
        return (mode == (getPresenter().getSupportedAudio() & mode));
    }

    private boolean isAudio(int mode) {
        return (mode == getPresenter().getAudioMode());
    }

    @Override
    public void displayDialpad(boolean value, boolean animate) {
        mShowDialpadButton.setChecked(value);
        if (getActivity() != null && getActivity() instanceof InCallActivity) {
            ((InCallActivity) getActivity()).displayDialpad(value, animate);
        }
    }

    @Override
    public boolean isDialpadVisible() {
        if (getActivity() != null && getActivity() instanceof InCallActivity) {
            return ((InCallActivity) getActivity()).isDialpadVisible();
        }
        return false;
    }
    
    @Override
    public boolean isManageConferenceVisible() {
        if (getActivity() != null && getActivity() instanceof InCallActivity) {
            return ((InCallActivity) getActivity()).isManageConferenceVisible();
        }
        return false;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    private void maybeSendAccessibilityEvent(View view, int stringId) {
        final Context context = getActivity();
        AccessibilityManager manager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager != null && manager.isEnabled()) {
            AccessibilityEvent e = AccessibilityEvent.obtain();
            e.setSource(view);
            e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            e.setClassName(getClass().getName());
            e.setPackageName(context.getPackageName());
            e.getText().add(context.getResources().getString(stringId));
            manager.sendAccessibilityEvent(e);
        }
    }
    
    public void setVisible(boolean show){
    	getView().setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    public void updateRecordTime(String time, boolean visible) {
        if (visible) {
            //mRecordButton.setBackgroundResource(R.drawable.recording);
        	mRecordButton.setDrawableEnabled(false);
        	mRecordButton.setPtext(R.string.gn_record);
            mRecordTime.setText(time);
        } else {
        	//mRecordButton.setBackgroundResource(R.drawable.aurora_record);
        	mRecordButton.setDrawableEnabled(true);
        	mRecordButton.setPtext(R.string.aurora_record_button_text_off);
        	mRecordTime.setText("");
        }
    }
    
    public void updateRecordBtnState() {
    	Log.d(this, "updateRecordBtnState");
        StorageManager sManager = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);
        if (!sManager.getVolumeState(GnPhoneRecordHelper.SDCARD_DEFAULT_PATH).equals(Environment.MEDIA_MOUNTED) && 
                !sManager.getVolumeState(GnPhoneRecordHelper.getSecondPath(getContext())).equals(Environment.MEDIA_MOUNTED)) {
        	Log.d(this, "sdcard not mounted");
            if (mRecordButton.isChecked()) {
                mRecordButton.setChecked(false);
            }
            mRecordButton.setEnabled(false);
        } else {
            Call fgCall = CallList.getInstance().getActiveCall();
            if(fgCall == null || fgCall.getState() != Call.State.ACTIVE) {
            	Log.d(this, "set record item disabled");
                if (mRecordButton.isChecked()) {
                    mRecordButton.setChecked(false);
                }
                mRecordButton.setEnabled(false);
            } else {
            	Log.d(this, "set record item enabled");
                mRecordButton.setEnabled(true);
            }
        } 
        //aurora add liguangyu 20140730 for BUG #7013 start
//        if(PhoneGlobals.getPhone().getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA 
//    			&& InCallApp.getInstance().cdmaPhoneCallState.getSiggleDialingState()) {
//            if (mRecordButton.isChecked()) {
//                mRecordButton.setChecked(false);
//            }
//            mRecordButton.setEnabled(false);        	
//        }
        //aurora add liguangyu 20140730 for BUG #7013 end
    }
    
    public void dismissPopupMenu() {}
}
