package com.aurora.voiceassistant.view;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import com.aurora.voiceassistant.model.CFG;
import com.aurora.voiceassistant.model.Event;
import com.aurora.voiceassistant.model.LoadData;
import com.aurora.voiceassistant.model.Parse;
import com.aurora.voiceassistant.model.QuizText;
import com.aurora.voiceassistant.model.Response;
import com.aurora.voiceassistant.model.RspJson;
import com.aurora.voiceassistant.model.RspJsonReminderAlarm;
import com.aurora.voiceassistant.model.RspText;
import com.aurora.voiceassistant.model.TtsRes;
import com.aurora.voiceassistant.model.LoadData.Item;
import com.sogou.speech.listener.RecognizerListener;
import com.wowenwen.yy.api.RequestListener;
import com.wowenwen.yy.api.SogouYYSDK;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import com.aurora.voiceassistant.R;
//shigq
import aurora.widget.AuroraListView;

public class VoiceAssistant 
{
	private Context context;
	//private ProgressDialog proDialog =null;
	private LoadData loadData;
	private String quizStr;
	private Handler handler;
	private ArrayList<String> bdArray  = new ArrayList<String>();
	private ArrayList<String> morArray  = new ArrayList<String>();
//	private ArrayList<String> amArray  = new ArrayList<String>();
	private ArrayList<String> midArray = new ArrayList<String>();
	private ArrayList<String> pmArray  = new ArrayList<String>();
	private ArrayList<String> nigArray = new ArrayList<String>();
	private ArrayList<String> otherArray = new ArrayList<String>();
    
    //nlp
  	final String key = "17D0F8E22A6D2298A84E7A5655822936";
  	private RequestListener reqListener = null;
  	private SogouYYSDK yySDK = null;

  	//private boolean isInited = false;
  	private boolean isBeforeDawn = false;
  	private boolean isMorning = false;
  	private boolean isMiddle = false;
  	private boolean isAfternoon = false;
  	private boolean isNight = false;
  	
  	//miche
  	private NetworkInfo netinfo;
  	private SharedPreferences pre;
  	private boolean home_stop_search = false;

  	//For scrollView
  	private LayoutManager mLayoutManager;
//  	private ContentViewConstructor mViewConstructor;
  	//For scrollView
  	
  	private MainActivity mainActivity;
  	
  	public VoiceAssistant(Context context,Handler handler) {
		this.context = context;
		this.handler = handler;
	}
  	
  	public void getMainActivity(MainActivity activity) {
  		mainActivity = activity;
  	}
  	
  	//For scrollView
  	public void yySDKInit(boolean isFirst) {
  		
  		loadData = new LoadData();
  		
  		yySDK = new SogouYYSDK(context, key, 1);
        yySDK.initSogouYYSDK();  
//        yySDK.enableSdkLbs(true);         
    	yySDK.uploadCustomizedData();

        reqListener = new RequestListener() {
    		@Override
    		public void onRequestEnd() {
    			if(0 != yySDK.getResponseState()) {
    				return ;
    			}
    			
    			Thread parseThread = new Thread(new parseThread(yySDK.getResponseJson()));
    			parseThread.start();
    		}
    		
    		@Override
    		public void onRequestCancel() {
    			Thread thread = new Thread(new searchErrorNotifThread(-2));
    			thread.start();
    		}
    		
    		@Override
    		public void onRequestError(Exception arg0) {
    			Thread thread = new Thread(new searchErrorNotifThread(-1));
    			thread.start();
    		}
    	};
    	
    	pushGreeting(isFirst);
  	}
  	//For scrollView
	
	public void destory() {
		if(null != yySDK) {
			 yySDK.finalSogouYYSDK();
			 yySDK = null;
		}
	}
	
	//stop
	public void stopLocationService(){
		if(yySDK != null){
			yySDK.enableSdkLbs(false);
		}
	}
	
	public void startLocationService(){
		if(yySDK != null){
			yySDK.enableSdkLbs(true);
		}
	}
	
	public void requestText(String content,ProgressDialog dialog) {
		if(null == yySDK) return ;
		setStatusBarBG(true);
		if(null != content  && content.length() > 0 ) {
			//proDialog = dialog;
			quizStr = content.trim();
			yySDK.sendContentRequest(quizStr, true, false, reqListener);
			
			//if(null != proDialog) {
			//	proDialog.show();
			//}
		}
	}	
	
	private class parseThread implements Runnable {
		private JSONObject jsonData;
		
		parseThread(JSONObject data) {
			jsonData = data;
		}
		
		@Override
		public void run() {
			Message msg= Message.obtain();
			
			Parse pase = new Parse(jsonData);
			Response response = pase.execu();

			// No net
			if(null == response) {
//				loadData.listItemHidden();
				
				//add quiz
				QuizText quiz  = new QuizText();
        		quiz.setQuiz(quizStr);
        		loadData.listAdd(CFG.VIEW_TYPE_QUIZ, quiz, null);
        		
        		//add rspNode
     			RspText rspText = new RspText();
    		 	rspText.setAnswer(context.getResources().getString(R.string.vs_error_search));
    		 	
    		 	Response.Item item = new Response().new Item();
    		 	item.setRestext(rspText);
    		 	item.setType(CFG.VIEW_TYPE_TEXT); 
    		 	
    		 	Response rsp = new Response();
    		 	rsp.setFirstNodeType(CFG.VIEW_TYPE_TEXT);
    		 	rsp.listAdd(item);	        		 	
     			loadData.listAdd(CFG.VIEW_TYPE_RES, null, rsp);
     			
     			TtsRes  ttsRes = new TtsRes();
     			ttsRes.flag = true;
     			ttsRes.text = rspText.getAnswer();

     			rsp.setRequestString(quizStr);
     			rsp.setAnswerString(rspText.getAnswer());
     			Log.d("DEBUG", "null == response get ----------the quizStr = "+rsp.getRequestString());
     			Log.d("DEBUG", "null == response get -----------answer = "+rsp.getAnswerString());
     			
				msg.what = 0;
				msg.obj = ttsRes;
				handler.sendMessage(msg);
			} else {
//				loadData.listItemHidden();
				
				TtsRes  ttsRes = new TtsRes();
     			ttsRes.flag = false;
     			
				//add quiz
        		QuizText quiz  = new QuizText();
	        	quiz.setQuiz(quizStr);
        		loadData.listAdd(CFG.VIEW_TYPE_QUIZ, quiz, null);
        		
        		//add rsp search_content
        		String searchContent = response.getSearchContent();
        		Log.d("DEBUG", "==========================================searchContent "+searchContent);
//				if (searchContent == null && CFG.VIEW_TYPE_TEXT != response.getFirstNodeType()) {
				if (searchContent == null && CFG.VIEW_TYPE_TEXT != response.getFirstNodeType() && 
											 CFG.VIEW_TYPE_JOSN != response.getFirstNodeType()) {
					searchContent = mainActivity.getSearchContent();
				}
        		
        		if(null != searchContent && 0 != searchContent.length()) {
        			RspText rspText = new RspText();
        		 	rspText.setAnswer(context.getResources().getString(R.string.vs_searchresult)+searchContent);
        		 	
        		 	Response.Item item = new Response().new Item();
        		 	item.setRestext(rspText);
        		 	item.setType(CFG.VIEW_TYPE_TEXT);
        		 	
        		 	Response rsp = new Response();
        		 	rsp.setFirstNodeType(CFG.VIEW_TYPE_TEXT);
        		 	rsp.listAdd(item);
	     			
	     			loadData.listAdd(CFG.VIEW_TYPE_RES, null, rsp);
	     			
	     			ttsRes.flag = true;
	     			ttsRes.text = rspText.getAnswer();

	     			Log.d("DEBUG", "null != response & null != searchContent the quizStr = "+quizStr);
	     			Log.d("DEBUG", "null != response & null != searchContent answer = "+rspText.getAnswer());
	     			
        		} else {
        			if(CFG.VIEW_TYPE_TEXT == response.getFirstNodeType()) {
             			Log.d("DEBUG", "null != response CFG.VIEW_TYPE_TEXT the quizStr = "+quizStr);
             			Log.d("DEBUG", "null != response CFG.VIEW_TYPE_TEXT the answer = "+response.getList().get(0).getRestext().getAnswer());
        				
        				ttsRes.flag = true;
    	     			ttsRes.text = response.getList().get(0).getRestext().getAnswer();
    	     			
        			} else if (CFG.VIEW_TYPE_JOSN == response.getFirstNodeType()) {
        				Log.d("DEBUG", "null != response CFG.VIEW_TYPE_JOSN the quizStr = "+quizStr);
        				
        				Response.Item item = response.getList().get(0);
        				RspJson rspJson = item.getRspjson();
        				JSONObject jsonObjectData = rspJson.getJSONObjectData();
        				
        				String description = jsonObjectData.optString("description");
        				Log.d("DEBUG", "null != response CFG.VIEW_TYPE_JOSN the description = "+description);

    					if (CFG.DESCRIPTION_TYPE_REMIND_ALARM.equals(description)) {
    						String cmd = jsonObjectData.optString(CFG.CMD);
            				if (cmd != null) {
            					if ("add".equals(cmd)) {
            						ttsRes.text = context.getResources().getString(R.string.vs_reminder_alarm_answer);
            						
        						} else if ("view".equals(cmd)) {
        							RspJsonReminderAlarm mReminderAlarm = new RspJsonReminderAlarm(context, jsonObjectData);
        							ArrayList<Event> mEventList = mainActivity.getEventArrayList();
        							mEventList.clear();
        							
    								Time mTime = new Time();
    								mTime.year = mReminderAlarm.getReminderDateYear();
    								mTime.month = mReminderAlarm.getReminderDateMonth();
    								mTime.monthDay = mReminderAlarm.getReminderDateDay();
    								mTime.normalize(true);
    								Long timeLong = mTime.toMillis(true);
    								
    								int time = mTime.getJulianDay(timeLong, mTime.gmtoff);
    								int duration = mReminderAlarm.getReminderDaysDuration();
    								
    								Event.loadEvents(context, mEventList, time, duration);
    								
    								String tempDay = null;
    								String tempDayOfWeek = null;
    								if ("0".equals(mReminderAlarm.getDateType())) {
										switch (mReminderAlarm.getReminderDateDayOfWeek()) {
										case 1:
											tempDayOfWeek = " 周日 ";
											break;
										case 2:
											tempDayOfWeek = " 周一 "	;
											break;
										case 3:
											tempDayOfWeek = " 周二 ";
											break;
										case 4:
											tempDayOfWeek = " 周三 ";
											break;
										case 5:
											tempDayOfWeek = " 周四 ";
											break;
										case 6:
											tempDayOfWeek = " 周五 ";
											break;
										case 7:
											tempDayOfWeek = " 周六 ";
											break;
										}
										
									} else	if ("1".equals(mReminderAlarm.getDateType())) {
										if (quizStr.contains("今天")) {
											tempDay = "今天";
										} else if (quizStr.contains("今日")) {
											tempDay = "今日";
										} else if (quizStr.contains("明天")) {
											tempDay = "明天";
										} else if (quizStr.contains("明日")) {
											tempDay = "明日";
										} else if (quizStr.contains("本周")) {
											tempDay = "本周";
										} else if (quizStr.contains("下周")) {
											tempDay = "下周";
										} else if (quizStr.contains("本月")) {
											tempDay = "本月";
										}
									}
    								
    								if (mEventList.size() <= 0) {
    									String answerString = mainActivity.getResources().getString(R.string.vs_reminder_alarm_no_reminder_found);
    									if (tempDay != null) {
											ttsRes.text = tempDay + answerString;
										} else {
											ttsRes.text = mReminderAlarm.getReminderDateMonth() + 1 + "月" + 
														  mReminderAlarm.getReminderDateDay() + "日" + tempDayOfWeek + answerString;
										}
    					     			
    								} else {
    									if (tempDay != null) {
											ttsRes.text = tempDay + "有" + mEventList.size() + "个提醒";
										} else {
											ttsRes.text = mReminderAlarm.getReminderDateMonth() + 1 + "月" + 
													  mReminderAlarm.getReminderDateDay() + "日" + tempDayOfWeek + "有" + mEventList.size() + "个提醒";
										}
    								}
    								
    							}
            				}
    					}
        				
					}
        		}
        		
        		//add rsp rspNode
     			loadData.listAdd(CFG.VIEW_TYPE_RES, null, response);
     			Log.d("DEBUG", "response.setRequestString = "+quizStr);
     			Log.d("DEBUG", "response.setAnswerString = "+ttsRes.text);
     			response.setRequestString(quizStr);
     			response.setAnswerString(ttsRes.text);
     			
				msg.what = 0;
				msg.obj = ttsRes;
				handler.sendMessage(msg);
			}
		}
	}
	
	private class searchErrorNotifThread implements Runnable {
		private int type;
		
		searchErrorNotifThread(int errType) {
			type = errType;
		}
		@Override
		public void run() {
			Log.d("DEBUG", "searchErrorNotifThread------------run()");
			/*if(isHome_stop_search()){
				Log.d("DEBUG", "searchErrorNotifThread-----------isHome_stop_search()");
				setHome_stop_search(false);
				
			}else{*/
			TtsRes  ttsRes = new TtsRes();
 			ttsRes.flag = true;
 			
			if(-1 == type) {
				Log.d("DEBUG", "searchErrorNotifThread-----------run()-------------type = -1");
				loadData.listItemHidden();
				
				//add quiz
				QuizText quiz  = new QuizText();
        		quiz.setQuiz(quizStr);
        		loadData.listAdd(CFG.VIEW_TYPE_QUIZ, quiz, null);
        		
        		//add rspNode
     			RspText rspText = new RspText();
    		 	
     			ConnectivityManager conMng = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
     			netinfo = conMng.getActiveNetworkInfo();
				boolean isdelay = false;
     			if(netinfo != null && netinfo.isAvailable()){
     				rspText.setAnswer(context.getResources().getString(R.string.vs_net_available)); //当前网络环境欠佳
     			}else{
     				rspText.setAnswer(context.getResources().getString(R.string.vs_net_error)); //当前网络不可用
					isdelay = true;
     			}
    		 	
    		 	Response.Item item = new Response().new Item();
    		 	item.setRestext(rspText);
    		 	item.setType(CFG.VIEW_TYPE_TEXT); 
    		 	
    		 	Response rsp = new Response();
    		 	rsp.setFirstNodeType(CFG.VIEW_TYPE_TEXT);
    		 	rsp.listAdd(item);	        		 	
     			loadData.listAdd(CFG.VIEW_TYPE_RES, null, rsp);
     			
     			
     			ttsRes.text = rspText.getAnswer();
     			
     			//For scrollView
     			rsp.setRequestString(quizStr);
     			rsp.setAnswerString(ttsRes.text);
     			//For scrollView
     			
				Message msg= Message.obtain();
				msg.what = 0;
				msg.obj = ttsRes;
					
				if(isdelay) {
					handler.sendMessageDelayed(msg, 100);
				} else {
					handler.sendMessage(msg);
				}
				
			} else if(-2 == type) {
				Log.d("DEBUG", "searchErrorNotifThread-----------run()-------------type = -2");
				loadData.listItemHidden();
				
				//add quiz
				QuizText quiz  =new QuizText();
        		quiz.setQuiz(quizStr);
        		loadData.listAdd(CFG.VIEW_TYPE_QUIZ, quiz, null);
        		
				
        		//add rspNode
				RspText rspText = new RspText();
    		 	rspText.setAnswer(context.getResources().getString(R.string.vs_cancel_search)); //您已取消了搜索
    		 	
    		 	Response.Item item = new Response().new Item();
    		 	item.setRestext(rspText);
    		 	item.setType(CFG.VIEW_TYPE_TEXT); 
    		 	
    		 	Response rsp = new Response();
    		 	rsp.setFirstNodeType(CFG.VIEW_TYPE_TEXT);
    		 	rsp.listAdd(item);	        		 	
     			
     			loadData.listAdd(CFG.VIEW_TYPE_RES, null, rsp);
     			ttsRes.text = rspText.getAnswer();
     			
     			rsp.setRequestString(quizStr);
     			rsp.setAnswerString(ttsRes.text);
     			
     			Message msg= Message.obtain();
    			msg.what = 0;
    			msg.obj = ttsRes;
    			handler.sendMessage(msg);
			}
						
			/*Message msg= Message.obtain();
			msg.what = 0;
			msg.obj = ttsRes;
			handler.sendMessage(msg);*/
//			}
		}
	}
	
	private RecognizerListener listener = new RecognizerListener() {

        @Override
        // call it when the last package has received valid results
        public void onResults(List<List<String>> results) {
            showResults(results);
        }

        @Override
        // call it when some error occurs
        public void onError(int err) {
            // TODO: show error code for users when necessary
        }

        @Override
        // call it when the former packages have received valid results
        public void onPartResults(List<List<String>> results) {
            //addToWholeResult(results);
            showResults(results);
        }

        @Override
        // call it when the last package has no valid result but the former
        // packages have some valid results
        public void onQuitQuietly(int err) {
            // TODO: this is not an error, no need to show it for users
        }
    };
    
    // show wholeResult for users when necessary
    private void showResults(List<List<String>> results) {
        String result = "";
        for (List<String> list : results) {
        	 result += list.get(0);
        }
        if(result.length()>0) {
        	
        	//if(null != proDialog) {
			//	proDialog.show();
			//}
        	
        	quizStr = result;
        	yySDK.sendContentRequest(result, true, false, reqListener);
        }
    }
    
    private void pushGreeting(boolean isFisrt) {
		 String[] bdGreeting = {"Hi 凌晨好"};//00:00-6:00		//add"凌晨好！"
	  	 String[] morGreeting = {"Hi 早上好"};//6:00-11:00		//change the time for morning, change 9:00 as 11:00
//	  	 String[] amGreeting  = {};//9:00-12:00
	  	 String[] midGreeting = {"Hi 中午好"};//11:00-13:00	//change the greeting and time for noon
	  	 String[] pmGreeting  = {"Hi 下午好"};//13:00-18:00	//add greeting and change the time for afternoon
	  	 String[] nigGreeting = {"Hi 晚上好"};//18:00-00:00		//change the greeting for night
	  	 String[] othGreeting = {"我一直在等你。",
			  				"想我做些什么？尽管吩咐！",
			  				"你是我独一无二的，随时找我，我永远会在。",
			  				"众里寻他千百度，蓦然低头，我就在你手心处。",
			  				"你再不找我，我都要认不出你的声音了！",
			  				"三人行，必有我师，别害羞尽管问吧~",
			  				"一时不见，如隔三秋。",
			  				"想我了吧？说吧，什么事？",
			  				"悄悄告诉你，今天好想你。",
			  				"天气那么好，是不是来叫我出去玩了？",
			  				"爱你不是两三天，你怎么才找我一遍。",
			  				"你怎么才来呀，我好想你呀！"};
	  	 
	  	int index = 0;
	  	/*while(index < othGreeting.length) {
	  		bdArray.add(othGreeting[index]);
	  		morArray.add(othGreeting[index]);
//	  		amArray.add(othGreeting[index]);
	  		midArray.add(othGreeting[index]);
	  		pmArray.add(othGreeting[index]);
	  		nigArray.add(othGreeting[index]);
	  		otherArray.add(othGreeting[index]);
	  		
	  		index++;
	  	}*/
	  	
	  	index = 0;
	  	while(index < bdGreeting.length) {
	  		bdArray.add(bdGreeting[index]);
	  		index++;
	  	}
	  	index = 0;
	  	while(index < morGreeting.length) {
	  		morArray.add(morGreeting[index]);
	  		index++;
	  	}
	  	/*index = 0;
	  	while(index < amGreeting.length) {
	  		amArray.add(amGreeting[index]);
	  		index++;
	  	}*/
	  	index = 0;
	  	while(index < midGreeting.length) {
	  		midArray.add(midGreeting[index]);
	  		index++;
	  	}
	  	index = 0;
	  	while(index < pmGreeting.length) {
	  		pmArray.add(pmGreeting[index]);
	  		index++;
	  	}
	  	index = 0;
	  	while(index < nigGreeting.length) {
	  		nigArray.add(nigGreeting[index]);
	  		index++;
	  	}
	  	
	  	new Thread(new GetDateRunable(isFisrt)).start();
  	}
    
    class GetDateRunable implements Runnable {
    	boolean bool;
    	public GetDateRunable(boolean isFirst){
    		bool = isFirst;
    	}
    	
		@Override
		public void run() {
			String greeting  = null;
			Date   curDate   =   new   Date(System.currentTimeMillis());
			int hour = curDate.getHours();
			int rand = 0 ;
			
			if(hour < 6) {
				rand = (int)(Math.random()*(bdArray.size()-1));
				greeting = bdArray.get(rand);
			} else if(hour >= 6 && hour < 11) {
				rand = (int)(Math.random()*(morArray.size()-1));
				greeting = morArray.get(rand);
			} else if(hour >= 11 && hour < 13) {
				rand = (int)(Math.random()*(midArray.size()-1));
				greeting = midArray.get(rand);
			} else if(hour >= 13 && hour < 18) {
				rand = (int)(Math.random()*(pmArray.size()-1));
				greeting = pmArray.get(rand);
			} else {
				rand = (int)(Math.random()*(nigArray.size()-1));
				greeting = nigArray.get(rand);
			}
			
			if(pre == null){
				pre = context.getSharedPreferences("vswitch", Context.MODE_PRIVATE);
			}
			boolean init_bool = pre.getBoolean("initvoice", false);
			init_bool = true;
			
			if(!init_bool){
				//greeting = context.getResources().getString(R.string.vs_init_error);
				//isInited = false;
			}else{
				//isInited = true; //初始化成功
				
				//执行问候
				Date date = new Date(System.currentTimeMillis());
				int hours = date.getHours();
				if (hour < 6) {
					//The time is before dawn
					if (!isBeforeDawn) {
						isBeforeDawn = true;
					}
					isMorning = false;
					isMiddle = false;
					isAfternoon = false;
					isNight = false;
				} else if (hour >= 6 && hour < 11) {
					if (!isMorning) {
						isMorning = true;
					}
					isBeforeDawn = false;
					isMiddle = false;
					isAfternoon = false;
					isNight = false;
				} else if (hour >= 11 && hour < 13) {
					if (!isMiddle) {
						isMiddle = true;
					}
					isBeforeDawn = false;
					isMorning = false;
					isAfternoon = false;
					isNight = false;
				} else if (hour >= 13 && hour < 18) {
					if (!isAfternoon) {
						isAfternoon = true;
					}
					isBeforeDawn = false;
					isMorning = false;
					isMiddle = false;
					isNight = false;
				} else if ((hour >= 18)) {
					if (!isNight) {
						isNight = true;
					}
					isBeforeDawn = false;
					isMorning = false;
					isMiddle = false;
					isAfternoon = false;
				}
			}
			
			loadData.listItemHidden();
			
			//add rspNode
			RspText rspText = new RspText();
		 	rspText.setAnswer(greeting);
		 	
		 	Response.Item item = new Response().new Item();
		 	item.setRestext(rspText);
		 	item.setType(CFG.VIEW_TYPE_TEXT); 
		 	
		 	Response rsp = new Response();
		 	rsp.setFirstNodeType(CFG.VIEW_TYPE_TEXT);
		 	rsp.listAdd(item);	        		 	
			loadData.listAdd(CFG.VIEW_TYPE_RES, null, rsp);
			
			rsp.setRequestString(null);
			rsp.setAnswerString(rspText.getAnswer());
			
			Message msg= Message.obtain();
			
			TtsRes  ttsRes = new TtsRes();
			ttsRes.flag = true;
			ttsRes.text = rspText.getAnswer();
						
			if(bool){
				msg.what = 7;
			}else{
				msg.what = 0;
			}
			bool = false;
			msg.obj = ttsRes;
			handler.sendMessage(msg);
		}
	}
    
    public int getNodeType() {
    	int type = loadData.getList().get(loadData.getListSize()-1).getType();
    	
    	if(CFG.VIEW_TYPE_QUIZ == type) {
    		return 0;
    	} else {
    		Response node = loadData.getList().get(loadData.getListSize()-1).getResponse();
    		if(CFG.VIEW_TYPE_TEXT != node.getFirstNodeType() &&  CFG.VIEW_TYPE_JOSN != node.getFirstNodeType()) {
    			return 1;
    		} else {
    			return 0;
    		}
		}
    }
    
    //shigq add
    public void startPushGreet() {
    	new Thread(new GetDateRunable(true)).start();
    	/*
    	TimerTask mTimerTask = new TimerTask() {  
  			@Override      
  		    public void run() {  
  				new Thread(new GetDateRunable(true)).start();
  		    } 
  		}; 	
  		
  		Timer timer = new Timer();
  		timer.schedule(mTimerTask,500);
  		*/
    }
    
    public void checkTimeToPushGreet() {
    	Date curDate = new Date(System.currentTimeMillis());
		int hour = curDate.getHours();
		if(hour < 6){
			if(!isBeforeDawn){
				startPushGreet();
				isBeforeDawn = true;
			}
			isMorning = false;
			isMiddle = false;
			isAfternoon = false;
			isNight = false;
		} else if ((hour >= 6 && hour < 11)) {
			if (!isMorning) {
				startPushGreet();
				isMorning = true;
			}
			isBeforeDawn = false;
			isMiddle = false;
			isAfternoon = false;
			isNight = false;
		} else if ((hour >= 11 && hour < 13)) {
			if (!isMiddle) {
				startPushGreet();
				isMiddle = true;
			}
			isBeforeDawn = false;
			isMorning = false;
			isAfternoon = false;
			isNight = false;
		} else if(hour >= 13 && hour < 18){
			if(!isAfternoon){
				startPushGreet();
				isAfternoon = true;
			}
			isBeforeDawn = false;
			isMorning = false;
			isMiddle = false;
			isNight = false;
		}else{
			if (!isNight) {
				startPushGreet();
				isNight = true;
			}
			isBeforeDawn = false;
			isMorning = false;
			isAfternoon = false;
			isMiddle = false;
		}
    }
    
    public void cancelRequestText() {
    	if (yySDK != null) {
    		yySDK.cancelRequest();
    	} else {
    		return;
    	}
    }
    
    /*public void cancelRequestText(boolean myType){
    	if(yySDK != null){
    		yySDK.cancelRequest();
    		setHome_stop_search(myType);
    		return;
    	}else{
    		return;
    	}
    }*/
    
    public void setAllTimeState(boolean b) {
		this.isBeforeDawn = b;
		this.isMorning = b;
		this.isMiddle = b;
		this.isAfternoon = b;
		this.isNight = b;
	}
    
    
    //shigq add
    
    //For scrollView
  	public void getLayoutManager(LayoutManager l) {
  		mLayoutManager = l;
  	}
  	
  	/*public void getContentViewConstructor(ContentViewConstructor c) {
  		mViewConstructor = c;
  	}*/
  	
  	public LoadData getLoadData() {
  		return loadData;
  	}
  	//For scrollView
  	
  	public boolean isHome_stop_search() {
		return home_stop_search;
	}

	public void setHome_stop_search(boolean home_stop_search) {
		this.home_stop_search = home_stop_search;
	}
	
	public void setStatusBarBG(boolean isTransparent) {
        Intent StatusBarBGIntent = new Intent();
        StatusBarBGIntent.setAction("aurora.action.SET_STATUSBAR_TRANSPARENT");
        StatusBarBGIntent.putExtra("transparent", isTransparent);
        context.sendBroadcast(StatusBarBGIntent);
    }
	
	//baidu location start
	public void updateLocationInfo(String latitude, String longitude) {
		yySDK.setPrefValues(SogouYYSDK.SETTING_LATITUDE, latitude);
        yySDK.setPrefValues(SogouYYSDK.SETTING_LONGITUDE, longitude);
	}
	//baidu location end
}
