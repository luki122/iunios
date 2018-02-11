package com.aurora.market.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.market.R;

public class MainTabItemView extends FrameLayout {
	
	private ImageView iv_icon;
	private TextView tv_text;
	private TextView tv_text_horizontal;
	
	private int paddingTop = 0;
	private int paddingTopHor = 0;
	private int paddingTopHorText = 0;
	private int marginLeftHorText = 0;
	private int transLeft = 0;
	
	private float fast = 0.0f;

	public MainTabItemView(Context context) {
		super(context);
	}

	public MainTabItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initView();
		
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.main_tab_item);
		
		int imgSrc = mTypedArray.getResourceId(R.styleable.main_tab_item_tab_img, R.drawable.ic_launcher);
		int textSrc = mTypedArray.getResourceId(R.styleable.main_tab_item_tab_text, R.string.app_name);
		int texthorSrc = mTypedArray.getResourceId(R.styleable.main_tab_item_tab_text_hor, R.string.app_name);
		
		iv_icon.setImageResource(imgSrc);
		tv_text.setText(textSrc);
		tv_text_horizontal.setText(texthorSrc);

		mTypedArray.recycle();
	}
	
	private void initView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.view_main_tab_item, this);
		
		iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
		tv_text = (TextView) view.findViewById(R.id.tv_text);
		tv_text_horizontal = (TextView) view.findViewById(R.id.tv_text_horizontal);
		
		paddingTop = getResources().getDimensionPixelOffset(R.dimen.homepage_main_tab_item_padding_top); // 原20dp
		paddingTopHor = getResources().getDimensionPixelOffset(R.dimen.homepage_main_tab_item_hor_padding_top); // 原8.3dp
		paddingTopHorText = getResources().getDimensionPixelOffset(R.dimen.homepage_main_tab_item_hor_text_padding_top);
		marginLeftHorText = getResources().getDimensionPixelOffset(R.dimen.homepage_main_tab_item_hor_text_margin_left);
		transLeft = getResources().getDimensionPixelOffset(R.dimen.homepage_main_tab_item_trans_left);
	}
	
	/**
	 * @Title: setProgress
	 * @Description: 设置动画进度
	 * @param @param progress
	 * @return void
	 * @throws
	 */
	public void setProgress(float progress) {
		
		float fastProgress = 0;
		float fastEndValue = 1 - fast;
		if (progress <= fastEndValue) {
			fastProgress = progress / fastEndValue;
		} else {
			fastProgress = 1;
		}
		
		// 缩放图片(最大缩放为maxScale, 总进度的imgEndValue跑完进度)
		float imgProgress = 0;
		float imgEndValue = 0.8f - fast;
		if (progress <= imgEndValue) {
			imgProgress = (imgEndValue - progress) / imgEndValue;
		} else {
			imgEndValue = 0;
		}
		float maxScale = 0.8f;
		iv_icon.setScaleX((maxScale + imgProgress * (1 - maxScale)));
		iv_icon.setScaleY((maxScale + imgProgress * (1 - maxScale)));
		
		// 缩放底部文字
		tv_text.setScaleX((1 - fastProgress * 0.3f));
		tv_text.setScaleY((1 - fastProgress * 0.3f));
		
		// 下移图片、下移底部文字、下移横向文字
		iv_icon.setTranslationY(progress * (paddingTop + paddingTopHor));
		tv_text.setTranslationY((progress * (paddingTop + paddingTopHor) * (2 * 1.0f / 3)));
		tv_text_horizontal.setTranslationY(progress * (paddingTop + paddingTopHor + paddingTopHorText));
		
		// 左移图片及底部文字
		iv_icon.setTranslationX(-(progress * transLeft));
		tv_text.setTranslationX(-(progress * transLeft));
		
		// 底部文字透明度(总进度的tvEndValue跑完进度)
		float tvProgress = 0;
		float tvEndValue = 0.6f;
		if (progress <= tvEndValue) {
			tvProgress = (tvEndValue - fastProgress) / tvEndValue;
		} else {
			tvProgress = 0;
		}
		tv_text.setAlpha(tvProgress);

		// 右移横向文字
		float hTxtProgress = 0;
		float hTxtEndValue = 1 - fast;
		if (progress <= hTxtEndValue) {
			hTxtProgress = progress / hTxtEndValue;
		} else {
			hTxtProgress = 1;
		}
		tv_text_horizontal.setTranslationX(hTxtProgress * marginLeftHorText);
		
		// 横向文字透明度(从总进度的horStartValue开始跑)
		float horProgress = 0;
		float horStartValue = 0.5f;
		if (fastEndValue >= horStartValue) {
			horProgress = (fastProgress - horStartValue) / (1 - horStartValue);
		} else {
			horProgress = 0;
		}
		tv_text_horizontal.setAlpha(horProgress);
		
	}

	public void setFast(float fast) {
		this.fast = fast;
	}

}
