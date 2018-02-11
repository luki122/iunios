package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.BottomButtonUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ResourceCacheUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SimpleButtonUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquRelativeLayout;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BubbleBottomTwo extends UIPart {

    private boolean mDisLogo = false; // Whether the display button logo
    private View mDuoqu_bottom_split_line, mDuoqu_btn_split_line;// The separation column and column line
    private View mBtn1 = null, mBtn2 = null; // Button 1, button 2
    private TextView mTextView1 = null, mTextView2 = null;
    private int mNormalHeight = -1;
    private int mMinHeight = -1;
    private DuoquRelativeLayout mDlayout;
    private int mSize = 0;// The number of buttons

    public int getSize() {
        return mSize;
    }

    private static final int FIRST_TEXTVIEW_PADDING_TOP = (int) ViewUtil
            .getDimension(R.dimen.duoqu_first_textview_padding_bottom);
    private static final int SECOND_TEXTVIEW_PADDING_TOP = (int) ViewUtil
            .getDimension(R.dimen.duoqu_first_textview_padding_bottom);

    public BubbleBottomTwo(Activity mContext, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(mContext, message, callback, layoutId, root, partId);

    }

    @Override
    public void initUi() throws Exception {
        mNormalHeight = Math.round(mContext.getResources().getDimension(R.dimen.bubble_bottom_two_height));
        mMinHeight = Math.round(mContext.getResources().getDimension(R.dimen.bubble_bottom_two_minheight));
        mDuoqu_bottom_split_line = mView.findViewById(R.id.duoqu_bottom_split_line);
        mDuoqu_btn_split_line = mView.findViewById(R.id.duoqu_btn_split_line);
        mDlayout = (DuoquRelativeLayout) mView.findViewById(R.id.duoqu_bubble_bottom_two);
        mBtn1 = mView.findViewById(R.id.duoqu_btn_1);
        mBtn2 = mView.findViewById(R.id.duoqu_btn_2);
        mTextView1 = (TextView) mView.findViewById(R.id.duoqu_btn_text_1);
        mTextView2 = (TextView) mView.findViewById(R.id.duoqu_btn_text_2);
    }

    public void setButtonTextAndImg(TextView buttonText, String action, boolean disLogo) {
        try {
            String buttonName = buttonText.getText().toString();
            boolean setText = StringUtils.isNull(buttonName);

            int resLogoId = SimpleButtonUtil.bindButtonData(buttonText, action, setText, true);

            if (disLogo && resLogoId != -1) {
                Drawable dw = Constant.getContext().getResources().getDrawable(resLogoId);
                dw.setBounds(0, ViewUtil.dp2px(mContext, 1), ViewUtil.dp2px(mContext, 24), ViewUtil.dp2px(mContext, 24));
                buttonText.setCompoundDrawables(dw, null, null, null);
            } else {
                buttonText.setCompoundDrawables(null, null, null, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                // Processing action
                JSONObject jsonObject = (JSONObject) v.getTag();
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("simIndex", mMessage.simIndex + "");
                valueMap.put("phoneNum", mMessage.originatingAddress + "");
                valueMap.put("content", mMessage.getMessageBody() + "");
                byte viewType = mMessage.viewType;
                valueMap.put("viewType", viewType + "");
                JsonUtil.putJsonToMap(jsonObject, valueMap);
                String action_data = (String) JsonUtil.getValueFromJsonObject(jsonObject, "action_data");
                DuoquUtils.doAction(mContext, action_data, valueMap);
            } catch (Exception e) {
                if (LogManager.debug) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void setBotton(View button, final TextView buttonText, final JSONObject actionMap, boolean disLogo) {
        if (actionMap == null) {
            return;
        }

        final String action = (String) JsonUtil.getValueFromJsonObject(actionMap, "action");
        String btnName = ContentUtil.getBtnName(actionMap);
        if (!StringUtils.isNull(btnName)) {
            buttonText.setText(btnName);
            setButtonTextAndImg(buttonText, action, disLogo);
        }
        if (!StringUtils.isNull(action)) {
            button.setTag(actionMap);
            button.setOnClickListener(mOnClickListener);
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;

        if (!isRebind) {
            mTextView1.setPadding(0, 0, 0, FIRST_TEXTVIEW_PADDING_TOP);
            mTextView2.setPadding(0, 0, 0, SECOND_TEXTVIEW_PADDING_TOP);
        }
        
        
        
        setBgColor((String) message.getValue("v_bt_bg"));
        String strBtnTextColor = (String) message.getValue("v_bt_color");
        int btnTextColor = -1;
        if (!StringUtils.isNull(strBtnTextColor)) {
            btnTextColor = ResourceCacheUtil.parseColor(strBtnTextColor);
        }else {
            //btnTextColor = ResourceCacheUtil.parseColor("#a3a3a3");
        }

        JSONArray actionArr = message.getActionJsonArray();

        if (actionArr == null) {
            mSize = 0;
        } else {
            mSize = actionArr.length();
        }
        switch (mSize) {
        case 0:
        	mDuoqu_bottom_split_line.setVisibility(View.GONE);
            setButtonViewVisibility(View.GONE, View.GONE, View.GONE);
            break;
        case 1:
        	mDuoqu_bottom_split_line.setVisibility(View.VISIBLE);
        	
        	getLine(message);
            setButtonViewVisibility(View.VISIBLE, View.GONE, View.GONE);
            setButtonView(mBtn1, mTextView1, btnTextColor, actionArr.getJSONObject(0));
            break;
        case 2:
        	mDuoqu_bottom_split_line.setVisibility(View.VISIBLE);
        	
        	getLine(message);
            setButtonViewVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE);
            setButtonView(mBtn1, mTextView1, btnTextColor, actionArr.getJSONObject(1));
            setButtonView(mBtn2, mTextView2, btnTextColor, actionArr.getJSONObject(0));

            break;
        }
        changeCardBodySplitHeight();
    }

	private void getLine(BusinessSmsMessage message) throws Exception {
		String duoquSplitLine=message.getImgNameByKey("v_bt_body_split_bg");
		if (!StringUtils.isNull(duoquSplitLine)) {
			ViewManger.setViewBg(Constant.getContext(), mDuoqu_bottom_split_line,
					duoquSplitLine, 0, -1);
		}
	}

    /**
     * 动态设置CardBodySplit高度
     */
    private void changeCardBodySplitHeight() {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("buttonNum", mSize);
        param.put("type", 2);
        if (mBasePopupView != null) {
            mBasePopupView.changeData(param);
        }
    }

    /**
     * 设置背景颜色，颜色值为null或空时以#E6E6E6代替
     * 
     * @param color
     */
    private void setBgColor(String color) {
        if (StringUtils.isNull(color)) {
            //color = "#E6E6E6";
        }
        try {
            ViewManger.setViewBg(mContext, mView, color, R.drawable.duoqu_bottom_rectangle, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setButtonViewVisibility(int button1Visible, int button2Visible, int splitLineVisible) throws Exception {
        ContentUtil.setViewVisibility(mDuoqu_btn_split_line, splitLineVisible);
        ContentUtil.setViewVisibility(mBtn1, button1Visible);
        ContentUtil.setViewVisibility(mBtn2, button2Visible);
        ContentUtil.setViewVisibility(mTextView1, button1Visible);
        ContentUtil.setViewVisibility(mTextView2, button2Visible);
        ViewGroup.LayoutParams lp = mView.getLayoutParams();
        if (button1Visible == View.VISIBLE) {
            if (lp != null) {
                if (mNormalHeight != -1) {
                    lp.height = mNormalHeight;// normal high
                    mView.setLayoutParams(lp);
                }
            }
        } else {
            if (lp != null) {
                if (mMinHeight != -1) {
                    lp.height = mMinHeight;// Minimum high
                    mView.setLayoutParams(lp);
                }
            }

            setBgColor((String) mMessage.getImgNameByKey("v_by_bg"));
        }
    }

    private void setButtonView(View buttonView, TextView textView, int color, JSONObject action) throws JSONException {
        setBotton(buttonView, textView, action, mDisLogo);
        if (color != -1) {
            textView.setTextColor(color);
        }
    }

    public void setLayoutParam() {
        ViewGroup.LayoutParams lp = mView.getLayoutParams();
        if (lp == null) {
            return;
        }
        if (mSize == 0) {
            if (mMinHeight != -1) {
                lp.height = mMinHeight;
            }
        } else {
            if (mNormalHeight != -1) {
                lp.height = mNormalHeight;
            }
        }
        mView.setLayoutParams(lp);
    }

    @Override
    public void destroy() {
        //ViewUtil.recycleViewBg(mDuoqu_bottom_split_line);
        ViewUtil.recycleViewBg(mView);
        super.destroy();
    }
}
