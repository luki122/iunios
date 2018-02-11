/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.browser;

import com.android.browser.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.BrowserContract;
import android.provider.BrowserContract.Bookmarks;
import android.provider.BrowserContract.History;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.android.browser.UrlInputView.StateListener;
import com.android.browser.UrlInputView.UrlInputIsSearchListener;

public class NavigationBarPhone extends NavigationBarBase implements
        StateListener, OnMenuItemClickListener, OnDismissListener,UrlInputIsSearchListener {

    private ImageView mStopButton;
    private ImageView mMagnify;
    private ImageView mClearButton;
    private ImageView book_mark;
    private TextView enter;
    private TextView cancle;
    private TextView search;
 //   private ImageView mVoiceButton;
    private Drawable mStopDrawable;
    private Drawable mRefreshDrawable;
    private Drawable earth;
    private Drawable mMagnifyDrawable;
    private String mStopDescription;
    private String mRefreshDescription;
  //  private View mTabSwitcher;
  //  private View mComboIcon;
    private View mTitleContainer;
  //  private View mMore;
    private Drawable mTextfieldBgDrawable;
    private PopupMenu mPopupMenu;
    private boolean mOverflowMenuShowing;
    private boolean mNeedsMenu;
  // private View mIncognitoIcon;

    private String mHomepageBaseUrl;
    private String mHomepageBaseTitle;

    public NavigationBarPhone(Context context) {
        super(context);
    }

    public NavigationBarPhone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationBarPhone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mStopButton = (ImageView) findViewById(R.id.stop);
        mStopButton.setOnClickListener(this);
        mClearButton = (ImageView) findViewById(R.id.clear);
        mClearButton.setOnClickListener(this);
        enter=(TextView) findViewById(R.id.enter);
        enter.setOnClickListener(this);
        cancle=(TextView) findViewById(R.id.cancel);
        cancle.setOnClickListener(this);
        search=(TextView) findViewById(R.id.search);
        search.setOnClickListener(this);
        book_mark=(ImageView) findViewById(R.id.book_mark);
        book_mark.setOnClickListener(this);
     //   mVoiceButton = (ImageView) findViewById(R.id.voice);
     //   mVoiceButton.setOnClickListener(this);
        mMagnify = (ImageView) findViewById(R.id.magnify);
    //    mTabSwitcher = findViewById(R.id.tab_switcher);
     //   mTabSwitcher.setOnClickListener(this);
     //   mMore = findViewById(R.id.more);
      //  mMore.setOnClickListener(this);
      //  mComboIcon = findViewById(R.id.iconcombo);
      //  mComboIcon.setOnClickListener(this);
        mTitleContainer = findViewById(R.id.title_bg);
        setFocusState(false);
        Resources res = getContext().getResources();
        mStopDrawable = res.getDrawable(R.drawable.stop);
        mRefreshDrawable = res.getDrawable(R.drawable.refresh);
        earth=res.getDrawable(R.drawable.earth_small);
        mMagnifyDrawable=res.getDrawable(R.drawable.search_glass);
        mStopDescription = res.getString(R.string.accessibility_button_stop);
        mRefreshDescription = res.getString(R.string.accessibility_button_refresh);
        mTextfieldBgDrawable = res.getDrawable(R.drawable.url_input_view_bg);
        mUrlInput.setContainer(this);
        mUrlInput.setStateListener(this);
        mUrlInput.setUrlInputIsSearchListener(this);
        mNeedsMenu = !ViewConfiguration.get(getContext()).hasPermanentMenuKey();
    //    mIncognitoIcon = findViewById(R.id.incognito_icon);
        mHomepageBaseUrl = res.getString(R.string.homepage_base);
        mHomepageBaseTitle = res.getString(R.string.search_hint);
    }

    @Override
    public void onProgressStarted() {
        super.onProgressStarted();
        if (mStopButton.getDrawable() != mStopDrawable) {
            mStopButton.setImageDrawable(mStopDrawable);
            mStopButton.setContentDescription(mStopDescription);
            if (mStopButton.getVisibility() != View.VISIBLE) {
            //    mComboIcon.setVisibility(View.GONE);
                mStopButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onProgressStopped() {
        super.onProgressStopped();
        mStopButton.setImageDrawable(mRefreshDrawable);
        mStopButton.setContentDescription(mRefreshDescription);
        if (!isEditingUrl()) {
         //   mComboIcon.setVisibility(View.VISIBLE);
        }
        onStateChanged(mUrlInput.getState());
    }

    /**
     * Update the text displayed in the title bar.
     * @param title String to display.  If null, the new tab string will be
     *      shown.
     */
    @Override
    void setDisplayTitle(String title) {
        mUrlInput.setTag(title);
        if (!isEditingUrl()) {
            if (title == null) {
                mUrlInput.setText(R.string.new_tab);
            } else {
                if (mHomepageBaseUrl.equals(title)) {
                    title = mHomepageBaseTitle;
                }
                mUrlInput.setText(UrlUtils.stripUrl(title), false);
            }
            mUrlInput.setSelection(0);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mStopButton) {
            if (mTitleBar.isInLoad()) {
                mUiController.stopLoading();
            } else {
                WebView web = mBaseUi.getWebView();
                if (web != null) {
                	mBaseUi.setTabError(false);
                    stopEditingUrl();
                    web.reload();
                }
            }
//        } else if (v == mTabSwitcher) {
//            ((PhoneUi) mBaseUi).toggleNavScreen();
//        } else if (mMore == v) {
//            showMenu(mMore);
        } else if (mClearButton == v) {
            mUrlInput.setText("");
            mClearButton.setVisibility(View.GONE);
//        } else if (mComboIcon == v) {
//            mUiController.showPageInfo();
//        } else if (mVoiceButton == v) {
//            mUiController.startVoiceRecognizer();
            
        } else if(cancle==v){
        	Log.i("xie", "--------------cancle==v--------------------");
        	clearFocus();
        	
        } else if(book_mark	==v){
        	mUiController.showBookmarkCrtPageView();
        	
        	
        } else if(enter==v){
        	Log.i("xie", "--------------cancle==v--------------------");
        	mUrlInput.finishInput(mUrlInput.getUrlText(), null, UrlInputView.TYPED);
        } else if(search==v){
        	mUrlInput.finishInput(mUrlInput.getUrlText(), null, UrlInputView.TYPED);
        	Log.i("xie", "--------------cancle==v--------------------");
        	doInsertSearchContent(mUrlInput.getUrlText().trim());
        
         }else {
            super.onClick(v);
        }
    }
    
    private void doInsertSearchContent(String searchContent) {
    	if(TextUtils.isEmpty(searchContent)) return;
    	
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = null;
        try {
            c = cr.query(BrowserContract.Bookmarks.CONTENT_URI, new String[] { BrowserContract.Bookmarks._ID },
            		BrowserContract.Bookmarks.TITLE + "=?", new String[] { searchContent }, null);
            
            if (!c.moveToFirst()) {
            	ContentValues values = new ContentValues();
                values.put(BrowserContract.Bookmarks.URL, searchContent);
                values.put(BrowserContract.Bookmarks.TITLE, searchContent);
                values.put(BrowserContract.Bookmarks.IS_FOLDER, 3);
                values.put(BrowserContract.Bookmarks.PARENT, "0");
                values.put(BrowserContract.Bookmarks.POSITION, "0");
                values.put(BrowserContract.Bookmarks.DATE_CREATED, "" + System.currentTimeMillis());
                
                cr.insert(BrowserContract.Bookmarks.CONTENT_URI, values);
            } 
        } finally {
            if (c != null) c.close();
        }
    }

    @Override
    public boolean isMenuShowing() {
        return super.isMenuShowing() || mOverflowMenuShowing;
    }

    void showMenu(View anchor) {
        Activity activity = mUiController.getActivity();
        if (mPopupMenu == null) {
            mPopupMenu = new PopupMenu(mContext, anchor);
            mPopupMenu.setOnMenuItemClickListener(this);
            mPopupMenu.setOnDismissListener(this);
            if (!activity.onCreateOptionsMenu(mPopupMenu.getMenu())) {
                mPopupMenu = null;
                return;
            }
        }
        Menu menu = mPopupMenu.getMenu();
        if (activity.onPrepareOptionsMenu(menu)) {
            mOverflowMenuShowing = true;
            mPopupMenu.show();
        }
    }

    @Override
    public void onDismiss(PopupMenu menu) {
        if (menu == mPopupMenu) {
            onMenuHidden();
        }
    }

    private void onMenuHidden() {
        mOverflowMenuShowing = false;
        mBaseUi.showTitleBarForDuration();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == mUrlInput) {
            if (hasFocus && !mUrlInput.getText().toString().equals(mUrlInput.getTag())) {
                // only change text if different
                mUrlInput.setText((String) mUrlInput.getTag(), false);
                mUrlInput.selectAll();

                if (mHomepageBaseUrl.equals(mUrlInput.getTag())) {
                    mUrlInput.forceFilter();
                    mUrlInput.setText("");
                }
            } else {
                setDisplayTitle(mUrlInput.getText().toString());
            }
        }
        super.onFocusChange(view, hasFocus);
    }

    @Override
    public void onStateChanged(int state) {
     //   mVoiceButton.setVisibility(View.GONE);
        switch(state) {
        case StateListener.STATE_NORMAL:
        	Log.i("xie1", "_______STATE_NORMAL________________");
          //  mComboIcon.setVisibility(View.VISIBLE);
            mStopButton.setVisibility(View.VISIBLE);
            book_mark.setVisibility(View.VISIBLE);
            mClearButton.setVisibility(View.GONE);
            mMagnify.setVisibility(View.GONE);
            enter.setVisibility(View.GONE);
            cancle.setVisibility(View.GONE);
            search.setVisibility(View.GONE);
          //  mTabSwitcher.setVisibility(View.VISIBLE);
            mTitleContainer.setBackgroundDrawable(mTextfieldBgDrawable);
         //   mMore.setVisibility(mNeedsMenu ? View.VISIBLE : View.GONE);
            break;
        case StateListener.STATE_HIGHLIGHTED:
        	Log.i("xie1", "_______STATE_HIGHLIGHTED________________");
          //  mComboIcon.setVisibility(View.GONE);
            mStopButton.setVisibility(View.GONE);
            mClearButton.setVisibility(View.VISIBLE);
            book_mark.setVisibility(View.GONE);
            search.setVisibility(View.GONE);
            cancle.setVisibility(View.GONE);
            enter.setVisibility(View.VISIBLE);
            if (mUrlInput.isSearch(mUrlInput.getUrlText())) {
                mMagnify.setImageDrawable(mMagnifyDrawable);
            } else {
                mMagnify.setImageDrawable(earth);
            }
            if ((mUiController != null) && mUiController.supportsVoice()) {
           //     mVoiceButton.setVisibility(View.VISIBLE);
            }
            mMagnify.setVisibility(View.VISIBLE);
         //   mTabSwitcher.setVisibility(View.GONE);
         //   mMore.setVisibility(View.GONE);
            mTitleContainer.setBackgroundDrawable(mTextfieldBgDrawable);
            break;
        case StateListener.STATE_EDITED:
        	Log.i("xie1", "_______STATE_EDITED________________");
          //  mComboIcon.setVisibility(View.GONE);
            mStopButton.setVisibility(View.GONE);
            String urlText = mUrlInput.getUrlText();
            if (TextUtils.isEmpty(urlText)) {
                mClearButton.setVisibility(View.GONE);
            } else {
                mClearButton.setVisibility(View.VISIBLE);
            }
            book_mark.setVisibility(View.GONE);
            if (TextUtils.isEmpty(urlText) || mUrlInput.isSearch(urlText)) {
                mMagnify.setImageDrawable(mMagnifyDrawable);
            } else {
                mMagnify.setImageDrawable(earth);
            }
            mMagnify.setVisibility(View.VISIBLE);
            search.setVisibility(View.GONE);
            cancle.setVisibility(View.VISIBLE);
            enter.setVisibility(View.GONE);
        //    mTabSwitcher.setVisibility(View.GONE);
          //  mMore.setVisibility(View.GONE);
            mTitleContainer.setBackgroundDrawable(mTextfieldBgDrawable);
            break;
        }
    }

    @Override
    public void onTabDataChanged(Tab tab) {
        super.onTabDataChanged(tab);
//        mIncognitoIcon.setVisibility(tab.isPrivateBrowsingEnabled()
//                ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mUiController.onOptionsItemSelected(item);
    }

	@Override
	public void onTextChange(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		if(mUrlInput.getState()==StateListener.STATE_EDITED){
			 Log.i("xie1", "_____________onTextChange_____________0___________:"+mUrlInput.getUrlText()+"/");
			if(!mUrlInput.getUrlText().equals("")){
				 mClearButton.setVisibility(View.VISIBLE);
				if(mUrlInput.isSearch(String.valueOf(s))){			
			            	mMagnify.setImageDrawable(mMagnifyDrawable);	
			           
					 mMagnify.setVisibility(View.VISIBLE);
			            enter.setVisibility(View.GONE);
			            cancle.setVisibility(View.GONE);
			            search.setVisibility(View.VISIBLE);
			            Log.i("xie1", "_____________onTextChange_____________1_____________"+s+"\n"+mUrlInput.isSearch(String.valueOf(s)));	
						 
					
				}else{
					mMagnify.setImageDrawable(earth);	
					mMagnify.setVisibility(View.VISIBLE);								            				         
					enter.setVisibility(View.VISIBLE);
		            cancle.setVisibility(View.GONE);
		            search.setVisibility(View.GONE);
		            Log.i("xie1", "_____________onTextChange______________2____________"+s+"\n"+mUrlInput.isSearch(String.valueOf(s)));	
					
//		            if(mUrlInput.getUrlText().equals("")){
//		            	Log.i("xie66", "____________");
//		            	enter.setVisibility(View.GONE);
//			            cancle.setVisibility(View.VISIBLE);
//		            }else{
//		            	enter.setVisibility(View.VISIBLE);
//			            cancle.setVisibility(View.GONE);
//		            }
		            
				}	
			}else{
				Log.i("xie1", "_____________onTextChange_____________3_____________"+s+"\n"+mUrlInput.isSearch(String.valueOf(s)));	
				    mMagnify.setImageDrawable(mMagnifyDrawable);	
				    mMagnify.setVisibility(View.VISIBLE);
		            enter.setVisibility(View.GONE);
		            cancle.setVisibility(View.VISIBLE);
		            search.setVisibility(View.GONE);
		            mClearButton.setVisibility(View.GONE);
				
			}
				
			
		}
		
		
	}

}
