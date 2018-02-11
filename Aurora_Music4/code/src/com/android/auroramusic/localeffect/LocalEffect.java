package com.android.auroramusic.localeffect;



import java.util.UUID;

import com.android.auroramusic.util.LogUtil;

import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.AudioEffect.Descriptor;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;



public class LocalEffect {
	
	private static final String TAG = "LocalEffect";
	private static LocalEffect mInstance = null;
	private static Context mContext = null;
	
	private int mAudioSession = AudioEffect.ERROR_BAD_VALUE;
	private String mCallingPackageName = "aurora_empty";
	private boolean mVirtualizerSupported;
    private boolean mVirtualizerIsHeadphoneOnly;
    private String[] mEQPresetNames;
    private int mEQPresetUserPos = 1;
    private int mEQPreset;
    private int mEQPresetPrevious;
    
    private int mPRPreset;
    private int mPRPresetPrevious;
    
    private int mCurIndex = -1;
    private boolean binitVirt = false;
    private boolean binitBoost = false;
    
    private final static int EQUALIZER_MAX_BANDS = 32;
    private int mNumberEqualizerBands;
    private int mEqualizerMinBandLevel;
    private int[] mEQPresetUserBandLevelsPrev;
    
    private boolean mbLow = false;
    
    /**
     * Indicates if BassBoost effect is supported.
     */
    private boolean mBassBoostSupported;
    /**
     * Indicates if Equalizer effect is supported.
     */
    private boolean mEqualizerSupported;
    /**
     * Indicates if Preset Reverb effect is supported.
     */
    private boolean mPresetReverbSupported;
	
	
	private LocalEffect(Context context, int audioSession) {
        super();
        
        //Log.i(TAG, "zll ----- LocalEffect context:"+context);
        mContext = context.getApplicationContext();
        
        mAudioSession = audioSession;
        //init();
    }
	
	synchronized public static LocalEffect getInstance(Context context, int audioSession){
        if(mInstance == null){
        	mInstance = new LocalEffect(context, audioSession);
        }
        return mInstance;
    }

	public void init() {
        //Log.i(TAG, "zll init audio session: " + mAudioSession);
        
        mCallingPackageName = mContext.getPackageName();
        //Log.i(TAG, "zll -" + mCallingPackageName + " (" + mAudioSession + ")");
        ControlPanelEffect.initEffectsPreferences(mContext, mCallingPackageName, mAudioSession);
        
        // query available effects
        final Descriptor[] effects = AudioEffect.queryEffects();

        // Determine available/supported effects
        LogUtil.d(TAG, "---------------zll Available effects:"+effects.length);
        for (final Descriptor effect : effects) {
            LogUtil.d(TAG, "------------" + effect.name.toString() + ", type: " + effect.type.toString());

            if (effect.type.equals(AudioEffect.EFFECT_TYPE_VIRTUALIZER)) {
                mVirtualizerSupported = true;
                if (effect.uuid.equals(UUID.fromString("1d4033c0-8557-11df-9f2d-0002a5d5c51b"))) {
                    mVirtualizerIsHeadphoneOnly = true;
                }
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_BASS_BOOST)) {
                mBassBoostSupported = true;
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_EQUALIZER)) {
                mEqualizerSupported = true;
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_PRESET_REVERB)) {
                mPresetReverbSupported = true;
            }
        }
        LogUtil.d(TAG, "---mVirtualizerSupported:"+mVirtualizerSupported+" mVirtualizerIsHeadphoneOnly:"+mVirtualizerIsHeadphoneOnly
        		+" mBassBoostSupported:"+mBassBoostSupported+" mEqualizerSupported:"+mEqualizerSupported+" mPresetReverbSupported:"+mPresetReverbSupported);
        final int numPresets = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_presets);
        mEQPresetNames = new String[numPresets];
        for (short i = 0; i < numPresets; i++) {
            mEQPresetNames[i] = ControlPanelEffect.getParameterString(mContext,
                    mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_preset_name, i);
            LogUtil.i(TAG, "zll ---- yyyy 1 i:"+i+",mEQPresetNames[i]:"+mEQPresetNames[i]);
        }
        /*mEQPresetNames[numPresets] = getString(R.string.ci_extreme);
        mEQPresetNames[numPresets + 1] = getString(R.string.user);*/
        mEQPresetUserPos = numPresets + 1;
        
        binitVirt = false;
        binitBoost = false;
        mbLow = false;
        
        if ((mVirtualizerSupported) || (mBassBoostSupported) || (mEqualizerSupported)
             || (mPresetReverbSupported)) {
			
        	final boolean isEnabled = ControlPanelEffect.getParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.global_enabled);
        	LogUtil.d(TAG, "--------------------isEnabled:"+isEnabled);
        	if (mVirtualizerSupported && isEnabled) {
        		initVirt();
			}
        	
        	if (mBassBoostSupported && isEnabled) {
        		initBoost();
			}
        	
        	if (mEqualizerSupported) {
                mEQPreset = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                        mAudioSession, ControlPanelEffect.Key.eq_current_preset);
                LogUtil.d(TAG, "mEQPreset:"+mEQPreset+" mEQPresetNames.length:"+mEQPresetNames.length);
                if (mEQPreset >= mEQPresetNames.length) {
                    mEQPreset = 8;
                }
                LogUtil.i(TAG, "zll ---- init mEQPresetPrevious:"+mEQPresetPrevious+",mEQPreset:"+mEQPreset+",mEQPresetNames:"+mEQPresetNames[mEQPreset]);
                mEQPresetPrevious = mEQPreset;
                mCurIndex = mEQPresetPrevious;
                //equalizerSetPreset(mEQPresetUserPos);
                //equalizerSpinnerInit();
                equalizerBandsInit();
            }
        	
        	if (mPresetReverbSupported) {
                mPRPreset = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                        mAudioSession, ControlPanelEffect.Key.pr_current_preset);
                mPRPresetPrevious = mPRPreset;
                //reverbSpinnerInit();
            }
        	
		} else {
			
		}
        
		return;
	}
	
	public String [] getEffectStr(){
		if (mEQPresetNames == null || 
			(mEQPresetNames != null && mEQPresetNames.length == 0)) {
			return null;
		}
		
		return mEQPresetNames;
	}
	
	private void initVirt() {
		LogUtil.d(TAG, "zll ----- initVirt binitVirt:"+binitVirt);
		if (binitVirt) {
			return;
		}
		
		boolean hasvirt = ControlPanelEffect.getParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.virt_enabled);
		if (!hasvirt) {
			ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.virt_enabled, true);
		}
		
		ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.virt_strength, 100);
		
		binitVirt = true;
		return;
	}
	
	private void initBoost() {
		LogUtil.d(TAG, "zll ----- initBoost binitBoost:"+binitBoost);
		if (binitBoost) {
			return;
		}
		
		boolean hasBass = ControlPanelEffect.getParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.bb_enabled);
		if (!hasBass) {
			ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.bb_enabled, true);
		}
		
		ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.bb_strength, 100);
		binitBoost = true;
		return;
	}
	
	/**
     * Initializes the equalizer elements. Set the SeekBars and Spinner listeners.
     */
    private void equalizerBandsInit() {
        // Initialize the N-Band Equalizer elements.
        mNumberEqualizerBands = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_bands);
        mEQPresetUserBandLevelsPrev = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_preset_user_band_level);
        final int[] centerFreqs = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_center_freq);
        final int[] bandLevelRange = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_level_range);
        mEqualizerMinBandLevel = bandLevelRange[0];
        final int mEqualizerMaxBandLevel = bandLevelRange[1];

        //Log.i(TAG, "zll ---- equalizerBandsInit mNumberEqualizerBands:"+mNumberEqualizerBands+",mEQPresetUserBandLevelsPrev:"+mEQPresetUserBandLevelsPrev+",mEqualizerMinBandLevel:"+mEqualizerMinBandLevel);
        for (int band = 0; band < mNumberEqualizerBands; band++) {
            // Unit conversion from mHz to Hz and use k prefix if necessary to display
            final int centerFreq = centerFreqs[band] / 1000;
            float centerFreqHz = centerFreq;
            String unitPrefix = "";
            if (centerFreqHz >= 1000) {
                centerFreqHz = centerFreqHz / 1000;
                unitPrefix = "k";
            }
        }

        // Hide the inactive Equalizer bands.
        /*for (int band = mNumberEqualizerBands; band < EQUALIZER_MAX_BANDS; band++) {
        }*/

        equalizerUpdateDisplay();
    }
	
	private void equalizerSetPreset(final int preset) {
    	//Log.i(TAG, "zll ----- equalizerSetPreset ");
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,ControlPanelEffect.Key.eq_current_preset, preset);
        equalizerUpdateDisplay();
    }
	
	private void equalizerUpdateDisplay() {
        final int[] bandLevels = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_band_level);
        
        final int[] bandLevelRange = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_level_range);
        mEqualizerMinBandLevel = bandLevelRange[0];
        boolean isEnabled = ControlPanelEffect.getParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.global_enabled);
        //Log.i(TAG, "zll --- equalizerUpdateDisplay 1 bandLevels length:"+bandLevels.length+",mNumberEqualizerBands:"+mNumberEqualizerBands+",mEqualizerMinBandLevel:"+mEqualizerMinBandLevel);
        for (short band = 0; band < mNumberEqualizerBands; band++) {
            final int level = bandLevels[band];
            final int progress = level - mEqualizerMinBandLevel;
            
            LogUtil.d(TAG, "zll --- equalizerUpdateDisplay 2 bandLevels band:"+band+",level:"+level+",progress:"+progress+" isEnabled:"+isEnabled);
        }
    }
	
	public String getCurrentIndexEffects() {
		if (mEQPresetPrevious < 0 || mEQPresetPrevious >= mEQPresetNames.length) {
			return mEQPresetNames[0];
		}
		
		final boolean isEnabled = ControlPanelEffect.getParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.global_enabled);
		//LogUtil.i(TAG, "zll ---- getIndexByInit mEQPresetPrevious:"+mEQPresetPrevious+",mEQPreset:"+mEQPreset+",isEnabled:"+isEnabled);
		if (!isEnabled) {
			return "default";
		}
		
		return mEQPresetNames[mEQPresetPrevious];
	}
	
	public void setLocalEffects(int index) {
	//	LogUtil.i(TAG, "zll ----- setLocalEffects index:"+index+",mCurIndex:"+mCurIndex);
		if (mCurIndex == index) {
			return;
		}
		
		/*if (index >= 0) {
			Log.i(TAG, "zll ----- setLocalEffects index:"+index+",mEQPresetNames[index]:"+mEQPresetNames[index]);
		}*/
		
		if (index < 0) {
			mCurIndex = index;
			ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.global_enabled, false);
			return;
		} else {
			final boolean isEnabled = ControlPanelEffect.getParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.global_enabled);
			LogUtil.d(TAG, "-------start---setLocalEffects isEnabled:"+isEnabled);
			if (!isEnabled) {
				ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.global_enabled, true);
			}
			LogUtil.d(TAG, "-------end---setLocalEffects isEnabled:"+isEnabled);
		}
		
		initVirt();
		initBoost();
		
		equalizerSetPreset(index);
		if (mEQPresetNames[index].equalsIgnoreCase("normal")) {
			mbLow = true;
			ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.virt_strength, 0);
			ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.bb_strength, 1000);
		} else if (mEQPresetNames[index].equalsIgnoreCase("Hip Hop")) {
			ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.virt_strength, 0);
			ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.bb_strength, 100);
			mbLow = true;
		} else if (mEQPresetNames[index].equalsIgnoreCase("Classical")) {
			ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.virt_strength, 1000);
			ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.bb_strength, 100);
			mbLow = true;
		} else if (mEQPresetNames[index].equalsIgnoreCase("Dance")) {
			ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.virt_strength, 1000);
			ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.bb_strength, 100);
			mbLow = true;
		} else {
			if (mbLow) {
				ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.virt_strength, 100);
				ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession, ControlPanelEffect.Key.bb_strength, 100);
			}
			mbLow = false;
		}

		mCurIndex = index;
		mEQPresetPrevious = index;
	}
	
}
