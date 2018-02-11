package com.aurora.audioprofile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.client.utils.URIUtils;

import com.aurora.audioprofile.adapter.SongItemAdapter;
import com.aurora.audioprofile.entity.Song;
import com.aurora.audioprofile.utils.AuroraPinYinUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settings.R;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;
import android.os.SystemProperties;
public class AuroraSongRingPickerActivity extends AuroraActivity implements AdapterView.OnItemClickListener{
	static{
		MediaPlayer.stopMusic = true;
	}
	private AuroraActionBar mActionBar;
	private ListView mSongList; 
	private ArrayList<Song> mSongListData;
	private MediaPlayer mPlayer;
	private int mClickedPos = -1;	
	private TextView mEmptyText;
	private int mSongId = -1;
	public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE;
	private boolean isItemClick;
	private boolean isPause;
	private TextView mOkButton;
	// 2014 05 05
	/*private View.OnClickListener mOkButtonOnClickListener = 
            new View.OnClickListener() {

        @Override
        public void onClick(View v) {
        	Intent resultIntent = new Intent();
        	if(mClickedPos != -1){
        		resultIntent.putExtra(AuroraRingPickerActivity.PICKED_SONG, mSongListData.get(mClickedPos));
            	setResult(AuroraRingPickerActivity.SONG_PICKED_RESULT_CODE, resultIntent);
        	}
        	
//        	stopRingtonePlay();
        	finish();
        }
    };*/
	
	private StorageManager mStorageManager;
	private StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
        	 if (newState.equals(Environment.MEDIA_SHARED)) {  
                 finish();
             
             } 
        }
    };
    
    private void sendResult(){
    	Intent resultIntent = new Intent();
    	if(mClickedPos != -1){
    		resultIntent.putExtra(AuroraRingPickerActivity.PICKED_SONG, mSongListData.get(mClickedPos));
        	setResult(AuroraRingPickerActivity.SONG_PICKED_RESULT_CODE, resultIntent);
    	}
    	
    	stopRingtonePlay();
    }
    
    private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener = new OnAuroraActionBarBackItemClickListener() {
		public void onAuroraActionBarBackItemClicked(int itemId) {
			switch (itemId) {
			case -1:
				sendResult();
				finish();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if(AuroraRingPickerActivity.mIsFullScreen){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setTheme(com.aurora.R.style.Theme_aurora);

		}else{
			setTheme(R.style.GnSettingsLightTheme);
		}
		super.onCreate(savedInstanceState);			
		
//		setAuroraContentView(R.layout.aurora_ring_listview_layout,AuroraActionBar.Type.Dashboard); // 2014 05 05
		setAuroraContentView(R.layout.aurora_ring_listview_layout,AuroraActionBar.Type.Normal);

		if(AuroraRingPickerActivity.mIsFullScreen){
			
			getWindow().setBackgroundDrawable(new BitmapDrawable(AuroraRingPickerActivity.mBitmap));			
			getAuroraActionBar().setBackgroundResource(R.drawable.mengban);
			LinearLayout ll = (LinearLayout)findViewById(R.id.ring_layout);
			ll.setBackgroundResource(R.drawable.mengban);
			
			
		}

		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		mSongList = (ListView) findViewById(R.id.ListView_ring);
		mEmptyText = (TextView)findViewById(R.id.empty_text);
		
		
		mSongId = getIntent().getIntExtra(AuroraRingPickerActivity.SONG_ID,-1);	
		Log.i("qy", "mSongId = " + mSongId );
		mSongList.setOnItemClickListener(this); 
		mSongList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		
		// deal with the actionbar's button listener		
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.other_ringtone_title);
		mActionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);
		
		// del 2014 05 05
	/*	mOkButton = mActionBar.getOkButton();
		mOkButton.setOnClickListener(mOkButtonOnClickListener);
		if(AuroraRingPickerActivity.mIsFullScreen){
			mActionBar.getCancelButton().setTextColor(this.getResources().getColorStateList(R.color.aurora_action_bar_button_change_color));
			if(mSongId == -1){
				mOkButton.setTextColor(this.getResources().getColor(R.color.summary));
//				mOkButton.setOnClickListener(null);
//				mOkButton.setFocusable(false);
				mOkButton.setEnabled(false);
			}else{
				mActionBar.getOkButton().setTextColor(this.getResources().getColorStateList(R.color.aurora_action_bar_button_change_color));
			}
			
		}else{
			if(mSongId == -1){
//				mOkButton.setTextColor(this.getResources().getColor(list_item_text_summary));
				mOkButton.setTextColor(this.getResources().getColor(R.color.okbutton_disable_color));
//				mOkButton.setOnClickListener(null);
//				mOkButton.setFocusable(false);
				mOkButton.setEnabled(false);
			}
		}
		
		
		mActionBar.getCancelButton().setOnClickListener(
	            new View.OnClickListener() {

	                @Override
	                public void onClick(View v) {
//	                	stopRingtonePlay();
	                    finish();
	                }
	            });
		
		*/
		
		
		
		
		// register storage event listener
        mStorageManager = StorageManager.from(this);
		mStorageManager.registerListener(mStorageListener);
		
	}	
	
	
	
	private void stopRingtonePlay(){
		try {
			if ( mPlayer.isPlaying()) {
				mPlayer.stop();
				mPlayer.release();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
    	
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(isItemClick && isPause){
			mSongList.setItemChecked(mClickedPos, true);
		}else{
			mSongListData = searchSongs();
			if(mSongListData.size()>0){
				Collections.sort(mSongListData, songNameComparator);
				
				
				if(mSongId != -1){
					for(int i=0;i< mSongListData.size();i++){
						Log.i("qy", "AuroraSongRingPickerActivity i= " +i);
						if((mSongListData.get(i).getId()) == mSongId){
							mClickedPos = i;
							Log.i("qy", "AuroraSongRingPickerActivity mClickedPos = " +mClickedPos);
							break;
						}else{
							mClickedPos = -1;
						}
					}		
					
				}
				
				
				mSongList.setAdapter(new SongItemAdapter(this, mSongListData, mClickedPos));
				/*mSongList.setOnItemClickListener(this); 
				mSongList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);*/
			}else{
				mEmptyText.setVisibility(View.VISIBLE);
				mSongList.setVisibility(View.GONE);
			}
			
			mSongList.setItemChecked(mClickedPos, true);
			//set selected position
			
			if(mClickedPos > 11){
				mSongList.setSelection(mClickedPos-5);
			}
			
		}
		

		
		isPause = false;
	}
	
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try {
			if ( mPlayer.isPlaying()) {
				mPlayer.stop();				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		isPause = true;
	}



	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		isItemClick = true;
		mClickedPos = position;
		// test
		/*String path = mSongListData.get(position).getFilePath();
		Log.i("qy", "path = "+path+"**");*/
		
		Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mSongListData.get(position).getId()); 
		try {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.reset();
		
//			mPlayer.setDataSource(path);
			mPlayer.setDataSource(this,songUri);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		mOkButton.setOnClickListener(mOkButtonOnClickListener);
		// del 2014 05 05
		/*mOkButton.setEnabled(true);
		if(AuroraRingPickerActivity.mIsFullScreen){
			mOkButton.setTextColor(this.getResources().getColorStateList(R.color.aurora_action_bar_button_change_color));
		}else{
			mOkButton.setTextColor(this.getResources().getColorStateList(R.color.aurora_action_bar_button_change_color_ringtone));
		}
		*/
		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub	    	
    	
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_BACK:
    	
//    		stopRingtonePlay();
    		sendResult();
    		finish();
    		return true;    	
    	default:    			
			return super.onKeyDown(keyCode, event);
    	}
    }
	
	private boolean existSDCard() {  
        if (gionee.os.storage.GnStorageManager
        		.getInstance(this).getExternalStoragePath() != null) {  
            return true;  
        } else  
            return false;  
    }
	
	private String getValidatePath(){
		 
		 StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
		 
		 StringBuilder sb = new StringBuilder();
		 
	    	for (int i = 0; i < storageVolumes.length; i++) {
	        	String state = mStorageManager.getVolumeState(storageVolumes[i].getPath());
	        	if(state.equals("mounted") ) {
	        		String path = storageVolumes[i].getPath().toString();
//	        		Log.i("qy", "**Valid*** path = "+path );
	        		String where = MediaStore.Audio.Media.DATA + " LIKE '" + path + "%'";
	        		sb.append(where);
	        		sb.append(" OR ");        		
	    	    	
	        	}
	    	}
	    	if(sb.length() >4){
	    		sb.delete(sb.length()-4, sb.length());
	    	}
	    	if(sb.length()>0){
	    		return sb.toString();
	    	}else{
	    		return null;
	    	}
	    	
	}
		
		
	
	
	private ArrayList<Song> searchSongs(){
		String[] projection = new String[] {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.TITLE
				 };
		// update list item date of usbotg
		if(!existSDCard()){
			String path = "/storage/usb" ;// song path ==/storage/usbotg/Pixie Lott - Nasty (2).mp3
			String where = MediaStore.Audio.Media.DATA + " LIKE '" + path + "%'"; 
			getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, null);
		}
		
		// query the date from databse
		ArrayList<Song> songList = new ArrayList<Song>();
		Cursor cr= null;
		String soundRecordPath = "/storage/sdcard_/通话录音/"; // filter this folder files
//		String emulatedPath = null;
		String deviceName = SystemProperties.get("ro.product.name");
        
        /*if (deviceName.contains("IUNI")) {
        	emulatedPath = "/storage/emulated/"; // u3
        }*/
		String validePaths = getValidatePath();
		Log.i("qy", "validePaths = " +validePaths );
		
		String memoPath = "/storage/sdcard_/note/";
		String soundRecordPathWhere = validePaths +" AND "+  MediaStore.Audio.Media.DATA + " NOT LIKE '" + soundRecordPath + "%'" /*+" AND "+ 
				MediaStore.Audio.Media.DATA + " NOT LIKE '" + emulatedPath + "%'" */
				+" AND "+ MediaStore.Audio.Media.DATA + " NOT LIKE '" + memoPath + "%'"; 
		try {
			 cr = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, soundRecordPathWhere,
					null, null); 
			Song song = null;
			
			if(cr!=null)
			{
				while (cr.moveToNext()) {
					song = new Song();
					song.setId(cr.getInt(cr
								.getColumnIndex(MediaStore.Audio.Media._ID)));
					String songName = cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
					if(songName != null){
						int indexTemp = songName.lastIndexOf('.');
						if(indexTemp > 0){
							songName = songName.substring(0, indexTemp);					
						}
						
						song.setDisplayName(songName);
						Log.i("qy", "song path ==" +cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.DATA)));
						song.setFilePath(cr.getString(
								cr.getColumnIndex(MediaStore.Audio.Media.DATA))
								.toLowerCase());
						song.setOrderName(AuroraPinYinUtils.getSpell(
								cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
								));
						songList.add(song);
					}else{
						String songTitleName = cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.TITLE));
						if(songTitleName != null){
							song.setDisplayName(songTitleName);
							Log.i("qy", "songTitleName path ==" +cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.DATA)));
							song.setFilePath(cr.getString(
									cr.getColumnIndex(MediaStore.Audio.Media.DATA))
									.toLowerCase());
							song.setOrderName(AuroraPinYinUtils.getSpell(songTitleName));
							songList.add(song);
							
						}
					}
				}
				cr.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			if(cr!=null)
			{
				cr.close();
			}
		}
		
		return songList;
		
	}
	
	private Comparator<Song> songNameComparator = new Comparator<Song>(){

		@Override
		public int compare(Song sn1, Song sn2) {
			// TODO Auto-generated method stub
			String name1 = sn1.getOrderName();
			String name2 = sn2.getOrderName();
			boolean isLetterOrDigit1 = Character.isLetterOrDigit(name1
					.charAt(0));
			boolean isLetterOrDigit2 = Character.isLetterOrDigit(name2
					.charAt(0));
			if (isLetterOrDigit1 && isLetterOrDigit2) {
				return name1.compareToIgnoreCase(name2);
			} else if (isLetterOrDigit1 && !isLetterOrDigit2) {
				return 1;
			} else if (!isLetterOrDigit1 && isLetterOrDigit2) {
				return -1;
			}
			return name1.compareToIgnoreCase(name2);
		}
		
	};
	
	
	 @Override
		protected void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			if (mStorageManager != null && mStorageListener != null) {
	            mStorageManager.unregisterListener(mStorageListener);
	        }
			if(mPlayer != null ){
				mPlayer.release();
				mPlayer = null;
			}
			
		}



}
