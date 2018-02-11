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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.provider.CallLog.Calls;
import gionee.provider.GnContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateUtils;
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
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.TextView;
import aurora.widget.AuroraListView;
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
import com.android.contacts.util.Constants;
import com.android.contacts.util.GnHotLinesUtil;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
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
	 * Change Feature by Mediatek End.
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
	private final float move1;
	private final float move2;
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
	AuroraCallLogListItemViewV2 itemviewv2;
	
	private String hotnames[];
	private String hotnumbers[];
	
	private final int LISTITEM_CHANGING = 0;
	private final int Check_Anim = 1;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case LISTITEM_CHANGING:
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

	public AuroraCallLogAdapterV2(Context context, CallFetcher callFetcher) {
		super(context, null, false);
		mContext = context;
		mCallFetcher = callFetcher;

		Resources resources = mContext.getResources();
        move1 = resources.getDimension(R.dimen.aurora_call_log_animate_move_length_1);
        move2 = resources.getDimension(R.dimen.aurora_call_log_animate_move_length_2);
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
		mCallTypeResHolder = new CallLogResHolder(mContext);
		
		//aurora add liguangyu 20140327 for BUG #3549 begin
		mTextEditColor1 = mContext.getResources().getColor(R.color.aurora_calllog_list_text_color_edit_1);
		mTextEditColor2 = mContext.getResources().getColor(R.color.aurora_calllog_list_text_color_edit_2);
		mTextEditColor3 = mContext.getResources().getColor(R.color.aurora_calllog_list_text_color_edit_3);
		mTextEditColorReject = mContext.getResources().getColor(R.color.aurora_calllog_list_text_color_edit_reject);
		//aurora add liguangyu 20140327 for BUG #3549 end
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (!mCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position "
					+ positons[position]);
		}
		CursorIndex=position;
		View v;
		if (convertView == null) {
			v = newView(mContext, mCursor, parent);
		} else {
			v = convertView;
		}
		bindView(v, mContext, mCursor);
		
		itemviewv2.mCheckBox.auroraSetChecked(checkeds[position], isCheckAnim);

		
		//aurora change liguangyu 20131110 start
		if(is_listitem_changing && is_editor_mode) {
			itemviewv2.mSecondaryButton.setVisibility(View.INVISIBLE);
        	mListView.auroraStartCheckBoxAppearingAnim(itemviewv2.mContent, itemviewv2.mCheckBox);
		} else if (is_listitem_changing) {
			    itemviewv2.mSecondaryButton.setVisibility(View.VISIBLE);
        		mListView.auroraStartCheckBoxDisappearingAnim(itemviewv2.mContent, itemviewv2.mCheckBox);
		} else {
			
			mListView.auroraSetCheckBoxVisible(itemviewv2.mContent, itemviewv2.mCheckBox, is_editor_mode);
			if (is_editor_mode) {
				itemviewv2.mSecondaryButton.setVisibility(View.INVISIBLE);
				itemviewv2.mContent.setTranslationX(move1);	
			} else {			
				itemviewv2.mContent.setTranslationX(0f);	
				itemviewv2.mSecondaryButton.setVisibility(View.VISIBLE);
				itemviewv2.refreshDrawableState();
			}
		}
		//aurora change liguangyu 20131110 end

		if (is_listitem_changing)
			handler.sendMessage(handler.obtainMessage(LISTITEM_CHANGING));
		//aurora add liguangyu 20140331 for bug 3948 start
		if(isCheckAnim) {
			handler.sendMessage(handler.obtainMessage(Check_Anim));
		}
		//aurora add liguangyu 20140331 for bug 3948 end
		ContactsLog.logt(TAG, "getView end");
		return v;
	}

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
		GnContactInfo info = null;
		String hashKey = c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_NUMBER)
				+ c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_DATE);
		info = mContactInfoMap.get(hashKey);
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

	private static final String TAG = "AuroraCallLogAdapterV2";
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
		// TODO Auto-generated method stub
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
		   if (view.getLastVisiblePosition() == (view.getCount() - 1) && mIsSlideDownToShowMore) {
				if(positons.length >= mCount + DEFAULT_LISTVIE_COUNT_MORE) {
					mCount += DEFAULT_LISTVIE_COUNT_MORE;              
				} else {
					mCount = positons.length;
				}
				notifyDataSetChanged();
		   }
		} 
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
		log("changeCursor(), cursor = " + cursor);
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
		AuroraCallLogListItemViewV2 cliv = AuroraCallLogListItemViewV2
				.create(mContext);
		mNormalCallColor = cliv.mName.getTextColors();		
		mNumberColor = cliv.mNumber.getTextColors();
		
		//aurora add liguangyu 20140327 for BUG #3549 begin
		mTextNormalColor1 = cliv.mDate.getTextColors();
		mTextNormalColor2 = cliv.mCallCount.getTextColors();
		mTextNormalColor3 = cliv.mArea.getTextColors();
		mTextNormalColorReject = cliv.mReject.getTextColors();
		//aurora add liguangyu 20140327 for BUG #3549 end
		
		View v = (View) LayoutInflater.from(mContext).inflate(
                com.aurora.R.layout.aurora_slid_listview, null);
		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_list_header));
		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_listview_custom_front));
		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_listview_divider));
		((LinearLayout)v).removeView(v.findViewById(com.aurora.R.id.aurora_item_sliding_switch));
		RelativeLayout mainUi = (RelativeLayout) v
                .findViewById(com.aurora.R.id.aurora_listview_front);
        mainUi.addView(cliv, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        
        ItemHolder itemHolder = new ItemHolder();
        itemHolder.itemviewv2 = cliv;
        itemHolder.auroraChildContent = (LinearLayout) v.findViewById(com.aurora.R.id.content);
        v.setTag(itemHolder);
    
		return v;
	}
	
	private class ItemHolder{
		public AuroraCallLogListItemViewV2 itemviewv2;
		public LinearLayout auroraChildContent;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		  //aurora add liguangyu 20140409 for AuroraListView SlideDelete start
		ItemHolder itemHolder = (ItemHolder) view.getTag();
  		ViewGroup.LayoutParams lp =view.getLayoutParams(); 
  		if (lp != null) {
  			lp.height = preferredHeight;
  			view.setLayoutParams(lp);
  			itemHolder.auroraChildContent.setAlpha(255);
  		}//haoshidian4
  		//aurora add liguangyu 20140409 for AuroraListView SlideDelete end
        itemviewv2 = itemHolder.itemviewv2;
        contactInfo=getContactInfo(cursor);

        if(!contactInfo.isInitDisplay){
        	contactInfo.isInitDisplay = true;
    		// New items also use the highlighted version of the text.
    		final boolean isHighlighted = contactInfo.isRead == 0;
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
    		} else if (contactInfo.contactSimId > 0) {
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
    		}
    		if (TextUtils.isEmpty(contactInfo.name)) {
    			contactInfo.photoId = ContactPhotoManager.DEFAULT_UNKOWN_CONTACT_PHOTO;
    			contactInfo.hideNumber = true;
    			String number = contactInfo.number;
    			if (null != number) {
    				if (isGioneeNumber(contactInfo.stripedNumber)) {
    					contactInfo.name = mContext.getResources().getString(
    							R.string.gn_customer_service);
    				} else {
    					if (number.startsWith("sip:")) {
    						number = number.substring(4);
    					}
    					contactInfo.name = mPhoneNumberHelper.getDisplayNumber(
    							contactInfo.number, contactInfo.formattedNumber)
    							.toString();
    				}
    			}
    		}
    		
    		contactInfo.simIcon = ContactsUtils.getSimIcon(mContext, contactInfo.simId);

    		if (ContactsApplication.sIsGnAreoNumAreaSupport) {
    			if(!TextUtils.isEmpty(contactInfo.area)) {
    				contactInfo.displayArea = contactInfo.area.replace(" ", "");
//    				if (ContactsApplication.sIsHotLinesSupport && hotnumbers != null) {
//    					if(hotnumberIndexs[CursorIndex]>-1) {
//    						String hotLineArea = hotnames[hotnumberIndexs[CursorIndex]];
//    						if(hotLineArea.equalsIgnoreCase(contactInfo.area)) {
//    							contactInfo.displayArea = mContext.getResources().getString(R.string.aurora_service_number);
//    						}
//    					}
//    				}
    				
    			} else {
    				contactInfo.displayArea = mContext.getResources().getString(R.string.aurora_unknow_source_calllog);
    			}
    		}

    		contactInfo.callTypeRes = mCallTypeResHolder.getCallTypeDrawable(
    				contactInfo.type,
    				mSIMInfoWrapper.getSlotIdBySimId(contactInfo.simId),
    				contactInfo.vtCall == 1);

    		if (contactInfo.type == Calls.MISSED_TYPE) {
    			if (!TextUtils.isEmpty(contactInfo.displayArea)) {
    				contactInfo.displayDuration = mCallTypeResHolder.getCallDurationText(
    						contactInfo.type, contactInfo.duration)+"，";
    			} else {
    				contactInfo.displayDuration = mCallTypeResHolder.getCallDurationText(
    						contactInfo.type, contactInfo.duration);
    			}	
    		}
        }
        mCountText = (contactInfo.gnCallsCount > 1) ? "(" + contactInfo.gnCallsCount+ ")" : "";

		itemviewv2.mSecondaryButton.setTag(contactInfo);

       	if(GNContactsUtils.isMultiSimEnabled()) {
       		itemviewv2.mSimIcon.setVisibility(View.VISIBLE);
	        itemviewv2.mSimIcon.setImageResource(contactInfo.simIcon);
       	} else {
       		itemviewv2.mSimIcon.setVisibility(View.GONE);
       	}
       
		
       	itemviewv2.mName.setText(contactInfo.name);
		itemviewv2.mCallCount.setText(mCountText);
		itemviewv2.mCallCount.setVisibility(TextUtils.isEmpty(mCountText)? View.GONE : View.VISIBLE);
		itemviewv2.mNumber.setText(contactInfo.number);
		itemviewv2.mNumber.setTag(contactInfo.number);
		if (ContactsApplication.sIsGnAreoNumAreaSupport) {
			itemviewv2.mArea.setText(contactInfo.displayArea);
		}
		
		// The date of this call, relative to the current time.
		CharSequence dateText = DateUtils.getRelativeTimeSpanString(
				contactInfo.date, System.currentTimeMillis(),
				DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
		String date =dateText.toString().replaceAll(" ", ""); 
		if(android.os.Build.VERSION.SDK_INT==18){
			date  = replaceString(date);
		}
		itemviewv2.mDate.setText(date);
		if (null != itemviewv2.mDuration) {
			if (contactInfo.type == Calls.MISSED_TYPE) {
				Log.v(TAG, "contactInfo.duration="+contactInfo.duration);
				itemviewv2.mDuration.setText(contactInfo.displayDuration);
			}
		}

		itemviewv2.mCallType.setImageResource(contactInfo.callTypeRes);

		itemviewv2.setClickable(false);
		
		
		if(!TextUtils.isEmpty(contactInfo.mark) && contactInfo.lookupUri == null && ContactsApplication.sIsAuroraRejectSupport) {
			itemviewv2.mSogouLine.setVisibility(View.VISIBLE);
			if(contactInfo.userMark >= 0) {
				itemviewv2.mSogouIcon.setVisibility(View.VISIBLE);
				float nameWidth = getCharacterWidth(itemviewv2.mName, contactInfo.name);
				float dateWidth = getCharacterWidth(itemviewv2.mDate, date);
				float rejectWidth = getCharacterWidth(itemviewv2.mReject, contactInfo.mark);
				float countWidth = 0;			
				int totalWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.aurora_call_log_text_width);
				if(TextUtils.isEmpty(mCountText)){
					countWidth -=  mContext.getResources().getDimensionPixelOffset(R.dimen.aurora_call_log_count_margin);
				} else {
					countWidth = getCharacterWidth(itemviewv2.mCallCount, mCountText);
				}
				int rejectMaxWidth = (int) (totalWidth- nameWidth -  dateWidth - countWidth);
				if(rejectWidth > rejectMaxWidth) {
					String rejectShort = TextUtils.ellipsize(contactInfo.mark, itemviewv2.mReject.getPaint(), rejectMaxWidth, TruncateAt.END).toString();
					itemviewv2.mReject.setText(rejectShort);
				} else {
					itemviewv2.mReject.setText(contactInfo.mark);
				}
			} else {
				itemviewv2.mSogouIcon.setVisibility(View.GONE);
				itemviewv2.mReject.setText(contactInfo.mark);
			}			
		} else {
			itemviewv2.mSogouLine.setVisibility(View.GONE);
		}
		
		
		//aurora add liguangyu 20140327 for BUG #3549 start
		if (is_editor_mode) {
			itemviewv2.mDate.setTextColor(mTextEditColor1);
			itemviewv2.mName.setTextColor(mTextEditColor2);
			itemviewv2.mCallCount.setTextColor(mTextEditColor2);
			itemviewv2.mNumber.setTextColor(mTextEditColor3);
			itemviewv2.mArea.setTextColor(mTextEditColor3);
			itemviewv2.mDuration.setTextColor(mTextEditColor3);	
			itemviewv2.mReject.setTextColor(mTextEditColorReject);	
		} else {
			itemviewv2.mDate.setTextColor(mTextNormalColor1);
			itemviewv2.mName.setTextColor(mTextNormalColor2);
			itemviewv2.mCallCount.setTextColor(mTextNormalColor2);
			itemviewv2.mNumber.setTextColor(mTextNormalColor3);
			itemviewv2.mArea.setTextColor(mTextNormalColor3);
			itemviewv2.mDuration.setTextColor(mTextNormalColor3);		
			itemviewv2.mReject.setTextColor(mTextNormalColorReject);	
		}
		//aurora add liguangyu 20140327 for BUG #3549 end

		if (contactInfo.type == Calls.MISSED_TYPE) {
			itemviewv2.mName.setTextColor(mMissedCallColor);
			itemviewv2.mCallCount.setTextColor(mMissedCallColor);// aurora change
			itemviewv2.mDuration.setVisibility(View.VISIBLE);	
			itemviewv2.mDuration.setTextColor(mMissedCallColor2);
			itemviewv2.mArea.setTextColor(mMissedCallColor2);
			itemviewv2.mNumber.setTextColor(mMissedCallColor2);
		} else {
			itemviewv2.mDuration.setVisibility(View.GONE);	   // 20130919
		}

		setVisibility(itemviewv2.mNumber, View.GONE);
		itemviewv2.mArea.setVisibility(View.VISIBLE);
		bindViewAction(itemviewv2, context, contactInfo, cursor);

        if(!mIsPrivate && contactInfo.private_id > 0 && contactInfo.private_id == AuroraPrivacyUtils.getCurrentAccountId()) {
			itemviewv2.mPirvateIcon.setVisibility(View.VISIBLE);
		} else {
			itemviewv2.mPirvateIcon.setVisibility(View.GONE);
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
		itemView.mSecondaryButton.setEnabled(dialable);
		if (dialable || ResConstant.isCallLogListItemCallPrimary()) {
			itemView.mSecondaryButton
					.setOnClickListener(mSecondaryButtonClick);
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
