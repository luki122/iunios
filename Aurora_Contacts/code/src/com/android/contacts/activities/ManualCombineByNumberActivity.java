package com.android.contacts.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.DialogInterface.OnCancelListener;
import android.content.Entity.NamedContentValues;
import android.content.EntityIterator;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.Photo;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.widget.AuroraListView;
import android.widget.AbsListView.OnScrollListener;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.ContactSaveService.Listener;
import com.android.contacts.R;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.MergeContacts;
import com.android.contacts.util.MergeContacts.CombineItemData;
import com.android.contacts.util.MergeContacts.LetStopException;
import com.android.contacts.util.MergeContacts.RawContactItem;
import com.android.contacts.util.MergeContacts.SameContactItems;
import com.android.contacts.widget.CombineItemView;
import com.android.contacts.widget.CombineItemView.CombineItemViewClick;
import com.android.contacts.widget.ContactSimpleInfoView;
import com.android.contacts.widget.LinearListAdapter;


// gionee xuhz 20120830 add for CR00678320 start
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import gionee.provider.GnContactsContract.Contacts;

import com.android.contacts.model.ExchangeAccountType;
import com.android.contacts.model.GoogleAccountType;
import com.mediatek.contacts.model.LocalPhoneAccountType;


// gionee xuhz 20120830 add for CR00678320 end
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import aurora.app.AuroraProgressDialog;


public class ManualCombineByNumberActivity extends AuroraActivity implements Listener {    
	protected static final String TAG = "liyang-ManualCombineByNumberActivity";
	private SameContactItems mAllSameNameContacts;
	private List<CombineItemData> mAllSameNumberContacts;
	private CombineItemsAdapter mCombineItemsAdapter;
	private ContentResolver mContentResolver;
	private AuroraListView mListView;
	private TextView noContent ;
	private MergeContacts mMergeContact = new MergeContacts();
	private CombineItemData mContacts4Combine;
	private Uri mTmpContactCombined;
	private boolean mTmpContactConfirmed = false;
	// gionee xuhz 20120830 add for CR00678320 start
	private Uri mPhotoUri;
	// gionee xuhz 20120830 add for CR00678320 end

	//Gionee <xuhz> <2013-08-15> add for CR00857585 begin
	private ContactPhotoManager mContactPhotoManager;
	//Gionee <xuhz> <2013-08-15> add for CR00857585 end
	private AuroraProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ContactsApplication.sendSimContactBroad();
		Log.d(TAG, "onCreate");
		// gionee xuhz add for support gn theme start
		if (ContactsApplication.sIsGnTransparentTheme) {
			setTheme(R.style.GN_PeopleTheme);
		} else if (ContactsApplication.sIsGnDarkTheme) {
			setTheme(R.style.GN_PeopleTheme_dark);
		} else if (ContactsApplication.sIsGnLightTheme) {
			setTheme(R.style.GN_PeopleTheme_light);
		} 
		// gionee xuhz add for support gn theme start
		super.onCreate(savedInstanceState);
		//Gionee <xuhz> <2013-08-15> add for CR00857585 begin
		if (mContactPhotoManager == null) {
			mContactPhotoManager = ContactPhotoManager.getInstance(this);
		}
		//Gionee <xuhz> <2013-08-15> add for CR00857585 end

		mContentResolver = getContentResolver();
		// aurora <wangth> <2013-9-27> modify for aurora ui begin
		/*
		initView();
		 */
		setAuroraContentView(R.layout.manual_combine,
				AuroraActionBar.Type.Normal);
		AuroraActionBar actionBar = getAuroraActionBar();
		actionBar.setTitle(R.string.gn_menu_merge_contacts);
		noContent=(TextView)findViewById(R.id.no_content);
		mListView = (AuroraListView) findViewById(android.R.id.list);
		// aurora <wangth> <2013-9-27> modify for aurora ui end
		showDialog();
		showDuplicate(this);
	}

	protected void initView() {
		setContentView(R.layout.manual_combine);

		mListView = (AuroraListView) findViewById(android.R.id.list);
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		actionbar.setTitle(R.string.gn_combine_contacts_title);

	}

	protected void showDuplicate(final Context context)
	{
		new AsyncTask<Integer, Integer, Integer>()
		{

			@Override
			protected Integer doInBackground(Integer... params)
			{
				Log.d(TAG,"showDuplicate");
				mAllSameNumberContacts = new ArrayList<MergeContacts.CombineItemData>();
				SameContactItems allSameNumberContacts = null;
				try
				{
					mAllSameNameContacts = mMergeContact.querySameRawContact(context);
					Log.i(TAG, System.currentTimeMillis()+"          s");
					allSameNumberContacts = mMergeContact.querySameNumberRawContact(context);
					Log.i(TAG, System.currentTimeMillis()+"          e");
					Log.d(TAG,"allSameNumberContacts size:"+(allSameNumberContacts==null?0:allSameNumberContacts.size())
							+" mAllSameNameContacts size:"+(mAllSameNameContacts==null?0:mAllSameNameContacts.size()));
				} catch (LetStopException e) {
					e.printStackTrace();
				} finally {
					if(allSameNumberContacts==null&&mAllSameNameContacts==null) {
						publishProgress(0);
						return null;
					}else if(allSameNumberContacts==null&&mAllSameNameContacts!=null) {
						allSameNumberContacts=mAllSameNameContacts;
					}else if(allSameNumberContacts!=null&&mAllSameNameContacts!=null){
						for (ArrayList<RawContactItem> sameName : mAllSameNameContacts) {
							allSameNumberContacts.add(sameName);
						}
					}

					if (mMergeContact.isLetStop() || null == allSameNumberContacts ||
							allSameNumberContacts.size() <= 0) {
						publishProgress(0);
						return null;
					}	

					Log.d(TAG, "allSameNumberContacts size:"+allSameNumberContacts.size());
				}




				for (ArrayList<RawContactItem> sameName : allSameNumberContacts) {
					if (null != sameName) {
						for (RawContactItem rci : sameName) {
							if (null != rci) {
								rci.mRawCom = null;
							}
						}
					}
				}

				//~~~~~~~~~~~~~~~~~~~~~inject number start~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				final int sizeList = allSameNumberContacts.size();
				ArrayList<RawContactItem> items = null;
				RawContactItem item = null;
				// gionee xuhz 20120823 modify for CR00678320 start
				final String sel = Data.MIMETYPE +" in ('"+ Phone.CONTENT_ITEM_TYPE +
						"' , '" + Photo.CONTENT_ITEM_TYPE + "')" + 
						" AND " + Data.RAW_CONTACT_ID + " in ("; 
				/*final String sel = Data.MIMETYPE +"='"+ Phone.CONTENT_ITEM_TYPE +
					"' AND " + Data.RAW_CONTACT_ID + " in ("; */
				// gionee xuhz 20120823 modify for CR00678320 end
				StringBuilder sb = null;
				String[] projection = new String[]{"_id", Data.RAW_CONTACT_ID, "data1", "photo_id"};

				//				mCombineItemsAdapter = new CombineItemsAdapter(context, mAllSameNumberContacts);
				//              publishProgress(1);
				HashMap<Integer, RawContactItem> itemMap = null;
				for (int i = 0; i < sizeList; ++i) {
					CombineItemData data = new CombineItemData();
					sb = new StringBuilder(sel);
					items = allSameNumberContacts.get(i);
					int sizeItems = items.size();
					Log.d(TAG, "sizeItems:"+sizeItems);
					itemMap = new HashMap<Integer, RawContactItem>(sizeItems);
					for (int k = 0; k < sizeItems; ++k) {
						item = items.get(k);
						itemMap.put(item.mRawContactId, item);
						Log.d(TAG, "item.mRawContactId:"+item.mRawContactId);
						sb.append(item.mRawContactId).append(",");
					}
					sb.replace(sb.length()-1, sb.length(), ")");

					Log.d(TAG,"sb.toString():"+sb.toString());
					Cursor cursor = mContentResolver.query(Data.CONTENT_URI,
							projection, sb.toString(), null, null);
					Log.d(TAG,"cursor count:"+cursor.getCount());
					if (null != cursor) {
						if (cursor.moveToFirst()) {
							do {
								int rcid = cursor.getInt(1);
								item = itemMap.get(rcid);
								//								if(item != null){
								// gionee xuhz 20120823 modify for CR00678320 start
								String tmpNum = cursor.getString(2);
								Log.d(TAG,"tmpNum:"+tmpNum);
								if (!TextUtils.isEmpty(tmpNum)) {
									if (null == item.mRawCom) {
										item.mRawCom = tmpNum; 
									} else {
										//											numbers = numbers + "\n" + tmpNum;
									}
								}
								// gionee xuhz 20120823 modify for CR00678320 end


								int photoId = cursor.getInt(3);
								if (0 != photoId) {
									item.mPhotoId = photoId;
								}

								List<String> numbers = item.mNumbers;
								if(numbers == null){
									numbers = new ArrayList<String>();
									item.mNumbers = numbers;
								}
								numbers.add(tmpNum);
								//								}
							} while(cursor.moveToNext());
						}

						cursor.close();
					}

					data.data = items;
					mAllSameNumberContacts.add(data);
					//					publishProgress(2);
					itemMap.clear();
				}
				publishProgress(3);
				//~~~~~~~~~~~~~~~~~~~~~inject number end~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				/*int ret = (null != values && values.length == 1) ? values[0] : -1;*/
				int ret = values[0];
				switch (ret) {
				case 0:
					if(dialog!=null){
						dialog.dismiss();
					}
					noContent.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
					break;
				case 1:					
					mListView.setAdapter(mCombineItemsAdapter);
					mListView.setOnScrollListener(mCombineItemsAdapter);
					updataUi();
					break;
				case 2:
					mCombineItemsAdapter.notifyDataSetChanged();
					break;
				case 3:
					if(dialog!=null){
						dialog.dismiss();
					}
					mCombineItemsAdapter = new CombineItemsAdapter(context, mAllSameNumberContacts);
					mListView.setAdapter(mCombineItemsAdapter);
					mListView.setOnScrollListener(mCombineItemsAdapter);
					break;

				default:
					break;
				}

			};    		
		}.execute();
	}

	class CombineNumbersAdapter extends LinearListAdapter{
		private List<String> mDate;
		private Context mContext;
		private LayoutInflater mInflater;
		public CombineNumbersAdapter(Context context){
			mContext = context;
			this.mInflater = LayoutInflater.from(context);
		}
		public void setData(List<String> numbers){
			mDate = numbers;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mDate == null?0:mDate.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			String number = mDate.get(position);


			ViewHolder holder = null;

			if (convertView == null) {

				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.combine_number_item_view, null);

				holder.num = (TextView) convertView.findViewById(R.id.num);


				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();

			}
			holder.num.setText(number);
			return convertView;
		}



	}


	public static class ViewHolder {

		public TextView num;
	}

	class CombineItemAdapter extends LinearListAdapter{
		private ArrayList<RawContactItem> mDate;
		private Context mContext;
		public CombineItemAdapter(Context context){
			mContext = context;
		}
		public void setData(ArrayList<RawContactItem> record){
			mDate = record;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mDate == null?0:mDate.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			RawContactItem r = mDate.get(position);
			ContactSimpleInfoView infoView;
			if(convertView == null){
				infoView = ContactSimpleInfoView.create(mContext);
			} else {
				infoView = (ContactSimpleInfoView)convertView;
			}
			infoView.mNameTv.setText(r.mDisplayName);

			// aurora <wangth> <2013-9-27> add for aurora ui begin
			infoView.mHeadIv.setVisibility(View.GONE);
			// aurora <wangth> <2013-9-27> add for aurora ui end

			CombineNumbersAdapter itemAdapter = (CombineNumbersAdapter)infoView.mNumberTv.getAdapter();
			if(itemAdapter == null){
				itemAdapter = new CombineNumbersAdapter(mContext);
				itemAdapter.setData(r.mNumbers);
				infoView.mNumberTv.setAdapter(itemAdapter);
			} else {
				itemAdapter.setData(r.mNumbers);
				itemAdapter.notifyDataSetChanged();
			}
			return infoView;
		}

	}

	class CombineItemsAdapter extends BaseAdapter implements OnScrollListener {
		private Context mContext;
		private List<CombineItemData> mSameAllRecord;
		//Gionee <xuhz> <2013-08-15> delete for CR00857585 begin
		//private final ContactPhotoManager mContactPhotoManager;
		//Gionee <xuhz> <2013-08-15> delete for CR00857585 end

		public CombineItemsAdapter(Context context, List<CombineItemData> sameAllRecord) {
			mContext = context;
			mSameAllRecord = sameAllRecord;
			//Gionee <xuhz> <2013-08-15> delete for CR00857585 begin
			//mContactPhotoManager = ContactPhotoManager.getInstance(mContext);
			//Gionee <xuhz> <2013-08-15> delete for CR00857585 end
		}

		@Override
		public int getCount() {
			return mSameAllRecord.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			System.out.println("position="+position);
			CombineItemView combineItemView = null;
			if (null != convertView) {
				combineItemView = (CombineItemView) convertView;
				//				combineItemView.mCombineItemContainer.removeAllViews();
			} else {
				combineItemView = CombineItemView.create(mContext);
				combineItemView.setOnCombineItemClickListener(combineItemViewClick);
				//combineItemView.mCombineItemContainer.setPadding(0, 0, 0, 4);
			}
			CombineItemData data = mSameAllRecord.get(position);
			combineItemView.setState(data.state);
			final ArrayList<RawContactItem> record = data.data;
			CombineItemAdapter itemAdapter = (CombineItemAdapter)combineItemView.mCombineItemContainer.getAdapter();
			if(itemAdapter == null){
				itemAdapter = new CombineItemAdapter(mContext);
				itemAdapter.setData(record);
				combineItemView.mCombineItemContainer.setAdapter(itemAdapter);
			} else {
				itemAdapter.setData(record);
				itemAdapter.notifyDataSetChanged();
			}
			combineItemView.setDate(data);

			return combineItemView;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				mContactPhotoManager.resume();
				//	            notifyDataSetChanged();
			} else {
				mContactPhotoManager.pause();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	}

	private CombineItemViewClick combineItemViewClick = new CombineItemViewClick() {

		@Override
		public void combine(Object date) {
			showMergingProgess((CombineItemData)date);
//			goCombine((CombineItemData)date);
		}
	};

	protected ContactsUtils.AuroraContactsProgressDialog mQueryProgressDialog;

	protected void showMergingProgess(final CombineItemData data) {
		mQueryProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(this, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
		mQueryProgressDialog.setTitle(getString(R.string.isdealing));			
		mQueryProgressDialog.setIndeterminate(true);
		mQueryProgressDialog.setMax(1);
		mQueryProgressDialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
		mQueryProgressDialog.setOnCancelListener(new OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				Log.d(TAG, "onCancel");
			}
		});
		mQueryProgressDialog.setProgress(0);
		mQueryProgressDialog.setCanceledOnTouchOutside(false);
		mQueryProgressDialog.show();	

		new AsyncTask<CombineItemData, Integer, Integer>() {
			@Override
			protected Integer doInBackground(CombineItemData... params) {
				goCombine(data);
				return null;
			}
			protected void onPostExecute(Integer result) {
				Log.d(TAG, "onPostExecute");
				
			}

		}.execute();
	}

	protected void goCombine(CombineItemData data) {
		if (null == data) {
			return;
		}
		ArrayList<RawContactItem> items4CombineList = data.data;
		if(items4CombineList.size() < 2){
			return;
		}

		mContacts4Combine = data;

		StringBuilder selection = new StringBuilder("_id in (");

		for (RawContactItem item : items4CombineList) {
			selection.append(item.mRawContactId).append(",");
		}
		selection.replace(selection.length()-1, selection.length(), ")");
		Log.d(TAG,"goCombine,selection:"+selection);
		Cursor cursor=null;
		//		try{
		cursor = getContentResolver().query(
				/*RawContactsEntity.CONTENT_URI*/Uri.parse("content://com.android.contacts/raw_contact_entities_v2")/*liyang 修改手动分组逻辑 20160115 */, null, selection.toString(),
				null, null);
		//		}catch(Exception e){
		//			Log.d("merge1","e:"+e);
		//		}


		if(cursor==null){
			Log.d(TAG,"cursur null");
			return;
		}

		cursor.moveToPosition(-1);
		//		while(cursor.moveToNext()){
		//			Log.d("merge1","cursor111:"+cursor.getLong(cursor.getColumnIndex("contact_last_updated_timestamp_v2")));
		//		}
		if (null != cursor) {
			EntityIterator iterator = RawContacts.newEntityIterator(cursor);
			ArrayList<ContentValues> values = new ArrayList<ContentValues>();
			AccountWithDataSet account = null;
			while (iterator.hasNext()) {
				Entity e = iterator.next();

				if (null == account) {
					String type = e.getEntityValues().getAsString("account_type");
					String name = e.getEntityValues().getAsString("account_name");
					//Gionee <xuhz> <2013-08-09> modify for CR00850854 begin raw_contact_entities 
					if (null != type && null != name) {
						//Gionee <xuhz> <2013-08-09> modify for CR00850854 end
						account = new AccountWithDataSet(name, type, null);	
					}
				}
				ArrayList<NamedContentValues> subs = e.getSubValues();
				Log.d("merge1","subs.count:"+subs.size());
				for (NamedContentValues s : subs) {
					Log.d("merge1","s:"+s.values.size());
					values.add(s.values);
				}
			}

			//Gionee:huangzy 20120801 modify for CR00651523 start
			int k=0;
			for (ContentValues v : values) {
				v.remove(Data.DATA_VERSION);
				v.remove(Data.IS_SUPER_PRIMARY);
				v.remove(Data.IS_PRIMARY);
				v.remove(Data._ID);			
				v.remove("data_sync4");
				cursor.moveToPosition(k++);//liyang 修改手动分组逻辑 20160115 
				v.put("contact_last_updated_timestamp_v2",cursor.getLong(cursor.getColumnIndex("contact_last_updated_timestamp_v2"))); //liyang 修改手动分组逻辑 20160115 
				v.put("contact_id",cursor.getInt(cursor.getColumnIndex("contact_id"))); //liyang 修改手动分组逻辑 20160115 
				v.put("is_delete",0);
			}

			mPhotoUri = null;

			//liyang 修改手动分组逻辑 20160115 begin
			String latestName=null;
			String latestOrganization=null;
			String latestContactEvent=null;
			String latestRingtone=null;
			String latestGroup=null;
			String latestNote=null;
			String latestPhoto=null;
			int latestUpdateContactId=0;
			if(values.size()>0){
				latestUpdateContactId=(Integer) values.get(0).get("contact_id");

				long max=(Long) values.get(0).get("contact_last_updated_timestamp_v2");

				for (int i = 0; i < values.size(); i++) {
					ContentValues map=values.get(i);
					for (String key : map.keySet()) {  
						Log.d(TAG,"i="+i+" key= "+ key + " and value= " + map.get(key));  
					} 
					Log.d(TAG,"size:"+values.size()+" values"+i+":"+values.get(i).get(Data.DATA1)+"\nupdatedtime:"
							+values.get(i).get("contact_last_updated_timestamp_v2"));

					if(max<(Long)map.get("contact_last_updated_timestamp_v2")){
						max=(Long)map.get("contact_last_updated_timestamp_v2");
						latestUpdateContactId=(Integer) map.get("contact_id");

					}
				}
			}

			for (int i = 0; i < values.size(); i++) {
				ContentValues value = values.get(i);
				int contact_id=(Integer) value.get("contact_id");
				String mimeType = (String) value.get(Data.MIMETYPE);
				String data1 = value.getAsString(Data.DATA1);
				if(contact_id==latestUpdateContactId){
					if("vnd.android.cursor.item/name".equalsIgnoreCase(mimeType)){
						latestName=data1;
					}else if("vnd.android.cursor.item/group_membership".equalsIgnoreCase(mimeType)){
						latestGroup=data1;
					}else if("vnd.android.cursor.item/note".equalsIgnoreCase(mimeType)){
						latestNote=data1;
					}else if("vnd.android.cursor.item/organization".equalsIgnoreCase(mimeType)){
						latestOrganization=data1;
					}else if("vnd.android.cursor.item/contact_event".equalsIgnoreCase(mimeType)){
						latestContactEvent=data1;
					}else if("vnd.android.cursor.item/photo".equalsIgnoreCase(mimeType)){
						latestPhoto=value.getAsString(Data.DATA14);
					}

				}
			}

			boolean nameFound=false;
			boolean groupFound=false;
			boolean noteFound=false;
			boolean organizationFound=false;
			boolean contactEventFount=false;
			boolean photoFound=false;
			Log.d(TAG,"latestName:"+latestName+" latestGroup:"+latestGroup+" latestNote:"+latestNote+" latestOrganization:"+latestOrganization
					+" latestContactEvent:"+latestContactEvent+" latestPhoto:"+latestPhoto);

			Log.d(TAG,"latestUpdateContactId:"+latestUpdateContactId);
			ArrayList numberList=new ArrayList();
			for (int i = 0; i < values.size(); i++) {	
				ContentValues value = values.get(i);
				String mimeType = (String) value.get(Data.MIMETYPE);
				String data1 = value.getAsString(Data.DATA1); 
				int contact_id=(Integer) value.get("contact_id");
				Log.d("merge1","mimeType:"+mimeType+" data1:"+data1);
				if("vnd.android.cursor.item/name".equalsIgnoreCase(mimeType)){
					Log.d("merge1","vnd.android.cursor.item/name");
					if(contact_id!=latestUpdateContactId && latestName!=null){
						Log.d("merge1","vnd.android.cursor.item/name1");
						//						values.remove(i);
						value.put("is_delete",1);//0 not delete; 1 delete
					}else if(contact_id!=latestUpdateContactId && latestName==null){
						Log.d("merge1","vnd.android.cursor.item/name2");
						if(!TextUtils.isEmpty(data1)){
							Log.d("merge1","vnd.android.cursor.item/name3");
							if(nameFound) {
								Log.d("merge1","vnd.android.cursor.item/name4");
								//								values.remove(i);
								value.put("is_delete",1);//0 not delete; 1 delete
							}

							if(!nameFound) {
								nameFound=true;
							}

						}
					}
				}else if("vnd.android.cursor.item/group_membership".equalsIgnoreCase(mimeType)){
					if(contact_id!=latestUpdateContactId && latestGroup!=null){
						//						values.remove(i);
						value.put("is_delete",1);//0 not delete; 1 delete
					}else if(contact_id!=latestUpdateContactId && latestGroup==null){
						if(!TextUtils.isEmpty(data1)){
							if(groupFound) {
								//								values.remove(i);
								value.put("is_delete",1);//0 not delete; 1 delete
							}

							if(!groupFound) {
								groupFound=true;
							}

						}
					}
				}else if("vnd.android.cursor.item/note".equalsIgnoreCase(mimeType)){
					Log.d("merge1","vnd.android.cursor.item/note");
					if(contact_id!=latestUpdateContactId && latestNote!=null){
						Log.d("merge1","vnd.android.cursor.item/note1");
						//						values.remove(i);
						value.put("is_delete",1);//0 not delete; 1 delete
					}else if(contact_id!=latestUpdateContactId && latestNote==null){
						Log.d("merge1","vnd.android.cursor.item/note2");
						if(!TextUtils.isEmpty(data1)){
							Log.d("merge1","vnd.android.cursor.item/note3");
							if(noteFound) {
								Log.d("merge1","vnd.android.cursor.item/note4");
								//								values.remove(i);
								value.put("is_delete",1);//0 not delete; 1 delete
							}

							if(!noteFound) {
								groupFound=true;
							}

						}
					}
				}else if("vnd.android.cursor.item/organization".equalsIgnoreCase(mimeType)){
					if(contact_id!=latestUpdateContactId && latestOrganization!=null){
						//							values.remove(i);
						value.put("is_delete",1);//0 not delete; 1 delete
					}else if(contact_id!=latestUpdateContactId && latestOrganization==null){
						if(!TextUtils.isEmpty(data1)){
							if(organizationFound) {
								//									values.remove(i);
								value.put("is_delete",1);//0 not delete; 1 delete
							}

							if(!organizationFound) {
								groupFound=true;
							}

						}
					}
				}else if("vnd.android.cursor.item/contact_event".equalsIgnoreCase(mimeType)){
					if(contact_id!=latestUpdateContactId && latestContactEvent!=null){
						//						values.remove(i);
						value.put("is_delete",1);//0 not delete; 1 delete
					}else if(contact_id!=latestUpdateContactId && latestContactEvent==null){
						if(!TextUtils.isEmpty(data1)){
							if(contactEventFount) {
								//								values.remove(i);
								value.put("is_delete",1);//0 not delete; 1 delete
							}

							if(!contactEventFount) {
								contactEventFount=true;
							}

						}
					}
				}else if("vnd.android.cursor.item/photo".equalsIgnoreCase(mimeType)){
					if(contact_id!=latestUpdateContactId && latestPhoto!=null){
						//					values.remove(i);
						value.put("is_delete",1);//0 not delete; 1 delete
					}else if(contact_id!=latestUpdateContactId && latestPhoto==null){
						if(!TextUtils.isEmpty( value.getAsString(Data.DATA14))){
							if(photoFound) {
								//							values.remove(i);
								value.put("is_delete",1);//0 not delete; 1 delete
							}

							if(!photoFound) {
								photoFound=true;
							}

						}
					}
				}else if (Phone.CONTENT_ITEM_TYPE.equalsIgnoreCase(mimeType)) {
					if(!TextUtils.isEmpty(data1)){
						if(!numberList.contains(data1)){
							Log.d("merge1","numberList.contains(data1)");
							numberList.add(data1);
						}else{
							value.put("is_delete",1);
						}
					}
				}
			}

			//delete rows
			ArrayList<ContentValues> resultValues = new ArrayList<ContentValues>();
			for(int i=0;i<values.size();i++){
				ContentValues value = values.get(i);
				if(value.get("is_delete").toString().equals("0")){
					resultValues.add(value);
				}
			}

			for(ContentValues v:resultValues){
				if(v.get("data1")!=null){
					Log.d("merge1","data1:"+v.get("data1").toString());
				}else if(v.get("data14")!=null){
					Log.d("merge1","data14:"+v.get("data14").toString());

				}
			}

			//liyang 修改手动分组逻辑 20160115 end



			/*for (int i = 0; i < values.size(); i++) {				
				for (int j = i + 1; j < values.size();) {
					ContentValues valuesA = values.get(i);
					ContentValues valuesB = values.get(j);
					String mimeTypeB = (String) valuesB.get(Data.MIMETYPE);
					String mimeTypeA = (String) valuesA.get(Data.MIMETYPE);
					if (Phone.CONTENT_ITEM_TYPE.equalsIgnoreCase(mimeTypeA)) {

						// Gionee <xuhz> <2013-07-29> modify for CR00838797 begin
						String aData1 = valuesA.getAsString(Data.DATA1);
						if (Phone.CONTENT_ITEM_TYPE.equalsIgnoreCase(mimeTypeB) && 
								aData1 != null && aData1.equals(valuesB.getAsString(Data.DATA1))) {
							// Gionee <xuhz> <2013-07-29> modify for CR00838797 end
							if( valuesA.getAsString(Data.DATA2)==null){
								if( valuesB.getAsString(Data.DATA2)!=null){
									valuesA.put(Data.DATA2, valuesB.getAsString(Data.DATA2));
								}
							}else{
								valuesB.put(Data.DATA2, valuesA.getAsString(Data.DATA2));
							}
						}
					}
					if("vnd.android.cursor.item/name".equalsIgnoreCase(mimeTypeA)&&"vnd.android.cursor.item/name".equalsIgnoreCase(mimeTypeB)){
						values.remove(j);
					} else {
						if (valuesA.equals(valuesB)) {
							values.remove(j);
						} else {
							j++;
						}
					}     
				}

				// gionee xuhz 20120830 add for CR00678320 start
				ContentValues valuesA = values.get(i);
				String mimeTypeA = (String) valuesA.get(Data.MIMETYPE);
				if (Photo.CONTENT_ITEM_TYPE.equalsIgnoreCase(mimeTypeA)) {
					Integer data14 = valuesA.getAsInteger(Data.DATA14);
					if (mPhotoUri == null) {
						if (data14 != null) {
							String phontoUri = "content://com.android.contacts/display_photo/" + data14.intValue();
							mPhotoUri = Uri.parse(phontoUri);
						} else {
							values.remove(i);
						}
					} else {
						values.remove(i);
					}
				}
				// gionee xuhz 20120830 add for CR00678320 end
			}*/			
			//Gionee:huangzy 20120801 modify for CR00651523 end

			iterator.close();
			cursor.close();

			Log.d(TAG, "startService");
			startService(ContactSaveService.createNewRawContactIntent(this, resultValues, account, 
					ManualCombineByNumberActivity.class, Intent.ACTION_EDIT));

			ContactSaveService.registerListener(this);
		}			
	}	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);	    
		if (resultCode == RESULT_OK) {
			mTmpContactConfirmed = true;
			if (null != mContacts4Combine) {
				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

				// gionee xuhz 20120830 add for CR00678320 start
				Uri lookUpUri = data.getData();
				Uri contactUri = Contacts.lookupContact(mContentResolver, lookUpUri);
				long contactId = ContentUris.parseId(contactUri);
				Long[] mRawContactIds;
				ArrayList<Long> rawContactIdsList = AttachPhotoActivity.queryForAllRawContactIds(
						mContentResolver, contactId);
				mRawContactIds = new Long[rawContactIdsList.size()];
				mRawContactIds = rawContactIdsList.toArray(mRawContactIds);

				Bitmap photo = null;
				if (null != mPhotoUri) {
					try {
						InputStream is = mContentResolver.openInputStream(mPhotoUri);
						photo = BitmapFactory.decodeStream(is);                    
						is.close();
					} catch (Exception e) {
					}

					if (null != photo) {
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);

						final ContentValues imageValues = new ContentValues();
						imageValues.put(Photo.PHOTO, stream.toByteArray());
						imageValues.put(RawContacts.Data.IS_SUPER_PRIMARY, 1);

						// attach the photo to every raw contact
						for (Long rawContactId : mRawContactIds) {
							Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
									rawContactId);
							Uri rawContactDataUri = Uri.withAppendedPath(rawContactUri,
									RawContacts.Data.CONTENT_DIRECTORY);
							updatePhoto(imageValues, rawContactDataUri, true);
						}
					}
				}
				// gionee xuhz 20120830 add for CR00678320 end

				ArrayList<RawContactItem> items4CombineList = mContacts4Combine.data;
				for (RawContactItem con : items4CombineList) {
					ops.add(ContentProviderOperation.newDelete(
							ContentUris.withAppendedId(RawContacts.CONTENT_URI, con.mRawContactId)).build());
				}
				try {
					getContentResolver().applyBatch(GnContactsContract.AUTHORITY, ops);
				} catch (Exception e){
				}

				mContacts4Combine.state = CombineItemData.COMBINE_FINISH;
				updataUi();
			}
		} else {
			deleteTmpContact();
		}
	}

	@Override
	public void onServiceCompleted(Intent callbackIntent) {
		if (null == callbackIntent) {
			return;
		}        

		if(mQueryProgressDialog!=null) mQueryProgressDialog.dismiss();
		
		mTmpContactCombined = callbackIntent.getData();
		Intent intent = new Intent(Intent.ACTION_EDIT, mTmpContactCombined);
		//Gionee:huangzy 20130401 modify for CR00792013 start
		intent.addCategory(IntentFactory.GN_CATEGORY);
		//Gionee:huangzy 20130401 modify for CR00792013 end
		intent.putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
		intent.putExtra(ContactEditorActivity.INTENT_KEY_AUTO_SAVE_ON_BACK_PRESS, false);
		startActivityForResult(intent, 1);
	}

	private void deleteTmpContact() {
		if (null != mTmpContactCombined) {
			getContentResolver().delete(mTmpContactCombined, null, null);   
		}        
	}

	protected void updataUi() {
		mCombineItemsAdapter.notifyDataSetChanged();
		//        getActionBar().setSubtitle(String.format(getString(R.string.gn_combine_contacts_sub_title),
		//                mCombineItemsAdapter.getCount()));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {            
		case android.R.id.home:
			onBackPressed();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (!mTmpContactConfirmed) {
			deleteTmpContact();   
		}
		Log.i(TAG, "123");
	}

	// gionee xuhz 20120830 add for CR00678320 start
	/**
	 * Inserts a photo on the raw contact.
	 * @param values the photo values
	 * @param assertAccount if true, will check to verify that no photos exist for Google,
	 *     Exchange and unsynced phone account types. These account types only take one picture,
	 *     so if one exists, the account will be updated with the new photo.
	 */
	private void insertPhoto(ContentValues values, Uri rawContactDataUri,
			boolean assertAccount) {

		ArrayList<ContentProviderOperation> operations =
				new ArrayList<ContentProviderOperation>();

		if (assertAccount) {
			// Make sure no pictures exist for Google, Exchange and unsynced phone accounts.
			/*
			 * Bug Fix by Mediatek Begin.
			 *   Original Android's code:
			 *     operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                    .withSelection(Photo.MIMETYPE + "=? AND "
                            + RawContacts.DATA_SET + " IS NULL AND ("
                            + RawContacts.ACCOUNT_TYPE + " IN (?,?) OR "
                            + RawContacts.ACCOUNT_TYPE + " IS NULL)",
                            new String[] {Photo.CONTENT_ITEM_TYPE, GoogleAccountType.ACCOUNT_TYPE,
                            ExchangeAccountType.ACCOUNT_TYPE})
                            .withExpectedCount(0).build());
			 *   CR ID: ALPS00249819
			 *   Descriptions: add phone account type in query 
			 */
			operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
					.withSelection(Photo.MIMETYPE + "=? AND "
							+ RawContacts.DATA_SET + " IS NULL AND ("
							+ RawContacts.ACCOUNT_TYPE + " IN (?,?,?) OR "
							+ RawContacts.ACCOUNT_TYPE + " IS NULL)",
							new String[] {Photo.CONTENT_ITEM_TYPE, GoogleAccountType.ACCOUNT_TYPE,
							ExchangeAccountType.ACCOUNT_TYPE, LocalPhoneAccountType.ACCOUNT_TYPE})
							.withExpectedCount(0).build());
			/*
			 * Bug Fix by Mediatek End.
			 */
		}

		// insert the photo
		values.put(Photo.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
		operations.add(ContentProviderOperation.newInsert(rawContactDataUri)
				.withValues(values).build());

		try {
			mContentResolver.applyBatch(GnContactsContract.AUTHORITY, operations);
		} catch (RemoteException e) {
			throw new IllegalStateException("Problem querying raw_contacts/data", e);
		} catch (OperationApplicationException e) {
			// the account doesn't allow multiple photos, so update
			if (assertAccount) {
				updatePhoto(values, rawContactDataUri, false);
			} else {
				throw new IllegalStateException("Problem inserting photo into raw_contacts/data", e);
			}
		}
	}

	/**
	 * Tries to update the photo on the raw_contact.  If no photo exists, and allowInsert == true,
	 * then will try to {@link #updatePhoto(ContentValues, boolean)}
	 */
	private void updatePhoto(ContentValues values, Uri rawContactDataUri,
			boolean allowInsert) {
		ArrayList<ContentProviderOperation> operations =
				new ArrayList<ContentProviderOperation>();

		values.remove(Photo.MIMETYPE);

		// check that a photo exists
		operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
				.withSelection(Photo.MIMETYPE + "=?", new String[] {
						Photo.CONTENT_ITEM_TYPE
				}).withExpectedCount(1).build());

		// update that photo
		operations.add(ContentProviderOperation.newUpdate(rawContactDataUri)
				.withSelection(Photo.MIMETYPE + "=?", new String[] {Photo.CONTENT_ITEM_TYPE})
				.withValues(values).build());

		try {
			mContentResolver.applyBatch(GnContactsContract.AUTHORITY, operations);
		} catch (RemoteException e) {
			throw new IllegalStateException("Problem querying raw_contacts/data", e);
		} catch (OperationApplicationException e) {
			if (allowInsert) {
				// they deleted the photo between insert and update, so insert one
				insertPhoto(values, rawContactDataUri, false);
			} else {
				throw new IllegalStateException("Problem inserting photo raw_contacts/data", e);
			}
		}
	}
	// gionee xuhz 20120830 add for CR00678320 end

	private void showDialog(){
		if (dialog == null) {
			dialog = new AuroraProgressDialog(this);
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.setMessage(getResources().getString(R.string.gn_combine_contacts_load));
		}
		dialog.show();
	}



}
