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
import java.util.List;
import java.util.Locale;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetails.PhoneCallRecord;
import com.android.contacts.R;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.R.color;
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
import android.widget.TextView;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.util.OperatorUtils;



// gionee xuhz 201206013 add for CR00622694 CR00622676 start
import android.widget.ImageView;

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

    private final Context mContext;
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
    
    private static boolean mIsRejectMode = false;

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

    public AuroraCallDetailHistoryAdapter(Context context, LayoutInflater layoutInflater,
            CallTypeHelper callTypeHelper, PhoneCallDetails[] phoneCallDetails,
            boolean showVoicemail, boolean showCallAndSms, View controls, boolean issRejectFlag) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mCallTypeHelper = callTypeHelper;
        mPhoneCallDetails = phoneCallDetails;
        mShowVoicemail = showVoicemail;
        mShowCallAndSms = showCallAndSms;
        mControls = controls;
        //Gionee:huangzy 20120704 modify for CR00633111 start
        mCallTypeResHolder = new CallLogResHolder(mContext);
        //Gionee:huangzy 20120704 modify for CR00633111 end
        
        mIsRejectMode = issRejectFlag;
    }

    @Override
    public int getCount() {
        return mPhoneCallDetails.length + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) {
            return null;
        }
        return mPhoneCallDetails[position - 1];
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return -1;
        }
        return position - 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
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

        return mContext.getString(R.string.callDetailsDurationFormat, minutes, seconds);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
        	
        	View header = convertView == null
                ? mLayoutInflater.inflate(R.layout.gn_call_detail_history_without_voicemail_header_v2, parent, false)
                : convertView;
                
            View addToContacts = header.findViewById(R.id.gn_header_add_to_contacts);
            addToContacts.setVisibility(View.GONE);
            
        	// Call and SMS controls are only shown in the main UI if there is a known number.
            View callAndSmsContainer = header.findViewById(R.id.header_call_and_sms_container);
            callAndSmsContainer.setVisibility(mShowCallAndSms ? View.VISIBLE : View.GONE);
            // The previous lines are provided and maintained by Mediatek Inc.
//            LayoutParams linearParams = (LayoutParams) header.getLayoutParams();
//            Log.i("liumxxx","mControls.getHeight():"+mControls.getHeight());
//            linearParams.height = mControls.getHeight();
//            header.setLayoutParams(linearParams);
//            header.invalidate();
            header.setFocusable(true);
            header.setOnFocusChangeListener(mHeaderFocusChangeListener);
            return header;
        }

        // Make sure we have a valid convertView to start with
        final View result = convertView == null
                ? mLayoutInflater.inflate(R.layout.aurora_call_detail_history_item, parent, false)
                : convertView;

        final PhoneCallDetails details = mPhoneCallDetails[position - 1];
        // gionee xuhz 201206013 modify for CR00622694 CR00622676 start
        ImageView callTypeIconView =
                (ImageView) result.findViewById(R.id.call_type_icon);
        // gionee xuhz 201206013 modify for CR00622694 CR00622676 end
        ImageView simSlotView =
                (ImageView) result.findViewById(R.id.aurora_sim_icon);
       	if(GNContactsUtils.isMultiSimEnabled()) {
       		simSlotView.setVisibility(View.VISIBLE);
	        int simId = details.simId;
	        int iconRes = ContactsUtils.getSimIcon(mContext, simId);
	        simSlotView.setImageResource(iconRes);
        } else {
      		simSlotView.setVisibility(View.GONE);
        }
        

//        TextView callTypeTextView = (TextView) result.findViewById(R.id.call_type_text);
        TextView dateView = (TextView) result.findViewById(R.id.date);
        TextView durationView = (TextView) result.findViewById(R.id.duration);

        int callType = details.callType;
        // gionee xuhz 201206013 modify for CR00622694 CR00622676 start
        int slotId = ContactsUtils.getSlotBySubId(details.simId);
        
        int callTypeRes = mCallTypeResHolder.getCallTypeDrawable(callType,
                slotId);
        
        callTypeIconView.setImageResource(callTypeRes);
        // gionee xuhz 201206013 modify for CR00622694 CR00622676 end
        
        /**
         * Change Feature by Mediatek End
         */
//        callTypeTextView.setText(mCallTypeHelper.getCallTypeText(callType));
        // Set the date.
        //GIONEE xuhz 20120522 modify for CR00575945 start
//        CharSequence dateValue = DateUtils.formatDateRange(mContext, details.date, details.date,
//        		DateUtils.FORMAT_CAP_NOON_MIDNIGHT | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
//                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
        CharSequence dateValue = formatGNTime(mContext, details.date);
        //GIONEE xuhz 20120522 modify for CR00575945 start
        dateView.setText(dateValue);
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
            durationView.setVisibility(View.GONE);
        } else {
            durationView.setVisibility(View.VISIBLE);
            if(callType == Calls.MISSED_TYPE) {
            	durationView.setText(mCallTypeResHolder.getCallDurationText(
            			callType, details.duration));
            }
            else
            {
             durationView.setText(formatDuration(details.duration).replaceAll(" ", ""));
            }
        }
        
        if (mIsRejectMode) {
        	durationView.setVisibility(View.GONE);
        	callTypeIconView.setVisibility(View.GONE);
        	
        }
        
        final View playRecordIcon = result.findViewById(R.id.gn_phone_record_play_icon);
        if (null != playRecordIcon) {
        	if (null != details.getPhoneRecords() && result != null) {
        		playRecordIcon.setVisibility(View.VISIBLE);
        		result.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (playRecordIcon != null && playRecordIcon.isShown()) {
							playRecord(details.getPhoneRecords());
						}
					}
				});
        	} else {
        		result.setClickable(false);
        		result.setFocusable(false);
        		playRecordIcon.setVisibility(View.GONE);
        	}
        }
        
        return result;
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
    	new AuroraAlertDialog.Builder(mContext)
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
        intent.setDataAndType(data, record.getMimeType());  
        mContext.startActivity(intent);
    }
    
    //GIONEE xuhz 20120522 add for CR00575945 start
    private String formatGNTime(Context context, long l) {
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
}
