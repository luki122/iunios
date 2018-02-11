/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import aurora.widget.AuroraAutoCompleteTextView;

import com.android.browser.SuggestionsAdapter.CompletionListener;
import com.android.browser.SuggestionsAdapter.SuggestItem;
import com.android.browser.UI.ComboViews;
import com.android.browser.search.SearchEngine;
import com.android.browser.search.SearchEngineInfo;
import com.android.browser.search.SearchEngines;
import com.android.internal.R;

import java.util.List;

/**
 * url/search input view
 * handling suggestions
 */
public class UrlInputView extends AuroraAutoCompleteTextView
        implements OnEditorActionListener,
        CompletionListener, OnItemClickListener, TextWatcher ,OnLongClickListener{

    static final String TYPED = "browser-type";
    static final String SUGGESTED = "browser-suggest";

    static final int POST_DELAY = 100;
    

    static interface StateListener {
        static final int STATE_NORMAL = 0;
        static final int STATE_HIGHLIGHTED = 1;
        static final int STATE_EDITED = 2;

        public void onStateChanged(int state);
    }

    private UrlInputListener   mListener;
    private UrlInputIsSearchListener  searchListener;    
    private InputMethodManager mInputManager;
    private SuggestionsAdapter mAdapter;
    private View mContainer;
    private boolean mLandscape;
    private boolean mIncognitoMode;
    private boolean mNeedsUpdate;

    protected int mState;
    private StateListener mStateListener;
    private Rect mPopupPadding;
    private Context mContext;
    private boolean isLongClick=false;
    
    private UiController mUiController;

    public UrlInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.PopupWindow,
                R.attr.autoCompleteTextViewStyle, 0);

        Drawable popupbg = a.getDrawable(R.styleable.PopupWindow_popupBackground);
        a.recycle();
        mPopupPadding = new Rect();
        popupbg.getPadding(mPopupPadding);
        init(context);
        mContext=context;
    }

    public UrlInputView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.autoCompleteTextViewStyle);
        mContext=context;
    }

    public UrlInputView(Context context) {
        this(context, null);
        mContext=context;
    }

    private void init(Context ctx) {
        mInputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        setOnEditorActionListener(this);
        mAdapter = new SuggestionsAdapter(ctx, this,this);
        setAdapter(mAdapter);
        setSelectAllOnFocus(true);
        onConfigurationChanged(ctx.getResources().getConfiguration());
        setThreshold(1);
        setOnItemClickListener(this);
        mNeedsUpdate = false;
        addTextChangedListener(this);
        setOnLongClickListener(this);
        mState = StateListener.STATE_NORMAL;
        setDropDownAlwaysVisible(true);
    }
    
    /**
     * 
     * Vulcan created this method in 2015年3月9日 下午2:07:06 .
     * @return
     */
    public String getPrimaryClip() {
		ClipboardManager cm = (ClipboardManager) mContext
				.getSystemService(Context.CLIPBOARD_SERVICE);
		//if( cm.getText()==null){return  super.performLongClick();}
		
		if(!cm.hasPrimaryClip()) {
			return "";
		}
		
		String url = "";
		if(cm.getPrimaryClip() != null) {
			if(cm.getPrimaryClip().getItemAt(0) != null) {
				if(cm.getPrimaryClip().getItemAt(0).getText() != null) {
					if(cm.getPrimaryClip().getItemAt(0).getText().toString() != null) {
						url = cm.getPrimaryClip().getItemAt(0).getText().toString();
					}
				}
			}
		}

		//String url = cm.getText().toString();
		if (TextUtils.isEmpty(url))
			return "";
		return url;
    }
    
	@Override
	public boolean performLongClick() {

		String url = getPrimaryClip();
		//final String url = "test";
		
		if(url.length() == 0) {
			return super.performLongClick();
		}

		Boolean mIsSearch = isSearch(url);
		if (!mIsSearch) {
			addEditorItem(100, com.android.browser.R.string.paste_and_go);
		}
		return super.performLongClick();

	}
    
    
    /**
     * 
     * Vulcan created this method in 2015年2月4日 下午2:49:22 .
     * @return
     */
	private boolean navScreenIsVisible() {
		if (mUiController.getUi() instanceof PhoneUi) {
			PhoneUi pu = (PhoneUi) mUiController.getUi();
			return pu.showingNavScreen();
		}
		return false;
	}
	@Override
	protected void onContextItemClicked(int itemId){
		
		if (itemId == 100) {

			String url = getPrimaryClip();
			Log.i("xiexiujie",
					"_______________onContextItemClicked________________" + url);
			if (TextUtils.isEmpty(url)) {
				return;
			}

			this.setText(url, false);
			WebView currentTopWebView = mUiController.getCurrentTopWebView();
			if (currentTopWebView != null) {
				currentTopWebView.requestFocus();
			}
			Log.i("xiexiujie",
					"_______________onContextItemClicked________________"
							+ getAdapter());

			mUiController.getCurrentTab().loadUrl(url, null);

		}
		
	}

	protected void onFocusChanged(boolean focused, int direction, Rect prevRect) {
		super.onFocusChanged(focused, direction, prevRect);
		Log.i("xie", "*************url***************onFocusChanged******");
		int state = -1;
		if (focused) {
			if (hasSelection()) {
				if (mUiController == null) {
					throw new RuntimeException("mUiController is null");
				}
				if (!navScreenIsVisible()&&!isLongClick) {
					String origText = this.getText().toString();
					this.setText("");
					this.setText(origText);
					setSelection(0, origText.length());
					isLongClick=false;
				}
				state = StateListener.STATE_HIGHLIGHTED;
				isLongClick=false;
			} else {
				state = StateListener.STATE_EDITED;
				isLongClick=false;
			}
		} else {
			// reset the selection state
			state = StateListener.STATE_NORMAL;
			isLongClick=false;
		}
		final int s = state;
		post(new Runnable() {
			public void run() {
				changeState(s);
			}
		});
	}

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        boolean hasSelection = hasSelection();
        boolean res = super.onTouchEvent(evt);
        if ((MotionEvent.ACTION_DOWN == evt.getActionMasked())
              && hasSelection) {
            postDelayed(new Runnable() {
                public void run() {
                    changeState(StateListener.STATE_EDITED);
                }}, POST_DELAY);
        }
        return res;
    }

    /**
     * check if focus change requires a title bar update
     */
    boolean needsUpdate() {
        return mNeedsUpdate;
    }

    /**
     * clear the focus change needs title bar update flag
     */
    void clearNeedsUpdate() {
        mNeedsUpdate = false;
    }

	void setController(UiController controller) {

		mUiController = controller;
		
		//2015-3-10 Vulcan Yang
		//屏蔽代码不再打开复制粘贴窗口
		/*
		UrlSelectionActionMode urlSelectionMode = new UrlSelectionActionMode(
				controller);
		setCustomSelectionActionModeCallback(urlSelectionMode);
		*/
	}

    void setContainer(View container) {
        mContainer = container;
    }

    public void setUrlInputListener(UrlInputListener listener) {
        mListener = listener;
    }

    public void setStateListener(StateListener listener) {
        mStateListener = listener;
        // update listener
        changeState(mState);
    }

    private void changeState(int newState) {
        mState = newState;
        if (mStateListener != null) {
            mStateListener.onStateChanged(mState);
        }
    }

    int getState() {
        return mState;
    }

    @Override
    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        mLandscape = (config.orientation &
                Configuration.ORIENTATION_LANDSCAPE) != 0;
        mAdapter.setLandscapeMode(mLandscape);
        if (isPopupShowing() && (getVisibility() == View.VISIBLE)) {
            setupDropDown();
            performFiltering(getText(), 0);
        }
    }

    @Override
    public void showDropDown() {
        setupDropDown();
        super.showDropDown();
    }

    @Override
    public void dismissDropDown() {
        super.dismissDropDown();
        mAdapter.clearCache();
    }

    private void setupDropDown() {
        int width = mContainer != null ? mContainer.getWidth() : getWidth();
        width += mPopupPadding.left + mPopupPadding.right;
//        if (width != getDropDownWidth()) {
//            setDropDownWidth(width);
//        }
        setDropDownWidth(width);
        setDropDownHeight((int)mContext.getResources().getDimension(com.android.browser.R.dimen.suggest_drop_height));
        setDropDownVerticalOffset(18);
//        int left = getLeft();
//        left += mPopupPadding.left;
//        if (left != -getDropDownHorizontalOffset()) {
//            setDropDownHorizontalOffset(-left);
//        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    	Log.i("xie1", "_____urlinput______________onEditorAction____________________");
        finishInput(getText().toString(), null, TYPED);
        return true;
    }

    public String getUrlText(){
        return getText().toString();
    }

    public void forceFilter() {
        showDropDown();
        performFiltering(getText(), 0);
    }

    void hideIME() {
        mInputManager.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    void showIME() {
        mInputManager.focusIn(this);
        mInputManager.showSoftInput(this, 0);
    }

    public void finishInput(String url, String extra, String source) {
        mNeedsUpdate = true;
        dismissDropDown();
        mInputManager.hideSoftInputFromWindow(getWindowToken(), 0);
        if (TextUtils.isEmpty(url)) {
            mListener.onDismiss();
        } else {
            if (mIncognitoMode && isSearch(url)) {
                // To prevent logging, intercept this request
                // TODO: This is a quick hack, refactor this
                SearchEngine searchEngine = BrowserSettings.getInstance()
                        .getSearchEngine();
                Log.i("xie1", "________SearchEngine__________name_______________"+searchEngine.getName());
                if (searchEngine == null) return;
                SearchEngineInfo engineInfo = SearchEngines
                        .getSearchEngineInfo(mContext, searchEngine.getName());
                if (engineInfo == null) return;
                url = engineInfo.getSearchUriForQuery(url);
                // mLister.onAction can take it from here without logging
            }
            mListener.onAction(url, extra, source);
        }
    }

    boolean isSearch(String inUrl) {
        String url = UrlUtils.fixUrl(inUrl).trim().replaceAll("。", ".");
        if (TextUtils.isEmpty(url)) return false;

        if (Patterns.WEB_URL.matcher(url).matches()
                || UrlUtils.ACCEPTED_URI_SCHEMA.matcher(url).matches()) {
            return false;
        }
        return true;
    }

    // Completion Listener

    @Override
    public void onSearch(String search) {
        mListener.onCopySuggestion(search);
    }

    @Override
    public void onSelect(String url, int type, String extra) {
    	Log.i("xie1", "_____urlinput______________onSelect____________________");
        finishInput(url, extra, SUGGESTED);
    }
    
    @Override
	public void onBookmarkOrHistory(ComboViews comboView) {
    	
    	//2015-3-10 Vulcan Yang
    	//因为屏蔽复制粘贴窗口，需要同时屏蔽此处代码
    	
    	//UrlSelectionActionMode urlSelectionMode = (UrlSelectionActionMode) getCustomSelectionActionModeCallback();
    	//urlSelectionMode.mUiController.bookmarksOrHistoryPicker(comboView);

    	mUiController.bookmarksOrHistoryPicker(comboView);
	}

    @Override
    public void onItemClick(
            AdapterView<?> parent, View view, int position, long id) {
        SuggestItem item = mAdapter.getItem(position);
        onSelect(SuggestionsAdapter.getSuggestionUrl(item), item.type, item.extra);
    }

    interface UrlInputListener {

        public void onDismiss();

        public void onAction(String text, String extra, String source);

        public void onCopySuggestion(String text);

    }

    public void setIncognitoMode(boolean incognito) {
        mIncognitoMode = incognito;
        mAdapter.setIncognitoMode(mIncognitoMode);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent evt) {
    	if(keyCode == KeyEvent.KEYCODE_BACK){
    		stopTextSelectionMode();
    	}
        if (keyCode == KeyEvent.KEYCODE_ESCAPE && !isInTouchMode()) {
            finishInput(null, null, null);
            return true;
        }
        return super.onKeyDown(keyCode, evt);
    }

    public SuggestionsAdapter getAdapter() {
        return mAdapter;
    }

    /*
     * no-op to prevent scrolling of webview when embedded titlebar
     * gets edited
     */
    @Override
    public boolean requestRectangleOnScreen(Rect rect, boolean immediate) {
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    	
        if (StateListener.STATE_HIGHLIGHTED == mState) {
            changeState(StateListener.STATE_EDITED);
        }
    	
    	if(searchListener!=null){
    		 searchListener.onTextChange(s, start, before, count);	
    	}
       
    }

    @Override
    public void afterTextChanged(Editable s) { 
    	if(s != null && s.toString().equals("") && getAdapter() != null && getAdapter().mMixedResults != null && getAdapter().mMixedResults.items != null) {
    		if(getAdapter().mMixedResults.items.size() > 1) {
    			for(int i=getAdapter().mMixedResults.items.size()-1; i>0; i--) {
    				getAdapter().mMixedResults.items.remove(i);
    			}
    		}
    		getAdapter().notifyDataSetChanged();
    	}
    }
    public void setUrlInputIsSearchListener(UrlInputIsSearchListener lis) {
    	Log.i("xie1", "setUrlInputIsSearchListener________");
    	searchListener = lis;
    }
    interface UrlInputIsSearchListener {

        public void onTextChange(CharSequence s, int start, int before, int count);

    }

	@Override
	public boolean onLongClick(View arg0) {
		Log.i("xiexiujie", "______________onLongClick______________________");
		isLongClick=true;
		// TODO Auto-generated method stub
		return false;
	}

}
