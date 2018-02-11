/**
 * Vulcan created this file in 2014年10月10日 下午2:24:29 .
 */
package com.privacymanage.activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;



/**
 * 
 * Vulcan created PageIndicator in 2014年10月10日 .
 *
 */
public class PageIndicator extends LinearLayout {
	
	public final int MAX_LENGTH = UserGuide.PAGE_TOTAL;
	
	int mIndexSelectedItem = 0;
	int mLength = 0;

	/**
	 * 
	 * @param context
	 */
	public PageIndicator(Context context, AttributeSet as) {
		super(context,as);
		setOrientation(HORIZONTAL);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月10日 下午2:36:25 .
	 * @param indexItem
	 */
	public void switchTo(int indexItem) {
		//inverse old item
		getChildAt(mIndexSelectedItem).setSelected(false);

		//select new item
		getChildAt(indexItem).setSelected(true);
		mIndexSelectedItem = indexItem;
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月23日 上午11:08:35 .
	 * @param newLength
	 */
	public void changeLength(int newLength) {
		Log.d("vind","changeLength: newLength = " + newLength);
		Log.d("vind","changeLength: mLength = " + mLength);
		
		int m = 0;
		if(newLength > MAX_LENGTH) {
			newLength = MAX_LENGTH;
		}
		
		mLength = getChildCount();
		
		for(m = mLength;m < newLength;m ++) {
			getChildAt(m).setVisibility(View.VISIBLE);
		}
		
		for(m = newLength;m < mLength;m ++) {
			getChildAt(m).setVisibility(View.GONE);
		}
	}

}
