package com.aurora.thememanager.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import aurora.widget.AuroraListView;

import com.aurora.internet.HttpUtils;
import com.aurora.internet.InternetError;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.activity.MainActivity;
import com.aurora.thememanager.activity.MainActivity.OnNetworkChangeListener;
import com.aurora.thememanager.adapter.RingtoneListAdapter;
import com.aurora.thememanager.adapter.WallpaperAdapter;
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
	
	private RelativeLayout mRingtoneRela;
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
	private Theme mCurrentTheme;
	
	public MediaPlayer mediaPlayer; 
	private static final int PLAYER_STATE_STOP = 0;
	private static final int PLAYER_STATE_PLAY = 1;
	private static final int PLAYER_STATE_PAUSE = 2;
	private int mPlayerState = 0;
	
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
			showNoNetworkView(!SystemUtils.isNetworkConnected(getActivity()));
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mRingtoneRela = (RelativeLayout)inflater.inflate(R.layout.ringtone_theme_fragment, null);
		mRingtoneListAdapter = new RingtoneListAdapter(getActivity());
		
		mNoNetWorkView = mRingtoneRela.findViewById(R.id.no_network_error);
		mProgress =  mRingtoneRela.findViewById(R.id.progress);
		mRingtoneList = (AuroraListView)mRingtoneRela.findViewById(R.id.ringtone_list);
		mRingtoneList.setAdapter(mRingtoneListAdapter);
		mRingtoneListAdapter.setCallBack(this);
		
		mRingtoneList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Log.e("101010", "-----mRingtoneList.setOnItemClickListener------");
				mCurrentTheme = mRingtoneListAdapter.getTheme(position);
				testPlay(mCurrentTheme);
			}
		});
		
		mListDelegate = new ListViewDelegate(mRingtoneList, true,false);
		mListDelegate.setCallBack(this);
		mListDelegate.setAdapter(mRingtoneListAdapter);
		
		mHttpListener = new JsonHttpListener(mHttpCallBack);
		mThemeLoadHelperRing = new ThemeInternetHelper(getActivity());
		mThemeLoadHelperMessage = new ThemeInternetHelper(getActivity());
		
		requestThemeRing(mCurrentPageRing);
		requestThemeMessage(mCurrentPageMessage);
		
		((MainActivity)getActivity()).addNetworkListener(this);
		
		initMediaPlayer();

		return mRingtoneRela;
	}
	
	private void testPlay(Theme theme) {
		if (!SystemUtils.hasNetwork()) {
			Toast.makeText(mContext, mContext
					.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
			return;
		}
		if (mPlayerState == PLAYER_STATE_PLAY) {  
            mediaPlayer.stop();  
            mPlayerState = PLAYER_STATE_STOP;
        } else if (mPlayerState == PLAYER_STATE_STOP) {  
        	Thread thread = new Thread(this);
        	thread.start();
        }
	}
	
	private void initMediaPlayer() {
        try {  
            mediaPlayer = new MediaPlayer();  
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  
            mediaPlayer.setOnBufferingUpdateListener(this);  
            mediaPlayer.setOnPreparedListener(this);  
        } catch (Exception e) {  
            Log.e("mediaPlayer", "error", e);  
        }  
	}
	
	private void playUrl(Theme theme)  
    {  
        try {  
        	Log.e("101010", "----playUrl111-----");
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
            mediaPlayer.release();   
            mediaPlayer = null;   
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
		Log.e("101010", "----onPrepared-----");
		mPlayerState = PLAYER_STATE_PLAY;
		mp.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 请求网络数据
	 * @param page
	 */
	private void requestThemeRing(int page){
		Map<String,Object> themeMap = HttpUtils.createPostMap("pageNum", page, getActivity());
		themeMap.put("type", ThemeAudio.RINGTONE);
		
		mThemeLoadHelperRing.clearRequest();
		mThemeLoadHelperRing.request(ThemeConfig.HttpConfig.THEME_RINTONG_REQUEST_URL,getHttpListener(),HttpUtils.createPostParamsFromMap(themeMap));
		mThemeLoadHelperRing.startRequest();
	}

	private void requestThemeMessage(int page){
		Map<String,Object> themeMap = HttpUtils.createPostMap("pageNum", page, getActivity());
		themeMap.put("type", ThemeAudio.NOTIFICATION);
		
		mThemeLoadHelperMessage.clearRequest();
		mThemeLoadHelperMessage.request(ThemeConfig.HttpConfig.THEME_RINTONG_REQUEST_URL,mHttpListener,HttpUtils.createPostParamsFromMap(themeMap));
		mThemeLoadHelperMessage.startRequest();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		RingtongDownloadService.registerUpdateListener(updateListener);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.reset();
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
		showNoNetworkView(!SystemUtils.isNetworkConnected(getActivity()));
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
			mCurrentPageMessage++;
			mListDelegate.loadFinished(false);
			requestThemeMessage(mCurrentPageMessage);
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
			mCurrentPageRing++;
			mRingtoneListAdapter.setLoadViewStatus(ListViewLoadView.STATUS_LOADMORE);
			requestThemeRing(mCurrentPageRing);
		}else{
			mRingtoneListAdapter.setLoadViewStatus(ListViewLoadView.STATUS_LOADFINISHED);
		}
		mRingtoneListAdapter.notifyDataSetChanged();
	}
}
