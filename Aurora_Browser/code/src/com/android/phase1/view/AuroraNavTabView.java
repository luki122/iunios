/**
 * Vulcan created this file in 2015年2月3日 下午2:19:55 .
 */
package com.android.phase1.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.android.browser.NavTabView;
import com.android.browser.R;
import com.android.browser.Tab;

/**
 * Vulcan created AuroraNavTabView in 2015年2月3日 .
 * 
 */
public class AuroraNavTabView extends NavTabView {

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public AuroraNavTabView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public AuroraNavTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 */
	public AuroraNavTabView(Context context) {
		super(context);
		init();
	}
	
    private void init() {
    	mIconTitle = (ImageView) findViewById(R.id.icon_title);
    	return;
    }
	
    /**
     * 
     * Vulcan created this method in 2015年1月31日 下午2:22:43 .
     * @return
     */
    public View getImageView() {
    	return mImage;
    }
    
    /**
     * 
     * Vulcan created this method in 2015年2月3日 下午2:07:44 .
     * @param bmp
     */
    public void setTitleIcon(Bitmap bmp) {
        if (bmp == null) {
        	mIconTitle.setPadding(mIconTitle.getPaddingLeft(), 0, 0, 0);
        } else {
        	mIconTitle.setPadding(0, 0, 0, 0);
        }
        mIconTitle.setImageBitmap(bmp);
    }
    
    /* (non-Javadoc)
	 * @see com.android.browser.NavTabView#setWebView(com.android.browser.Tab)
	 */
	@Override
	public void setWebView(Tab tab) {
		// TODO Auto-generated method stub
		super.setWebView(tab);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年4月8日 下午2:32:24 .
	 * @return
	 */
	public Bitmap getScreenshot() {
		return mTab.getScreenshot();
	}

	protected ImageView mIconTitle = null;

}
