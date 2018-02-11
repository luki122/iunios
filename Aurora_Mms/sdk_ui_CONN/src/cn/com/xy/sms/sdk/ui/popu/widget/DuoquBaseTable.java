package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public abstract class DuoquBaseTable extends RelativeLayout {
	protected int mChildId = 0;
	protected List<ViewHolder> mViewHolderList = null;
	Context context;

	public DuoquBaseTable(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initParams(context, attrs);
	}

	public class ViewHolder {
		public TextView titleView;
		public TextView contentView;
		public RelativeLayout mlineGroup;

		public ViewHolder() {

		}

		public void setContent(int pos, BusinessSmsMessage message, String dataKey, boolean isReBind) {
			try {
				JSONObject jsobj = (JSONObject) message.getTableData(pos, dataKey);
				String titleText = (String) JsonUtil.getValFromJsonObject(jsobj, "t1");
				String contentText = (String) JsonUtil.getValFromJsonObject(jsobj, "t2");
				titleView.setText(titleText);
				contentView.setText(contentText);
				setViewStyle(jsobj, message);
			} catch (Throwable ex) {
				LogManager.e("xiaoyuan", ex.getMessage(), ex);
			}
		}

		private void setViewStyle(JSONObject jsobj, BusinessSmsMessage message) {
			try {
				// set text color from parseUtilBubble
				String titleColor = (String) message.getImgNameByKey("v_by_l_color");
				String contentColor = (String) message.getImgNameByKey("v_by_r_color");
				if (!StringUtils.isNull(titleColor)) {
					ContentUtil.textSetColor(titleView, titleColor);
				} else {
					titleView.setTextColor(context.getResources().getColor(R.color.duoqu_calls_l));
				}
				if (!StringUtils.isNull(contentColor)) {
					ContentUtil.textSetColor(contentView, contentColor);
				} else {
					contentView.setTextColor(context.getResources().getColor(R.color.duoqu_calls_r));
				}

				// set text size from parseUtilBubble
				String titleSize = (String) JsonUtil.getValFromJsonObject(jsobj, "s1");
				String contentSize = (String) JsonUtil.getValFromJsonObject(jsobj, "s2");
				if (!StringUtils.isNull(titleSize)) {
					titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Float.parseFloat(titleSize));
				}
				if (!StringUtils.isNull(contentSize)) {
					contentView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Float.parseFloat(contentSize));
				} else {
					Object defaultTextSizeObj = contentView.getTag(R.id.tag_default_content_text_size);
					if (defaultTextSizeObj != null) {
						contentView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
								Float.parseFloat(defaultTextSizeObj.toString()));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void setVisibility(int visibility) {
			try {
				if (mlineGroup != null) {
					mlineGroup.setVisibility(visibility);
				}
				titleView.setVisibility(visibility);
				contentView.setVisibility(visibility);
				if (contentView.getTag(R.id.tag_parent_layout) != null) {
					((RelativeLayout) contentView.getTag(R.id.tag_parent_layout)).setVisibility(visibility);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void setContentList(BusinessSmsMessage message, int dataSize, String dataKey, boolean isRebind) {
		try {
			if (dataSize == 0) {
				this.setVisibility(View.GONE);
				return;
			}
			this.setVisibility(View.VISIBLE);
			final List<ViewHolder> holderList = mViewHolderList;
			if (isRebind && holderList != null) {
				int holderSize = holderList.size();
				int diffSize = holderSize - dataSize;
				ViewHolder tempHolder = null;
				if (diffSize > 0) {
					for (int i = 0; i < holderSize; i++) {
						tempHolder = holderList.get(i);
						if (i < dataSize) {
							tempHolder.setVisibility(View.VISIBLE);
							tempHolder.setContent(i, message, dataKey, isRebind);
						} else {
							tempHolder.setVisibility(View.GONE);
						}
					}
				} else {
					for (int i = 0; i < dataSize; i++) {
						if (i < holderSize) {
							tempHolder = holderList.get(i);
							tempHolder.setVisibility(View.VISIBLE);
							tempHolder.setContent(i, message, dataKey, isRebind);
						} else {
							getHolder(i, message, dataSize, dataKey, false);
						}
					}
				}
				return;
			}
			mViewHolderList = new ArrayList<ViewHolder>();
			for (int i = 0; i < dataSize; i++) {
				getHolder(i, message, dataSize, dataKey, false);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Set the location of view
	 */
	protected abstract RelativeLayout.LayoutParams getLayoutParams(int childId);

	/**
	 * get custom params
	 */
	protected abstract void initParams(Context context, AttributeSet attrs);

	/**
	 * create and set TextView
	 * 
	 * @param pos
	 * @param message
	 * @param dataSize
	 * @param dataKey
	 */
	protected abstract void getHolder(int pos, BusinessSmsMessage message, int dataSize, String dataKey,
			boolean isRebind);

}
