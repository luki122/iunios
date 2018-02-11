package com.mediatek.contacts.dialpad;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.R.integer;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.CallLog.Calls;
import gionee.provider.GnContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.activities.AuroraCallRecordActivity;
import com.android.contacts.activities.AuroraContactsSetting;
import com.android.contacts.calllog.AuroraCallLogListItemViewV2;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.calllog.CallLogResHolder;
import com.android.contacts.calllog.GnContactInfo;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2;
import com.android.contacts.util.Constants;
import com.android.contacts.util.DensityUtil;
import com.android.contacts.util.GnHotLinesUtil;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.StatisticsUtil;
import com.android.contacts.util.YuloreUtils;
import com.android.contacts.widget.AuroraDialerSearchItemView;
import com.mediatek.contacts.CallOptionHandler;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.calllog.CallLogSimInfoHelper;
import com.mediatek.contacts.dialpad.IDialerSearchController.GnDialerSearchResultColumns;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.android.contacts.util.GnCallForSelectSim;
import com.android.contacts.GNContactsUtils;

public class AuroraDialerSearchAdapter extends CursorAdapter implements OnClickListener {
	private static final String TAG = "liyang-DialerSearchAdapter";

	protected Context mContext;

	protected int mSpanColorFg;
	protected int mSpanColorBg;

	protected ContactPhotoManager mContactPhotoManager;
	protected CallLogSimInfoHelper mCallLogSimInfoHelper;

	protected SIMInfoWrapper mSIMInfoWrapper;

	protected DisplayMetrics mDisplayMetrics;

	protected int mOperatorVerticalPadding;
	protected int mOperatorHorizontalPadding;

	protected HyphonManager mHyphonManager;
	protected CallOptionHandler mCallOptionHandler;

	public static PopupWindow popupWindow;
	private LinearLayout pop_detail;
	private LinearLayout pop_sendsms;
	private LinearLayout pop_copy;
	private LinearLayout pop_delete;
	private String[] strs;
	private static ExecutorService exec = Executors.newSingleThreadExecutor();

	private boolean showDouble=false;


	public AuroraDialerSearchAdapter(Context context) {
		super(context, null, false);
		mContext = context;

		mSpanColorFg = Color.WHITE;
		mSpanColorBg = context.getResources().getColor(R.color.aurora_search_matched_highlight);

		mSIMInfoWrapper = SIMInfoWrapper.getDefault();

		mContactPhotoManager = ContactPhotoManager.getInstance(context);
		mCallLogSimInfoHelper = new CallLogSimInfoHelper(mContext.getResources());

		final Resources r = mContext.getResources();
		mOperatorVerticalPadding = r.getDimensionPixelSize(R.dimen.dialpad_operator_vertical_padding);
		mOperatorHorizontalPadding = r.getDimensionPixelSize(R.dimen.dialpad_operator_horizontal_padding);

		mHyphonManager = HyphonManager.getInstance();
		mMissedCallColor = context.getResources().getColor(
				R.color.gn_misscall_color);
		mNormalCallColor = context.getResources().getColor(
				R.color.aurora_dialer_search_text_color);
		mCallTypeResHolder = new CallLogResHolder(context);

		showDouble = ContactsUtils.isShowDoubleButton();



		deviderLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
		deviderLayoutParams.setMargins(mContext.getResources().getDimensionPixelOffset(R.dimen.double_list_item_photo_width),0,0,0);


	}

	private int CursorIndex;
	private Cursor mCursor;
	@Override
	public void changeCursor(Cursor cursor) {
		if (mCursor != cursor) {
			mCursor = cursor;
		}

		super.changeCursor(cursor);
	}

	//	@Override
	//	public View getView(int position, View convertView, ViewGroup parent) {
	//		CursorIndex=position;	
	//		View view=newView(mContext, mCursor, parent);
	//		bindView(view, mContext, mCursor);
	//		return view;
	//	}


	private LinearLayout.LayoutParams deviderLayoutParams;
	private AuroraDialpadFragmentV2 auroraDialpadFragmentV2;
	public AuroraDialerSearchAdapter(Context context, CallOptionHandler callOptionHandler,AuroraDialpadFragmentV2 auroraDialpadFragmentV2) {
		this(context);
		this.auroraDialpadFragmentV2=auroraDialpadFragmentV2;
		mCallOptionHandler = callOptionHandler;
	}

	public void bindView(View view, Context context, Cursor cursor) {
		/*Log.d(TAG,"search bindview"+view+" cursor:"+cursor+" getPosition():"+cursor.getPosition());
//		if(true) return;
		ItemHolder itemHolder = (ItemHolder) view.getTag();
		AuroraDialerSearchItemView itemviewv2 = itemHolder.itemviewv2;

		final int contact_id = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);

//		Log.d(TAG,"search bindview,view:"+view+" contact_id:"+contact_id);

		if (view instanceof AuroraDialerSearchItemView) {
			//			AuroraDialerSearchItemView itemView = (AuroraDialerSearchItemView)view;
			//			itemView.setOnClickListener(this);
			//aurora change liguangyu 201311125 for BUG #1022 start
			//    		itemView.mSecondaryButton.setBackgroundResource(R.drawable.aurora_call_log_detail);
			if(contact_id <= 0) {
				//    			itemView.mSecondaryButton.setVisibility(View.GONE);
			} else {
				//    			itemView.mSecondaryButton.setVisibility(View.VISIBLE);
			}    		
			//aurora change liguangyu 201311125 for BUG #1022 end    		
		}


		if (contact_id > 0) {
//			Log.d(TAG,"search bindview >0");
			bindViewDialerSearch(view, context, cursor);
		} else if (contact_id < 0) {

			bindViewHotLines(view, context, cursor);
		} else {

			bindViewCallLogs(view, context, cursor);
		}*/

		final int contact_id = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);

		//    	if (view instanceof AuroraDialerSearchItemView) {
		//    		AuroraDialerSearchItemView itemView = (AuroraDialerSearchItemView)view;
		//    		itemView.setOnClickListener(this);
		//    		//aurora change liguangyu 201311125 for BUG #1022 start
		////    		itemView.mSecondaryButton.setBackgroundResource(R.drawable.aurora_call_log_detail);
		//    		if(contact_id <= 0) {
		////    			itemView.mSecondaryButton.setVisibility(View.GONE);
		//    		} else {
		////    			itemView.mSecondaryButton.setVisibility(View.VISIBLE);
		//    		}    		
		//    		//aurora change liguangyu 201311125 for BUG #1022 end    		
		//    	}

		if (contact_id > 0) {
			bindViewDialerSearch(view, context, cursor);
		} else if (contact_id < 0) {
			bindViewHotLines(view, context, cursor);
		} else {
			bindViewCallLogs(view, context, cursor);
		}

	}

	//	@Override
	//	public View getView(int position, View convertView, ViewGroup parent) {
	//		Log.d(TAG, "getView,convertView:"+convertView);
	//		View v;
	//		if (convertView == null) {
	//			v = newView(mContext, mCursor, parent);
	//		} else {
	//			v = convertView;
	//		}
	//
	//		bindView(v, mContext, mCursor);
	//		
	//		return v;
	//	}

	public View newView(Context context, Cursor cursor, ViewGroup parent) {


		AuroraDialerSearchItemView cliv = AuroraDialerSearchItemView.create(context);
		cliv.setId(R.id.dialer_search_item_view);   

		//		View v = (View) LayoutInflater.from(mContext).inflate(
		//				com.aurora.R.layout.aurora_slid_listview, null);

		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_listview_custom_front));
		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_item_sliding_switch));
		//
		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_list_header));
		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_list_left_checkbox));
		//
		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_listview_back));

		/*		RelativeLayout mainUi = (RelativeLayout) v
				.findViewById(com.aurora.R.id.aurora_listview_front);
		mainUi.addView(cliv, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		ImageView iv = (ImageView) v.findViewById(com.aurora.R.id.aurora_listview_divider);
		((LinearLayout)iv.getParent()).setBackgroundColor(mContext.getResources().getColor(R.color.listview_item_color));
		iv.setLayoutParams(deviderLayoutParams);
		iv.setVisibility(View.VISIBLE);
		cliv.mDivider=iv;

		View paddingView=v.findViewById(com.aurora.R.id.control_padding);
		paddingView.setPadding(0, 0, 0, 0);

		ItemHolder itemHolder = new ItemHolder();
		itemHolder.itemviewv2 = cliv;
//		itemHolder.auroraChildContent = (LinearLayout) v.findViewById(com.aurora.R.id.content);
		v.setTag(itemHolder);*/

		Log.d(TAG, "newview,v:"+cliv);
		return cliv;


	}

	private class ItemHolder{
		public AuroraDialerSearchItemView itemviewv2;
		//		public LinearLayout auroraChildContent;
	}

	protected String formatNumber(String number) {
		if(TextUtils.isEmpty(number))
			return number;

		return PhoneNumberUtils.formatNumber(number);
	}

	protected Uri getContactUri(Cursor cursor) {
		final String lookup = cursor.getString(GnDialerSearchResultColumns.LOOKUP_KEY_INDEX);
		final int contactId = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
		return Contacts.getLookupUri(contactId, lookup);
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
			mContactPhotoManager.pause();
		} else {
			mContactPhotoManager.resume();
			notifyDataSetChanged();
		}
	}

	public class ViewHolder {
		public QuickContactBadge photo;
		public ImageButton call;
		public TextView name;
		public TextView labelAndNumber;
		public ImageView type;
		public TextView operator;
		public TextView date;

		public int callId;
		public int viewType;
		public Uri contactUri;
		public View divider;
	}


	private AuroraAlertDialog mCleanCallLogDialog = null;
	public void onMenuCleanCallLog(final int callsCount,final int[] ids) {
		mCleanCallLogDialog = new AuroraAlertDialog.Builder(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
		.setTitle(R.string.gn_clearSelectedCallLogConfirmation_title)  // gionee xuhz 20120728 modify for CR00658189
		//		.setMessage(R.string.gn_clearSelectedCallLogConfirmation)无可显示的联系人
		.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeCallLogRecord(callsCount,ids);

			}
		})
		.setNegativeButton(android.R.string.no, null).create();
		mCleanCallLogDialog.show();        
	}



	protected int getCurCallType() {
		return getCallsType(getCurFilterType());
	}

	private String toDeleteSelectionId(ArrayList<Integer> selected) {
		if (null == selected || selected.size() <= 0) {
			return "";
		}

		int callType = getCurCallType();
		StringBuilder selection = new StringBuilder();
		if (callType > 0) {
			selection.append("type=").append(callType).append(" And ");
		}

		if (ContactsApplication.sIsGnCombineCalllogMatchNumber) {
			selection.append("_id in (");
			for (Integer id : selected) {
				selection.append("'").append(id.intValue()).append("',");
			}

			selection.setLength(selection.length() - 1);
			selection.append(")");
		}

		return selection.toString();
	}

	public void removeCallLogRecord(final int callsCount,final int[] ids) {
		ArrayList<Integer> als = getCallLogCheckedId(callsCount,ids);
		if (als == null || als.size() == 0) {
			return;
		}

		final String selection = toDeleteSelectionId(als);
		new AsyncTask<Integer, Integer, Integer>() {

			protected void onPreExecute() {

			};

			@Override
			protected Integer doInBackground(Integer... params) {
				String privateString = "";
				if (ContactsApplication.sIsAuroraPrivacySupport) {
					privateString = " AND privacy_id > -1 "; 
				} 
				mContext.getContentResolver().delete(Calls.CONTENT_URI,
						selection + privateString , null);

				return null;
			}

			protected void onPostExecute(Integer result) {

			};
		}.executeOnExecutor(exec);

	}

	//aurora add zhouxiaobing 20130929 start	
	public ArrayList<Integer> getCallLogCheckedId(int callsCount,int[] ids) {
		ArrayList<Integer> als = new ArrayList<Integer>();

		for(int j=0;j<callsCount;j++) {
			Log.d("liyang","j:"+j+" ids["+j+"]:"+ids[j]);
			als.add(Integer.valueOf(ids[j]));

		}
		return als;
	}


	String number = null;
	int type=0,callid=0,callscount=0;
	Uri voicemailuri=null;
	int ids[]=null;
	public void onClick(View v) {
		Log.d("liyang","onClick2:"+v);

		int id = v.getId();	


		switch (id) {	

		/*case R.id.pop_delete://删除通话记录
		{

			Log.d("liyang","onclick pop_delete");
			dismissPopupWindow();
			if(strs!=null&&strs.length==6){								

				String callids=strs[3];
				if(callids!=null){
					if(callids.contains(",")){
						String[] idss=callids.split(",");
						ids=new int[idss.length];
						for(int j=0;j<idss.length;j++){
							ids[j]=Integer.parseInt(idss[j]);
						}
					}else{
						ids=new int[1];
						ids[0]=Integer.parseInt(callids);
					}
				}

				callscount=Integer.parseInt(strs[4]);
				Log.d("liyang","callids:"+callids+" callsCount:"+callscount);

				onMenuCleanCallLog(callscount,ids);
			}

			//			onMenuCleanCallLog(contactInfo);
			return;
		}*/

		case R.id.call_date_ll:
		case R.id.expand:
			Log.d("liyang","onclick expand");
		case R.id.main_container:
			Log.d("liyang","onclick main_container");
		case R.id.contact_photo:
			Log.d("liyang","onclick contact_photo");
		case R.id.contact_photo1:
			Log.d("liyang","onclick contact_photo1");
		case R.id.aurora_search_calllog:{
			Log.d("liyang","onclick aurora_search_calllog");
			boolean isCallPrimary = ResConstant.isCallLogListItemCallPrimary();
			boolean isPrimaryClick = false;
			Uri contactUri = null;
			int simId = -1;
			boolean isHotLine = false;

			boolean isPrivacyContact = false;

			Object obj = null;
			View contactPhotoView = null;
			View expandView = null;


			if(id==R.id.contact_photo||id==R.id.contact_photo1) {
				contactPhotoView=v;
			}else if(id==R.id.main_container){
				contactPhotoView=v.findViewById(R.id.contact_photo1);
			}else if(id==R.id.aurora_search_calllog){
				contactPhotoView=v.findViewById(R.id.contact_photo);
			}else if(id==R.id.expand||id==R.id.call_date_ll){
				expandView=v;
			}

			if (null != contactPhotoView) {
				Log.d("liyang","--1");
				obj = contactPhotoView.getTag();
			}else if(expandView!=null){
				Log.d("liyang","--2");
				obj = expandView.getTag();
			}



			if (null != obj && obj instanceof String[]) {
				strs = (String[])obj;
				for(String str:strs){
					Log.d(TAG,"str:"+str);
				}
				if (3 == strs.length) {
					number = strs[0];
					mNumber = number;
					if (null != strs[1]) {
						contactUri = Uri.parse(strs[1]);
						mContactUri = contactUri;
					}

					if (null != strs[2]) {
						isPrivacyContact = Long.valueOf(strs[2]) > 0 ? true : false;
					}
				} else if (2 == strs.length) {
					number = strs[0];
					if(null != strs[1]) {
						simId = Integer.valueOf(strs[1]);
					}
					mNumber = null;
					mContactUri = null;
				} else if(strs.length==6){

					number = strs[0];
					type=Integer.parseInt(strs[1]);
					callid=Integer.parseInt(strs[2]);					

					String callids=strs[3];	
					if(callids!=null){
						if(callids.contains(",")){
							String[] idss=callids.split(",");
							ids=new int[idss.length];
							for(int j=0;j<idss.length;j++){
								ids[j]=Integer.parseInt(idss[j]);
							}
						}else{
							ids=new int[1];
							ids[0]=Integer.parseInt(callids);
						}
					}

					callscount=Integer.parseInt(strs[4]);
					if(strs[5]!=null){
						voicemailuri=Uri.parse(strs[5]);
					}

					Log.d("liyang","number:"+number+"\ntype:"+type+"\ncallid:"+callid+"\ncallids:"+callids+"\ncallscount:"+callscount);

				}		
				else {
					number = strs[0];
					isHotLine = true;
					mNumber = null;
					mContactUri = null;
				}
			}




			if(id==R.id.call_date_ll||id==R.id.expand){
				Log.d("liyang","popupwindow");

				int[] location = new int[2];				
				((View)v.getParent().getParent()).getLocationInWindow(location);	
				int height=location[1]
						+mContext.getResources().getDimensionPixelOffset(R.dimen.double_list_item_height)/2;


				((AuroraActivity)mContext).setAuroraSystemMenuCallBack(auroraMenuCallBack);
				((AuroraActivity)mContext).setAuroraMenuItems(R.menu.calllog_expand_dialsearch);
				((AuroraActivity)mContext).showAuroraMenu(v, Gravity.TOP | Gravity.RIGHT,
						0,height);

				//				View contentView = View.inflate(mContext,
				//						R.layout.popupwindow, null);
				//
				//				pop_detail = (LinearLayout) contentView
				//						.findViewById(R.id.pop_detail);
				//				pop_sendsms = (LinearLayout) contentView
				//						.findViewById(R.id.pop_sendsms);
				//				pop_copy = (LinearLayout) contentView
				//						.findViewById(R.id.pop_copy);
				//				pop_delete = (LinearLayout) contentView
				//						.findViewById(R.id.pop_delete);
				//				pop_delete.setVisibility(View.GONE);
				//
				//				pop_detail.setOnClickListener(AuroraDialerSearchAdapter.this);
				//				pop_sendsms.setOnClickListener(AuroraDialerSearchAdapter.this);
				//				pop_copy.setOnClickListener(AuroraDialerSearchAdapter.this);
				////				pop_delete.setOnClickListener(AuroraDialerSearchAdapter.this);
				//
				//				LinearLayout ll_popup_container = (LinearLayout) contentView
				//						.findViewById(R.id.ll_popup_container);
				//
				//				ScaleAnimation sa = new ScaleAnimation( 0.0f,
				//						1.0f,
				//						0.0f,
				//						1.0f,
				//						Animation.RELATIVE_TO_SELF, 
				//						1.0f, 
				//						Animation.RELATIVE_TO_SELF, 
				//						0.0f);
				//				sa.setDuration(100);				
				//
				//
				//				DisplayMetrics dm = new DisplayMetrics();
				//				((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
				//				int screenW = dm.widthPixels;// 获取分辨率宽度
				//
				//				popupWindow = new PopupWindow(contentView, DensityUtil.dip2px(mContext, 130), 
				//						DensityUtil.dip2px(mContext, 160));
				//				popupWindow.setBackgroundDrawable(new ColorDrawable(
				//						Color.TRANSPARENT));
				//
				//				int[] location = new int[2];
				//				v.getLocationInWindow(location);
				//				Log.d("liyang", "location[0]"+location[0]
				//						+"\nlocation[1]:"+location[1]
				//								+"\nDensityUtil.dip2px(context, 146):"+DensityUtil.dip2px(mContext, 146)
				//								+"\nDensityUtil.dip2px(context, 42):"+DensityUtil.dip2px(mContext, 42)
				//								+"\nscreenW:"+screenW
				//						);
				//				popupWindow.showAtLocation(v, Gravity.TOP | Gravity.LEFT,
				//						screenW-DensityUtil.dip2px(mContext, 146), location[1]+DensityUtil.dip2px(mContext, 42));
				//
				//				ll_popup_container.startAnimation(sa);

				return;
			}



			//		if ((isPrimaryClick && isCallPrimary) || ((!isPrimaryClick && !isCallPrimary))) {
			//			if (!((isPrimaryClick && isCallPrimary) || ((!isPrimaryClick && !isCallPrimary)))) {//aurora change zhouxiaobing 20130918
			if(id==R.id.main_container||id==R.id.aurora_search_calllog){
				//call
				Log.d(TAG,"onClick2");

				//			if(isHotLine && GNContactsUtils.isMultiSimEnabled()) {
				if(ContactsApplication.isMultiSimEnabled &&showDouble) {
					Log.d(TAG,"onClick2_1");
					if(mOnDialCompleteListener != null) {
						Log.d(TAG,"onClick3");

						mOnDialCompleteListener.onHotLineFill(number);
					}
					return;
				}
				long now = SystemClock.uptimeMillis();
				if (now < mLastCallActionTime + 2000) {
					return;
				}
				mLastCallActionTime = now;
				Log.d("liyang","--3:"+number);
				if (!TextUtils.isEmpty(number)) {
					Log.d(TAG,"onClick4");

					if (ContactsApplication.sIsGnDualSimSelectSupport) {
						Log.d(TAG,"onClick5");

						Intent intent = IntentFactory.newGnDualSimSelectIntent(v, number);
						mContext.startActivity(intent);
					} else {
						Log.d(TAG,"onClick6..");
						Intent intent = IntentFactory.newDialNumberIntent(number);
						intent.putExtra("contactUri", contactUri);
						if(simId != -1) {
							Log.d(TAG,"onClick7.");
							int slot = SIMInfoWrapper.getDefault().getInsertedSimSlotById(simId);
							intent.putExtra(ContactsUtils.getSlotExtraKey(), slot);
						}


						mContext.startActivity(intent);
					}
					if(mOnDialCompleteListener != null) {
						mOnDialCompleteListener.onDialComplete();
					}
				}
			} else {

				Log.d(TAG, "click photo");
				if(strs!=null&&strs.length==6){
					// detail
					int filterType = getCurFilterType();
					int callsType = getCallsType(filterType);

					Intent intent = IntentFactory.newShowCallDetailIntent(mContext,
							number, callsType, callid,
							callscount, voicemailuri,
							ids);

					mContext.startActivity(intent);
				}else if(strs!=null&&strs.length!=6){
					//detail
					if (null != contactUri) {
						Log.d("liyang","strs[1]:"+strs[1]+"\nstrs[2]:"+strs[2]);
						Intent intent = IntentFactory.newViewContactIntent(contactUri);
						intent.putExtra("is_privacy_contact", isPrivacyContact);
						mContext.startActivity(intent);

					}
				}		

			}
		}
		}
	}


	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {

			case R.id.pop_detail://查看详情
			{			
				Log.d("liyang","details");
				if(strs!=null&&strs.length==6){

					number = strs[0];
					type=Integer.parseInt(strs[1]);
					callid=Integer.parseInt(strs[2]);					

					String callids=strs[3];	
					if(callids!=null){
						if(callids.contains(",")){
							String[] idss=callids.split(",");
							ids=new int[idss.length];
							for(int j=0;j<idss.length;j++){
								ids[j]=Integer.parseInt(idss[j]);
							}
						}else{
							ids=new int[1];
							ids[0]=Integer.parseInt(callids);
						}
					}

					callscount=Integer.parseInt(strs[4]);
					if(strs[5]!=null){
						voicemailuri=Uri.parse(strs[5]);
					}

					// detail
					int filterType = getCurFilterType();
					int callsType = getCallsType(filterType);
					Intent intent = IntentFactory.newShowCallDetailIntent(mContext,
							number, callsType, callid,
							callscount, voicemailuri,
							ids);
					mContext.startActivity(intent);
				}

				return;
			}

			case R.id.pop_sendsms://发送短信
			{
				if (null != strs&&strs.length==6) {
					Intent intent = IntentFactory.newCreateSmsIntent(strs[0]);
					try {
						mContext.startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Log.d(TAG, "ActivityNotFoundException for secondaryIntent");
					}
				}

				return;
			}

			case R.id.pop_copy://复制
			{
				if (null != strs&&strs.length==6) {
					//					ClipboardManager clipboard = (ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
					//					clipboard.setPrimaryClip(ClipData.newPlainText(null, strs[0]));

					auroraDialpadFragmentV2.mDigits.setText(strs[0]);
					Toast.makeText(mContext, R.string.toast_text_copied, Toast.LENGTH_SHORT).show();
				}
				return;
			}


			default:
				break;
			}
		}
	};


	protected int getCurFilterType() {
		SharedPreferences prefs = AuroraPreferenceManager
				.getDefaultSharedPreferences(mContext);
		return prefs.getInt(Constants.TYPE_FILTER_PREF,
				Constants.FILTER_TYPE_DEFAULT);
	}

	// gionee xuhz 20120517 modify for CR00601200 CR00601202 start
	protected int getCallsType(int filterType) {
		switch (filterType) {
		case Constants.FILTER_TYPE_ALL:
			return 0;
		case Constants.FILTER_TYPE_OUTGOING:
			return Calls.OUTGOING_TYPE;
		case Constants.FILTER_TYPE_INCOMING:
			return Calls.INCOMING_TYPE;
		case Constants.FILTER_TYPE_MISSED:
			return Calls.MISSED_TYPE;
		default:
			return 0;
		}
	}

	//	public void removeCallLogRecord(final GnContactInfo contactInfo) {
	//		//ArrayList<String> als = getCallLogChecked();
	//		ArrayList<Integer> als = getCallLogCheckedId(contactInfo);
	//		if (als == null || als.size() == 0) {
	//			initActionBar(false);
	//			switch2NormalMode();
	//			return;
	//		}
	//		//		initActionBar(false);
	//		//final String selection = toDeleteSelection(als);
	//		final String selection = toDeleteSelectionId(als);
	//		new AsyncTask<Integer, Integer, Integer>() {
	//			private Context mContext = getActivity();
	//			private AuroraProgressDialog mProgressDialog;
	//
	//			protected void onPreExecute() {
	//				if (getActivity() != null && !getActivity().isFinishing()) {
	//					mProgressDialog = AuroraProgressDialog.show(mContext, "",
	//							getString(R.string.deleting_call_log));
	//				}
	//			};
	//
	//			@Override
	//			protected Integer doInBackground(Integer... params) {
	//				String privateString = "";
	//				if (ContactsApplication.sIsAuroraPrivacySupport) {
	//					privateString = " AND privacy_id > -1 "; 
	//				} 
	//				mContext.getContentResolver().delete(Calls.CONTENT_URI,
	//						selection + privateString , null);
	//				return null;
	//			}
	//
	//			protected void onPostExecute(Integer result) {
	//				if (getActivity() != null && !getActivity().isFinishing()) {
	//					mProgressDialog.dismiss();
	//					if(contactInfo==null){
	//					initActionBar(false);
	//					switch2NormalMode();
	//					}
	//				}
	//			};
	//		}.executeOnExecutor(exec);
	//
	//	}


	//	protected void bindViewAction(final AuroraDialerSearchItemView itemView,
	//			Context context, final String number) {
	//
	//		boolean dialable = (null != number && !number.equals("-1"));
	//		if (dialable || ResConstant.isCallLogListItemCallPrimary()) {
	//			itemView.contactPhoto
	//			.setOnClickListener(this);
	//
	//			itemView.contact_expand_layout
	//			.setOnClickListener(this);
	//		}
	//	}

	private void bindViewDialerSearch(View view, Context context, Cursor cursor) {
		AuroraDialerSearchItemView dsiv = (AuroraDialerSearchItemView) view;
		dsiv.mMainContainer.setVisibility(View.VISIBLE);
		dsiv.mMainContainer.setOnClickListener(this);
		dsiv.mCallLog.setVisibility(View.GONE);

		final String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
		//		Log.d(TAG,"number01:"+number+" ,"+AuroraDialpadFragmentV2.mDigits.getText().toString().replaceAll(" ", ""));
		if(number.equals(auroraDialpadFragmentV2.mDigits.getText().toString().replaceAll(" ", ""))){
			Log.d(TAG,"number1:"+number);
			auroraDialpadFragmentV2.footer_add_contact.setVisibility(View.GONE);
		}/*else{
			Log.d(TAG,"number2:"+number);
			AuroraDialpadFragmentV2.footer_add_contact.setVisibility(View.VISIBLE);
		}*/
		final String formatNumber = mHyphonManager.formatNumber(number);
		int indicate = cursor.getInt(GnDialerSearchResultColumns.INDICATE_PHONE_SIM_INDEX);
		int contactId = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
		Uri contactUri = getContactUri(cursor);

		//		//aurora add liguangyu 20131224 start
		//		String area = NumberAreaUtil.getInstance(mContext).getNumAreaFromAoraLock(mContext, number, false);
		//		
		////		String area = cursor.getString(GnDialerSearchResultColumns.NAME_INDEX);
		//		if(!TextUtils.isEmpty(area)) {
		//			area = area.replace(" ", "");
		//		}else {
		//			area = mContext.getResources().getString(R.string.aurora_unknow_source_calllog);
		//		}	
		//		Log.d(TAG, "bind dial area1:"+area);
		////		dsiv.mArea.setVisibility(!TextUtils.isEmpty(area) ? View.VISIBLE : View.GONE);
		//		dsiv.mArea.setText(area);
		//		dsiv.mArea.setVisibility(View.VISIBLE);
		//		//aurora add liguangyu 20131224 end

		dsiv.mArea.setVisibility(View.GONE);

		if (!TextUtils.isEmpty(formatNumber)) {
			highlightNumber(dsiv.mNumber, formatNumber, cursor);
		} else {
			dsiv.mNumber.setText(null);
		}

		highlightName(dsiv.mName, cursor);

		long is_privacy = cursor.getLong(GnDialerSearchResultColumns.IS_PRIVACY_INDEX);

		dsiv.contactPhoto.setTag(new String[]{formatNumber, contactUri.toString(), String.valueOf(is_privacy)});
		dsiv.contactPhoto.setOnClickListener(this);

		int photoId=cursor.getInt(GnDialerSearchResultColumns.PHOTO_ID_INDEX);
		Log.d("liyang","number:"+number+" photoId:"+photoId);
		if(photoId>0){
			dsiv.contactPhoto.setVisibility(View.VISIBLE);
			dsiv.name_ll.setVisibility(View.VISIBLE);
			dsiv.number_ll.setVisibility(View.VISIBLE);
			mContactPhotoManager.loadPhoto((ImageView)dsiv.contactPhoto, photoId, true,
					false, ContactPhotoManager.DEFAULT_BLANK);
		}else{
			if(photoId==-10){
				dsiv.contactPhoto.setVisibility(View.INVISIBLE);
				dsiv.name_ll.setVisibility(View.GONE);
				dsiv.number_ll.setVisibility(View.VISIBLE);
			}else{
				dsiv.contactPhoto.setVisibility(View.VISIBLE);
				dsiv.name_ll.setVisibility(View.VISIBLE);
				dsiv.number_ll.setVisibility(View.VISIBLE);		

				int index=(int) (contactId%(ResConstant.randomContactPhotoId.length));
				Log.d(TAG,"contactId:"+contactId+" index:"+index);

				if(index<ResConstant.randomContactPhotoId.length){
					((ImageView)dsiv.contactPhoto).setImageDrawable(mContext.getResources().getDrawable(ResConstant.randomContactPhotoId[index]));
				}else{
					((ImageView)dsiv.contactPhoto).setImageDrawable(mContext.getResources().getDrawable(R.drawable.svg_dial_default_photo1));
				}
			}

		}

		//		if (!cursor.isLast()) {
		//			cursor.moveToNext();
		//			int nextContactId = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
		//			cursor.moveToPrevious();        	
		//		}
	}

	private void bindViewHotLines(View view, Context context, Cursor cursor) {
		AuroraDialerSearchItemView dsiv = (AuroraDialerSearchItemView) view;
		dsiv.mMainContainer.setVisibility(View.VISIBLE);
		dsiv.mCallLog.setVisibility(View.GONE);
		dsiv.mMainContainer.setOnClickListener(this);
		final String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
		final String formatNumber = mHyphonManager.formatNumber(number);
		final String name = cursor.getString(GnDialerSearchResultColumns.NAME_INDEX);
		final String photoName = cursor.getString(GnDialerSearchResultColumns.PHOTO_ID_INDEX);
		//aurora add liguangyu 20131224 start
		dsiv.mArea.setVisibility(View.GONE);
		//aurora add liguangyu 20131224 end

		Uri contactUri = getContactUri(cursor);
		long is_privacy = cursor.getLong(GnDialerSearchResultColumns.IS_PRIVACY_INDEX);
		dsiv.contactPhoto.setTag(new String[]{formatNumber, contactUri.toString(), String.valueOf(is_privacy)});

		highlightName(dsiv.mName, cursor);
		highlightNumber(dsiv.mNumber, formatNumber, cursor);

		((ImageView)dsiv.contactPhoto).setImageDrawable(context.getResources().getDrawable(R.drawable.svg_dial_default_photo1));


		//        dsiv.mSecondaryButton.setTag(new String[]{formatNumber});
		//        dsiv.mSecondaryButton.setOnClickListener(this);
	}

	private void highlightNumber(TextView tv, String formatNumber, Cursor cursor) {
		if (!TextUtils.isEmpty(formatNumber)) {
			String numberHighlight = cursor.getString(GnDialerSearchResultColumns.DATA_HIGHLIGHT_INDEX);
			if (null != numberHighlight && numberHighlight.length() == 2) {
				try {
					SpannableStringBuilder numberStyle = new SpannableStringBuilder(
							formatNumber);
					int start = numberHighlight.charAt(0);
					int end = numberHighlight.charAt(1);

					char[] numChars = formatNumber.toCharArray();
					for (int s = 0; s <= start; ++s) {
						if (' ' == numChars[s] || '-' == numChars[s]) {
							++start;
							++end;
						}
					}

					for (int e = start + 1; e <= end && e < numChars.length; ++e) {
						if (' ' == numChars[e] || '-' == numChars[e]) {
							++end;
						}
					}

					if (end > formatNumber.length()) {
						end = formatNumber.length();
					}

					numberStyle.setSpan(new ForegroundColorSpan(mSpanColorBg),
							start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					tv.setText(numberStyle);
					return;
				} catch (ArrayIndexOutOfBoundsException ae) {
					ae.printStackTrace();
				} catch (IndexOutOfBoundsException ie) {
					ie.printStackTrace();
				}
			}

			tv.setText(formatNumber);
		}
	}

	private void highlightName(TextView tv, Cursor cursor) {      
		String name = cursor.getString(GnDialerSearchResultColumns.NAME_INDEX);
		String pinyin = cursor.getString(GnDialerSearchResultColumns.PINYIN_INDEX);

		if (TextUtils.isEmpty(name)) {
			String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
			if (!TextUtils.isEmpty(number)) {
				tv.setText(number);
				setVisibility(tv, View.VISIBLE);
				return;
			}

			tv.setText(null);
			return;
		}

		if (!TextUtils.isEmpty(pinyin)) {
			String pinyinHighlight = cursor.getString(GnDialerSearchResultColumns.PINYIN_HIGHLIGHT_INDEX);
			if (pinyinHighlight != null) {
				SpannableStringBuilder namestyle = new SpannableStringBuilder(name);
				SpannableStringBuilder pinyinstyle = new SpannableStringBuilder(pinyin);
				if (!name.equals(pinyin)) {
					ArrayList<Integer> nameToken = new ArrayList<Integer>();
					int nameIndex = 0;
					int pinyinIndex = 0;
					int pinyinLength = pinyin.length();
					for (int i = 0; i < pinyinLength; i++) {
						char c = pinyin.charAt(i);
						if (0 == i && c != name.charAt(nameIndex)) {
							pinyinIndex++;
							continue;
						} else if (i > 0 && (nameIndex + 1) < name.length() &&
								c != name.charAt(nameIndex + 1) && ('a' <= c && c <= 'z')) {
							pinyinIndex++;
							continue;
						} else if (i > 0 && (nameIndex + 1) == name.length()) {
							if (pinyinIndex < pinyinLength && ('A' <= c && c <= 'Z')) {
								nameToken.add(pinyinIndex);
								pinyinIndex++;
								continue;
							}

							pinyinIndex++;
							if (pinyinIndex == pinyinLength) {
								nameToken.add(pinyinIndex);
								break;
							}
						} else {
							if (pinyinIndex > 0) {
								nameToken.add(pinyinIndex);
							}
							nameIndex++;
							pinyinIndex++;
						}
					}


					if (nameToken.size() <= 0) {
						tv.setText(name);
						setVisibility(tv, View.VISIBLE);
						return;
					}

					if (pinyin.charAt(pinyin.length() - 1) == name.charAt(name.length() - 1)) {
						nameToken.add(nameToken.get(nameToken.size() - 1) + 1);
					}

					//                    for (int i : nameToken) {
					//                        Log.d(TAG, " = " + i);
					//                    }

					for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
						int start = (int) pinyinHighlight.charAt(i);
						int end = (int) pinyinHighlight.charAt(i + 1);
						int startIndex = 0;
						int endIndex = 0;
						//                        Log.d(TAG, "start = " + start + "  end = " + end);
						if (start ==0 && end == 0) {
							break;
						}

						for (int temp = 0; temp < nameToken.size(); temp++) {
							if (start == 0) {
								startIndex = 0;
							} else if (start == nameToken.get(temp)) {
								startIndex = temp + 1;
							}

							if (end > nameToken.get(nameToken.size() - 1)) {
								endIndex = nameToken.size();
							} else if (end == nameToken.get(temp)) {
								endIndex = temp + 1;
							} else if (end > nameToken.get(temp) && end < nameToken.get(temp + 1)) {
								endIndex = temp + 2;
							} else if (end <= nameToken.get(0)) {
								endIndex = 1;
							}
							//                            Log.d(TAG, "startIndex = " + startIndex + "  endIndex = " + endIndex);
						}

						try {
							namestyle.setSpan(new ForegroundColorSpan(mSpanColorBg), startIndex, endIndex,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					tv.setText(namestyle);
					setVisibility(tv, View.VISIBLE);
					return;
				}

				for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
					int start = (int) pinyinHighlight.charAt(i);
					int end = (int) pinyinHighlight.charAt(i + 1);
					pinyinstyle.setSpan(new ForegroundColorSpan(mSpanColorBg), start, end,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				tv.setText(pinyinstyle);
				setVisibility(tv, View.VISIBLE);
				return;
			}
		}

		tv.setText(name);
		setVisibility(tv, View.VISIBLE);
	}

	void setVisibility(View view, int visibility) {
		if (view.getVisibility() != visibility) {
			view.setVisibility(visibility);
		}
	}

	private void bindViewCallLogs(View view, Context context, Cursor cursor) {
		AuroraDialerSearchItemView dsivO = (AuroraDialerSearchItemView) view;
		dsivO.mMainContainer.setVisibility(View.GONE);
		dsivO.mCallLog.setVisibility(View.VISIBLE);
		dsivO.mCallLog.setOnClickListener(this);
		AuroraCallLogListItemViewV2 dsiv = (AuroraCallLogListItemViewV2)view.findViewById(R.id.aurora_search_calllog);    
		final String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
		final String formatNumber = mHyphonManager.formatNumber(number);
		final long date = cursor.getLong(GnDialerSearchResultColumns.PINYIN_INDEX);
//		final long duration = cursor.getLong(GnDialerSearchResultColumns.PINYIN_HIGHLIGHT_INDEX);
		final int type = cursor.getInt(GnDialerSearchResultColumns.PHOTO_ID_INDEX);
		final String name=cursor.getString(GnDialerSearchResultColumns.NAME_INDEX);

		if(TextUtils.isEmpty(name)){
			highlightNumber(dsiv.mName, formatNumber, cursor);
			String area = cursor.getString(GnDialerSearchResultColumns.AREA_INDEX);
			if(!TextUtils.isEmpty(area)) {
				area = area.replace(" ", "");
			}else {
				area = mContext.getResources().getString(R.string.aurora_unknow_source_calllog);
			}
			dsiv.mArea.setText(area);
		}else{
			highlightNumber(dsiv.mArea, formatNumber, cursor);
			dsiv.mName.setText(name);
		}
		
		int callid=cursor.getInt(GnDialerSearchResultColumns.CALL_ID_INDEX);
		int callscount=cursor.getInt(GnDialerSearchResultColumns.CALLS_COUNT_INDEX);
		String callids=cursor.getString(GnDialerSearchResultColumns.CALL_IDS_INDEX);
		String voicemailuri=cursor.getString(GnDialerSearchResultColumns.VOICEMAILURI_INDEX);

		dsiv.mCallCount.setText("("+callscount+")");

		String time=DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_TIME).toString();
		dsiv.mDate.setText(time);

		int callTypeRes = mCallTypeResHolder.getCallTypeDrawable(
				type,
				0,
				false);
		dsiv.mCallType.setImageResource(callTypeRes);

		if(ContactsApplication.isMultiSimEnabled && showDouble) {
			dsiv.mSimIcon.setVisibility(View.VISIBLE);
			//			int iconRes = ContactsUtils.getSimConvertIcon(context, simId);
			final int simId = cursor.getInt(GnDialerSearchResultColumns.INDEX_IN_SIM_INDEX);
			int iconRes=ContactsUtils.getSimIcon(mContext, simId);
			dsiv.mSimIcon.setImageResource(iconRes);
			//	        dsiv.mSimIcon.setAlpha(0.7f);
		} else {
			dsiv.mSimIcon.setVisibility(View.GONE);
		}			

		dsiv.expand.setTag(new String[]{formatNumber,type+"",callid+"",callids,callscount+"",voicemailuri});
		dsiv.expand.setOnClickListener(this);

		dsiv.call_date_ll.setTag(new String[]{formatNumber,type+"",callid+"",callids,callscount+"",voicemailuri});
		dsiv.call_date_ll.setOnClickListener(this);

		dsiv.contactPhoto.setTag(new String[]{formatNumber,type+"",callid+"",callids,callscount+"",voicemailuri});
		dsiv.contactPhoto.setOnClickListener(this);
		((ImageView)dsiv.contactPhoto).setImageDrawable(context.getResources().getDrawable(R.drawable.svg_dial_default_photo1));
		byte[] photo=cursor.getBlob(GnDialerSearchResultColumns.PHOTO_INDEX);
		Log.d(TAG, "photo:"+photo);
		if(photo!=null){
			Bitmap bitmap=BitmapFactory.decodeByteArray(photo,0,photo.length);
			((ImageView)dsiv.contactPhoto).setImageBitmap(bitmap);
		}

	}
	//aurora add liguangyu 20131224 end

	private int mNormalCallColor;
	private final int mMissedCallColor;
	private CallLogResHolder mCallTypeResHolder;
	private long mLastCallActionTime;

	public interface OnDialCompleteListener {
		void onDialComplete();
		//aurora add liguangyu 20140829 for start
		void onHotLineFill(String number);
		//aurora add liguangyu 20140829 for end
	}

	private OnDialCompleteListener mOnDialCompleteListener = null;

	public void setDialCompleteListener(OnDialCompleteListener l) {
		mOnDialCompleteListener = l;
	} 

	private Uri mContactUri;
	public Uri getContactUri() {
		return mContactUri;
	}

	private String mNumber;
	public String getDialNumber() {
		return mNumber;
	}

	public void resetLastClickResult(){
		mNumber = null;
		mContactUri = null;
	}

}