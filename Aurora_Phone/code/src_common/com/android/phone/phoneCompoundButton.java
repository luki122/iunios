package com.android.phone;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.content.res.ColorStateList;

public class phoneCompoundButton extends CompoundButton{


	private Drawable mImageDrawable;
	private String text;
    private int topmargin1,topmargin2;
    private int textsize;
    //private int textcolor;
    private ColorStateList textcolor;
	private boolean denable;
	private Context mcontext;
	public phoneCompoundButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.phonecomp, 0, 0);
		mImageDrawable=a.getDrawable(R.styleable.phonecomp_pimage);
		text=a.getString(R.styleable.phonecomp_ptext);
		topmargin1=a.getDimensionPixelSize(R.styleable.phonecomp_ptopmargin1, 0);
		topmargin2=a.getDimensionPixelSize(R.styleable.phonecomp_ptopmargin2, 0);
		textsize=a.getDimensionPixelSize(R.styleable.phonecomp_ptextsize, 0);
		textcolor=a.getColorStateList(R.styleable.phonecomp_ptextcolor);
		a.recycle();
		mcontext=context;
		denable=true;
		// TODO Auto-generated constructor stub
	}
	public phoneCompoundButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}	
	
	public phoneCompoundButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs,defStyle);
		
	}
	public void setDrawableEnabled(boolean enable)
	{
		
		denable=enable;
		this.invalidate();
	}
	
	public void setDrawable(int resid)
	{
		mImageDrawable = mcontext.getResources().getDrawable(resid);
        mImageDrawable.setState(getDrawableState());
        
		this.invalidate();
	}
	
	public void setPtext(int resid)
	{
		text=mcontext.getResources().getString(resid);
		this.invalidate();
	}
	public void setPtext(String tex)
	{
		text=tex;
		this.invalidate();
	}	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		super.onDraw(canvas);
        final Drawable buttonDrawable = mImageDrawable;
        Log.v("phoneCompoundButton", "mImageDrawable="+mImageDrawable);
        if (buttonDrawable != null) {
            final int drawableHeight = buttonDrawable.getIntrinsicHeight();
            final int drawableWidth = buttonDrawable.getIntrinsicWidth();

            int top = 0;
            top = topmargin1;
            int bottom = top + drawableHeight;
            int left =(getWidth() - drawableWidth)/2 ;
            int right = (getWidth() + drawableWidth)/2 ;

            buttonDrawable.setBounds(left, top, right, bottom);
            Log.v("phoneCompoundButton", "left="+left+"top="+top+"right="+right+"bottom="+bottom);
           if(denable) 
            buttonDrawable.draw(canvas); 
            if(text!=null)
            {
            	Paint paint=new Paint();
            	paint.setAntiAlias(true);
            	paint.setTextSize(textsize);
            	paint.setColor(textcolor.getColorForState(getDrawableState(), 0x80FFFFFF));
            	float textwidth=paint.measureText(text);
            	canvas.drawText(text, (getWidth() - textwidth)/2, bottom+topmargin2+textsize, paint);
            	
            }
        }
        
	}
	@Override
	public void toggle() {
        //setChecked(!mChecked);
    }
	@Override
	protected void drawableStateChanged() {
		// TODO Auto-generated method stub
		super.drawableStateChanged();
		if (mImageDrawable != null) {
            int[] myDrawableState = getDrawableState();
            
            // Set the state of the Drawable
            mImageDrawable.setState(myDrawableState);
            
            invalidate();
        }
	}
	@Override
	public void jumpDrawablesToCurrentState() {
		// TODO Auto-generated method stub
		super.jumpDrawablesToCurrentState();
		if (mImageDrawable != null) mImageDrawable.jumpToCurrentState();
	}	
	
}
