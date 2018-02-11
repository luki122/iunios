/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.calllog;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.PrivateCredentialPermission;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ListFragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import gionee.provider.GnContactsContract.PhoneLookup;
import gionee.provider.GnTelephony.SIMInfo;
import android.telephony.PhoneNumberUtils;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.TextView;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraTextView;
import android.widget.QuickContactBadge;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetailsHelper;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.activities.ContactsLog;
import com.android.contacts.detail.AssociationSimActivity.SimInfoMgr;
import com.android.contacts.dialpad.AnimUtils;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2;
import com.android.contacts.util.Constants;
import com.android.contacts.util.DensityUtil;
import com.android.contacts.util.GnHotLinesUtil;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.calllog.CallLogListItemView;
import com.mediatek.contacts.calllog.CallLogSimInfoHelper;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.OperatorUtils;
import com.mediatek.contacts.widget.QuickContactBadgeWithPhoneNumber;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.content.Intent;
import android.util.DisplayMetrics;

import com.android.contacts.util.GnCallForSelectSim;
import com.android.contacts.widget.IndexerListAdapter.Placement;

/**
 * Adapter class to fill in data for the Call Log.
 */
/**
 * Change Feature by Mediatek Begin. Original Android's Code: (package) class
 * CallLogAdapter extends GroupingListAdapter Descriptions:
 */
public class AuroraCallLogAdapterV2 extends CursorAdapter implements
OnScrollListener {
	/**
	 */
	/** Interface used to initiate a refresh of the content. */
	public interface CallFetcher {
		public void fetchCalls();
	}

	/**
	 * Stores a phone number of a call with the country code where it originally
	 * occurred.
	 * <p>
	 * Note the country does not necessarily specifies the country of the phone
	 * number itself, but it is the country in which the user was in when the
	 * call was placed or received.
	 */
	public static final class NumberWithCountryIso {
		public final String number;
		public final String countryIso;

		public NumberWithCountryIso(String number, String countryIso) {
			this.number = number;
			this.countryIso = countryIso;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (!(o instanceof NumberWithCountryIso))
				return false;
			NumberWithCountryIso other = (NumberWithCountryIso) o;
			return TextUtils.equals(number, other.number)
					&& TextUtils.equals(countryIso, other.countryIso);
		}

		@Override
		public int hashCode() {
			return (number == null ? 0 : number.hashCode())
					^ (countryIso == null ? 0 : countryIso.hashCode());
		}
	}
	private GnContactInfo contactInfo;
	//	private final float move1;
	//	private final float move2;
	private final int preferredHeight ;
	private final Context mContext;
	private final CallFetcher mCallFetcher;

	/**
	 * A cache of the contact details for the phone numbers in the call log.
	 * <p>
	 * The content of the cache is expired (but not purged) whenever the
	 * application comes to the foreground.
	 * <p>
	 * The key is number with the country in which the call was placed or
	 * received.
	 */

	private volatile boolean mDone;
	private boolean mLoading = true;

	/** Helper to parse and process phone numbers. */
	protected PhoneNumberHelper mPhoneNumberHelper;

	/** Can be set to true by tests to disable processing of requests. */
	private volatile boolean mRequestProcessingDisabled = false;

	private Cursor mCursor;

	private SIMInfoWrapper mSIMInfoWrapper;
	private final int mMissedCallColor, mMissedCallColor2;
	private final int mNormalColor, mNormalColor2;

	//aurora add liguangyu 20140327 for BUG #3549 begin
	private ColorStateList mTextNormalColor1;
	private ColorStateList mTextNormalColor2;
	private ColorStateList mTextNormalColor3;
	private ColorStateList mTextNormalColorReject;
	private final int mTextEditColor1;
	private final int mTextEditColor2;
	private final int mTextEditColor3;
	private final int mTextEditColorReject;
	//aurora add liguangyu 20140327 for BUG #3549 end

	private ColorStateList mNormalCallColor;
	// gionee xuhz add for call log number/number area color start
	private ColorStateList mNumberColor;
	// gionee xuhz add for call log number/number area color end
	// aurora change zhouxiaobing 20130912 start
	private boolean is_editor_mode = false;
	private boolean[] checkeds;
	private boolean is_all_checked = false;
	private boolean is_listitem_changing = false;
	//aurora change liguangyu 20131108 for BUG #419 start
	int positons[];
	//aurora change liguangyu 20131108 for BUG #419 end
	private int counts[];
	private int hotnumberIndexs[];
	private int CursorIndex;
	private AuroraCallLogListItemViewV2 itemviewv2;

	private String hotnames[];
	private String hotnumbers[];

	private final int LISTITEM_CHANGING = 0;
	private final int Check_Anim = 1;


	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case LISTITEM_CHANGING:
				Log.d(TAG,"position1:"+" LISTITEM_CHANGING");
				is_listitem_changing = false;
				super.handleMessage(msg);
				break;
			case Check_Anim:
				setAllSelectFlag(false);
				break;
			}
		}

	};
	// aurora change zhouxiaobing 20130912 end

	private OnItemClickListener mOnItemClickListener;
	private LinearLayout.LayoutParams deviderLayoutParams;
	private OnItemLongClickListener mOnItemLongClickListener;

	ContactPhotoManager contactPhotoManager;


	//	boolean showDouble = false;
	public List<Long> simInfoId; 
	public AuroraCallLogAdapterV2(Context context, CallFetcher callFetcher) {
		super(context, null, false);
		mContext = context;
		mCallFetcher = callFetcher;

		Resources resources = mContext.getResources();
		//		move1 = resources.getDimension(R.dimen.aurora_call_log_animate_move_length_1);
		//		move2 = resources.getDimension(R.dimen.aurora_call_log_animate_move_length_2);
		preferredHeight = resources.getDimensionPixelOffset(R.dimen.aurora_two_lines_list_view_item_height);
		CallTypeHelper callTypeHelper = new CallTypeHelper(resources);

		mPhoneNumberHelper = new PhoneNumberHelper(resources);
		/**
		 * Change Feature by Mediatek Begin. Original Android's Code:
		 * PhoneCallDetailsHelper phoneCallDetailsHelper = new
		 * PhoneCallDetailsHelper( resources, callTypeHelper,
		 * mPhoneNumberHelper); mCallLogViewsHelper = new CallLogListItemHelper(
		 * phoneCallDetailsHelper, mPhoneNumberHelper, resources); Descriptions:
		 */
		mCallLogSimInfoHelper = new CallLogSimInfoHelper(resources);
		mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(resources,
				callTypeHelper, mPhoneNumberHelper, mCallLogSimInfoHelper,
				mContext);
		/**
		 * Change Feature by Mediatek End.
		 */

		if (null == mContactInfoMap) {
			mContactInfoMap = new HashMap<String, GnContactInfo>();
		} else {
			mContactInfoMap.clear();
		}

		mSIMInfoWrapper = SIMInfoWrapper.getDefault();

		mMissedCallColor = mContext.getResources().getColor(
				R.color.gn_misscall_color);
		mMissedCallColor2 = mContext.getResources().getColor(
				R.color.aurora_calllog_missed_color2);		


		mNormalColor = mContext.getResources().getColor(
				R.color.name_textview_color);
		mNormalColor2 = mContext.getResources().getColor(
				R.color.number_textview_color); 



		mCallTypeResHolder = new CallLogResHolder(mContext);

		//aurora add liguangyu 20140327 for BUG #3549 begin
		mTextEditColor1 = mContext.getResources().getColor(R.color.aurora_calllog_list_text_color_edit_1);
		mTextEditColor2 = mContext.getResources().getColor(R.color.aurora_calllog_list_text_color_edit_2);
		mTextEditColor3 = mContext.getResources().getColor(R.color.aurora_calllog_list_text_color_edit_3);
		mTextEditColorReject = mContext.getResources().getColor(R.color.aurora_calllog_list_text_color_edit_reject);
		//aurora add liguangyu 20140327 for BUG #3549 end

		contactPhotoManager = ContactPhotoManager.getInstance(context);
		simInfoId=new ArrayList<Long>();
		List<SIMInfo> simInfo=mSIMInfoWrapper.getInsertedSimInfoList();
		for(SIMInfo info:simInfo){
			Log.d(TAG,"info.simId:"+info.mSimId+" info.simslot:"+info.mSlot);
			simInfoId.add(info.mSimId);
		}

		MAX_LENGTH=DensityUtil.dip2px(mContext, 180);

		deviderLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
		deviderLayoutParams.setMargins(mContext.getResources().getDimensionPixelOffset(R.dimen.double_list_item_photo_width),0,0,0);

		distance=DensityUtil.dip2px(mContext,40);
		nameWidth1=DensityUtil.dip2px(mContext,180);
		nameWidth2=mContext.getResources().getDimensionPixelOffset(R.dimen.listitem_double_name_width);


	}

	/**
	 * Requery on background thread when {@link Cursor} changes.
	 */
	@Override
	protected void onContentChanged() {
		mCallFetcher.fetchCalls();
	}

	public void setLoading(boolean loading) {
		mLoading = loading;
	}	


	public void setEditMode(boolean is_edit) {
		is_editor_mode = is_edit;
	}

	public void setAllSelect(boolean all) {
		for (int i = 0; i < checkeds.length; i++)
			checkeds[i] = all;
	}

	//aurora add liguangyu 20140331 for bug 3948 start
	boolean isCheckAnim = false;
	public void setAllSelectFlag(boolean value) {
		isCheckAnim = value;
	}
	//aurora add liguangyu 20140331 for bug 3948 end

	public void createCheckedArray(int count) {
		if (checkeds == null || count != checkeds.length)
			checkeds = new boolean[count];
		for (int i = 0; i < count; i++)
			checkeds[i] = false;
	}

	public boolean isIs_listitem_changing() {
		return is_listitem_changing;
	}

	public void setIs_listitem_changing(boolean is_listitem_changing) {
		this.is_listitem_changing = is_listitem_changing;
		Log.d(TAG,"setIs_listitem_changing:"+is_listitem_changing);
	}

	public void setCheckedArrayValue(int position, boolean value) {
		checkeds[position] = value;
	}

	public boolean getCheckedArrayValue(int position) {
		return checkeds[position];
	}

	public boolean isAllSelect() {
		for (int i = 0; i < checkeds.length; i++) {
			if (!checkeds[i])
				return false;
		}
		return true;
	}

	public int getCheckedCount() {
		if (getCount() == 0) {
			return 0;
		}
		int mChecked = 0;
		for (int i = 0; i < checkeds.length; i++) {
			if (checkeds[i]) {
				mChecked++;
			}
		}
		return mChecked;
	}

	public void clearAllcheckes() {
		for (int i = 0; i < checkeds.length; i++)
			checkeds[i] = false;
	}

	public int[] getHotlineIndex(int count) {
		if(count==0)
			return null;
		hotnumberIndexs=new int[count];
		return hotnumberIndexs;
	}

	public void creatHotlinesNumber(int count) {
		if (hotnames==null) {
			hotnames=new String[count];
			hotnumbers=new String[count];
		}
	}

	public String[] getHotlinesName() {
		return hotnames;
	}

	public String[] getHotlinesNumber() {
		return hotnumbers;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (mDataValid && mCursor != null) {
			mCursor.moveToPosition(position);
			return mCursor;
		} else {
			return null;
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mCursor == null) {
			return 0;
		} else {			
			return mCursor.getCount();
		}
	}

	public int getRealCount() {
		if (mCursor == null) {
			return 0;
		} else {			
			return mCursor.getCount();
		}
	}

	public static int startPosition;
	int distance,nameWidth1,nameWidth2;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG,"getView is_listitem_changing:"+is_listitem_changing+" startPosition:"+startPosition+" position:"+position+" convertView:"+convertView
				+" is_editor_mode:"+is_editor_mode);
		// TODO Auto-generated method stub
		if (!mCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position "
					+ positons[position]);
		}

		CursorIndex=position;
		View v;

		if (convertView == null) {

			v = newView(mContext, mCursor, parent);
			Log.d(TAG,"convertView-= null,position:"+position+" v:"+v);
		} else {
			Log.d(TAG,"convertView-!= null,position:"+position+"convertView:"+convertView);
			v = convertView;
		}

		bindView(v, mContext, mCursor);

		itemviewv2.mCheckBox.setChecked(checkeds[position]);

		if(is_listitem_changing && is_editor_mode) {
			Log.d(TAG,"position1:"+position+" is_listitem_changing && is_editor_mode");
			//			AuroraListView.auroraStartCheckBoxAppearingAnim(itemviewv2.call_date_ll, itemviewv2.mCheckBox);		

			AnimUtils.move(itemviewv2.call_date_ll,500,0,AnimUtils.CURVE_SHOW,true,0,-distance,moveAnimatorListenerAdapter);

			LayoutParams p=itemviewv2.mName.getLayoutParams();
			p.width = nameWidth1;
			itemviewv2.mName.setLayoutParams(p); 

			AuroraListView.auroraSetCheckBoxVisible(itemviewv2.call_date_ll, itemviewv2.mCheckBox, true);
			itemviewv2.expand.setVisibility(View.INVISIBLE);


		}else if (is_listitem_changing&&!is_editor_mode) {
			Log.d(TAG,"position1:"+position+" is_listitem_changing&&!is_editor_mode");

			AnimUtils.move(itemviewv2.call_date_ll,500,0,AnimUtils.CURVE_SHOW,true,-distance,0,restoreMoveAnimatorListenerAdapter);

			LayoutParams p=itemviewv2.mName.getLayoutParams();
			p.width = nameWidth2;
			itemviewv2.mName.setLayoutParams(p);

			itemviewv2.expand.setVisibility(View.VISIBLE);
			//			AuroraListView.auroraStartCheckBoxDisappearingAnim(itemviewv2.call_date_ll, itemviewv2.mCheckBox);
			AuroraListView.auroraSetCheckBoxVisible(itemviewv2.call_date_ll, itemviewv2.mCheckBox, false);
		} 

		if(is_editor_mode&&itemviewv2.mCheckBox.getVisibility()==View.GONE){
			Log.d(TAG,"position1:"+position+" mCheckBox gone");
			LayoutParams p=itemviewv2.mName.getLayoutParams();
			p.width = nameWidth1;
			itemviewv2.mName.setLayoutParams(p); 

			//			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
			//					RelativeLayout.LayoutParams.WRAP_CONTENT);
			//			Log.d(TAG,"itemviewv2.firstLineWidth:"+itemviewv2.firstLine.getWidth()+" itemviewv2.dateViewWidth:"+itemviewv2.call_date_ll.getWidth()+" distance:"+distance);
			//			Log.d(TAG,"width:"+(itemviewv2.firstLine.getWidth()-itemviewv2.mDate.getWidth()-distance));
			//			layoutParams.setMargins(itemviewv2.firstLine.getWidth()-itemviewv2.call_date_ll.getWidth()-distance,0,distance,0);
			//			itemviewv2.call_date_ll.setLayoutParams(layoutParams);

			AnimUtils.move(itemviewv2.call_date_ll,0,0,AnimUtils.CURVE_SHOW,true,0,-distance,moveAnimatorListenerAdapter);

			AuroraListView.auroraSetCheckBoxVisible(itemviewv2.call_date_ll, itemviewv2.mCheckBox, true);
			itemviewv2.expand.setVisibility(View.INVISIBLE);
		}else if(!is_editor_mode&&itemviewv2.mCheckBox.getVisibility()==View.VISIBLE){
			Log.d(TAG,"position1:"+position+" mCheckBox visible");
			AnimUtils.move(itemviewv2.call_date_ll,0,0,AnimUtils.CURVE_SHOW,true,-distance,0,restoreMoveAnimatorListenerAdapter);

			LayoutParams p=itemviewv2.mName.getLayoutParams();
			p.width = nameWidth2;
			itemviewv2.mName.setLayoutParams(p);

			itemviewv2.expand.setVisibility(View.VISIBLE);
			//			AuroraListView.auroraStartCheckBoxDisappearingAnim(itemviewv2.call_date_ll, itemviewv2.mCheckBox);
			AuroraListView.auroraSetCheckBoxVisible(itemviewv2.call_date_ll, itemviewv2.mCheckBox, false);
		}

		Log.d(TAG, "count:"+mCursor.getCount());

		//		if(is_listitem_changing&&mCursor.getCount()<11){
		//			Log.d(TAG, "count<11:"+mCursor.getCount());
		//			handler.sendMessage(handler.obtainMessage(LISTITEM_CHANGING));
		//		}else if(is_listitem_changing&&mCursor.getCount()>=11&&position>startPosition+4){
		//			handler.sendMessage(handler.obtainMessage(LISTITEM_CHANGING));
		//		}

		if(is_listitem_changing){
			Log.d(TAG, "sendMessage");
			handler.sendMessage(handler.obtainMessage(LISTITEM_CHANGING));
		}

		return v;
	}

	final AnimatorListenerAdapter moveAnimatorListenerAdapter = new AnimatorListenerAdapter() {

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			super.onAnimationEnd(animation);
			//			itemviewv2.expand.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub
			super.onAnimationStart(animation);
		}		

	};

	final AnimatorListenerAdapter restoreMoveAnimatorListenerAdapter = new AnimatorListenerAdapter() {

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			super.onAnimationEnd(animation);

		}

		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub
			super.onAnimationStart(animation);
			//			itemviewv2.expand.setVisibility(View.VISIBLE);
		}		

	};

	// aurora change zhouxiaobing 20130912 end
	@Override
	public boolean isEmpty() {
		if (mLoading) {
			// We don't want the empty state to show when loading.
			return false;
		} else {
			return super.isEmpty();
		}
	}

	/**
	 * Checks whether the contact info from the call log matches the one from
	 * the contacts db.
	 */
	protected boolean callLogInfoMatches(GnContactInfo callLogInfo,
			GnContactInfo info) {
		// The call log only contains a subset of the fields in the contacts db.
		// Only check those.
		return TextUtils.equals(callLogInfo.name, info.name)
				&& callLogInfo.type == info.type
				&& TextUtils.equals(callLogInfo.label, info.label);
	}

	protected GnContactInfo getContactInfo(Cursor c) {
//		Log.d(TAG, "getContactInfo:"+c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_NUMBER));
		GnContactInfo info = null;
		String hashKey = c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_NUMBER)
				+ c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_DATE);
		Log.d(TAG, "getContactInfo:"+c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_NUMBER)+" hashKey:"+hashKey);
		info = mContactInfoMap.get(hashKey);
		Log.d(TAG,"info:"+info);
		if (null == info) {

			info = getContactInfoFromCallLog(c);

			mContactInfoMap.put(hashKey, info);
		}

		return info;
	}


	/** Returns the contact information as stored in the call log. */
	protected GnContactInfo getContactInfoFromCallLog(Cursor c) {
		GnContactInfo info = GnContactInfo.fromCursor(c,mContext);
		return info;
	}

	/**
	 * Returns the call types for the given number of items in the cursor.
	 * <p>
	 * It uses the next {@code count} rows in the cursor to extract the types.
	 * <p>
	 * It position in the cursor is unchanged by this function.
	 */

	/*
	 * Get the number from the Contacts, if available, since sometimes the
	 * number provided by caller id may not be formatted properly depending on
	 * the carrier (roaming) in use at the time of the incoming call. Logic : If
	 * the caller-id number starts with a "+", use it Else if the number in the
	 * contacts starts with a "+", use that one Else if the number in the
	 * contacts is longer, use that one
	 */
	public String getBetterNumberFromContacts(String number, String countryIso) {
		String matchingNumber = null;
		// Look in the cache first. If it's not found then query the Phones db
		NumberWithCountryIso numberCountryIso = new NumberWithCountryIso(
				number, countryIso);
		GnContactInfo ci = null;
		if (ci != null && ci != GnContactInfo.EMPTY) {
			matchingNumber = ci.number;
		} else {
			try {
				Cursor phonesCursor = mContext.getContentResolver().query(
						Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
								number), PhoneQuery._PROJECTION, null, null,
								null);
				if (phonesCursor != null) {
					if (phonesCursor.moveToFirst()) {
						matchingNumber = phonesCursor
								.getString(PhoneQuery.MATCHED_NUMBER);
					}
					phonesCursor.close();
					phonesCursor = null;
				}
			} catch (Exception e) {
				// Use the number from the call log
			}
		}
		if (!TextUtils.isEmpty(matchingNumber)
				&& (matchingNumber.startsWith("+") || matchingNumber.length() > number
						.length())) {
			number = matchingNumber;
		}
		return number;
	}

	// The following lines are provided and maintained by Mediatek Inc.
	protected final PhoneCallDetailsHelper mPhoneCallDetailsHelper;
	private final CallLogSimInfoHelper mCallLogSimInfoHelper;
	private HashMap<String, GnContactInfo> mContactInfoMap = null;

	private static final String TAG = "liyang-AuroraCallLogAdapterV2";
	private void log(final String log) {
		Log.i(TAG, log);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub		  

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//		// TODO Auto-generated method stub
		//		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
		//			if (view.getLastVisiblePosition() == (view.getCount() - 1) && mIsSlideDownToShowMore) {
		//				if(positons.length >= mCount + DEFAULT_LISTVIE_COUNT_MORE) {
		//					mCount += DEFAULT_LISTVIE_COUNT_MORE;              
		//				} else {
		//					mCount = positons.length;
		//				}
		//				notifyDataSetChanged();
		//			}
		//		} 
	}

	public void clearCachedContactInfo() {
		mContactInfoMap.clear();
	}

	/** Returns the description used by the call action for this phone call. */
	private CharSequence getCallActionDescription(PhoneCallDetails details) {
		final CharSequence recipient;
		if (!TextUtils.isEmpty(details.name)) {
			recipient = details.name;
		} else {
			recipient = mPhoneNumberHelper.getDisplayNumber(details.number,
					details.formattedNumber);
		}
		return mContext.getResources().getString(R.string.description_call,
				recipient);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		log("changeCursor(), cursor = " + cursor+" count:"+((cursor==null)?"null":cursor.getCount()));
		if (mCursor != cursor) {
			mCursor = cursor;
			mContactInfoMap.clear();
		}

		if(cursor == null || cursor.getCount() == 0) {
			setIs_listitem_changing(false);
		}

		super.changeCursor(cursor);
	}

	// The previous lines are provided and maintained by Mediatek Inc.

	@Override
	public View newView(Context mContext, Cursor cursor, ViewGroup parent) {
		Log.d(TAG, "newview");
		AuroraCallLogListItemViewV2 cliv = AuroraCallLogListItemViewV2
				.create(mContext);

		View v = (View) LayoutInflater.from(mContext).inflate(
				com.aurora.R.layout.aurora_slid_listview, null);
		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_listview_custom_front));
		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_item_sliding_switch));

		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_custom_action_bar_left_panel));
		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.control_padding));

		//		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_listview_back));

		RelativeLayout mainUi = (RelativeLayout) v
				.findViewById(com.aurora.R.id.aurora_listview_front);
		mainUi.addView(cliv, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		ImageView iv = (ImageView) v.findViewById(com.aurora.R.id.aurora_listview_divider);
		((LinearLayout)iv.getParent()).setBackgroundColor(mContext.getResources().getColor(R.color.listview_item_color));
		iv.setLayoutParams(deviderLayoutParams);
		cliv.dividerLine=iv;

		View paddingView=v.findViewById(com.aurora.R.id.control_padding);
		paddingView.setPadding(0, 0, 0, 0);

		cliv.mCheckBox=(CheckBox) v.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		cliv.headerUi=(LinearLayout)v.findViewById(com.aurora.R.id.aurora_list_header);

		ItemHolder itemHolder = new ItemHolder();
		itemHolder.itemviewv2 = cliv;
		//		itemHolder.auroraChildContent = (LinearLayout) v.findViewById(com.aurora.R.id.content);
		v.setTag(itemHolder);
		return v;
	}

	private class ItemHolder{
		public AuroraCallLogListItemViewV2 itemviewv2;
		//		public LinearLayout auroraChildContent;
	}



	private int MAX_LENGTH;
	public String filterLength(TextView view,CharSequence source){
		TextPaint paint = view.getPaint();
		for (int index = 0; index < source.length(); index++) {
			if(Layout.getDesiredWidth(source, 0, index, paint)>MAX_LENGTH){
				return source.toString().substring(0, index);
			}
		}
		return source.toString();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ItemHolder itemHolder = (ItemHolder) view.getTag();

		itemviewv2 = itemHolder.itemviewv2;
		contactInfo=getContactInfo(cursor);

		//begin
		if(!contactInfo.isInitDisplay){
			contactInfo.isInitDisplay = true;
			// New items also use the highlighted version of the text.
			//			final boolean isHighlighted = contactInfo.isRead == 0;
			final boolean isEmergencyNumber = mPhoneNumberHelper
					.isEmergencyNumber(contactInfo.number);
			final boolean isVoiceMailNumber = mPhoneNumberHelper
					.isVoiceMailNumberForMtk(contactInfo.number, contactInfo.simId);
			// final boolean isSipCallNumber =
			// PhoneNumberHelper.isSipNumber(contactInfo.number);

			if (isEmergencyNumber || isVoiceMailNumber) {
				if (TextUtils.isEmpty(contactInfo.name)) {
					contactInfo.photoId = 0;
					contactInfo.lookupUri = null;
				}
			} 

			/*			else if (contactInfo.contactSimId > 0) {
				int slotId = SIMInfoWrapper.getDefault().getSlotIdBySimId(
						contactInfo.contactSimId);

				// Gionee <xuhz> <2013-08-16> modify for CR00858149 begin
				// old:if (OperatorUtils.getOptrProperties().equals("OP02")) {
				if (OperatorUtils.getActualOptrProperties().equals("OP02")) {
					// Gionee <xuhz> <2013-08-16> modify for CR00858149 end
					contactInfo.photoId = (slotId == 0) ? -3 : -4;
				} else if (ContactsUtils.mIsGnContactsSupport
						&& (ContactsUtils.mIsGnShowSlotSupport || ContactsUtils.mIsGnShowDigitalSlotSupport)) {
					if (FeatureOption.MTK_GEMINI_SUPPORT) {
						if (slotId == 0) {
							contactInfo.photoId = -3;
						} else if (slotId == 1) {
							contactInfo.photoId = -4;
						}
					} else {
						if (slotId == 0) {
							contactInfo.photoId = -1;
						}
					}
				} else {
					contactInfo.photoId = SimCardUtils.isSimUsimType(slotId) ? -2
							: -1;
				}
			}*/

			Log.d(TAG, "contactInfo.name:"+contactInfo.name+" contactInfo.callName:"+contactInfo.callName);

			contactInfo.simIcon = ContactsUtils.getSimIcon(mContext, contactInfo.simId);
//			if(TextUtils.isEmpty(contactInfo.name)&&!TextUtils.isEmpty(contactInfo.callName)){
//				contactInfo.name=contactInfo.callName;
//			}

			if(!TextUtils.isEmpty(contactInfo.name)){
				if (null != contactInfo.number) {
					if (contactInfo.number.startsWith("sip:")) {
						contactInfo.number = contactInfo.number.substring(4);
					}
					contactInfo.displayArea=mPhoneNumberHelper.getDisplayNumber(
							contactInfo.number, contactInfo.formattedNumber)
							.toString();
				}

				if(contactInfo.displayArea.length()>17) contactInfo.displayArea=contactInfo.displayArea.substring(0,17);
			}else {
				contactInfo.photoId = 0;
				contactInfo.hideNumber = true;
				if (null != contactInfo.number) {
					if (contactInfo.number.startsWith("sip:")) {
						contactInfo.number = contactInfo.number.substring(4);
					}
					contactInfo.name = mPhoneNumberHelper.getDisplayNumber(
							contactInfo.number, contactInfo.formattedNumber)
							.toString();				
				}

				if (ContactsApplication.sIsGnAreoNumAreaSupport) {
					if(!TextUtils.isEmpty(contactInfo.area)) {
						contactInfo.displayArea = contactInfo.area.replace(" ", "");

						if(contactInfo.displayArea.length()>12) contactInfo.displayArea=contactInfo.displayArea.substring(0,12);
						//								if (ContactsApplication.sIsHotLinesSupport && hotnumbers != null) {
						//									Log.d(TAG, "sIsHotLinesSupport1");
						//									if(hotnumberIndexs[CursorIndex]>-1) {
						//										
						//										String hotLineArea = hotnames[hotnumberIndexs[CursorIndex]];
						//										Log.d(TAG, "sIsHotLinesSupport1:"+hotLineArea);
						//										if(hotLineArea.equalsIgnoreCase(contactInfo.area)) {
						//											contactInfo.displayArea = mContext.getResources().getString(R.string.aurora_service_number);
						//										}
						//									}
						//								}

					} else {
						contactInfo.displayArea = mContext.getResources().getString(R.string.aurora_unknow_source_calllog);
					}			

				}

			}

			contactInfo.callTypeRes = mCallTypeResHolder.getCallTypeDrawable(
					contactInfo.type,
					mSIMInfoWrapper.getSlotIdBySimId(contactInfo.simId),
					contactInfo.vtCall == 1,
					contactInfo.duration);

			//			if (contactInfo.type == Calls.MISSED_TYPE) {
			//				if (!TextUtils.isEmpty(contactInfo.displayArea)) {
			//					contactInfo.displayDuration = mCallTypeResHolder.getCallDurationText(
			//							contactInfo.type, contactInfo.duration)+"，";
			//				} else {
			//					contactInfo.displayDuration = mCallTypeResHolder.getCallDurationText(
			//							contactInfo.type, contactInfo.duration);
			//				}	
			//			}		


			contactInfo.dateString=HyphonManager.getInstance().formatDateTime(contactInfo.date);
			contactInfo.timeString=DateUtils.formatDateTime(context, contactInfo.date, DateUtils.FORMAT_SHOW_TIME).toString();

			mCursor.moveToPrevious();
			if(CursorIndex>0){
				contactInfo.preDateString=HyphonManager.getInstance().formatDateTime(mCursor.getLong(CallLogQuery.CALLS_JOIN_DATA_VIEW_DATE));
			}
			mCursor.move(2);

			if(CursorIndex<mCursor.getCount()-1){
				contactInfo.nextDateString=HyphonManager.getInstance().formatDateTime(mCursor.getLong(CallLogQuery.CALLS_JOIN_DATA_VIEW_DATE));
			}
			mCursor.moveToPrevious();
		}
		//end

		mCountText = (contactInfo.gnCallsCount > 1) ? "(" + contactInfo.gnCallsCount+ ")" : "";
		Log.d(TAG, "bindview,number:"+contactInfo.number
				+"\ngnCallsCount:"+contactInfo.gnCallsCount
				+"\nsimId:"+contactInfo.simId
				+"\nname:"+contactInfo.number
				+"\ndisplayarea:"+contactInfo.displayArea);

		itemviewv2.contactPhoto.setTag(contactInfo);
		itemviewv2.call_date_ll.setTag(contactInfo);
		itemviewv2.expand.setTag(contactInfo);
		//		itemviewv2.mCheckBox.setTag(contactInfo);

		if(ContactsApplication.isMultiSimEnabled&&ContactsApplication.isShowDouble) {
			if(simInfoId.contains(Long.parseLong(String.valueOf(contactInfo.simId)))){
				Log.d(TAG, "showsimIcon");
				itemviewv2.mSimIcon.setVisibility(View.VISIBLE);
				itemviewv2.mSimIcon.setImageResource(contactInfo.simIcon);
			}else{
				itemviewv2.mSimIcon.setVisibility(View.GONE);
			}


		} else {
			itemviewv2.mSimIcon.setVisibility(View.GONE);
		}

		if(!TextUtils.isEmpty(contactInfo.mark) && contactInfo.lookupUri == null && ContactsApplication.sIsAuroraRejectSupport) {  
			itemviewv2.mReject.setText(contactInfo.mark);
			itemviewv2.mReject.setVisibility(View.VISIBLE);
		} else {
			itemviewv2.mReject.setVisibility(View.GONE);
		}		

		itemviewv2.mName.setText(contactInfo.name);
		itemviewv2.mCallCount.setText(mCountText);
		itemviewv2.mCallCount.setVisibility(TextUtils.isEmpty(mCountText)? View.GONE : View.VISIBLE);
		itemviewv2.mArea.setText(contactInfo.displayArea);

		//liyang add:按日期分组
		auroraBindSectionHeaderAndDivider(view, contactInfo.dateString, contactInfo.preDateString,cursor);


		if(CursorIndex!=mCursor.getCount()-1){

			if (!contactInfo.nextDateString.equals(contactInfo.dateString)) {  
				itemviewv2.dividerLine.setVisibility(View.GONE);
			} else {  
				itemviewv2.dividerLine.setVisibility(View.VISIBLE);
			}  

		}else{
			itemviewv2.dividerLine.setVisibility(View.GONE);
		}



		itemviewv2.mDate.setText(contactInfo.timeString);
		//		if (null != itemviewv2.mDuration) {
		//			if (contactInfo.type == Calls.MISSED_TYPE) {
		//				itemviewv2.mDuration.setText(contactInfo.displayDuration);
		//			}
		//		}

		itemviewv2.mCallType.setImageResource(contactInfo.callTypeRes);

		itemviewv2.setClickable(false);
		if (contactInfo.type == Calls.MISSED_TYPE) {
			itemviewv2.mName.setTextColor(mMissedCallColor);
			itemviewv2.mArea.setTextColor(mMissedCallColor2);
			itemviewv2.mCallCount.setTextColor(mMissedCallColor);

		} else {
			itemviewv2.mName.setTextColor(mNormalColor);
			itemviewv2.mArea.setTextColor(mNormalColor2);
			itemviewv2.mCallCount.setTextColor(mNormalColor2);
		}

		itemviewv2.mArea.setVisibility(View.VISIBLE);
		bindViewAction(itemviewv2, context, contactInfo, cursor);

		if(contactInfo.photoUri!=null){
			contactPhotoManager.loadPhoto((ImageView)itemviewv2.contactPhoto, contactInfo.photoUri, true,
					false, ContactPhotoManager.DEFAULT_BLANK);
		}else{
			((ImageView)itemviewv2.contactPhoto).setImageDrawable(contactInfo.drawable);
			/*
			int index=(int) (contactInfo.contactId%(ResConstant.randomContactPhotoId.length));
			if(index<ResConstant.randomContactPhotoId.length&&contactInfo.contactId!=0){
				((ImageView)itemviewv2.contactPhoto).setImageDrawable(mContext.getResources().getDrawable(ResConstant.randomContactPhotoId[index]));
			}else{
				if(contactInfo.callPhoto==null){
					((ImageView)itemviewv2.contactPhoto).setImageDrawable(mContext.getResources().getDrawable(R.drawable.svg_dial_default_photo1));
				}else{
					Bitmap bitmap=BitmapFactory.decodeByteArray(contactInfo.callPhoto,0,contactInfo.callPhoto.length);
					((ImageView)itemviewv2.contactPhoto).setImageBitmap(bitmap);
//					bitmap.recycle();
				}
			}
		*/
			}
	}


	protected void auroraBindSectionHeaderAndDivider(View view, String date,String preDateString,
			Cursor mCursor) {

		if (CursorIndex == 0) {  
			LinearLayout headerUi = itemviewv2.headerUi;
			if (headerUi != null) {
				ViewGroup.LayoutParams params = headerUi.getLayoutParams();
				params.height = mContext.getResources().getDimensionPixelSize(
						R.dimen.aurora_edit_group_margin_top);
				AuroraTextView tv = new AuroraTextView(mContext);
				tv.setText(date);
				int paddingLeft = mContext.getResources()
						.getDimensionPixelSize(
								R.dimen.aurora_group_entrance_left_margin);
				tv.setTextAppearance(mContext, R.style.aurora_list_header_style);
				tv.setHeight(params.height);
				tv.setPadding(paddingLeft, 0, 0, 0);
				tv.setGravity(Gravity.CENTER_VERTICAL);
				//				tv.setTextColor(0x3C414142);

				headerUi.setBackgroundColor(mContext.getResources().getColor(R.color.contact_list_header_background_color));
				headerUi.removeAllViews();
				headerUi.setEnabled(false);
				headerUi.setClickable(false);
				headerUi.addView(tv);
				headerUi.setLayoutParams(params);					
				headerUi.setVisibility(View.VISIBLE);
			}

		} else { 				
			LinearLayout headerUi = itemviewv2.headerUi;
			if (!preDateString.equals(date)) {  
				if (headerUi != null) {
					ViewGroup.LayoutParams params = headerUi.getLayoutParams();
					params.height = mContext.getResources().getDimensionPixelSize(
							R.dimen.aurora_edit_group_margin_top);
					AuroraTextView tv = new AuroraTextView(mContext);
					tv.setText(date);
					int paddingLeft = mContext.getResources()
							.getDimensionPixelSize(
									R.dimen.aurora_group_entrance_left_margin);
					tv.setTextAppearance(mContext, R.style.aurora_list_header_style);
					tv.setHeight(params.height);
					tv.setPadding(paddingLeft, 0, 0, 0);
					tv.setGravity(Gravity.CENTER_VERTICAL);
					//					tv.setTextColor(0x3C414142);

					headerUi.setBackgroundColor(mContext.getResources().getColor(R.color.contact_list_header_background_color));
					headerUi.removeAllViews();
					headerUi.setEnabled(false);
					headerUi.setClickable(false);
					headerUi.addView(tv);
					headerUi.setLayoutParams(params);					
					headerUi.setVisibility(View.VISIBLE);
				}else{
					//					headerUi.setVisibility(View.GONE);		
				}
			} else {  
				if(headerUi!=null){
					headerUi.setVisibility(View.GONE);
				}
			}  
		}
	}



	private CallLogResHolder mCallTypeResHolder;

	public boolean refreshSimColor() {
		if (null == mCallTypeResHolder) {
			return false;
		}

		mCallTypeResHolder.refreshSimColor();
		return true;
	}

	protected void bindViewAction(final AuroraCallLogListItemViewV2 itemView,
			Context context, final GnContactInfo contactInfo, Cursor cursor) {
		final String number = contactInfo.number;

		boolean dialable = (null != number && !number.equals("-1"));
		//		itemView.mSecondaryButton.setEnabled(dialable);
		if (dialable || ResConstant.isCallLogListItemCallPrimary()) {
			itemView.contactPhoto
			.setOnClickListener(mSecondaryButtonClick);

			//liyang add
			itemView.call_date_ll
			.setOnClickListener(mSecondaryButtonClick);
			itemView.expand
			.setOnClickListener(mSecondaryButtonClick);

			//			itemView.mCheckBox
			//			.setOnClickListener(mSecondaryButtonClick);

		}
	}

	private OnClickListener mSecondaryButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (null != mOnItemClickListener) {
				mOnItemClickListener.onItemClick(null, v,
						-1, -1);
			}
		}
	};


	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}


	// custom number maybe modify for iuni
	private String[] mGioneeNumbers;
	private boolean isGioneeNumber(String number) {
		if (null == mGioneeNumbers) {
			mGioneeNumbers = mContext.getResources().getStringArray(
					R.array.gn_gionee_number);
		}

		if (TextUtils.isEmpty(number) || null == mGioneeNumbers) {
			return false;
		} else {
			for (int i = 0; i < mGioneeNumbers.length; i++) {
				if (mGioneeNumbers[i].equals(number)) {
					return true;
				}
			}
		}
		return false;
	}

	void setVisibility(View view, int visibility) {
		if (view.getVisibility() != visibility) {
			view.setVisibility(visibility);
		}
	}

	private static String from[] =  {"十二","十一","一","二","三","四","五","六","七","八","九","十"};
	private static String to[] = {"12","11","1","2","3","4","5","6","7","8","9","10"} ;

	String replaceString(String src) 
	{
		if(src == null) {
			return null;
		} 
		if(src.startsWith("十二")){
			src = src.replaceAll(from[0],to[0]); 
		}else if(src.startsWith("十一")){
			src = src.replaceAll(from[1],to[1]); 
		}else if(src.startsWith("一")){
			src = src.replaceAll(from[2],to[2]); 
		}else if(src.startsWith("二")){
			src = src.replaceAll(from[3],to[3]); 
		}else if(src.startsWith("三")){
			src = src.replaceAll(from[4],to[4]); 
		}else if(src.startsWith("四")){
			src = src.replaceAll(from[5],to[5]); 
		}else if(src.startsWith("五")){
			src = src.replaceAll(from[6],to[6]); 
		}else if(src.startsWith("六")){
			src = src.replaceAll(from[7],to[7]); 
		}else if(src.startsWith("七")){
			src = src.replaceAll(from[8],to[8]); 
		}else if(src.startsWith("八")){
			src = src.replaceAll(from[9],to[9]); 
		}else if(src.startsWith("九")){
			src = src.replaceAll(from[10],to[10]); 
		}else if(src.startsWith("十")){
			src = src.replaceAll(from[11],to[11]); 
		}
		return src; 
	} 

	private AuroraListView mListView;
	public void setListView(AuroraListView l){
		mListView = l;
	}

	public float getCharacterWidth(TextView tv, String text){
		if(TextUtils.isEmpty(text) || tv == null) {
			return 0;
		}
		return tv.getPaint().measureText(text) * tv.getScaleX();
	}	

	String mCountText="";
	public static final int DEFAULT_LISTVIE_COUNT = 500;
	public static final int DEFAULT_LISTVIE_COUNT_MORE = 200;
	int mCount = DEFAULT_LISTVIE_COUNT;
	public static final boolean mIsSlideDownToShowMore = false;

	public void setPrivate(boolean value) {
		mIsPrivate = value;
	}
	private boolean mIsPrivate = false;
}
