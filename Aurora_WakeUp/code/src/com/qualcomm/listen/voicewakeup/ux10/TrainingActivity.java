/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup.ux10;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraButton;

import com.qualcomm.listen.voicewakeup.Global;
import com.qualcomm.listen.voicewakeup.ListenAudioRecorder;
import com.qualcomm.listen.voicewakeup.MessageType;
import com.qualcomm.listen.voicewakeup.R;
import com.qualcomm.listen.voicewakeup.Utils;
import com.qualcomm.listen.voicewakeup.VwuService;


/**
 * Actions:
 *
 * From UI
 *
 * - when mic button is pressed, it starts training by starting speech detection for five times.
 *      -audioRecord.getInstance();
 *      -audioRecord.start();
 *      <user speaks keyword>
 *      -audioRecord.stop();
 *      -audioRecord.release();
 * - when close button pressed, training is aborted and model is reloaded from file.
 * - When required number of utterances are accepted, lets user select continue or cancel this training session.
 * - When user clicks continue, updates sound model and saves it to file.
 *
 * Incoming messages:
 * When speech is detected, message arrives with keyword confidence.
 * This activity makes a decision based on the value TRAINING_CONFIDENCE_LEVEL_THRESHOLD.
 * If accepted, update UI, increase counts, accumulate current utterance.
 * If rejected, notify user.
 */
public class TrainingActivity extends AuroraActivity {
	private final static String TAG = "ListenLog.TrainingActivity";
	private final static String MYTAG = "iht";
	public final static String EXTRA_NEWUSER = "extra.training.newUser";
	public final static String EXTRA_USERNAME = "extra.training.userName";
	public final static String EXTRA_KEYWORD = "extra.training.keyword";
	//默认匹配度
	public final static int TRAINING_CONFIDENCE_LEVEL_THRESHOLD = 72;
    private final static int TRAINING_RECORDING_DURATION = 2000;
    private final static int MSG_STOP_TRAINING_RECORDING = 1;

    //录音计数器
	private RecordingCounter recordingCounter = null;
	
	private boolean completedTraining = false;
	private String trainingUserName;
	private String trainingKeyword;
    
	//录制、转换状态
	private String recordingState; //录制状态
	private String processingState;
	
	
	private Messenger sendToServiceMessenger;
    private ListenAudioRecorder recorder;

	private ImageButton uiMic; //录音开始按钮
	//private ImageButton uiClose; //停止录音
	//private TextView uiUserName;
	private Button uiContinue; //录音成功后，保存文件
	private TextView uiNotice;
	//private TextView uiRecordingState;
	//private TextView uiRecordingKeyword; //关键字
	//private View uiWelcomeLayout;
	//private View uiRecordLayout; //录音界面
	private RelativeLayout uiMicLayout; //MIC布局

	//private View uiDisableLayout;

	//HT
	private RelativeLayout layout_training_tips;
	private RelativeLayout layout_training_record;
	
	//提示语
	private RelativeLayout sketch_keyword; //提示语的动画
	private RelativeLayout ani_image;
	private RelativeLayout recording_user;
	private RelativeLayout training_recording_process;

	//动画
	private AnimatorSet soundCompoundAnimatorSet;
	
	
	private AuroraButton ready_to_recording;
	//private TextView recording_keywords;
	private TextView training_recording_notice;
	private AuroraButton fail_to_recording_again;
	//private TextView recording;
	
	//private Button training_recording_continue;
	
	private AuroraButton compound_fail_to_again;
	
	
    private Timer trainingTimer;
    private ProgressDialog progressDialog = null;
    
    private ImageView[] imageViews; //1-5的录音状态

    private ImageView recording_ani; //录制动画
    private ImageView recording_mic;
    private ImageView recording_exc;
    
    private TextView recording_username;
    private TextView recording_result;
    
    //已经成功创建，正在合成动画，此时不能退出
    private boolean isCompound = false;
    
    //actionBar
    private AuroraActionBar mActionBar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        Log.v(MYTAG, "onCreate");
	    super.onCreate(savedInstanceState);
        setAuroraContentView(R.layout.activity_training, AuroraActionBar.Type.Normal);
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.wakeup_recording);
	    
	    Global.getInstance().removeExistingRecordingFiles(); 
	    
	    //初始化界面
	    initView();
	    
	    //添加名称
	    showAddUserDialog();
	    
	    //创建通信
	    Intent intent = new Intent(this, VwuService.class); //绑定服务？？
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        try {
        	recording_ani = (ImageView) findViewById(R.id.recording_ani);
        	recording_exc = (ImageView) findViewById(R.id.recording_exc);
        	recording_mic = (ImageView) findViewById(R.id.recording_mic);
        	
        	
            imageViews = new ImageView[] {
                    (ImageView)findViewById(R.id.training_record1),
                    (ImageView)findViewById(R.id.training_record2),
                    (ImageView)findViewById(R.id.training_record3),
                    (ImageView)findViewById(R.id.training_record4),
                    (ImageView)findViewById(R.id.training_record5)
            };
            recordingCounter = new RecordingCounter(this, imageViews, recording_ani); //注册计数器，实时改变当前录制进度
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	//添加新语音用户名称
	private AuroraAlertDialog addNewUserDialog;
	private void showAddUserDialog() {
		
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
		.setTitle(R.string.soundmodels_dialog_adduser_title)
			.setCancelable(false)
			//.setCancelable(true)
			.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//若取消，则直接返回至原来activity
					finish();
				}
			})  
			.setPositiveButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText editText = (EditText)addNewUserDialog.findViewById(R.id.soundmodels_adduser_nameedit);
					String username = editText.getText().toString();
					Log.v(MYTAG, "showAddUserDialog: user input username as: " + username);
                   
					/*if(username == null || username.trim().equals("")){
	                    
						//填写名称
						//String inavlidTitle = getString(R.string.training_dialog_close_title);
	                    //String inavlidContent = getString(R.string.soundmodels_dialog_invalid_content);
				        //openAlertDialog(inavlidTitle, inavlidContent);
				        
                    }else*/ 
                    	
                    if(Global.getInstance().soundModelFileExists(sound_name, username)){
                    	//名称重复
                    	showDuplicateSoundModelDialog();
                    }else{
                    	//进入选择语音模板界面
                    	//showKeywordSelection(username);
                    	trainingUserName = username.trim();
                    	trainingKeyword = sound_name.trim();
                    	if(trainingUserName != null && trainingKeyword != null){
                    		ready_to_recording.setVisibility(View.VISIBLE);
                    		
                    		//将声音模板保存
                    		SoundListAdapter.ItemData item = new SoundListAdapter.ItemData(true, 
                    				trainingKeyword, trainingUserName, trainingKeyword+"_"+trainingUserName+".vwu", trainingUserName, "0");
                    		Global.getInstance().setAndSaveSelectedSoundModel(TrainingActivity.this, item.keyword(), item.username());
                    	}
                    }
                    
                    //注意格式
                   /* if (Global.getInstance().soundModelFileExists(inKeyword, username)) {
				        showDuplicateSoundModelDialog();
				    } else if (username.equalsIgnoreCase("") || username.equalsIgnoreCase("<Add New User>")) {
	                    Log.v(TAG, "showAddUserDialog: username is invalid");
	                    String inavlidTitle = getString(R.string.soundmodels_dialog_invalid_title);
	                    String inavlidContent = getString(R.string.soundmodels_dialog_invalid_content);
				        openAlertDialog(inavlidTitle, inavlidContent);
				    } else {
    					Log.v(TAG, "showAddUserDialog: new username= " + editText.getText().toString());
    					
    					//addSoundModel(inKeyword, username); //保存关键字，用户名称
    					//进入---> 选择语音模板dialog
				    }*/
				}
			});
		addNewUserDialog = builder.show();
		addNewUserDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		
		//添加监听事件
		addNewUserDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK){
					finish();
				}
				return false;
			}
		});
		
		
		EditText editText = (EditText)addNewUserDialog.findViewById(R.id.soundmodels_adduser_nameedit);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addNewUserDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
					addNewUserDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
				}else{
					addNewUserDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
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
	
	//已经存在
	private void showDuplicateSoundModelDialog() {
        String duplicateTitle = getString(R.string.training_dialog_close_title);
        String duplicateContent = getString(R.string.soundmodels_dialog_duplicate_content);
        openAlertDialog(duplicateTitle, duplicateContent);
    }
	
	//警告dialog
    private void openAlertDialog(String title, String message) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        builder.setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//取消则直接返回
					finish();
				}
			});

        if (false == ((Activity) TrainingActivity.this).isFinishing()) {
            builder.show();
        }
    }
    
    //语音选择Dialog
    /*private AlertDialog dialogKeywordSelection = null;
	private void showKeywordSelection(final String userName) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_keywordlist, null); //listview

        //显示SoundModel-list
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.soundmodels_dialog_selectkeyword_title)
            .setView(view)
            .setCancelable(true)
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dialogKeywordSelection = null;
                    
                    //取消则直接退出
                    finish();
                }
            });

        //所有模板的名字List（关键字List）
        String[] keywordList = Utils.getKeywordList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, keywordList);

        ListView uiKeywordSelection = (ListView)view.findViewById(R.id.list);
        uiKeywordSelection.setAdapter(adapter);
        uiKeywordSelection.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                TextView uiText = (TextView)arg1.findViewById(android.R.id.text1);
                String text = uiText.getText().toString();

                if (null != dialogKeywordSelection) {
                    dialogKeywordSelection.dismiss();
                    dialogKeywordSelection = null;
                }
                
                if(Global.getInstance().soundModelFileExists(text, userName)){
                	showDuplicateSoundModelDialog();
                }else{
                	//名称，语音模板，确定后，即可进行录音
                	//注意录音文件成功后，处理步骤
                	trainingUserName = userName.trim();
                	trainingKeyword = text.trim();
                	if(trainingUserName != null && trainingKeyword != null){
                		ready_to_recording.setVisibility(View.VISIBLE);
                		
                		//将声音模板保存
                		SoundListAdapter.ItemData item = new SoundListAdapter.ItemData(true, 
                				trainingKeyword, trainingUserName, trainingKeyword+"_"+trainingUserName+".vwu", trainingUserName, "1");
                		Global.getInstance().setAndSaveSelectedSoundModel(TrainingActivity.this, item.keyword(), item.username());
                	}
                }
            }
        });

        String keywordName = Global.getInstance().getKeyword();
        Log.v(MYTAG, "***keywordName:"+keywordName);
        for (int lp = 0; lp < keywordList.length; lp++) {
            if (keywordName.equals(keywordList[lp])) {
                uiKeywordSelection.setItemChecked(lp, true);
                break;
            }
        }
        dialogKeywordSelection = dialog.show();
    }*/
	
	//关键字列表（关键字文件名称列表）
	/*private String[] getKeywordList() {
        Log.v(TAG, "getKeywordList");
        File dir = new File(Global.APP_PATH);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(Global.SOUND_MODEL_FILE_EXT);
            }
        });

        List<String> keywordArrayList = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            String soundModelFileName = files[i].getName();
            String keywordName = "";
            if (soundModelFileName.contains("_")) {
                keywordName = soundModelFileName.substring(0, soundModelFileName.lastIndexOf('_'));
            } else {
                keywordName = soundModelFileName.substring(0, soundModelFileName.lastIndexOf('.'));
            }
            if (keywordArrayList.contains(keywordName) == false) {
                keywordArrayList.add(keywordName);
            }
        }

        String[] keywordArray = keywordArrayList.toArray(new String[keywordArrayList.size()]);
        return keywordArray;
    }*/
    
    //****************************************************************************************************************
	private void initView(){
        //HT
		layout_training_tips = (RelativeLayout) findViewById(R.id.layout_training_tips);
		layout_training_tips.setVisibility(View.VISIBLE);
		
		layout_training_record = (RelativeLayout) findViewById(R.id.layout_training_record);
		layout_training_record.setVisibility(View.INVISIBLE);
		
		/*training_recording_continue = (Button) findViewById(R.id.training_recording_continue);
		training_recording_continue.setVisibility(View.INVISIBLE);
		training_recording_continue.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//保存文件
				//saveSoundModel();
			}
		});*/
		
		compound_fail_to_again = (AuroraButton) findViewById(R.id.compound_fail_to_again) ;
		compound_fail_to_again.setVisibility(View.INVISIBLE);
		compound_fail_to_again.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//重新录制
				Intent intent = new Intent(TrainingActivity.this, TrainingActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		
		
        ready_to_recording = (AuroraButton) findViewById(R.id.ready_to_recording);
        ready_to_recording.setVisibility(View.INVISIBLE);
        ready_to_recording.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 显示录音界面，同时进行录音
				setVisibleRecordingArea();
                
                //同时需要播放其录音动画
                startRecordingAniUptoDown();
                //enRecordedAniDowntoUp();
                
                //录音在动画之后开始
                //startTraining();
			}
		});
        
        //文字初始化
        //recording_keywords = (TextView) findViewById(R.id.recording_keywords);
        sketch_keyword = (RelativeLayout) findViewById(R.id.sketch_keyword);
        ani_image = (RelativeLayout) findViewById(R.id.ani_image); 
        recording_user = (RelativeLayout) findViewById(R.id.recording_user); 
        training_recording_process = (RelativeLayout) findViewById(R.id.training_recording_process);
        
        
        recording_username = (TextView) findViewById(R.id.recording_username);
        recording_result = (TextView) findViewById(R.id.recording_result);
        
        //录音提示
        training_recording_notice = (TextView) findViewById(R.id.training_recording_notice);
        training_recording_notice.setVisibility(View.INVISIBLE);
        
        //recording = (TextView) findViewById(R.id.recording);
        //recording.setVisibility(View.INVISIBLE);
        
        //录制失败，再来一次
        fail_to_recording_again = (AuroraButton) findViewById(R.id.fail_to_recording_again);
        fail_to_recording_again.setVisibility(View.INVISIBLE);
        fail_to_recording_again.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    Log.v(MYTAG, "uiClose.onClick...................录制失败，重新来过.......");
			    
			    //已经成功录制，则询问是否放弃(***************************监控已成功录制，但返回键，Home键的处理问题********************)
			    if (completedTraining) {
			    	Log.v(TAG, "uiClose.onClick: completedTraining is true");
			    	 AlertDialog.Builder builder = new AlertDialog.Builder(TrainingActivity.this);
				        builder.setTitle(R.string.training_dialog_close_title)
				        .setMessage(R.string.training_dialog_close_message)
				        .setCancelable(false)
				        .setNegativeButton(R.string.dialog_cancel, null)
				        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
				            @Override
				            public void onClick(DialogInterface dialog, int which) {
				            	//重新录制？？？
				                resetTrainings();
				                
				                closeRecordingWindow();
				                Log.v(TAG, "uiClose.onClick: completedTraining set to false b/c user clicked OK");
				            }
				        });

				        if (false == ((Activity) TrainingActivity.this).isFinishing()) {
				            builder.show();
				        }			    	
			    }

			    //录制到一半，点击后，直接进行重新录制
			    if (completedTraining == false) {
                    Log.v(MYTAG, "uiClose.onClick: completedTraining is false..........重新录制");
			        
                    closeRecordingWindow();
			        
			        Log.v(MYTAG, "uiClose.onClick: getNumUserRecordings: " + Global.getInstance().getNumUserRecordings()+"-----当前录制的次数");
			    }
			}
			
			//关闭窗口，被动执行
            private void closeRecordingWindow() {
                //setVisibleTrainingMainArea();
            	
            	//fail_to_recording_again.setText(getResources().getString(R.string.recording));
            	//fail_to_recording_again.setClickable(false);
            	
            	//recording.setVisibility(View.VISIBLE);
            	//continue_or_save.setVisibility(View.VISIBLE);
            	
            	fail_to_recording_again.setVisibility(View.INVISIBLE);
            	training_recording_notice.setVisibility(View.INVISIBLE);
            	
                stopTraining();
                stopTrainingTimer();
                
                //继续录音
                startTraining();
            }
		});
	}
	
	//Hello IUNI 动画
	private void startRecordingAniUptoDown(){
		ObjectAnimator ani = ObjectAnimator.ofFloat(sketch_keyword, "translationY", -335, 0);
		ani.setDuration(400);
		ani.setInterpolator(new DecelerateInterpolator());
		ani.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				//启动录音功能
				startTraining();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		ani.start();
	}
	
	private void enRecordedAniDowntoUp(){
		
		//Hello 向上移动
		ObjectAnimator helloAni = ObjectAnimator.ofFloat(sketch_keyword, "translationY", 0, -335f);
		helloAni.setDuration(500);
		
		//mic向上
		ObjectAnimator imageAni = ObjectAnimator.ofFloat(ani_image, "translationY", 0, -156);
		imageAni.setDuration(500);
		
		//用户显示
		recording_username.setText(Global.getInstance().getUsername());
		recording_user.setAlpha(0.0f);
		recording_user.setVisibility(View.VISIBLE);
		ObjectAnimator userAni = ObjectAnimator.ofFloat(recording_user, "alpha", 0, 1.0f);
		userAni.setStartDelay(50);
		userAni.setDuration(450);
		
		
		//点点往中间移动
		int[] img0Location = new int[2];
		imageViews[0].getLocationInWindow(img0Location);
		
		int[] img1Location = new int[2];
		imageViews[1].getLocationInWindow(img1Location);
		
		final int[] img2Location = new int[2];
		imageViews[2].getLocationInWindow(img2Location);
		
		int[] img3Location = new int[2];
		imageViews[3].getLocationInWindow(img3Location);
		
		int[] img4Location = new int[2];
		imageViews[4].getLocationInWindow(img4Location);

		//1
		AnimatorSet img0Set = createImageViewsAnimatorSet(imageViews[0], img2Location[0] - img0Location[0]);
		img0Set.setDuration(500);
		//2
		AnimatorSet img1Set = createImageViewsAnimatorSet(imageViews[1], img2Location[0] - img1Location[0]);
		img1Set.setDuration(400);
		//3
		ObjectAnimator img2_alpha_Ani = ObjectAnimator.ofFloat(imageViews[2], "alpha", 1.0f, 0.0f);
		img2_alpha_Ani.setDuration(300);
		//4
		AnimatorSet img3Set = createImageViewsAnimatorSet(imageViews[3], img2Location[0] - img3Location[0]);
		img3Set.setDuration(400);
		//5
		AnimatorSet img4Set = createImageViewsAnimatorSet(imageViews[4], img2Location[0] - img4Location[0]); 
		img4Set.setDuration(500);
		
		//1+2+3+4+5
		AnimatorSet proSet = new AnimatorSet();
		//proSet.setDuration(500 * 10);
		proSet.playTogether(img0Set, img1Set, img2_alpha_Ani, img3Set, img4Set);
		
		//*********************************************************************
		AnimatorSet aSet = new AnimatorSet();
		aSet.setStartDelay(250);
		aSet.setInterpolator(new DecelerateInterpolator());
		aSet.playTogether(helloAni, imageAni, userAni, proSet);
		aSet.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				Log.v(MYTAG, "------->"+img2Location[0]);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				Log.v(MYTAG, "------->"+img2Location[0]);
				
				//imageViews[1].setAlpha(1.0f);
				//imageViews[2].setAlpha(1.0f);
				//imageViews[3].setAlpha(1.0f);
				
				imageViews[1].setX( img2Location[0] - 80 );
				imageViews[3].setX( img2Location[0] + 80 );
				
				//开始保存文件
				saveSoundModel();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		aSet.start();
	}
	
	private AnimatorSet SoundModelCompoundAnimatorSet(){
		//点1-->凸显
		AnimatorSet img1_expand = imageViewProcessExpand(imageViews[1]);
		img1_expand.setInterpolator(new DecelerateInterpolator());
		img1_expand.setDuration(500);
		//img1_expand.start();
		
		//点1-->缩隐；点2-->凸显
		AnimatorSet img1_narrow = imageViewProcessNarrow(imageViews[1]);
		img1_narrow.setInterpolator(new AccelerateInterpolator());
		img1_narrow.setDuration(500);
		img1_narrow.setStartDelay(500);
		//img1_narrow.start();
		
		AnimatorSet img2_expand = imageViewProcessExpand(imageViews[2]);
		img2_expand.setInterpolator(new DecelerateInterpolator());
		img2_expand.setDuration(500);
		img2_expand.setStartDelay(250);
		//img2_expand.start();
		
		//d点2-->缩隐；点3凸显
		AnimatorSet img2_narrow = imageViewProcessNarrow(imageViews[2]);
		img2_narrow.setInterpolator(new AccelerateInterpolator());
		img2_narrow.setDuration(500);
		img2_narrow.setStartDelay(750);
		
		AnimatorSet img3_expand = imageViewProcessExpand(imageViews[3]);
		img3_expand.setDuration(500);
		img3_expand.setStartDelay(500);
		
		//点3-->缩隐
		AnimatorSet img3_narrow = imageViewProcessNarrow(imageViews[3]);
		img3_narrow.setDuration(500);
		img3_narrow.setStartDelay(1000);
		
		//总动画
		AnimatorSet aSet = new AnimatorSet();
		aSet.playTogether(img1_expand, img1_narrow, img2_expand, img2_narrow, img3_expand, img3_narrow);
		//aSet.start();
		aSet.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				imageViews[1].setImageDrawable(getResources().getDrawable(R.drawable.vs_wake_surecord_red));
				imageViews[2].setImageDrawable(getResources().getDrawable(R.drawable.vs_wake_surecord_red));
				imageViews[3].setImageDrawable(getResources().getDrawable(R.drawable.vs_wake_surecord_red));
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				animation.start();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		return aSet;
	}
	
	
	private void soundModelCompoundSuccess(){

		//绿色圆扩大
		ValueAnimator recording_Round_Ani = ValueAnimator.ofFloat(0, 120); //正弦  
		recording_Round_Ani.setDuration(600);
		recording_Round_Ani.setInterpolator(new DecelerateInterpolator(1.5f));
		recording_Round_Ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				float value = (Float)animation.getAnimatedValue();
				float outter = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_outter);
				float center = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_center);
				double R1 = Math.sin(Math.toRadians(value)) *(outter - center) + center; //(820-360)+360
				
				//更新原图
				recording_ani.setImageBitmap(createBitmap((float)R1, /*"#019c73"*/
						TrainingActivity.this.getResources().getColor(R.color.sound_model_compound_success))); //"#019c73"
			}
		});
		//recording_Round_Ani.start();
		
		//话筒隐去
		ObjectAnimator recording_mic_Ani = ObjectAnimator.ofFloat(recording_mic, "alpha", 1.0f, 0);
		recording_mic_Ani.setDuration(300);
		recording_mic_Ani.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				recording_mic.setImageResource(R.drawable.vs_wake_record_success);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		//recording_mic_Ani.start();
		
		float outter = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_outter);
		float center = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_center);
		float corect = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_corect);
		
		//白色圈扩大
		float R2 = (float)(Math.sin(Math.toRadians(120)) *(outter-center)+center) - corect; //(820-360)+360)-10
		recording_exc.setImageBitmap(createBitmap(R2, /*"#ffffff"*/
				TrainingActivity.this.getResources().getColor(R.color.sound_model_compound_success_white))); //#ffffff
		
		ObjectAnimator recording_exc_AniX = ObjectAnimator.ofFloat(recording_exc, "scaleX", 0.0f, 1.0f);
		ObjectAnimator recording_exc_AniY = ObjectAnimator.ofFloat(recording_exc, "scaleY", 0.0f, 1.0f);
		AnimatorSet excSet = new AnimatorSet();
		excSet.playTogether(recording_exc_AniX, recording_exc_AniY);
		excSet.setDuration(600);
		excSet.setInterpolator(new DecelerateInterpolator());
		//excSet.start();
		
		//OK凸显
		ObjectAnimator recording_ok_Ani = ObjectAnimator.ofFloat(recording_mic, "alpha", 0.0f, 1.0f);
		ObjectAnimator recording_ok_AniX = ObjectAnimator.ofFloat(recording_mic, "scaleX", 0.5f, 1.0f);
		ObjectAnimator recording_ok_AniY = ObjectAnimator.ofFloat(recording_mic, "scaleY", 0.5f, 1.0f);
		AnimatorSet micSet = new AnimatorSet();
		micSet.playTogether(recording_ok_Ani, recording_ok_AniX, recording_ok_AniY);
		micSet.setStartDelay(300);
		micSet.setDuration(300);
		//micSet.start();
		
		//总Animator
		AnimatorSet aSet = new AnimatorSet();
		aSet.playTogether(recording_Round_Ani, recording_mic_Ani, excSet, micSet);
		aSet.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				//动画结束后
				TrainingActivity.this.finish();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		aSet.start();
	}
	
	private AnimatorSet soundModelCompoundFail(){

		//绿色圆扩大
		ValueAnimator recording_Round_Ani = ValueAnimator.ofFloat(0, 140); //正弦  
		recording_Round_Ani.setDuration(800);
		recording_Round_Ani.setInterpolator(new AccelerateDecelerateInterpolator());
		recording_Round_Ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				float value = (Float)animation.getAnimatedValue();
				//double R1 = Math.sin(Math.toRadians(value)) *(820 - 360) + 360; //(820-360)+360
				float outter = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_outter);
				float center = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_center);
				double R1 = Math.sin(Math.toRadians(value)) *(outter - center) + center; //(820-360)+360
				//更新原图
				recording_ani.setImageBitmap(createBitmap((float)R1, /*"#019c73"*/
						TrainingActivity.this.getResources().getColor(R.color.sound_model_compound_fail))); //#019c73 
			}
		});
		//recording_Round_Ani.start();
		
		//话筒隐去
		ObjectAnimator recording_mic_Ani = ObjectAnimator.ofFloat(recording_mic, "alpha", 1.0f, 0);
		recording_mic_Ani.setDuration(400);
		recording_mic_Ani.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				recording_mic.setImageResource(R.drawable.vs_wake_record_error);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		//recording_mic_Ani.start();
		
		float outter = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_outter);
		float center = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_center);
		float corect = TrainingActivity.this.getResources().getDimension(R.dimen.training_round_center_corect);
		
		//白色圈扩大
		float R2 = (float)(Math.sin(Math.toRadians(140)) *(outter - center) + corect); // - 10; //(820-360)+362
		recording_exc.setImageBitmap(createBitmap(R2, /*"ffffff"*/
				TrainingActivity.this.getResources().getColor(R.color.sound_model_compound_success_white)));
		ObjectAnimator recording_exc_AniX = ObjectAnimator.ofFloat(recording_exc, "scaleX", 0.0f, 1.0f);
		ObjectAnimator recording_exc_AniY = ObjectAnimator.ofFloat(recording_exc, "scaleY", 0.0f, 1.0f);
		AnimatorSet excSet = new AnimatorSet();
		excSet.playTogether(recording_exc_AniX, recording_exc_AniY);
		excSet.setDuration(800);
		excSet.setInterpolator(new AccelerateInterpolator());
		//excSet.start();
		
		//OK凸显
		AnimatorSet micSet = new AnimatorSet();
		ObjectAnimator recording_fail_Ani = ObjectAnimator.ofFloat(recording_mic, "alpha", 0.0f, 1.0f);
		ObjectAnimator recording_fail_AniX = ObjectAnimator.ofFloat(recording_mic, "scaleX", 0.5f, 1.0f);
		ObjectAnimator recording_fail_AniY = ObjectAnimator.ofFloat(recording_mic, "scaleY", 0.5f, 1.0f);
		micSet.setDuration(400);
		micSet.setStartDelay(400);
		micSet.playTogether(recording_fail_Ani,recording_fail_AniX,recording_fail_AniY);
		//micSet.start();
		
		//总Animator
		AnimatorSet aSet = new AnimatorSet();
		aSet.playTogether(recording_Round_Ani, recording_mic_Ani, excSet, micSet);
		aSet.start();
		
		return aSet;
	}
	
	private Bitmap createBitmap(float radius, int color){
		Bitmap bit = Bitmap.createBitmap((int)radius, (int)radius, Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bit);
		//canvas.drawColor(Color.parseColor("#f2f2f2")); //画布没有颜色，即透明
		
		//画笔一画绿圆
		Paint paint = new Paint();
		//paint.setColor(Color.parseColor(color)); //"#019c73" 绿色
		paint.setColor(color);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		
		canvas.drawCircle(radius/2, radius/2, radius/2, paint);
		
		
		//画笔(2)
		/*Paint paint2 = new Paint();
		paint2.setAntiAlias(true);
		paint2.setStyle(Style.FILL);
		canvas.drawCircle(radius/2, radius/2, radius/2 - 30, paint2);*/
		
		return bit;
	}
	
	private AnimatorSet imageViewProcessExpand(View view){
		AnimatorSet aSet = new AnimatorSet();
		
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1.0f);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1.5f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1.5f);
		
		aSet.playTogether(alpha, scaleX, scaleY);
		return aSet;
	}
	
	private AnimatorSet imageViewProcessNarrow(View view){
		AnimatorSet aSet = new AnimatorSet();
		
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0f);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.5f, 0f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.5f, 0f);
		
		aSet.playTogether(alpha, scaleX, scaleY);
		return aSet;
	}
	
	private AnimatorSet createImageViewsAnimatorSet(View view, float distance){
		AnimatorSet aSet = new AnimatorSet();
		ObjectAnimator trans = ObjectAnimator.ofFloat(view, "translationX", 0, distance);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f);
		aSet.playTogether(trans, alpha);
		return aSet;
	}

	
	
	//*********************************************************************************************
	private void showDialogOnBackPress(){
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(TrainingActivity.this);
	    builder.setTitle(R.string.training_dialog_close_title) //你确定要关闭
	    .setMessage(R.string.recording_onbackpress) 
	    .setCancelable(true)
	    .setNegativeButton(R.string.dialog_cancel, null)
	    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
	    	@Override
	        public void onClick(DialogInterface dialog, int which) {
	    		//删除录制的小样
	    		cancelRecording();
	    		setResult(RESULT_CANCELED);
	    		finish();
	        }
	    });
	    
	    if (false == ((Activity) TrainingActivity.this).isFinishing()) {
	    	builder.show();
	    }
	}
	
	//终止录制
	private void cancelRecording(){
        Global.getInstance().removeExistingRecordingFiles();
        Global.getInstance().removeUserRecordings();
	}

	
	private void saveSoundModel(){
		
		//目前Diolog--->换成动画
		//String pleaseWait = getString(R.string.home_dialog_pleasewait);
        //String creatingSoundModel = getString(R.string.training_dialog_createsoundmodel);
        //progressDialog = ProgressDialog.show(TrainingActivity.this, creatingSoundModel, pleaseWait);
		
		//启动合成动画
		soundCompoundAnimatorSet = SoundModelCompoundAnimatorSet();
		soundCompoundAnimatorSet.start();
		
        //发送消息合成
        sendReply(MessageType.MSG_EXTEND_SOUNDMODEL, null); //发送成功消息？？
	}
	
	private void initalizeUserInterface() {
        Log.v(TAG, "initalizeUserInterface");
        
        //uiWelcomeLayout = findViewById(R.id.layout_training_welcome);
        //uiDisableLayout = findViewById(R.id.layout_training_disableview);
        //uiUserName = (TextView)findViewById(R.id.training_username);
        
        //uiRecordLayout = findViewById(R.id.layout_training_record); //录音界面

        //uiRecordingState = (TextView)findViewById(R.id.training_recording_state);
        
        
        //uiClose = (ImageButton)findViewById(R.id.training_recording_close); //停止录影（或，再来一遍）
        
        //uiRecordingKeyword = (TextView)findViewById(R.id.training_recording_keyword); //关键字
        
        
        //uiNotice = (TextView)findViewById(R.id.training_recording_notice); //录音异常提示
        
        //uiMicLayout = (RelativeLayout)findViewById(R.id.layout_training_mic); //MIC布局
		
        //uiMic = (ImageButton)findViewById(R.id.training_mic); //mic按钮
		
        //uiContinue = (Button)findViewById(R.id.training_recording_continue); //录音成功后，保存文件

		recordingState = getString(R.string.training_recording_recording); //RECORDING...
		processingState = getString(R.string.training_recording_processing); //PROCESSING...

		//开始录音按钮（MIC图片）
		/*uiMic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                Log.v(TAG, "uiMic.onClick");
				setVisibleRecordingArea();
                startTraining();
			}
		});*/

		
		//停止录音需要做的事情（*****************************************************）
		/*uiClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    Log.v(TAG, "uiClose.onClick");
			    if (completedTraining) {
			        Log.v(TAG, "uiClose.onClick: completedTraining is true");
			        AlertDialog.Builder builder = new AlertDialog.Builder(TrainingActivity.this);
			        builder.setTitle(R.string.training_dialog_close_title)
			        .setMessage(R.string.training_dialog_close_message)
			        .setCancelable(false)
			        .setNegativeButton(R.string.dialog_cancel, null)
			        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			                resetTrainings();
                            closeRecordingWindow();
			                Log.v(TAG, "uiClose.onClick: completedTraining set to false b/c user clicked OK");
			            }
			        });

			        if (false == ((Activity) TrainingActivity.this).isFinishing()) {
			            builder.show();
			        }
			    }

			    if (completedTraining == false) {
                    Log.v(TAG, "uiClose.onClick: completedTraining is false");
			        closeRecordingWindow();
			        Log.v(TAG, "uiClose.onClick: getNumUserRecordings: "
			                + Global.getInstance().getNumUserRecordings());
			    }
			}

            private void closeRecordingWindow() {
                setVisibleTrainingMainArea();
                stopTraining();
                stopTrainingTimer();
            }
		});
		*/

		//uiUserName.setText(trainingUserName);
		//uiRecordingKeyword.setText(trainingKeyword); //关键字

		uiContinue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        Log.v(TAG, "uiContinue.onClick");
		        String pleaseWait = getString(R.string.home_dialog_pleasewait);
		        String creatingSoundModel = getString(R.string.training_dialog_createsoundmodel);

                progressDialog = ProgressDialog.show(TrainingActivity.this, creatingSoundModel, pleaseWait);
                Log.v(TAG, "uiContinue.onClick: progress dialog started");
                sendReply(MessageType.MSG_EXTEND_SOUNDMODEL, null); //发送成功消息？？
			}
		});
	}

	//重新录制，将之前录制的文件删除
	private void resetTrainings() {
        Global.getInstance().removeExistingRecordingFiles();
        Global.getInstance().removeUserRecordings();
        try {
            recordingCounter = new RecordingCounter(TrainingActivity.this, imageViews, recording_ani);
        } catch (Exception e) {
            e.printStackTrace();
        }
        completedTraining = false;
    }

    private void setVisibleRecordingArea() {
        Log.e(MYTAG, "显示录音界面...");
    	
        
        //uiWelcomeLayout.setEnabled(false);
        layout_training_tips.setVisibility(View.INVISIBLE);
        //uiRecordLayout.setVisibility(View.VISIBLE);
        layout_training_record.setVisibility(View.VISIBLE);

        training_recording_notice.setVisibility(View.INVISIBLE);
        
        //training_recording_continue.setVisibility(View.INVISIBLE);
        
        fail_to_recording_again.setVisibility(View.INVISIBLE);
        
        //recording.setVisibility(View.VISIBLE);
        //continue_or_save.setVisibility(View.VISIBLE);
        
        //uiDisableLayout.setVisibility(View.VISIBLE);
        
        //uiRecordingState.setText(recordingState);
        
        //uiNotice.setVisibility(View.INVISIBLE); //录音异常提示
        
        //uiContinue.setVisibility(View.INVISIBLE); //录音成功后保存文件
        
        //录音按钮
        //uiMic.setImageResource(R.drawable.speaker_on);
        //uiMic.setClickable(false);
    }

    private void setVisibleTrainingMainArea() {
        Log.v(TAG, "setVisibleTrainingMain");
        //uiWelcomeLayout.setEnabled(true);
        layout_training_tips.setVisibility(View.VISIBLE);
        //uiRecordLayout.setVisibility(View.INVISIBLE);
        layout_training_record.setVisibility(View.INVISIBLE);

        //uiDisableLayout.setVisibility(View.INVISIBLE);

        //录音按钮
        //uiMic.setImageResource(R.drawable.speaker_off);
        //uiMic.setClickable(true);
        
        //uiMicLayout.setVisibility(View.VISIBLE); //mic布局
    }

	// Send back to SoundModelsActivity whether training completed successfully
	@Override
	protected void onPause() {
		Log.v(MYTAG, "onPause.........TraningActivity");
        if (null != recorder) {
            stopTraining();
            stopTrainingTimer();
        }
        
        if(!completedTraining){
            if(recordingCounter != null){
            	fail_to_recording_again.setVisibility(View.VISIBLE);
            	fail_to_recording_again.setText(R.string.stop_to_recording_again); 
            	if(training_recording_notice.getVisibility() == View.VISIBLE){
            		training_recording_notice.setVisibility(View.INVISIBLE);
            	}
            	recordingCounter.stopTheAnimation();
            }
        }
        
		/*if (completedTraining) {
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
			Global.getInstance().unselectAndUnsaveSoundModel(TrainingActivity.this); //失败则注销模板
		}*/
		super.onPause();
	}

	@Override
	public void onBackPressed() {
        Log.v(MYTAG, "onBackPressed......TraningActivity-------->"+isCompound());
        //关闭之前，询问
        if(!isCompound()){
            if(trainingUserName != null && !trainingUserName.equals("")){
            	showDialogOnBackPress();
            }else{
            	finish();
            }
        }
		//super.onBackPressed();
	}

    @Override
    protected void onDestroy() {  
        Log.v(MYTAG, "onDestroy...........................................TrainingActivity()");
        super.onDestroy();
        sendReply(MessageType.MSG_UNREGISTER_CLIENT, null);
        if (null != sendToServiceMessenger) {
            unbindService(mConnection);
        }
    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.v(TAG, "uiHandler");
            if (MSG_STOP_TRAINING_RECORDING == msg.what) {
                stopTraining();
                sendReply(MessageType.MSG_VERIFY_RECORDING, null); //停止录音，发送消息至“服务”验证
            }
        };
    };

	// Resets keyword and user detection bars after SHOW_DETECTION_RESULT_LENGTH milliseconds
    private void startTrainingTimer() {
        Log.v(TAG, "startTrainingTimer");
        stopTrainingTimer();

        trainingTimer = new Timer();
        trainingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                uiHandler.sendEmptyMessage(MSG_STOP_TRAINING_RECORDING);
            }
        }, TRAINING_RECORDING_DURATION);
    }

    private void stopTrainingTimer() {
        Log.v(TAG, "stopTrainingTimer");
        if(null != trainingTimer) {
            trainingTimer.cancel();
            trainingTimer = null;
        }
    }

	private void startTraining() {
        Log.e(MYTAG, "start training......");
        recordingCounter.startRecordingAnimation();
        recorder = ListenAudioRecorder.getInstance();
        recorder.start();
        startTrainingTimer();
        
        //uiRecordingState.setText(recordingState);
        
        //正在录音的状态
        //uiMic.setImageResource(R.drawable.speaker_on);
        //Log.v(TAG, "startTraining: recorder.start()");

	}

	private void stopTraining() {
        Log.e(MYTAG, "stop training....");
        recorder.stop();
        
        //uiRecordingState.setText(processingState);
        
        //录音话筒的状态
        //uiMic.setImageResource(R.drawable.speaker_off);
        //Log.v(TAG, "stopTraining: recorder.stop");
	}

	// Updates UI and sends a message to the Service to extend the sound model.
	private void finishTraining() {
        Log.v(MYTAG, "finishTraining............SUCCCESS??");
        //uiMicLayout.setVisibility(View.INVISIBLE); //MIC布局
		//uiNotice.setVisibility(View.INVISIBLE); //录音异常提示
		//uiContinue.setVisibility(View.VISIBLE); //录音成功后，保存文件
        
        //recording.setVisibility(View.INVISIBLE);
        
        //成功后,显示的完成
        training_recording_notice.setVisibility(View.INVISIBLE);
        //training_recording_notice.setTextColor(getResources().getColor(R.color.bg_recording_good));
        //training_recording_notice.setText(R.string.training_recording_complete);
        
        //training_recording_continue.setVisibility(View.VISIBLE);
        //training_recording_continue.setText(R.string.saving);
        
        //continue_or_save.setVisibility(View.VISIBLE);
        //continue_or_save.setText(R.string.saving);
        
        Log.v(MYTAG, "*****************************************************************save_file_start");
        setCompound(true);
        enRecordedAniDowntoUp();
        //直接保存文件
        //saveSoundModel();
        
		//String recordingState = getString(R.string.training_recording_complete);
		//uiRecordingState.setText(recordingState);
	}


	// Sets up service connection
	private ServiceConnection mConnection = new ServiceConnection() {

	    // Registers as a client to receive messages from the Service
		public void onServiceConnected(ComponentName name, IBinder service) {
	        Log.v(TAG, "onServiceConnected");
			sendToServiceMessenger = new Messenger(service);

			// Registers client
			sendReply(MessageType.MSG_REGISTER_CLIENT, null);  //注册接收消息的客户端
			Log.v(TAG, "connected service");
		}

		// Unregisters as a client to receive messages from the Service
		public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "onServiceDisconnected");
			sendReply(MessageType.MSG_UNREGISTER_CLIENT, null);
			sendToServiceMessenger = null;
			Log.v(TAG, "disconnected service");
		}
	};

	// Handles incoming messages from the Service
	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
            Log.v(TAG, "handleMessage");
			switch(msg.what) {
			    case MessageType.MSG_RECORDING_RESULT: //客户说一段，经过service验证之后
                    Log.v(MYTAG, "handleMessage: MSG_RECORDING_RESULT......处理录音结果");
                    updateTraining(checkTrainingSuccess(msg.arg1));
                    break;

                //创建新声纹文件，返回结果
			    case MessageType.MSG_EXTEND_SOUNDMODEL:
			    	
                    Log.v(MYTAG, "handleMessage: MSG_EXTEND_SOUNDMODEL...........del the reback infromation");
                    
                    /*if (progressDialog != null && progressDialog.isShowing()) {
                    	progressDialog.dismiss();
                        Log.v(MYTAG, "handleMessage: MSG_EXTEND_SOUNDMODEL: progress dialog dismissed");
                    }*/
                    
                    if(soundCompoundAnimatorSet != null && soundCompoundAnimatorSet.isRunning()){
                    	soundCompoundAnimatorSet.end();
                    	soundCompoundAnimatorSet.cancel();
                    }
                    setCompound(false);

                    if (msg.arg1 == Global.SUCCESS) { //成功
                    	Log.v(MYTAG, "*****************************************************************save_file_end");
                    	
                    	soundModelCompoundSuccess();
                    	setResult(RESULT_OK);
                        
                    	//此时可以返回
                        setCompound(true);
                    	
                        //文件保存成功
                        //training_recording_continue.setText(R.string.saved);
                        //continue_or_save.setText(R.string.saved);
                        
                        recording_result.setText(R.string.vs_wakeup_recording_success);
                        
                        //保存刚刚录制的文件
                        //finish(); //结束后，即返回
                        
                        
                    } else if (msg.arg1 == Global.FAILURE) { //失败
                    	Log.v(MYTAG, "handleMessage: MSG_EXTEND_SOUNDMODEL failed");
                    	
                    	soundModelCompoundFail();

                    	recording_result.setText(R.string.vs_wakeup_recording_fail);
                    	compound_fail_to_again.setVisibility(View.VISIBLE);
                    	
                    	/*
                        AlertDialog.Builder builder = new AlertDialog.Builder(TrainingActivity.this);
                        builder.setTitle(R.string.training_dialog_smcreatefailed_title)
                        .setMessage(R.string.training_dialog_smcreatefailed_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.v(TAG, "handleMessage: MSG_EXTEND_SOUNDMODEL failed: alert- user selected OK");
                                resetTrainings();
                                setVisibleRecordingArea();
                                //uiMicLayout.setVisibility(View.VISIBLE); //MIC布局
                                startTraining();
                            }
                        });
                        if (false == ((Activity) TrainingActivity.this).isFinishing()) {
                            builder.show();
                        }
                        */
                        
                    } else {
                        Log.e(TAG, "handleMessage: MSG_EXTEND_SOUNDMODEL: unknown message= " + msg.arg1);
                    }
                    break;

                default:
                   Log.v(TAG, "handleMessage: no such case: " + msg.what);
			}
		}

		//根据录音结果的水平，判断是否录音成功
		private Boolean checkTrainingSuccess(int confidenceLevel) {
            Log.v(MYTAG, "checkTrainingSuccess: confidence level = " + confidenceLevel+"-----------根据录音水平决定，录音成败");
            return (confidenceLevel > TRAINING_CONFIDENCE_LEVEL_THRESHOLD) ? true : false; //72标准
		}

		private void updateTraining(Boolean trainingSuccessful) {
            Log.v(MYTAG, "updateTraining............更新录音");
            Log.v(MYTAG, "updateTraining: trainingSuccessful = " + trainingSuccessful);
	        
            //uiNotice.setVisibility(View.VISIBLE); //录音异常提示
            training_recording_notice.setVisibility(View.VISIBLE);
            
	        recordingCounter.updateRecordingResult(trainingSuccessful);

	        if (trainingSuccessful) {
	            // start new recording
	            Log.v(MYTAG, "录音正常，请继续...........train speech is ACCEPTED\n");
	            
	            //录音正常提示（******************************************************************）
	            //uiNotice.setTextColor(getResources().getColor(R.color.bg_recording_good));
	            //uiNotice.setText(R.string.training_recording_notice_good);
	            
	            training_recording_notice.setVisibility(View.INVISIBLE);
	            //training_recording_notice.setTextColor(getResources().getColor(R.color.bg_recording_good));
	            //training_recording_notice.setText(R.string.training_recording_notice_good);
	            
	        } else {
	            Log.v(TAG, "train speech is REJECTED\n");
	            
	            //录音异常提示（******************************************************************）
	            //uiNotice.setTextColor(getResources().getColor(R.color.bg_recording_bad));
	            //uiNotice.setText(R.string.training_recording_notice_bad);
	            training_recording_notice.setTextColor(getResources().getColor(R.color.bg_recording_bad));
	            training_recording_notice.setText(R.string.training_recording_error);
	            
	            //再来一遍
	            //recording.setVisibility(View.INVISIBLE);
	            compound_fail_to_again.setVisibility(View.INVISIBLE);
	            fail_to_recording_again.setVisibility(View.VISIBLE);
	            fail_to_recording_again.setText(R.string.fail_to_recording_again); 
	        }
	        
	        //************进行下一轮录音*****************
	        if (recordingCounter.isFinished()) {
                completedTraining = true;
	            finishTraining();
	        } else {
	            if(trainingSuccessful){ //如果录制成功则继续
	            	startTraining();
	            }
	        }
	        //enRecordedAniDowntoUp();
        }
	};

	private final Messenger mMessenger = new Messenger(mHandler);

	// Sends messages to the Service
	private void sendReply(int what, Object obj) {
        Log.v(TAG, "sendReply");
		if (null == sendToServiceMessenger) {
			return;
		}

		Message msg = Message.obtain(null, what, obj);
		msg.replyTo = mMessenger;
		try {
			sendToServiceMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public boolean isCompound() {
		return isCompound;
	}

	public void setCompound(boolean isCompound) {
		this.isCompound = isCompound;
	}

}
