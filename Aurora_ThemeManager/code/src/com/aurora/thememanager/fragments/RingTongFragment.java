package com.aurora.thememanager.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.aurora.internet.HttpUtils;
import com.aurora.internet.InternetError;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.activity.MainActivity;
import com.aurora.thememanager.activity.MainActivity.OnNetworkChangeListener;
import com.aurora.thememanager.adapter.RingtoneListAdapter;
import com.aurora.thememanager.adapter.RingtoneListAdapter.OnItemApplyListener;
import com.aurora.thememanager.adapter.WallpaperAdapter;
import com.aurora.thememanager.cache.CacheManager;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeAudio;
import com.aurora.thememanager.parser.JsonParser;
import com.aurora.thememanager.parser.JsonParser.CallBack;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadManager;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadStatusCallback;
import com.aurora.thememanager.utils.download.DownloadUpdateListener;
import com.aurora.thememanager.utils.download.RingtongDownloadService;
import com.aurora.thememanager.utils.download.FileDownloader;
import com.aurora.thememanager.utils.themehelper.ThemeInternetHelper;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.view.ListViewDelegate;
import com.aurora.thememanager.view.ListViewLoadView;

public class RingTongFragment extends SuperAwesomeCardFragment implements OnNetworkChangeListener,
JsonParser.CallBack,OnClickListener,ListViewDelegate.OnListScrollChange, RingtoneListAdapter.OnRingToneListLoadViewClick, 
OnBufferingUpdateListener, OnCompletionListener, MediaPlayer.OnPreparedListener, Runnable{
	
	private static final String TAG = "RingTongFragment";
	private ThemeInternetHelper mThemeLoadHelperRing;
	private ThemeInternetHelper mThemeLoadHelperMessage;

	private List<Object> mThemes = new ArrayList<Object>();
	
	private RingtoneListAdapter mRingtoneListAdapter;
	
	private FrameLayout mRingtoneRela;
	private AuroraListView mRingtoneList;
	
	private View mProgress;
	private View mNoNetWorkView;
	private ListViewDelegate mListDelegate;
	
	private JsonHttpListener mHttpListener;
	
	private int mCurrentPageRing = 1;
	private int mTotalPageRing;
	private int mCurrentPageMessage = 1;
	private int mTotalPageMessage;
	
	private Context mContext;
	private AuroraActivity mAuroraActivity;
	private ThemeManager mRingTongFragmentThemeManager;
	private Theme mRingTongFragmentApplyTheme;
	private Theme mCurrentTheme;
	
	public MediaPlayer mediaPlayer; 
	private static final int PLAYER_STATE_STOP = 0;
	private static final int PLAYER_STATE_PLAY = 1;
	private static final int PLAYER_STATE_CHANGE = 2;
	private static final int PLAYER_STATE_LOADING = 3;
	private int mPlayerState = PLAYER_STATE_STOP;
	
	private SoundPool mSoundPool;
	
	private boolean mIsStartPlay = false;
	private Handler mStopHandler = new Handler();
	
	private ImageView mLoadingAndPlayAnimImage;
	private ImageView mLastLoadingAndPlayAnimImage;
	private AnimationDrawable mAnimationDrawable;
	private TextView mRingtoneDuration;
	private TextView mLastRingtoneDuration;
	
	private boolean mIsRequestingForPhone;
	private boolean mIsRequestingForMessage;
	
	private AnimatorSet mSet = new AnimatorSet();
	
	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			if (mRingtoneListAdapter != null) {
				// adapter.notifyDataSetChanged();
				Log.e("101010", "-----mRingtoneListAdapter.updateView(mRingtoneList)-----");
				mRingtoneListAdapter.updateView(mRingtoneList);
			}
		}
	};
	
	private Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			
			synchronized (mThemes) {
				if(mThemes != null && mThemes.size() > 0){

					for(Object theme:mThemes){

						ThemeAudio th  = (ThemeAudio)theme;
						mRingtoneListAdapter.addData(th);
					}
				}
			}
		};
		
	};
	
	private HttpCallBack mHttpCallBack = new HttpCallBack() {

		@Override
		public void onPreExecute() {
			// TODO Auto-generated method stub
			mProgress.setVisibility(View.VISIBLE);
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			mIsRequestingForMessage = false;
		}

		@Override
		public void onSuccess(Object response) {
			// TODO Auto-generated method stub
			mProgress.setVisibility(View.GONE);
			if(response != null){
				synchronized (mThemes) {

					Parser parser = getThemeParser(Parser.TYPE_RINGTONG);
					parser.setCallBack(new CallBack() {
						@Override
						public void onParserSuccess(boolean success, int statusCode, String desc,
								int totalPage) {
							// TODO Auto-generated method stub
							Log.e("101010", "---mTotalPageMessage = ---" + totalPage);
							if(success && statusCode == 0){
								mRingtoneList.setVisibility(View.VISIBLE);
								if(mTotalPageMessage == 0){
									mTotalPageMessage = totalPage;
								}
							}
						}
					});
					mThemes =parser.startParser(response.toString());
					if(mThemes != null && mThemes.size() > 0){

						for(Object theme:mThemes){

							ThemeAudio th  = (ThemeAudio)theme;
							mRingtoneListAdapter.addData(th);
						}
					}
				}
			}
		}

		@Override
		public void onError(InternetError error) {
			// TODO Auto-generated method stub
			mProgress.setVisibility(View.GONE);
			Context context = getActivity();
			if(context != null){
				showNoNetworkView(!SystemUtils.isNetworkConnected(getActivity()));
			}
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onNetworking() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUsedCache() {
			// TODO Auto-generated method stub
			showNoNetworkView(false);
		}

		@Override
		public void onRetry() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProgressChange(long fileSize, long downloadedSize) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void initBottomMenu() {
		if(mAuroraActivity != null) {
			mAuroraActivity.setAuroraSystemMenuCallBack(new OnAuroraMenuItemClickListener() {
				
				@Override
				public void auroraMenuItemClick(int itemId) {
					// TODO Auto-generated method stub
					if(mRingTongFragmentApplyTheme == null || mRingTongFragmentThemeManager == null) {
						return;
					}
					switch (itemId) {
					case R.id.aurora_menu_message:
						((ThemeAudio)mRingTongFragmentApplyTheme).ringtongType = ThemeAudio.MESSAGE;
						mRingTongFragmentThemeManager.apply(mRingTongFragmentApplyTheme);
						break;
					case R.id.aurora_menu_notification:
						((ThemeAudio)mRingTongFragmentApplyTheme).ringtongType = ThemeAudio.NOTIFICATION;
						mRingTongFragmentThemeManager.apply(mRingTongFragmentApplyTheme);
						break;
					case R.id.aurora_menu_both:
						((ThemeAudio)mRingTongFragmentApplyTheme).ringtongType = ThemeAudio.MESSAGE_AND_NOTIFICATION;
						mRingTongFragmentThemeManager.apply(mRingTongFragmentApplyTheme);
						break;
	
					default:
						break;
					}
				}
			});
			mAuroraActivity.setAuroraMenuItems(R.menu.aurora_menu_choose_ringtonetype, com.aurora.R.layout.aurora_menu_fillparent);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mRingtoneRela = (FrameLayout)inflater.inflate(R.layout.ringtone_theme_fragment, null);
		mRingtoneListAdapter = new RingtoneListAdapter(getActivity());
		
		mNoNetWorkView = mRingtoneRela.findViewById(R.id.no_network_error);
		mProgress =  mRingtoneRela.findViewById(R.id.progress);
		mRingtoneList = (AuroraListView)mRingtoneRela.findViewById(R.id.ringtone_list);
		mRingtoneList.setAdapter(mRingtoneListAdapter);
		mRingtoneListAdapter.setCallBack(this);
		
		mRingtoneListAdapter.setOnItemApplyListener(new OnItemApplyListener() {
			
			@Override
			public void setItemApply(Theme theme, ThemeManager mThemeManager) {
				// TODO Auto-generated method stub
				if(mAuroraActivity != null) {
					mRingTongFragmentThemeManager = mThemeManager;
					mRingTongFragmentApplyTheme = theme;
					mAuroraActivity.showAuroraMenu();
				}
			}
		});
		
		mRingtoneList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				//Log.e("101010", "-----mRingtoneList.setOnItemClickListener------");
				if(!mRingtoneListAdapter.getTheme(position).equals(mCurrentTheme)) {
					Log.e("101010", "-------onItemClick mPlayerState = ------" + mPlayerState);
					if(mPlayerState == PLAYER_STATE_PLAY || mPlayerState == PLAYER_STATE_LOADING) {
						mPlayerState = PLAYER_STATE_CHANGE;
						mLastLoadingAndPlayAnimImage = mLoadingAndPlayAnimImage;
						mLastRingtoneDuration = mRingtoneDuration;
					} else {
						mPlayerState = PLAYER_STATE_STOP;
					}
				} else {
					Log.e("101010", "-mPlayerState == PLAYER_STATE_LOADING--");
					if(mPlayerState == PLAYER_STATE_LOADING) {
						mPlayerState = PLAYER_STATE_PLAY;
					}
				}
				mLoadingAndPlayAnimImage = (ImageView)view.findViewById(R.id.loadingandplayanim);
				mRingtoneDuration = (TextView)view.findViewById(R.id.ringtone_duration);
				mCurrentTheme = mRingtoneListAdapter.getTheme(position);
				testPlay(mCurrentTheme);
			}
		});
		
		mListDelegate = new ListViewDelegate(mRingtoneList, true,false);
		mListDelegate.setCallBack(this);
		mListDelegate.setAdapter(mRingtoneListAdapter);
		
		mHttpListener = new JsonHttpListener(mHttpCallBack);
		mThemeLoadHelperRing = new ThemeInternetHelper(getActivity(),CacheManager.CACHE_RINGTONG);
		mThemeLoadHelperMessage = new ThemeInternetHelper(getActivity(),CacheManager.CACHE_RINGTONG);
		
		requestThemeRing(mCurrentPageRing);
		requestThemeMessage(mCurrentPageMessage);
		
		((MainActivity)getActivity()).addNetworkListener(this);
		
		initMediaPlayer();
		
		initBottomMenu();

		return mRingtoneRela;
	}
	
	private void testPlay(Theme theme) {
		if (mPlayerState == PLAYER_STATE_PLAY) {  
			playAudioAnim();
            stop();
            mStopHandler.removeCallbacks(mStopRunnable);
        } else if (mPlayerState == PLAYER_STATE_STOP || mPlayerState == PLAYER_STATE_CHANGE || mPlayerState == PLAYER_STATE_LOADING) { 
    		if (!SystemUtils.hasNetwork()) {
    			Toast.makeText(mContext, mContext
    					.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
    			return;
    		}
        	Thread thread = new Thread(this);
        	thread.start();
            if(mLoadingAndPlayAnimImage != null) {
            	mLoadingAndPlayAnimImage.setImageResource(R.drawable.ringtone_loading_anim);
            	mAnimationDrawable = (AnimationDrawable)mLoadingAndPlayAnimImage.getDrawable();
            	mAnimationDrawable.stop();
            	mAnimationDrawable.start();
            }
            playAudioAnim();
        }
	}
	
	private void playAudioAnim() {
		
		Log.e("101010", "-------playAudioAnim mPlayerState = ------" + mPlayerState);
		mSet = new AnimatorSet();
		
		if(mPlayerState == PLAYER_STATE_STOP || mPlayerState == PLAYER_STATE_LOADING) {
			ObjectAnimator alphaOut2 = ObjectAnimator.ofFloat(
					mLoadingAndPlayAnimImage, "Alpha", 0.0f, 1.0f);
			alphaOut2.setDuration(200);
			
			ObjectAnimator alphaOut1 = ObjectAnimator.ofFloat(
					mRingtoneDuration, "Alpha", 1.0f, 0.0f);
			alphaOut1.setDuration(200);
			ObjectAnimator alphaOut3 = ObjectAnimator.ofFloat(
					mRingtoneDuration, "Alpha", 0.0f, 1.0f);
			alphaOut3.setDuration(200);
			
			alphaOut1.addListener(new AnimatorListener() {
				
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
					mLoadingAndPlayAnimImage.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub
					
				}
			});
			mSet.play(alphaOut2).with(alphaOut3).after(alphaOut1);
		} else if(mPlayerState == PLAYER_STATE_PLAY) {
			ObjectAnimator alphaOut2 = ObjectAnimator.ofFloat(
					mLoadingAndPlayAnimImage, "Alpha", 1.0f, 0.0f);
			alphaOut2.setDuration(200);
			
			ObjectAnimator alphaOut1 = ObjectAnimator.ofFloat(
					mRingtoneDuration, "Alpha", 1.0f, 0.0f);
			alphaOut1.setDuration(200);
			ObjectAnimator alphaOut3 = ObjectAnimator.ofFloat(
					mRingtoneDuration, "Alpha", 0.0f, 1.0f);
			alphaOut3.setDuration(200);
			
			alphaOut1.addListener(new AnimatorListener() {
				
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
					Log.e("101010", "-------mLoadingAndPlayAnimImage.setVisibility(View.GONE)------");
					mLoadingAndPlayAnimImage.setVisibility(View.GONE);
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub
					
				}
			});
			mSet.play(alphaOut1).with(alphaOut2).before(alphaOut3);
		} else if (mPlayerState == PLAYER_STATE_CHANGE) {
			ObjectAnimator alphaOut2 = ObjectAnimator.ofFloat(
					mLastLoadingAndPlayAnimImage, "Alpha", 1.0f, 0.0f);
			alphaOut2.setDuration(200);
			
			ObjectAnimator alphaOut1 = ObjectAnimator.ofFloat(
					mLastRingtoneDuration, "Alpha", 1.0f, 0.0f);
			alphaOut1.setDuration(200);
			
			ObjectAnimator alphaOut5 = ObjectAnimator.ofFloat(
					mLastRingtoneDuration, "Alpha", 0.0f, 1.0f);
			alphaOut5.setDuration(200);
			
			ObjectAnimator alphaOut3 = ObjectAnimator.ofFloat(
					mRingtoneDuration, "Alpha", 0.0f, 1.0f);
			alphaOut3.setDuration(200);
			
			ObjectAnimator alphaOut4 = ObjectAnimator.ofFloat(
					mLoadingAndPlayAnimImage, "Alpha", 0.0f, 1.0f);
			alphaOut4.setDuration(200);
			
			alphaOut1.addListener(new AnimatorListener() {
				
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
					mLastLoadingAndPlayAnimImage.setVisibility(View.GONE);
					mLoadingAndPlayAnimImage.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub
					
				}
			});
			
			mSet.play(alphaOut2).with(alphaOut1);
			mSet.play(alphaOut4).with(alphaOut3).with(alphaOut5).after(alphaOut2);
		}
		mSet.start();
	}
	
	private void initMediaPlayer() {
        try {  
            mediaPlayer = new MediaPlayer();  
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  
            mediaPlayer.setOnBufferingUpdateListener(this);  
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {  
            Log.e("mediaPlayer", "error", e);  
        }  
	}
	
	private void playUrl(Theme theme)  
    {  
        try {  
        	Log.e("101010", "----playUrl111 downloadPath = -----" + theme.downloadPath);
        	mPlayerState = PLAYER_STATE_LOADING;
        	mIsStartPlay = true;
            mediaPlayer.stop();
            mediaPlayer.reset();  
            mediaPlayer.setDataSource(theme.downloadPath);  
            mediaPlayer.prepare();//prepare之后自动播放  
            Log.e("101010", "----playUrl222-----");
            //mediaPlayer.start();  
        } catch (IllegalArgumentException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IllegalStateException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
    }  
	
	private void pause()  
    {  
		if (mediaPlayer != null) {   
			mediaPlayer.pause();  
		}
    }
      
    private void stop()  
    {  
        if (mediaPlayer != null) {   
            mediaPlayer.stop();
            Log.e("101010", "--stop mPlayerState = PLAYER_STATE_STOP----");
            mPlayerState = PLAYER_STATE_STOP;
        }
		if(mLoadingAndPlayAnimImage != null) {
        	if(mAnimationDrawable != null) {
        		mAnimationDrawable.stop();
        	}
		}
    }
    
	@Override
	public void run() {
		// TODO Auto-generated method stub
		playUrl(mCurrentTheme);
	}
    
	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
//		mPlayerState = PLAYER_STATE_PLAY;
//		mp.setLooping(false);
//		mp.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		//playAudioAnim();
		//stop();
		Log.e("101010", "----onCompletion-----");
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		Log.e("101010", "---onBufferingUpdate percent = ---" + percent + ", mp.getDuration() = " + mp.getDuration());
		if(percent == 100 && mIsStartPlay) {
			//Log.e("101010", "----开始播放--------");
			mIsStartPlay = false;
			mPlayerState = PLAYER_STATE_PLAY;
			mp.setLooping(false);
			mp.start();
			if(mLoadingAndPlayAnimImage != null) {
            	mAnimationDrawable.stop();
            	mLoadingAndPlayAnimImage.setImageResource(R.drawable.ringtone_playing_anim);
            	mAnimationDrawable = (AnimationDrawable)mLoadingAndPlayAnimImage.getDrawable();
            	mAnimationDrawable.stop();
            	mAnimationDrawable.start();
			}
			mStopHandler.removeCallbacks(mStopRunnable);
			mStopHandler.postDelayed(mStopRunnable, mp.getDuration());
		}
	}
	private Runnable mStopRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			playAudioAnim();
			stop();
			//Log.e("101010", "----播放完毕--------");
		}
	};
	
	/**
	 * 请求网络数据
	 * @param page
	 */
	private void requestThemeRing(int page){
		Log.e("101010", "----requestThemeRing mIsRequestingForPhone = -----" + mIsRequestingForPhone);
		if(!mIsRequestingForPhone) {
			Map<String,Object> themeMap = HttpUtils.createPostMap("pageNum", page, getActivity());
			themeMap.put("type", ThemeAudio.RINGTONE);
			
			mThemeLoadHelperRing.clearRequest();
			mThemeLoadHelperRing.request(ThemeConfig.HttpConfig.THEME_RINTONG_REQUEST_URL,getHttpListener(),HttpUtils.createPostParamsFromMap(themeMap));
			mThemeLoadHelperRing.startRequest();
			
			mIsRequestingForPhone = true;
		}
	}

	private void requestThemeMessage(int page){
		Log.e("101010", "----requestThemeMessage mIsRequestingForMessage = -----" + mIsRequestingForMessage);
		if(!mIsRequestingForMessage) {
			Map<String,Object> themeMap = HttpUtils.createPostMap("pageNum", page, getActivity());
			themeMap.put("type", ThemeAudio.NOTIFICATION);
			
			mThemeLoadHelperMessage.clearRequest();
			mThemeLoadHelperMessage.request(ThemeConfig.HttpConfig.THEME_RINTONG_REQUEST_URL,mHttpListener,HttpUtils.createPostParamsFromMap(themeMap));
			mThemeLoadHelperMessage.startRequest();
			
			mIsRequestingForMessage = true;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		RingtongDownloadService.registerUpdateListener(updateListener);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mRingtoneListAdapter != null) {
			mRingtoneListAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(mediaPlayer != null) {
			stop();
		}
		if(mLoadingAndPlayAnimImage != null) {
			mLoadingAndPlayAnimImage.setVisibility(View.GONE);
		}
		if(mLastLoadingAndPlayAnimImage != null) {
			mLastLoadingAndPlayAnimImage.setVisibility(View.GONE);
		}
		if(mSet != null && mSet.isRunning()) {
			mSet.cancel();
		}
		mStopHandler.removeCallbacks(mStopRunnable);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mediaPlayer != null) {
			stop();
			mediaPlayer.release();
		}
		RingtongDownloadService.unRegisterUpdateListener(updateListener);
		mThemeLoadHelperRing.stopRequest();
		mThemeLoadHelperMessage.stopRequest();
		if(mRingtoneListAdapter != null){
			mRingtoneListAdapter.stopQueue();
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = (Context)activity;
		mAuroraActivity = (AuroraActivity)activity;
	}

	@Override
	public void onPreExecute() {
		// TODO Auto-generated method stub
		if(mCurrentPageRing == 1){
			mListDelegate.hideLoadMoreView();
		}
		mProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		mIsRequestingForPhone = false;
	}

	@Override
	public void onSuccess(Object response) {
		// TODO Auto-generated method stub

		mProgress.setVisibility(View.GONE);
		if(response != null){
			synchronized (mThemes) {

				Parser parser = getThemeParser(Parser.TYPE_RINGTONG);
				parser.setCallBack(this);
				mThemes =parser.startParser(response.toString());
				if(mThemes != null && mThemes.size() > 0){

					for(Object theme:mThemes){

						ThemeAudio th  = (ThemeAudio)theme;
						mRingtoneListAdapter.addData(th);
					}
				}

			}
		}
	}

	@Override
	public void onError(InternetError error) {
		// TODO Auto-generated method stub
		mProgress.setVisibility(View.GONE);
		Context context = getActivity();
		if(context != null){
			showNoNetworkView(!SystemUtils.isNetworkConnected(getActivity()));
		}
	}
	
	private void showNoNetworkView(boolean show){
		mNoNetWorkView.setVisibility(show?View.VISIBLE:View.GONE);
		mRingtoneList.setVisibility(show?View.GONE:View.VISIBLE);
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNetworking() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUsedCache() {
		// TODO Auto-generated method stub
		showNoNetworkView(false);
	}

	@Override
	public void onRetry() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgressChange(long fileSize, long downloadedSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onParserSuccess(boolean success, int statusCode, String desc,
			int totalPage) {
		// TODO Auto-generated method stub
		Log.e("101010", "---mTotalPageRing = ---" + totalPage);
		if(success && statusCode == 0){
			mRingtoneList.setVisibility(View.VISIBLE);
			if(mTotalPageRing == 0){
				mTotalPageRing = totalPage;
			}
		}
	}

	@Override
	public void onNetConnnectedChange(boolean hasNetwork) {
		// TODO Auto-generated method stub
	    boolean showNoNetworkView = mThemes.size() < 1 && !hasNetwork;
		mNoNetWorkView.setVisibility(showNoNetworkView?View.VISIBLE:View.GONE);
		if(hasNetwork && mThemes.size() < 1){
			requestThemeRing(mCurrentPageRing);
			requestThemeMessage(mCurrentPageMessage);
		}
	}

	@Override
	public void onShowLoadMoreView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHideLoadMoreView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadMore() {
		// TODO Auto-generated method stub
		if(mCurrentPageMessage < mTotalPageMessage){
			if(!mIsRequestingForMessage) {
				mCurrentPageMessage++;
				mListDelegate.loadFinished(false);
				requestThemeMessage(mCurrentPageMessage);
			}
		}else{
			mListDelegate.loadFinished(true);
		}
	}
	
	@Override
	public void onRingToneListShowLoadMoreView() {
		
	}
	
	@Override
	public void onRingToneListHideLoadMoreView() {
		
	}
	
	@Override
	public void onRingToneListLoadMore() {
		if(mCurrentPageRing < mTotalPageRing){
			if(!mIsRequestingForPhone) {
				mCurrentPageRing++;
				mRingtoneListAdapter.setLoadViewStatus(ListViewLoadView.STATUS_LOADMORE);
				requestThemeRing(mCurrentPageRing);
			}
		}else{
			mRingtoneListAdapter.setLoadViewStatus(ListViewLoadView.STATUS_LOADFINISHED);
		}
		mRingtoneListAdapter.notifyDataSetChanged();
	}
}
