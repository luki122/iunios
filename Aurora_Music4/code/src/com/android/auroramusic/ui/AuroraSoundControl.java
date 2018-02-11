package com.android.auroramusic.ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Build.VERSION;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraSwitch;

import com.android.auroramusic.adapter.AuroraSoundControlAdapter;
import com.android.auroramusic.dts.DtsEffects;
import com.android.auroramusic.localeffect.LocalEffect;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.music.MusicUtils;
import com.android.music.R;

public class AuroraSoundControl extends AuroraActivity{

	private static final String TAG = "AuroraSoundControl";
	private View controlMainView,mainLayout;
	private boolean isshowAnim = false;
	private AuroraSwitch hifiSwitchView,noiseSwitchView;
	private GridView mGridView;
	private AuroraSoundControlAdapter mAuroraSoundControlAdapter;
	private View headseTextView;
	private TextView mHeadsetView;
	
	private TextView mNoiseView;
	private TextView mHifiView;
	
	private AudioManager localAudioManager;
	private static final int ANIM_TIME =350;
	private boolean isshowWindow=false;
	
	private View mLine1;
	private View mLine2;
	private View mLayout1;
	private int AudioSession = AudioEffect.ERROR_BAD_VALUE;
	private static final int AURORA_SOUND_MSG_HEADER_OUT = 300;
	
	private static final String AURORA_DATA_DTS_STATUS = "aurora_dts";
	public static final String AURORA_DATA_HIFI_STATUS = "aurora_hifi";
	private static final String AURORA_DATA_NOISE_STATUS = "aurora_noise";
	public static final String AURORA_DATA_DTS_HEADSET_STATUS = "aurora_headset";
	private int mDtsPos = -1;
	private int mHIfi = -1;
	private int mNoise = -1;
	private int mHeadsetPos = -1;
	
	private boolean mInitHifi = false;
	private boolean mbHasEar = false;
	private static final int mCount = 100;
	
	private int mtmpDtsPos = -1;
	private int mtmpHIfi = -1;
	private int mtmpNoise = -1;
	private boolean mtmpbHasEar = false;
	private boolean mtmpInitHifi = false;
	
	private LocalEffect mLocalEffect;
	private DtsEffects mDtsEffects;
	private static final int MSG_SET_EFFECT = 301;
	private static String[] mEffects = {
		"default", "pop", "dance", "rock", "normal", "jazz", "hip hop", "classical",
	};
	public static final int FLAG_TRANSLUCENT_STATUS = 67108864;
	public static final int FLAG_TRANSLUCENT_NAVIGATION = 134217728;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (VERSION.SDK_INT >= 19) {
			getWindow().addFlags(FLAG_TRANSLUCENT_STATUS);
			getWindow().addFlags(FLAG_TRANSLUCENT_NAVIGATION);
		}
		
		final Intent intent = getIntent();
		AudioSession = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR_BAD_VALUE);
		
		setContentView(R.layout.aurora_sound_control);
		
		mbHasEar = false;
		if (Globals.SWITCH_FOR_SOUND_CONTROL) {
			mDtsEffects = DtsEffects.getInstance(AuroraSoundControl.this);
		} else {
			mLocalEffect = LocalEffect.getInstance(AuroraSoundControl.this, AudioSession);
			mLocalEffect.init();
		}
		onLoadData();
		mInitHifi = (mHIfi == 1)?true:false;
		
		localAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		initRegister();
		initviews();
		
		controlMainView=findViewById(R.id.sound_control_main);
		mainLayout = findViewById(R.id.control_main_layout);
		ViewTreeObserver viewObserver = controlMainView.getViewTreeObserver();
		viewObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				controlMainView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				showSoundControl();
			}
		});
		
		ActivityInitFunction();
		upControlMainView();
				
		LogUtil.d(TAG, "onCreate ...end");
	}	
	
	private void initRegister() {
		if (Globals.SWITCH_FOR_SOUND_CONTROL) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_HEADSET_PLUG);
			registerReceiver(mHeaderSetListener, filter);
		}
		
		return;
	}
	
	private void onLoadData(){
		mDtsPos = MusicUtils.getIntPref(this, AURORA_DATA_DTS_STATUS, 1);
		mHIfi = MusicUtils.getIntPref(this, AURORA_DATA_HIFI_STATUS, -1);
		mNoise = MusicUtils.getIntPref(this, AURORA_DATA_NOISE_STATUS, -1);
		mHeadsetPos = MusicUtils.getIntPref(this, AURORA_DATA_DTS_HEADSET_STATUS, -1);
		//add by chenhl start
		if (Globals.SWITCH_FOR_SOUND_CONTROL) {
			if(mDtsEffects.isDtsOpen()){
				mDtsPos = mDtsEffects.getDtsEffect();
				if(mDtsPos==0){
					mDtsPos=1;
				}
			}
		}else {
			mDtsPos=0;
		}
		LogUtil.d(TAG, "-----Globals.SWITCH_FOR_SOUND_CONTROL"+Globals.SWITCH_FOR_SOUND_CONTROL);
		//add by chenhl end
		LogUtil.i(TAG, "zll ----- onLoadData mDtsPos:"+mDtsPos+",mHIfi:"+mHIfi+",mNoise:"+mNoise+",mHeadsetPos:"+mHeadsetPos);
		return;
	}
	
	private void onSaveData() {
		LogUtil.i(TAG, "zll ----- saveData mDtsPos:"+mDtsPos+",mHIfi:"+mHIfi+",mNoise:"+mNoise+",mHeadsetPos:"+mHeadsetPos);
		MusicUtils.setIntPref(this, AURORA_DATA_DTS_STATUS, mDtsPos);
		MusicUtils.setIntPref(this, AURORA_DATA_HIFI_STATUS, mHIfi);
		MusicUtils.setIntPref(this, AURORA_DATA_NOISE_STATUS, mNoise);
		MusicUtils.setIntPref(this, AURORA_DATA_DTS_HEADSET_STATUS, mHeadsetPos);
	}
	
	private final BroadcastReceiver mHeaderSetListener = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(action.equals(Intent.ACTION_HEADSET_PLUG)){
				int state = intent.getIntExtra("state", 0);
				//Log.i(TAG, "zll ---- onReceive cccc state:"+state);
				if (state == 0) {//out
					mbHasEar = false;
					mHandler.removeMessages(AURORA_SOUND_MSG_HEADER_OUT);
					mHandler.obtainMessage(AURORA_SOUND_MSG_HEADER_OUT, 0, -1).sendToTarget();
				} else if (state == 1) {//in
					mbHasEar = true;
					mHandler.removeMessages(AURORA_SOUND_MSG_HEADER_OUT);
					mHandler.obtainMessage(AURORA_SOUND_MSG_HEADER_OUT, 1, -1).sendToTarget();
				}
			}
		}
	};
	
	private void initDtsHeadsetName() {
		
		if (mHeadsetView != null && 
			mHeadsetView.getVisibility() == View.VISIBLE) {
			mHeadsetPos = MusicUtils.getIntPref(this, AURORA_DATA_DTS_HEADSET_STATUS, -1);
			//Log.i(TAG, "zll ----- initDtsHeadsetName mHeadsetPos:"+mHeadsetPos);
			String name = getString(R.string.aurora_null);
			if (mHeadsetPos == 1) {
				name = getString(R.string.aurora_rock_headset);
			} else if (mHeadsetPos == 2) {
				name = getString(R.string.aurora_iuni_twist_tone);
			} else if (mHeadsetPos == 3) {
				name = getString(R.string.aurora_iuni_roise_headset);
			} else if (mHeadsetPos == 4) {
				name = getString(R.string.aurora_earpods);
			} else if (mHeadsetPos == 5) {
				name = getString(R.string.aurora_akg450);
			} else if (mHeadsetPos == 6) {
				name = getString(R.string.aurora_urbeats);
			}
			mHeadsetView.setText(name);
		}
		return;
	}
	
	private void updateViewsAndSetConfig() {
		synchronized (this) {
			
			if (Globals.SWITCH_FOR_SOUND_CONTROL) {
				if ((mbHasEar == mtmpbHasEar) && 
					(mInitHifi == mtmpInitHifi) &&
					(mNoise == mtmpNoise) &&
					(mDtsPos == mtmpDtsPos)) {
					LogUtil.i(TAG, "----- onResume 0 mbHasEar:"+mbHasEar+",mInitHifi:"+mInitHifi+",mNoise:"+mNoise+",mDtsPos:"+mDtsPos);
					initDtsHeadsetName();
					
					return;
				}
				
				LogUtil.i(TAG, " ----- onResume 1 mbHasEar:"+mbHasEar+",mInitHifi:"+mInitHifi+",mNoise:"+mNoise+",mDtsPos:"+mDtsPos);
				hifiSwitchView.setOnCheckedChangeListener(null);
				noiseSwitchView.setOnCheckedChangeListener(null);
				if (mbHasEar) {
					if (mInitHifi) {
						//dts close
						if (mDtsEffects != null) {
							mDtsEffects.setDtsEffect(0);
							if (mAuroraSoundControlAdapter != null) {
								mAuroraSoundControlAdapter.setSoundControlEnable(false);
							}
							mDtsPos = 0;
						}
						
						//headset set
						if (headseTextView != null) {
							headseTextView.setEnabled(false);
							headseTextView.setAlpha(0.6f);
						}
						
						if (localAudioManager != null) {
							//noise close
							if (mNoiseView != null) {
								mNoiseView.setAlpha(0.6f);
							}
							localAudioManager.setParameters("anc_enabled=false"); // to disable
							noiseSwitchView.setEnabled(false);
							noiseSwitchView.setChecked(false);
							mNoise = 0;
							
							//hifi open
							if (mHifiView != null) {
								mHifiView.setAlpha(1f);
							}
							hifiSwitchView.setEnabled(true);
							hifiSwitchView.setChecked(true);
							
							if (true) {
								if (MusicUtils.sService != null) {
									try {
										MusicUtils.sService.onHifiChanged(1);
									} catch (Exception e) {
										
									}
								}
							} else {
								localAudioManager.setParameters("HIFI_SWITCH=1");
								int num1 = 0;
						        while(!localAudioManager.getParameters("HIFI_SWITCH").contentEquals("1")){
						        	if (num1 > mCount) {
						        		LogUtil.d(TAG, " ----- onResume 2 set fail :hifi > 1s");
										break;
									}
						        	
						        	num1++;
						            try {
						                Thread.sleep(10);
						            } catch (InterruptedException e) {
						                e.printStackTrace();
						            }
						        }
							}
							
					        mHIfi = 1;
						}
						
					} else {
						if (localAudioManager != null) {
							//noise
							boolean noise = (mNoise == 1)?true:false;
							noiseSwitchView.setEnabled(true);
							noiseSwitchView.setChecked(noise);
							if (noise) {
								localAudioManager.setParameters("anc_enabled=true"); // to disable
							} else {
								localAudioManager.setParameters("anc_enabled=false"); // to disable
							}
							if (mNoiseView != null) {
								mNoiseView.setAlpha(1f);
							}
							
							//hifi close
							if (mHifiView != null) {
								mHifiView.setAlpha(1f);
							}
							hifiSwitchView.setEnabled(true);
							hifiSwitchView.setChecked(false);
							
							if (true) {
								if (MusicUtils.sService != null) {
									try {
										MusicUtils.sService.onHifiChanged(0);
									} catch (Exception e) {
										
									}
								}
							} else {
								localAudioManager.setParameters("HIFI_SWITCH=0");
								int num2 = 0;
						        while(!localAudioManager.getParameters("HIFI_SWITCH").contentEquals("0")){
						        	if (num2 > mCount) {
						        		LogUtil.d(TAG, "zll ----- onResume 3 set fail :hifi > 1s");
										break;
									}
						        	
						        	num2++;
						            try {
						                Thread.sleep(10);
						            } catch (InterruptedException e) {
						                e.printStackTrace();
						            }
						        }
							}
							
					        mHIfi = 0;
						}
						
						//dts open
						if (mDtsEffects != null) {
							LogUtil.d(TAG, " dts opnen mDtsPos:"+mDtsPos);
							mDtsEffects.setDtsEffect(mDtsPos);
							if(mAuroraSoundControlAdapter!=null)
								mAuroraSoundControlAdapter.updateControl(mDtsPos, true);
						}
						
						//headset set
						if (headseTextView != null) {
							if(mDtsPos>0){
								headseTextView.setEnabled(true);
								headseTextView.setAlpha(1f);
							}else {
								headseTextView.setEnabled(false);
								headseTextView.setAlpha(0.6f);
							}
						}
					}
				} else {
					if (localAudioManager != null) {
						//noise close
						if (mNoiseView != null) {
							mNoiseView.setAlpha(0.6f);
						}
						noiseSwitchView.setEnabled(false);
						noiseSwitchView.setChecked(false);
						localAudioManager.setParameters("anc_enabled=false"); // to disable
						//mNoise = 0;
						
						//hifi close
						if (mHifiView != null) {
							mHifiView.setAlpha(0.6f);
						}
						hifiSwitchView.setEnabled(false);
						hifiSwitchView.setChecked(false);
						
						if (true) {
							if (MusicUtils.sService != null) {
								try {
									MusicUtils.sService.onHifiChanged(0);
								} catch (Exception e) {
									
								}
							}
						} else {
							localAudioManager.setParameters("HIFI_SWITCH=0");
							int num2 = 0;
					        while(!localAudioManager.getParameters("HIFI_SWITCH").contentEquals("0")){
					        	if (num2 > mCount) {
					        		LogUtil.d(TAG, "zll ----- onResume 3 set fail :hifi > 1s");
									break;
								}
					        	
					        	num2++;
					            try {
					                Thread.sleep(10);
					            } catch (InterruptedException e) {
					                e.printStackTrace();
					            }
					        }
					        //mHIfi = 0;
						}
						
					}
					
					//dts open
					if (mDtsEffects != null) {
						mDtsEffects.setDtsEffect(mDtsPos);
						if(mAuroraSoundControlAdapter!=null)
							mAuroraSoundControlAdapter.setSelectControl(mDtsPos);
					}
					
					//headset set
					if (headseTextView != null && mDtsPos>0) {
						headseTextView.setEnabled(false);
						headseTextView.setAlpha(0.6f);
					}
				}
				
				initDtsHeadsetName();
				
				mtmpDtsPos = mDtsPos;
				mtmpHIfi = mHIfi;
				mtmpNoise = mNoise;
				mtmpbHasEar = mbHasEar;
				mtmpInitHifi = mInitHifi;
				
				hifiSwitchView.setOnCheckedChangeListener(mOnHifiCheckedListener);
				noiseSwitchView.setOnCheckedChangeListener(mOnNoiseCheckedListener);
			}
		}
		return;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//onHifiOpenOrClose(false);
		
		if (Globals.SWITCH_FOR_SOUND_CONTROL) {
			// add by chenhl start
			if (mDtsEffects.isDtsOpen()) {
				mDtsPos = mDtsEffects.getDtsEffect();
				if (mDtsPos == 0) {
					mDtsPos = 1;
				}
			} /*else {
				mDtsPos = 0;
			}*/
			// add by chenhl end
			updateViewsAndSetConfig();
		} else {
			if (mLocalEffect != null && mGridView != null) {
				String effect = mLocalEffect.getCurrentIndexEffects().toLowerCase();
				int num = mEffects.length;
				int pos = 0;
				for (int i = 0; i < num; i++) {
					if (mEffects[i].equals(effect)) {
						pos = i;
						break;
					}
				}
				
				mDtsPos = pos;
				if(mAuroraSoundControlAdapter!=null)
					mAuroraSoundControlAdapter.setSelectControl(pos);
			}
		}
		if (mSettingsObserver != null) {
			mSettingsObserver.observe();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (mSettingsObserver != null) {
			mSettingsObserver.unregister();
		}
	}
	
	private final Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SET_EFFECT:
				int pos = msg.arg1;
				setAuroraEffect(pos);
				break;
				
			case AURORA_SOUND_MSG_HEADER_OUT:
				boolean hasEar = ((int)msg.arg1 == 0)?false:true;
				mbHasEar = hasEar;
				Log.i(TAG, "zll ---- AURORA_SOUND_MSG_HEADER_OUT xxxx hasEar:"+hasEar+",mbHasEar:"+mbHasEar);
				if (hasEar) {
					onLoadData();
				} 
				
				updateViewsAndSetConfig();
				if (hasEar) {
					onSaveData();
				}
				break;

			default:
				break;
			}
		}
		
	} ;
	
	private void setAuroraEffect(int pos) {
		if (mLocalEffect == null) {
			return;
		}
		
		if (pos == 0) {
			mDtsPos = pos;
			mLocalEffect.setLocalEffects(-1);
			return;
		} 
		
		int index = 0;
		String tmp[] = mLocalEffect.getEffectStr();
		for (int j = 0; j < tmp.length; j++) {
			//Log.i(TAG, "zll ---- yyyy 1 j:"+j+",tmp[j]:"+tmp[j]+",pos:"+pos+",mEffects[pos]:"+mEffects[pos]+",tmp.length:"+tmp.length);
			if (tmp[j].equalsIgnoreCase(mEffects[pos])) {
				index = j;
				break;
			}
		}
		
		LogUtil.i(TAG, "zll ---- yyyy 2 index:"+index);
		mDtsPos = index;
		mLocalEffect.setLocalEffects(index);
		return;
	}
	
	private void setDtsEffect(int pos) {
		if (Globals.SWITCH_FOR_SOUND_CONTROL) {
			if (mDtsEffects == null) {
				return;
			}
			
			mDtsPos = pos;
			//mDtsEffects.setDtsEffect(pos);
			mHIfi = 0;
			mInitHifi = false;
			updateViewsAndSetConfig();
		} else {
			if(mAuroraSoundControlAdapter!=null)
				mAuroraSoundControlAdapter.setSelectControl(pos);
			
			mHandler.removeMessages(MSG_SET_EFFECT);
			mHandler.obtainMessage(MSG_SET_EFFECT, pos, -1).sendToTarget();
		}
		
		return;
	}
	
	private void upDateView(boolean bshow) {
		if (bshow) {
			if (mLayout1 != null) {
				mLayout1.setVisibility(View.GONE);
			}
			
			if (hifiSwitchView != null) {
				hifiSwitchView.setVisibility(View.GONE);
			}
			
			if (noiseSwitchView != null) {
				noiseSwitchView.setVisibility(View.GONE);
			}
			
			if (headseTextView != null) {
				headseTextView.setVisibility(View.VISIBLE);
			}
			
			if (mLine1 != null) {
				mLine1.setVisibility(View.VISIBLE);
			}
			
			if (mLine2 != null) {
				mLine2.setVisibility(View.GONE);
			}
			
		} else {
			if (mLayout1 != null) {
				mLayout1.setVisibility(View.GONE);
			}
			
			if (hifiSwitchView != null) {
				hifiSwitchView.setVisibility(View.GONE);
			}
			
			if (noiseSwitchView != null) {
				noiseSwitchView.setVisibility(View.GONE);
			}
			
			if (headseTextView != null) {
				headseTextView.setVisibility(View.GONE);
			}
			
			if (mLine1 != null) {
				mLine1.setVisibility(View.GONE);
			}
			
			if (mLine2 != null) {
				mLine2.setVisibility(View.GONE);
			}
		}
		
		return;
	}
	
	private void initviews(){
		mGridView = (GridView)findViewById(R.id.sound_control);
		mLayout1 = (View)findViewById(R.id.effect_check);
		hifiSwitchView=(AuroraSwitch)findViewById(R.id.aurora_hifi_switch);
		noiseSwitchView=(AuroraSwitch)findViewById(R.id.aurora_noise_reduction_switch);
		headseTextView = (View)findViewById(R.id.aurora_headset_select_entry);
		mHeadsetView = (TextView)findViewById(R.id.aurora_headset_name);
		
		mNoiseView = (TextView)findViewById(R.id.noise_text);
		mHifiView = (TextView)findViewById(R.id.hifi_text);
		
		mLine1 = (View)findViewById(R.id.effect_line1);
		mLine2 = (View)findViewById(R.id.effect_line2);
		if (Globals.SWITCH_FOR_SOUND_CONTROL) {
			upDateView(true);
		} else {
			upDateView(false);
		}
		
		mAuroraSoundControlAdapter = new AuroraSoundControlAdapter(this);
		mGridView.setAdapter(mAuroraSoundControlAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//Log.i(TAG, "zll onItemClick...:"+arg2);
				
				/*if(mAuroraSoundControlAdapter!=null)
					mAuroraSoundControlAdapter.setSelectControl(arg2);*/
				setDtsEffect(arg2);
				onSaveData();
			}			
		});
		
		boolean bNotVaild = false;
		if (Globals.SWITCH_FOR_SOUND_CONTROL) {
			
			boolean noise = false;
			noise = (mNoise == 1)?true:false;

			hifiSwitchView.setOnCheckedChangeListener(mOnHifiCheckedListener);
			noiseSwitchView.setOnCheckedChangeListener(mOnNoiseCheckedListener);
			headseTextView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(AuroraSoundControl.this,HeasetSelectActivity.class);
					startActivityForResult(intent, HeasetSelectActivity.AURORA_HEADSET_CHANGDE);
				}
			});
			
			//判断耳机是否插入
			if(localAudioManager.isWiredHeadsetOn() && mDtsPos>0/* dts open*/){
				mbHasEar = true;
				if (headseTextView != null) {
					headseTextView.setEnabled(true);
					headseTextView.setAlpha(1f);
				}
				
				/*hifiSwitchView.setEnabled(true);
				hifiSwitchView.setChecked(mInitHifi);
				
				noiseSwitchView.setEnabled(true);
				noiseSwitchView.setChecked(noise);
				
				if (headseTextView != null) {
					headseTextView.setEnabled(true);
					headseTextView.setAlpha(1f);
				}*/
			}else{
				if (headseTextView != null) {
					headseTextView.setEnabled(false);
					headseTextView.setAlpha(0.6f);
				}
				/*hifiSwitchView.setEnabled(false);
				hifiSwitchView.setChecked(false);
				
				noiseSwitchView.setEnabled(false);
				noiseSwitchView.setChecked(false);
				
				if (headseTextView != null) {
					headseTextView.setEnabled(false);
					headseTextView.setAlpha(0.6f);
				}*/
			}
			
			//initGirdViewTouch(!mInitHifi);
			//Log.i(TAG, "zll initviews 1 flag:"+flag+",hasEar:"+hasEar);
		} else {
			initGirdViewTouch(true);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "zll ---- onActivityResult requestCode:"+requestCode+",resultCode:"+resultCode);
		if (resultCode == HeasetSelectActivity.AURORA_HEADSET_CHANGDE) {
			if (resultCode == RESULT_OK) {/*
				mHIfi = 0;
				mInitHifi = false;
				
				if (hifiSwitchView != null) {
					hifiSwitchView.setEnabled(true);
					hifiSwitchView.setChecked(false);
				}
				
				localAudioManager.setParameters("HIFI_SWITCH=0");
				int num2 = 0;
		        while(!localAudioManager.getParameters("HIFI_SWITCH").contentEquals("0")){
		        	if (num2 > 10) {
		        		Log.i(TAG, "zll ----- onActivityResult 1 set fail :hifi > 1s");
						break;
					}
		        	
		        	num2++;
		            try {
		                Thread.sleep(10);
		            } catch (InterruptedException e) {
		                e.printStackTrace();
		            }
		        }
			*/}
		}
	}

	private void initGirdViewTouch(boolean bNotVaild) {
		Log.i(TAG, "zll ---- initGirdViewTouch bNotVaild:"+bNotVaild);
		synchronized (this) {
			if (mAuroraSoundControlAdapter != null) {
				mAuroraSoundControlAdapter.setSoundControlEnable(bNotVaild);
			}
		}
		
		return;
	}
	
	private void onHeadsetCanTouch() {
		if (localAudioManager != null && localAudioManager.isWiredHeadsetOn()&& mDtsPos>0) {
			if (headseTextView != null) {
				headseTextView.setEnabled(true);
				headseTextView.setAlpha(1f);
			}
			
			if (noiseSwitchView != null) {
				noiseSwitchView.setEnabled(true);
				noiseSwitchView.setChecked((mNoise==1)?true:false);
				if (mNoiseView != null) {
					mNoiseView.setAlpha(1f);
				}
			}
		} else {
			if (headseTextView != null) {
				headseTextView.setEnabled(false);
				headseTextView.setAlpha(0.6f);
			}
			
			if (noiseSwitchView != null) {
				noiseSwitchView.setEnabled(false);
				noiseSwitchView.setChecked(false);
				if (mNoiseView != null) {
					mNoiseView.setAlpha(0.6f);
				}
			}
		}
		
		return;
	}
	
	private void onHifiOpenOrClose(boolean close) {
		if (close) {
			if (mAuroraSoundControlAdapter != null) {
				mAuroraSoundControlAdapter.setSoundControlEnable(true);
			}
			
			onHeadsetCanTouch();
			
			if (mDtsEffects != null) {
				Log.i(TAG, "zll ----- onHifiOpenOrClose 1 mDtsPos:"+mDtsPos);
				mDtsEffects.closeOrOpenDts(true);
				
				//mDtsEffects.setDtsEffect(mDtsPos);
				if(mAuroraSoundControlAdapter!=null)
					mAuroraSoundControlAdapter.setSelectControl(mDtsPos);
			}
			
			initDtsHeadsetName();
		} else {
			if (noiseSwitchView != null) {
				noiseSwitchView.setEnabled(false);
				noiseSwitchView.setChecked(false);
				
				if (mNoiseView != null) {
					mNoiseView.setAlpha(0.6f);
				}
			}
			
			if (mAuroraSoundControlAdapter != null) {
				mAuroraSoundControlAdapter.setSoundControlEnable(false);
			}
			
			if (headseTextView != null) {
				headseTextView.setEnabled(false);
				headseTextView.setAlpha(0.6f);
			}
			
			if (mDtsEffects != null) {
				Log.i(TAG, "zll ----- onHifiOpenOrClose 2 :");
				mDtsEffects.closeOrOpenDts(false);
				
				//mDtsEffects.setDtsEffect(0);
				if(mAuroraSoundControlAdapter!=null)
					mAuroraSoundControlAdapter.setSelectControl(mDtsPos);
			}
		}
		
		return;
	}
	
	private void onHifiChange(boolean bopen) {
		synchronized (this) {
			Log.i(TAG, "zll -- onHifiChange 1 bopen:"+bopen+",mHIfi:"+mHIfi+",mInitHifi:"+mInitHifi);
			mHIfi = bopen?1:0;
			mInitHifi = bopen;
			updateViewsAndSetConfig();
			Log.i(TAG, "zll -- onHifiChange 2 bopen:"+bopen+",mHIfi:"+mHIfi+",mInitHifi:"+mInitHifi);
		}
		
		return;
	}
	
	private OnCheckedChangeListener mOnHifiCheckedListener= new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			Log.i(TAG, "zll hifi checked:"+arg0.isChecked());
			onHifiChange(arg0.isChecked());
			
			onSaveData();
		}		
	};
	
	private void onNoiseChanged(boolean bopen) {
		if (localAudioManager == null ) {
			return;
		}
		
		mNoise = bopen?1:0;
		mHIfi = 0;
		mInitHifi = false;
		updateViewsAndSetConfig();
		onSaveData();
		
		return;
	}
	
	private OnCheckedChangeListener mOnNoiseCheckedListener= new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

			Log.i(TAG, "zll noise checked:"+arg0.isChecked());
			onNoiseChanged(arg0.isChecked());
		}		
	};
	
	@Override
	protected void onStop() {
		super.onStop();
		//Log.i(TAG, "zll ----- onStop ");
		onSaveData();
	}

	@Override
	protected void onDestroy() {
		//Log.i(TAG, "zll ----- onDestroy ");
		//LogUtil.d(TAG, "onDestroy... isshowWindow:"+isshowWindow);
		mHandler.removeCallbacksAndMessages(null);
		if (Globals.SWITCH_FOR_SOUND_CONTROL) {
			unregisterReceiver(mHeaderSetListener);
		}
		
		super.onDestroy();
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!isshowAnim){
				disSoundControl();
			}
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	public void onBackPressed() {
		
//		super.onBackPressed();
		if (!isshowAnim){
			disSoundControl();
		}
	}
	
	private void showSoundControl(){
		int height = controlMainView.getMeasuredHeight();
		Log.d("chenhl", "height:"+height);
		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(controlMainView,
				"translationY", new float[] { height, 0 });
		ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mainLayout, "alpha", new float[]{0,1});
		animatorSet.playTogether(objectAnimator,objectAnimator2);
		animatorSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				isshowAnim = true;
				controlMainView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {

				isshowAnim = false;
				isshowWindow=true;
			}

			@Override
			public void onAnimationCancel(Animator arg0) {

			}
		});
		animatorSet.setDuration(ANIM_TIME);
		animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animatorSet.start();
	}
	
	private void disSoundControl() {
		int height = controlMainView.getMeasuredHeight();
		Log.d("chenhl", "height1:"+height);
		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(controlMainView,
				"translationY", new float[] { 0, height });
		ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mainLayout, "alpha", new float[]{1,0});
		animatorSet.playTogether(objectAnimator,objectAnimator2);
		animatorSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				//Log.i(TAG, "zll ---- disSoundControl onAnimationEnd");
				isshowWindow=false;
				finish();
				
				onSaveData();
				//LogUtil.d(TAG, "onAnimationEnd");
			}

			@Override
			public void onAnimationCancel(Animator arg0) {

			}
		});
		animatorSet.setDuration(ANIM_TIME);
		animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animatorSet.start();
	}
	
	//add by JXH 20150908 U3 虚拟键控制 begin
	public static final String NAVI_KEY_HIDE = "navigation_key_hide"; // Settings.System
	/**
	 * 监听虚拟键状态的改变 通过 ContentObserver#onChange()获得 NAVI_KEY_HIDE 改变的情况
	 */
	private SettingsObserver mSettingsObserver;
	private boolean mHasNaviBar = false;

	public boolean ismHasNaviBar() {
		return mHasNaviBar;
	}

	private void ActivityInitFunction() {
		try {
			mHasNaviBar = getResources().getBoolean(
					com.android.internal.R.bool.config_showNavigationBar);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (mSettingsObserver == null&&(mHasNaviBar||Globals.currentMode.equalsIgnoreCase("U3"))) {
				mSettingsObserver = new SettingsObserver(new Handler());
			}
			LogUtil.d(TAG, "-------------mHasNaviBar:"+mHasNaviBar);
		}

	}

	private class SettingsObserver extends ContentObserver {
		ContentResolver resolver = getContentResolver();

		SettingsObserver(Handler handler) {
			super(handler);
		}

		private void observe() {
			resolver.registerContentObserver(
					Settings.System.getUriFor(NAVI_KEY_HIDE), false, this);
			// LogUtil.log(TAG,
			// "auroraUpdateSettings registerContentObserver");
		}

		void unregister() {
			resolver.unregisterContentObserver(this);
			// LogUtil
			// .log(TAG, "auroraUpdateSettings unregisterContentObserver");
		}

		@Override
		public void onChange(boolean selfChange) {
			LogUtil.d(TAG, "---------------selfChange"+selfChange);
				upControlMainView();
		}

	}
	
	private void upControlMainView(){
			try {
				if(mHasNaviBar||Globals.currentMode.equalsIgnoreCase("U3")){
					boolean hide = Settings.System
							.getInt(this.getContentResolver(),
									NAVI_KEY_HIDE, 0) != 0;
					FrameLayout.LayoutParams layoutParams = (android.widget.FrameLayout.LayoutParams) controlMainView.getLayoutParams();
					if(hide){
						layoutParams.bottomMargin=0;
					}else {
						layoutParams.bottomMargin=(int)getResources().getDimension(R.dimen.view_bottom_u3);
					}
					LogUtil.d(TAG, "--------layoutParams.bottomMargin:"+layoutParams.bottomMargin);
					controlMainView.setLayoutParams(layoutParams);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	//add by JXH 20150908 U3 虚拟键控制 end

}
