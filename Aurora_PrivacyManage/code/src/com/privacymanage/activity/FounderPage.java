/**
 * 
 */
package com.privacymanage.activity;

import com.privacymanage.utils.LogUtils;
import com.privacymanage.utils.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.Type;

/**
 * @author vulcan
 *
 */
public class FounderPage extends AuroraActivity implements OnClickListener,
		OnAuroraActionBarBackItemClickListener,OnGlobalLayoutListener {

	public FounderPage() {

	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月28日 下午2:20:52 .
	 * @return
	 */
	public boolean isPrivateActivity() {
		return false;
	}

	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午5:34:30 .
	 */
	public int getPageId() {
		return this.mPageId;
	}

	@Override
	public void onAuroraActionBarBackItemClicked(int arg0) {
		showPreviousPage();
	}

	@Override
	public void onClick(View v) {
		showNextPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see aurora.app.AuroraActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Log.d("vprivacy","onCreate: UnderlyingWrapper.isInPrivacySpace() = " + UnderlyingWrapper.isInPrivacySpace());
		
		ActivityMan.addActivity(this);
		
		//Since android OS has not told us when we should call the method showSoftInput of InputMethodManager,
		//we have to call the method delaying 300 ms.
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				FounderPage.this.showSoftInput();
			}
		}, 300);
		
		
		getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(this);

		if (mLayoutResId == RESOURCE_ID_INVALID) {
			return;
		}

		TextView textViewNextStep = null;
		//setAuroraContentView(mLayoutResId);
		setAuroraContentView(mLayoutResId, mActionBarIsEmpty?Type.Empty:Type.Normal);
		LogUtils.printWithLogCat("vround", "mActionBarIsEmpty = " + mActionBarIsEmpty);

		// setup text
		if (mStringNextStepResId != RESOURCE_ID_INVALID) {
			getAuroraActionBar().addItem(ResIdMan.LAYOUT_FILE_NEXT_STEP,
					ACTION_BAR_ITEM_ID_NEXT_STEP);
			textViewNextStep = (TextView) findViewById(ResIdMan.TEXTVIEW_NEXT_STEP);
			textViewNextStep.setText(mStringNextStepResId);
		}

		if (mStringActionBarTitle != RESOURCE_ID_INVALID) {
			getAuroraActionBar().setTitle(mStringActionBarTitle);
		}

		// setup listener of back key
		getAuroraActionBar().setmOnActionBarBackItemListener(this);
		Log.d("newPr", "onCreate: ");

		// setup listern of next step key
		if (textViewNextStep != null) {
			textViewNextStep.setOnClickListener(this);
		}

		if (mActionBarIsEmpty) {
			getAuroraActionBar().setBackgroundResource(ResIdMan.IMAGE_PAGE_TOP_WITHE);
			// getAuroraActionBar().setVisibility(View.INVISIBLE);
		}
		
		return;
	}

	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:41:50 .
	 * 
	 * @return
	 */
	protected static PageMan getPageMan() {
		return PageMan.getInstance();
	}

	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:42:54 .
	 * 
	 * @return
	 */
	protected FounderPage getNextPage() {
		return getPageMan().getNextPage(mPageId);
	}

	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:45:50 .
	 * 
	 * @return
	 */
	protected FounderPage getPreviousPage() {
		return getPageMan().getPreviousPage(mPageId);
	}

	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:56:03 .
	 * 
	 * @return
	 */
	protected boolean isFirstPage() {
		return getPageMan().isFirstPage(mPageId);
	}

	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:56:31 .
	 * 
	 * @return
	 */
	protected boolean isLastPage() {
		return getPageMan().isLastPage(mPageId);
	}

	// constant member
	protected static int ACTION_BAR_ITEM_ID_NEXT_STEP = 0;
	protected static int RESOURCE_ID_INVALID = 0;

	// page id definition
	protected static int PAGE_ID_WELCOME = 0;
	protected static int PAGE_ID_CREATE_ACCOUNT = 1;
	protected static int PAGE_ID_PASSWORD_AGAIN = 2;
	protected static int PAGE_ID_PASSWORD_PROTECTION = 3;
	protected static int PAGE_ID_CREATE_DONE = 4;

	// layout resource id
	protected int mLayoutResId = RESOURCE_ID_INVALID;

	// resource in action bar
	protected int mStringNextStepResId = RESOURCE_ID_INVALID;
	protected int mStringActionBarTitle = RESOURCE_ID_INVALID;

	//protected boolean mShoudShowActionBar = true;
	protected boolean mActionBarIsEmpty = false;

	// page id
	protected int mPageId = 0;

	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午4:55:05 .
	 */
	private void showNextPage() {
		if (!isLastPage()) {
			FounderPage nextPage = this.getNextPage();
			Intent intentNextPage = new Intent(this, nextPage.getClass());
			startActivity(intentNextPage);
		}
	}

	/**
	 * 
	 * Vulcan created this method in 2014年11月4日 下午4:54:25 .
	 */
	private void showPreviousPage() {
		if (!isFirstPage()) {
			//FounderPage previousPage = this.getPreviousPage();
			//Intent intentPreviousPage = new Intent(this, previousPage.getClass());
			//startActivity(intentPreviousPage);
			finish();
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月7日 上午11:45:41 .
	 */
	public void showSoftInput() {
		final EditText etPassword = (EditText) findViewById(ResIdMan.EDITTEXT_PASSWORD);
		if(etPassword != null) {
			Utils.showSoftInput(this, etPassword);
			LogUtils.printWithLogCat("vinput", "showSoftInput: etPassword show");
			return;
		}
		
		final EditText etEmail = (EditText) findViewById(ResIdMan.EDITTEXT_EMAIL_ADDR);
		if(etEmail != null) {
			Utils.showSoftInput(this, etEmail);
			LogUtils.printWithLogCat("vinput", "showSoftInput: etEmail show");
			return;
		}
		return;
	}

	@Override
	public void onGlobalLayout() {
		int width = getWindow().getDecorView().getWidth();
		int height = getWindow().getDecorView().getHeight();
		
		if(width == -1 || height == -1) {
			return;
		}
		
		if(!mOnLayoutCreatedCalled) {
			onLayoutCreated();
			mOnLayoutCreatedCalled = true;
		}
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年11月12日 下午4:37:47 .
	 */
	public void onLayoutCreated() {};
	
	boolean mOnLayoutCreatedCalled = false;

}
