/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup.ux10;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;

import com.qualcomm.listen.voicewakeup.Global;
import com.qualcomm.listen.voicewakeup.MessageType;
import com.qualcomm.listen.voicewakeup.R;
import com.qualcomm.listen.voicewakeup.Utils;
import com.qualcomm.listen.voicewakeup.VwuService;
import com.qualcomm.listen.voicewakeup.ux10.SoundListAdapter.ItemData;
import com.qualcomm.listen.voicewakeup.ux10.SoundListAdapter.RowViewHolder;


public class HomeActivity extends AuroraActivity {
	private final static String TAG = "ListenLog.HomeActivity";
	private final static String MYTAG = "iht";
	private final static int MSG_RESET_RESULT_VALUES = 1;
    private static final int VIEW_INVISIBLE = 55;
	private final static int REQUEST_TRAINING_NEWUSER = 4451;
	private final static int SHOW_DETECTION_RESULT_LENGTH = 1500;
    public final static String EXTRA_AUTOSTART = "extra.homeActivity.autoStart";

	private Messenger sendToServiceMessenger; //注册与服务通信
	private Timer detectedInformationTimer = null;
    
	//private int keyDetectionsCounter = 0;
    //private int userDetectionsCounter = 0;
    
    private int detectionSuccessCounter = 0;
    private int detectionFailureCounter = 0;

    private LinearLayout uiSoundModelLayout; //蓝色线条内容
    private RelativeLayout home_mic_layout;
    private RelativeLayout home_text_layout;
    
    
    //介绍
    private RelativeLayout wakeup_intro;
    private RelativeLayout listlayout;
    private TextView wakeup_tips;
    //private Timer txtinvisibleTimer;
    
	private ImageView uiMic;
    private TextView uiKeywordText;
    private TextView uiUsernameText;
    private ToggleButton uiStartStopVwuButton; //start/stop voice activation
    
    private AuroraListView soundListView;
    private SoundListAdapter soundListAdapter;
    //private SoundModelListAdapter soundListAdapter;
    private ArrayList<SoundListAdapter.ItemData> soundmodelist;
    private SoundListAdapter.ItemData focusedSoundModel;
    
    //private TextView uiKeywordDetections; //检测的次数
    //private TextView uiUserDetections; //使用者检测次数
	//private ProgressBar uiKeywordBar; //关键字，识别的结果
	//private ProgressBar uiUserBar; //使用者，进度条
    //private TextView uiKeywordScore; //关键字识别的分数
    //private TextView uiUserScore; //使用者检测分数
	//private LinearLayout uiAdvancedDetailLayout; //进度条详细
	//private LinearLayout uiParentLayout;
	
	//private ImageView uiResetCount; //检测结果，重置 

    private static final int AURORA_ADD_BUTTON_ID = 0;
    private AuroraActionBar mActionBar;
    
    //存储文件别名
    private final String FILE_ALIAS = "soundmodel_file_alias";
    private SharedPreferences soundmodel_file_alias;
    
    //广播过滤
    private IntentFilter myFilter;
    private String HOME_CLICK = "dismissmenubyhomekey";
    
    //服务解绑
    private boolean isBind;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
       
        setAuroraContentView(R.layout.activity_home, AuroraActionBar.Type.Normal);    		
        mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.wakeup); 

		//初始化（是否准备声纹）
        Bundle extras = this.getIntent().getExtras();
        Log.v(MYTAG, "onCreate.extras="+extras);
        if (null != extras) {
            if (extras.containsKey(EXTRA_AUTOSTART)) {
                if ("true".equals(extras.getString(EXTRA_AUTOSTART))) {
                    Global.getInstance().setAutoStart(true);
                    Global.getInstance().setAndSaveSelectedSoundModel(this,Global.DEFAULT_KEYWORD, Global.DEFAULT_USERNAME);
                }
                Log.v(MYTAG, "onCreate: intent extras autoStart= " + extras.getString(EXTRA_AUTOSTART));
            }
        }
        
        //初始化
		Global.getInstance().setEnableListen(true);
		Global.getInstance().setEnableVoiceWakeup(true);
		
        Global.getInstance().readVoiceWakeUpPreference(this);
        
        // set up the UI
        initializeUserInterface();
        
        myFilter = new IntentFilter();
        myFilter.addAction(HOME_CLICK);
        
        registerReceiver(myReceiver, myFilter);
    }

	private void initializeUserInterface() {
        Log.v(TAG, "initializeUserInterface");
	    //开关
        uiStartStopVwuButton = (ToggleButton)findViewById(R.id.home_start_voice_wakeup);
        uiStartStopVwuButton.setChecked(false);
		//蓝色线框布局
	    uiSoundModelLayout = (LinearLayout)findViewById(R.id.layout_home_soundmodel);
		
	    //HT-TEST*******************************************************************************************************
	    home_mic_layout = (RelativeLayout) findViewById(R.id.home_mic_layout);
	    /*home_mic_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
	             
				if (Global.getInstance().isASoundModelRegistered()) {
					Log.e(MYTAG, "*********已经设置声闻，需要更换时，必须执行释放！....");
	               	deregisterSoundModel(); //模板正常，点击后，取消；-->新的模板列表；（如果当前已经注册声闻，则重新设置声闻时，必须释放注册）
	            }
	            Intent intent = new Intent(getApplicationContext(), SoundModelsActivity.class); //语音model列表
	            startActivity(intent);
			}
		});*/
	    
	    /*home_text_layout = (RelativeLayout) findViewById(R.id.home_text_layout);
	    home_text_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (Global.getInstance().isASoundModelRegistered()) {
	               	deregisterSoundModel(); //模板正常，点击后，取消；-->新的模板列表；（如果当前已经注册声闻，则重新设置声闻时，必须释放注册）
	            }
				Intent intent = new Intent(HomeActivity.this, TrainingActivity.class);
				startActivityForResult(intent, REQUEST_TRAINING_NEWUSER);
			}
		});*/
	    //*******************************************************************************************************
	    
	    
	    
	    //uiAdvancedDetailLayout = (LinearLayout)findViewById(R.id.home_advancedlayout); //进度条，详细
		//uiParentLayout = (LinearLayout)findViewById(R.id.home_parent_layout);
		
		//关键字，使用者
	    uiMic = (ImageView)findViewById(R.id.home_mic);
	    uiKeywordText = (TextView)findViewById(R.id.home_keyword);
        uiUsernameText = (TextView)findViewById(R.id.home_username);
        
		//uiKeywordBar = (ProgressBar)findViewById(R.id.home_keyword_bar); //识别的结果（进度条中的长度）
		
        //uiUserBar = (ProgressBar)findViewById(R.id.home_user_bar); //使用者，进度条
        
		//uiKeywordDetections = (TextView)findViewById(R.id.home_keyword_detections); //检测的次数
        
        //uiUserDetections= (TextView)findViewById(R.id.home_user_detections); //使用者检测次数
        
        //uiKeywordScore = (TextView)findViewById(R.id.home_keyword_score);
        
        //uiUserScore = (TextView)findViewById(R.id.home_user_score); //使用者，检测分数
        
        //uiResetCount = (ImageView)findViewById(R.id.home_reset_count); //检测结果，重置

        
        
        // Registers and deregisters sound models when the user clicks the Start Voice Activation button
		uiStartStopVwuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "uiStartStopVwuButton.onClick");
                uiStartStopVwuButton.setChecked(!uiStartStopVwuButton.isChecked()); //当前状态的对立状态
                if (uiStartStopVwuButton.isChecked() == false) {
                	Log.v(MYTAG, "*******************************注册新的语音模板"); //当没选择语音模板-->执行
                    registerSoundModel();
                } else {
                	Log.v(MYTAG, "****************************************语音模板正常，取消模板");
                    deregisterSoundModel(); //注销
                }
            }
        });

		
		// Starts the SoundModelsActivity when the user clicks the Sound Model Layout
        /*uiSoundModelLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopServiceListener();
                if (Global.getInstance().isASoundModelRegistered()) {
                	Log.e(MYTAG, "*********已经设置声闻，需要更换时，必须执行释放！....");
                	deregisterSoundModel(); //模板正常，点击后，取消；-->新的模板列表；（如果当前已经注册声闻，则重新设置声闻时，必须释放注册）
                }
                Intent intent = new Intent(getApplicationContext(), SoundModelsActivity.class); //语音model列表
                startActivity(intent);
            }
        });
        */
        
		
        // Resets the number of detections shown on the screen
        /*uiResetCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resetProgressBarsAndCounters();
            }
        });*/

        //updateAdvancedDetailVisibility();

        //获得当前保存的“声闻模板”
        Global.getInstance().loadSavedSoundModel(getApplicationContext());
        
        listlayout = (RelativeLayout) findViewById(R.id.listlayout); 
        listlayout.setVisibility(View.VISIBLE);
        
        wakeup_tips = (TextView) findViewById(R.id.wakeup_tips);
        wakeup_tips.setVisibility(View.VISIBLE);
        
        soundListView = (AuroraListView) findViewById(R.id.soundmodel_list);

        //隐藏
        /*txtinvisibleTimer = new Timer(true);
        txtinvisibleTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				uiHandler.sendEmptyMessage(VIEW_INVISIBLE);
			}
		}, 1000*15);*/
		
        //uiKeywordBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_score_bad)); //关键字识别的结果
        //uiKeywordBar.setProgress(0);

        //使用者，检测进度条
        //uiUserBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_score_bad));
		//uiUserBar.setProgress(0);
        
        //keyDetectionsCounter = 0;
        //userDetectionsCounter = 0;
        
        //没有内容则显示提示
        //wheather_show_intro();
        
        
        /*RelativeLayout top = (RelativeLayout)findViewById(R.id.top);
        top.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                //Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                //startActivity(intent);
			}
		});*/
	}
	
	//if Service is started, update micView and startButton status;
	private void init_mic_bttn(){
        
		/*
		if(Global.getInstance().getVoiceWakeUpServiceStatus()){
    		uiStartStopVwuButton.setChecked(true);
            uiMic.setImageResource(R.drawable.mic_on);
        }
        */
		
	}
	
	//（设置里设定，是否显示详细）是否显示详细
    private void updateAdvancedDetailVisibility() {

    	//boolean isVisibile = Global.getInstance().getShowAdvancedDetail();
        
        //uiAdvancedDetailLayout.setVisibility(isVisibile ? View.VISIBLE : View.INVISIBLE);
        //uiParentLayout.setBackgroundColor( isVisibile ?  getResources().getColor(R.color.bg_home) : getResources().getColor(R.color.bg_content));
        
        //HT_改变MIC状态
        //init_mic_bttn();
    }

    // Sets up menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    // Sets up menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.home_menu_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.home_menu_version:
                openAlertDialog(getString(R.string.training_dialog_infor_title) ,Global.getInstance().getVersionNumber());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.v(MYTAG, "***************************************onStart()");
		
		//服务
		Intent intent = new Intent("com.qualcomm.listen.voicewakeup.REMOTE_SERVICE");
		startService(intent);

		intent = new Intent("com.qualcomm.listen.voicewakeup.REMOTE_SERVICE");
		setBind(bindService(intent, mConnection, Context.BIND_AUTO_CREATE));
		
        //没有内容则显示提示
        wheather_show_intro();
		
		super.onStart();
	}
    

	@Override
	protected void onResume() {
		Log.v(MYTAG, "***************************************onResume()");
		if(soundListView != null){
			soundListView.auroraOnResume(); //控件释放资源
		}
        startDetectedInformationTimer();

		if (Global.getInstance().getIsASoundModelSelected() == false) {
			//uiKeywordText.setText(R.string.unselect_model);
            //uiUsernameText.setText(" ");
		} else {
            Log.v(TAG, "onResume: a soundModelIsSelected, Global.keyword= " + Global.getInstance().getKeyword() +", Global.UserName= " + Global.getInstance().getUsername());
            //uiKeywordText.setText(Global.getInstance().getKeyword());
            //uiUsernameText.setText(Global.getInstance().getUsername());
		}
		
		//updateAdvancedDetailVisibility();
		
		super.onResume();
	}

    @Override
	protected void onPause() {
    	Log.v(MYTAG, "***************************************onPause()");
    	if(soundListView != null){
    		soundListView.auroraOnPause();
    	}
		//stopDetectedInformationTimer();
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		Log.v(MYTAG, "*************onSaveInstanceState()");
		super.onSaveInstanceState(outState);
	}
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.v(TAG, "onWindowFocusChanged");
        super.onWindowFocusChanged(hasFocus);
    }
    
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.v(MYTAG, "***************************************onStop()");
	  	if(soundListView != null && soundListView.auroraIsRubbishOut()){
    		soundListView.auroraSetRubbishBack(); // 收回垃圾箱
    	}
		stopDetectedInformationTimer();
		super.onStop();
	}
    
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	  	if(soundListView != null && soundListView.auroraIsRubbishOut()){
    		soundListView.auroraSetRubbishBack(); // 收回垃圾箱
    	}else{
    		finish();
    	}
		//super.onBackPressed();
	}
	
    @Override
    protected void onDestroy() {
    	Log.v(MYTAG, "***************************************onDestroy()");
	  	//解除绑定
    	if(sendToServiceMessenger != null && isBind()){
            unregisterService();
            unbindService(mConnection);
            setBind(false);
            
            if(!Global.getInstance().getVoiceWakeUpServiceStatus()){
            	Intent intent = new Intent(this, VwuService.class);
            	stopService(intent);
            }
    	} 
    	
    	//清除不必要的键值
    	clearSoundModelSharePerfence();
    	
    	//清除广播监听
    	unregisterReceiver(myReceiver);
    	
    	
    	//disableFeatures();
        //closeSession();
        
        //unregisterService();
        //unbindService(mConnection);
        super.onDestroy();
    }

    // Creates a callback method for once the Service is connected
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
	        Log.v(TAG, "onServiceConnected");
			sendToServiceMessenger = new Messenger(service);

			registerService(); //注册服务  
			
			//初始化阶段(********************HT)
            Log.v(MYTAG, "onServiceConnected: Global.getEnableListen()=  " + Global.getInstance().getEnableListen()+ ", Global.getEnableVoiceWakeup= " + Global.getInstance().getEnableVoiceWakeup());
            if (Global.getInstance().getEnableListen()){
            	Log.v(MYTAG, "send message to service :"+MessageType.MSG_LISTEN_SET_PARAM);
			    sendDataReply(MessageType.MSG_LISTEN_SET_PARAM, MessageType.MSG_ENABLE, null);
			}
			if (Global.getInstance().getEnableVoiceWakeup()){
				Log.v(MYTAG, "send message to service :"+MessageType.MSG_VOICEWAKEUP_SET_PARAM);
				sendDataReply(MessageType.MSG_VOICEWAKEUP_SET_PARAM, MessageType.MSG_ENABLE, null);
            }
		}

		public void onServiceDisconnected(ComponentName name) {
            Log.v(MYTAG, "onServiceDisconnected---------------->>>>>>>>>>>断开服务链接");
			unregisterService();
		}
	};

	private void registerService() {
        Log.v(TAG, "registerService");
		Message msg = Message.obtain(null, MessageType.MSG_REGISTER_CLIENT); //1
		msg.replyTo = mMessenger;
		sendToService(msg);
	}

	private void unregisterService() {
        Log.v(MYTAG, "+++unregisterService---");
		Message msg = Message.obtain(null, MessageType.MSG_UNREGISTER_CLIENT); //2
		sendToService(msg);
		sendToServiceMessenger = null;
	}

	// Sends message to the Service with one or two ints as data
	void sendDataReply(int what, int msgArg1, Integer msgArg2) {
        Log.v(TAG, "sendMessageDataAll");
        if (null == sendToServiceMessenger) {
            return;
        }

        Message msg = Message.obtain(null, what);
        msg.arg1 = msgArg1;
        if (null != msgArg2) {
            msg.arg2 = msgArg2.intValue();
        }
        try {
            sendToServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

	private void registerSoundModel() {
        Log.v(TAG, "registerSoundModel");
        
        //判断是否有model
        if (Global.getInstance().getUserVerification() &&
                uiUsernameText.getText().equals(Global.NO_USERNAME)) {
            openAlertDialog(getString(R.string.training_dialog_close_title)
            		,getString(R.string.vs_wakeup_register_cause_fail)); //失败 
        	return;
        }
        
        //发送信息
        Message msg = Message.obtain(null, MessageType.MSG_REGISTER_SOUNDMODEL); //10
        Log.v(MYTAG, "*********************已经发送消息至服务:注册");
        sendToService(msg);
    }

    private void deregisterSoundModel() {
        Log.v(TAG, "deregisterSoundModel");
        Message msg = Message.obtain(null, MessageType.MSG_DEREGISTER_SOUNDMODEL); //11
        sendToService(msg);
    }

	private void disableFeatures() {
        Log.v(TAG, "disableFeatures");
        sendDataReply(MessageType.MSG_LISTEN_SET_PARAM, MessageType.MSG_DISABLE, null);
        sendDataReply(MessageType.MSG_VOICEWAKEUP_SET_PARAM, MessageType.MSG_DISABLE, null);
	}

	private void closeSession() {
	    Message msg = Message.obtain(null, MessageType.MSG_CLOSE_VWUSESSION);
        sendToService(msg);
	}

	// Sends a message to the service without any associated data //没有任何关联数据
	private void sendToService(Message msg) {
        Log.v(TAG, "sendToService");
		try {
			Log.v(MYTAG, "sendToServiceMessenger == "+sendToServiceMessenger); //null？？
			if(sendToServiceMessenger != null){
				sendToServiceMessenger.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Handles messages coming from the Service
	/** The msg handler. */
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
	        Log.v(TAG, "handleMessage");
			switch(msg.what) {
				case MessageType.MSG_DETECT_SUCCEEDED:  //13
		            Log.v(TAG, "MSG_DETECT_SUCCEEDED");
				    updateAdvancedDetail(true, msg.arg1, msg.arg2);
                    break;

				case MessageType.MSG_DETECT_FAILED:  //14
                    Log.v(TAG, "MSG_DETECT_FAILED");
                    updateAdvancedDetail(false, msg.arg1, msg.arg2);
                    break;

                //声音模板注册成功之后，返回信息，并修改界面状态
				case MessageType.MSG_REGISTER_SOUNDMODEL: //10
					Log.v(MYTAG, "MSG_REGISTER_SOUNDMODEL: msg.arg1= " + msg.arg1);
                    if (msg.arg1 != Global.SUCCESS) {
                    	Global.getInstance().writeVoiceWakeUpPreference(HomeActivity.this, false);
                    	
                    	/*********声纹注册失败，需要处理一下啊*************/
                    	Log.v(MYTAG, "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                    	
                        openAlertDialog(getString(R.string.training_dialog_close_title)
                        		,getString(R.string.register_soundmodel_fail)); //失败 
                        
                    } else {
                    	Global.getInstance().writeVoiceWakeUpPreference(HomeActivity.this, true);
                        
                    	//uiStartStopVwuButton.setChecked(true);
                        //uiMic.setImageResource(R.drawable.mic_on);
                        
                        //排序
                        uiHandler.sendEmptyMessageDelayed(10, 1000);
                    }
                    break;

				case MessageType.MSG_DEREGISTER_SOUNDMODEL: //11
                    Log.v(TAG, "MSG_DEREGISTER_SOUNDMODEL: msg.arg1= " + msg.arg1);
                    if (msg.arg1 != Global.SUCCESS) {
                        
                    	openAlertDialog(getString(R.string.training_dialog_close_title)
                        		,getString(R.string.register_soundmodel_fail)); //失败
                    	
                    } else {
                    	Global.getInstance().writeVoiceWakeUpPreference(HomeActivity.this, false);
                        
                    	//uiStartStopVwuButton.setChecked(false);
                        //uiMic.setImageResource(R.drawable.mic_off);
                        
                        resetProgressBarsAndCounters();
                    }
                    break;

				case MessageType.MSG_LISTEN_ENABLED: //24
                    if (msg.arg1 == MessageType.MSG_LISTEN_GET_PARAM) {
                        Log.v(TAG, "handleMessage: MSG_LISTEN_ENABLED msg received- " +
                                    "get param returned listen is enabled");
                    } else if (msg.arg1 == Global.SUCCESS) {
                        Log.v(TAG, "handleMessage: MSG_LISTEN_ENABLED msg received with status= " + msg.arg1);
                    } else if (msg.arg1 == Global.FAILURE) {
                        Log.v(TAG, "handleMessage: MSG_LISTEN_ENABLED msg received with status= " + msg.arg1);
                    }
                    break;

				case MessageType.MSG_VOICEWAKEUP_ENABLED: //28
                    if (msg.arg1 == MessageType.MSG_VOICEWAKEUP_GET_PARAM) {
                        Log.v(TAG, "handleMessage: MSG_VOICEWAKEUP_ENABLED msg received- " +
                                    "get param returned voicewakeup is enabled");
                    } else if (msg.arg1 == Global.SUCCESS) {
                        Log.v(TAG, "handleMessage: MSG_VOICEWAKEUP_ENABLED msg received with status= " + msg.arg1);
                        if (Global.getInstance().getAutoStart()) {
                            registerSoundModel();
                            Global.getInstance().setAutoStart(false);
                        }
                    } else if (msg.arg1 == Global.FAILURE) {
                        Log.v(TAG, "handleMessage: MSG_VOICEWAKEUP_ENABLED msg received with status= " + msg.arg1);
                    }
                    break;

				case MessageType.MSG_LMCGETINSTANCE_FAILED:  //出现不兼容的包，程序无法继续，请点击OK，退出程序，重新进入。//0 
				    Log.v(TAG, "handleMessage: MSG_LMCGETINSTANCE_FAILED msg received");
                    AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(HomeActivity.this);
                    builder.setTitle(R.string.dialog_error)
                    .setMessage(R.string.home_dialog_badlibs_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.v(TAG, "handleMessage: MSG_LMCGETINSTANCE_FAILED user clicked close");
                            finish();
                        }
                    });

                    if (false == ((Activity) HomeActivity.this).isFinishing()) {
                        builder.show();
                    }
                    break;

				case MessageType.MSG_LISTEN_STOPPED: //18
                    Log.v(TAG, "handleMessage: MSG_LISTEN_STOPPED msg received");
                    
                    //uiStartStopVwuButton.setChecked(false);
                    //uiMic.setImageResource(R.drawable.mic_off);
                    
                    break;

				case MessageType.MSG_LISTEN_RUNNING: //17
                    Log.v(TAG, "handleMessage: MSG_LISTEN_RUNNING msg received");
                    
                    //uiStartStopVwuButton.setChecked(true);
                    //uiMic.setImageResource(R.drawable.mic_on);
                    
                    break;

				default:
                    Log.v(TAG, "handleMessage: no such case: " + msg.what);
                    break;
            }
		}
	};
	private final Messenger mMessenger = new Messenger(mHandler);

    private void updateAdvancedDetail(Boolean succeeded, int keywordScore, int userScore) {
        Log.i(TAG, "updateAdvancedDetail: keywordScore= " + keywordScore + " userScore= " + userScore);

        startDetectedInformationTimer();

        //Drawable keywordDrawable;
        //Drawable userDrawable;
        if (succeeded) {
            Log.i(TAG, "updateAdvancedDetail: detectionSuccessCounter= " + ++detectionSuccessCounter);
            //keywordDrawable = getResources().getDrawable(R.drawable.progress_score_good);
            //userDrawable = getResources().getDrawable(R.drawable.progress_score_good);
        } else {
            Log.i(TAG, "updateAdvancedDetail: detectionFailureCounter= " + ++detectionFailureCounter);
            //keywordDrawable = getResources().getDrawable(R.drawable.progress_score_bad);
            //userDrawable = getResources().getDrawable(R.drawable.progress_score_bad);
        }
        
        //显示已经检测的次数
        //uiKeywordDetections.setText(String.valueOf(++keyDetectionsCounter));
        //Log.i(TAG, "updateAdvancedDetail: keyword detections= " + keyDetectionsCounter);
        
        //关键字，识别的结果
        //uiKeywordBar.setProgressDrawable(keywordDrawable);
        //uiKeywordBar.setProgress(keywordScore);
        
        //关键字，识别的分数
        //uiKeywordScore.setText(String.valueOf(keywordScore));
        
        //uiUserScore.setText(String.valueOf(userScore)); //使用者，检测分数
        
        if (userScore > 0) {
            
        	//uiUserDetections.setText(String.valueOf(++userDetectionsCounter)); //使用者检测次数
            //Log.i(TAG, "updateAdvancedDetail: user detections= " + userDetectionsCounter);
            
        	//使用者，检测的进度条
        	//uiUserBar.setProgressDrawable(userDrawable);
            //uiUserBar.setProgress(userScore);
        }
    }

    // Resets keyword and user detection bars after SHOW_DETECTION_RESULT_LENGTH milliseconds
	private void startDetectedInformationTimer() {
        Log.v(TAG, "startDetectedInformationTimer");
		stopDetectedInformationTimer();


        Log.v(TAG, "startDetectedInformationTimer: starting new timer");
		detectedInformationTimer = new Timer();
		TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                uiHandler.sendEmptyMessage(MSG_RESET_RESULT_VALUES);
            }
        };
		detectedInformationTimer.schedule(timerTask, SHOW_DETECTION_RESULT_LENGTH);
	}

	private void stopDetectedInformationTimer() {
        Log.v(TAG, "stopDetectedInformationTimer: stopping previous timer");
		if(null != detectedInformationTimer) {
			detectedInformationTimer.cancel();
			detectedInformationTimer = null;
		}
	}

	private Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
	        Log.v(MYTAG, "uiHandler");
			if (MSG_RESET_RESULT_VALUES == msg.what) {
			    //Log.v(TAG, "uiHandler: MSG_RESET_RESULT_VALUES");
			    //uiKeywordBar.setProgress(0); //关键字，识别的结果
			    //uiUserBar.setProgress(0); //使用者，检测进度条
			}
			
			if(msg.what == 10){
				
				Collections.sort(soundmodelist);          //排序
				soundListAdapter.notifyDataSetChanged();  //刷新
				
				soundListAdapter.refreshViewState();
				if(!soundListView.isEnabled()){
					soundListView.setEnabled(true);
				}
			}
			
			//隐藏小TIPS
			/*if(msg.what == VIEW_INVISIBLE){
				if(wakeup_tips.getVisibility() == View.VISIBLE){
					wakeup_tips.setVisibility(View.INVISIBLE);
				}
				
				if(txtinvisibleTimer != null){
					txtinvisibleTimer.cancel();
					txtinvisibleTimer = null;
				}
			}*/
			
		};
	};

	//警告dialog
    private void openAlertDialog(String title, String message) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        builder.setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setNegativeButton(R.string.dialog_cancel, null);

        if (false == ((Activity) HomeActivity.this).isFinishing()) {
            builder.show();
        }
    }
    

    private void resetProgressBarsAndCounters() {
        
    	//keyDetectionsCounter = 0;
        //userDetectionsCounter = 0;
        
        //uiKeywordDetections.setText(String.valueOf(keyDetectionsCounter)); //显示检测的次数
        
        //uiUserDetections.setText(String.valueOf(userDetectionsCounter)); //使用者，检测次数
        //Log.v(TAG, "resetProgressBarsAndCounters: keyword and user detections reset");
        
        //关键字，识别的结果
        //uiKeywordBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_score_bad));
        //uiKeywordBar.setProgress(0);

        //使用者，检测进度条
        //uiUserBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_score_bad));
        //uiUserBar.setProgress(0);
        
        //uiKeywordScore.setText(String.valueOf(keyDetectionsCounter)); //关键字识别的分数
        
        //uiUserScore.setText(String.valueOf(userDetectionsCounter)); //使用者，检测分数
    }
    
    //获得所有的SoundModel
    private ArrayList<SoundListAdapter.ItemData> getAllSoundModels() {
    	
    	///storage/sdcard0/VoiceWakeUp
    	File dir = new File(Global.APP_PATH); 
    	File[] files = dir.listFiles(new FilenameFilter() {
    		public boolean accept(File dir, String filename) {
    			return filename.endsWith(Global.SOUND_MODEL_FILE_EXT);  //.vwu
			}
    	});
    	
    	ArrayList<SoundListAdapter.ItemData> soundModels = new ArrayList<SoundListAdapter.ItemData>();
    	
    	//*******是什么意思？
    	//final boolean globalIsASoundModelSelected = Global.getInstance().getIsASoundModelSelected();
    	//final String globalUserName = Global.getInstance().getUsername();
    	//final String globalKeywordName = Global.getInstance().getKeyword();
    	
    	boolean globalIsASoundModelSelected = Global.getInstance().getIsASoundModelSelected();
    	String globalUserName = Global.getInstance().getUsername();
    	String globalKeywordName = Global.getInstance().getKeyword();
    	
    	Log.e(MYTAG, "globalIsASoundModelSelected:"+globalIsASoundModelSelected);
    	Log.e(MYTAG, "globalUserName:"+globalUserName);
    	Log.e(MYTAG, "globalKeywordName:"+globalKeywordName);
    	
    	if(files == null){
    		return soundModels;
    	}
    	
    	for(int i=0; i<files.length; i++) {
    		String soundModelFileName = files[i].getName();
    		String keywordName = null;
            String userName = null;

            //新录制的声闻：HelloIUNIThr41_123.vwu
            if (soundModelFileName.contains("_")) {
                keywordName = soundModelFileName.substring(0, soundModelFileName.indexOf("_"));
                userName = soundModelFileName.substring(soundModelFileName.indexOf("_")+1,soundModelFileName.lastIndexOf('.'));
            } else {
            	
            	//keywordName = soundModelFileName.substring(0, soundModelFileName.lastIndexOf('.'));
                //userName = Global.NO_USERNAME;
            }

    		boolean checked = false;
    		if(globalIsASoundModelSelected && globalKeywordName.equalsIgnoreCase(keywordName) && globalUserName.equalsIgnoreCase(userName)) {
    			checked = true;
    		}

    		SoundListAdapter.ItemData item = new SoundListAdapter.ItemData(checked, keywordName, 
    				userName, soundModelFileName, userName, "1");
    		//soundModels.add(item);
    		if(item != null && (item.keyword() != null && item.username() != null)){
    			soundModels.add(item); //原始模板不显示
    			//make sure the focusedSoundModel is not null when service is stoped by hand
    			if (checked) {
    				focusedSoundModel = item;
    				item.setOrder("0");
    			}
    		}
    	}
    	gaintheFilealias(soundModels);
    	Collections.sort(soundModels); //
    	return soundModels;
    }

    //若有保存的文件别名，则使用别名
    private void gaintheFilealias(ArrayList<SoundListAdapter.ItemData> soundModels){
    	if(!soundModels.isEmpty()){
    		soundmodel_file_alias = HomeActivity.this.getSharedPreferences(FILE_ALIAS, MODE_PRIVATE);
    		for(SoundListAdapter.ItemData item : soundModels){
    			if(soundmodel_file_alias.contains(item.filename())){
    				item.setAliasname(soundmodel_file_alias.getString(item.filename(), item.username()));
    			}
    		}
    	}
    }
    
    
	// Shows a dialog to delete a sound model when a user clicks the x in the list of sound models
	/*private SoundListAdapter.OnItemActionListener onItemActionListener =
	            new SoundModelListAdapter.OnItemActionListener() {
		@Override
		public void onOptionClicked(View v, ItemData item) {
	        Log.v(MYTAG, "ListUserAdapter.onOptionClicked:::"+item.username());
			focusedSoundModel = item;
	        showDeleteSoundModelDialog();
		}
	};*/
    
    
    private SoundListAdapter.OnItemSelectActionListener onItemSelectActionListener = 
    		new SoundListAdapter.OnItemSelectActionListener() {

    	@Override
    	public void showSelectedView(View view, ItemData item) {
    		// TODO Auto-generated method stub
    		if(Global.getInstance().isASoundModelRegistered()){
    			deregisterSoundModel();
    		}
    		
    		if(item != null && item.checked()){
    			Log.v(MYTAG, "**停止录音，更换语音模板**");
    			focusedSoundModel = item;
                
    			//uiKeywordText.setText(item.keyword());
                //uiUsernameText.setText(item.username());
                
                Global.getInstance().setAndSaveSelectedSoundModel(HomeActivity.this, item.keyword(), item.username());
                
                //紧接着，注册该声纹
                registerSoundModel();
    		}else{
    			
    			/*
    			soundListAdapter.refreshViewState();
    			if(!soundListView.isEnabled()){
    				soundListView.setEnabled(true);
    			}
    			focusedSoundModel = null;
                //uiKeywordText.setText(R.string.unselect_model);
                //uiUsernameText.setText(null);
    			Global.getInstance().unselectAndUnsaveSoundModel(HomeActivity.this);
    			*/
    		}
    	}
    };
    
	//显示删除dialog
    private void showDeleteSoundModelDialog() {
    	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        //View view = getLayoutInflater().inflate(R.layout.dialog_deletesoundmodel, null);
        //builder.setView(view)
          builder.setTitle(R.string.dialog_del)
            .setCancelable(true)
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(soundListView != null && soundListView.auroraIsRubbishOut()){
						soundListView.auroraSetRubbishBack();
					}
				}
			})
            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
					if(soundListView != null && soundListView.auroraIsRubbishOut()){
						soundListView.auroraSetRubbishBack();
					}
                    delModelAndUnregister();
                }
            });

        //提示内容
        //TextView uiContent = (TextView)view.findViewById(R.id.soundmodels_deletesoundmodel);
        //String content = "The " + focusedSoundModel.keyword() + " sound model for "+ focusedSoundModel.username() + " will be deleted.";
        String content = getString(R.string.the) +"\""+focusedSoundModel.username()+ "\"" + getString(R.string.will_del);  
        //uiContent.setText(content);
        builder.setMessage(content);
        builder.show();
    }
    
    private void delModelAndUnregister(){
    	
    	//说明当前被选择的对象，就是显示对象
    	if(focusedSoundModel.keyword().equals(Global.getInstance().getKeyword()) && 
    			focusedSoundModel.username().equals(Global.getInstance().getUsername()) ){
    		
    		//若已经注册，则取消注册
    		if(Global.getInstance().isASoundModelRegistered()){
    			deregisterSoundModel();
    		}
    		
            //uiKeywordText.setText(R.string.unselect_model);
            //uiUsernameText.setText(null);
			
            Global.getInstance().unselectAndUnsaveSoundModel(HomeActivity.this);
    	}
    	
    	//删除文件
    	deleteSoundModel();
    	
    	if(soundmodelist == null || soundmodelist.isEmpty()){
    		mActionBar.removeItem(AURORA_ADD_BUTTON_ID);
    		
    		//显示View
    		showView();
    	}
    }
    
    
    //删除model，其已经被传出去
	private void deleteSoundModel() {
        String filePath = Global.getInstance().generateSoundModelFilePath(focusedSoundModel.keyword(), focusedSoundModel.username());
        Log.v(MYTAG, "被删除文件的路径：deleteSoundModel: filePath= " + filePath);
		File file = new File(filePath);
		if (file.exists()) {
			if (file.delete()) { //成功删除文件之后，删除视图数据
				soundmodelist.remove(focusedSoundModel);
				soundListAdapter.notifyDataSetChanged(); //直接更新控件，View更新
				if(!Global.getInstance().getIsASoundModelSelected()){
					focusedSoundModel = null; //***
				}
				
			} else {
	            Log.v(MYTAG, "deleteSoundModel: unknown failure");
	        }
		} else {
		    Log.v(MYTAG, "deleteSoundModel: failed because file does not exist");
		}
	} 
	//需要清理已经选中的对象
	private void clearCheckedSoundModels() {
		 for (SoundListAdapter.ItemData item : soundmodelist) {
	    	item.setChecked(false);
	    }
	 }
	
	//录制新的声纹模板后，返回
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.v(MYTAG, "onActivityResult::：：：：：：：：：：：：：：：：：：:requestCode="+requestCode);
		Log.v(MYTAG, "onActivityResult::：：：：：：：：：：：：：：：：：：:resultCode="+resultCode);
		
		if(requestCode == REQUEST_TRAINING_NEWUSER){
			if(resultCode == RESULT_OK){
				//取消注册
				//if(Global.getInstance().isASoundModelRegistered()){
				//	deregisterSoundModel();
				//}
				//成功
				clearCheckedSoundModels();
				
				String keyword = Global.getInstance().getKeyword();
				String usname  = Global.getInstance().getUsername();
				
				SoundListAdapter.ItemData item = new SoundListAdapter
						.ItemData(true, keyword, usname, keyword+"_"+usname+".vwu", usname, "0");
				
				soundmodelist.add(item);
				Collections.sort(soundmodelist);
				soundListAdapter.notifyDataSetChanged();
				
				focusedSoundModel = item;
				//显示
				if(listlayout.getVisibility() == View.INVISIBLE){
					listlayout.setVisibility(View.VISIBLE);
					
					if(wakeup_intro.getVisibility() == View.VISIBLE){
						wakeup_intro.setVisibility(View.INVISIBLE);
					}
				}
				
				//注册当前声纹
				registerSoundModel();
			}else{
				//出错
				if(focusedSoundModel != null){
					
					//还原添加录制之前的状态
		            //uiKeywordText.setText(focusedSoundModel.keyword());
		            //uiUsernameText.setText(focusedSoundModel.username());
					
					//注意已经删除来最后一个语音文本
					if(soundmodelist != null && !soundmodelist.isEmpty()){
			            Global.getInstance().setAndSaveSelectedSoundModel(HomeActivity.this, 
								focusedSoundModel.keyword(), focusedSoundModel.username());
						//注册
						registerSoundModel();
					}else{
						Global.getInstance().unselectAndUnsaveSoundModel(this);
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	//增加新的功能，按钮监听事件
	private OnAuroraActionBarItemClickListener onbarItemClickListener = new OnAuroraActionBarItemClickListener() {
		
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			// TODO Auto-generated method stub
			if (Global.getInstance().isASoundModelRegistered()) {
               	deregisterSoundModel(); //模板正常，点击后，取消；-->新的模板列表；（如果当前已经注册声闻，则重新设置声闻时，必须释放注册）
            }
			Intent intent = new Intent(HomeActivity.this, TrainingActivity.class);
			startActivityForResult(intent, REQUEST_TRAINING_NEWUSER);
		}
	};
	
	private void wheather_show_intro(){
		
        //AuroraListView--soundmodel
        if(soundmodelist != null){
        	soundmodelist.clear();
        }else{
        	soundmodelist = new ArrayList<SoundListAdapter.ItemData>();
        }
        soundmodelist = getAllSoundModels();
        
        soundListAdapter = new SoundListAdapter(HomeActivity.this, 
        		soundmodelist, onItemSelectActionListener); 
        soundListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        soundListView.setAdapter(soundListAdapter);
        soundListView.setEnabled(true);
        
        soundListView.auroraSetNeedSlideDelete(true);
        soundListView.auroraSetAuroraBackOnClickListener(new AuroraBackOnClickListener() {
			
			@Override
			public void auroraPrepareDraged(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void auroraOnClick(int position) {
				// TODO Auto-generated method stub

				focusedSoundModel = soundmodelist.get(position);
		        showDeleteSoundModelDialog();
		        
				//soundList.remove(position);
				//adapter.notifyDataSetChanged();
			}
			
			@Override
			public void auroraDragedUnSuccess(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void auroraDragedSuccess(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
        //点击事件
        soundListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				RowViewHolder v = (RowViewHolder) view.getTag();
				if(!v.itemData.checked()){
					soundListView.setEnabled(false);
					soundListAdapter.onItemClick(view);
				}

				/*
				if(v.itemData.checked()){
					soundListView.setEnabled(true);
				}else{
					soundListView.setEnabled(false);
				}
				soundListAdapter.onItemClick(view);
				*/
			}
		});
        

        //长按修改别名
        soundListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				showRenameUserDialog(soundmodelist.get(position));
				return false;
			}
		});
        
        //显示界面
        showView();
	}
	
	private void showView() {
		if (soundmodelist == null || soundmodelist.isEmpty()) {
			//soundListView.setVisibility(View.INVISIBLE);
			listlayout.setVisibility(View.INVISIBLE);
			wakeup_intro = (RelativeLayout) findViewById(R.id.wakeup_intro);
			wakeup_intro.setVisibility(View.VISIBLE);

			AuroraButton add_soundmodel = (AuroraButton) findViewById(R.id.add_soundmodel);
			add_soundmodel.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (Global.getInstance().isASoundModelRegistered()) {
						deregisterSoundModel(); // 模板正常，点击后，取消；-->新的模板列表；（如果当前已经注册声闻，则重新设置声闻时，必须释放注册）
					}
					Intent intent = new Intent(HomeActivity.this,
							TrainingActivity.class);
					startActivityForResult(intent, REQUEST_TRAINING_NEWUSER);
				}
			});
		} else {
			AuroraActionBarItem item = mActionBar.getItem(AURORA_ADD_BUTTON_ID);
			if(item == null){
				addAuroraActionBarItem(AuroraActionBarItem.Type.Add,AURORA_ADD_BUTTON_ID);
				mActionBar.setOnAuroraActionBarListener(onbarItemClickListener);
			}
		}
	}
	
	//添加新语音用户名称
	private AuroraAlertDialog renameNewUserDialog;
	private void showRenameUserDialog(final ItemData item) {
		
		//获得所有模板的名称
		String[] keywordList = Utils.getKeywordList();
		if(keywordList == null || keywordList.length == 0){
            String inavlidTitle = getString(R.string.soundmodels_dialog_invalid_title);
            String inavlidContent = getString(R.string.no_soundmodel);
	        openAlertDialog(inavlidTitle, inavlidContent);
			return;
		}
		
		//模板名称
		final String sound_name = keywordList[0].toString();
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
		View view = getLayoutInflater().inflate(R.layout.dialog_adduser, null); //添加用户布局
		builder.setView(view)
			.setTitle(R.string.soundmodels_dialog_rename_title)
			//.setCancelable(false)
			.setCancelable(true)
			.setNegativeButton(R.string.dialog_cancel, null)  
			.setPositiveButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					EditText editText = (EditText)renameNewUserDialog.findViewById(R.id.soundmodels_adduser_nameedit);
					String username = editText.getText().toString();
                    
					if(username.equals(item.aliasname())){
                    	//直接取消
                    	renameNewUserDialog.dismiss();
                    }else if(isExist(username)){
                    	
                    	//用户名重复
                    	openAlertDialog(getString(R.string.training_dialog_close_title), 
                    			getString(R.string.soundmodels_dialog_duplicate_content));
                    	
                    }else{
                    	item.setAliasname(username);
                    	soundListAdapter.notifyDataSetChanged(); 
                    	soundmodel_file_alias = HomeActivity.this.getSharedPreferences(FILE_ALIAS, MODE_PRIVATE);
                    	soundmodel_file_alias.edit().putString(item.filename(), username).commit();
                    }
				}
			});
		renameNewUserDialog = builder.show();
		
        EditText editText = (EditText)renameNewUserDialog.findViewById(R.id.soundmodels_adduser_nameedit);
        editText.setText(item.aliasname());
        CharSequence txt = editText.getText();
        if(txt instanceof Spannable){
        	Selection.setSelection((Spannable) txt, txt.length());
        }
        
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    renameNewUserDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        
        //字符过滤
        //数字、字母、中文
        final Pattern ps = Pattern.compile("[0-9]");
        final Pattern pz = Pattern.compile("[a-zA-Z]");
        final Pattern ph = Pattern.compile("[\u4e00-\u9fa5]");
        
		editText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if(s.toString().trim().equals("")){
					renameNewUserDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
				}else{
					renameNewUserDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
				Matcher mm;
				for(int i=0; i<s.length(); i++){
					String ca = String.valueOf(s.charAt(i));

					mm = ps.matcher(ca);
					if(mm.matches()){
						continue;
					}
					
					mm = pz.matcher(ca);
					if(mm.matches()){
						continue;
					}
					
					mm = ph.matcher(ca);
					if(mm.matches()){
						continue;
					}
					
					if(i == s.length()){
						break;
					}

					s.delete(i, i+1);
				}
				
			}
		});
        
	}

	//别名是否存在
	private boolean isExist(String newname){
		for(ItemData item : soundmodelist){
			if(item.aliasname().equals(newname)){
				return true;
			}
		}
		return false;
	}
	
	//清除不必要的SharePreference的键值对
	private void clearSoundModelSharePerfence(){
		if(soundmodel_file_alias == null){
			return;
		}
		soundmodel_file_alias = HomeActivity.this.getSharedPreferences(FILE_ALIAS, MODE_PRIVATE);
		if(soundmodelist == null || soundmodelist.isEmpty()){
			soundmodel_file_alias.edit().clear().commit();
			return;
		}else{
			Map<String, ?> map = soundmodel_file_alias.getAll();
			Iterator ite = map.entrySet().iterator();
			while(ite.hasNext()){
				boolean bool = false;
				Entry entry = (Entry) ite.next();
				for(ItemData item : soundmodelist){
					if(entry.getKey().equals(item.filename())){
						bool = true;
						break;
					}
				}
				if(!bool){
					soundmodel_file_alias.edit().remove(entry.getKey().toString()).commit();
				}
			}
		}
	}
	
	
	//Home键广播
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(HOME_CLICK)  ){
				
				Log.v(MYTAG, "------------>---HomeKey");
				
				if(sendToServiceMessenger != null && isBind()){
			        //unregisterService();
			        unbindService(mConnection);
			        setBind(false);
				}
			}
		}
	};

	public boolean isBind() {
		return isBind;
	}

	public void setBind(boolean isBind) {
		this.isBind = isBind;
	}
}