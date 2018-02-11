package com.android.gallery3d.filtershow.category;

import java.util.ArrayList;

import com.android.gallery3d.filtershow.auroraeffects.ImageProcessor;

import com.android.gallery3d.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.res.Resources;

public class AuroraEffectView extends LinearLayout implements AuroraEffectViewListener {
	
	private Bitmap mFilteredBitmap;
	private Bitmap mFilteredPressedBitmap;
	
	private ImageView mImageView;
	private TextView mTextView;

	private int mFilterType;
	private float mImageViewWidthHeight;
	private float mTextViewHeight;
	
	private AuroraAction mAuroraAction;
	
	private Handler mHandler = new Handler();
	
	//private static final String AURORA_DAY_FONT_PATH = "system/fonts/DroidSansFallback.ttf";
	//private static final Typeface mAuroraChinese = Typeface.createFromFile(AURORA_DAY_FONT_PATH);

	public AuroraEffectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
		Log.i("SQF_LOG", "AuroraEffectView::AuroraEffectView");
		
		Resources res = this.getResources();
		
		this.setOrientation(LinearLayout.VERTICAL);
		this.setGravity(Gravity.CENTER);
		
		mImageViewWidthHeight = res.getDimension(R.dimen.aurora_effect_view_bmp_width);
		mTextViewHeight = res.getDimension(R.dimen.aurora_effect_view_text_height);
		float imageViewMarginTop = res.getDimension(R.dimen.aurora_effect_view_bmp_top_margin);
		
		mImageView = new ImageView(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.width = Math.round(mImageViewWidthHeight);
		lp.height = Math.round(mImageViewWidthHeight);
		lp.gravity = Gravity.CENTER_HORIZONTAL;  
		lp.setMargins(0, Math.round(imageViewMarginTop), 0, 0);
		mImageView.setBackgroundColor(Color.BLACK);//res.getColor(R.color.aurora_effect_view_default_image_bgcolor)
		mImageView.setLayoutParams(lp);
		addView(mImageView);
		
		mTextView = new TextView(context);
		lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.height = Math.round(mTextViewHeight);
		lp.gravity = Gravity.CENTER_HORIZONTAL;
		lp.setMargins(10, 0, 10, 10);
		mTextView.setLayoutParams(lp);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setSingleLine(true);
		mTextView.setEllipsize(TruncateAt.MARQUEE);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX , res.getDimension(R.dimen.aurora_effect_view_text_size));
		//mTextView.setTypeface(mAuroraChinese);
		ColorStateList csl = (ColorStateList) res.getColorStateList(R.color.filtershow_custom_action_bar_button_color);
		mTextView.setTextColor(csl);
		addView(mTextView);

		TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.AuroraEffectView);
        String text = tArray.getString(R.styleable.AuroraEffectView_text);
        mTextView.setText(text);
        tArray.recycle();
		
	}
	
	public void setText(String text) {
		mTextView.setText(text);
	}
	
	public void setBitmap(Bitmap bm) {
		mFilteredBitmap = bm;
		mImageView.setImageBitmap(bm);
	}
	
	public Bitmap getFilteredBitmap() {
		return mFilteredBitmap;
	}
	
	public Bitmap getFilteredPressedBitmap() {
		if(mFilteredPressedBitmap == null) {
			if(mFilteredBitmap == null) return null;
			mFilteredPressedBitmap = Bitmap.createBitmap(mFilteredBitmap.getWidth(), mFilteredBitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(mFilteredPressedBitmap);
			canvas.drawBitmap(getFilteredBitmap(), 0, 0, null);
			Bitmap selectedFrame = BitmapFactory.decodeResource(getResources(), R.drawable.filter_selected);
			Rect destRect = new Rect(0, 0, (int)mImageViewWidthHeight, (int)mImageViewWidthHeight);
			canvas.drawBitmap(selectedFrame, null, destRect, null);
			selectedFrame.recycle();
			selectedFrame = null;
		}
		return mFilteredPressedBitmap;
	}
	
	public void recycleBitmaps() {
		if(mFilteredBitmap != null) {
			mFilteredBitmap.recycle();
			mFilteredBitmap = null;
		}
		if(mFilteredPressedBitmap != null) {
			mFilteredPressedBitmap.recycle();
			mFilteredPressedBitmap = null;
		}
	}
	
	public void setAuroraAction(AuroraAction action) {
		mAuroraAction = action;
		
		
		//mHandler.postDelayed(new Runnable() {

		//	@Override
		//	public void run() {
				// TODO Auto-generated method stub
				mAuroraAction.postNewIconRenderRequest((int)mImageViewWidthHeight, (int)mImageViewWidthHeight);
		//	}
			
		//}, 300);
		
	}
	
	public AuroraAction getAuroraAction() {
		return mAuroraAction;
	}

	@Override
	public void commonBitmapLoaded() {
		// TODO Auto-generated method stub
		
	}
	
	public void setSelected(boolean selected) {
		mTextView.setSelected(selected);
		if(selected) {
			mImageView.setImageBitmap(getFilteredPressedBitmap());
		} else {
			if(mFilteredBitmap != null) {
				mImageView.setImageBitmap(mFilteredBitmap);
			}
		}
	}
}
