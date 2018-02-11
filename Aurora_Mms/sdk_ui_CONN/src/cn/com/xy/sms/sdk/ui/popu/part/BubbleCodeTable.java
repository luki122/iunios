package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BubbleCodeTable extends UIPart {

	private TextView mDuoquBaseCode;
	private TextView mDuoquBaseCodeTime;

	public BubbleCodeTable(Activity context, BusinessSmsMessage message,
			XyCallBack callback, int layoutId, ViewGroup root, int partId) {
		super(context, message, callback, layoutId, root, partId);
	}

	@Override
	public void initUi() throws Exception {
		mDuoquBaseCode = (TextView) super.mView
				.findViewById(R.id.duoqu_base_code);
		mDuoquBaseCodeTime = (TextView) super.mView
				.findViewById(R.id.duoqu_base_code_time);
	}

	@Override
	public void setContent(BusinessSmsMessage message, boolean isRebind)
			throws Exception {
		this.mMessage = message;
		bindCodeInfo(message);
	}

	private void bindCodeInfo(BusinessSmsMessage message) {

		showSelectTime(false);
		ContentUtil.setText(mDuoquBaseCode,
				(String) message.getValue("view_call"), null);

		String mCodeTime = (String) message.getValue("view_call_time");
		if (!StringUtils.isNull(mCodeTime)) {
			ContentUtil.setText(mDuoquBaseCodeTime, "有效时间：" + mCodeTime, null);
			showSelectTime(true);
		} else {
			// LinearLayout.LayoutParams layoutParams = new
			// LinearLayout.LayoutParams(
			// ViewGroup.LayoutParams.WRAP_CONTENT,
			// ViewGroup.LayoutParams.WRAP_CONTENT);
			// layoutParams.setMargins(0, 0, 0, 20);
			// mDuoquBaseCode.setLayoutParams(layoutParams);
		}

	}

	private void showSelectTime(boolean show) {
		ContentUtil.setViewVisibility(mDuoquBaseCodeTime, (show ? View.VISIBLE
				: View.GONE));
	}

}
