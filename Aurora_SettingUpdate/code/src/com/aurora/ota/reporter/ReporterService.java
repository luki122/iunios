
package com.aurora.ota.reporter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

//import com.amap.api.location.AMapLocation;
//import com.amap.api.location.AMapLocationListener;
//import com.amap.api.location.LocationManagerProxy;
//import com.amap.api.location.LocationProviderProxy;
import com.aurora.ota.database.DataBaseCreator;
import com.aurora.ota.database.DataBaseHandler;
import com.aurora.ota.database.ReporterKey;
import com.aurora.ota.database.RepoterManager;
import com.aurora.ota.database.RepoterManager.ReportType;
import com.aurora.ota.database.Repoters;
import com.aurora.ota.database.DataBaseCreator.RepoterColumns;
import com.aurora.ota.location.LocationHandler;
//import com.aurora.ota.location.LocationHandler.Callback;
import com.aurora.ota.location.LocationInfo;
import com.aurora.ota.reporter.ReporterThread.CallBack;
import com.aurora.ota.reporter.ReporterThread.Error;

import gn.com.android.update.UpgradeApp;
import gn.com.android.update.business.OtaReceiver;
import gn.com.android.update.settings.SharePreferenceOperator;
import gn.com.android.update.utils.HttpUtils;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.Util;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import gn.com.android.update.R;

public class ReporterService extends Service implements CallBack, ReporterKey/*,Runnable,AMapLocationListener*/{

    public static final String SERVIER_NAME = "com.aurora.ota.reporter.ReporterService";
    public static final String BOOT_REPORTER_ACTION = "com.aurora.ota.RPService";
    public static final String ACTION_REPORT_ITEM = "com.aurora.ota.REPORT_ITEM";
    public static final String ACTION_REPORT_ITEMS = "com.aurora.ota.REPORT_ITEMS";

    public static final String KEY_TODAY_IS_REPORTED = "today_is_reported";

    private static final long reportCycle = 24 * 60 * 60 * 1000;
    private static final String TAG = "ReporterService";
    private static final int REPORTED = 1;
    private static final int NOT_REPORTED = 0;
    private static final int MSG_START_REPORT = 0x100;
    private static final int MSG_DELETE = 0x101;
    private static final int MSG_UPDATE = 0x102;
    
    
    private static final int MSG_GET_LOCATION = 0x103;
    
    private static final int MSG_CREATE_ITEM = 0x104;
    
    private static  ThreadPoolExecutor executor;
    
    private Error mError;
    private Message mHandlerMSG;

    private long startuptime = 0;
    private long shutdowntime = 0;

    private RepoterReceiverInner mReceiverInner = new RepoterReceiverInner();
    private TimeReceiver mTimeReceiver = new TimeReceiver();
    private RepoterManager mManager;
    private DataBaseHandler mDBHandler;
    private NetFocade mFocade;

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;
    private boolean mRedelivery;
    private boolean mTodayIsReported = false;
    private ReporterItem mTempItem;

    private ReportType mReportType;
    
    
   // private LocationManagerProxy aMapLocManager = null;
    private TextView myLocation;
    //private AMapLocation aMapLocation;// 用于判断定位超时
    
    private LocationInfo mLocationInfo;
    
    private LocationHandler mLocationHandler;
    

    private final List<Integer> reporters = new ArrayList<Integer>();

    private List<ReporterItem> items = new ArrayList<ReporterItem>();
    public static final int MAX_CONCURRENT = 10;

    private Object obj = new Object();
    
   // private LocationManagerProxy mLocationManager;
    
    private Context mContext;
    
    
    
    private final ThreadPoolExecutor mExecutor = buildExecutor();
    private Handler mLocationH = new Handler(){
        public void handleMessage(Message msg) {
//            aMapLocManager = LocationManagerProxy.getInstance(ReporterService.this);
//            aMapLocManager.setGpsEnable(false);
//            mLocationHandler = new LocationHandler(ReporterService.this, aMapLocManager);
//            mLocationHandler.setCallback(new com.aurora.ota.location.LocationHandler.Callback() {
//                
//                @Override
//                public void callback(LocationInfo info) {
//                    // TODO Auto-generated method stub
//                    LogUtils.logd(TAG, "LocationHandler   callback   LocationInfo =   "+info.toString());
//                    saveLocation(info);
//                    mLocationHandler.stopLocation();
//                }
//
//                @Override
//                public void ipCallback(String ip) {
//                    // TODO Auto-generated method stub
//                    saveIp(ip);
//                    mLocationHandler.stopLocation();
//                }
//
//                @Override
//                public void countryCallback(String country) {
//                    // TODO Auto-generated method stub
//                    
//                }
//            });
//            mLocationHandler.start();
        };
        
    };

   /* private static ThreadPoolExecutor buildExecutor() {

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                MAX_CONCURRENT, MAX_CONCURRENT, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }*/

    private final ThreadPoolExecutor mDBExecutor = buildExecutor();
    
    private final ThreadPoolExecutor mSendDBExecutor = buildExecutor();

    private static ThreadPoolExecutor buildExecutor() {

    	if(executor == null){
         executor = new ThreadPoolExecutor(
                MAX_CONCURRENT, MAX_CONCURRENT, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        executor.allowCoreThreadTimeOut(true);
    	}
        return executor;
    }

    private final class ServiceHandler extends Handler {
    	 public ServiceHandler() {    
             super();    
         }    
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            //onHandle((Integer) msg.obj);
        	//LogUtils.log(TAG, " start report at 12:30  currentThread  =  " + Thread.currentThread().getName());
        	mReportDBHandler.sendEmptyMessage(0);
        	buildItem();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
    	  mContext = getApplicationContext();
        mManager = RepoterManager.getInstance(this);
        mDBHandler = new DataBaseHandler(this);
        if (mFocade == null) {
            mFocade = new RealNetFocade(this);
        }
        HandlerThread thread = new HandlerThread("ReportService");
        thread.start();
        LogUtils.log(TAG, " onCreate Service ");
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        super.onCreate();

    }
    
   /* private void stopLocation() {
        
        mLocationHandler.stopLocation();
    }*/

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REPORT_ITEM);
        filter.addAction(Constants.ACTION_FROM_NET_CHANGE);
        filter.addAction(Constants.ACTION_UPDATE_LOCATION);
        registerReceiver(mReceiverInner, filter);

        IntentFilter timeFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, timeFilter);
        LogUtils.log(TAG, " onStart Service ");
        mHandlerMSG = mServiceHandler.obtainMessage();
        
        IntentFilter usageTimeFilter = new IntentFilter();
        usageTimeFilter.addAction(Intent.ACTION_SCREEN_ON);
        usageTimeFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(usageTime, usageTimeFilter);

      if (intent == null) {
            return;
        }

        mReportType = mManager.getReportType();
        reportWhenBoot(mReportType);
        final int netOption = intent.getIntExtra(Constants.KEY_NET_CHANGED, 0);
        if (netOption == Constants.VALUE_NET_CHANGED) {

            if (HttpUtils.isWIFIConnection(this)) {
                if(Constants.NEED_LOCATION){
                    mLocationH.sendEmptyMessage(MSG_GET_LOCATION);
                }
                reportItemsByThread();
            }
        }
        
//        aMapLocManager = LocationManagerProxy.getInstance(this);
//        aMapLocManager.setGpsEnable(false);
//        mLocationHandler = new LocationHandler(this, aMapLocManager);
//        mLocationHandler.setCallback(new Callback() {
//            
//            @Override
//            public void callback(LocationInfo info) {
//                // TODO Auto-generated method stub
//                Log.e("loo", "info:"+info.toString());
//                saveLocation(info);
//            }
//
//            @Override
//            public void ipCallback(String ip) {
//                // TODO Auto-generated method stub
//                saveIp(ip);
//            }
//        });
        if(Constants.NEED_LOCATION){
            if(HttpUtils.isWIFIConnection(this)){
                mLocationH.sendEmptyMessage(MSG_GET_LOCATION);
            }
           
        }
       
        
    }
    
    private String getDefaultCountry(){
        return getResources().getString(R.string.default_location_country);
    }
    
    /**
     * Saved the location just get from internet to Sharepreference
     * @param location
     */
    private void saveLocation(LocationInfo location){
        LocationInfo info =new LocationInfo();
        String city = location.getCity();
        String province = location.getProvince();
        String country = getDefaultCountry();
//        Log.e("json", "country:"+country);
        if(!TextUtils.isEmpty(province) && !TextUtils.isEmpty(city)){
            info.setCountry(country);
            info.setProvince(location.getProvince());
            info.setCity(location.getCity());
            mManager.savePhoneSize(getPhoneSize()[0], getPhoneSize()[1]);
            mManager.saveLocation(info);
        }
    }
    
    private void saveIp(String ip){
        mManager.saveIp(ip);
//        Log.e("json", "ip:"+ip);
    }

    private int[] getPhoneSize(){
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
//        Log.e("json", "screenWidth:"+screenWidth);
//        Log.e("json", "screenHeight:"+screenHeight);
        int[] size = new int[2];
        size[0] = screenWidth;
        size[1] = screenHeight;
        return size;
    }
    
    private void reportWhenBoot(final ReportType type) {
        switch (type) {
            case Update:
            case Delay:
               // reportItem();
            //	LogUtils.log(TAG, " reportWhenBoot    currentThread  =  " + Thread.currentThread().getName());
            	mHandlerMSG.sendToTarget();
                break;
            default:
                break;
        }

    }

    private ReporterItem createItem() {
        return ReporterItem.createItem(this);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        this.unregisterReceiver(mReceiverInner);
        this.unregisterReceiver(mTimeReceiver);
        LogUtils.logd(TAG, "Report Service onDestroy()");
        mServiceLooper.quit();
        super.onDestroy();
    }

    private boolean startReport() {
        return reportItems();
    }

    private void reportItem() {
       mCreateItemHandler.sendEmptyMessage(MSG_CREATE_ITEM);
    }

    /**
     * report items
     */
    private boolean reportItems() {
            items.clear();
            items.addAll(mDBHandler.queryList());
            if (items.size() > 0) {
                for (ReporterItem i : items) {
                    readyToReport(mExecutor, i);
                }
            }
        return false;

    }

    private void readyToReport(ThreadPoolExecutor executor, ReporterItem item) {
        synchronized (obj) {
            ReporterThread thread = new ReporterThread(item, mFocade,mManager.getHistoryLocation());
            thread.registerCallBack(this);

            executor.submit(thread);
        }
    }

    private void reportItemNow(Uri uri) {
    	LogUtils.log(TAG, "reportItemNow  Uri  = " + uri);
        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = mManager.query(resolver, uri);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ReporterItem item = getCursorItem(cursor);

                if (item != null) {
                    readyToReport(mExecutor, item);
                }

            }
        }
        cursor.close();
        // mHandlerMSG.obj = MSG_START_REPORT;
        // mServiceHandler.sendMessage(mHandlerMSG);
    }

    private ReporterItem getCursorItem(Cursor c) {
        String city = "";
        String province ="";
        String width = "1080";
        String height = "1920";
        ReporterItem item = new ReporterItem();
        item.setApkVersion(getCursorString(c, KEY_APP_VERSION));
        item.setAppName(getCursorString(c, KEY_APP_NAME));
        item.setImei(getCursorString(c, KEY_IMEI));
        item.setmChanel(getCursorString(c, KEY_CHANEL));
        item.setMobileModel(getCursorString(c, KEY_MOBILE_MODEL));
        item.setMobileNumber(getCursorString(c, KEY_MOBILE_NUMBER));
        item.setRegisterUserId(getCursorString(c, KEY_REGISTER_USER_ID));
        item.setShutdownTime(getCursorString(c, KEY_SHUT_DOWN_TIME));
        item.setStartupTime(getCursorString(c, KEY_BOOT_TIME));
        item.setStatus(getCursorInt(c, KEY_STATUS));
        item.setReported(getCursorInt(c, KEY_REPORTED));
        item.setAppNum(getCursorInt(c, KEY_APP_NUM));
        item.setCreatItemTime(getCursorString(c, KEY_CREATE_ITEM_TIME));
        LogUtils.log(TAG, "duration columns  : c.getColumnIndex(columnName) "  + c.getColumnIndex(KEY_DURATION_TIME));
        LogUtils.log(TAG, "duration columns  : getCursorString(c, KEY_DURATION_TIME) "  + getCursorString(c, KEY_DURATION_TIME));
        item.setDuration(getCursorString(c, KEY_DURATION_TIME));
        item.setId(getCursorInt(c, RepoterColumns.ID));
//        String location = getCursorString(c,RepoterColumns.KEY_LOCATION);
        String phoneSize = getCursorString(c,RepoterColumns.KEY_PHONE_SIZE);
        
//        if(!TextUtils.isEmpty(location)){
//            String[] loc = location.split(Constants.SPLITE);
//            if(loc.length == 2){
//                province = loc[0];
//                city = loc[1];
//            }
//        }
        if(!TextUtils.isEmpty(phoneSize)){
            String[] size = phoneSize.split(Constants.SPLITE);
            if(size.length == 2){
                height = size[0];
               width = size[1];
            }
        }
        item.setPhoneHeight(height);
        item.setPhoneWidth(width);
//        item.setProvince(province);
//        item.setCity(city);
//        item.setLocation(getCursorString(c,RepoterColumns.KEY_LOCATION));
        return item;
    }

    private String getCursorString(Cursor c, String columnName) {
        return c.getString(c.getColumnIndex(columnName));
    }

    private int getCursorInt(Cursor c, String columnName) {
        return c.getInt(c.getColumnIndex(columnName));
    }

    @Override
    public void error(ReporterItem item, Error error) {
        readyToUpdateDatabase(item, error);
    }

    @Override
    public void success(ReporterItem item) {
        readyToUpdateDatabase(item, null);

    }

    @Override
    public void interupt(ReporterItem item, Error error) {
        readyToUpdateDatabase(item, error);
    }

    private void onHandle(Integer msg) {
        LogUtils.logd(TAG, "ServiceHandler  onHandle");
        if (msg == MSG_START_REPORT) {
            LogUtils.logd(TAG, "readyToReport");
            startReport();
        }

    }

    private void readyToUpdateDatabase(ReporterItem item, Error error) {
        synchronized (obj) {
            DatabaseThread thread = new DatabaseThread(item, error, mDBHandler);

            mDBExecutor.submit(thread);

        }

    }
    
    private class DBCallBack implements com.aurora.ota.reporter.SendDBThread.CallBack{

		@Override
		public void error(Error error) {
			 Calendar cal = Calendar.getInstance();
             int hour = cal.get(Calendar.HOUR_OF_DAY);
             int minute = cal.get(Calendar.MINUTE);
			if(hour == 13 && minute >= 0){
				initModuleDB();
			}else{
				mReportDBHandler.sendEmptyMessageDelayed(0, 60*1000);  //延迟一分钟，继续发送
			}
			
		}

		@Override
		public void success() {
			initModuleDB();
		}

		@Override
		public void interupt(Error error) {
			
		}

	
    	
    }
    /**
     * 
     * 方法描述：初始化统计数据库表
     * 创建时间：2015-3-5 下午5:35:57
     * @author jiyouguang
     */
    private void initModuleDB(){
    	SQLiteDatabase DB = mContext.openOrCreateDatabase(DataBaseCreator.DB_NAME,Context.MODE_PRIVATE, null);
		try{
			DB.execSQL("update "+DataBaseCreator.Tables.DB_MODULE_TABLE+" set "+DataBaseCreator.RepoterColumns.KEY_VALUE+"=0");
			/*DB.execSQL("DELETE FROM "+DataBaseCreator.Tables.DB_MODULE_TABLE);
			DB.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = '"+DataBaseCreator.Tables.DB_MODULE_TABLE+"'");//自增列归零
*/				
			LogUtils.log(TAG, "clean module DB ! ! !");
		}catch(Exception e){
			LogUtils.log(TAG, "init module DB have exception ! ! !");
		}
    }

    class DatabaseThread extends Thread {
        private ReporterItem item;
        private Error error;
        private DataBaseHandler dbHandler;

        public DatabaseThread(ReporterItem item, Error error, DataBaseHandler dbHandler) {
            this.error = error;
            this.item = item;
            this.dbHandler = dbHandler;
        }

        @Override
        public void run() {
            if (error != null) {
                updateDatabase();
            } else {
                deleteDatabase();
            }
        }

        private void updateDatabase() {
            if(error == Error.DO_NOT_HAS_ADDRESS){
                String province = "";
                String city = "";
                LocationInfo location = mManager.getHistoryLocation();
                if(location != null){
                    province = location.getProvince()+"";
                    city = location.getCity()+"";
                }
                if(!TextUtils.isEmpty(city) && !TextUtils.isEmpty(province)){
                    item.setLocation(province+Constants.SPLITE+city);
                }
            }
            item.setReported(0);
            dbHandler.update(item);
//            if (!reporters.contains(item.getId())) {
//                reporters.add(item.getId());
//            }
        }

        private void deleteDatabase() {
            item.setReported(1);
            if (dbHandler.update(item) != 0) {
                 dbHandler.delete(item.getId());
            }
//            if (reporters.contains(item.getId())) {
//                reporters.remove(item.getId());
//            }
        }

    }

    /**
     * report item
     * 
     * @author iuni
     */
    class RepoterReceiverInner extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent != null) {
                String action = intent.getAction();
                if (action.equals(ACTION_REPORT_ITEM)) {
                    Bundle b = intent.getBundleExtra(Repoters.Columns.KEY_ITEM);
                    if (b != null) {
                        Uri uri = b.getParcelable(Repoters.Columns.KEY_ITEM);
                        if (uri == null) {
                            return;
                        }
                        mCurrentUri = uri;
                        reportItemByThread();

                    }
                } else if (action.equals(Constants.ACTION_FROM_NET_CHANGE)) {
                    LogUtils.log(TAG, "RepoterReceiverInner  onReceive netchanged");
                    if (HttpUtils.isWIFIConnection(context)) {
                    	 LogUtils.log(TAG, "Now is Wifi connect ");
                        if(Constants.NEED_LOCATION){
                            mLocationH.sendEmptyMessage(MSG_GET_LOCATION);
                        }
                        reportItemsByThread();
                    }

                }
            }

        }

    }

    class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent == null) {
                return;
            }
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
            LogUtils.log(TAG, "hour: "+hour+"  minute : "+minute);
            if ((hour == 12) && (minute == 30)) {
//                    Log.e("luofu", "**************** report");
              //  reportItem();
                	LogUtils.log(TAG, " onReceive  ACTION_TIME_TICK  ");
                	 mHandlerMSG = mServiceHandler.obtainMessage();
                	 mHandlerMSG.sendToTarget();
              }
            }
        }

    }
	private static boolean flag = false;
	private static long startTime = 0;
	private static long endTime = 0;
    private BroadcastReceiver usageTime = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(Intent.ACTION_SCREEN_ON.equals( intent.getAction())){
				startTime = System.currentTimeMillis();
				flag = true;
			} else if(Intent.ACTION_SCREEN_OFF.equals( intent.getAction())){
				  if(flag){
					    flag = false;
					  	endTime = System.currentTimeMillis();
					  	mManager.storeUsageTime((endTime - startTime) + mManager.getUsageTime());
					  						  	
				  }
				  	endTime = 0;
				  	startTime = 0;
			}
						
		}};
    
    private void reportItemByThread(){
        if(mReportItemHandler.getLooper() == Looper.myLooper()){
            reportItemNow(mCurrentUri);
        }else{
            mReportItemHandler.post(mReportItemAction);
        }
    }
    
    private void reportItemsByThread(){
        if(mReportItemsHandler.getLooper() == Looper.myLooper()){
            reportItems();
        }else{
            mReportItemsHandler.post(mReportItemsAction);
        }
    }
    
    
    private Uri mCurrentUri;
    
    private Runnable mReportItemAction = new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            reportItemNow(mCurrentUri);
        }
    };
    
    private Runnable mReportItemsAction = new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            reportItems();
        }
    };
    
    private Handler mReportDBHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			  synchronized (obj) {
		            SendDBThread thread = new SendDBThread(mContext,mFocade);
		            thread.registerCallBack(new DBCallBack());

		            mSendDBExecutor.submit(thread);
		        }
			
		}
    	
    };
    private Handler mReportItemsHandler = new Handler();
    private Handler mReportItemHandler = new Handler();
    private Handler mCreateItemHandler = new Handler(){
        public void handleMessage(Message msg) {
            Calendar cal = Calendar.getInstance();
            long time = System.currentTimeMillis();
            cal.setTimeInMillis(time);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH)+1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            
            int date = year+month+day;
            ReporterItem item = createItem();
            ContentResolver r = getContentResolver();
            mManager.insert(r, item);
            mManager.storeTodayReported(true);
            mManager.storeReportDate(date);
        };
    };
    
   /* class CreateItemThread extends Thread {
        public Handler mHandler;
        @Override
        public void run() {
            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // process incoming messages here
                }
            };

            Looper.loop();
        }
    }*/
    public void buildItem(){
    	 Calendar cal = Calendar.getInstance();
         long time = System.currentTimeMillis();
         cal.setTimeInMillis(time);
         int year = cal.get(Calendar.YEAR);
         int month = cal.get(Calendar.MONTH)+1;
         int day = cal.get(Calendar.DAY_OF_MONTH);
         
         int date = year+month+day;
         ReporterItem item = createItem();
         OtaReceiver.shutdownTime = 0;//把关机的时间置为初始值
         OtaReceiver.startupTime = mManager.getTime(Constants.KEY_START_UP_TIME); //把开机时间置为本次开机的时间
        mManager.storeUsageTime(0);//把使用时间归零
         ContentResolver r = getContentResolver();
         mManager.insert(r, item);
         mManager.storeTodayReported(true);
         mManager.storeReportDate(date);
    }
 }
    

