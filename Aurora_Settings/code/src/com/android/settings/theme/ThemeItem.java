package com.android.settings.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ThemeItem extends LinearLayout {
	private RelativeLayout mRelative;
	private View mView;
	private TextView mTextView;
	private CheckBox mCheckBox;
	private LinearLayout mLinearLayout;
	private ImageView mImageViewLeft;
	private ImageView mImageViewRight;

	private RelativeLayout.LayoutParams mViewRelativeParams;
	private RelativeLayout.LayoutParams mTextViewRelativeParams;
	private RelativeLayout.LayoutParams mCheckBoxRelativeParams;

	private LinearLayout.LayoutParams mLayoutParams;

	public static final int RELATIVE_HIGHT = 31; // dp
	public static final int RELATIVE_TOP_MARGIN = 17;// dp
	private int mRelativeLayoutHight;

	public ThemeItem(Context context) {
		this(context, null);
	}

	public ThemeItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ThemeItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		final TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.ThemeItem);

		String title = array.getString(R.styleable.ThemeItem_title);
		int viewColor = array.getColor(R.styleable.ThemeItem_titleColor, 0);
		ColorStateList titleColor = array
				.getColorStateList(R.styleable.ThemeItem_titleColor);

		setOrientation(LinearLayout.VERTICAL);
		
		View topView = new View(context, attrs, defStyle);
		addView(topView, ViewGroup.LayoutParams.MATCH_PARENT, dip2px(context, RELATIVE_TOP_MARGIN));
		
		
		mRelative = new RelativeLayout(context, attrs, defStyle);
		//mRelative.setVerticalGravity(dip2px(context, RELATIVE_TOP_MARGIN));
		addView(mRelative, ViewGroup.LayoutParams.MATCH_PARENT,
				dip2px(context, RELATIVE_HIGHT));

		mViewRelativeParams = new RelativeLayout.LayoutParams(
				dip2px(context, 3), dip2px(context, 23));
		mViewRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		mViewRelativeParams.bottomMargin = dip2px(context, 8);
		mView = new View(context, attrs, defStyle);
		if (viewColor != 0) {
			mView.setBackgroundColor(viewColor);
		}
		mRelative.addView(mView, mViewRelativeParams);

		mTextViewRelativeParams = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mTextViewRelativeParams.addRule(RelativeLayout.ALIGN_RIGHT,
				mView.getId());
		mTextViewRelativeParams
				.addRule(RelativeLayout.ALIGN_TOP, mView.getId());
		mTextViewRelativeParams.leftMargin = dip2px(context, 7);
		mTextView = new TextView(context, attrs, defStyle);
		if (title != null) {
			mTextView.setText(title);
		}
		if (titleColor != null) {
			mTextView.setTextColor(titleColor);
		}
		mTextView.setTextSize(20);
		mRelative.addView(mTextView, mTextViewRelativeParams);

		int checkBoxId = array.getResourceId(R.styleable.ThemeItem_checkboxId,
				-1);
		mCheckBoxRelativeParams = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mCheckBox = new CheckBox(context);
		mCheckBox.setId(checkBoxId);
		mCheckBox.setButtonDrawable(R.drawable.my_theme_checkbox);
		mCheckBoxRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mCheckBoxRelativeParams.rightMargin = dip2px(context, 11);
		mCheckBoxRelativeParams.bottomMargin = dip2px(context, 7);
		mRelative.addView(mCheckBox, mCheckBoxRelativeParams);

		mLayoutParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mLinearLayout = new LinearLayout(context, attrs, defStyle);
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		addView(mLinearLayout, mLayoutParams);

		Drawable resourceLeft = array
				.getDrawable(R.styleable.ThemeItem_imageResourceLeft);
		Drawable resourceRight = array
				.getDrawable(R.styleable.ThemeItem_imageResourceRight);
		int imageViewId = array
				.getResourceId(R.styleable.ThemeItem_imageId, -1);

		mImageViewLeft = new ImageView(context, attrs, defStyle);
		mImageViewLeft.setId(imageViewId);
		if (resourceLeft != null) {
			mImageViewLeft.setBackground(resourceLeft);
		}
		mLinearLayout.addView(mImageViewLeft, dip2px(context, 179.6f),
				dip2px(context, 320f));

		View view = new View(context, attrs, defStyle);
		mLinearLayout.addView(view, dip2px(context, 1f), dip2px(context, 320f));

		mImageViewRight = new ImageView(context, attrs, defStyle);
		mImageViewRight.setId(imageViewId);
		if (resourceRight != null) {
			mImageViewRight.setBackground(resourceRight);
		}
		mLinearLayout.addView(mImageViewRight, dip2px(context, 179.3f),
				dip2px(context, 320f));

		View bottomview = new View(context, attrs, defStyle);
		addView(bottomview, ViewGroup.LayoutParams.MATCH_PARENT, dip2px(context, 10f));
		
		array.recycle();
	}

	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public void setOnClickListener(View.OnClickListener listener) {
	//	mImageViewLeft.setOnClickListener(listener);
	//	mImageViewRight.setOnClickListener(listener);
	}

	public void setOnCheckedChangeListener(
			CompoundButton.OnCheckedChangeListener listener) {
		mCheckBox.setOnCheckedChangeListener(listener);
	}

	public void setCheckBoxChecked(boolean check) {
		mCheckBox.setChecked(check);
	}

	public boolean isChecked() {
		return mCheckBox.isChecked();
	}

}