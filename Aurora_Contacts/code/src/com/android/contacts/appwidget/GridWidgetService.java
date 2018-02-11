package com.android.contacts.appwidget;

import android.app.PendingIntent;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import gionee.provider.GnContactsContract.Contacts;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.util.IntentFactory;

import gionee.provider.GnCallLog;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class GridWidgetService extends RemoteViewsService{

	private static final String TAG = "liyang-GridWidgetService";
	 
	private Context mContext;
	@Override
	public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
		Log.d(TAG, "GridWidgetService");
		return new GridRemoteViewsFactory(this, intent);
	}




	/*private CallLogChangeObserver callLogChangeObserver;
	private class CallLogChangeObserver extends ContentObserver {
		public CallLogChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.i(TAG, "onChange1");
			MyAppWidgetProvider.beginUpdate();

		}
	}*/



	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d(TAG,"onCreate1");
		
		super.onCreate();
	}




	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG,"onDestroy1");
		super.onDestroy();
	}




	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}




	private class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {


		private int mAppWidgetId;		

		private String IMAGE_ITEM = "imgage_item";
		private String TEXT_ITEM = "text_item";



		/**
		 * 构造GridRemoteViewsFactory
		 */
		public GridRemoteViewsFactory(Context context, Intent intent) {
			mContext=context;
			mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			Log.d(TAG, "mAppWidgetId:"+mAppWidgetId);
		}

		@Override
		public RemoteViews getViewAt(int position) {
			MyContact myContact;

			// 获取 grid_view_item.xml 对应的RemoteViews
			RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.appwidget_grid_view_item);

			// 设置 第position位的“视图”的数据
			Log.d(TAG, "data2:"+AppWidgetUpdateService.data);
			
			if(position>AppWidgetUpdateService.data.size()-1) return null;
			
			myContact=AppWidgetUpdateService.data.get(position);

			if(!TextUtils.isEmpty(myContact.getPhotoUri())){
				rv.setImageViewUri(R.id.itemImage, Uri.parse(myContact.getPhotoUri()));
			}else{
				int index=(int) (myContact.getConatctId()%(ResConstant.randomContactPhotoId.length));

				if(index<ResConstant.randomContactPhotoId.length&&myContact.getConatctId()!=0){
					rv.setImageViewResource(R.id.itemImage, ResConstant.randomContactPhotoId[index]);
				}else{
					rv.setImageViewResource(R.id.itemImage, R.drawable.svg_dial_default_photo1);
				}
			}

			if(!TextUtils.isEmpty(myContact.getName())){
				rv.setTextViewText(R.id.itemText, myContact.getName());
			}else{
				rv.setTextViewText(R.id.itemText, myContact.getNumber());
			}


			// 设置 第position位的“视图”对应的响应事件
			Intent fillInIntent = new Intent();
			fillInIntent.putExtra("contactUri", myContact.getLookupUri());
			fillInIntent.putExtra("contactNumber", myContact.getNumber());
			rv.setOnClickFillInIntent(R.id.itemLayout, fillInIntent);

			return rv;
		}		

		/**
		 * 初始化GridView的数据
		 */
		private void initGridViewData() {
			Log.d(TAG, "initGridViewData");



		}

		@Override
		public void onCreate() {
			Log.d(TAG, "onCreate");
			// 初始化“集合视图”中的数据
			initGridViewData();
		}


		@Override
		public int getCount() {			
			Log.d(TAG, "datasize1:"+(AppWidgetUpdateService.data!=null?AppWidgetUpdateService.data.size():"null")+" data:"+AppWidgetUpdateService.data);
			if(AppWidgetUpdateService.data==null) return 0;
			// 返回“集合视图”中的数据的总数
			if(AppWidgetUpdateService.data.size()>4){
				AppWidgetUpdateService.data=AppWidgetUpdateService.data.subList(0, 4);
			}
			Log.d(TAG, "getCount:"+AppWidgetUpdateService.data.size());
			return AppWidgetUpdateService.data.size();       	

		}

		@Override
		public long getItemId(int position) {
			Log.d(TAG, "getItemId:"+position);
			// 返回当前项在“集合视图”中的位置
			return position;
		}

		@Override
		public RemoteViews getLoadingView() {
			return null;
		}

		@Override
		public int getViewTypeCount() {
			// 只有一类 GridView
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}		

		@Override
		public void onDataSetChanged() {	
			Log.d(TAG,"onDataSetChanged");


		}

		@Override
		public void onDestroy() {
			Log.d(TAG,"onDestroy");
		}
	}
}
