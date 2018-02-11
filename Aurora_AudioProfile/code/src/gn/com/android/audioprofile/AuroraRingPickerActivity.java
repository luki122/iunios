package gn.com.android.audioprofile;

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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.Toast;
import gn.com.android.audioprofile.R;
import gn.com.android.audioprofile.entity.Song;
import gn.com.android.audioprofile.utils.AuroraPinYinUtils;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.Calendar;

import gn.com.android.audioprofile.utils.Blur;
import android.os.storage.StorageEventListener;


public class AuroraRingPickerActivity extends AuroraActivity implements Runnable,AdapterView.OnItemClickListener{
	

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
	
	private static final int STARTHOUR = 6;
    private static final String LOCKSCREENDEFAULTPATH = "/system/iuni/aurora/change/lockscreen/Timely/";
    public static boolean mIsFullScreen =false;
    private boolean mIsChronometer = false;
    private boolean mIsNativeStart = false;  //是否是从设置中启动的
    
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

        if (mClickedPos == mDefaultRingtonePos) {
            // Set it to the default Uri that they originally gave us
            uri = mUriForDefaultItem;
        } else if (mClickedPos == mSilentPos) {
            // A null Uri is for the 'Silent' item
            uri = Uri.parse(VALUE_RINGTONE_SILENT);
            
        } else if (mClickedPos == OTHER_SONG_LOC){
        	uri= mSongUri;
        } else {
            uri = mRingtoneManager.getRingtoneUri(getRingtoneManagerPosition(mClickedPos));
        }
        
        // save the uri
        
        if(mType == RingtoneManager.TYPE_NOTIFICATION || mType == RingtoneManager.TYPE_RINGTONE &&  !mTitle.equals(typeContactRing)|| 
        		mType == RingtoneManager.TYPE_ALARM && !mIsChronometer){
        	if(isSmsList){
        		Settings.System.putString(getContentResolver(), "sms_sound", uri.toString());
        	} else {

        		RingtoneManager.setActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType, uri); 
        	}
        }
        if(null != uri){
        Log.i(TAG, "save uri == " +uri.toString());
        }
        resultIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, uri);
        setResult(RESULT_OK, resultIntent);
        
	}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Log.i(TAG, "item clicked position ==" +position);
			Log.i(TAG, "OTHER_SONG_LOC ==" +OTHER_SONG_LOC);
			if( position == OTHER_SONG_LOC){
				
				stopAnyPlayingRingtone();
				Intent songRingIntent = new Intent(this,AuroraSongRingPickerActivity.class);
				songRingIntent.putExtra(SONG_ID,mSongId);
				startActivityForResult(songRingIntent, SONG_RING_ACTIVITY_REQUEST_CODE);
			}else{
				mClickedPos = position;
				mItemRadioBtn.setChecked(false);

	            // Play clip
	            playRingtone(position, 0);
			}
			
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
	    		opts.inPreferredConfig = Bitmap.Config.RGB_565;
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
		Log.i(TAG, " onCreate " );
		super.onCreate(savedInstanceState);
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
		Log.i(TAG, " path = " + path );
		if(mIsFullScreen && path !=null){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setTheme(com.aurora.R.style.Theme_aurora);			
			mBitmap = getLockScreenFromDefaultPath(this,path);
			if(mBitmap != null){
				mBitmap = Blur.fastblur(this, mBitmap, 23);
			}
			

		}else{
			setTheme(com.aurora.R.style.Theme_aurora_Light);
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
        
        
        mHasDefaultItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        mUriForDefaultItem = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
        if (mUriForDefaultItem == null) {
            mUriForDefaultItem = Settings.System.DEFAULT_RINGTONE_URI;
        }

        
        // Get whether to show the 'Silent' item
        mHasSilentItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);  		
		mRingtoneManager = new RingtoneManager(this);
		
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
		Log.i(TAG, "the ringToneType == " +mType);
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
			
		// search the default ringtone uri and the default notification uri		
		if(mType == RingtoneManager.TYPE_NOTIFICATION || mType == AudioProfileActivity.TYPE_RINGTONE_SMS){
//t			mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mDefaultNotificationUri));
			while (mCursor.moveToNext()) {
				if(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("光点（默认）")
					||mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("光点")	){
					int notificationId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
					mDefaultNotificationUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, notificationId);
					Log.i(TAG, " **mDefaultNotificationUri = "+mDefaultNotificationUri.toString() );
					break;
				}
				
			}
			
		}else if(mType == RingtoneManager.TYPE_RINGTONE){
			
			while (mCursor.moveToNext()) {
				if(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("IUNI音调（默认）")
					||mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("IUNI 音调")){
					int ringtonId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
					mDefaultRingtoneUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, ringtonId);
					Log.i("qy", " **mDefaultRingtoneUri = "+mDefaultRingtoneUri.toString() );
					break;
				}
				
			}
		} else if(mType == RingtoneManager.TYPE_ALARM){
			
			while (mCursor.moveToNext()) {
				if(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("清晨（默认）")
					||mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim().equals("清晨")){
					int ringtonId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
					mDefaultAlarmUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, ringtonId);
					Log.i("qy", " **mDefaultAlarmUri = "+mDefaultAlarmUri.toString() );
					break;
				}
				
			}
		} 
		
		
		// The volume keys will control the stream that we are choosing a ringtone for
		Log.i(TAG, " **mRingtoneManager.inferStreamType() = "+mRingtoneManager.inferStreamType());
        setVolumeControlStream(mRingtoneManager.inferStreamType());

        // Get the URI whose list item should have a checkmark
//        mExistingUri = intent
//                .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
//        
//        mExistingUri = RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType);
   
        if(mTitle.equals(typeContactRing) || mIsChronometer){
        	Log.i(TAG, " OnCreat  mExistingUri  11111111 =   "  +  RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
        	mExistingUri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
        	
        }else{
        	Log.i(TAG, " OnCreat  mExistingUri  2222222 =    "+ isSmsList);
        	if(isSmsList){
        		String smsUri =  Settings.System.getString(cr, "sms_sound");
        		if(null != smsUri){
        			mExistingUri = Uri.parse(smsUri);
        		}else{
        			//fixed bug 10418
        			mExistingUri = RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType);
        			if(!mDefaultNotificationUri.equals(mExistingUri)){
        				mExistingUri = mDefaultNotificationUri;
        			}
        		}
        		
        	}else{
        			mExistingUri = RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType);
        	}
        	Log.i(TAG, " OnCreat  test test  =    "+ Settings.System.getString(cr, "445"));
        	
        }
        Log.i(TAG, " **mExistingUri =  "+mExistingUri);
        // chronometer        
        /*if( mExistingUri == null && mIsChronometer){
        	while (mCursor.moveToNext()) {
				if(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).equals("Ringing Alarm2")){
					int ringtonId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
					mExistingUri =  ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, ringtonId);
					Log.i("qy", "chronometer Uri ==" +mExistingUri.toString());
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
        
        mRingtoneList.setAdapter(ringCursorAdapter);  
      
        mRingtoneList.setOnItemClickListener(this);  
//        mylist.setOnItemSelectedListener(this);  
        mRingtoneList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);//.CHOICE_MODE_SINGLE);  
 
        if (savedInstanceState != null) {
            mClickedPos = savedInstanceState.getInt(SAVE_CLICKED_POS, -1);
            Log.i(TAG,"OnCreat  savedInstanceState != null  mClickedPos =  "  + mClickedPos);
            
        }
//        	mRingtoneList.setItemChecked(mClickedPos, true);
        // register storage event listener
        mStorageManager = StorageManager.from(this);
		mStorageManager.registerListener(mStorageListener);
		
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

	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		Log.i(TAG, "onResume  mClickedPos  =  "   +  mClickedPos);
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
        TextView textView = (TextView) getLayoutInflater().inflate(
        		R.layout.aurora_ring_listitem, listView, false);
        textView.setText(textResId);        
        listView.addHeaderView(textView);
        mStaticItemCount++;
        return listView.getHeaderViewsCount() - 1;
    }
    
    private int addOtherRingtoneItem(ListView listView) {
        
      //adapt to the alarm clock
    	View view =null;
    	
        if(mIsFullScreen){
        	Log.i("qy", "addOtherRingtoneItem  mIsFullScreen !=null");
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

    
    public void prepareListView(ListView listView) {

    	Log.i(TAG, "prepareListView mHasDefaultItem  =  " + mHasDefaultItem);
        if (mHasDefaultItem) {
            mDefaultRingtonePos = addDefaultRingtoneItem(listView);

            if (RingtoneManager.isDefault(mExistingUri)) {
                mClickedPos = mDefaultRingtonePos;
            }
        }
        //test  mHasSilentItem***************************
        Log.i(TAG, "prepareListView mHasSilentItem  =  " + mHasSilentItem);
        if (mHasSilentItem) {
            mSilentPos = addSilentItem(listView);

            // The 'Silent' item should use a null Uri
            if (mExistingUri == null) {
                mClickedPos = mSilentPos;
            }
        }

    	addOtherRingtoneItem(listView);

    	// deal with the loc of song item
    	if (!mHasSilentItem && !mHasDefaultItem){
    		OTHER_SONG_LOC =0;
    	}else if (mHasSilentItem && !mHasDefaultItem ||!mHasSilentItem && mHasDefaultItem ){
    		OTHER_SONG_LOC =1;
    	}else if(mHasSilentItem && mHasDefaultItem){
    		OTHER_SONG_LOC =2;
    	}
        		
        
        
    	 Log.i(TAG, "prepareListView mClickedPos  =  " + mClickedPos);
        if (mClickedPos == -1) {
            mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mExistingUri));           
            
            Log.i(TAG, "prepareListView mClickedPos 2222222 =  " + mClickedPos);
            // qy compatability e6 .
            if(mClickedPos == -1){
            	mSongUri = mExistingUri;
            	mClickedPos = OTHER_SONG_LOC;
            	
            	String title = null /*RingtoneManager.getRingtone(this, mExistingUri).getTitle(this)*/;
            	int id = -1;

            	try{
            		id = (int)ContentUris.parseId(mExistingUri);
            	}catch(Exception e){
            		Log.i("qy", e.toString());
            	}
            	Log.i(TAG, "id ==  "+id);
            	
            	Cursor cr = getContentResolver().query(
        				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
        						MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME}, MediaStore.Audio.Media._ID + "=?",
        				new String[] {id+""}, null);
            	Log.i(TAG, "cr.getCount() == "+cr.getCount());
            	
            	//add by jiyouguang  fixed bug 11352
            	//如果短信的铃声被删除了，此时短信铃声则显示通知铃声的位置 
            	if(isSmsList && null != cr &&cr.getCount() == 0){
            		
            		try{
                		id = (int)ContentUris.parseId(RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType));
                	}catch(Exception e){
                		Log.i("qy", e.toString());
                	}
            		Log.i(TAG, "getActualDefaultRingtoneUri  ==  "+ RingtoneManager.getActualDefaultRingtoneUri(AuroraRingPickerActivity.this, mType));
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
            		Log.i("qy", "cr.getCount() == "+cr.getCount());
            		title = cr.getString(1);   
            		int indexTemp = title.lastIndexOf('.');
    				if(indexTemp > 0){
    					title = title.substring(0, indexTemp);					
    				}            		

            		Log.i("qy", "ringtone title == "+title);
                	mSongDisplayName = title;              	
                	mSongId =id ;/*cr.getInt(cr.getColumnIndex(MediaStore.Audio.Media._ID))*/ // 2014 07 01
                	updateOtherRingtoneItem(title,true);
            	}else{
            		
            		updateOtherRingtoneItem(getString(R.string.none_select),false);
            		if(mType == RingtoneManager.TYPE_NOTIFICATION){
            			mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mDefaultNotificationUri));
            			
            		}else if(mType == RingtoneManager.TYPE_RINGTONE){
            			mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mDefaultRingtoneUri));
            			
            		}else if(mType == RingtoneManager.TYPE_ALARM){
            			mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mDefaultAlarmUri));
            			
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
		}

		@Override
	    protected void onPause() {
	        super.onPause();
	        sendResult();
	        stopAnyPlayingRingtone();
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

	        // If the manager position is -1 (for not found), return that
	        if (ringtoneManagerPos < 0) return ringtoneManagerPos;

	        return ringtoneManagerPos + mStaticItemCount;
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
				Log.i("qy", "onResult  *** RingPicker SongName =" +pickedSong.getDisplayName());
				mClickedPos = OTHER_SONG_LOC;
				updateOtherRingtoneItem(pickedSong.getDisplayName(),true);
				
				
//				Uri newUri = MediaStore.Audio.Media.getContentUriForPath(pickedSong.getFilePath());
				Uri newUri =ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, pickedSong.getId()); //Long.valueOf(_id)
				mSongUri = newUri;
				mSongId = pickedSong.getId();
				mSongDisplayName = pickedSong.getDisplayName();
				 
		  
				
			}
			
		}


	    
	    
}
