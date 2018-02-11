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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.FullCallDetailActivity;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetails.PhoneCallRecord;
import com.android.contacts.R;
import com.android.contacts.interactions.ContactInteractionUtil;
import com.android.contacts.list.AuroraCallRecordHistoryAdapter.AuroraCallRecord;

import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.R.color;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.util.OperatorUtils;















// gionee xuhz 201206013 add for CR00622694 CR00622676 start
import android.widget.ImageView;

import com.mediatek.contacts.calllog.CallLogDateFormatHelper;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;

import gionee.provider.GnTelephony.SIMInfo;
// gionee xuhz 201206013 add for CR00622694 CR00622676 end

import android.os.Build;
// Gionee: 20121107 xuyongji add for CR00725775 begin
import android.os.SystemProperties;
// Gionee: 20121107 xuyongji add for CR00725775 end

/**
 * Adapter for a ListView containing history items from the details of a call.
 */
public class AuroraCallDetailHistoryAdapter extends BaseAdapter {
	/** The top element is a blank header, which is hidden under the rest of the UI. */
	private static final int VIEW_TYPE_HEADER = 0;
	/** Each history item shows the detail of a call. */
	private static final int VIEW_TYPE_HISTORY_ITEM = 1;
	public boolean editMode = false;
	private Set<Uri> selectSet;
	private HashMap<String, PhoneCallDetails> mCheckedItem = new HashMap<String, PhoneCallDetails>();
	private boolean mCheckBoxEnable = false;
	private boolean mNeedAnim = false;
	public void setNeedAnim(boolean flag) {
		mNeedAnim = flag;
	}

	public boolean getNeedAnim() {
		return mNeedAnim;
	}
	private final Activity mActivity;
	private final LayoutInflater mLayoutInflater;
	private final CallTypeHelper mCallTypeHelper;
	private final PhoneCallDetails[] mPhoneCallDetails;
	/** Whether the voicemail controls are shown. */
	private final boolean mShowVoicemail;
	/** Whether the call and SMS controls are shown. */
	private final boolean mShowCallAndSms;
	/** The controls that are shown on top of the history list. */
	private final View mControls;
	/** The listener to changes of focus of the header. */

	//Gionee:huangzy 20120704 modify for CR00633111 start
	private CallLogResHolder mCallTypeResHolder;
	//Gionee:huangzy 20120704 modify for CR00633111 end

	// Gionee: 20121107 xuyongji add for CR00725775 begin
	private static final boolean gnCommonAPM = SystemProperties.get("ro.gn.oversea.custom").equals("SOUTH_AMERICA_BLU");
	// Gionee: 20121107 xuyongji add for CR00725775 end
	private static final String TAG = "liyang-AuroraCallDetailHistoryAdapter";

	private static boolean mIsRejectMode = false;

	private Uri[] mCallEntryUris;

	public void setCallEntryUris(Uri[] callEntryUris) {
		this.mCallEntryUris = callEntryUris;
	}

	private final int mMissedCallColor, mNormalColor;

	private View.OnFocusChangeListener mHeaderFocusChangeListener =
			new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// When the header is focused, focus the controls above it instead.
			if (hasFocus) {
				mControls.requestFocus();
			}
		}
	};

	public AuroraCallDetailHistoryAdapter(Activity context, LayoutInflater layoutInflater,
			CallTypeHelper callTypeHelper, PhoneCallDetails[] phoneCallDetails,
			boolean showVoicemail, boolean showCallAndSms, View controls, boolean issRejectFlag) {
		mActivity = context;
		mLayoutInflater = layoutInflater;
		mCallTypeHelper = callTypeHelper;
		mPhoneCallDetails = phoneCallDetails;
		mShowVoicemail = showVoicemail;
		mShowCallAndSms = showCallAndSms;
		mControls = controls;
		//Gionee:huangzy 20120704 modify for CR00633111 start
		mCallTypeResHolder = new CallLogResHolder(mActivity);
		//Gionee:huangzy 20120704 modify for CR00633111 end
		Log.e("FullCallDetailActivity","mPhoneCallDetails: "+mPhoneCallDetails.length);

		mIsRejectMode = issRejectFlag;
		mMissedCallColor = context.getResources().getColor(
				R.color.aurora_calllog_missed_color_v2);		
		mNormalColor= context.getResources().getColor(
				R.color.quickcontact_entry_header_text_color);	
		selectSet = new HashSet<Uri>();
	}

	@Override
	public int getCount() {
		return mPhoneCallDetails.length;
	}

	private void checkCheck(int postion, BaseHolder holder) {
		try{
			if (postion >= 0) {
				if (selectSet.contains(mCallEntryUris[postion])) {
					holder.cb.auroraSetChecked(true, mNeedAnim);
				} else {
					holder.cb.auroraSetChecked(false, mNeedAnim);
				}
			}
		}catch(Exception e){
			Log.d(TAG, "e:"+e);
		}
	}

	@Override
	public Object getItem(int position) {
		//        if (position == 0) {
		//            return null;
		//        }
		return mPhoneCallDetails[position];
	}

	@Override
	public long getItemId(int position) {
		//        if (position == 0) {
		//            return -1;
		//        }
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		//        if (position == 0) {
		//            return VIEW_TYPE_HEADER;
		//        }
		return VIEW_TYPE_HISTORY_ITEM;
	}

	private String formatDuration(long elapsedSeconds) {
		long minutes = 0;
		long seconds = 0;

		if (elapsedSeconds >= 60) {
			minutes = elapsedSeconds / 60;
			elapsedSeconds -= minutes * 60;
		}
		seconds = elapsedSeconds;

		return mActivity.getString(R.string.callDetailsDurationFormat, minutes, seconds);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.e("FullCallDetailActivity","position: "+position);
		BaseHolder baseHolder = null;
		CallItemHolder callItemHolder = null;

		//        if (position == 0) {
		//        	
		//        	View header = convertView == null
		//                ? mLayoutInflater.inflate(R.layout.gn_call_detail_history_without_voicemail_header_v2, parent, false)
		//                : convertView;
		//                
		//            View addToContacts = header.findViewById(R.id.gn_header_add_to_contacts);
		//            addToContacts.setVisibility(View.GONE);
		//            
		//        	// Call and SMS controls are only shown in the main UI if there is a known number.
		//            View callAndSmsContainer = header.findViewById(R.id.header_call_and_sms_container);
		//            callAndSmsContainer.setVisibility(mShowCallAndSms ? View.VISIBLE : View.GONE);
		//            // The previous lines are provided and maintained by Mediatek Inc.
		//            header.setFocusable(true);
		//            header.setOnFocusChangeListener(mHeaderFocusChangeListener);
		//            return header;
		//        }

		final PhoneCallDetails details = mPhoneCallDetails[position];

		Log.e("FullCallDetailActivity","details: "+details.numberLabel);
		Log.e("FullCallDetailActivity","details: "+details.vtCall);

		// Make sure we have a valid convertView to start with
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(
					com.aurora.R.layout.aurora_slid_listview, null);
			RelativeLayout front1 = (RelativeLayout) convertView
					.findViewById(com.aurora.R.id.aurora_listview_front);
			mLayoutInflater.inflate(R.layout.aurora_call_detail_history_item, front1);

			callItemHolder = createCallItemHolder(convertView);
			baseHolder = callItemHolder;
			convertView.setTag(callItemHolder);
		} else {
			callItemHolder = (CallItemHolder)convertView.getTag();
			baseHolder = callItemHolder;
		}

		checkCheck(position, callItemHolder);
		checkShowCheckBox(details, baseHolder);


		if(ContactsApplication.isMultiSimEnabled) {
			callItemHolder.simSlotView.setVisibility(View.VISIBLE);
			int simId = details.simId;
			int iconRes = ContactsUtils.getAuroraSimIcon(mActivity, simId);
			callItemHolder.simSlotView.setImageResource(iconRes);
		} else {
			callItemHolder.simSlotView.setVisibility(View.GONE);
		}

		callItemHolder.numberView.setText(details.number);


		int callType = details.callType;
		int bVTCall = details.vtCall;
		// gionee xuhz 201206013 modify for CR00622694 CR00622676 start
		int slotId = SIMInfoWrapper.getDefault().getSlotIdBySimId(details.simId);

		int callTypeRes = mCallTypeResHolder.getCallTypeDrawable(callType,
				slotId, bVTCall == 1);

		callItemHolder.callTypeIconView.setImageResource(callTypeRes);
		// gionee xuhz 201206013 modify for CR00622694 CR00622676 end

		/**
		 * Change Feature by Mediatek End
		 */
		//        callTypeTextView.setText(mCallTypeHelper.getCallTypeText(callType));
		// Set the date.
		//GIONEE xuhz 20120522 modify for CR00575945 start
		//        CharSequence dateValue = DateUtils.formatDateRange(mActivity, details.date, details.date,
		//        		DateUtils.FORMAT_CAP_NOON_MIDNIGHT | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
		//                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
		CharSequence dateValue = ContactInteractionUtil.formatDateStringFromTimestamp(details.date, mActivity);
		//formatGNTime(mActivity, details.date);//CallLogDateFormatHelper.getFormatedDateText(mActivity, details.date);
		//GIONEE xuhz 20120522 modify for CR00575945 start
		callItemHolder.dateView.setText(dateValue);
		// Set the duration
		/**
		 * Change Feature by Mediatek Begin.
		 * Original Android's Code:
        if (callType == Calls.MISSED_TYPE || callType == Calls.VOICEMAIL_TYPE) {
		 * Descriptions: MTK_OP01_PROTECT
		 */
		if (/*callType == Calls.MISSED_TYPE ||*/ callType == Calls.VOICEMAIL_TYPE//aurora changes zhouxiaobing 20140327 for missed call need duration
				|| OperatorUtils.getOptrProperties().equals("OP01")) {
			/**
			 * Change Feature by Mediatek End.
			 */
			callItemHolder.durationView.setVisibility(View.GONE);
		} else {
			callItemHolder.durationView.setVisibility(View.VISIBLE);
			callItemHolder.durationView.setText(mCallTypeResHolder.getCallDurationText(
					callType, details.duration));
			if(callType == Calls.MISSED_TYPE) {
				callItemHolder.durationView.setTextColor(mMissedCallColor);
			}
			else
			{
				//            	callItemHolder.durationView.setText(formatDuration(details.duration).replaceAll(" ", ""));
				callItemHolder.durationView.setTextColor(mNormalColor);
			}
		}

		if (editMode) {
			callItemHolder.callTypeIconView.setVisibility(View.INVISIBLE);
			callItemHolder.durationView.setVisibility(View.INVISIBLE);
		} else {
			callItemHolder.callTypeIconView.setVisibility(View.VISIBLE);
			callItemHolder.durationView.setVisibility(View.VISIBLE);
		}

		if (mIsRejectMode) {
			callItemHolder.durationView.setVisibility(View.GONE);
			callItemHolder.callTypeIconView.setVisibility(View.GONE);

		}

		Log.d(TAG, "editMode:"+editMode);
		final View playRecordIcon = convertView.findViewById(R.id.gn_phone_record_play_icon);
		if (null != playRecordIcon) {
			if (null != details.getPhoneRecords() && convertView != null) {
				playRecordIcon.setVisibility(View.VISIBLE);

				if (!editMode) {
					playRecordIcon.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (playRecordIcon != null && playRecordIcon.isShown()) {
								Log.d(TAG, "editMode:"+editMode);
								playRecord(details.getPhoneRecords());
							}
						}
					});
				} else {
					playRecordIcon.setClickable(false);
					playRecordIcon.setFocusable(false);
				}
			} else {
				playRecordIcon.setClickable(false);
				playRecordIcon.setFocusable(false);
				playRecordIcon.setVisibility(View.GONE);
			}
		}

//		if (null != details.getPhoneRecords() && convertView != null) {
//			convertView.setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					if(!editMode){
//						playRecord(details.getPhoneRecords());
//					}
//				}
//			});
//		}
		return convertView;
	}

	private void checkShowCheckBox(PhoneCallDetails details, BaseHolder holder) {
		//		Log.e("FullCallDetailActivity", "editMode: " + editMode);
		//		Log.e("FullCallDetailActivity", "mNeedAnim: " + mNeedAnim);
		if (editMode) {
			if (mNeedAnim) {
				AuroraListView.auroraStartCheckBoxAppearingAnim(holder.front,
						holder.cb);
			} else {
				AuroraListView.auroraSetCheckBoxVisible(holder.front,
						holder.cb, true);
			}

		} else {
			if (mNeedAnim) {
				AuroraListView.auroraStartCheckBoxDisappearingAnim(
						holder.front, holder.cb);
			} else {
				AuroraListView.auroraSetCheckBoxVisible(holder.front,
						holder.cb, false);
			}
		}

	}

	public void playRecord(int position){
		final PhoneCallDetails details = mPhoneCallDetails[position];		
		playRecord(details.getPhoneRecords());
	}
	
	private void playRecord(final List<PhoneCallRecord> records) {
		if (null == records) {
			return;
		}

		final int size = records.size();

		if (size == 1) {
			playRecord(records.get(0));
			return;
		}

		CharSequence[] items = new CharSequence[size];
		for (int i = 0; i < items.length; i++) {
			items[i] = new File(records.get(i).getPath()).getName().substring(0, 13) + ".amr";
		}
		new AuroraAlertDialog.Builder(mActivity)
		.setTitle(R.string.gn_phone_call_record_tile).setTitleDividerVisible(true)
		.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				playRecord(records.get(which));
			}
		})
		.show();
	}

	private void playRecord(PhoneCallRecord record) {
		if (null == record) {
			return;
		}

		Uri data = Uri.fromFile(new File(record.getPath()));  
		Intent intent = new Intent(Intent.ACTION_VIEW);  
		intent.setClassName("com.android.music", "com.android.music.AudioPreview");
		intent.setDataAndType(data, record.getMimeType());  
		mActivity.startActivity(intent);
	}

	//GIONEE xuhz 20120522 add for CR00575945 start
	public String formatGNTime(Context context, long l) {
		String sRet = null;
		String sTemp = null;

		String strTimeFormat = android.provider.Settings.System.getString(
				context.getContentResolver(), android.provider.Settings.System.TIME_12_24);

		String sTime = "";
		String sDate = "";
		String sZhAPM = "";
		boolean isZh = Locale.getDefault().getLanguage().equals("zh");
		if (0 != l) {
			java.text.DateFormat df = DateFormat.getDateFormat(context);
			sTemp = df.format(l);
			sDate = RelativeFormatData(context,sTemp,l);//sTemp; //aurora change zhouxiaobing 20140327 
			if (mIsRejectMode) {
				sDate = sTemp;
			}
			if (null != strTimeFormat && strTimeFormat.equals("12")) {
				if (isZh) {
					sTime = "hh:mm";
					String sHour = DateFormat.format("kk", l).toString();
					Long lHour = Long.valueOf(sHour);
					// Gionee: 20121107 xuyongji add for CR00725775 begin
					if (gnCommonAPM) {
						if (0 <= lHour && 12 > lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_am);
						} else if (12 <= lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_pm);
						}
					} else {
						// Gionee: 20121107 xuyongji add for CR00725775 end
						if (0 == lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_midnight);
						} else if (1 <= lHour && 6 > lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_dawn);
						} else if (6 <= lHour && 12 > lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_am);
						} else if (12 == lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_noon);
						} else if (13 <= lHour && 18 > lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_pm);
						} else if (18 == lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_dusk);
						} else {
							sZhAPM = context.getResources().getString(R.string.gn_night);
						}
						// Gionee: 20121107 xuyongji add for CR00725775 begin
					}
					// Gionee: 20121107 xuyongji add for CR00725775 end
				} else {
					sTime = "hh:mm aaa";
				}

			} else if (null != strTimeFormat && strTimeFormat.equals("24")) {
				sTime = "kk:mm";
			} else {
				if (isZh) {
					sTime = "hh:mm";
					String sHour = DateFormat.format("kk", l).toString();
					Long lHour = Long.valueOf(sHour);
					// Gionee: 20121107 xuyongji add for CR00725775 begin
					if (gnCommonAPM) {
						if (0 <= lHour && 12 > lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_am);
						} else if (12 <= lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_pm);
						}
					} else {
						// Gionee: 20121107 xuyongji add for CR00725775 end
						if (0 == lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_midnight);
						} else if (1 <= lHour && 6 > lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_dawn);
						} else if (6 <= lHour && 12 > lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_am);
						} else if (12 == lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_noon);
						} else if (13 <= lHour && 18 > lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_pm);
						} else if (18 == lHour) {
							sZhAPM = context.getResources().getString(R.string.gn_dusk);
						} else {
							sZhAPM = context.getResources().getString(R.string.gn_night);
						}
						// Gionee: 20121107 xuyongji add for CR00725775 begin
					}
					// Gionee: 20121107 xuyongji add for CR00725775 end
				} else {
					sTime = "hh:mm aaa";
				}
			}

			if (!sZhAPM.equals("")) {
				sRet = sDate + " " + sZhAPM + " " + DateFormat.format(sTime, l).toString();
			} else {
				sRet = sDate + " " + DateFormat.format(sTime, l).toString();
			}

		}

		return sRet;
	}
	//aurora add zhouxiaobing 20140327  start 
	@SuppressWarnings("deprecation")
	public String RelativeFormatData(Context context,String s,long l)
	{
		Date date1=new Date();    	
		Date date2=new Date(l);    
		if(date1.getYear()==date2.getYear())
		{
			String template = "-";
			if(Build.VERSION.SDK_INT >= 21) {
				template = "/";
			}
			int start1=s.indexOf(template, 0) ;
			int start2=s.indexOf(template, start1+1);
			if(start1==4)
			{
				return s.substring(5);

			}
			else if(start2==7)
			{
				return s.substring(0, 3)+s.substring(8, s.length());
			}
			else
			{
				return s.substring(0,s.length()-5);
			}
		}
		else
		{
			return s;
		}

	}
	//aurora add zhouxiaobing 20140327  end     
	//GIONEE xuhz 20120522 add for CR00575945 end

	static class CallItemHolder extends BaseHolder {
		ImageView callTypeIconView;
		ImageView simSlotView;
		TextView numberView;
		TextView dateView;
		TextView durationView;
		//        View playRecordIcon;
	}

	static class BaseHolder {
		int position;
		RelativeLayout front;
		AuroraCheckBox cb;
	}

	private void findBaseHolder(View view, BaseHolder baseHolder) {
		baseHolder.front = (RelativeLayout) view
				.findViewById(com.aurora.R.id.aurora_listview_front);
		baseHolder.cb = (AuroraCheckBox) view
				.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
	}

	private CallItemHolder createCallItemHolder(View view) {
		CallItemHolder mCallItemHolder = new CallItemHolder();
		findBaseHolder(view, mCallItemHolder);
		mCallItemHolder.callTypeIconView = (ImageView) view.findViewById(R.id.call_type_icon);
		mCallItemHolder.simSlotView = (ImageView) view.findViewById(R.id.aurora_sim_icon);
		mCallItemHolder.numberView = (TextView) view.findViewById(R.id.number);
		mCallItemHolder.dateView = (TextView) view.findViewById(R.id.date);
		mCallItemHolder.durationView = (TextView) view.findViewById(R.id.duration);
		//		mCallItemHolder.playRecordIcon = view.findViewById(R.id.gn_phone_record_play_icon);
		return mCallItemHolder;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public Set<Uri> getSelectSet() {
		return selectSet;
	}
}
