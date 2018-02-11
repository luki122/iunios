package com.aurora.audioprofile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.Toast;

import com.android.settings.R;
import com.aurora.audioprofile.entity.Song;
import com.aurora.audioprofile.utils.AuroraPinYinUtils;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.telecom.Connection;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.widget.HeaderViewListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ListAdapter;

import java.lang.String;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import com.aurora.audioprofile.utils.Blur;
import com.mediatek.audioprofile.AudioProfileManager;

import android.os.storage.StorageEventListener;


public class AuroraRingPickerActivity extends AuroraActivity implements Runnable,AdapterView.OnItemClickListener{
	
	static{
		MediaPlayer.stopMusic = true;
	}
	private ListView mRingtoneList; 
	private AuroraActionBar auroraActionBar;
	public static final String ACTION_RINGTONE_PICKER ="gn.com.android.audioprofile.action.RINGTONE_PICKER";
	private  String mTitle = null;
	 
	private static final String TAG = "RingtonePickerActivity";

    private static final int DELAY_MS_SELECTION_PLAYED = 300;

    private static final String SAVE_CLICKED_POS = "clicked_pos";
    /// M: Request codes to MusicPicker for add more ringtone
    private static final int ADD_MORE_RINGTONES = 1;

    private RingtoneManager mRingtoneManager;
    
    private Cursor mCursor;
    
    private Cursor mOrderCursor = null;
    
    private Handler mHandler;

    /** The position in the list of the 'Silent' item. */
    private int mSilentPos = -1;
    
    /** The position in the list of the 'Default' item. */
    private int mDefaultRingtonePos = -1;

    /** The position in the list of the last clicked item. */
    private int mClickedPos = -1;
    
    /** The position in the list of the ringtone to sample. */
    private int mSampleRingtonePos = -1;

    /** Whether this list has the 'Silent' item. */
    private boolean mHasSilentItem;
    
    /** The Uri to place a checkmark next to. */
    private Uri mExistingUri;
    
    /** The number of static items in the list. */
    private int mStaticItemCount;
    
    /** Whether this list has the 'Default' item. */
    private boolean mHasDefaultItem;
    
    /** The Uri to play when the 'Default' item is clicked. */
    private Uri mUriForDefaultItem;
    
    /** M: Whether this list has the 'More Ringtongs' item. */
    private boolean mHasMoreRingtonesItem = false;
    
    /** M: The position in the list of the 'More Ringtongs' item. */
    private int mMoreRingtonesPos = -1;
    
    /** M: The ringtone type to show and add in the list. */
    private int mType = -1;
    
    private boolean isSmsList  =  false;  //是否是短信铃声界面
    
    /** M: Whether need to refresh listview after activity on resume. */
    private boolean mNeedRefreshOnResume = false;
    
    /**
     * A Ringtone for the default ringtone. In most cases, the RingtoneManager
     * will stop the previous ringtone. However, the RingtoneManager doesn't
     * manage the default ringtone for us, so we should stop this one manually.
     */
    private Ringtone mDefaultRingtone;
	private String typeContactRing;
	
	private static final int SONG_RING_ACTIVITY_REQUEST_CODE =100;
	public static final int SONG_PICKED_RESULT_CODE = 101;
	public static final String PICKED_SONG = "picked_song";
	public static final String SONG_DISPLAY_NAME = "song_display_name";
	public static final String SONG_ID = "song_id";
	
	public static final String VALUE_RINGTONE_SILENT = "silent";
	private RadioButton mItemRadioBtn ;
	private TextView mSongName;
	private Uri mSongUri;
	private String mSongDisplayName;
	private int OTHER_SONG_LOC;
	private Uri mDefaultRingtoneUri;  
	private Uri mDefaultNotificationUri; 
	private Uri mDefaultAlarmUri;
	public static Bitmap mBitmap;
	private int mSongId = -1;
	//add by luolaigang for out folder music 
	private SharedPreferences sharedPreferences;
	private static final String SAVE_FOLDER = "save_folder";
	private ArrayList<Song> musicNames;
	private int MUSICNAMES_INDEX;
	private MediaPlayer mPlayer;
	private static final int STARTHOUR = 6;
    private static final String LOCKSCREENDEFAULTPATH = "/system/iuni/aurora/change/lockscreen/Timely/";
    public static boolean mIsFullScreen =false;
    private boolean mIsChronometer = false;
    private boolean mIsNativeStart = false;  //是否是从设置中启动的
    
    private  AudioProfileManager mProfileManager;
    
    private StorageManager mStorageManager;
	private StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
        	 if (newState.equals(Environment.MEDIA_SHARED)) {  
                 finish();
             
             } 
        }
    };

	 
	 
    private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener = new OnAuroraActionBarBackItemClickListener() {
			public void onAuroraActionBarBackItemClicked(int itemId) {
				switch (itemId) {
				case -1:
					// setMenuEnable(true);
//					Toast.makeText(AuroraRingPickerActivity.this, "getCancelBtn",
//							 Toast.LENGTH_SHORT).show();
					sendResult();
					if(mIsNativeStart){
	        			Intent t = new Intent("gn.com.android.audioprofile.action.AUDIO");
	                	startActivity(t);
	                	overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);
	        		}
					
					finish();
					break;
				default:
					break;
				}
			}
		};
		
	private void sendResult(){
		Intent resultIntent = new Intent(AuroraRingPickerActivity.this ,AudioProfileActivity.class);
        Uri uri = null;
        Log.i(TAG, "mClickedPos="+mClickedPos);
        if (mClickedPos == mDefaultRingtonePos) {
            // Set it to the default Uri that they originally gave us
            uri = mUriForDefaultItem;
            Log.i(TAG, "sendResult  *** mDefaultRingtonePos" );
        } else if (mClickedPos == mSilentPos) {
            // A null Uri is for the 'Silent' item
            uri = Uri.parse(VALUE_RINGTONE_SILENT);
            Log.i(TAG, "sendResult  *** mSilentPos" );
        } else if (mClickedPos == OTHER_SONG_LOC){
        	uri= mSongUri;
        	Log.i(TAG, "sendResult  *** OTHER_SONG_LOC" );
        } else if(mClickedPos<=MUSICNAMES_INDEX) {
        	uri= mSongUri;
        	Log.i(TAG, "sendResult  *** OTHER_SONG_LOC" );
        }else {
            uri = mRingtoneManager.getRingtoneUri(getRingtoneManagerPosition(mClickedPos));
        }
        Log.i(TAG, "sendResult  *** uri = " +  (uri == null ? "null" : uri.toString()));

        // save the uri
        if((mType == RingtoneManager.TYPE_NOTIFICATION) || (mType == RingtoneManager.TYPE_RINGTONE &&!mTitle.equals(typeContactRing))|| 
        		(mType == RingtoneManager.TYPE_ALARM &&!mIsChronometer)){
        	Log.i(TAG, "sendResult  *** mType =  "  + mType);
        	if(isSmsList){
        		Log.i(TAG, "sendResult  *** isSmsList " );
        		Settings.System.putString(getContentResolver(), "sms_sound", uri.toString());
        	} else {
        		if(mProfileManager != null){
        			mProfileManager.setRingtoneUri("mtk_audioprofile_general", mType, -1, uri);
        		}else{
        			RingtoneManager.setActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType, uri); 
        		}
        		
        	}
        }

        resultIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, uri);
       
        setResult(RESULT_OK, resultIntent);
        
	}
	
	private String getKey(){
		return SAVE_FOLDER+mType+isSmsList;
	}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			//记录是否选择文件夹音乐
			Editor editor = sharedPreferences.edit();
			stopAnyPlayingRingtone();
			// TODO Auto-generated method stub
			if( position == OTHER_SONG_LOC){
				editor.putBoolean(getKey(), false);
				Intent songRingIntent = new Intent(this,AuroraSongRingPickerActivity.class);
				songRingIntent.putExtra(SONG_ID,mSongId);
				startActivityForResult(songRingIntent, SONG_RING_ACTIVITY_REQUEST_CODE);
			}else if(position<=MUSICNAMES_INDEX){
				editor.putBoolean(getKey(), true);
				mSongUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicNames.get(position-1).getId()); 
				Log.i(TAG, "mSongUri="+mSongUri.toString() );
				mClickedPos = position;
				mItemRadioBtn.setChecked(false);
				sendResult();
				try {
					mPlayer.reset();
					mPlayer.setDataSource(this,mSongUri);
					mPlayer.prepare();
					mPlayer.start();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				editor.putBoolean(getKey(), false);
				mClickedPos = position;
				mItemRadioBtn.setChecked(false);

	            // Play clip
	            playRingtone(position, 0);
			}
			editor.commit();
		}
		
		public static Bitmap getLockScreenFromDefaultPath(Context context,String indexstring) {
	    	
	    	/*String indexstring;
	    	int index;
	    	Calendar c = Calendar.getInstance();
	        c.setTimeInMillis(System.currentTimeMillis());
	        int hour = c.get(Calendar.HOUR_OF_DAY);
	        
	        if ( hour >= STARTHOUR ) {
	        	index = (hour - STARTHOUR) / 2 + 1;
	        } else {
	        	index = hour / 2 + 10;
	        }
	        
	        if ( index < 10 ) {
	        	indexstring = "0" + String.valueOf(index);
	        } else {
	        	indexstring = String.valueOf(index);
	        }

	        indexstring = LOCKSCREENDEFAULTPATH + "data" + indexstring + ".png";*/
	        
	       
	        
	        FileInputStream fis = null;
	        Bitmap bitmap = null;
	        File file = new File(indexstring);
	        try {
	            fis = new FileInputStream(indexstring);
	    		BitmapFactory.Options opts = new BitmapFactory.Options();
	    		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
	    		opts.inPurgeable = true;
	    		opts.inInputShareable = true;
	    		opts.inSampleSize = 4;
	    		Drawable drawable = Drawable.createFromResourceStream(context.getResources(), null, fis, "src", opts);
	    		bitmap = ((BitmapDrawable)drawable).getBitmap();
	        } catch (FileNotFoundException e) {
	        	e.printStackTrace();
	            
	        } finally {
	            if (fis != null) {
	                try {
	                    fis.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
			
	        return bitmap;
	    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// adapt to alarm clock
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		
		if (!Environment.getExternalStorageState().equals(  
                Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, R.string.internal_storage_disable,Toast.LENGTH_SHORT ).show();
			finish();
			return;
		}

		mIsFullScreen = false;
		Intent intent = getIntent();
		
//		mBitmap = (Bitmap)intent.getParcelableExtra("deskclock");
		
		mIsNativeStart = intent.getBooleanExtra("native_start", false);
		mIsFullScreen = intent.getBooleanExtra("fullscreen", false);
		mIsChronometer = intent.getBooleanExtra("chronometer", false);

		String path = intent.getStringExtra("lockscreenpath");
		if(mIsFullScreen && path !=null){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setTheme(com.aurora.R.style.Theme_aurora);			
			mBitmap = getLockScreenFromDefaultPath(this,path);
			if(mBitmap != null){
				mBitmap = Blur.fastblur(this, mBitmap, 23);
			}
			

		}else{
			setTheme(R.style.GnSettingsLightTheme);
		}


		setAuroraContentView(R.layout.aurora_ring_listview_layout,AuroraActionBar.Type.Normal);
		
		typeContactRing = getString(R.string.set_ringtone);
		// adapt to alarm clock
		if(mIsFullScreen){
			if(mBitmap != null){
				getWindow().setBackgroundDrawable(new BitmapDrawable(mBitmap));
			}
			
			getAuroraActionBar().setBackgroundResource(R.drawable.mengban);
			LinearLayout ll = (LinearLayout)findViewById(R.id.ring_layout);
			ll.setBackgroundResource(R.drawable.mengban);


		}
		
		// remove the other song ringtone list
		 ContentValues values = new ContentValues();
	    values.put(MediaStore.Audio.Media.IS_RINGTONE, false);  
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);  
        values.put(MediaStore.Audio.Media.IS_ALARM, false);  
        values.put(MediaStore.Audio.Media.IS_MUSIC, false); 
        try {
        	getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,values,null,null); 
		} catch (Exception e) {
			// TODO: handle exception
		}
               
		
		
		mHandler = new Handler();

        
        
        mTitle = (String)intent.getCharSequenceExtra(RingtoneManager.EXTRA_RINGTONE_TITLE);
        if (mTitle == null) {
        	mTitle = getString(com.android.internal.R.string.ringtone_picker_title);
        }
        
        auroraActionBar = getAuroraActionBar();
		auroraActionBar.setTitle(mTitle);  
		auroraActionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);
        
        
        //mHasDefaultItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        mUriForDefaultItem = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
        if (mUriForDefaultItem == null) {
            mUriForDefaultItem = Settings.System.DEFAULT_RINGTONE_URI;
        }

        
        // Get whether to show the 'Silent' item
        //mHasSilentItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);  		
		mRingtoneManager = new RingtoneManager(this);
		
		mProfileManager = (AudioProfileManager)getSystemService(Context.AUDIO_PROFILE_SERVICE);
		
		 // Get whether to include DRM ringtones
        final boolean includeDrm = intent.getBooleanExtra(
                RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM, true);
        mRingtoneManager.setIncludeDrm(includeDrm);

        // Get the types of ringtones to show
        mType = intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, -1);
        
        if(mType == AudioProfileActivity.TYPE_RINGTONE_SMS){
        		isSmsList = true;
        		mType = RingtoneManager.TYPE_NOTIFICATION;
        } else {
        	isSmsList = false;
        }
        
        if (mType != -1) {
            mRingtoneManager.setType(mType);
        }		
	
		mCursor = mRingtoneManager.getCursor();	
		ContentResolver cr = getContentResolver();
		if(mType == RingtoneManager.TYPE_RINGTONE){
		mOrderCursor = cr.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.IS_RINGTONE + "='1'", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		} else if(mType == RingtoneManager.TYPE_NOTIFICATION || mType == AudioProfileActivity.TYPE_RINGTONE_SMS){
			mOrderCursor = cr.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.IS_NOTIFICATION + "='1'", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		} else if(mType == RingtoneManager.TYPE_ALARM){
			mOrderCursor = cr.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.IS_ALARM + "='1'", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		}
		if(mOrderCursor !=null){
		startManagingCursor(mOrderCursor);  //mOrderCursor交由activity管理，mOrderCursor的生命周期就和activity自动同步了
		}
			
		Uri mDefaultUri = getDefaultUri(mType, mCursor);
		// search the default ringtone uri and the default notification uri		
		if(mType == RingtoneManager.TYPE_NOTIFICATION || mType == AudioProfileActivity.TYPE_RINGTONE_SMS){
			mDefaultNotificationUri = mDefaultUri;
		}else if(mType == RingtoneManager.TYPE_RINGTONE){
			mDefaultRingtoneUri = mDefaultUri;
		} else if(mType == RingtoneManager.TYPE_ALARM){
			mDefaultAlarmUri = mDefaultUri;
		} 		
		
		// The volume keys will control the stream that we are choosing a ringtone for
        setVolumeControlStream(mRingtoneManager.inferStreamType());

        // Get the URI whose list item should have a checkmark
//        mExistingUri = intent
//                .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
//        
//        mExistingUri = RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType);
   
        if(mTitle.equals(typeContactRing) || mIsChronometer){
        	Log.i(TAG, "onCreate1");
        	mExistingUri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
        	
        }else{
        	if(isSmsList){
        		String smsUri =  Settings.System.getString(cr, "sms_sound");
        		if(null != smsUri){
        			Log.i(TAG, "onCreate2");
        			mExistingUri = Uri.parse(smsUri);
        		}else{
        			//fixed bug 10418
        			//mExistingUri = RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType);
        			Log.i(TAG, "onCreate3");
        			mExistingUri = mProfileManager.getRingtoneUri("mtk_audioprofile_general", mType, -1);
        			if(!mDefaultNotificationUri.equals(mExistingUri)){
        				Log.i(TAG, "onCreate4");
        				mExistingUri = mDefaultNotificationUri;
        			}
        		}
        		
        	}else{
        		if(mProfileManager != null){
        			Log.i(TAG, "onCreate5");
        			mExistingUri = mProfileManager.getRingtoneUri("mtk_audioprofile_general", mType, -1);
        		}else{
        			Log.i(TAG, "onCreate6");
        			mExistingUri = RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType);
        		}
        	}
        	
        }
        
        Log.i(TAG, "onCreate  *** mExistingUri = " +  (mExistingUri == null ? "null" : mExistingUri.toString()));
        // chronometer        
        /*if( mExistingUri == null && mIsChronometer){
        	while (mCursor.moveToNext()) {
				if(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).equals("Ringing Alarm2")){
					int ringtonId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
					mExistingUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, ringtonId);
					Log.i(TAG, "chronometer Uri ==" +mExistingUri.toString());
					break;
				}
				
			}
        }*/
        String []from1= new String[1];
        if(null != mOrderCursor){
	        if(getResources().getConfiguration().locale.getCountry().equals("CN")){
	        
	        	from1[0]=MediaStore.Audio.Media.TITLE;
	        }else{
	        	
	        	from1[0]=MediaStore.Audio.Media.DISPLAY_NAME;
	        }
        } else {
        	from1[0]=MediaStore.Audio.Media.TITLE;
        }
        
		//String []from1={MediaStore.Audio.Media.TITLE+""};
        int[]to1={android.R.id.text1};
		
        SimpleCursorAdapter ringCursorAdapter =null;
        mRingtoneList = (ListView) findViewById(R.id.ListView_ring);  
     
        
        // adapt to the alarm clock
        if(null != mOrderCursor){
	        if(mIsFullScreen){
	        	ringCursorAdapter = new MySimpleCursorAdapter(this, R.layout.aurora_ring_listitem_alarm, mOrderCursor, from1, to1);
	        }else{
	        	ringCursorAdapter = new MySimpleCursorAdapter(this, R.layout.aurora_ring_listitem, mOrderCursor, from1, to1);
	        }
	        ringCursorAdapter.setViewBinder(viewBinder);
        } else {
        	 if(mIsFullScreen){
             	ringCursorAdapter = new MySimpleCursorAdapter(this, R.layout.aurora_ring_listitem_alarm, mCursor, from1, to1);
             }else{
             	ringCursorAdapter = new MySimpleCursorAdapter(this, R.layout.aurora_ring_listitem, mCursor, from1, to1);
             }
        }
        
        prepareListView(mRingtoneList);
        
        mRingtoneList.setOnItemClickListener(this);  
//        mylist.setOnItemSelectedListener(this);  
        mRingtoneList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);//.CHOICE_MODE_SINGLE);  
 
        if (savedInstanceState != null) {
            mClickedPos = savedInstanceState.getInt(SAVE_CLICKED_POS, -1);
            Log.i(TAG, "mClickedPos10="+mClickedPos);
        }
        // register storage event listener
        mStorageManager = StorageManager.from(this);
		mStorageManager.registerListener(mStorageListener);		
		Log.i("luolaigang", "mClickedPos:"+mClickedPos);
		mRingtoneList.setAdapter(ringCursorAdapter);  
		
		createPhoneListener();
	}
	
	public static Uri getDefaultUri(int mType, Cursor mCursor){
		Uri mDefaultUri = null;
		// search the default ringtone uri and the default notification uri		
				if(mType == RingtoneManager.TYPE_NOTIFICATION || mType == AudioProfileActivity.TYPE_RINGTONE_SMS){
		//t			mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mDefaultNotificationUri));
					while (mCursor.moveToNext()) {
						if(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("光点（默认）")
							||mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("光点")	){
							int notificationId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
							mDefaultUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, notificationId);
							break;
						}
						
					}
					
				}else if(mType == RingtoneManager.TYPE_RINGTONE){
					
					while (mCursor.moveToNext()) {
						if(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("iuni音调（默认）")
							||mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("iuni 音调")){
							int ringtonId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
							mDefaultUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, ringtonId);
							break;
						}
						
					}
				} else if(mType == RingtoneManager.TYPE_ALARM){
					
					while (mCursor.moveToNext()) {
						if(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("清晨（默认）")
							||mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("清晨")){
							int ringtonId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
							mDefaultUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, ringtonId);
							break;
						}
						
					}
				} 
		return mDefaultUri;
	}
	
    private SimpleCursorAdapter.ViewBinder viewBinder=new SimpleCursorAdapter.ViewBinder() {     
        
        @Override  
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {  
       if(!getResources().getConfiguration().locale.getCountry().equals("CN")){
         if(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)==columnIndex){    //DISPLAY_NAME为数据库中对应的属性列  
          TextView tv=(TextView)view;  
      String 	title =cursor.getString(columnIndex);   
		int indexTemp = title.lastIndexOf('.');
		if(indexTemp > 0){
			title = title.substring(0, indexTemp);					
		}            		
          tv.setText(title);  
          return true;  
         }  
       }
         return false;  
        }  
       };  
	
	public class  MySimpleCursorAdapter extends SimpleCursorAdapter{

		

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			AuroraCheckedTextViewWithTitle auroraCheckedTextViewWithTitle = (AuroraCheckedTextViewWithTitle)view;
			if(position == 0){
	    		auroraCheckedTextViewWithTitle.setTitle(getResources().getString(R.string.system_ringtone).toString());
			}else{
	    		auroraCheckedTextViewWithTitle.setTitle(null);
			}
			if(mClickedPos>MUSICNAMES_INDEX){
				auroraCheckedTextViewWithTitle.setChecked( (mClickedPos-(MUSICNAMES_INDEX+1))== position);
			}
			return view;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		mRingtoneList.setItemChecked(mClickedPos, true);
	}
	
	
	
	/**
     * Adds a static item to the top of the list. A static item is one that is not from the
     * RingtoneManager.
     *
     * @param listView The ListView to add to.
     * @param textResId The resource ID of the text for the item.
     * @return The position of the inserted item.
     * com.android.internal.R.layout.select_dialog_singlechoice_holo
     */
    private int addStaticItem(ListView listView, int textResId) {
    	TextView textView;
    	if(mType == RingtoneManager.TYPE_ALARM){
    		 textView = (TextView) getLayoutInflater().inflate(
             		R.layout.aurora_ring_listitem_alarm, listView, false);
        /* textView = (TextView) getLayoutInflater().inflate(
        		R.layout.aurora_ring_listitem, listView, false);*/
    	}else{
    		/* textView = (TextView) getLayoutInflater().inflate(
            		R.layout.aurora_ring_listitem_alarm, listView, false);*/
    		 textView = (TextView) getLayoutInflater().inflate(
    	        		R.layout.aurora_ring_listitem, listView, false);
    	}
        textView.setText(textResId);        
        listView.addHeaderView(textView);
        mStaticItemCount++;
        return listView.getHeaderViewsCount() - 1;
    }
    
    private int addFolderRingtoneItem(ListView listView, String name, boolean firstItem, int title) {
    	TextView textView;
    	View convertView = null;
    	if(mType == RingtoneManager.TYPE_ALARM){
    		convertView = getLayoutInflater().inflate(R.layout.aurora_ring_listitem_alarm, listView, false);
    	}else{
    		convertView =  getLayoutInflater().inflate(R.layout.aurora_ring_listitem, listView, false);
    	}
    	if(firstItem){
			AuroraCheckedTextViewWithTitle auroraCheckedTextViewWithTitle = (AuroraCheckedTextViewWithTitle)convertView;
    		auroraCheckedTextViewWithTitle.setTitle(getResources().getString(title).toString());
		}else{
			AuroraCheckedTextViewWithTitle auroraCheckedTextViewWithTitle = (AuroraCheckedTextViewWithTitle)convertView;
    		auroraCheckedTextViewWithTitle.setTitle(null);
		}
    	textView =(TextView)convertView.findViewById(android.R.id.text1);		
        textView.setText(name);        
        listView.addHeaderView(textView);
        mStaticItemCount++;
        return listView.getHeaderViewsCount() - 1;
      }
    
    private int addOtherRingtoneItem(ListView listView) {
        
      //adapt to the alarm clock
    	View view =null;
    	
        if(mIsFullScreen){
        	view =  getLayoutInflater().inflate(
            		R.layout.aurora_other_ring_listitem_alarm, listView, false);
        	
        }else{
        	view =  getLayoutInflater().inflate(
            		R.layout.aurora_other_ring_listitem, listView, false);
        }
        mSongName = (TextView)view.findViewById(R.id.song_name);
        
        mItemRadioBtn = (RadioButton)view.findViewById(R.id.rb_other_ringtone);
        mSongName.setText(R.string.none_select);
        
        
        listView.addHeaderView(view);
        mStaticItemCount++;
        return listView.getHeaderViewsCount() - 1;
    }
     
	
    private int addDefaultRingtoneItem(ListView listView) {
        if (mType == RingtoneManager.TYPE_NOTIFICATION) {
            return addStaticItem(listView, R.string.notification_sound_default);
        } else if (mType == RingtoneManager.TYPE_ALARM) {
            return addStaticItem(listView, R.string.alarm_sound_default);
        }

        return addStaticItem(listView, R.string.ringtone_default);
    }

    private int addSilentItem(ListView listView) {
        return addStaticItem(listView, R.string.silent_settings_title);
    }

    public static final String THEME_RINGTONG_DOWNLLOAD_PATH =  Environment.getExternalStorageDirectory()+"/IUNI/theme/download/ringtong/";
    
    public void prepareListView(ListView listView) {

        if (mHasDefaultItem) {
            mDefaultRingtonePos = addDefaultRingtoneItem(listView);

            if (RingtoneManager.isDefault(mExistingUri)) {
                mClickedPos = mDefaultRingtonePos;
                Log.i(TAG, "mClickedPos1="+mClickedPos);
            }
        }
        //test  mHasSilentItem***************************
        if (mHasSilentItem) {
            mSilentPos = addSilentItem(listView);

            // The 'Silent' item should use a null Uri
            if (mExistingUri == null) {
                mClickedPos = mSilentPos;
                Log.i(TAG, "mClickedPos2="+mClickedPos);
            }
        }

    	addOtherRingtoneItem(listView);
    	Log.i(TAG, Environment.getExternalStorageDirectory().toString());
    	musicNames = searchThemeSongs((mType == RingtoneManager.TYPE_RINGTONE)?0:1);
    	//searchSongs(THEME_RINGTONG_DOWNLLOAD_PATH);
    	if(musicNames.size()>0){
			Collections.sort(musicNames, songNameComparator);
    	}
    	for(int i=0; i<musicNames.size(); i++){
    		MUSICNAMES_INDEX = addFolderRingtoneItem(listView, musicNames.get(i).getDisplayName(), i==0, R.string.self_ringtone);
    	}
    	// deal with the loc of song item
    	if (!mHasSilentItem && !mHasDefaultItem){
    		OTHER_SONG_LOC =0;
    	}else if (mHasSilentItem && !mHasDefaultItem ||!mHasSilentItem && mHasDefaultItem ){
    		OTHER_SONG_LOC =1;
    	}else if(mHasSilentItem && mHasDefaultItem){
    		OTHER_SONG_LOC =2;
    	}
        		
        
        
        if (mClickedPos == -1) {
            mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mExistingUri));   
            Log.i(TAG, "mClickedPos3="+mClickedPos);
            // qy compatability e6 .
            if(mClickedPos == -1){
            	mSongUri = mExistingUri;
            	mClickedPos = OTHER_SONG_LOC;
            	Log.i(TAG, "mClickedPos4="+mClickedPos);
            	String title = null /*RingtoneManager.getRingtone(this, mExistingUri).getTitle(this)*/;
            	int id = -1;

            	try{
            		id = (int)ContentUris.parseId(mExistingUri);
            	}catch(Exception e){
            		Log.i(TAG, e.toString());
            	}
            	
            	Cursor cr = getContentResolver().query(
        				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
        						MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.TITLE}, MediaStore.Audio.Media._ID + "=?",
        				new String[] {id+""}, null);
            	
            	//add by jiyouguang  fixed bug 11352
            	//如果短信的铃声被删除了，此时短信铃声则显示通知铃声的位置 
            	if(isSmsList && null != cr &&cr.getCount() == 0){
            		
            		try{
                		id = (int)ContentUris.parseId(RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType));
                	}catch(Exception e){
                		Log.i(TAG, e.toString());
                	}
                	   cr = getContentResolver().query(
            				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
            						MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME}, MediaStore.Audio.Media._ID + "=?",
            				new String[] {id+""}, null);
                	   if (cr.moveToFirst() && cr.getCount() > 0){
                		   mSongUri = RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType);
                	   }
            	}
            	//end
            	if (cr.moveToFirst() && cr.getCount() > 0) { 
            		title = cr.getString(1);   
            		if(title != null){
	            		int indexTemp = title.lastIndexOf('.');
	    				if(indexTemp > 0){
	    					title = title.substring(0, indexTemp);					
	    				}      
            		}else{
            			title = cr.getString(2);  
            		}

                	mSongDisplayName = title;              	
                	mSongId =id ;/*cr.getInt(cr.getColumnIndex(MediaStore.Audio.Media._ID))*/ // 2014 07 01
                	updateOtherRingtoneItem(title,true);
            	}else{
            		
            		updateOtherRingtoneItem(getString(R.string.none_select),false);
            		if(mType == RingtoneManager.TYPE_NOTIFICATION){
            			mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mDefaultNotificationUri));
            			Log.i(TAG, "mClickedPos5="+mClickedPos);
            		}else if(mType == RingtoneManager.TYPE_RINGTONE){
            			mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mDefaultRingtoneUri));
            			Log.i(TAG, "mClickedPos6="+mClickedPos);
            		}else if(mType == RingtoneManager.TYPE_ALARM){
            			mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mDefaultAlarmUri));
            			Log.i(TAG, "mClickedPos7="+mClickedPos);
            		}

            	}
            	
            }
            
        }

        
    }
    
	 @Override
	 public void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        outState.putInt(SAVE_CLICKED_POS, mClickedPos);
	    }
	
	 
	 private void playRingtone(int position, int delayMs) {
	        mHandler.removeCallbacks(this);
	        mSampleRingtonePos = position;
	        mHandler.postDelayed(this, delayMs);
	    }
	 
	 public void run() {

	        if (mSampleRingtonePos == mSilentPos) {
	            mRingtoneManager.stopPreviousRingtone();
	            return;
	        }

	        /*
	         * Stop the default ringtone, if it's playing (other ringtones will be
	         * stopped by the RingtoneManager when we get another Ringtone from it.
	         */
	        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
	            mDefaultRingtone.stop();
	            mDefaultRingtone = null;
	        }

	        Ringtone ringtone;
	        if (mSampleRingtonePos == mDefaultRingtonePos) {
	            if (mDefaultRingtone == null) {
	                mDefaultRingtone = RingtoneManager.getRingtone(this, mUriForDefaultItem);
	            }
	           /*
	            * Stream type of mDefaultRingtone is not set explicitly here.
	            * It should be set in accordance with mRingtoneManager of this Activity.
	            */
	            if (mDefaultRingtone != null) {
	                mDefaultRingtone.setStreamType(mRingtoneManager.inferStreamType());
	            }
	            ringtone = mDefaultRingtone;

	            /*
	             * Normally the non-static RingtoneManager.getRingtone stops the
	             * previous ringtone, but we're getting the default ringtone outside
	             * of the RingtoneManager instance, so let's stop the previous
	             * ringtone manually.
	             */
	            mRingtoneManager.stopPreviousRingtone();

	        } else {
	            ringtone = mRingtoneManager.getRingtone(getRingtoneManagerPosition(mSampleRingtonePos));
	        }

	        if (ringtone != null) {
	            ringtone.play();
	        }
	    }
	 
	 private Uri getRingtoneUri(int position){
		 if (mOrderCursor == null || !mOrderCursor.moveToPosition(position)) {
	            return null;
	        }
	        
	        return ContentUris.withAppendedId(Uri.parse(mOrderCursor.getString(2)), mOrderCursor
                    .getLong(0));
	 }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        stopAnyPlayingRingtone();
	     
	    }
	    
	    

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

		@Override
	    protected void onPause() {
			/**
			 * when this activity going to onPause,we must update audio type 
			 * in database
			 */
		try {
			if (mSongUri != null) {
				updateRingtong(mSongUri, mType);
			}
			if (mExistingUri != null
					&& !RingtoneManager.isDefault(mExistingUri)) {
				updateRingtong(mExistingUri, mType);
			}
		}catch(Exception e){
				Log.d(TAG, "update url exception:"+e);
			}
	        super.onPause();
	        sendResult();
	        stopAnyPlayingRingtone();
	        stopRingtonePlay();
	    }

	    private void stopAnyPlayingRingtone() {

	        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
	            mDefaultRingtone.stop();
	        }

	        if (mRingtoneManager != null) {
	            mRingtoneManager.stopPreviousRingtone();
	        }
	    }

	    private int getRingtoneManagerPosition(int listPos) {
	        return listPos - mStaticItemCount;
	    }

	    private int getListPosition(int ringtoneManagerPos) {
	    	boolean isFolderMusic = sharedPreferences.getBoolean(getKey(), false);
        	Log.i("RingtonePickerActivity", ringtoneManagerPos+"默认"+isFolderMusic);
        	if(isFolderMusic){
        		int id = -1;
    			try{
            		id = (int)ContentUris.parseId(mExistingUri);
            		Log.i("RingtonePickerActivity", "id="+id);
            	}catch(Exception e){
            	}
        		for(int i=0; i<musicNames.size(); i++){     
        			Log.i("RingtonePickerActivity", "musicNames.get(i).getId()="+musicNames.get(i).getId());
        			if(id ==  musicNames.get(i).getId()){
        				mSongUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicNames.get(i).getId()); 
        				ringtoneManagerPos = mClickedPos = MUSICNAMES_INDEX-musicNames.size()+i+1;
        				Log.i(TAG, "mClickedPos8="+mClickedPos);
        				break;
        			}
        		}
        		return ringtoneManagerPos;
        	}else{
        		// If the manager position is -1 (for not found), return that
    	        if (ringtoneManagerPos == -1) return ringtoneManagerPos;
    	        return ringtoneManagerPos + mStaticItemCount;
        	}
	    }
	    
	    
	  /*  @Override
	    public void finish() {
	    	// TODO Auto-generated method stub
	    	overridePendingTransition(R.anim.aurora_activity_close_enter,R.anim.aurora_activity_close_exit);
	    	super.finish();
	    	sendResult();
	    	
	    }*/
	    
	    
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub	    	
	    	
	    	switch (keyCode) {
        	case KeyEvent.KEYCODE_BACK:
        	
        		sendResult();
        		if(mIsNativeStart){
        			Intent t = new Intent("gn.com.android.audioprofile.action.AUDIO");
                	startActivity(t);
                	overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);
        		}
        		
            	finish();
        		return true;
        	
        	default:
        			
    			return super.onKeyDown(keyCode, event);
	    	}
	    }
	    private void updateOtherRingtoneItem(String str,boolean isChecked){	    	
	    	
	    	mItemRadioBtn.setChecked(isChecked);
			mSongName.setText(str);
	    }
	  
	    
	    
	    @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			// TODO Auto-generated method stub
			super.onActivityResult(requestCode, resultCode, data);
			if(requestCode == SONG_RING_ACTIVITY_REQUEST_CODE && resultCode == SONG_PICKED_RESULT_CODE){
				Song pickedSong = data.getParcelableExtra(AuroraRingPickerActivity.PICKED_SONG);
				Log.i(TAG, "onResult  *** RingPicker SongName =" +pickedSong.getDisplayName());
				mClickedPos = OTHER_SONG_LOC;
				updateOtherRingtoneItem(pickedSong.getDisplayName(),true);
				
				
//				Uri newUri = MediaStore.Audio.Media.getContentUriForPath(pickedSong.getFilePath());
				Uri newUri =ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, pickedSong.getId()); //Long.valueOf(_id)
				updateRingtong(newUri, mType);
				mSongUri = newUri;
				mSongId = pickedSong.getId();
				mSongDisplayName = pickedSong.getDisplayName();
				 
		  
				
			}
			
		}


	    /**
	     * update ringtong state ,so we can get ringtong from ringtongmanager
	     * @param newUri
	     * @param type
	     */
	    private void updateRingtong(Uri newUri, int type){
			ContentValues values = new ContentValues();
			String typeString = getTypeString(type);
			if(!TextUtils.isEmpty(typeString)){
				values.put(typeString, true);
				getContentResolver().update(newUri, values, null, null);/*insert(newUri, values);*/
			}
		}
		
	    
		private String getTypeString(int type){
		        if ((type & RingtoneManager.TYPE_RINGTONE) != 0) {
		            return MediaStore.Audio.Media.IS_RINGTONE;
		        } else if ((type & RingtoneManager.TYPE_NOTIFICATION) != 0) {
		            return MediaStore.Audio.Media.IS_NOTIFICATION;
		        } else if ((type & RingtoneManager.TYPE_ALARM) != 0) {
		            return MediaStore.Audio.Media.IS_ALARM;
		        } else {
		            return null;
		        }
		}
		
		/**
		 *  文件存放目录
		 */
		public static final String FILE_DIR = "filedir"; 
		/**
		 * 文件名称
		 */
		public static final String FILE_NAME = "filename"; 
		
		public static final String RINGTONE_TYPE = "ringtone_type";	
		public static final String NAME = "name"; 
		
		/**
		 * 唐骏提供的下载铃声选择列表获取方式
		 * @param folderPath
		 * @return
		 */
		private ArrayList<Song> searchThemeSongs( int type){
			Cursor cursor = getContentResolver().query(Uri.parse("content://com.aurora.thememanager.ringtoneprovider"), new String[]{FILE_DIR, FILE_NAME, NAME}, RINGTONE_TYPE + "=?", new String[] { type+"" }, null);
			
			// query the date from databse
			ArrayList<Song> songList = new ArrayList<Song>();
			if(cursor==null || cursor.getCount()==0){
				return songList;
			}
			try {				
				Song song = null;
				if(cursor!=null)
				{
					while (cursor.moveToNext()) {
						song = getSongFromMediaProvider(cursor.getString(cursor.getColumnIndex(FILE_DIR))+File.separator+cursor.getString(cursor.getColumnIndex(FILE_NAME)));
						if(song!=null){
							song.setDisplayName(cursor.getString(cursor.getColumnIndex(NAME)));
							songList.add(song);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(cursor!=null){
					cursor.close();
				}
			}
			Log.i(TAG, "songList.size()="+songList.size());
			return songList;			
		}
		
		private String[] projection = new String[] {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.TITLE
		};
		
		private Song getSongFromMediaProvider(String folderPath){
			File folderPathFile = new File(folderPath);
			Log.i(TAG, "folderPath="+folderPath);
			// query the date from databse
			if(!folderPathFile.exists() || folderPathFile.isDirectory()){
				Log.i(TAG, "getSongFromMediaProvider1");
				return null;
			}
			
			Song song = null;
			Cursor cr= null;
			String soundRecordPathWhere = MediaStore.Audio.Media.DATA + " = '" + folderPath+"'";
			try {
				 cr = getContentResolver().query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, soundRecordPathWhere,
						null, null); 
				Log.i(TAG, "cr.getCount()="+cr.getCount());
				if(cr!=null)
				{
					if (cr.moveToFirst()) {
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
						}else{
							String songTitleName = cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.TITLE));
							if(songTitleName != null){
								song.setDisplayName(songTitleName);
								Log.i("qy", "songTitleName path ==" +cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.DATA)));
								song.setFilePath(cr.getString(
										cr.getColumnIndex(MediaStore.Audio.Media.DATA))
										.toLowerCase());
								song.setOrderName(AuroraPinYinUtils.getSpell(songTitleName));
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
			Log.i(TAG, "song==null:"+(song==null));
			return song;
		}
		
		private ArrayList<Song> searchSongs(String folderPath){
			File folderPathFile = new File(folderPath);
			// query the date from databse
			ArrayList<Song> songList = new ArrayList<Song>();
			if(!folderPathFile.exists() || !folderPathFile.isDirectory()){
				return songList;
			}
			String[] projection = new String[] {
					MediaStore.Audio.Media._ID,
					MediaStore.Audio.Media.DISPLAY_NAME,
					MediaStore.Audio.Media.DATA,
					MediaStore.Audio.Media.TITLE
			};
			
			Cursor cr= null;
			String soundRecordPathWhere = MediaStore.Audio.Media.DATA + " LIKE '" + folderPath + "%'";
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
		
		/** 
	     * 按钮-监听电话 
	     * @param v 
	     */  
	    public void createPhoneListener() {  
	        TelephonyManager telephony = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);  
	        telephony.listen(new OnePhoneStateListener(),  PhoneStateListener.LISTEN_CALL_STATE);  
	        Log.i("luolaigang", "注册事件");
	    }  
	      
	    /** 
	     * 电话状态监听. 
	     * @author stephen 
	     * 
	     */  
	    class OnePhoneStateListener extends PhoneStateListener{  
	        @Override  
	        public void onCallStateChanged(int state, String incomingNumber) {  
	        	Log.i("luolaigang", "state="+state);
	        	if (mPlayer.isPlaying()) {
					mPlayer.stop();
				}
	        	super.onCallStateChanged(state, incomingNumber);  
	        }  
	    }  
}
