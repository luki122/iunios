package com.android.auroramusic.dts;

import com.android.auroramusic.ui.AuroraSoundControl;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.music.MusicUtils;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class DtsEffects {

	private static final String TAG = "DtsEffects";
	private static DtsEffects instance = null;
	private static AudioManager mAudioManager;
	private static Context mContext = null;
	public static final int DtsEffectsMaxVolume = 14;
	
	private double[][] generalHeadphoneParam;
	private String headphoneCommonParams[];
	
	/**
	 * U3m参数 8905
	 */
	private double[][] generalHeadphoneParamU3m = new double[][] {
			// center, focus, definition, trubass, space, compressor
			{ 0.500, 0.000, 0.180, 0.200, 0.500, 0.100 }, // 无-通用

			{ 0.500, 0.100, 0.300, 0.400, 0.500, 0.100 }, // IUNI摇滚动力耳机
															// center/focus/definition/trubass/space/compressor
			{ 0.500, 0.100, 0.340, 0.120, 0.500, 0.050 }, // IUNI扭音耳机
			{ 0.500, 0.160, 0.300, 0.200, 0.500, 0.050 }, // IUNI降噪耳机

			{ 0.500, 0.150, 0.320, 0.520, 0.500, 0.220 }, // Iphone Earpods耳机
			{ 0.500, 0.150, 0.320, 0.180, 0.500, 0.100 }, // AKG 450耳机
			{ 0.500, 0.150, 0.320, 0.150, 0.500, 0.050 } // urBeats耳机
	};

	/**
	 * U3m参数 8905
	 */
	// 针对通用耳塞和头戴耳机的参数
  private String headphoneCommonParamsU3m[] = new String[]{
	  "srs_cfg:trumedia_preset=0", "srs_cfg:trumedia_igain_ext=0.800",
	  "srs_mus_ext:wowhd_igain=0.500", "srs_mus_ext:wowhd_trubass_freq=70",
	  "srs_mus_ext:wowhd_trubass_analysis=50",
	  "srs_mus_ext:wowhd_srs_enable=0",
	  
	  "srs_spk_ext:trueq_left_enable=1", // TruEQ Left Toggle - toggle
	  "srs_spk_ext:trueq_lband0_enable=1", //TruEQ Left Band 0 Toggle - toggle
	  "srs_spk_ext:trueq_lband1_enable=1", //TruEQ Left Band 1 Toggle - toggle
	  "srs_spk_ext:trueq_lband2_enable=1", //TruEQ Left Band 2 Toggle - toggle
	  "srs_spk_ext:trueq_lband3_enable=1", //TruEQ Left Band 3 Toggle - toggle
	  "srs_spk_ext:trueq_right_enable=1", //TruEQ Right Toggle - toggle
	  "srs_spk_ext:trueq_rband0_enable=1", /// TruEQ Right Band 0 Toggle -toggle 
	  "srs_spk_ext:trueq_rband1_enable=1", // TruEQ Right Band 1 Toggle- toggle 
	  "srs_spk_ext:trueq_rband2_enable=1", // TruEQ Right Band 2 Toggle - toggle
	  "srs_spk_ext:trueq_rband3_enable=1", // TruEQ Right Band3 Toggle - toggle
	  "srs_spk_ext:trueq_lband0=100.000000,8.000000,2.000000", // TruEQ Left Band 0 Tuning (CF Gain Q) -
	  "srs_spk_ext:trueq_lband1=1200.000000,10.000000,1.200000", // TruEQ LeftBand 1 Tuning -
	  "srs_spk_ext:trueq_lband2=5200.000000,-3.000000,2.200000", // TruEQ Left Band 2 Tuning -
	  "srs_spk_ext:trueq_lband3=12000.000000,3.000000,2.000000", // TruEQ LeftBand 3 Tuning - 
	  "srs_spk_ext:trueq_rband0=100.000000,8.000000,2.000000",
	  // TruEQ Right Band 0 Tuning -
	  "srs_spk_ext:trueq_rband1=1200.000000,10.000000,1.200000", // TruEQ Right Band 1 Tuning -
	  "srs_spk_ext:trueq_rband2=5200.000000,-3.000000,2.200000 ", // TruEQ Right Band 2 Tuning -
	  "srs_spk_ext:trueq_rband3=12000.000000,3.000000,2.000000 ", // TruEQRight Band 3 Tuning - 
	  "srs_spk_ext:trueq_skip=0", // Skips TruEQ when true – toggle 
	  "srs_limit_ext:hlimit_boost=2.500" 
	  };
	 
	
	/**
	 * N1 参数
	 */
	private double[][] generalHeadphoneParamN1 = new double[][] {
			// center, focus, definition, trubass, space, compressor
			{ 0.500, 0.100, 0.320, 0.460, 0.500, 0.200 }, // 无-通用

			{ 0.500, 0.100, 0.300, 0.400, 0.500, 0.100 }, // IUNI摇滚动力耳机
															// center/focus/definition/trubass/space/compressor
			{ 0.500, 0.100, 0.340, 0.120, 0.500, 0.050 }, // IUNI扭音耳机
			{ 0.500, 0.100, 0.300, 0.200, 0.500, 0.050 }, // IUNI降噪耳机

			{ 0.500, 0.030, 0.320, 0.520, 0.500, 0.200 }, // Iphone Earpods耳机
			{ 0.500, 0.150, 0.320, 0.180, 0.500, 0.100 }, // AKG 450耳机
			{ 0.500, 0.100, 0.320, 0.150, 0.500, 0.050 } // urBeats耳机
	};

	/**
	 * N1 参数
	 */
	// 针对通用耳塞和头戴耳机的参数
	private String headphoneCommonParamsN1[] = new String[] {
			"srs_cfg:trumedia_preset=0",
			"srs_cfg:trumedia_igain_ext=1.000",
			"srs_mus_ext:wowhd_igain=0.500",
			"srs_mus_ext:wowhd_trubass_freq=70",
			"srs_mus_ext:wowhd_trubass_analysis=50",
			"srs_mus_ext:wowhd_srs_enable=0",

			// ********For Android5.0 setting
			// Start**************************************************
			"srs_spk_ext:aeq_igain=0.8",
			"srs_spk_ext:aeq_lband0=10.000000,0.000000,2.000000",
			"srs_spk_ext:aeq_lband1=50.000000,8.000000,2.000000",
			"srs_spk_ext:aeq_lband2=100.000000,-2.000000,2.000000",
			"srs_spk_ext:aeq_lband3=250.000000,0.000000,2.000000",
			"srs_spk_ext:aeq_lband4=600.000000,4.000000,2.000000",
			"srs_spk_ext:aeq_lband5=1200.000000,8.000000,2.000000",
			"srs_spk_ext:aeq_lband6=2000.000000,0.000000,2.000000",
			"srs_spk_ext:aeq_lband7=3200.000000,-3.000000,2.000000",
			"srs_spk_ext:aeq_lband8=4000.000000,1.000000,2.000000",
			"srs_spk_ext:aeq_lband9=5600.000000,-4.000000,2.500000",
			"srs_spk_ext:aeq_lband10=8000.000000,-1.000000,2.000000",
			"srs_spk_ext:aeq_lband11=10000.000000,-3.000000,2.000000",
			"srs_spk_ext:aeq_rband0=10.000000,0.000000,2.000000",
			"srs_spk_ext:aeq_rband1=50.000000,8.000000,2.000000",
			"srs_spk_ext:aeq_rband2=100.000000,-2.000000,2.000000",
			"srs_spk_ext:aeq_rband3=250.000000,0.000000,2.000000",
			"srs_spk_ext:aeq_rband4=600.000000,4.000000,2.000000",
			"srs_spk_ext:aeq_rband5=1200.000000,8.000000,2.000000",
			"srs_spk_ext:aeq_rband6=2000.000000,0.000000,2.000000",
			"srs_spk_ext:aeq_rband7=3200.000000,-3.000000,2.000000",
			"srs_spk_ext:aeq_rband8=4000.000000,1.000000,2.000000",
			"srs_spk_ext:aeq_rband9=5600.000000,-4.000000,2.500000",
			"srs_spk_ext:aeq_rband10=8000.000000,-1.000000,2.000000",
			"srs_spk_ext:aeq_rband11=10000.000000,-3.000000,2.000000",
			"srs_limit_ext:hlimit_boost=2.200",

			// *********For Android5.0 setting End
			// **************************************************
			// External Speaker HPF
			"srs_hpf_ext:hipass_order=4", "srs_hpf_ext:hipass_frequency=60",
			"srs_hpf_ext:hipass_is32bit=1", "srs_hpf_ext:hipass_skip=0",

	};

	public static enum HeadphoneType {
		HEADPHONE_IN_EAR, HEADPHONE_PISTON, HEADPHONE_HEADSET
	}

	private DtsEffects(Context context) {
		super();
		mContext = context.getApplicationContext();
		mAudioManager = (AudioManager) mContext.getApplicationContext()
				.getSystemService(Context.AUDIO_SERVICE);
		if(Globals.currentMode.equalsIgnoreCase("IUNI i1")||Globals.currentMode.equalsIgnoreCase("GiONEE i1")){
			headphoneCommonParams=headphoneCommonParamsU3m;
			generalHeadphoneParam=generalHeadphoneParamU3m;
		}else if (Globals.currentMode.equalsIgnoreCase("IUNI N1")||Globals.currentMode.equalsIgnoreCase("IUNI U5")) {
			headphoneCommonParams=headphoneCommonParamsN1;
			generalHeadphoneParam=generalHeadphoneParamN1;
		}
		setHeadphoneCommonParams(mAudioManager);
	}

	synchronized public static DtsEffects getInstance(Context context) {
		if (instance == null) {
			instance = new DtsEffects(context);
		}
		return instance;
	}

	private double[] getGeneralHeadphoneParam(int type) {
		if (type < 0 || type > 6) {
			return null;
		}
		return generalHeadphoneParam[type];
	}

	private void setHeadphoneCommonParams(AudioManager audioManager) {
		if(headphoneCommonParams==null){
			return;
		}
		for (int i = 0; i < headphoneCommonParams.length; i++) {
			audioManager.setParameters(headphoneCommonParams[i]);
		}
	}

	public void setHeadphoneParam(int type) {
		double[] params = getGeneralHeadphoneParam(type);

		if (mAudioManager == null) {
			Log.i(TAG, "zll ----- setHeadphoneParam fail");
		}

		mAudioManager.setParameters("srs_mus_ext:wowhd_center=" + params[0]);
		mAudioManager.setParameters("srs_mus_ext:wowhd_focus=" + params[1]);
		mAudioManager.setParameters("srs_mus_ext:wowhd_definition_slide="
				+ params[2]);
		mAudioManager.setParameters("srs_mus_ext:wowhd_trubass_slide="
				+ params[3]);
		mAudioManager.setParameters("srs_mus_ext:wowhd_space=" + params[4]);
		mAudioManager.setParameters("srs_mus_ext:wowhd_trubass_compressor="
				+ params[5]);
	}

	public int getDtsEffect() {
		int index = 0;

		if (mAudioManager == null) {
			return 0;
		}

		try {
			String tmp = mAudioManager.getParameters("srs_cfg:geq_ext_preset=");
			if (tmp != null && !tmp.isEmpty()) {
				int start = tmp.lastIndexOf("=") + 1;
				int end = tmp.length() - 1;
				String suffix = tmp.substring(start, end);
				index = Integer.parseInt(suffix);

				LogUtil.i(TAG, "zll ----- getDtsEffect index:" + index
						+ ",tmp:" + tmp + ",suffix:" + suffix);
			}
		} catch (Exception e) {

		}

		return index;
	}

	public boolean isDtsOpen() {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) mContext.getApplicationContext()
					.getSystemService(Context.AUDIO_SERVICE);
		}
		String inParams = mAudioManager
				.getParameters("srs_cfg:trumedia_enable");
		 LogUtil.d(TAG, "isDtsOpen ---------inParams--------==" + inParams);
		return inParams.contains("1");
	}

	public void closeOrOpenDts(boolean open) {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) mContext.getApplicationContext()
					.getSystemService(Context.AUDIO_SERVICE);
		}

		String inParams = mAudioManager
				.getParameters("srs_cfg:trumedia_enable");
		Log.v(TAG, "zll closeDts ---------inParams--------==" + inParams);
		if (inParams.contains("1") && open) {
			return;
		} else if (inParams.contains("0") && !open) {
			return;
		} else {
			if (open) {
				mAudioManager
						.setParameters("srs_cfg:trumedia_enable=1;srs_cfg:trumedia_preset=0");
			} else {
				mAudioManager.setParameters("srs_cfg:trumedia_enable=0");
			}
		}
		return;
	}

	public void setDtsEffect(int index) {
		if (index < 0 || index > 7) {
			return;
		}
		LogUtil.d(TAG, "----- ----- setDtsEffect index:" + index);
		if (index == 0) {
			closeOrOpenDts(false);
			return;
		} else {
			closeOrOpenDts(true);
		}

		mAudioManager.setParameters("srs_cfg:geq_ext_preset=" + index);

		/*String params = mAudioManager
				.getParameters("srs_geq_0_ext:geq_presetname");
		LogUtil.d(TAG, "--------- -----xxxx 1 params:" + params);

		String params2 = mAudioManager
				.getParameters("srs_geq_0_ext:geq_usergains");
		LogUtil.d(TAG, "---------------xxxx 2 params:" + params2);*/

	}

	public void setHeadphoneParam(double compressor, double definition_slide,
			double trubass_slide) {
		mAudioManager.setParameters("srs_mus_ext:wowhd_trubass_compressor="
				+ compressor);
		mAudioManager.setParameters("srs_mus_ext:wowhd_definition_slide="
				+ definition_slide);
		mAudioManager.setParameters("srs_mus_ext:wowhd_trubass_slide="
				+ trubass_slide);
	}

	/**
	 * just test
	 * 
	 * @return
	 */
	public double getTrubassCompressor() {
		String compressor = mAudioManager
				.getParameters("srs_mus_ext:wowhd_trubass_compressor");
		String de = mAudioManager
				.getParameters("srs_mus_ext:wowhd_definition_slide");
		String tr = mAudioManager
				.getParameters("srs_mus_ext:wowhd_trubass_slide");
		// srs_mus_ext:wowhd_trubass_compressor=0.100
		LogUtil.d(TAG, "--------compressor:" + compressor + " de:" + de
				+ " tr:" + tr);
		try {
			if (compressor != null && !compressor.isEmpty()) {
				int start = compressor.lastIndexOf("=") + 1;
				int end = compressor.length() - 1;
				String suffix = compressor.substring(start, end);
				return Double.parseDouble(suffix);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public double[] getHeadphoneParam(Context context) {
		int mHeadSetPos = MusicUtils.getIntPref(context,
				AuroraSoundControl.AURORA_DATA_DTS_HEADSET_STATUS, 0);
		double[] ds = getGeneralHeadphoneParam(mHeadSetPos);
		return ds;
	}

}
