package cn.com.xy.sms.sdk.ui.popu.widget;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.util.JsonUtil;

/**
 * Two fonts level relationship in a Item ---------------- XXXXX XXXXXX
 * ---------------- XXXXX XXXXXX ---------------- . . .
 * 
 * @author Administrator
 *
 */
public class DuoquHorizItemTable extends DuoquBaseTable {

	private int mTitleSize = 0;
	private int mContentSize = 0;
	// private int mTitleColor = 0;
	private int mTitlePaddingTop = 0;
	private int mContentPaddingLeft = 0;
	private int mLineSpacing = 0;
	private int mMarginTop = 0;
	private String mSingleLine = null;
	private int mLayoutWidth = 0;
	private int mMarginLeft = 0;
	private int mMarginRight = 0;
	private int mMarginBottom = 0;
	private int mLayoutId = 1000;
	public DuoquHorizItemTable(Context context, AttributeSet attrs) {
		super(context, attrs);
		initParams(context, attrs);
	}

	@Override
	protected void initParams(Context context, AttributeSet attrs) {
		TypedArray duoquTbAttr = context.obtainStyledAttributes(attrs,
				R.styleable.duoqu_table_attr);
		mTitleSize = Math.round(duoquTbAttr.getDimension(
				R.styleable.duoqu_table_attr_title_textsize, 0));
		mContentSize = Math.round(duoquTbAttr.getDimension(
				R.styleable.duoqu_table_attr_content_textsize, 0));
		// mTitleColor =
		// duoquTbAttr.getResourceId(R.styleable.duoqu_table_attr_title_textcolor,
		// 0);
		mTitlePaddingTop = Math.round(duoquTbAttr.getDimension(
				R.styleable.duoqu_table_attr_title_paddingtop, 0));
		mContentPaddingLeft = Math.round(duoquTbAttr.getDimension(
				R.styleable.duoqu_table_attr_content_paddingleft, 0));
		mLineSpacing = Math.round(duoquTbAttr.getDimension(
				R.styleable.duoqu_table_attr_line_spacing, 0));
		mSingleLine = duoquTbAttr
				.getString(R.styleable.duoqu_table_attr_single_line);
		mMarginTop = Math.round(duoquTbAttr.getDimension(
				R.styleable.duoqu_table_attr_margin_top, 0));
		mMarginLeft = Math.round(duoquTbAttr.getDimension(
				R.styleable.duoqu_table_attr_margin_left, 0));
		mMarginRight = Math.round(duoquTbAttr.getDimension(
				R.styleable.duoqu_table_attr_margin_right, 0));
		mMarginBottom = Math.round(duoquTbAttr.getDimension(
				R.styleable.duoqu_table_attr_margin_bottom, 0));

		mLayoutWidth = (int) context.getResources().getDimension(
				R.dimen.duoqu_custom_layout_width);
		duoquTbAttr.recycle();
		if (mMarginTop != 0 || mMarginLeft != 0 || mMarginBottom != 0
				|| mMarginRight != 0) {
			// the row width and height
			RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			rp.setMargins(mMarginLeft, mMarginTop, mMarginRight, mMarginBottom);
			this.setLayoutParams(rp);
		}
	}

	@Override
	protected void getHolder(int pos, BusinessSmsMessage message, int dataSize,
			String dataKey, boolean isRebind) {
		ViewHolder holder = new ViewHolder();
		holder.titleView = new TextView(this.getContext());
		holder.contentView = new TextView(this.getContext());
		holder.mlineGroup=new RelativeLayout(this.getContext());
	    holder.mlineGroup.setId(++mLayoutId);
		holder.titleView.setId(++mChildId);
		RelativeLayout.LayoutParams leftParam = getLayoutParams(mChildId,
				mLayoutWidth);
		 holder.mlineGroup.addView(holder.titleView, leftParam);
        
		holder.contentView.setId(++mChildId);
		RelativeLayout.LayoutParams rightParam = getLayoutParams(mChildId, 0);
		 holder.mlineGroup.addView(holder.contentView, rightParam);

		holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
		holder.contentView
				.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContentSize);
	    this.addView( holder.mlineGroup, getRelativeLayoutParams(mLayoutId));
		if (mTitlePaddingTop > 0) {
			holder.titleView.setPadding(0, mTitlePaddingTop, 0, 0);
		}

		if (mContentPaddingLeft > 0 || mTitlePaddingTop > 0) {
			holder.contentView.setPadding(mContentPaddingLeft,
					mTitlePaddingTop, 0, 0);
		}

		if ("true".equals(mSingleLine)) {
			// holder.contentView.setSingleLine();
			holder.contentView.setSingleLine();
			holder.contentView
					.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
		}
		holder.setContent(pos, message, dataKey, isRebind);
		mViewHolderList.add(holder);
	}
	private LayoutParams getRelativeLayoutParams(int viewId){
	        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
	                LayoutParams.WRAP_CONTENT);
	        if (viewId > 1001) {
	            int prevLayoutId = viewId - 1;
	            params.addRule(RelativeLayout.BELOW, prevLayoutId);
	            params.setMargins(0, mLineSpacing, 0, 0);
	        }
	        return params;
	    }
	public RelativeLayout.LayoutParams getLayoutParams(int childId,
			int customLayoutWidth) {
		RelativeLayout.LayoutParams params = null;
		if (customLayoutWidth != 0) {
			params = new RelativeLayout.LayoutParams(customLayoutWidth,
					LayoutParams.WRAP_CONTENT);
		} else {
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}
		if (childId == 1) {
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.setMargins(0, mLineSpacing, 0, 0);// mLineSpacing
																// 0
		}

		if (childId % 2 != 0) {// Singular
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		} else {// dual
			params.addRule(RelativeLayout.RIGHT_OF, childId - 1);
			params.addRule(RelativeLayout.ALIGN_TOP, childId - 1);
		}
		return params;
	}

	@Override
	protected LayoutParams getLayoutParams(int childId) {
		// TODO Auto-generated method stub
		return null;
	}
}
