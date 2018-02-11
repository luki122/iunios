package gn.com.android.audioprofile;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.sax.StartElementListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import aurora.preference.*;
import aurora.app.AuroraActivity;

import com.aurora.R;
import com.aurora.R;
public class AuroraRingtonePickerPreference extends AuroraRingtonePreference {
	
	private int mRingtoneType;
    private boolean mShowDefault;
    private boolean mShowSilent;
    private  AuroraActivity mActivity;
    
//    private int mRequestCode;
	
	public AuroraRingtonePickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(com.aurora.R.layout.aurora_preference_widget_arrow);
        mActivity = (AuroraActivity)context;
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.RingtonePreference, defStyle, 0);
//        mRingtoneType = a.getInt(com.android.internal.R.styleable.RingtonePreference_ringtoneType,
//                RingtoneManager.TYPE_RINGTONE);
        mShowDefault = a.getBoolean(com.android.internal.R.styleable.RingtonePreference_showDefault,
                true);
        mShowSilent = a.getBoolean(com.android.internal.R.styleable.RingtonePreference_showSilent,
                true);
        a.recycle();
	}
	
    public AuroraRingtonePickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.ringtonePreferenceStyle);
    }
    
    public AuroraRingtonePickerPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onClick() {
        // Launch the ringtone picker
        Intent intent = new Intent(AuroraRingPickerActivity.ACTION_RINGTONE_PICKER);
        onPrepareRingtonePickerIntent(intent);
//        AuroraPreferenceFragment owningFragment = getPreferenceManager().getFragment();
//        if (owningFragment != null) {
//            owningFragment.startActivityForResult(intent, mRequestCode);
//        } else {
//            getPreferenceManager().getActivity().startActivityForResult(intent, mRequestCode);
//        }
//        mActivity.startActivity(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("native_start", true);
        getContext().startActivity(intent);
        mActivity.overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,com.aurora.R.anim.aurora_activity_open_exit);
        mActivity.finish();
        
       
    }
    
  



    /**
     * Returns the sound type(s) that are shown in the picker.
     * 
     * @return The sound type(s) that are shown in the picker.
     * @see #setRingtoneType(int)
     */
    public int getRingtoneType() {
        return mRingtoneType;
    }

    /**
     * Sets the sound type(s) that are shown in the picker.
     * 
     * @param type The sound type(s) that are shown in the picker.
     * @see RingtoneManager#EXTRA_RINGTONE_TYPE
     */
    public void setRingtoneType(int type) {
        mRingtoneType = type;
    }
//
//    /**
//     * Returns whether to a show an item for the default sound/ringtone.
//     * 
//     * @return Whether to show an item for the default sound/ringtone.
//     */
//    public boolean getShowDefault() {
//        return mShowDefault;
//    }
//
//    /**
//     * Sets whether to show an item for the default sound/ringtone. The default
//     * to use will be deduced from the sound type(s) being shown.
//     * 
//     * @param showDefault Whether to show the default or not.
//     * @see RingtoneManager#EXTRA_RINGTONE_SHOW_DEFAULT
//     */
//    public void setShowDefault(boolean showDefault) {
//        mShowDefault = showDefault;
//    }
//
//    /**
//     * Returns whether to a show an item for 'Silent'.
//     * 
//     * @return Whether to show an item for 'Silent'.
//     */
//    public boolean getShowSilent() {
//        return mShowSilent;
//    }
//
//    /**
//     * Sets whether to show an item for 'Silent'.
//     * 
//     * @param showSilent Whether to show 'Silent'.
//     * @see RingtoneManager#EXTRA_RINGTONE_SHOW_SILENT
//     */
//    public void setShowSilent(boolean showSilent) {
//        mShowSilent = showSilent;
//    }
//
//    @Override
//    protected void onClick() {
//        // Launch the ringtone picker
//        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
//        onPrepareRingtonePickerIntent(intent);
//        PreferenceFragment owningFragment = getPreferenceManager().getFragment();
//        if (owningFragment != null) {
//            owningFragment.startActivityForResult(intent, mRequestCode);
//        } else {
//            getPreferenceManager().getActivity().startActivityForResult(intent, mRequestCode);
//            
//        }
//        
//    }
//
    /**
     * Prepares the intent to launch the ringtone picker. This can be modified
     * to adjust the parameters of the ringtone picker.
     * 
     * @param ringtonePickerIntent The ringtone picker intent that can be
     *            modified by putting extras.
     */
    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {

        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                onRestoreRingtone());
        
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, mShowDefault);
        if (mShowDefault) {
            ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    RingtoneManager.getDefaultUri(getRingtoneType()));
        }

        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false); // mShowSilent
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, mRingtoneType);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getTitle());
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
    }
    
    @Override
     protected void onSaveRingtone(Uri ringtoneUri) {
    	Log.i("qy", "onSaveRingtone(Uri ringtoneUri)");
        RingtoneManager.setActualDefaultRingtoneUri(getContext(), mRingtoneType, ringtoneUri); //getRingtoneType()
      }
    
      @Override
      protected Uri onRestoreRingtone() {
    	  Log.i("qy", "onRestoreRingtone()");
         return RingtoneManager.getActualDefaultRingtoneUri(getContext(), mRingtoneType);
       }
    
    
    
    
//    /**
//     * Called when a ringtone is chosen.
//     * <p>
//     * By default, this saves the ringtone URI to the persistent storage as a
//     * string.
//     * 
//     * @param ringtoneUri The chosen ringtone's {@link Uri}. Can be null.
//     */
//    protected void onSaveRingtone(Uri ringtoneUri) {
//        persistString(ringtoneUri != null ? ringtoneUri.toString() : "");
//    }
//
//    /**
//     * Called when the chooser is about to be shown and the current ringtone
//     * should be marked. Can return null to not mark any ringtone.
//     * <p>
//     * By default, this restores the previous ringtone URI from the persistent
//     * storage.
//     * 
//     * @return The ringtone to be marked as the current ringtone.
//     */
//    protected Uri onRestoreRingtone() {
//        final String uriString = getPersistedString(null);
//        return !TextUtils.isEmpty(uriString) ? Uri.parse(uriString) : null;
//    }
//    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        String defaultValue = (String) defaultValueObj;
        
        /*
         * This method is normally to make sure the internal state and UI
         * matches either the persisted value or the default value. Since we
         * don't show the current value in the UI (until the dialog is opened)
         * and we don't keep local state, if we are restoring the persisted
         * value we don't need to do anything.
         */
        if (restorePersistedValue) {
            return;
        }
        
        // If we are setting to the default value, we should persist it.
        if (!TextUtils.isEmpty(defaultValue)) {
            onSaveRingtone(Uri.parse(defaultValue));
        }
    }
//
//    @Override
//    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
//        super.onAttachedToHierarchy(preferenceManager);
//        
//        preferenceManager.registerOnActivityResultListener(this);
//        mRequestCode = preferenceManager.getNextRequestCode();
//    }
//
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("qy", resultCode+"");
        if (-1 == resultCode) {
            
//            if (data != null) {
//                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
//                
//                Log.i("qy", "data != null");
//                if (callChangeListener(uri != null ? uri.toString() : "")) {
//                    onSaveRingtone(uri);
//                }
//            }
            
            return true;
        }
        
        return false;
    }
}