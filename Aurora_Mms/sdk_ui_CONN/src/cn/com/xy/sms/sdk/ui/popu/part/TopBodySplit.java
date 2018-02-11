package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.Map;
import org.json.JSONArray;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;

public class TopBodySplit extends UIPart {

    private int mHeight = 0;

    public TopBodySplit(Activity context, BusinessSmsMessage message,
            XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    @Override
    public void initUi() throws Exception {
        try {
            super.initUi();
            mHeight = ViewManger.getIntDimen(Constant.getContext(),
                    R.dimen.duoqu_type_split_lr_height_111);
            putParam("H", mHeight);
            putParam("MTPO", -mHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind)
            throws Exception {
        this.mMessage = message;
        JSONArray actionArr =message.getActionJsonArray();
        if(actionArr == null || actionArr.length() == 0){
            mView.setVisibility(View.GONE);
        }else{
            mView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void changeData(Map<String, Object> param) {
        // Hide the dotted line
        if(param == null){
            return;
        }
        boolean showdash = param.containsKey("showDashing") ? (Boolean) param.get("showDashing") : true;
        if (!showdash) {
            mView.setVisibility(View.GONE);
        } else {
            mView.setVisibility(View.VISIBLE);
        }
    }
}
