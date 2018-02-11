package com.android.contacts.appwidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import gionee.provider.GnContactsContract.Contacts;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.calllog.CallLogQueryHandler;

public class AppWidgetUpdateService extends Service{

	private static final String TAG = "liyang-AppWidgetUpdateService";

	private CallLogQueryHandler mCallLogQueryHandler;
	public static String FETCH_APPWIDGET_TOKEN="FETCH_APPWIDGET_TOKEN";


	private CallLogQueryHandler.AppWidgetListener listener;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate:"+this);
		registerObserver();
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy:"+this);
		unregisterObserver();
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void registerObserver(){
		Log.d(TAG, "registerObserver:"+mContactChangeObserver+" this:"+this);
		if(mContactChangeObserver==null){
			Log.d(TAG, "registerObserver1");
			mContactChangeObserver = new ContactChangeObserver();
			this.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mContactChangeObserver);
			//		callLogChangeObserver = new CallLogChangeObserver();
			this.getContentResolver().registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, mContactChangeObserver);
		}
	}

	private void unregisterObserver(){
		Log.d(TAG,"unregisterContactChangeObserver");
		if(mContactChangeObserver!=null){
			try{
				Log.d(TAG,"unregisterContactChangeObserver1");
				this.getContentResolver().unregisterContentObserver(mContactChangeObserver);
				mContactChangeObserver=null;
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	}

	private ContactChangeObserver mContactChangeObserver;
	private Handler mHandler=new Handler(); 
	private class ContactChangeObserver extends ContentObserver {
		public ContactChangeObserver() {
			super(new Handler());
		}


		@Override
		public void onChange(boolean selfChange) {
			Log.i(TAG, "onChange");			
			mHandler.removeCallbacks(mRunnable);
			mHandler.postDelayed(mRunnable, 12000);
			super.onChange(selfChange);
		}
	}

	public Runnable mRunnable=new Runnable(){    
		public void run() {    
			beginUpdate();
		} 
	};

	private void beginUpdate(){		

		Log.d(TAG, "beginUpdate1"); 

		if(listener==null) {
			listener=new MyAppWidgetListener();
		}

		if(mCallLogQueryHandler==null){
			mCallLogQueryHandler = new CallLogQueryHandler(ContactsApplication.getInstance().getApplicationContext()
					.getContentResolver(), listener,0);
		}
		mCallLogQueryHandler.FetchAppWidgetContacts();
	}	

	public class MyAppWidgetListener implements CallLogQueryHandler.AppWidgetListener{

		@Override
		public void onCallsFetched(final Cursor mCursor) {
			Log.d(TAG, "oncalllogfetched");
			sort(mCursor);
			updateAppWidget();

		}

	}




	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(intent==null) {
			Log.d(TAG, "intent null");
			return super.onStartCommand(intent, Service.START_REDELIVER_INTENT, startId); 
		}
		Log.d(TAG, "onStartcommand,this:"+this+" intent:"+intent+" onstart:"+intent.getBooleanExtra("onstart", true));
		if(intent.getBooleanExtra("onstart", true)){
			mHandler.removeCallbacks(mRunnable);
			mHandler.postDelayed(mRunnable, intent.getIntExtra("delay", 0));
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private void sort(Cursor cursor){
		data.clear();

		if(cursor==null) return;

		while(cursor.moveToNext()){
			if(!TextUtils.isEmpty(cursor.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_DISPLAY_NAME))){
				Log.d(TAG, "calls count:"+cursor.getInt(CallLogQuery.GN_CALLS_JOIN_DATA_VIEW_CALLS_COUNT));

				//					long contactId = ContentUris.parseId(Contacts.lookupContact(
				//                            mContext.getContentResolver(), Uri.parse("content://"+MyAppWidgetProvider.MyAppWidgetProvider.cursor.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_LOOKUP_KEY))));

				//					Log.d(TAG,"contactId:"+contactId);

				Cursor cursor1=this.getContentResolver().query(Contacts.CONTENT_URI,
						new String[]{"_id"},Contacts.LOOKUP_KEY+"='"+cursor.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_LOOKUP_KEY)+"'",null,null);
				long id=0;
				if(cursor1 != null) {
					if(cursor1.getCount()>0){
						if(cursor1.moveToFirst()){
							id=cursor1.getLong(cursor1.getColumnIndex("_id"));
							Log.d(TAG,"id:"+id);
						}
					}
					cursor1.close();
				}


				data.add(new MyContact(
						cursor.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_DISPLAY_NAME),
						cursor.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_NUMBER),
						cursor.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_LOOKUP_KEY),
						cursor.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_PHOTO_URI),
						cursor.getInt(CallLogQuery.GN_CALLS_JOIN_DATA_VIEW_CALLS_COUNT),
						cursor.getLong(CallLogQuery.CALLS_JOIN_DATA_VIEW_DATE),
						id));
			}
		}


		cursor.close();

		ComparatorContacts comparator=new ComparatorContacts();
		Collections.sort(AppWidgetUpdateService.data, comparator);
		Log.d(TAG, "datasize:"+AppWidgetUpdateService.data.size()+" AppWidgetUpdateService.data:"+AppWidgetUpdateService.data);
	}

	public static List<MyContact> data=new ArrayList();
	private RemoteViews rv;
	private RemoteViews emptyRemoteViews;
	public void updateAppWidget() { 
		Log.d(TAG,"updateAppWidget()");
		// 设置 “GridView(gridview)” 的adapter。
		// (01) intent: 对应启动 GridWidgetService(RemoteViewsService) 的intent  
		// (02) setRemoteAdapter: 设置 gridview的适配器
		//    通过setRemoteAdapter将gridview和GridWidgetService关联起来，
		//    以达到通过 GridWidgetService 更新 gridview 的目的

		if(rv==null){
			rv = new RemoteViews(this.getPackageName(), R.layout.appwidget_provider);  
		}

		if(emptyRemoteViews==null){
			emptyRemoteViews=new RemoteViews(this.getPackageName(), R.layout.appwidget_provider_emptyview);  
		}

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
				MyAppWidgetProvider.class));
		if(appWidgetIds.length<=0) return;
		// 设置响应 “GridView(gridview)” 的intent模板	        
		// 说明：“集合控件(如GridView、ListView、StackView等)”中包含很多子元素，如GridView包含很多格子。
		//     它们不能像普通的按钮一样通过 setOnClickPendingIntent 设置点击事件，必须先通过两步。
		//        (01) 通过 setPendingIntentTemplate 设置 “intent模板”，这是比不可少的！
		//        (02) 然后在处理该“集合控件”的RemoteViewsFactory类的getViewAt()接口中 通过 setOnClickFillInIntent 设置“集合控件的某一项的数据”
		for (int appWidgetId : appWidgetIds) {
			Log.d(TAG,"appWidgetId:"+appWidgetId);

			if(data==null||data.size()==0){
				Log.d(TAG, "data null");
				appWidgetManager.updateAppWidget(appWidgetId, emptyRemoteViews);
			}else{	
				Log.d(TAG, "data not null");
				Intent serviceIntent = new Intent(this, GridWidgetService.class);   
				serviceIntent.putExtra(appWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetId);
				rv.setRemoteAdapter(R.id.gridview, serviceIntent);

				//				rv.setEmptyView(R.id.gridview,R.id.empty_view_text);

				Intent gridIntent = new Intent();
				gridIntent.setAction(MyAppWidgetProvider.COLLECTION_VIEW_ACTION);
				gridIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, gridIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				// 设置intent模板
				rv.setPendingIntentTemplate(R.id.gridview, pendingIntent);
				// 调用集合管理器对集合进行更新
				appWidgetManager.updateAppWidget(appWidgetId, rv);
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.gridview);
			}
		}
	}


	//比较拨打次数
	//具体的比较类，实现Comparator接口
	public class ComparatorContacts implements Comparator{

		public int compare(Object arg0, Object arg1) {
			MyContact user0=(MyContact)arg0;
			MyContact user1=(MyContact)arg1;

			//首先比较次数，如果次数相同，则比较时间

			if(user0.getCount()<user1.getCount()){
				return 1;
			}else if(user0.getCount()>user1.getCount()){
				return -1;
			}else{
				if(user0.getDate()<user1.getDate()){
					return 1;
				}else if(user0.getDate()>user1.getDate()){
					return -1;
				}else{
					return 0;
				}
			}
		}
	}

}

