package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.Map;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ResourceCacheUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class CardBodySplit extends UIPart {
    private TextView mCallName = null;
    private ImageView mTelCard = null;
    private TextView mCallTime = null;
    private ViewGroup mBodyView = null;
    private int mNormalHeight = -1;
    private int mMinHeight = -1;

    public CardBodySplit(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    @Override
    public void initUi() throws Exception {
        mBodyView = (ViewGroup) super.mView.findViewById(R.id.duoqu_card_body_bottom);
        mCallName = (TextView) super.mView.findViewById(R.id.duoqu_call_name);
        mTelCard = (ImageView) super.mView.findViewById(R.id.duoqu_tel_card);
        mCallTime = (TextView) super.mView.findViewById(R.id.duoqu_call_time);
        mNormalHeight = Math.round(mContext.getResources().getDimension(R.dimen.card_body_split_height));
        mMinHeight = Math.round(mContext.getResources().getDimension(R.dimen.card_body_split_minheight));
    }

    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        super.mMessage = message;
        if (message == null) {
            return;
        }

        int simIndex = message.simIndex;
        String simName = message.simName;
        ViewUtil.setTextViewValue(mCallTime, ContentUtil.getTimeText(Constant.getContext(), message.msgTime));
        ViewUtil.setTextViewValue(mCallName, simName);
        int color = ResourceCacheUtil.parseColor(message.getImgNameByKey("v_by_bt_bg"));
        if (color != 0) {
        	mBodyView.setBackgroundColor(color);
        }
        
        String spcolor=(String)message.getImgNameByKey("v_bt_by_sp_color");
        if (!StringUtils.isNull(spcolor)) {
            ContentUtil.textSetColor(mCallName,spcolor);
            ContentUtil.textSetColor(mCallTime,spcolor);
        }else {
			mCallName.setTextColor(mView.getResources().getColor(
					R.color.duoqu_white_transparent_40));
			mCallTime.setTextColor(mView.getResources().getColor(R.color.duoqu_white_transparent_40));
        }

        String drawable = null;
        switch (simIndex) {
        case 0:
        	drawable=getCardNumLogo(message,0);
            break;
        case 1:
        	drawable=getCardNumLogo(message,1);
            break;
        default:
            drawable = null;
            break;
        }
        if (drawable != null) {
        	ViewManger.setViewBg(Constant.getContext(), mTelCard,
        			drawable, R.drawable.duoqu_bottom_rectangle, -1);
        } else {
            mTelCard.setVisibility(View.GONE);
        }
    }

	private String getCardNumLogo(BusinessSmsMessage message, int simIndex) {
		if (simIndex == 0) {
			String cardnumLogo = (String) message
					.getImgNameByKey("v_tc_card_fir");
			if (!StringUtils.isNull(cardnumLogo)) {
				return cardnumLogo;
			}
		} else {
			String cardnumLogo = (String) message
					.getImgNameByKey("v_tc_card_sec");
			if (!StringUtils.isNull(cardnumLogo)) {
				return cardnumLogo;
			}
		}
		return null;
	}

	@Override
    public void changeData(Map<String, Object> param) {
        if (param == null || !param.containsKey("buttonNum")) {
            return;
        }
        if (mView == null) {
            return;
        }
        ViewGroup.LayoutParams lp = mView.getLayoutParams();
        if (lp == null) {
            return;
        }
        int buttonNum = 0;
        try {
            buttonNum = (Integer) param.get("buttonNum");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (buttonNum > 0) {
            if (mNormalHeight != -1) {
                // 有按钮时使用默认高度
                lp.height = mNormalHeight;
            }
        } else {
            if (mMinHeight != -1) {
                // 无按钮时使用最小高度
                lp.height = mMinHeight;
            }
        }
        mView.setLayoutParams(lp);
    }
    
    @Override
    public void destroy() {
        super.destroy();
    }
}
