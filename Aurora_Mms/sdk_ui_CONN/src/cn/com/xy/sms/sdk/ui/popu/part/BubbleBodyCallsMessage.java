package cn.com.xy.sms.sdk.ui.popu.part;

import org.json.JSONObject;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ResourceCacheUtil;
import cn.com.xy.sms.sdk.ui.popu.util.UIConstant;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BubbleBodyCallsMessage extends UIPart {
    private final static String CALL_NUMBER_TEXT_COLOR ="smart_call_number_text_color";
    private final static String CALL_NUMBER_TEXT ="smart_call_number_text";
    private TextView mCallUserNameTextView;
    private TextView mCallUserNameTextViewlable;
    private TextView mNumberFrequencyTextView;
    private TextView mNumberFrequencyTextViewlable;
    private TextView mCallNumber;
    private TextView mCallNumberlable;
    private TextView mCallNumberdate;
    private TextView mCallNumberdatelable;
    private TextView mLastcallNumberdate;
    private TextView mLastcallNumberdatelable;
    private TextView mCallNumberfrom;
    private TextView mCallNumberfromlable;
    private TextView mCallNumberstate;
    private TextView mCallNumberstatelable;
    private TextView mCallingstate;
    private TextView mCallingstatelable;
    private TextView mCallingtime;
    private TextView mCallingtimelable;
    private int mVisibleCount = 0;
 
    public BubbleBodyCallsMessage(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);

    }

    @Override
    public void initUi() throws Exception {
        mCallUserNameTextView = (TextView) mView.findViewById(R.id.duoqu_call_username);
        mCallUserNameTextViewlable = (TextView) mView.findViewById(R.id.duoqu_call_username_title);
        mNumberFrequencyTextView = (TextView) mView.findViewById(R.id.duoqu_number_frequency);
        mNumberFrequencyTextViewlable = (TextView) mView.findViewById(R.id.duoqu_comingcall_number_title);
        mCallNumberdate = (TextView) mView.findViewById(R.id.duoqu_call_time);
        mCallNumberdatelable = (TextView) mView.findViewById(R.id.duoqu_call_time_title);
        mLastcallNumberdate = (TextView) mView.findViewById(R.id.duoqu_lastcall_time);
        mLastcallNumberdatelable = (TextView) mView.findViewById(R.id.duoqu_lastcall_time_title);
        mCallNumberfrom = (TextView) mView.findViewById(R.id.duoqu_call_from);
        mCallNumberfromlable = (TextView) mView.findViewById(R.id.duoqu_callfrom_time_title);
        mCallNumberstate = (TextView) mView.findViewById(R.id.duoqu_call_state);
        mCallNumberstatelable = (TextView) mView.findViewById(R.id.duoqu_call_state_title);
        mCallingstate = (TextView) mView.findViewById(R.id.duoqu_calling_state);
        mCallingstatelable = (TextView) mView.findViewById(R.id.duoqu_calling_state_title);
        mCallingtime = (TextView) mView.findViewById(R.id.duoqu_callingfrom_time);
        mCallingtimelable = (TextView) mView.findViewById(R.id.duoqu_callingfrom_time_title);
        mCallNumber = (TextView) mView.findViewById(R.id.duoqu_call_number);
        mCallNumberlable = (TextView) mView.findViewById(R.id.duoqu_call_number_title);
    }

    @Override
    public void setContent(final BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        mVisibleCount = 0;
        ContentUtil.setViewVisibility(super.mView, View.VISIBLE);
        String callNumber = (String) message.getValue("view_side_phone_num");
        String frequency = (String) message.getValue("view_frequency");
        String call_time = (String) message.getValue("view_call_time");
        String lastcall_time = (String) message.getValue("view_lastcall_time");
        String sideattribution = (String) message.getValue("view_sideattribution");
        String ownstate = (String) message.getValue("view_ownstate");
        String sidestate = (String) message.getValue("view_sidestate");
        String calling_time = (String) message.getValue("view_calling_time");
        
        setLableVisibility(mCallNumber, mCallNumberlable, callNumber, null,false);
        setLableVisibility(mNumberFrequencyTextView, mNumberFrequencyTextViewlable, frequency, null,false);
        setLableVisibility(mCallNumberdate, mCallNumberdatelable, call_time, null,false);
        setLableVisibility(mLastcallNumberdate, mLastcallNumberdatelable, lastcall_time, null,false);
        setLableVisibility(mCallNumberfrom, mCallNumberfromlable, sideattribution, null,false);
        setLableVisibility(mCallNumberstate, mCallNumberstatelable, ownstate, null,false);
        setLableVisibility(mCallingstate, mCallingstatelable, sidestate, null,false);
        setLableVisibility(mCallingtime, mCallingtimelable, calling_time, null,false);
       
        
        String bgColor = message.getImgNameByKey("v_by_bg");
		if(!StringUtils.isNull(bgColor)){
			ViewUtil.setViewBg(Constant.getContext(), mView, bgColor);
		}
		
		String lTextColor=(String)message.getImgNameByKey("v_by_l_color");
		String RTextColor=(String)message.getImgNameByKey("v_by_r_color");
		
		//左边
		ContentUtil.isTextSetColor(mCallUserNameTextViewlable, lTextColor,R.color.duoqu_calls_l);
		ContentUtil.isTextSetColor(mNumberFrequencyTextViewlable, lTextColor,R.color.duoqu_calls_l);
		ContentUtil.isTextSetColor(mCallNumberdatelable, lTextColor,R.color.duoqu_calls_l);
		ContentUtil.isTextSetColor(mLastcallNumberdatelable, lTextColor,R.color.duoqu_calls_l);
		ContentUtil.isTextSetColor(mCallNumberfromlable, lTextColor,R.color.duoqu_calls_l);
		ContentUtil.isTextSetColor(mCallNumberstatelable, lTextColor,R.color.duoqu_calls_l);
		ContentUtil.isTextSetColor(mCallingstatelable, lTextColor,R.color.duoqu_calls_l);
		ContentUtil.isTextSetColor(mCallingtimelable, lTextColor,R.color.duoqu_calls_l);
		ContentUtil.isTextSetColor(mCallNumberlable, lTextColor,R.color.duoqu_calls_l);
		
		//右边
		ContentUtil.isTextSetColor(mCallUserNameTextView, RTextColor,R.color.duoqu_calls_r);
		ContentUtil.isTextSetColor(mNumberFrequencyTextView, RTextColor,R.color.duoqu_calls_r);
		ContentUtil.isTextSetColor(mCallNumber, RTextColor,R.color.duoqu_calls_r);
		ContentUtil.isTextSetColor(mCallNumberdate, RTextColor,R.color.duoqu_calls_r);
		ContentUtil.isTextSetColor(mLastcallNumberdate, RTextColor,R.color.duoqu_calls_r);
		ContentUtil.isTextSetColor(mCallNumberfrom, RTextColor,R.color.duoqu_calls_r);
		ContentUtil.isTextSetColor(mCallNumberstate, RTextColor,R.color.duoqu_calls_r);
		ContentUtil.isTextSetColor(mCallingstate, RTextColor,R.color.duoqu_calls_r);
		ContentUtil.isTextSetColor(mCallingtime, RTextColor,R.color.duoqu_calls_r);
		
        String callNumberColor=(String)mMessage.getValue(CALL_NUMBER_TEXT_COLOR);
        if(callNumberColor != null){
//            android.util.Log.d("duoqu_test", "msgId: "+mMessage.smsId+" COLOR: "+callNumberColor+" name: "+(String)mMessage.getValue(CALL_NUMBER_TEXT));
            bindCallUserViewValue((String)mMessage.getValue(CALL_NUMBER_TEXT),callNumberColor,(String) mMessage.getValue("view_side_phone_num"));
            return ;
        }
        AsyncTask task = new AsyncTask(){
             @Override
            protected BusinessSmsMessage doInBackground(Object... arg) {
                 BusinessSmsMessage msg =(BusinessSmsMessage)arg[0];
                 String callNumber = (String) msg.getValue("view_side_phone_num");
                 JSONObject contactObj = DuoquUtils.getSdkDoAction().getContactObj(mContext, callNumber);
                 String contentColorStr = ContentUtil.COR_WHITE;
                 String name =null;
                 if(contactObj == null){
                     name = callNumber;
                 }else{
                	 Object conObj=JsonUtil.getValFromJsonObject(contactObj, UIConstant.CONTACT_TYPE);
                	 if(conObj!=null){
                		 Integer contactType=1;
                		 try {
                			 contactType=(Integer)conObj;
						} catch (Exception e) {
							// TODO: handle exception
							contactType=1;
						}
                     if(contactType != null && UIConstant.CONTCAT_TYPE_FRIEND != contactType && UIConstant.CONTCAT_TYPE_UNKNOW != contactType){
                         contentColorStr = ContentUtil.COR_RED;
                     }
                     }else{
                    	 contentColorStr = ContentUtil.COR_WHITE;
                     }
                     name = (String)JsonUtil.getValFromJsonObject(contactObj, UIConstant.CONTACT_NAME);
                 }
                 msg.putValue(CALL_NUMBER_TEXT_COLOR, contentColorStr);
                 msg.putValue(CALL_NUMBER_TEXT, name);
//                 android.util.Log.d("duoqu_test", "msgId: "+msg.smsId+" COLOR: "+contentColorStr+" name: "+name);
                return msg;
            }
             @Override
            protected void onPostExecute(Object result) {
               if(mMessage ==  result){
                   bindCallUserViewValue((String)mMessage.getValue(CALL_NUMBER_TEXT),(String)mMessage.getValue(CALL_NUMBER_TEXT_COLOR),(String) mMessage.getValue("view_side_phone_num"));
               }
            }
        };
        task.execute(mMessage);
    }
    
    private void bindCallUserViewValue(String callNumberName,String callNumberColor,String callNumber){
    	if(StringUtils.isNull(callNumberName)||StringUtils.isNull(callNumber)||callNumber.equals(callNumberName)){
    		mCallUserNameTextView.setVisibility(View.GONE);
    		mCallUserNameTextViewlable.setVisibility(View.GONE);
    	}else{
        setLableVisibility(mCallUserNameTextView, mCallUserNameTextViewlable, callNumberName,callNumberColor,true);
        }
    }
    public void setLableVisibility(TextView t1, TextView t2, String value, String color,boolean isShow) {

        if (isShow || (!StringUtils.isNull(value) && mVisibleCount < 4)) {
                mVisibleCount++;
                t1.setVisibility(View.VISIBLE);
                t2.setVisibility(View.VISIBLE);
                ContentUtil.setText(t1, value, null);
                ContentUtil.setTextColor(t1, color);
            } else {
                t1.setVisibility(View.GONE);
                t2.setVisibility(View.GONE);
            }
//        }
       
    }
}
