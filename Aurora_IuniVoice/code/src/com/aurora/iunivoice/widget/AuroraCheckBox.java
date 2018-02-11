package com.aurora.iunivoice.widget;


import com.aurora.iunivoice.R;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.content.res.TypedArray;
import android.util.Log;

public class AuroraCheckBox extends CheckBox {

	private static final String TAG = "AuroraCheckBox";
	private static final boolean DEBUG = true;
	private Drawable auroraSrcDrawable = null;

	private boolean auroraIsListViewClick = false;
	
	private boolean auroraRoundDrawable = false;
	
	public AuroraCheckBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		//auroraInitAttrs(context, attrs);
		
		auroraSetButtonOrignalDrawable();

	}
	
	private void auroraInitAttrs(Context context, AttributeSet attrs)
	{
		
		TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.AuroraCheckBox, 0, R.style.AuroraCheckBoxStyle);
        
        int n = a.getIndexCount();
       
        for(int i = 0; i < n; i++)
        {
			int attr = a.getIndex(i);    
			
			switch(attr)
			{
				case R.styleable.AuroraCheckBox_auroraRoundDrawable:
					
					auroraRoundDrawable = a.getBoolean(attr,false);
					
					break;
				
				default:
					break;
			}
		}
		
        a.recycle();
        
	}
	
	public AuroraCheckBox(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.checkboxStyle);
		// TODO Auto-generated constructor stub
	}

	public AuroraCheckBox(Context context) {
		  this(context, null);
		// TODO Auto-generated constructor stub
	}

	private void auroraInitButtonDrawable()
	{
		if(auroraIsListViewClick)
		{
			
			auroraSetButtonAnimDrawable();
		}
		else 
		{
			
			auroraSetButtonOrignalDrawable();
		}	 
	}
	
	private void auroraSetButtonAnimDrawable()
	{
		if(!auroraRoundDrawable)
		{
			auroraSrcDrawable = getResources().getDrawable(R.drawable.aurora_btn_checkbox_light);
		}
		else
		{
			auroraSrcDrawable = getResources().getDrawable(R.drawable.aurora_round_checkbox_selector);	
		}
		
		if (auroraSrcDrawable != null) 
		{
			setButtonDrawable(auroraSrcDrawable);
		}
		
	}
	
	private void auroraSetButtonOrignalDrawable()
	{
		if(!auroraRoundDrawable)
		{
			auroraSrcDrawable = getResources().getDrawable(R.drawable.aurora_btn_checkbox_light_orignal);
		}
		else
		{
			auroraSrcDrawable = getResources().getDrawable(R.drawable.aurora_round_checkbox_selector_orignal);
		}
		
		if (auroraSrcDrawable != null) 
		{
			setButtonDrawable(auroraSrcDrawable);
		}
		
	}
	
	public void auroraSetChecked(boolean checked,boolean isClick)
	{
		auroraIsListViewClick = isClick;
	
		setChecked(checked);	
	}
	
	public void auroraSetIsAnimNeeded( boolean isAnimNeeded ) {
		auroraIsListViewClick = isAnimNeeded;
		
		auroraInitButtonDrawable();
	}
	
	@Override
	public void setChecked(boolean checked) {
        
        if(isChecked() == checked)
        {
			log("setChecked() 1");
			return;
		}
		
		auroraInitButtonDrawable();
		
        super.setChecked(checked);
    }

	 @Override
    protected void drawableStateChanged() {

        super.drawableStateChanged();
        Drawable background = null;
    
        if(auroraIsListViewClick)
        {
			auroraIsListViewClick = false;
		}
		
        Drawable d = auroraSrcDrawable;
        
        if(d != null)
        {
			background = ((StateListDrawable)d).getCurrent();
		}
		
		if(background != null && background instanceof AnimationDrawable)
		{
			((AnimationDrawable)background).start();
		}
    }
    
    public void auroraSetRoundBackGround(boolean round)
    {
		auroraRoundDrawable = round;
		
		auroraSetButtonOrignalDrawable();
		
	}
    
    private void log(String str)
    {
		if(DEBUG)
			Log.e(TAG, str);
	}
}
