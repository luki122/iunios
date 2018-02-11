package com.aurora.thememanager.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.aurora.thememanager.R;
import com.aurora.thememanager.adapter.AbsThemeAdapter;
import com.aurora.thememanager.adapter.LocalRingtoneListAdapter;
import com.aurora.thememanager.adapter.LocalRingtoneListAdapter.OnLocalItemApplyListener;
import com.aurora.thememanager.adapter.RingtoneListAdapter;
import com.aurora.thememanager.adapter.ThemeLocalAdapter;
import com.aurora.thememanager.adapter.RingtoneListAdapter.OnItemApplyListener;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeAudio;
import com.aurora.thememanager.utils.FileUtils;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.RingtongDatabaseController;
import com.aurora.thememanager.utils.download.RingtongDownloadService;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themeloader.Loader;
import com.aurora.thememanager.utils.themeloader.ThemeLoadListener;
import com.aurora.thememanager.utils.themeloader.ThemePackageLoader;

public class DownloadedRingTongActivity extends BaseActivity implements OnCompletionListener, MediaPlayer.OnPreparedListener{

	private LocalRingtoneListAdapter mLocalRingtoneListAdapter;

	private RelativeLayout mLocalRingtoneRela;
	private AuroraListView mLocalRingtoneList;
	private AuroraActionBar mAuroraActionBar;
	private TextView mSelectLeftBtn;
	private TextView mSelectRightBtn;
	private TextView mDownLoadedText;
	private TextView mNoDownloadText;
	private ArrayList<String> audioFile = new ArrayList<String>();
	private List<DownloadData> audioDownloadDataPhone = new ArrayList<DownloadData>();
	private List<DownloadData> audioDownloadDataMessage = new ArrayList<DownloadData>();
	private MediaPlayer mMediaPlayer;
	private static final int DOWNLOAD_PLAYER_STATE_STOP = 0;
	private static final int DOWNLOAD_PLAYER_STATE_PLAY = 1;
	private static final int DOWNLOAD_PLAYER_STATE_PAUSE = 2;
	private int mPlayerState = DOWNLOAD_PLAYER_STATE_STOP;
	private String mCurrentRingTonePath;
	
	private ThemeManager mDownloadedRingTongThemeManager;
	private Theme mDownloadedRingTongApplyTheme;
	
	
	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.aurora_menu_del:
				AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(
						DownloadedRingTongActivity.this).setMessage(R.string.delete_trueorfalse).setTitle(R.string.delete_string).
						setNegativeButton("取消", null).setPositiveButton("确定",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								mLocalRingtoneListAdapter.setShowListCheckbox(!mLocalRingtoneListAdapter.getShowListCheckbox());
								deleteLocalRingtone();
								mLocalRingtoneListAdapter.updateAdapter(audioDownloadDataPhone, audioDownloadDataMessage, true);
								showEditPanel(false);
							}

						}).create();
				dialog.show();
				break;

			default:
				break;
			}
		}
	};
	
	private void deleteLocalRingtone() {
		if(mLocalRingtoneListAdapter.getCheckboxCheckList().size() > 0) {
			for(int i = 0; i < audioDownloadDataPhone.size(); i++) {
				if(mLocalRingtoneListAdapter.getCheckboxCheckList().contains(Integer.valueOf(i))) {
					Theme theme = (Theme)audioDownloadDataPhone.get(i);
					RingtongDownloadService.getDownloadController().delete(theme.downloadId);
					deleteItemInMediaProvider(theme);
					File f = new File(theme.fileDir, theme.fileName);
					f.delete();
				}
			}
			for(int i = 0; i < audioDownloadDataMessage.size(); i++) {
				if(mLocalRingtoneListAdapter.getCheckboxCheckList().contains(Integer.valueOf(i+audioDownloadDataPhone.size()))) {
					Theme theme = (Theme)audioDownloadDataMessage.get(i);
					RingtongDownloadService.getDownloadController().delete(theme.downloadId);
					deleteItemInMediaProvider(theme);
					File f = new File(theme.fileDir, theme.fileName);
					f.delete();
				}
			}
			audioDownloadDataPhone = ((RingtongDatabaseController)RingtongDownloadService.getDownloadController()).getDownloadedDatasForRingToneType(0);
			audioDownloadDataMessage = ((RingtongDatabaseController)RingtongDownloadService.getDownloadController()).getDownloadedDatasForRingToneType(1);
			setLocalRingtoneListVisibility();
		}
	}
	
	private void deleteItemInMediaProvider(Theme theme) {
    	String themePath = theme.fileDir+"/"+theme.fileName;
		Cursor cr= null;
		String soundRecordPathWhere = MediaStore.Audio.Media.DATA + "=?";
		cr = this.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID }, soundRecordPathWhere,
				new String[]{themePath}, null); 

		if(cr != null && cr.moveToFirst() && cr.getCount() > 0) {
			this.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, soundRecordPathWhere, new String[]{themePath});
		}
		if(cr != null) {
			cr.close();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.downloaded_ringtone_activity, AuroraActionBar.Type.Normal);
		initViews();
		initBottomMenu();
		initMediaPlayer();
	}
	
	private void initBottomMenu() {
		setAuroraSystemMenuCallBack(new OnAuroraMenuItemClickListener() {

			@Override
			public void auroraMenuItemClick(int itemId) {
				// TODO Auto-generated method stub
				if(mDownloadedRingTongApplyTheme == null || mDownloadedRingTongThemeManager == null) {
					return;
				}
				switch (itemId) {
				case R.id.aurora_menu_message:
					((ThemeAudio)mDownloadedRingTongApplyTheme).ringtongType = ThemeAudio.MESSAGE;
					mDownloadedRingTongThemeManager.apply(mDownloadedRingTongApplyTheme);
					break;
				case R.id.aurora_menu_notification:
					((ThemeAudio)mDownloadedRingTongApplyTheme).ringtongType = ThemeAudio.NOTIFICATION;
					mDownloadedRingTongThemeManager.apply(mDownloadedRingTongApplyTheme);
					break;
				case R.id.aurora_menu_both:
					((ThemeAudio)mDownloadedRingTongApplyTheme).ringtongType = ThemeAudio.MESSAGE_AND_NOTIFICATION;
					mDownloadedRingTongThemeManager.apply(mDownloadedRingTongApplyTheme);
					break;

				default:
					break;
				}
			}
		});
		setAuroraMenuItems(R.menu.aurora_menu_choose_ringtonetype, com.aurora.R.layout.aurora_menu_fillparent);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(mLocalRingtoneListAdapter != null && mLocalRingtoneListAdapter.getShowListCheckbox()) {
				mLocalRingtoneListAdapter.setShowListCheckbox(!mLocalRingtoneListAdapter.getShowListCheckbox());
				mLocalRingtoneListAdapter.updateAdapter(audioDownloadDataPhone, audioDownloadDataMessage, true);
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initMediaPlayer() {
        try {  
            mMediaPlayer = new MediaPlayer();  
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {  
            Log.e("mediaPlayer", "error", e);  
        }  
	}
	
	private void playRingTone(String ringTonePath) {
		if (mPlayerState == DOWNLOAD_PLAYER_STATE_PLAY) {  
            mMediaPlayer.stop();  
            mPlayerState = DOWNLOAD_PLAYER_STATE_STOP;
        } else if (mPlayerState == DOWNLOAD_PLAYER_STATE_STOP) { 
	        try {  
	            mMediaPlayer.stop();
	            mMediaPlayer.reset();
	            mMediaPlayer.setDataSource(ringTonePath);
	            mMediaPlayer.prepare();//prepare之后自动播放  
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
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mPlayerState = DOWNLOAD_PLAYER_STATE_PLAY;
		mp.setLooping(false);
		mp.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mMediaPlayer.stop();
		mPlayerState = DOWNLOAD_PLAYER_STATE_STOP;
		Log.e("101010", "----11onCompletion-----");
	}
	
	private void initViews( ) {
		mAuroraActionBar = getAuroraActionBar();
		mAuroraActionBar.setTitle(R.string.ringtone);
		setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		mAuroraActionBar.initActionBottomBarMenu(R.menu.aurora_menu_delete, 1);
		mDownLoadedText = (TextView)findViewById(R.id.local_ringtone_text);
		mNoDownloadText = (TextView)findViewById(R.id.no_downloadringtone_text);
		mLocalRingtoneList = (AuroraListView)findViewById(R.id.local_ringtone_list);
		
		mSelectLeftBtn = (TextView)mAuroraActionBar.getSelectLeftButton();
		mSelectLeftBtn.setTextColor(getResources().getColor(R.color.action_bar_selecte_text_color));
		mSelectLeftBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mLocalRingtoneListAdapter.setShowListCheckbox(!mLocalRingtoneListAdapter.getShowListCheckbox());
				mLocalRingtoneListAdapter.updateAdapter(audioDownloadDataPhone, audioDownloadDataMessage, true);
				showEditPanel(false);
			}
		});
		
		mSelectRightBtn = (TextView) mAuroraActionBar.getSelectRightButton();
		mSelectRightBtn.setTextColor(getResources().getColor(R.color.action_bar_selecte_text_color));
		mSelectRightBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(mSelectRightBtn.getText().equals(getResources().getText(R.string.select_all))) {
					for(int i = 0; i < audioDownloadDataPhone.size(); i++) {
						if(!mLocalRingtoneListAdapter.getCheckboxCheckList().contains(Integer.valueOf(i))) {
							mLocalRingtoneListAdapter.getCheckboxCheckList().add(Integer.valueOf(i));
						}
					}
					for(int i = 0; i < audioDownloadDataMessage.size(); i++) {
						if(!mLocalRingtoneListAdapter.getCheckboxCheckList().contains(Integer.valueOf(i+audioDownloadDataPhone.size()))) {
							mLocalRingtoneListAdapter.getCheckboxCheckList().add(Integer.valueOf(i+audioDownloadDataPhone.size()));
						}
					}
					mSelectRightBtn.setText(getResources().getText(R.string.no_select_all));
				} else {
					mLocalRingtoneListAdapter.getCheckboxCheckList().clear();
					mSelectRightBtn.setText(getResources().getText(R.string.select_all));
				}
				mLocalRingtoneListAdapter.updateAdapter(audioDownloadDataPhone, audioDownloadDataMessage, false);
			}
		});
		
		mLocalRingtoneList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if(mLocalRingtoneListAdapter.getShowListCheckbox()) {
					return false;
				}
				mLocalRingtoneListAdapter.setShowListCheckbox(!mLocalRingtoneListAdapter.getShowListCheckbox());
				if(position <= audioDownloadDataPhone.size()) {
					mLocalRingtoneListAdapter.getCheckboxCheckList().add(Integer.valueOf(position - 1));
				} else {
					mLocalRingtoneListAdapter.getCheckboxCheckList().add(
							Integer.valueOf(position - mLocalRingtoneListAdapter.getIndexForMessage()));
				}
				mLocalRingtoneListAdapter.updateAdapter(audioDownloadDataPhone, audioDownloadDataMessage, false);
				if(mLocalRingtoneListAdapter.getCheckboxCheckList().size() == (audioDownloadDataPhone.size()+ audioDownloadDataMessage.size())) {
					mSelectRightBtn.setText(getResources().getText(R.string.no_select_all));
				} else {
					mSelectRightBtn.setText(getResources().getText(R.string.select_all));
				}
				showEditPanel(true);
				return false;
			}
		});
		mLocalRingtoneList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if(mLocalRingtoneListAdapter.getShowListCheckbox()) {
					if(position <= audioDownloadDataPhone.size()) {
						if(mLocalRingtoneListAdapter.getCheckboxCheckList().contains(Integer.valueOf(position-1))) {
							mLocalRingtoneListAdapter.getCheckboxCheckList().remove(Integer.valueOf(position-1));
			        	} else {
			        		mLocalRingtoneListAdapter.getCheckboxCheckList().add(Integer.valueOf(position-1));
			        	}
					} else {
						if(mLocalRingtoneListAdapter.getCheckboxCheckList().contains(Integer.valueOf(position-mLocalRingtoneListAdapter.getIndexForMessage()))) {
							mLocalRingtoneListAdapter.getCheckboxCheckList().remove(Integer.valueOf(position-mLocalRingtoneListAdapter.getIndexForMessage()));
			        	} else {
			        		mLocalRingtoneListAdapter.getCheckboxCheckList().add(Integer.valueOf(position-mLocalRingtoneListAdapter.getIndexForMessage()));
			        	}
					}
					mLocalRingtoneListAdapter.updateAdapter(audioDownloadDataPhone, audioDownloadDataMessage, false);
					if(mLocalRingtoneListAdapter.getCheckboxCheckList().size() == audioDownloadDataPhone.size() + audioDownloadDataMessage.size()) {
						mSelectRightBtn.setText(getResources().getText(R.string.no_select_all));
					} else {
						mSelectRightBtn.setText(getResources().getText(R.string.select_all));
					}
				} else {
					//播放音频？
					int index = 0;
					StringBuilder ringTonePath = null;
					if(position <= audioDownloadDataPhone.size()) {
						index = position-1;
						ringTonePath = new StringBuilder(audioDownloadDataPhone.get(index).fileDir);
						ringTonePath.append("/").append(audioDownloadDataPhone.get(index).fileName);
					} else {
						index = position- mLocalRingtoneListAdapter.getIndexForMessage() - audioDownloadDataPhone.size();
						ringTonePath = new StringBuilder(audioDownloadDataMessage.get(index).fileDir);
						ringTonePath.append("/").append(audioDownloadDataMessage.get(index).fileName);
					}
					if(!ringTonePath.toString().equals(mCurrentRingTonePath)) {
						mPlayerState = DOWNLOAD_PLAYER_STATE_STOP;
					}
					mCurrentRingTonePath = ringTonePath.toString();
					playRingTone(mCurrentRingTonePath);
				}
			}
		});
		
		audioDownloadDataPhone = ((RingtongDatabaseController)RingtongDownloadService.getDownloadController()).getDownloadedDatasForRingToneType(0);
		audioDownloadDataMessage = ((RingtongDatabaseController)RingtongDownloadService.getDownloadController()).getDownloadedDatasForRingToneType(1);
		setLocalRingtoneListVisibility();
	}
	
	private void setLocalRingtoneListVisibility() {
		
		if(audioDownloadDataPhone.size() > 0 || audioDownloadDataMessage.size() > 0) {
			mLocalRingtoneListAdapter = new LocalRingtoneListAdapter(this, audioDownloadDataPhone, audioDownloadDataMessage);
			mLocalRingtoneList.setAdapter(mLocalRingtoneListAdapter);
			mLocalRingtoneListAdapter.setOnLocalItemApplyListener(new OnLocalItemApplyListener() {
				
				@Override
				public void setLocalItemApply(Theme theme, ThemeManager mThemeManager) {
					// TODO Auto-generated method stub
					mDownloadedRingTongThemeManager = mThemeManager;
					mDownloadedRingTongApplyTheme = theme;
					showAuroraMenu();
				}
			});
			mNoDownloadText.setVisibility(View.GONE);
		} else {
			mDownLoadedText.setVisibility(View.GONE);
			mLocalRingtoneList.setVisibility(View.GONE);
			mNoDownloadText.setVisibility(View.VISIBLE);
		}
	}
	
	private void showEditPanel(boolean isShow) {
		mAuroraActionBar.setShowBottomBarMenu(isShow);
		mAuroraActionBar.showActionBarDashBoard();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
			mPlayerState = DOWNLOAD_PLAYER_STATE_STOP;
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mPlayerState = DOWNLOAD_PLAYER_STATE_STOP;
		}
	}
}
