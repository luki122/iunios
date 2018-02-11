package com.android.contacts.calllog;

import android.content.Context;

import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import android.provider.CallLog.Calls;
import gionee.provider.GnTelephony.SIMInfo;
//gionee yewq 2012.10.26 modify for begin
import java.util.Locale;
//gionee yewq 2012.10.26 modify for end

public class CallLogResHolder {
    private Context mContext;
    private int mColorSimA;
    private int mColorSimB;
    
    //Gionee:huangzy 20120607 modify for CR00622094 start
    public CallLogResHolder(Context context) {
        mContext = context;
        refreshSimColor();
        
        mHourLabel = context.getString(R.string.gn_hour);
        mMinuteLabel = context.getString(R.string.gn_minute);
        mSecondLabel = context.getString(R.string.gn_second);
        mDisconnectLabel = context.getString(R.string.gn_call_unconnected);
        mOutGoingLabel = context.getString(R.string.gn_call_outgoing_type);
        mIncomingLabel = context.getString(R.string.gn_call_incoming_type);
        mRingTimesLabel = context.getString(R.string.gn_call_ring_times);
        mRingUnitLabel = context.getString(R.string.gn_call_ring_unit);
        //gionee yewq 2012.10.26 modify for begin
        mRingUnitLabels = context.getString(R.string.gn_call_ring_units);;
        //gionee yewq 2012.10.26 modify for end
    }
    
    public void refreshSimColor() {
        if (null == mContext) {
            return;
        }
       
        SIMInfo simInfo = null; 
        simInfo = SIMInfo.getSIMInfoBySlot(mContext, 0);
        mColorSimA = (null != simInfo) ? simInfo.mColor : 0;
        simInfo = SIMInfo.getSIMInfoBySlot(mContext, 1);
        mColorSimB = (null != simInfo) ? simInfo.mColor : 0;
    }
    //Gionee:huangzy 20120607 modify for CR00622094 start
    
    private final int[][] mIconReses = {
            {
               // R.drawable.gn_sim_a_incoming_color0,
              //  R.drawable.gn_sim_a_outgoing_color0,
              //  R.drawable.gn_sim_a_missed_color0,
              //  R.drawable.gn_sim_b_incoming_color0,
              //  R.drawable.gn_sim_b_outgoing_color0,
             //   R.drawable.gn_sim_b_missed_color0,
               // R.drawable.gn_sim_x_incoming_color0,//aurora change zhouxiaobing 20130918
              //  R.drawable.gn_sim_x_outgoing_color0,
              //  R.drawable.gn_sim_x_missed_color0,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.gn_sim_video_incoming_color0,
                R.drawable.gn_sim_video_outgoing_color0,
                R.drawable.gn_sim_video_missed_color0,
                //Gionee <wangth><2013-05-03> add for CR00805658 begin
                R.drawable.gn_sim_x_video_incoming_color0,
                R.drawable.gn_sim_x_video_outgoing_color0,
                R.drawable.gn_sim_x_video_missed_color0,
                //Gionee <wangth><2013-05-03> add for CR00805658 end
                },
            {
               // R.drawable.gn_sim_a_incoming_color1,
               // R.drawable.gn_sim_a_outgoing_color1,
              //  R.drawable.gn_sim_a_missed_color1,
              //  R.drawable.gn_sim_b_incoming_color1,
              //  R.drawable.gn_sim_b_outgoing_color1,
              //  R.drawable.gn_sim_b_missed_color1,
                //R.drawable.gn_sim_x_incoming_color0,//aurora change zhouxiaobing 20130918
                //R.drawable.gn_sim_x_outgoing_color0,
                //R.drawable.gn_sim_x_missed_color0,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.gn_sim_video_incoming_color1,
                R.drawable.gn_sim_video_outgoing_color1,
                R.drawable.gn_sim_video_missed_color1,
                //Gionee <wangth><2013-05-03> add for CR00805658 begin
                R.drawable.gn_sim_x_video_incoming_color0,
                R.drawable.gn_sim_x_video_outgoing_color0,
                R.drawable.gn_sim_x_video_missed_color0,
                //Gionee <wangth><2013-05-03> add for CR00805658 end
                },
            {
                //R.drawable.gn_sim_a_incoming_color2,
               // R.drawable.gn_sim_a_outgoing_color2,
               // R.drawable.gn_sim_a_missed_color2,
               // R.drawable.gn_sim_b_incoming_color2,
               // R.drawable.gn_sim_b_outgoing_color2,
                //R.drawable.gn_sim_b_missed_color2,
                //R.drawable.gn_sim_x_incoming_color0,//aurora change zhouxiaobing 20130918
                //R.drawable.gn_sim_x_outgoing_color0,
                //R.drawable.gn_sim_x_missed_color0,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.gn_sim_video_incoming_color2,
                R.drawable.gn_sim_video_outgoing_color2,
                R.drawable.gn_sim_video_missed_color2,
                //Gionee <wangth><2013-05-03> add for CR00805658 begin
                R.drawable.gn_sim_x_video_incoming_color0,
                R.drawable.gn_sim_x_video_outgoing_color0,
                R.drawable.gn_sim_x_video_missed_color0,
                //Gionee <wangth><2013-05-03> add for CR00805658 end
                },
            {
               // R.drawable.gn_sim_a_incoming_color3,
               // R.drawable.gn_sim_a_outgoing_color3,
              //  R.drawable.gn_sim_a_missed_color3,
              //  R.drawable.gn_sim_b_incoming_color3,
              //  R.drawable.gn_sim_b_outgoing_color3,
              //  R.drawable.gn_sim_b_missed_color3,
                //R.drawable.gn_sim_x_incoming_color0,//aurora change zhouxiaobing 20130918
               // R.drawable.gn_sim_x_outgoing_color0,
               // R.drawable.gn_sim_x_missed_color0,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                R.drawable.aurora_ic_call_incoming,
                R.drawable.aurora_ic_call_outgoing,
                R.drawable.aurora_ic_call_missed,
                
                R.drawable.gn_sim_video_incoming_color3,
                R.drawable.gn_sim_video_outgoing_color3,
                R.drawable.gn_sim_video_missed_color3,
                //Gionee <wangth><2013-05-03> add for CR00805658 begin
                R.drawable.gn_sim_x_video_incoming_color0,
                R.drawable.gn_sim_x_video_outgoing_color0,
                R.drawable.gn_sim_x_video_missed_color0,
                //Gionee <wangth><2013-05-03> add for CR00805658 end
                },
        };
    
    //Gionee:huangzy 20120702 modify for CR00633111 start
    public int getCallTypeDrawable(int callType, int slotId, boolean isVideoCall) {
        // Gionee:wangth 20130225 add for CR00787662 begin
        if (GNContactsUtils.isOnlyQcContactsSupport()) {
            if (callType == 5) {
                callType = 1;
            } else if (callType == 6) {
                callType = 2;
            } else if (callType == 7) {
                callType = 3;
            }
        }
        // Gionee:wangth 20130225 add for CR00787662 end
        
        if (callType > 3 || callType < 0) {
            return 0;
        }
        
        int colorIndex = (1 != slotId) ? mColorSimA : mColorSimB;
        int position = slotId;
        
        if (isVideoCall) {
            position = 3;
        } else if (slotId != 0 && slotId != 1){
            position = 2;
        }
        
        position = position * 3 + callType - 1;
        
        //Gionee <wangth><2013-05-03> add for CR00805658 begin
        if ((!GNContactsUtils.isOnlyQcContactsSupport() && !FeatureOption.MTK_GEMINI_SUPPORT) || 
                (GNContactsUtils.isOnlyQcContactsSupport() && !GNContactsUtils.isMultiSimEnabled())) {
            position = callType + 5;
            
            if (isVideoCall) {
                position = callType + 11;
            }
        }
        //Gionee <wangth><2013-05-03> add for CR00805658 end

//        return mIconReses[colorIndex][position];
        return mIconReses[0][position];
        
    }
    //Gionee:huangzy 20120702 modify for CR00633111 end
    
    private final int TOTAL_SECONDS_A_HOUR = 60 * 60;
    //Gionee:huangzy 20120630 add for CR00628527 start
    private final int SECONDS_PER_RINGTONE = 5;
    //Gionee:huangzy 20120630 add for CR00628527 end
    private String mHourLabel;
    private String mMinuteLabel;
    private String mSecondLabel;
    private String mDisconnectLabel;
    private String mOutGoingLabel;
    private String mIncomingLabel;
    private String mRingTimesLabel;
    private String mRingUnitLabel;
    //gionee yewq 2012.10.26 modify for begin
    private String mRingUnitLabels;
    //gionee yewq 2012.10.26 modify for end
    
    public String getCallDurationText(int callType, long elapsedSeconds) {
        
        if (0 == elapsedSeconds && Calls.OUTGOING_TYPE == callType) {
            return mDisconnectLabel;
        }
        
        String type;
        
        switch (callType) {
        case Calls.OUTGOING_TYPE:
            type = mOutGoingLabel;
            break;
        case Calls.INCOMING_TYPE:
            type = mIncomingLabel;
            break;
        case Calls.MISSED_TYPE:
            type = mRingTimesLabel;
            break;
        default:
            type = "";
            break;
        }
        
        StringBuilder duration = new StringBuilder(type);
        if (Calls.MISSED_TYPE == callType) {
            //gionee yewq 2012.10.26 modify for begin
            //Gionee:huangzy 20120630 modify for CR00628527 start
            //duration.append(elapsedSeconds/SECONDS_PER_RINGTONE + 1).append(mRingUnitLabel);
            //Gionee:huangzy 20120630 modify for CR00628527 end
            if(android.os.SystemProperties.get("ro.gn.oversea.product").equals("yes") &&
                //Gionee:luoguangming 2012.11.01 modify for CR00722848 start
				Locale.getDefault().getLanguage().toLowerCase().equals("it")||Locale.getDefault().getLanguage().toLowerCase().equals("de")){
                //Gionee:luoguangming 2012.11.01 modify for CR00722848  end
                duration = new StringBuilder();
                duration.append(elapsedSeconds/SECONDS_PER_RINGTONE + 1);
                if (elapsedSeconds/SECONDS_PER_RINGTONE >= 1){
                    duration.append(mRingUnitLabels);
                }else{
                    duration.append(mRingUnitLabel);
                }
            }else{
                duration.append(elapsedSeconds/SECONDS_PER_RINGTONE + 1).append(mRingUnitLabel);
            }
            //gionee yewq 2012.10.26 modify for end
        } else {
            long seconds = 0;
            long minutes = 0;
            long hour = 0;        

            if (elapsedSeconds < 60) {
                seconds = elapsedSeconds;
            } else if (elapsedSeconds < TOTAL_SECONDS_A_HOUR) {
                minutes = elapsedSeconds/60;
                seconds = elapsedSeconds%60;
            } else {
                hour = elapsedSeconds/TOTAL_SECONDS_A_HOUR;
                minutes = (elapsedSeconds%TOTAL_SECONDS_A_HOUR)/60;
            }
                    
            
            if (0 == hour && 0 == minutes) {
                duration.append(seconds).append(mSecondLabel);
            } else {
                if (hour > 0) {
                    duration.append(hour).append(mHourLabel).append(minutes).append(mMinuteLabel);
                } else if (minutes > 0) {
                    duration.append(minutes).append(mMinuteLabel).append(seconds).append(mSecondLabel);
                }
            }
        }

        return duration.toString();
    }
}
