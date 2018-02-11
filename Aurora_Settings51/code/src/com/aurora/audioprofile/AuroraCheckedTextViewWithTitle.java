package com.aurora.audioprofile;

import com.android.settings.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.CheckedTextView;

public class AuroraCheckedTextViewWithTitle extends CheckedTextView {
	private Paint paint = new Paint();
	public AuroraCheckedTextViewWithTitle(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public AuroraCheckedTextViewWithTitle(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);    
		paint.setAntiAlias(true);  
		textSize = 12*getResources().getDisplayMetrics().density;
		paint.setTextSize(textSize);
	}

	private String title;
	private float textSize;
	public void setTitle(String title) {
		if(title!=null && title.length()>0){
			hasSetPandding = false;
		}
		this.title = title;
		
	}

	boolean hasSetPandding = false;
	@Override
	protected void onDraw(Canvas arg0) {
		super.onDraw(arg0);
		if(title!=null && title.length()>0){
			if(!hasSetPandding){
				hasSetPandding = true;
				setGravity(Gravity.BOTTOM);
				float hight = getResources().getDimensionPixelSize(R.dimen.aurora_preference_title_hight) + getResources().getDimensionPixelSize(R.dimen.aurora_preference_item_hight);
				getLayoutParams().height = (int)hight;
				setPadding(getPaddingLeft(), 0, getPaddingLeft(), getResources().getDimensionPixelSize(R.dimen.aurora_preference_item_padding_inner));
			}
			paint.setColor(getResources().getColor(!AuroraRingPickerActivity.mIsFullScreen?R.color.aurora_volum_title_background_color : R.color.aurora_volum_title_background_color_alarm));
			arg0.drawRect(new Rect(0, 0, getWidth(), getResources().getDimensionPixelSize(R.dimen.aurora_preference_title_hight)), paint);
			paint.setColor(getResources().getColor(!AuroraRingPickerActivity.mIsFullScreen?R.color.aurora_volum_title_color:R.color.aurora_volum_title_color_alarm));
			arg0.drawText(title, getPaddingLeft(),  (getResources().getDimensionPixelSize(R.dimen.aurora_preference_title_hight)+getFontHeight(paint))/2 , paint);
		}else{
			setGravity(Gravity.CENTER_VERTICAL);
			float hight = getResources().getDimensionPixelSize(R.dimen.aurora_preference_item_hight);
			getLayoutParams().height = (int)hight;
			setPadding(getPaddingLeft(), 0, getPaddingLeft(), 0);
		}
	}
	
	/**
	 * 获取文字高度
	 * @param paint
	 * @return
	 */
    public int getFontHeight(Paint paint)  
    {  
    	 FontMetrics fontMetrics = paint.getFontMetrics();  
        return (int) Math.abs(fontMetrics.ascent)  ;  
    }  

}
