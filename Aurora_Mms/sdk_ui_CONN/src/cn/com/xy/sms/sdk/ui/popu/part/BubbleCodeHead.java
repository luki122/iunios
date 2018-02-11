package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.Map;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ResourceCacheUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquRelativeLayout;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BubbleCodeHead extends UIPart {

	private ImageView mLogoImageView = null;
	private TextView mTitleTextView = null;
	private ImageView mDashedline_i = null;
	private ImageView mDuoquCodeHeadLine = null;
	private DuoquRelativeLayout mCodeBubbleHead;
	private final String TAG = "BubbleCodeHead";

	public BubbleCodeHead(Activity context, BusinessSmsMessage message,
			XyCallBack callback, int layoutId, ViewGroup root, int partId) {
		super(context, message, callback, layoutId, root, partId);
	}

	@Override
	public void initUi() throws Exception {
		mLogoImageView = (ImageView) super.mView
				.findViewById(R.id.duoqu_code_head_logo);
		mTitleTextView = (TextView) super.mView
				.findViewById(R.id.duoqu_code_head_title);
		mCodeBubbleHead = (DuoquRelativeLayout) super.mView
				.findViewById(R.id.duoqu_code_bubble_head);
		mDuoquCodeHeadLine = (ImageView) super.mView
				.findViewById(R.id.duoqu_code_head_line);
		mDashedline_i = (ImageView) super.mView.findViewById(R.id.dashedline_i);
	}

	@Override
	public void setContent(BusinessSmsMessage message, boolean isRebind)
			throws Exception {
		this.mMessage = message;
		if (message == null) {
			LogManager.e(TAG, "setContent message is null",null);
			return;
		}

		String logoName = (String) message.getImgNameByKey("v_hd_logo");
		if (!StringUtils.isNull(logoName)) {
			ContentUtil.setViewVisibility(mLogoImageView, View.VISIBLE);
			ViewUtil.setImageSrc(mContext, mLogoImageView, logoName,
					message.viewType == 1);
		} else {
			ContentUtil.setViewVisibility(mLogoImageView, View.GONE);
		}

		ContentUtil.setText(mTitleTextView,
				(String) message.getValue("view_title_name"), null);
		
		String headTextColor=(String)message.getImgNameByKey("v_hd_color");
		if (!StringUtils.isNull(headTextColor)) {
            ContentUtil.textSetColor(mTitleTextView, headTextColor);
        }else {
            mTitleTextView.setTextColor(ResourceCacheUtil.parseColor("#EDEDED"));
        }

		String headBg = (String) message.getImgNameByKey("v_hd_bg");
		int resId = -1;

		resId = R.drawable.duoqu_top_rectangle;
		ViewManger.setViewBg(Constant.getContext(), mCodeBubbleHead, headBg,
				resId, -1, true);

	}

	@Override
	public void changeData(Map<String, Object> param) {
		if (param == null || !param.containsKey("showDashing")) {
			return;
		}
		if (mView == null) {
			return;
		}
		boolean isshow = param.containsKey("showDashing") ? (Boolean) param
				.get("showDashing") : true;

		if (!isshow) {
			mDuoquCodeHeadLine.setVisibility(View.GONE);
		} else {
			mDuoquCodeHeadLine.setVisibility(View.VISIBLE);
		}

	}

}
