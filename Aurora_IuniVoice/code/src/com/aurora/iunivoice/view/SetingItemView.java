package com.aurora.iunivoice.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.utils.DensityUtil;
import com.aurora.iunivoice.widget.AuroraSwitch;

/**
 * Created by lmjssjj on 2015/10/25.
 */
public class SetingItemView extends FrameLayout implements
		View.OnClickListener, OnCheckedChangeListener {

	private View mRootView;
	private ImageView iv_icon;
	private ImageView iv_right_icon;
	private TextView tv_info;
	private TextView tv_right_info;
	private TextView tv_news;
	private AuroraSwitch s_toggle;
	private View mDivider;

	private boolean mBDivider;
	private boolean mBToggle;
	private boolean mBNews;
	private boolean mBIcon;
	private boolean mBRightIcon;
	private boolean mBRightInfo;
	
	private float mInfoSize;
	private int mInfoMarginLeft;
	

	private Drawable mIconDrawble;
	private Drawable mRigthIconDrawble;
	private String mInfo;
	private String mNews;
	private String mRightInfo;

	private OnSettingItemClickListener listener;
	private OnSwithToggleListener switchListener;

	public SetingItemView(Context context) {
		this(context, null);
	}

	public SetingItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SetingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);

		setClickable(true);
		setOnClickListener(this);

		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.SettingItem);
		
		mInfoMarginLeft = (int) ta.getDimensionPixelSize(R.styleable.SettingItem_infoMarginLeft,-1);
		if(mInfoMarginLeft!=-1){
			setInfoTextMarginLeft(context,mInfoMarginLeft);
		}
		mInfoSize = ta.getDimension(R.styleable.SettingItem_infoSize,-1);
		if(mInfoSize!=-1){
			setInfoTextSize(mInfoSize);
		}
		
		mIconDrawble = ta.getDrawable(R.styleable.SettingItem_s_icon);
		mRigthIconDrawble = ta.getDrawable(R.styleable.SettingItem_right_icon);

		mInfo = ta.getString(R.styleable.SettingItem_info);
		mNews = ta.getString(R.styleable.SettingItem_news);
		mRightInfo = ta.getString(R.styleable.SettingItem_right_info);

		mBIcon = ta.getBoolean(R.styleable.SettingItem_s_icon_isShow, true);
		setIconVisiable(mBIcon ? View.VISIBLE : View.GONE);

		mBRightIcon = ta.getBoolean(R.styleable.SettingItem_right_icon_isShow,
				false);
		setRightIconVisiable(mBRightIcon ? View.VISIBLE : View.GONE);

		mBRightInfo = ta.getBoolean(R.styleable.SettingItem_right_info_isShow,
				false);
		setRightInfoTextVisiable(mBRightInfo ? View.VISIBLE : View.GONE);

		mBNews = ta.getBoolean(R.styleable.SettingItem_news_isShow, false);
		setNewsVisiable(mBNews ? View.VISIBLE : View.GONE);

		mBToggle = ta.getBoolean(R.styleable.SettingItem_switch_isShow, false);
		setSwitchVisiable(mBToggle ? View.VISIBLE : View.GONE);

		mBDivider = ta.getBoolean(R.styleable.SettingItem_s_divider, true);
		setDividerVisiable(mBDivider ? View.VISIBLE : View.GONE);

		if (mIconDrawble != null) {
			setImageIconDrawble(mIconDrawble);
		}
		if (mRightInfo != null) {
			setRightInfoText(mRightInfo);
		}
		if (mRigthIconDrawble != null) {
			setImageRightIconDrawble(mRigthIconDrawble);
		}
		if (mInfo != null) {
			setInfoText(mInfo);
		}
		if (mNews != null) {
			setNewsText(mNews);
		}

		ta.recycle();
	}

	private void init(Context context) {
		mRootView = View.inflate(context, R.layout.settiing_item, this);
		iv_icon = (ImageView) mRootView.findViewById(R.id.iv_icon);
		iv_right_icon = (ImageView) mRootView.findViewById(R.id.iv_right_icon);
		tv_info = (TextView) mRootView.findViewById(R.id.tv_info);
		tv_right_info = (TextView) mRootView.findViewById(R.id.tv_right_info);
		tv_news = (TextView) mRootView.findViewById(R.id.tv_news);
		mDivider = mRootView.findViewById(R.id.v_divider);
		s_toggle = (AuroraSwitch) mRootView.findViewById(R.id.s_toggle);

		s_toggle.setOnClickListener(this);
		s_toggle.setOnCheckedChangeListener(this);
	}

	/*----------设置文本信息------------*/
	public void setInfoText(String info) {
		tv_info.setText(info);
	}

	public void setNewsText(String newsText) {
		tv_news.setText(newsText);
	}

	public void setRightInfoText(String info) {
		tv_right_info.setText(info);
	}

	public void setNewsVisiable(int i) {
		tv_news.setVisibility(i);
	}

	public void setRightInfoTextVisiable(int i) {
		tv_right_info.setVisibility(i);
	}

	/*----------设置图标显示------------*/
	public void setIconVisiable(int i) {
		iv_icon.setVisibility(i);
	}

	public void setRightIconVisiable(int i) {
		iv_right_icon.setVisibility(i);
	}

	public void setDividerVisiable(int i) {
		mDivider.setVisibility(i);
	}

	public void setSwitchVisiable(int i) {
		s_toggle.setVisibility(i);
	}
	
	//==========================================
	public void setInfoTextSize(float size){
		tv_info.setTextSize(size);
	}
	public void setInfoTextMarginLeft(Context context ,int left){
		LayoutParams layoutParams = (LayoutParams) tv_info.getLayoutParams();
		layoutParams.setMargins(left, 0, 0, 0);
		tv_info.setLayoutParams(layoutParams);
	}
	/*----------------------*/
	public void setImageIconDrawble(Drawable d) {
		iv_icon.setImageDrawable(d);
	}

	public void setImageRightIconDrawble(Drawable d) {
		iv_right_icon.setImageDrawable(d);
	}

	public void setImageIconResource(int resId) {
		iv_icon.setImageResource(resId);
	}

	public void setImageRightIconResource(int resId) {
		iv_right_icon.setImageResource(resId);
	}

	// ==========================================================

	public interface OnSettingItemClickListener {
		void onItemClickListener(View v);
	}

	public interface OnSwithToggleListener {
		void onToggle(CompoundButton v, boolean isChecked);
	}

	// ===============事件=========================================
	public void setOnSettingItemClickListener(
			OnSettingItemClickListener listener) {
		this.listener = listener;
	}

	public void setOnSwithToggleListener(OnSwithToggleListener listener) {
		this.switchListener = listener;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (switchListener != null) {
			switchListener.onToggle(buttonView, isChecked);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.s_toggle) {
			
		} else {
			if (listener != null)
				listener.onItemClickListener(v);
		}
	}
}
