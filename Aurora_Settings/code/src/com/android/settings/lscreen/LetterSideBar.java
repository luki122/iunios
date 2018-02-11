package com.android.settings.lscreen;

import com.android.settings.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;

public class LetterSideBar extends View {
	// 26个字母
	public static final String[] CHARTS = {"*", "#", "A", "B", "C", "D", "E", "F", "G", "H",
				"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
				"V", "W", "X", "Y", "Z" };

	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;	
	private int choose = -1;
	private String needHighlightStr = null;
	private Paint paint = null;
	private boolean showBkg = false;
	private float singleHeight;
	private Typeface mFace;
	private boolean isDuringTouch = false;
	private float textViewWidth = 0;
	private float textViewMarginRight = 0;

	public LetterSideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public LetterSideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public LetterSideBar(Context context) {
		super(context);
		initView(context);
	}
	
	private void initView(Context context){
        textViewWidth = context.getResources().getDimension(R.dimen.letter_side_bar_text_width);
        textViewMarginRight = getResources().
        		getDimension(com.aurora.R.dimen.aurora_alphabetlist_paddingright);
        
		singleHeight = 0;
		mFace = Typeface.createFromFile("system/fonts/Roboto-Light.ttf"); //设置字体
		paint = new Paint();
		paint.setTypeface(mFace);
		paint.setAntiAlias(true);
		paint.setTextSize(context.getResources().
				getDimension(com.aurora.R.dimen.aurora_alphabetlist_textsize));			
		isDuringTouch = false;
	}
	
	/**
	 * 每一个字母显示的高度
	 *@param numble
	 *@return
	 *@exception 
	 *@author penggangding
	 *@Time
	 */
	private void calculateSingleHeight(){
		singleHeight = getHeight() / (CHARTS.length);
//		singleHeight = 19.3f*scale;
	}
	
	/**
	 * 重写这个方法
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (showBkg) {
			canvas.drawColor(Color.parseColor("#40000000"));
		}
		if(singleHeight <= 0){
			calculateSingleHeight();
		}
		for (int i = 0; i < CHARTS.length; i++) {				
			if(!isDuringTouch && CHARTS[i].equals(needHighlightStr)){
				paint.setColor(getResources().getColor(com.aurora.R.color.aurora_alphabetlist_presscolor));
			}else{
				paint.setColor(getResources().getColor(com.aurora.R.color.aurora_alphabetlist_normalcolor));
			}
			float xPos = textViewWidth / 2 - paint.measureText(CHARTS[i]) / 2;
			float yPos = singleHeight * i + singleHeight;
			canvas.drawText(CHARTS[i], xPos, yPos, paint);
		}
	}
	
	/**
	 * 为了两个view 能够嵌套，共存。
	 */
    @Override 
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {     	
        int width=(int)(textViewWidth+textViewMarginRight);           
        int expandSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);         
        super.onMeasure(expandSpec, heightMeasureSpec); 
    }
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float y = event.getY();
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		if(singleHeight <= 0){
			calculateSingleHeight();
		}
		final int c = (int) (y / singleHeight);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			isDuringTouch = true;
			if (oldChoose != c && listener != null) {
				if (c >=0 && c < CHARTS.length) {
					listener.onTouchingLetterChanged(CHARTS[c],y);
					choose = c;
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (oldChoose != c && listener != null) {
				if (c >= 0 && c < CHARTS.length) {
					listener.onTouchingLetterChanged(CHARTS[c],y);
					choose = c;
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			choose = -1;
			isDuringTouch = false;
			invalidate();
			break;
		}
		return true;
	}
	
	/**
	 * 当前显示是以哪个字母开头的
	 * @param title
	 */
	public void setCurChooseTitle(String title){
		if(title == null){
			return ;
		}
		
		if(!canFindThisChart(title)){
			title = CHARTS[0];
		}
		
		if(!title.equals(needHighlightStr)){
			needHighlightStr = title;
			postInvalidate();	
		}
	}
	
	private boolean canFindThisChart(String title){
        for(int i=0;i<CHARTS.length;i++){
			if(CHARTS[i].equals(title)){
				return true;
			}
		}
        
        return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	/**
	 * 向外公开的方法
	 * 
	 * @param onTouchingLetterChangedListener
	 */
	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	/**
	 * 接口
	 * @author coder
	 */
	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s,float positionOfY);
	}

}
