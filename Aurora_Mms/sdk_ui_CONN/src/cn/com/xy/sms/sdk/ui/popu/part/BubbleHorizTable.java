package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquHorizItemTable;

public class BubbleHorizTable extends UIPart {

	private DuoquHorizItemTable mContentListView;
	
	String bgColor = null;//背景颜色

	private static final String TABLE_KEY = "duoqu_table_data_horiz";

	public BubbleHorizTable(Activity mContext, BusinessSmsMessage message,
			XyCallBack callback, int layoutId, ViewGroup root, int partId) {
		super(mContext, message, callback, layoutId, root, partId);

	}

	@Override
	public void initUi() {
		mContentListView = (DuoquHorizItemTable) mView
				.findViewById(R.id.duoqu_horiz_list);

	}

	@Override
	public void setContent(BusinessSmsMessage message, boolean isRebind)
			throws Exception {
		this.mMessage = message;
		if (message == null) {
			return;
		}
		int size = message.getTableDataSize(TABLE_KEY);
		ViewManger.setViewBg(mContext, mView, message.getImgNameByKey("v_by_bg"), R.drawable.duoqu_rectangledrawble, 1,true);
		mContentListView.setContentList(message, size, TABLE_KEY, isRebind);
	}

	@Override
	public void destroy() {
		super.destroy();
	}
	//
	// @Override
	// public void changeData(Map<String, Object> param) {
	// super.changeData(param);
	// }
}
