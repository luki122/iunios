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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telecom.DisconnectCause;
import android.telecom.VideoProfile;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.common.widget.FloatingActionButtonController;
import com.android.phone.common.animation.AnimUtils;

import java.util.List;

/**
 * Fragment for call card.
 */
public class AuroraCallCardFragment extends BaseFragment<AuroraCallCardPresenter,AuroraCallCardPresenter.CallCardUi>
        implements AuroraCallCardPresenter.CallCardUi {

    private AnimatorSet mAnimatorSet;
    private boolean mIsDialpadShowing;

    // Primary caller info
    private TextView mPhoneNumber;
    private TextView mNumberLabel;
    private TextView mPrimaryName;
    private TextView mCallStateLabel;
    private TextView mCallTypeLabel;
    private View mCallNumberAndLabel;
    private ImageView mPhoto;
    private TextView mElapsedTime;

    // Container view that houses the entire primary call card, including the call buttons
    private View mPrimaryCallCardContainer;
    // Container view that houses the primary call information
    private ViewGroup mPrimaryCallInfo;
    private View mCallButtonsContainer;

    // Secondary caller info
    private View mSecondaryCallInfo;
    private TextView mSecondaryCallName;
    private ImageView mSecondaryCallPhoto, mSimIcon2;
    
    
    private View mSecondaryCallProviderInfo;
    private TextView mSecondaryCallProviderLabel;
//    private ImageView mSecondaryCallProviderIcon;
//    private View mSecondaryCallConferenceCallIcon;
    private View mProgressSpinner;

    private View mManageConferenceCallButton;

    // Dark number info bar
    private TextView mInCallMessageLabel;


    // Cached DisplayMetrics density.
    private float mDensity;

    
    
    AuroraCallCardAnimationController mAnimController;
    private View mSogouLine;
	private TextView mSogouNote;
	private ImageView mSogouIcon;
	private ImageView mSimIcon;
    private View mNoteLine;
	private TextView mNote;
	private TextView mAdrress;
	private ImageView meclipstimebg;
	private TextView mSloganNote;
    private ImageView mPhotoV;
    
    
	
    public AuroraCallCardFragment() {
    }


    @Override
    AuroraCallCardPresenter.CallCardUi getUi() {
        return this;
    }

    @Override
    AuroraCallCardPresenter createPresenter() {
        return new AuroraCallCardPresenter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final CallList calls = CallList.getInstance();
        final Call call = calls.getFirstCall();
        getPresenter().init(getActivity(), call);
 
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mDensity = getResources().getDisplayMetrics().density;

        
        return inflater.inflate(R.layout.aurora_call_card_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPhoneNumber = (TextView) view.findViewById(R.id.phoneNumber);
        mPrimaryName = (TextView) view.findViewById(R.id.name);
        mNumberLabel = (TextView) view.findViewById(R.id.label);
        mSecondaryCallInfo = view.findViewById(R.id.secondary_call_info);
        mSecondaryCallProviderInfo = view.findViewById(R.id.secondary_call_provider_info);
        mPhoto = (ImageView) view.findViewById(R.id.photo);    
        mCallStateLabel = (TextView) view.findViewById(R.id.callStateLabel);
        mCallNumberAndLabel = view.findViewById(R.id.labelAndNumber);
        mCallTypeLabel = (TextView) view.findViewById(R.id.callTypeLabel);
        mElapsedTime = (TextView) view.findViewById(R.id.elapsedTime);
        mPrimaryCallCardContainer = view.findViewById(R.id.primary_call_info_container);
        mPrimaryCallInfo = (ViewGroup) view.findViewById(R.id.primary_call_banner);
        mCallButtonsContainer = view.findViewById(R.id.callButtonFragment);
        mInCallMessageLabel = (TextView) view.findViewById(R.id.connectionServiceMessage);
        mProgressSpinner = view.findViewById(R.id.progressSpinner);


        mSecondaryCallInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().secondaryInfoClicked();
                updateFabPositionForSecondaryCallInfo();
            }
        });


        mManageConferenceCallButton = view.findViewById(R.id.manage_conference_call_button);
        mManageConferenceCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InCallActivity activity = (InCallActivity) getActivity();
                activity.showConferenceCallManager(true);
            }
        });

        mPrimaryName.setElegantTextHeight(false);
        mCallStateLabel.setElegantTextHeight(false);
        
        
        mSimIcon = (ImageView) view.findViewById(R.id.aurora_sim_slot);
        mSimIcon.setAlpha(0.7f);
	    mSogouLine = view.findViewById(R.id.sogou_line);
 	    mSogouNote = (TextView)view.findViewById(R.id.sogou_note);
 	    mSogouIcon = (ImageView)view.findViewById(R.id.sogou_icon);
        mNoteLine =  view.findViewById(R.id.note_line);
        mNote = (TextView) view.findViewById(R.id.note);
        mAdrress = (TextView) view.findViewById(R.id.numberAddress);
        meclipstimebg =(ImageView)view.findViewById(R.id.meclapsedbg); 
        
		mSloganNote = (TextView)view.findViewById(R.id.slogan);
        mPhotoV = (ImageView) view.findViewById(R.id.photoV);
        
        mAnimController = ((InCallActivity) getActivity()).mCallCardAnimationController;        
        mAnimController.setView(view);
        
    }

    @Override
    public void setVisible(boolean on) {
        if (on) {
            getView().setVisibility(View.VISIBLE);
        } else {
            getView().setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Hides or shows the progress spinner.
     *
     * @param visible {@code True} if the progress spinner should be visible.
     */
    @Override
    public void setProgressSpinnerVisible(boolean visible) {
        mProgressSpinner.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


    /**
     * Determines the amount of space below the call card for portrait layouts), or beside the
     * call card for landscape layouts.
     *
     * @return The amount of space below or beside the call card.
     */
    public float getSpaceBesideCallCard() {
    	return 0;
    }

    @Override
    public void setPrimaryName(String name, boolean nameIsNumber) {
        if (TextUtils.isEmpty(name)) {
            mPrimaryName.setText(null);
        } else {
            mPrimaryName.setText(name);

            // Set direction of the name field
            int nameDirection = View.TEXT_DIRECTION_INHERIT;
            if (nameIsNumber) {
                nameDirection = View.TEXT_DIRECTION_LTR;
            }
            mPrimaryName.setTextDirection(nameDirection);
        }
    }

    @Override
    public void setPrimaryImage(Drawable image) {
        if (image != null) {
            setDrawableToImageView(mPhoto, image);
        }
    }

    @Override
    public void setPrimaryPhoneNumber(String number) {
        // Set the number
        if (TextUtils.isEmpty(number)) {
            mPhoneNumber.setText(null);
            mPhoneNumber.setVisibility(View.GONE);
        } else {
            mPhoneNumber.setText(number);
            mPhoneNumber.setVisibility(View.VISIBLE);
            mPhoneNumber.setTextDirection(View.TEXT_DIRECTION_LTR);
        }
    }

    @Override
    public void setPrimaryLabel(String label) {
        if (!TextUtils.isEmpty(label)) {
            mNumberLabel.setText(label);
            mNumberLabel.setVisibility(View.VISIBLE);
        } else {
            mNumberLabel.setVisibility(View.GONE);
        }

    }

    @Override
    public void setPrimary(String number, String name, boolean nameIsNumber, String label,
            Drawable photo, boolean isConference, boolean canManageConference, boolean isSipCall) {
        Log.d(this, "Setting primary call");

        if (isConference) {
            name = getConferenceString(canManageConference);
            photo = getConferencePhoto(canManageConference);
            photo.setAutoMirrored(true);
            nameIsNumber = false;
        }

        // set the name field.
        setPrimaryName(name, nameIsNumber);

        if (TextUtils.isEmpty(number) && TextUtils.isEmpty(label)) {
            mCallNumberAndLabel.setVisibility(View.GONE);
        } else {
            mCallNumberAndLabel.setVisibility(View.VISIBLE);
        }

        setPrimaryPhoneNumber(number);

        // Set the label (Mobile, Work, etc)
        setPrimaryLabel(label);

        showInternetCallLabel(isSipCall);

        setDrawableToImageView(mPhoto, photo);
               
        
    }
       

    @Override
    public void setSecondary(boolean show, String name, boolean nameIsNumber, String label,
            String providerLabel, Drawable providerIcon, boolean isConference,
            boolean canManageConference) {

        if (show != mSecondaryCallInfo.isShown()) {
            updateFabPositionForSecondaryCallInfo();
        }

        if (show) {
            boolean hasProvider = !TextUtils.isEmpty(providerLabel);
            showAndInitializeSecondaryCallInfo(hasProvider);

//            if (isConference) {
//                name = getConferenceString(canManageConference);
//                nameIsNumber = false;
//                mSecondaryCallConferenceCallIcon.setVisibility(View.VISIBLE);
//            } else {
//                mSecondaryCallConferenceCallIcon.setVisibility(View.GONE);
//            }

            mSecondaryCallName.setText(name);
            if (hasProvider) {
                mSecondaryCallProviderLabel.setText(providerLabel);
//                mSecondaryCallProviderIcon.setImageDrawable(providerIcon);
            }

            int nameDirection = View.TEXT_DIRECTION_INHERIT;
            if (nameIsNumber) {
                nameDirection = View.TEXT_DIRECTION_LTR;
            }
            mSecondaryCallName.setTextDirection(nameDirection);
        } else {
            mSecondaryCallInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public void setCallState(
            int state,
            int videoState,
            int sessionModificationState,
            DisconnectCause disconnectCause,
            String connectionLabel,
            Drawable connectionIcon,
            String gatewayNumber) {
        boolean isGatewayCall = !TextUtils.isEmpty(gatewayNumber);
        CharSequence callStateLabel = getCallStateLabelFromState(state, videoState,
                sessionModificationState, disconnectCause, connectionLabel, isGatewayCall);

        Log.v(this, "setCallState " + callStateLabel);
        Log.v(this, "DisconnectCause " + disconnectCause.toString());
        Log.v(this, "gateway " + connectionLabel + gatewayNumber);

        if (TextUtils.equals(callStateLabel, mCallStateLabel.getText())) {
            // Nothing to do if the labels are the same
            return;
        }

        // Update the call state label and icon.
        if (!TextUtils.isEmpty(callStateLabel) && !getPresenter().isRinging()) {
            mCallStateLabel.setText(callStateLabel);
            mCallStateLabel.setAlpha(1);
            mCallStateLabel.setVisibility(View.VISIBLE);

        } else {
            Animation callStateAnimation = mCallStateLabel.getAnimation();
            if (callStateAnimation != null) {
                callStateAnimation.cancel();
            }
            mCallStateLabel.setText(null);
            mCallStateLabel.setAlpha(0);
            mCallStateLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public void setCallbackNumber(String callbackNumber, boolean isEmergencyCall) {
        if (mInCallMessageLabel == null) {
            return;
        }

        if (TextUtils.isEmpty(callbackNumber)) {
            mInCallMessageLabel.setVisibility(View.GONE);
            return;
        }

        // TODO: The new Locale-specific methods don't seem to be working. Revisit this.
        callbackNumber = PhoneNumberUtils.formatNumber(callbackNumber);

        int stringResourceId = isEmergencyCall ? R.string.card_title_callback_number_emergency
                : R.string.card_title_callback_number;

        String text = getString(stringResourceId, callbackNumber);
        mInCallMessageLabel.setText(text);

        mInCallMessageLabel.setVisibility(View.VISIBLE);
    }

    private void showInternetCallLabel(boolean show) {
        if (show) {
            final String label = getView().getContext().getString(
                    R.string.incall_call_type_label_sip);
            mCallTypeLabel.setVisibility(View.VISIBLE);
            mCallTypeLabel.setText(label);
        } else {
            mCallTypeLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public void setPrimaryCallElapsedTime(boolean show, String callTimeElapsed) {
//        if (show) {
//            if (mElapsedTime.getVisibility() != View.VISIBLE) {
//                AnimUtils.fadeIn(mElapsedTime, AnimUtils.DEFAULT_DURATION);
//            }
//            mElapsedTime.setText(callTimeElapsed);
//        } else {
//            // hide() animation has no effect if it is already hidden.
//            AnimUtils.fadeOut(mElapsedTime, AnimUtils.DEFAULT_DURATION);
//        }
        mElapsedTime.setText(callTimeElapsed);
    	mElapsedTime.setVisibility(show ? View.VISIBLE: View.INVISIBLE);
    }

    private void setDrawableToImageView(ImageView view, Drawable photo) {
        if (photo == null) {
            photo = view.getResources().getDrawable(R.drawable.photo_default_outgoing);
            photo.setAutoMirrored(true);
        }
        BitmapDrawable bd = (BitmapDrawable) photo;
		Bitmap bitmap1 = bd.getBitmap();
		Bitmap bitmap2 = RoundBitmapUtils.toRoundBitmap(bitmap1);
		bd = new BitmapDrawable(view.getContext().getResources(), bitmap2);

        final Drawable current = view.getDrawable();
        //aurora change by liguangyu
        if(mElapsedTime.getVisibility() != View.VISIBLE) {
            if (current == null) {
                view.setImageDrawable(bd);
                AnimUtils.fadeIn(mElapsedTime, AnimUtils.DEFAULT_DURATION);
            } else {
                InCallAnimationUtils.startCrossFade(view, current, bd);
                view.setVisibility(View.VISIBLE);
            }
        } else {
        	 view.setImageDrawable(bd);
        }
    }

    private String getConferenceString(boolean canManageConference) {
        Log.v(this, "canManageConferenceString: " + canManageConference);
        final int resId = canManageConference
                ? R.string.card_title_conf_call : R.string.card_title_in_call;
        return getView().getResources().getString(resId);
    }

    private Drawable getConferencePhoto(boolean canManageConference) {
        Log.v(this, "canManageConferencePhoto: " + canManageConference);
        final int resId = canManageConference ? R.drawable.img_conference : R.drawable.img_phone;
        return getView().getResources().getDrawable(resId);
    }

    /**
     * Gets the call state label based on the state of the call or cause of disconnect.
     *
     * Additional labels are applied as follows:
     *         1. All outgoing calls with display "Calling via [Provider]".
     *         2. Ongoing calls will display the name of the provider.
     *         3. Incoming calls will only display "Incoming via..." for accounts.
     *         4. Video calls, and session modification states (eg. requesting video).
     */
    private CharSequence getCallStateLabelFromState(int state, int videoState,
            int sessionModificationState, DisconnectCause disconnectCause, String label,
            boolean isGatewayCall) {
        final Context context = getView().getContext();
        CharSequence callStateLabel = null;  // Label to display as part of the call banner

        boolean isSpecialCall = label != null;
        boolean isAccount = isSpecialCall && !isGatewayCall;

        switch  (state) {
            case Call.State.IDLE:
                // "Call state" is meaningless in this state.
                break;
            case Call.State.ACTIVE:
                // We normally don't show a "call state label" at all in this state
                // (but we can use the call state label to display the provider name).
                if (isAccount) {
                    callStateLabel = label;
                } else if (sessionModificationState
                        == Call.SessionModificationState.REQUEST_FAILED) {
                    callStateLabel = context.getString(R.string.card_title_video_call_error);
                } else if (sessionModificationState
                        == Call.SessionModificationState.WAITING_FOR_RESPONSE) {
                    callStateLabel = context.getString(R.string.card_title_video_call_requesting);
                } else if (VideoProfile.VideoState.isBidirectional(videoState)) {
                    callStateLabel = context.getString(R.string.card_title_video_call);
                }
                break;
            case Call.State.ONHOLD:
                callStateLabel = context.getString(R.string.card_title_on_hold);
                break;
            case Call.State.CONNECTING:
            case Call.State.DIALING:
                if (isSpecialCall) {
                    callStateLabel = context.getString(R.string.calling_via_template, label);
                } else {
                    callStateLabel = context.getString(R.string.card_title_dialing);
                }
                break;
            case Call.State.REDIALING:
                callStateLabel = context.getString(R.string.card_title_redialing);
                break;
            case Call.State.INCOMING:
            case Call.State.CALL_WAITING:
                if (isAccount) {
                    callStateLabel = context.getString(R.string.incoming_via_template, label);
                } else if (VideoProfile.VideoState.isBidirectional(videoState)) {
                    callStateLabel = context.getString(R.string.notification_incoming_video_call);
                } else {
                    callStateLabel = context.getString(R.string.card_title_incoming_call);
                }
                break;
            case Call.State.DISCONNECTING:
                // While in the DISCONNECTING state we display a "Hanging up"
                // message in order to make the UI feel more responsive.  (In
                // GSM it's normal to see a delay of a couple of seconds while
                // negotiating the disconnect with the network, so the "Hanging
                // up" state at least lets the user know that we're doing
                // something.  This state is currently not used with CDMA.)
                callStateLabel = context.getString(R.string.card_title_hanging_up);
                break;
            case Call.State.DISCONNECTED:
                callStateLabel = disconnectCause.getLabel();
                if (TextUtils.isEmpty(callStateLabel)) {
                    callStateLabel = context.getString(R.string.card_title_call_ended);
                }
                break;
            case Call.State.CONFERENCED:
                callStateLabel = context.getString(R.string.card_title_conf_call);
                break;
            default:
                Log.wtf(this, "updateCallStateWidgets: unexpected call: " + state);
        }
        return callStateLabel;
    }

    private void showAndInitializeSecondaryCallInfo(boolean hasProvider) {
        mSecondaryCallInfo.setVisibility(View.VISIBLE);

        // mSecondaryCallName is initialized here (vs. onViewCreated) because it is inaccessible
        // until mSecondaryCallInfo is inflated in the call above.
        if (mSecondaryCallName == null) {
            mSecondaryCallName = (TextView) getView().findViewById(R.id.secondaryCallName);
            mSecondaryCallPhoto = (ImageView) getView().findViewById(R.id.secondaryCallPhoto);
            mSimIcon2 = (ImageView) getView().findViewById(R.id.aurora_sim_slot_2);
//            mSecondaryCallConferenceCallIcon =
//                    getView().findViewById(R.id.secondaryCallConferenceCallIcon);
//            if (hasProvider) {
//                mSecondaryCallProviderInfo.setVisibility(View.VISIBLE);
//                mSecondaryCallProviderLabel = (TextView) getView()
//                        .findViewById(R.id.secondaryCallProviderLabel);
////                mSecondaryCallProviderIcon = (ImageView) getView()
////                        .findViewById(R.id.secondaryCallProviderIcon);
//            }
        }
    }

    public void dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            dispatchPopulateAccessibilityEvent(event, mCallStateLabel);
            dispatchPopulateAccessibilityEvent(event, mPrimaryName);
            dispatchPopulateAccessibilityEvent(event, mPhoneNumber);
            return;
        }
        dispatchPopulateAccessibilityEvent(event, mCallStateLabel);
        dispatchPopulateAccessibilityEvent(event, mPrimaryName);
        dispatchPopulateAccessibilityEvent(event, mPhoneNumber);
        dispatchPopulateAccessibilityEvent(event, mCallTypeLabel);
        dispatchPopulateAccessibilityEvent(event, mSecondaryCallName);
        dispatchPopulateAccessibilityEvent(event, mSecondaryCallProviderLabel);

        return;
    }


    /**
     * Changes the visibility of the contact photo.
     *
     * @param isVisible {@code True} if the UI should show the contact photo.
     */
    @Override
    public void setPhotoVisible(boolean isVisible) {
        mPhoto.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Changes the visibility of the "manage conference call" button.
     *
     * @param visible Whether to set the button to be visible or not.
     */
    @Override
    public void showManageConferenceCallButton(boolean visible) {
        mManageConferenceCallButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void dispatchPopulateAccessibilityEvent(AccessibilityEvent event, View view) {
        if (view == null) return;
        final List<CharSequence> eventText = event.getText();
        int size = eventText.size();
        view.dispatchPopulateAccessibilityEvent(event);
        // if no text added write null to keep relative position
        if (size == eventText.size()) {
            eventText.add(null);
        }
    }

    public void animateForNewOutgoingCall(final Point touchPoint, final boolean showCircularReveal) {}

    public void onDialpadVisiblityChange(boolean isShown) {
        mIsDialpadShowing = isShown;
    }


    @Override
    public void onResume() {
        super.onResume();
        // If the previous launch animation is still running, cancel it so that we don't get
        // stuck in an intermediate animation state.
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }

    }

    /**
     * Adds a global layout listener to update the FAB's positioning on the next layout. This allows
     * us to position the FAB after the secondary call info's height has been calculated.
     */
    private void updateFabPositionForSecondaryCallInfo() {
        mSecondaryCallInfo.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        final ViewTreeObserver observer = mSecondaryCallInfo.getViewTreeObserver();
                        if (!observer.isAlive()) {
                            return;
                        }
                        observer.removeOnGlobalLayoutListener(this);

                        onDialpadVisiblityChange(mIsDialpadShowing);
                    }
                });
    }


    private final class LayoutIgnoringListener implements View.OnLayoutChangeListener {
        @Override
        public void onLayoutChange(View v,
                int left,
                int top,
                int right,
                int bottom,
                int oldLeft,
                int oldTop,
                int oldRight,
                int oldBottom) {
            v.setLeft(oldLeft);
            v.setRight(oldRight);
            v.setTop(oldTop);
            v.setBottom(oldBottom);
        }
    }
   	  
	
	View getPrimaryView(){
		return mPrimaryCallCardContainer;
	}
	
	public void updateRingingCallCardSize() {
	      Call primary = CallList.getInstance().getIncomingCall();
	      mAnimController.updateRingingCall(primary);    
	}
	
	private void updateArea(String area) {
	      Log.d(this, "updateArea area = " + area);
        if(area == null || area.length() == 0) {    
        	mAdrress.setVisibility(View.GONE);
        	mPhoneNumber.setPadding(0, 0, 0, 0);
        } else {
            mCallNumberAndLabel.setVisibility(View.VISIBLE);
        	mAdrress.setVisibility(View.VISIBLE);
        	mAdrress.setText(area);
        	int right= getResources().getDimensionPixelSize(R.dimen.call_number_address_distance);
        	mPhoneNumber.setPadding(0, 0, right, 0);
        }
	}
	
	private void updateNote(String note) {	
        if(TextUtils.isEmpty(note) || !getPresenter().isRinging()) {    
        	mNoteLine.setVisibility(View.GONE);
        	mNote.setText("");
        } else {
            mNoteLine.setVisibility(View.VISIBLE);
            mNote.setVisibility(View.VISIBLE);
        	mNote.setText(note);
        }
         
	}
	
	private void updateMark(String mark, String markcount) {                            
		if (!TextUtils.isEmpty(mark)) {
			if (TextUtils.isEmpty(mark) || !getPresenter().isRinging()) {
				mSogouLine.setVisibility(View.GONE);
				mSogouNote.setText("");
			} else {
				mSogouLine.setVisibility(View.VISIBLE);
				String finalNote = mark + " |";
				if (!TextUtils.isEmpty(markcount)) {
					if (Integer.valueOf(markcount) > 0) {
						finalNote += " " + markcount + getString(R.string.aurora_sogou_mark);
					}
					mSogouIcon.setVisibility(View.VISIBLE);
				} else {
					finalNote += getString(
							R.string.aurora_sogou_local_mark);
					mSogouIcon.setVisibility(View.GONE);
				}
				mSogouNote.setText(finalNote);
			}
		} else {
			mSogouLine.setVisibility(View.GONE);
		}
	}
	
	private void updateSlogan(String slogan) {	 		        
	    if(YuLoreUtils.isInit()) {
	        Log.d(this, "updateSlogan");
            mSloganNote.setText(slogan);
            if(!TextUtils.isEmpty(slogan)) {
            	mSloganNote.setVisibility(View.VISIBLE);
            } else {
                mSloganNote.setVisibility(View.INVISIBLE);
            }
            mPhotoV.setVisibility(YuLoreUtils.getIsV() && (getPresenter().isRinging() || getPresenter().isDialing()) ? View.VISIBLE : View.INVISIBLE);
	    }
	}
	
	public void updataSimIcon(int slot) {
     	if(TelephonyManager.getDefault().isMultiSimEnabled()) {
       		mSimIcon.setVisibility(View.VISIBLE);
	        mSimIcon.setImageResource(SimIconUtils.getSmallSimIcon(slot));
       	} else {
    		mSimIcon.setVisibility(View.GONE);
       	}
	}
	
	 public void setAuroraPrimary(String area,String note, String mark, String markcount, String slogan) {
	    	updateArea(area);
	    	updateNote(note);
	    	updateMark(mark, markcount);
	    	updateSlogan(slogan);
	    }
	
	public void hideViewsForPrivate() {
        Log.d(this, "hideViewsForPrivate");
        
        mNoteLine.setVisibility(View.GONE);
        mSogouLine.setVisibility(View.GONE);
        mSloganNote.setVisibility(View.INVISIBLE);
		mPhotoV.setVisibility(View.INVISIBLE);
        mPhoneNumber.setVisibility(View.GONE);
        mNumberLabel.setVisibility(View.GONE);
        mAdrress.setVisibility(View.GONE);        
	}
	
    public void updateDimEffectForSecondaryCallInfo(boolean dim) {
//        View view = getView().findViewById(R.id.dim_effect_for_secondary);
//        if (dim) {
//            mSecondaryCallInfo.setEnabled(false);
//            view.setVisibility(View.VISIBLE);
//        } else {
//            mSecondaryCallInfo.setEnabled(true);
//            view.setVisibility(View.GONE);
//        }
    }
    
    public void onDestoryView(){
    	super.onDestroyView();
    	mAnimController.setView(null);
    }
    
    public void onDetach() {
    	super.onDetach();

    }
    
    public void setAuroraSecondary(boolean show, String name, boolean nameIsNumber, String label,
            int simicon, Drawable photo, boolean isConference,
            boolean canManageConference){
    	  if (show != mSecondaryCallInfo.isShown()) {
              updateFabPositionForSecondaryCallInfo();
          }

          if (show) {
              showAndInitializeSecondaryCallInfo(false);

              mSecondaryCallName.setText(name);

              int nameDirection = View.TEXT_DIRECTION_INHERIT;
              if (nameIsNumber) {
                  nameDirection = View.TEXT_DIRECTION_LTR;
              }
              mSecondaryCallName.setTextDirection(nameDirection);
              if(photo != null) {
	              BitmapDrawable bd = (BitmapDrawable) photo;
		      		Bitmap bitmap1 = bd.getBitmap();
		      		Bitmap bitmap2 = RoundBitmapUtils.toRoundBitmap(bitmap1);
		      		bd = new BitmapDrawable(mSecondaryCallPhoto.getContext().getResources(), bitmap2);
	              mSecondaryCallPhoto.setImageDrawable(bd);
              } else {
            	  mSecondaryCallPhoto.setImageDrawable(photo);
              }
              mSimIcon2.setImageResource(simicon);
          } else {
              mSecondaryCallInfo.setVisibility(View.GONE);
          }
    }
}
