
package aurora.lib.widget;

import java.util.ArrayList;
import java.util.WeakHashMap;

import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.aurora.lib.R;
import com.aurora.lib.utils.HomeKeyWatcher;
import com.aurora.lib.utils.HomeKeyWatcher.OnHomePressedListener;

// Gionee <fenglp> <2013-07-31> modify for CR00812456 end

/**
 * A widget that provides a user interface for the user to enter a search query
 * and submit a request to a search provider. Shows a list of query suggestions
 * or results, if available, and allows the user to pick a suggestion or result
 * to launch into.
 * <p>
 * When the SearchView is used in an ActionBar as an action view for a
 * collapsible menu item, it needs to be set to iconified by default using
 * {@link #setIconifiedByDefault(boolean) setIconifiedByDefault(true)}. This is
 * the default, so nothing needs to be done.
 * </p>
 * <p>
 * If you want the search field to always be visible, then call
 * setIconifiedByDefault(false).
 * </p>
 * <div class="special reference"> <h3>Developer Guides</h3>
 * <p>
 * For information about using {@code SearchView}, read the
 * <a href="{@docRoot}, read the <a
 * href="{@docRoot}guide/topics/search/index.html">Search</a> developer guide.
 * </p>
 * </div>
 * 
 * @see android.view.MenuItem#SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
 * @attr ref android.R.styleable#SearchView_iconifiedByDefault
 * @attr ref android.R.styleable#SearchView_imeOptions
 * @attr ref android.R.styleable#SearchView_inputType
 * @attr ref android.R.styleable#SearchView_maxWidth
 * @attr ref android.R.styleable#SearchView_queryHint
 */
public class AuroraSearchView extends LinearLayout implements CollapsibleActionView {

    private static final boolean DBG = false;
    private static final String LOG_TAG = "SearchView";

    private static final int DEFAULT_MAX_LENGTH = 500;
    /**
     * Private constant for removing the microphone in the keyboard.
     */
    private static final String IME_OPTION_NO_MICROPHONE = "nm";

    private OnQueryTextListener mOnQueryChangeListener;
    private OnCloseListener mOnCloseListener;
    private OnFocusChangeListener mOnQueryTextFocusChangeListener;
    private OnSuggestionListener mOnSuggestionListener;
    private OnClickListener mOnSearchClickListener;

    private boolean mIconifiedByDefault;
    private boolean mIconified;
    private CursorAdapter mSuggestionsAdapter;
    private View mSearchButton;
    // private View mSubmitButton;
    private View mSearchPlate;
    // private View mSubmitArea;
    private ImageView mCloseButton;
    private View mSearchEditFrame;
    // private View mVoiceButton;
    private SearchAutoComplete mQueryTextView;
    private View mDropDownAnchor;
    private ImageView mSearchHintIcon;
    private boolean mSubmitButtonEnabled;
    private CharSequence mQueryHint;
    private boolean mQueryRefinement;
    private boolean mClearingFocus;
    private int mMaxWidth;
    private boolean mVoiceButtonEnabled;
    private CharSequence mOldQueryText;
    private CharSequence mUserQuery;
    private boolean mExpandedInActionView;
    private int mCollapsedImeOptions;

    private SearchableInfo mSearchable;
    private Bundle mAppSearchData;

    private View mSearchBar;
    private View mParent;
    private ImageView mHintIcon;
    private TextView mHintText;

    private InputMethodManager imm;

    private Context mContext;

    private boolean clickable;

    private ImageView coverImage;

    private boolean mClickable = true;

    private View mSearchViewBG;

    private HomeKeyWatcher mHomeKeyWatcher;

    private OnCloseButtonClickListener mOnCloseButtonClickListener;
    /*
     * SearchView can be set expanded before the IME is ready to be shown during
     * initial UI setup. The show operation is asynchronous to account for this.
     */
    private Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                // imm.showSoftInputUnchecked(0, null);
                // imm.showSoftInput(AuroraSearchView.this, 0, null);
                // imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                // imm.showSoftInput(mQueryTextView,
                // InputMethodManager.SHOW_FORCED);
            }
        }
    };

    private Runnable mUpdateDrawableStateRunnable = new Runnable() {
        public void run() {
            updateFocusedState();
        }
    };

    private Runnable mReleaseCursorRunnable = new Runnable() {
        public void run() {
        }
    };

    // For voice searching
    private final Intent mVoiceWebSearchIntent;
    private final Intent mVoiceAppSearchIntent;

    // A weak map of drawables we've gotten from other packages, so we don't
    // load them
    // more than once.
    private final WeakHashMap<String, Drawable.ConstantState> mOutsideDrawablesCache =
            new WeakHashMap<String, Drawable.ConstantState>();

    public interface OnCloseButtonClickListener {
        void onClick(boolean isClicked);
    }

    /**
     * Callbacks for changes to the query text.
     */
    public interface OnQueryTextListener {

        /**
         * Called when the user submits the query. This could be due to a key
         * press on the keyboard or due to pressing a submit button. The
         * listener can override the standard behavior by returning true to
         * indicate that it has handled the submit request. Otherwise return
         * false to let the SearchView handle the submission by launching any
         * associated intent.
         * 
         * @param query the query text that is to be submitted
         * @return true if the query has been handled by the listener, false to
         *         let the SearchView perform the default action.
         */
        boolean onQueryTextSubmit(String query);

        /**
         * Called when the query text is changed by the user.
         * 
         * @param newText the new content of the query text field.
         * @return false if the SearchView should perform the default action of
         *         showing any suggestions if available, true if the action was
         *         handled by the listener.
         */
        boolean onQueryTextChange(String newText);
    }

    public interface OnCloseListener {

        /**
         * The user is attempting to close the SearchView.
         * 
         * @return true if the listener wants to override the default behavior
         *         of clearing the text field and dismissing it, false
         *         otherwise.
         */
        boolean onClose();
    }

    /**
     * Callback interface for selection events on suggestions. These callbacks
     * are only relevant when a SearchableInfo has been specified by
     * {@link #setSearchableInfo}.
     */
    public interface OnSuggestionListener {

        /**
         * Called when a suggestion was selected by navigating to it.
         * 
         * @param position the absolute position in the list of suggestions.
         * @return true if the listener handles the event and wants to override
         *         the default behavior of possibly rewriting the query based on
         *         the selected item, false otherwise.
         */
        boolean onSuggestionSelect(int position);

        /**
         * Called when a suggestion was clicked.
         * 
         * @param position the absolute position of the clicked item in the list
         *            of suggestions.
         * @return true if the listener handles the event and wants to override
         *         the default behavior of launching any intent or submitting a
         *         search query specified on that item. Return false otherwise.
         */
        boolean onSuggestionClick(int position);
    }

    public AuroraSearchView(Context context) {
        this(context, null);
    }

    public AuroraSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Gionee zhangxx 2012-11-05 add for CR00715173 begin
        // inflater.inflate(R.layout.search_view, this, true);
        inflater.inflate(R.layout.aurora_search_view, this, true);
        // Gionee zhangxx 2012-11-05 add for CR00715173 begin

        mSearchButton = findViewById(R.id.aurora_search_button);
        mQueryTextView = (SearchAutoComplete) findViewById(R.id.aurora_search_src_text);
        mQueryTextView.setSearchView(this);

        mSearchEditFrame = findViewById(R.id.aurora_search_edit_frame);
        mSearchPlate = findViewById(R.id.aurora_search_plate);
        // mSubmitArea =
        // findViewById(R.id.aurora_submit_area);
        // mSubmitButton =
        // findViewById(R.id.aurora_search_go_btn);
        mCloseButton = (ImageView) findViewById(R.id.aurora_search_close_btn);
        // mVoiceButton =
        // findViewById(R.id.aurora_search_voice_btn);
        mSearchHintIcon = (ImageView) findViewById(R.id.aurora_search_mag_icon);
        mSearchBar = findViewById(R.id.aurora_searchview_bar);
        mHintIcon = (ImageView) findViewById(R.id.aurora_search_hint_icon);
        mHintText = (TextView) findViewById(R.id.aurora_search_hint_text);
        mParent = findViewById(R.id.aurora_search_view_parent);
        // coverImage =
        // (ImageView)findViewById(R.id.auroara_searchview_convert);
        mSearchViewBG = findViewById(R.id.aurora_searchview_bg);
        mSearchButton.setOnClickListener(mOnClickListener);
        mCloseButton.setOnClickListener(mOnClickListener);
        // mSubmitButton.setOnClickListener(mOnClickListener);
        // mVoiceButton.setOnClickListener(mOnClickListener);
        mQueryTextView.setOnClickListener(mOnClickListener);

        mQueryTextView.addTextChangedListener(mTextWatcher);
        mQueryTextView.setOnItemClickListener(mOnItemClickListener);
        mQueryTextView.setOnItemSelectedListener(mOnItemSelectedListener);
        mQueryTextView.setOnKeyListener(mTextKeyListener);
        setMaxLength(DEFAULT_MAX_LENGTH);
        // Inform any listener of focus changes
        // mQueryTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
        //
        // public void onFocusChange(View v, boolean hasFocus) {
        // if (mOnQueryTextFocusChangeListener != null) {
        // Log.e("mQueryTextView", "mQueryTextView="+hasFocus);
        // mOnQueryTextFocusChangeListener.onFocusChange(v, hasFocus);
        // }
        // }
        // });
        OnTouchListener touchListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mClickable) {
                    if (imm != null) {
                        imm.showSoftInput(mQueryTextView, InputMethodManager.SHOW_FORCED);
                    }
                    mQueryTextView.requestFocus();
                }
                return true;
            }

        };
        // mQueryTextView.setOnTouchListener(touchListener);
        mSearchPlate.setOnTouchListener(touchListener);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchView, 0, 0);
        // setIconifiedByDefault(a.getBoolean(android.R.styleable.SearchView_iconifiedByDefault,
        // true));
        int maxWidth = a.getDimensionPixelSize(R.styleable.SearchView_maxWidth, -1);
        if (maxWidth != -1) {
            setMaxWidth(maxWidth);
        }
        CharSequence queryHint = a.getText(R.styleable.SearchView_queryHint);
        if (!TextUtils.isEmpty(queryHint)) {
            setQueryHint(queryHint);
        }
        int imeOptions = a.getInt(R.styleable.SearchView_imeOptions, -1);
        if (imeOptions != -1) {
            setImeOptions(imeOptions);
        }
        int inputType = a.getInt(R.styleable.SearchView_inputType, -1);
        if (inputType != -1) {
            setInputType(inputType);
        }

        a.recycle();

        boolean focusable = true;
        Drawable background = null;

        a = context.obtainStyledAttributes(attrs, R.styleable.SearchView, 0, 0);
        focusable = a.getBoolean(R.styleable.SearchView_focusable, focusable);
        background = a.getDrawable(R.styleable.SearchView_background);
        a.recycle();

        int maxLength = 0;
        ColorStateList textColor = null;
        a = context.obtainStyledAttributes(attrs, R.styleable.SearchView, 0, 0);
        maxLength = a.getInteger(R.styleable.SearchView_maxLength, 0);
        textColor = a.getColorStateList(R.styleable.SearchView_textColor);
        a.recycle();

        if (textColor != null) {
            mQueryTextView.setTextColor(textColor);
        }

        if (maxLength > 0) {
            addLengthFilter(maxLength);
        }
        setFocusable(focusable);
        if (background != null) {
            mParent.setBackground(background);
        }
        // Save voice intent for later queries/launching
        mVoiceWebSearchIntent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
        mVoiceWebSearchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mVoiceWebSearchIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

        mVoiceAppSearchIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mVoiceAppSearchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mDropDownAnchor = findViewById(mQueryTextView.getDropDownAnchor());
        if (mDropDownAnchor != null) {
            mDropDownAnchor.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                        int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    adjustDropDownSizeAndPosition();
                }
            });
        }

        updateViewsVisibility(mIconifiedByDefault);
        updatePadding();
        updateQueryHint();

        // Gionee <zhangxx><2013-05-09> add for CR00811583 begin
        mIsGioneeViewStyle = isGioneeViewStyle();
        mIsGioneeWidget3Support = isGioneeWidget3Support();
        if (mIsGioneeViewStyle && mIsGioneeWidget3Support) {
            initSearchView();
            // Gionee <fenglp> <2013-08-01> add for CR00812456 begin
            startBeginAnimation();
            // Gionee <fenglp> <2013-08-01> add for CR00812456 end
        }
        // Gionee <zhangxx><2013-05-09> add for CR00811583 end
        // regesterHomeKeyEvent();
        
    }

    private void regesterHomeKeyEvent() {
        mHomeKeyWatcher = new HomeKeyWatcher(mContext);
        mHomeKeyWatcher.setOnHomePressedListener(new OnHomePressedListener() {

            @Override
            public void onHomePressed() {
                // TODO Auto-generated method stub
                Log.e("search", "onHomePressed()");
                if (mQueryTextView != null) {
                    mQueryTextView.clearFocus();
                }

            }

            @Override
            public void onHomeLongPressed() {
                // TODO Auto-generated method stub
                Log.e("search", "onHomeLongPressed()");
            }
        });
        mHomeKeyWatcher.startWatch();

    }

    public void setTextColor(int color) {
        if (mQueryTextView != null) {
            mQueryTextView.setTextColor(color);
        }
    }

    public void setTextSize(float textSize){
    	mQueryTextView.setTextSize(textSize);
    }
    
    public void setTextColor(ColorStateList colors) {
        if (mQueryTextView != null) {
            mQueryTextView.setTextColor(colors);
        }
    }

    public void setOnCloseButtonClickListener(OnCloseButtonClickListener listener) {
        this.mOnCloseButtonClickListener = listener;
    }

    public View getBackgroundView() {

        return mSearchViewBG;
    }

    /**
     * @param resID
     */
    public void setSearchViewBorder(int resID) {
        if (mSearchViewBG != null) {
            mSearchViewBG.setBackgroundResource(resID);
        }
    }

    /**
     * @param drawable
     */
    public void setSearchViewBorder(Drawable drawable) {
        if (mSearchViewBG != null) {
            mSearchViewBG.setBackground(drawable);
        }
    }

    /**
     * set delete button drawable
     * 
     * @param d
     */
    public void setDeleteButtonDrawable(Drawable d) {
        if (mCloseButton != null) {
            mCloseButton.setImageDrawable(d);
        }
    }

    /**
     * set delete button drawable
     * 
     * @param res
     */
    public void setDeleteButtonDrawable(int res) {
        if (mCloseButton != null) {
            mCloseButton.setImageResource(res);
        }
    }

    public void setPaddingLeft(int paddingLeft) {
        if (mParent != null) {
            mParent.setPadding(paddingLeft, mParent.getPaddingTop(),
                    mParent.getPaddingRight(), mParent.getPaddingBottom());
        }
    }

    public void setPaddingRight(int paddingRight) {
        if (mParent != null) {
            mParent.setPadding(mParent.getPaddingLeft(), mParent.getPaddingTop(),
                    paddingRight, mParent.getPaddingBottom());
        }
    }

    /**
     * set left icon drawable
     * 
     * @param d
     */
    public void setSearchIconDrawable(Drawable d) {
        if (mHintIcon != null) {
            mHintIcon.setImageDrawable(d);
        }
    }

    /**
     * set left icon drawable
     * 
     * @param res
     */
    public void setSearchIconDrawable(int res) {
        if (mHintIcon != null) {
            mHintIcon.setImageResource(res);
        }
    }

    /**
     * @param length
     */
    public void setMaxLength(final int length) {
        addLengthFilter(length);
    }

    private void addLengthFilter(final int length) {
        mQueryTextView.setFilters(new InputFilter[] {
            new LengthFilter(length)
        });
    }

    /**
     * get the QueryText Length
     * 
     * @return
     */
    public int getMaxLength() {
        return mQueryTextView != null ? mQueryTextView.length() : 0;
    }

    public void getFocus() {
      requestFocus();
        if (mClickable)
        {
            mQueryTextView.requestFocus();
            imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(mQueryTextView, InputMethodManager.SHOW_FORCED);

            }
        }
    	
    }
    
    public boolean hasFocus(){
    	return mQueryTextView.hasFocus();
    }

    public void clearText() {
        if (mQueryTextView != null) {
            mQueryTextView.setText("");
        }
    }

    public void setClickable(boolean clickable) {
        mQueryTextView.setEnabled(clickable);
        mClickable = clickable;
    }

    /**
     * Sets the SearchableInfo for this SearchView. Properties in the
     * SearchableInfo are used to display labels, hints, suggestions, create
     * intents for launching search results screens and controlling other
     * affordances such as a voice button.
     * 
     * @param searchable a SearchableInfo can be retrieved from the
     *            SearchManager, for a specific activity or a global search
     *            provider.
     */
    public void setSearchableInfo(SearchableInfo searchable) {
        mSearchable = searchable;
        if (mSearchable != null) {
            updateSearchAutoComplete();
            updateQueryHint();
        }
        // Cache the voice search capability
        mVoiceButtonEnabled = hasVoiceSearch();

        if (mVoiceButtonEnabled) {
            // Disable the microphone on the keyboard, as a mic is displayed
            // near the text box
            // TODO: use imeOptions to disable voice input when the new API will
            // be available
            mQueryTextView.setPrivateImeOptions(IME_OPTION_NO_MICROPHONE);
        }
        updateViewsVisibility(isIconified());
    }

    public void clearEditFocus() {
        if (mQueryTextView != null) {
            mQueryTextView.clearFocus();
            imm.hideSoftInputFromWindow(mQueryTextView.getWindowToken(), 0);
        }
    }

    public void closeButtonEnable(boolean flag) {
        if (mCloseButton == null) {
            return;
        }
        mCloseButton.setVisibility(flag ? VISIBLE : GONE);

    }

    /**
     * Sets the APP_DATA for legacy SearchDialog use.
     * 
     * @param appSearchData bundle provided by the app when launching the search
     *            dialog
     * @hide
     */
    public void setAppSearchData(Bundle appSearchData) {
        mAppSearchData = appSearchData;
    }

    public void setBackgroundColor(int color) {
        if (mSearchBar != null) {
            mSearchBar.setBackgroundColor(color);
        }

    }

    /**
     * Sets the IME options on the query text field.
     * 
     * @see TextView#setImeOptions(int)
     * @param imeOptions the options to set on the query text field
     * @attr ref android.R.styleable#SearchView_imeOptions
     */
    public void setImeOptions(int imeOptions) {
        mQueryTextView.setImeOptions(imeOptions);

    }

    /**
     * Returns the IME options set on the query text field.
     * 
     * @return the ime options
     * @see TextView#setImeOptions(int)
     * @attr ref android.R.styleable#SearchView_imeOptions
     */
    public int getImeOptions() {
        return mQueryTextView.getImeOptions();
    }

    /**
     * Sets the input type on the query text field.
     * 
     * @see TextView#setInputType(int)
     * @param inputType the input type to set on the query text field
     * @attr ref android.R.styleable#SearchView_inputType
     */
    public void setInputType(int inputType) {
        mQueryTextView.setInputType(inputType);
    }

    /**
     * Returns the input type set on the query text field.
     * 
     * @return the input type
     * @attr ref android.R.styleable#SearchView_inputType
     */
    public int getInputType() {
        return mQueryTextView.getInputType();
    }

    public void clickAble(boolean flag) {
        clickable = flag;
        if (flag) {
            mQueryTextView.setClickable(true);
            this.setClickable(true);
            mSearchPlate.setClickable(true);
        } else {
            mQueryTextView.setClickable(false);
            this.setClickable(false);
            mSearchPlate.setClickable(false);
        }
    }

    /** @hide */
    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        // Don't accept focus if in the middle of clearing focus
        if (mClearingFocus)
            return false;
        // Check if SearchView is focusable.
        if (!isFocusable())
            return false;
        // If it is not iconified, then give the focus to the text field
        if (!isIconified()) {
            boolean result = mQueryTextView.requestFocus(direction, previouslyFocusedRect);
            if (result) {
                updateViewsVisibility(false);
            }
            return result;
        } else {
            return super.requestFocus(direction, previouslyFocusedRect);
        }
    }

    /** @hide */
    @Override
    public void clearFocus() {
        mClearingFocus = true;
        setImeVisibility(false);
        super.clearFocus();
        mQueryTextView.clearFocus();
        mClearingFocus = false;
    }

    /**
     * Sets a listener for user actions within the SearchView.
     * 
     * @param listener the listener object that receives callbacks when the user
     *            performs actions in the SearchView such as clicking on buttons
     *            or typing a query.
     */
    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    /**
     * Sets a listener to inform when the user closes the SearchView.
     * 
     * @param listener the listener to call when the user closes the SearchView.
     */
    public void setOnCloseListener(OnCloseListener listener) {
        mOnCloseListener = listener;
    }

    /**
     * Sets a listener to inform when the focus of the query text field changes.
     * 
     * @param listener the listener to inform of focus changes.
     */
    public void setOnQueryTextFocusChangeListener(OnFocusChangeListener listener) {
        mOnQueryTextFocusChangeListener = listener;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        // mOnQueryTextFocusChangeListener = listener;
        mQueryTextView.setOnFocusChangeListener(listener);
    }

    /**
     * Sets a listener to inform when a suggestion is focused or clicked.
     * 
     * @param listener the listener to inform of suggestion selection events.
     */
    public void setOnSuggestionListener(OnSuggestionListener listener) {
        mOnSuggestionListener = listener;
    }

    /**
     * Sets a listener to inform when the search button is pressed. This is only
     * relevant when the text field is not visible by default. Calling
     * {@link #setIconified setIconified(false)} can also cause this listener to
     * be informed.
     * 
     * @param listener the listener to inform when the search button is clicked
     *            or the text field is programmatically de-iconified.
     */
    public void setOnSearchClickListener(OnClickListener listener) {
        mOnSearchClickListener = listener;
    }

    /**
     * Returns the query string currently in the text field.
     * 
     * @return the query string
     */
    public CharSequence getQuery() {
        return mQueryTextView.getText();
    }

    /**
     * Sets a query string in the text field and optionally submits the query as
     * well.
     * 
     * @param query the query string. This replaces any query text already
     *            present in the text field.
     * @param submit whether to submit the query right now or only update the
     *            contents of text field.
     */
    public void setQuery(CharSequence query, boolean submit) {
        mQueryTextView.setText(query);
        if (query != null) {
            mQueryTextView.setSelection(mQueryTextView.length());
            mUserQuery = query;
        }

        // If the query is not empty and submit is requested, submit the query
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery();
        }
    }

    /**
     * Sets the hint text to display in the query text field. This overrides any
     * hint specified in the SearchableInfo.
     * 
     * @param hint the hint text to display
     * @attr ref android.R.styleable#SearchView_queryHint
     */
    public void setQueryHint(CharSequence hint) {
        if (mHintText != null) {
            mHintText.setText(hint);
        }
        mQueryHint = hint;
        // mQueryTextView.setHint(mQueryHint);
        updateQueryHint();
    }

    private void updatePadding() {
        // int glassWidth = mHintIcon.getWidth();
        // int cursorPaddingLeft =
        // getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_search_view_hint_icon_padding_left)
        // +getResources().getDimensionPixelSize(com.aurora.R.dimen.aurora_search_view_hint_text_padding_left);
        // //+glassWidth;
        // cursorPaddingLeft = DensityUtil.dip2px(mContext, cursorPaddingLeft);
        // mQueryTextView.setPadding(cursorPaddingLeft,
        // mQueryTextView.getPaddingTop(), mQueryTextView.getPaddingRight(),
        // mQueryTextView.getPaddingBottom());
    }

    /**
     * Gets the hint text to display in the query text field.
     * 
     * @return the query hint text, if specified, null otherwise.
     * @attr ref android.R.styleable#SearchView_queryHint
     */
    public CharSequence getQueryHint() {
        if (mQueryHint != null) {
            return mQueryHint;
        } else if (mSearchable != null) {
            CharSequence hint = null;
            int hintId = mSearchable.getHintId();
            if (hintId != 0) {
                hint = getContext().getString(hintId);
            }
            return hint;
        }
        return null;
    }

    /**
     * Sets the default or resting state of the search field. If true, a single
     * search icon is shown by default and expands to show the text field and
     * other buttons when pressed. Also, if the default state is iconified, then
     * it collapses to that state when the close button is pressed. Changes to
     * this property will take effect immediately.
     * <p>
     * The default value is true.
     * </p>
     * 
     * @param iconified whether the search field should be iconified by default
     * @attr ref android.R.styleable#SearchView_iconifiedByDefault
     */
    public void setIconifiedByDefault(boolean iconified) {
        if (mIconifiedByDefault == iconified)
            return;
        mIconifiedByDefault = iconified;
        updateViewsVisibility(iconified);
        updateQueryHint();
    }

    /**
     * Returns the default iconified state of the search field.
     * 
     * @return
     * @attr ref android.R.styleable#SearchView_iconifiedByDefault
     */
    public boolean isIconfiedByDefault() {
        return mIconifiedByDefault;
    }

    /**
     * Iconifies or expands the SearchView. Any query text is cleared when
     * iconified. This is a temporary state and does not override the default
     * iconified state set by {@link #setIconifiedByDefault(boolean)}. If the
     * default state is iconified, then a false here will only be valid until
     * the user closes the field. And if the default state is expanded, then a
     * true here will only clear the text field and not close it.
     * 
     * @param iconify a true value will collapse the SearchView to an icon,
     *            while a false will expand it.
     */
    public void setIconified(boolean iconify) {
        if (iconify) {
            onCloseClicked();
        } else {
            onSearchClicked();
        }
    }

    /**
     * Returns the current iconified state of the SearchView.
     * 
     * @return true if the SearchView is currently iconified, false if the
     *         search field is fully visible.
     */
    public boolean isIconified() {
        return mIconified;
    }

    /**
     * Enables showing a submit button when the query is non-empty. In cases
     * where the SearchView is being used to filter the contents of the current
     * activity and doesn't launch a separate results activity, then the submit
     * button should be disabled.
     * 
     * @param enabled true to show a submit button for submitting queries, false
     *            if a submit button is not required.
     */
    public void setSubmitButtonEnabled(boolean enabled) {
        mSubmitButtonEnabled = enabled;
        updateViewsVisibility(isIconified());
    }

    /**
     * Returns whether the submit button is enabled when necessary or never
     * displayed.
     * 
     * @return whether the submit button is enabled automatically when necessary
     */
    public boolean isSubmitButtonEnabled() {
        return mSubmitButtonEnabled;
    }

    /**
     * Specifies if a query refinement button should be displayed alongside each
     * suggestion or if it should depend on the flags set in the individual
     * items retrieved from the suggestions provider. Clicking on the query
     * refinement button will replace the text in the query text field with the
     * text from the suggestion. This flag only takes effect if a SearchableInfo
     * has been specified with {@link #setSearchableInfo(SearchableInfo)} and
     * not when using a custom adapter.
     * 
     * @param enable true if all items should have a query refinement button,
     *            false if only those items that have a query refinement flag
     *            set should have the button.
     * @see SearchManager#SUGGEST_COLUMN_FLAGS
     * @see SearchManager#FLAG_QUERY_REFINEMENT
     */
    public void setQueryRefinementEnabled(boolean enable) {
//        mQueryRefinement = enable;
//        if (mSuggestionsAdapter instanceof AuroraSuggestionsAdapter) {
//            ((AuroraSuggestionsAdapter) mSuggestionsAdapter).setQueryRefinement(
//                    enable ? AuroraSuggestionsAdapter.REFINE_ALL
//                            : AuroraSuggestionsAdapter.REFINE_BY_ENTRY);
//        }
    }

    /**
     * Returns whether query refinement is enabled for all items or only
     * specific ones.
     * 
     * @return true if enabled for all items, false otherwise.
     */
    public boolean isQueryRefinementEnabled() {
        return mQueryRefinement;
    }

    /**
     * You can set a custom adapter if you wish. Otherwise the default adapter
     * is used to display the suggestions from the suggestions provider
     * associated with the SearchableInfo.
     * 
     * @see #setSearchableInfo(SearchableInfo)
     */
    public void setSuggestionsAdapter(CursorAdapter adapter) {
        mSuggestionsAdapter = adapter;

        mQueryTextView.setAdapter(mSuggestionsAdapter);
    }

    /**
     * Returns the adapter used for suggestions, if any.
     * 
     * @return the suggestions adapter
     */
    public CursorAdapter getSuggestionsAdapter() {
        return mSuggestionsAdapter;
    }

    /**
     * Makes the view at most this many pixels wide
     * 
     * @attr ref android.R.styleable#SearchView_maxWidth
     */
    public void setMaxWidth(int maxpixels) {
        mMaxWidth = maxpixels;

        requestLayout();
    }

    /**
     * Gets the specified maximum width in pixels, if set. Returns zero if no
     * maximum width was specified.
     * 
     * @return the maximum width of the view
     * @attr ref android.R.styleable#SearchView_maxWidth
     */
    public int getMaxWidth() {
        return mMaxWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Let the standard measurements take effect in iconified state.
        if (isIconified()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                // If there is an upper limit, don't exceed maximum width
                // (explicit or implicit)
                if (mMaxWidth > 0) {
                    width = Math.min(mMaxWidth, width);
                } else {
                    width = Math.min(getPreferredWidth(), width);
                }
                break;
            case MeasureSpec.EXACTLY:
                // If an exact width is specified, still don't exceed any
                // specified maximum width
                if (mMaxWidth > 0) {
                    width = Math.min(mMaxWidth, width);
                }
                break;
            case MeasureSpec.UNSPECIFIED:
                // Use maximum width, if specified, else preferred width
                width = mMaxWidth > 0 ? mMaxWidth : getPreferredWidth();
                break;
        }
        widthMode = MeasureSpec.EXACTLY;
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, widthMode), heightMeasureSpec);
    }

    private int getPreferredWidth() {
        return getContext().getResources()
                .getDimensionPixelSize(R.dimen.aurora_search_view_preferred_width);
    }

    private void updateViewsVisibility(final boolean collapsed) {
        // mIconified = collapsed;
        // // Visibility of views that are visible when collapsed
        // final int visCollapsed = collapsed ? VISIBLE : GONE;
        // // Is there text in the query
        final boolean hasText = !TextUtils.isEmpty(mQueryTextView.getText());
        //
        // mSearchButton.setVisibility(visCollapsed);
        // updateSubmitButton(hasText);
        // mSearchEditFrame.setVisibility(collapsed ? GONE : VISIBLE);
        // Gionee <fenglp> <2013-08-01> add for CR00812456 begin
        // mSearchHintIcon.setVisibility(mIconifiedByDefault ? GONE : VISIBLE);
        // Gionee <fenglp> <2013-08-01> add for CR00812456 end
        updateCloseButton();
        updateVoiceButton(!hasText);
        updateSubmitArea();
    }

    private boolean hasVoiceSearch() {
        if (mSearchable != null && mSearchable.getVoiceSearchEnabled()) {
            Intent testIntent = null;
            if (mSearchable.getVoiceSearchLaunchWebSearch()) {
                testIntent = mVoiceWebSearchIntent;
            } else if (mSearchable.getVoiceSearchLaunchRecognizer()) {
                testIntent = mVoiceAppSearchIntent;
            }
            if (testIntent != null) {
                ResolveInfo ri = getContext().getPackageManager().resolveActivity(testIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                return ri != null;
            }
        }
        return false;
    }

    private boolean isSubmitAreaEnabled() {
        return (mSubmitButtonEnabled || mVoiceButtonEnabled) && !isIconified();
    }

    private void updateSubmitButton(boolean hasText) {
        int visibility = GONE;
        if (mSubmitButtonEnabled && isSubmitAreaEnabled() && hasFocus()
                && (hasText || !mVoiceButtonEnabled)) {
            visibility = VISIBLE;
        }
        // mSubmitButton.setVisibility(visibility);
    }

    private void updateSubmitArea() {
        // int visibility = GONE;
        // if (isSubmitAreaEnabled()
        // && (mSubmitButton.getVisibility() == VISIBLE
        // || mVoiceButton.getVisibility() == VISIBLE)) {
        // visibility = VISIBLE;
        // }
        // mSubmitArea.setVisibility(visibility);
    }

    private void updateCloseButton() {
        final boolean hasText = !TextUtils.isEmpty(mQueryTextView.getText());
        // // Should we show the close button? It is not shown if there's no
        // focus,
        // // field is not iconified by default and there is no text in it.
        final boolean showClose = hasText || (mIconifiedByDefault && !mExpandedInActionView);
        // if(mOnCloseButtonClickListener != null){
        // mOnCloseButtonClickListener.onClick(false);
        // }
        mCloseButton.setVisibility(showClose ? VISIBLE : GONE);
        mCloseButton.getDrawable().setState(hasText ? ENABLED_STATE_SET : EMPTY_STATE_SET);
        // // Gionee <zhangxx><2013-05-09> add for CR00811583 begin
        // if (mIsGioneeViewStyle && mIsGioneeWidget3Support &&
        // mIsSearchVoiceMode) {
        // updateSearchVoiceButton(!showClose);
        // }
        // Gionee <zhangxx><2013-05-09> add for CR00811583 end
    }

    private void postUpdateFocusedState() {
        post(mUpdateDrawableStateRunnable);
    }

    private void updateFocusedState() {
        boolean focused = mQueryTextView.hasFocus();
        // mSearchPlate.getBackground().setState(focused ? FOCUSED_STATE_SET :
        // EMPTY_STATE_SET);
        // Gionee <fenglp> <2013-08-01> add for CR00812456 begin
        // mSubmitArea.getBackground().setState(focused ? FOCUSED_STATE_SET :
        // EMPTY_STATE_SET);
        // Gionee <fenglp> <2013-08-01> add for CR00812456 end
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(mUpdateDrawableStateRunnable);
        post(mReleaseCursorRunnable);
        if (mHomeKeyWatcher != null) {
            mHomeKeyWatcher.stopWatch();
        }
        super.onDetachedFromWindow();
    }

    private void setImeVisibility(final boolean visible) {
        if (visible) {
            post(mShowImeRunnable);
        } else {
            removeCallbacks(mShowImeRunnable);
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    /**
     * Called by the AuroraSuggestionsAdapter
     * 
     * @hide
     */
    /* package */void onQueryRefine(CharSequence queryText) {
        setQuery(queryText);
    }

    private final OnClickListener mOnClickListener = new OnClickListener() {

        public void onClick(View v) {
            if (v == mSearchButton) {
                onSearchClicked();
            } else if (v == mCloseButton) {
                onCloseClicked();
                /*
                 * } else if (v == mSubmitButton) { onSubmitQuery(); } else if
                 * (v == mVoiceButton) { onVoiceClicked();
                 */
            } else if (v == mQueryTextView) {
                forceSuggestionQuery();
            }
        }
    };

    /**
     * Handles the key down event for dealing with action keys.
     * 
     * @param keyCode This is the keycode of the typed key, and is the same
     *            value as found in the KeyEvent parameter.
     * @param event The complete event record for the typed key
     * @return true if the event was handled here, or false if not.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (mSearchable == null) {
//            return false;
//        }
//
//        // if it's an action specified by the searchable activity, launch the
//        // entered query with the action key
//        SearchableInfo.ActionKeyInfo actionKey = mSearchable.findActionKey(keyCode);
//        if ((actionKey != null) && (actionKey.getQueryActionMsg() != null)) {
//            launchQuerySearch(keyCode, actionKey.getQueryActionMsg(), mQueryTextView.getText()
//                    .toString());
//            return true;
//        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * React to the user typing "enter" or other hardwired keys while typing in
     * the search box. This handles these special keys while the edit box has
     * focus.
     */
    View.OnKeyListener mTextKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (DBG) {
                Log.d(LOG_TAG, "mTextListener.onKey(" + keyCode + "," + event + "), selection: "
                        + mQueryTextView.getListSelection());
            }

            // If a suggestion is selected, handle enter, search key, and action
            // keys
            // as presses on the selected suggestion
            if (mQueryTextView.isPopupShowing()
                    && mQueryTextView.getListSelection() != ListView.INVALID_POSITION) {
                return onSuggestionsKey(v, keyCode, event);
            }

            // If there is text in the query box, handle enter, and action keys
            // The search key is handled by the dialog's onKeyDown().
            if (!mQueryTextView.isEmpty() && event.hasNoModifiers()) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        v.cancelLongPress();
                        onSubmitQuery();
                        return true;
                    }
                }
//                if (mSearchable != null && event.getAction() == KeyEvent.ACTION_DOWN) {
//                    SearchableInfo.ActionKeyInfo actionKey = mSearchable.findActionKey(keyCode);
//                    if ((actionKey != null) && (actionKey.getQueryActionMsg() != null)) {
//                        launchQuerySearch(keyCode, actionKey.getQueryActionMsg(), mQueryTextView
//                                .getText().toString());
//                        return true;
//                    }
//                }
            }
            return false;
        }
    };

    /**
     * React to the user typing while in the suggestions list. First, check for
     * action keys. If not handled, try refocusing regular characters into the
     * EditText.
     */
    private boolean onSuggestionsKey(View v, int keyCode, KeyEvent event) {
//        // guard against possible race conditions (late arrival after dismiss)
//        if (mSearchable == null) {
//            return false;
//        }
//        if (mSuggestionsAdapter == null) {
//            return false;
//        }
//        if (event.getAction() == KeyEvent.ACTION_DOWN && event.hasNoModifiers()) {
//            // First, check for enter or search (both of which we'll treat as a
//            // "click")
//            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH
//                    || keyCode == KeyEvent.KEYCODE_TAB) {
//                int position = mQueryTextView.getListSelection();
//                return onItemClicked(position, KeyEvent.KEYCODE_UNKNOWN, null);
//            }
//
//            // Next, check for left/right moves, which we use to "return" the
//            // user to the edit view
//            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//                // give "focus" to text editor, with cursor at the beginning if
//                // left key, at end if right key
//                // TODO: Reverse left/right for right-to-left languages, e.g.
//                // Arabic
//                int selPoint = (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ? 0 : mQueryTextView
//                        .length();
//                mQueryTextView.setSelection(selPoint);
//                mQueryTextView.setListSelection(0);
//                mQueryTextView.clearListSelection();
//                mQueryTextView.ensureImeVisible(true);
//
//                return true;
//            }
//
//            // Next, check for an "up and out" move
//            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && 0 == mQueryTextView.getListSelection()) {
//                // TODO: restoreUserQuery();
//                // let ACTV complete the move
//                return false;
//            }
//
//            // Next, check for an "action key"
//            SearchableInfo.ActionKeyInfo actionKey = mSearchable.findActionKey(keyCode);
//            if ((actionKey != null)
//                    && ((actionKey.getSuggestActionMsg() != null) || (actionKey
//                            .getSuggestActionMsgColumn() != null))) {
//                // launch suggestion using action key column
//                int position = mQueryTextView.getListSelection();
//                if (position != ListView.INVALID_POSITION) {
//                    Cursor c = mSuggestionsAdapter.getCursor();
//                    if (c.moveToPosition(position)) {
//                        final String actionMsg = getActionKeyMessage(c, actionKey);
//                        if (actionMsg != null && (actionMsg.length() > 0)) {
//                            return onItemClicked(position, keyCode, actionMsg);
//                        }
//                    }
//                }
//            }
//        }
        return false;
    }

    /**
     * For a given suggestion and a given cursor row, get the action message. If
     * not provided by the specific row/column, also check for a single
     * definition (for the action key).
     * 
     * @param c The cursor providing suggestions
     * @param actionKey The actionkey record being examined
     * @return Returns a string, or null if no action key message for this
     *         suggestion
     */

    // private int getSearchIconId() {
    // TypedValue outValue = new TypedValue();
    // getContext().getTheme().resolveAttribute(com.android.internal.R.attr.searchViewSearchIcon,
    // outValue, true);
    // return outValue.resourceId;
    // }

    // Gionee zhangxx 2012-11-05 add for CR00715173 begin
    // private boolean noSearchIconInSearchView() {
    // TypedValue outValue = new TypedValue();
    //
    // boolean rtn =
    // getContext().getTheme().resolveAttribute(com.android.internal.R.attr.noSearchIconInSearchView,
    // outValue, true);
    // return outValue.data != 0;
    // }

    // private int getSearchViewLayoutId() {
    // TypedValue outValue = new TypedValue();
    //
    // boolean rtn =
    // getContext().getTheme().resolveAttribute(com.android.internal.R.attr.SearchViewLayout,
    // outValue, true);
    // return outValue.resourceId <= 0 ? R.layout.aurora_search_view :
    // outValue.resourceId;
    // }
    // Gionee zhangxx 2012-11-05 add for CR00715173 end

    private CharSequence getDecoratedHint(CharSequence hintText) {
        // If the field is always expanded, then don't add the search icon to
        // the hint
        if (!mIconifiedByDefault)
            return hintText;

        // Gionee zhangxx 2012-11-05 add for CR00715173 begin
        // if (noSearchIconInSearchView()) {
        // return hintText;
        // }
        // Gionee zhangxx 2012-11-05 add for CR00715173 end

        SpannableStringBuilder ssb = new SpannableStringBuilder("   "); // for
                                                                        // the
                                                                        // icon
        ssb.append(hintText);
        // Drawable searchIcon =
        // getContext().getResources().getDrawable(getSearchIconId());
        int textSize = (int) (mQueryTextView.getTextSize() * 1.25);
        // searchIcon.setBounds(0, 0, textSize, textSize);
        // ssb.setSpan(new ImageSpan(searchIcon), 1, 2,
        // Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    private void updateQueryHint() {
        if (mQueryHint != null) {
            // mQueryTextView.setHint(mQueryHint);
            mHintText.setText(mQueryHint);
            Log.e("searchview hint", "hint=" + mQueryHint);
        } else if (mSearchable != null) {
            CharSequence hint = null;
            int hintId = mSearchable.getHintId();
            if (hintId != 0) {
                hint = getContext().getString(hintId);
            }
            if (hint != null) {
                // mQueryTextView.setHint(getDecoratedHint(hint));
                mHintText.setText(mQueryHint);
            }
        } else {
            // mQueryTextView.setHint(getDecoratedHint(""));
            mHintText.setText("");
        }
    }

    /**
     * Updates the auto-complete text view.
     */
    private void updateSearchAutoComplete() {
        mQueryTextView.setDropDownAnimationStyle(0); // no animation
        mQueryTextView.setThreshold(mSearchable.getSuggestThreshold());
        mQueryTextView.setImeOptions(mSearchable.getImeOptions());
        int inputType = mSearchable.getInputType();
        // We only touch this if the input type is set up for text (which it
        // almost certainly
        // should be, in the case of search!)
        if ((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) {
            // The existence of a suggestions authority is the proxy for
            // "suggestions
            // are available here"
            inputType &= ~InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
            if (mSearchable.getSuggestAuthority() != null) {
                inputType |= InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
                // TYPE_TEXT_FLAG_AUTO_COMPLETE means that the text editor is
                // performing
                // auto-completion based on its own semantics, which it will
                // present to the user
                // as they type. This generally means that the input method
                // should not show its
                // own candidates, and the spell checker should not be in
                // action. The text editor
                // supplies its candidates by calling
                // InputMethodManager.displayCompletions(),
                // which in turn will call
                // InputMethodSession.displayCompletions().
                inputType |= InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
            }
        }
        mQueryTextView.setInputType(inputType);
        if (mSuggestionsAdapter != null) {
            mSuggestionsAdapter.changeCursor(null);
        }
        // attach the suggestions adapter, if suggestions are available
        // The existence of a suggestions authority is the proxy for
        // "suggestions available here"
//        if (mSearchable.getSuggestAuthority() != null) {
//            mSuggestionsAdapter = new AuroraSuggestionsAdapter(getContext(),
//                    this, mSearchable, mOutsideDrawablesCache);
//            mQueryTextView.setAdapter(mSuggestionsAdapter);
//            ((AuroraSuggestionsAdapter) mSuggestionsAdapter).setQueryRefinement(
//                    mQueryRefinement ? AuroraSuggestionsAdapter.REFINE_ALL
//                            : AuroraSuggestionsAdapter.REFINE_BY_ENTRY);
//        }
    }

    /**
     * Update the visibility of the voice button. There are actually two voice
     * search modes, either of which will activate the button.
     * 
     * @param empty whether the search query text field is empty. If it is, then
     *            the other criteria apply to make the voice button visible.
     */
    private void updateVoiceButton(boolean empty) {
        int visibility = GONE;
        if (mVoiceButtonEnabled && !isIconified() && empty) {
            visibility = VISIBLE;
            // mSubmitButton.setVisibility(GONE);
        }
        // mVoiceButton.setVisibility(visibility);
    }

    private void onTextChanged(CharSequence newText) {
        CharSequence text = mQueryTextView.getText();
        mUserQuery = text;
        boolean hasText = !TextUtils.isEmpty(text);
        updateSubmitButton(hasText);
        updateVoiceButton(!hasText);
        updateCloseButton();
        updateSubmitArea();
        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText)) {
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        mOldQueryText = newText.toString();
    }

    private void onSubmitQuery() {
        CharSequence query = mQueryTextView.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (mOnQueryChangeListener == null
                    || !mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
                if (mSearchable != null) {
                    launchQuerySearch(KeyEvent.KEYCODE_UNKNOWN, null, query.toString());
                    setImeVisibility(false);
                }
                dismissSuggestions();
            }
        }
    }

    private void dismissSuggestions() {
        mQueryTextView.dismissDropDown();
    }

    private void onCloseClicked() {
        if (mOnCloseButtonClickListener != null) {
            mOnCloseButtonClickListener.onClick(true);
        }
        CharSequence text = mQueryTextView.getText();
        if (TextUtils.isEmpty(text)) {
            if (mOnCloseListener == null || !mOnCloseListener.onClose()) {
                // hide the keyboard and remove focus
                // clearFocus();
                // collapse the search field
                updateViewsVisibility(true);
            }
            if (mIconifiedByDefault) {
                // If the app doesn't override the close behavior
                // if (mOnCloseListener == null || !mOnCloseListener.onClose())
                // {
                // // hide the keyboard and remove focus
                // clearFocus();
                // // collapse the search field
                // updateViewsVisibility(true);
                // }
            }
        } else {
            mQueryTextView.setText("");
            // mQueryTextView.requestFocus();
            // clearFocus();
            setImeVisibility(true);
        }

    }

    private void onSearchClicked() {
        updateViewsVisibility(false);
        mQueryTextView.requestFocus();
        // setImeVisibility(true);
        if (mOnSearchClickListener != null) {
            mOnSearchClickListener.onClick(this);
        }
    }

    // private void onVoiceClicked() {
    // // guard against possible race conditions
    // if (mSearchable == null) {
    // return;
    // }
    // SearchableInfo searchable = mSearchable;
    // try {
    // if (searchable.getVoiceSearchLaunchWebSearch()) {
    // Intent webSearchIntent =
    // createVoiceWebSearchIntent(mVoiceWebSearchIntent,
    // searchable);
    // getContext().startActivity(webSearchIntent);
    // } else if (searchable.getVoiceSearchLaunchRecognizer()) {
    // Intent appSearchIntent =
    // createVoiceAppSearchIntent(mVoiceAppSearchIntent,
    // searchable);
    // getContext().startActivity(appSearchIntent);
    // }
    // } catch (ActivityNotFoundException e) {
    // // Should not happen, since we check the availability of
    // // voice search before showing the button. But just in case...
    // Log.w(LOG_TAG, "Could not find voice search activity");
    // }
    // }

    void onTextFocusChanged() {
        updateViewsVisibility(isIconified());
        // Delayed update to make sure that the focus has settled down and
        // window focus changes
        // don't affect it. A synchronous update was not working.
        postUpdateFocusedState();
        if (mOnQueryTextFocusChangeListener != null) {
            mOnQueryTextFocusChangeListener
                    .onFocusChange(mQueryTextView, mQueryTextView.hasFocus());
        }
        if (mQueryTextView.hasFocus()) {
            forceSuggestionQuery();
        } else {
            if (imm != null) {
                imm.hideSoftInputFromWindow(mQueryTextView.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        postUpdateFocusedState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActionViewCollapsed() {
        clearFocus();
        updateViewsVisibility(true);
        mQueryTextView.setImeOptions(mCollapsedImeOptions);
        mExpandedInActionView = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActionViewExpanded() {
        if (mExpandedInActionView)
            return;

        mExpandedInActionView = true;
        mCollapsedImeOptions = mQueryTextView.getImeOptions();
        mQueryTextView.setImeOptions(mCollapsedImeOptions | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        mQueryTextView.setText("");
        setIconified(false);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AuroraSearchView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AuroraSearchView.class.getName());
    }

    private void adjustDropDownSizeAndPosition() {
        if (mDropDownAnchor.getWidth() > 1) {
            Resources res = getContext().getResources();
            int anchorPadding = mSearchPlate.getPaddingLeft();
            Rect dropDownPadding = new Rect();
            // zhangxx 2013-07-02 test begin
            final boolean isLayoutRtl = false;// isLayoutRtl();
            // zhangxx 2013-07-02 test end
            int iconOffset = mIconifiedByDefault
                    ? res.getDimensionPixelSize(R.dimen.aurora_dropdownitem_icon_width)
                            + res.getDimensionPixelSize(R.dimen.aurora_dropdownitem_text_padding_left)
                    : 0;
            mQueryTextView.getDropDownBackground().getPadding(dropDownPadding);
            int offset;
            if (isLayoutRtl) {
                offset = -dropDownPadding.left;
            } else {
                offset = anchorPadding - (dropDownPadding.left + iconOffset);
            }
            mQueryTextView.setDropDownHorizontalOffset(offset);
            final int width = mDropDownAnchor.getWidth() + dropDownPadding.left
                    + dropDownPadding.right + iconOffset - anchorPadding;
            mQueryTextView.setDropDownWidth(width);
        }
    }

    private boolean onItemClicked(int position, int actionKey, String actionMsg) {
        if (mOnSuggestionListener == null
                || !mOnSuggestionListener.onSuggestionClick(position)) {
            launchSuggestion(position, KeyEvent.KEYCODE_UNKNOWN, null);
            setImeVisibility(false);
            dismissSuggestions();
            return true;
        }
        return false;
    }

    private boolean onItemSelected(int position) {
        if (mOnSuggestionListener == null
                || !mOnSuggestionListener.onSuggestionSelect(position)) {
            rewriteQueryFromSuggestion(position);
            return true;
        }
        return false;
    }

    private final OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        /**
         * Implements OnItemClickListener
         */
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (DBG)
                Log.d(LOG_TAG, "onItemClick() position " + position);
            onItemClicked(position, KeyEvent.KEYCODE_UNKNOWN, null);
        }
    };

    private final OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {

        /**
         * Implements OnItemSelectedListener
         */
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (DBG)
                Log.d(LOG_TAG, "onItemSelected() position " + position);
            AuroraSearchView.this.onItemSelected(position);
        }

        /**
         * Implements OnItemSelectedListener
         */
        public void onNothingSelected(AdapterView<?> parent) {
            if (DBG)
                Log.d(LOG_TAG, "onNothingSelected()");
        }
    };

    // zhangxx 2013-07-02 test begin
    /*
     * @Override public void onRtlPropertiesChanged(int layoutDirection) {
     * mQueryTextView.setLayoutDirection(layoutDirection); }
     */
    // zhangxx 2013-07-02 test end

    /**
     * Query rewriting.
     */
    private void rewriteQueryFromSuggestion(int position) {
        CharSequence oldQuery = mQueryTextView.getText();
        Cursor c = mSuggestionsAdapter.getCursor();
        if (c == null) {
            return;
        }
        if (c.moveToPosition(position)) {
            // Get the new query from the suggestion.
            CharSequence newQuery = mSuggestionsAdapter.convertToString(c);
            if (newQuery != null) {
                // The suggestion rewrites the query.
                // Update the text field, without getting new suggestions.
                setQuery(newQuery);
            } else {
                // The suggestion does not rewrite the query, restore the user's
                // query.
                setQuery(oldQuery);
            }
        } else {
            // We got a bad position, restore the user's query.
            setQuery(oldQuery);
        }
    }

    /**
     * Launches an intent based on a suggestion.
     * 
     * @param position The index of the suggestion to create the intent from.
     * @param actionKey The key code of the action key that was pressed, or
     *            {@link KeyEvent#KEYCODE_UNKNOWN} if none.
     * @param actionMsg The message for the action key that was pressed, or
     *            <code>null</code> if none.
     * @return true if a successful launch, false if could not (e.g. bad
     *         position).
     */
    private boolean launchSuggestion(int position, int actionKey, String actionMsg) {
        Cursor c = mSuggestionsAdapter.getCursor();
        if ((c != null) && c.moveToPosition(position)) {

            Intent intent = createIntentFromSuggestion(c, actionKey, actionMsg);

            // launch the intent
            launchIntent(intent);

            return true;
        }
        return false;
    }

    /**
     * Launches an intent, including any special intent handling.
     */
    private void launchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            // If the intent was created from a suggestion, it will always have
            // an explicit
            // component here.
            getContext().startActivity(intent);
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "Failed launch activity: " + intent, ex);
        }
    }

    /**
     * Sets the text in the query box, without updating the suggestions.
     */
    private void setQuery(CharSequence query) {
        mQueryTextView.setText(query, true);
        // Move the cursor to the end
        mQueryTextView.setSelection(TextUtils.isEmpty(query) ? 0 : query.length());
    }

    private void launchQuerySearch(int actionKey, String actionMsg, String query) {
        String action = Intent.ACTION_SEARCH;
        Intent intent = createIntent(action, null, null, query, actionKey, actionMsg);
        getContext().startActivity(intent);
    }

    /**
     * Constructs an intent from the given information and the search dialog
     * state.
     * 
     * @param action Intent action.
     * @param data Intent data, or <code>null</code>.
     * @param extraData Data for {@link SearchManager#EXTRA_DATA_KEY} or
     *            <code>null</code>.
     * @param query Intent query, or <code>null</code>.
     * @param actionKey The key code of the action key that was pressed, or
     *            {@link KeyEvent#KEYCODE_UNKNOWN} if none.
     * @param actionMsg The message for the action key that was pressed, or
     *            <code>null</code> if none.
     * @param mode The search mode, one of the acceptable values for
     *            {@link SearchManager#SEARCH_MODE}, or {@code null}.
     * @return The intent.
     */
    private Intent createIntent(String action, Uri data, String extraData, String query,
            int actionKey, String actionMsg) {
        // Now build the Intent
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // We need CLEAR_TOP to avoid reusing an old task that has other
        // activities
        // on top of the one we want. We don't want to do this in in-app search
        // though,
        // as it can be destructive to the activity stack.
        if (data != null) {
            intent.setData(data);
        }
        intent.putExtra(SearchManager.USER_QUERY, mUserQuery);
        if (query != null) {
            intent.putExtra(SearchManager.QUERY, query);
        }
        if (extraData != null) {
            intent.putExtra(SearchManager.EXTRA_DATA_KEY, extraData);
        }
        if (mAppSearchData != null) {
            intent.putExtra(SearchManager.APP_DATA, mAppSearchData);
        }
        if (actionKey != KeyEvent.KEYCODE_UNKNOWN) {
            intent.putExtra(SearchManager.ACTION_KEY, actionKey);
            intent.putExtra(SearchManager.ACTION_MSG, actionMsg);
        }
        intent.setComponent(mSearchable.getSearchActivity());
        return intent;
    }

    /**
     * Create and return an Intent that can launch the voice search activity for
     * web search.
     */
    private Intent createVoiceWebSearchIntent(Intent baseIntent, SearchableInfo searchable) {
        Intent voiceIntent = new Intent(baseIntent);
        ComponentName searchActivity = searchable.getSearchActivity();
        voiceIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, searchActivity == null ? null
                : searchActivity.flattenToShortString());
        return voiceIntent;
    }

    /**
     * Create and return an Intent that can launch the voice search activity,
     * perform a specific voice transcription, and forward the results to the
     * searchable activity.
     * 
     * @param baseIntent The voice app search intent to start from
     * @return A completely-configured intent ready to send to the voice search
     *         activity
     */
    private Intent createVoiceAppSearchIntent(Intent baseIntent, SearchableInfo searchable) {
        ComponentName searchActivity = searchable.getSearchActivity();

        // create the necessary intent to set up a search-and-forward operation
        // in the voice search system. We have to keep the bundle separate,
        // because it becomes immutable once it enters the PendingIntent
        Intent queryIntent = new Intent(Intent.ACTION_SEARCH);
        queryIntent.setComponent(searchActivity);
        PendingIntent pending = PendingIntent.getActivity(getContext(), 0, queryIntent,
                PendingIntent.FLAG_ONE_SHOT);

        // Now set up the bundle that will be inserted into the pending intent
        // when it's time to do the search. We always build it here (even if
        // empty)
        // because the voice search activity will always need to insert "QUERY"
        // into
        // it anyway.
        Bundle queryExtras = new Bundle();
        if (mAppSearchData != null) {
            queryExtras.putParcelable(SearchManager.APP_DATA, mAppSearchData);
        }

        // Now build the intent to launch the voice search. Add all necessary
        // extras to launch the voice recognizer, and then all the necessary
        // extras
        // to forward the results to the searchable activity
        Intent voiceIntent = new Intent(baseIntent);

        // Add all of the configuration options supplied by the searchable's
        // metadata
        String languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
        String prompt = null;
        String language = null;
        int maxResults = 1;

        Resources resources = getResources();
        if (searchable.getVoiceLanguageModeId() != 0) {
            languageModel = resources.getString(searchable.getVoiceLanguageModeId());
        }
        if (searchable.getVoicePromptTextId() != 0) {
            prompt = resources.getString(searchable.getVoicePromptTextId());
        }
        if (searchable.getVoiceLanguageId() != 0) {
            language = resources.getString(searchable.getVoiceLanguageId());
        }
        if (searchable.getVoiceMaxResults() != 0) {
            maxResults = searchable.getVoiceMaxResults();
        }
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, searchActivity == null ? null
                : searchActivity.flattenToShortString());

        // Add the values that configure forwarding the results
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT, pending);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE, queryExtras);

        return voiceIntent;
    }

    /**
     * When a particular suggestion has been selected, perform the various
     * lookups required to use the suggestion. This includes checking the cursor
     * for suggestion-specific data, and/or falling back to the XML for
     * defaults; It also creates REST style Uri data when the suggestion
     * includes a data id.
     * 
     * @param c The suggestions cursor, moved to the row of the user's selection
     * @param actionKey The key code of the action key that was pressed, or
     *            {@link KeyEvent#KEYCODE_UNKNOWN} if none.
     * @param actionMsg The message for the action key that was pressed, or
     *            <code>null</code> if none.
     * @return An intent for the suggestion at the cursor's position.
     */
    private Intent createIntentFromSuggestion(Cursor c, int actionKey, String actionMsg) {/*
        try {
            // use specific action if supplied, or default action if supplied,
            // or fixed default
            String action = AuroraSuggestionsAdapter.getColumnString(c,
                    SearchManager.SUGGEST_COLUMN_INTENT_ACTION);

            if (action == null) {
                action = mSearchable.getSuggestIntentAction();
            }
            if (action == null) {
                action = Intent.ACTION_SEARCH;
            }

            // use specific data if supplied, or default data if supplied
            String data = AuroraSuggestionsAdapter.getColumnString(c,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA);
            if (data == null) {
                data = mSearchable.getSuggestIntentData();
            }
            // then, if an ID was provided, append it.
            if (data != null) {
                String id = AuroraSuggestionsAdapter.getColumnString(c,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
                if (id != null) {
                    data = data + "/" + Uri.encode(id);
                }
            }
            Uri dataUri = (data == null) ? null : Uri.parse(data);

            String query = AuroraSuggestionsAdapter.getColumnString(c,
                    SearchManager.SUGGEST_COLUMN_QUERY);
            String extraData = AuroraSuggestionsAdapter.getColumnString(c,
                    SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);

            return createIntent(action, dataUri, extraData, query, actionKey, actionMsg);
        } catch (RuntimeException e) {
            int rowNum;
            try { // be really paranoid now
                rowNum = c.getPosition();
            } catch (RuntimeException e2) {
                rowNum = -1;
            }
            Log.w(LOG_TAG, "Search suggestions cursor at row " + rowNum +
                    " returned exception.", e);
            return null;
        }
        return 
    */
    return null;	
    }

    private void forceSuggestionQuery() {
        mQueryTextView.doBeforeTextChanged();
        mQueryTextView.doAfterTextChanged();
    }

    static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Callback to watch the text field for empty/non-empty
     */
    private TextWatcher mTextWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int before, int after) {
        }

        public void onTextChanged(CharSequence s, int start,
                int before, int after) {
            AuroraSearchView.this.onTextChanged(s);
            if ("".equals(s.toString())) {
                // mHintIcon.setVisibility(View.VISIBLE);
                Log.e("hint", "hint visible");
                mHintText.setVisibility(View.VISIBLE);
            } else {
                // mHintIcon.setVisibility(View.GONE);
                mHintText.setVisibility(View.GONE);
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    /**
     * Local subclass for AutoCompleteTextView.
     * 
     * @hide
     */
    public static class SearchAutoComplete extends AuroraAutoCompleteTextView {

        private int mThreshold;
        private AuroraSearchView mSearchView;
        private OnKeyBackListener mKeyListener;

        public SearchAutoComplete(Context context) {
            super(context);
            mThreshold = getThreshold();
        }

        public SearchAutoComplete(Context context, AttributeSet attrs) {
            super(context, attrs);
            mThreshold = getThreshold();
        }

        public SearchAutoComplete(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            mThreshold = getThreshold();
        }

        void setSearchView(AuroraSearchView searchView) {
            // setPadding(20,0,0,0);
            mSearchView = searchView;
        }

        void setKeyBackListener(OnKeyBackListener l) {
            mKeyListener = l;
        }

        @Override
        public void setThreshold(int threshold) {
            super.setThreshold(threshold);
            mThreshold = threshold;
        }

        /**
         * Returns true if the text field is empty, or contains only whitespace.
         */
        private boolean isEmpty() {
            return TextUtils.getTrimmedLength(getText()) == 0;
        }

        /**
         * We override this method to avoid replacing the query box text when a
         * suggestion is clicked.
         */
        @Override
        protected void replaceText(CharSequence text) {
        }

        /**
         * We override this method to avoid an extra onItemClick being called on
         * the drop-down's OnItemClickListener by
         * {@link AutoCompleteTextView#onKeyUp(int, KeyEvent)} when an item is
         * clicked with the trackball.
         */
        @Override
        public void performCompletion() {
        }

        /**
         * We override this method to be sure and show the soft keyboard if
         * appropriate when the TextView has focus.
         */
        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);

            if (hasWindowFocus && mSearchView.hasFocus() && getVisibility() == VISIBLE) {
                InputMethodManager inputManager = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(this, 0);
                // If in landscape mode, then make sure that
                // the ime is in front of the dropdown.
                if (isLandscapeMode(getContext())) {
                    ensureImeVisible(true);
                }
            }
        }

        @Override
        protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            Log.e("fucos", "" + focused);
            mSearchView.onTextFocusChanged();
        }

        /**
         * We override this method so that we can allow a threshold of zero,
         * which ACTV does not.
         */
        @Override
        public boolean enoughToFilter() {
            return mThreshold <= 0 || super.enoughToFilter();
        }

        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // special case for the back key, we do not even try to send it
                // to the drop down list but instead, consume it immediately
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.startTracking(event, this);
                    }
                    return false;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.handleUpEvent(event);
                    }
                    if (event.isTracking() && !event.isCanceled()) {
                        mSearchView.clearFocus();
                        if (mKeyListener != null) {
                            Log.e("search", "mKeyListener");
                            mKeyListener.pressed();
                        }
                        mSearchView.setImeVisibility(false);
                        return true;
                    }
                }
            }
            return super.onKeyPreIme(keyCode, event);
        }

    }

    private OnKeyBackListener mKeyBackListener;

    public void setKeyBackListener(OnKeyBackListener l) {
        mKeyBackListener = l;
        mQueryTextView.setKeyBackListener(mKeyBackListener);
    }

    public interface OnKeyBackListener {
        public void pressed();
    }

    // Gionee <zhangxx><2013-05-09> add for CR00811583 begin
    private final int SCALE_ANIM_INDEX = 3;
    private final int SEARCHVIEW_ANIMTIME_LONG = 50;
    private final int SEARCHVIEW_ANIMTIME_SHORT = 10;
    private final int SEARCHVIEW_BACKGROUND_MINWIDTH = 100;
    private final double SEARCHVIEW_UNFOLD_INITIAL_SPEED = 20;
    private final int SEARCHVIEW_INVALIDATE_MESSAGE = 100;

    private boolean mIsGioneeViewStyle = false;
    private boolean mIsGioneeWidget3Support = false;
    private int mAnimIndex = 0;
    private boolean mIsShowScaleAnimation = false;
    private boolean mIsAnimationRunning = false;
    private boolean mIsShowPanelAnimation = false;
    private double mBackgroundWidth = 0;
    private int mBackgroundMaxWidth = getWidth();
    private ArrayList<Drawable> mSearchViewBackgrounds;
    private Animation mSearchViewAnim;

    private ImageView mGnSearchVoiceButton;
    private ImageView mGnSearchGoButton;

    private boolean mIsSearchVoiceMode = false;
    // Gionee <fenglp> <2013-08-08> add for CR00812456 begin
    private boolean mIsAnimationing = false;

    // Gionee <fenglp> <2013-08-08> add for CR00812456 end

    @Override
    public void draw(Canvas canvas) {
        if (mIsGioneeViewStyle && mIsGioneeWidget3Support) {
            Rect rect = new Rect();
            mSearchPlate.getGlobalVisibleRect(rect);
            int paddingLeft = rect.left - getLeft();
            int paddingTop = rect.top - getTop();
            mBackgroundMaxWidth = rect.right - rect.left;
            if (mIsAnimationRunning) {
                if (mIsShowScaleAnimation) {
                    Drawable icon = mSearchViewBackgrounds
                            .get(mAnimIndex < SCALE_ANIM_INDEX ? mAnimIndex : SCALE_ANIM_INDEX);
                    icon.setBounds(paddingLeft, (getHeight() - icon.getIntrinsicHeight()) / 2,
                            icon.getIntrinsicWidth() + paddingLeft,
                            (getHeight() + icon.getIntrinsicHeight()) / 2);
                    icon.draw(canvas);
                    return;
                }
                Drawable background = mSearchViewBackgrounds.get(0);
                background.setBounds(paddingLeft,
                        (getHeight() - background.getIntrinsicHeight()) / 2,
                        (int) mBackgroundWidth + paddingLeft,
                        (getHeight() + background.getIntrinsicHeight()) / 2);
                background.draw(canvas);
                return;
            }

            if (mIsShowPanelAnimation) {
                mIsShowPanelAnimation = false;
                mIsAnimationing = true;
                mSearchPlate.startAnimation(mSearchViewAnim);
            }
            // Gionee <fenglp> <2013-08-08> modify for CR00812456 begin
            if (mIsAnimationing) {
                Drawable background = mSearchViewBackgrounds.get(0);
                background.setBounds(paddingLeft,
                        (getHeight() - background.getIntrinsicHeight()) / 2,
                        (int) mBackgroundWidth + paddingLeft,
                        (getHeight() + background.getIntrinsicHeight()) / 2);
                background.draw(canvas);
            }
            // Gionee <fenglp> <2013-08-08> modify for CR00812456 end
        }

        super.draw(canvas);
    }

    private void initSearchView() {
        int count = 0;
        mSearchViewBackgrounds = new ArrayList<Drawable>();
        mSearchViewBackgrounds.add(count++,
                getResources().getDrawable(R.drawable.aurora_searchview_border));
        mSearchViewBackgrounds.add(count++,
                getResources().getDrawable(R.drawable.aurora_searchview_icon_1));
        mSearchViewBackgrounds.add(count++,
                getResources().getDrawable(R.drawable.aurora_searchview_icon_2));
        mSearchViewBackgrounds.add(count++,
                getResources().getDrawable(R.drawable.aurora_searchview_icon_3));

        mSearchViewAnim = AnimationUtils.loadAnimation(getContext(), R.anim.aurora_searchview_text);
        // Gionee <fenglp> <2013-08-08> add for CR00812456 begin
        mSearchViewAnim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                mIsAnimationing = false;
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        // Gionee <fenglp> <2013-08-08> add for CR00812456 end
        mGnSearchVoiceButton = (ImageView) findViewById(R.id.aurora_search_voice_btn);
        mGnSearchGoButton = (ImageView) findViewById(R.id.aurora_search_go_btn);
    }

    public void setSubmitSearchMode(boolean isSubmitSearchMode) {
        setSubmitSearchMode(isSubmitSearchMode, null);
    }

    public void setSubmitSearchMode(boolean isSubmitSearchMode,
            OnClickListener searchSubmitClickListener) {
        if (mGnSearchGoButton == null) {
            return;
        }

        mGnSearchGoButton.setVisibility(isSubmitSearchMode ? VISIBLE : GONE);
        if (isSubmitSearchMode) {
            mGnSearchGoButton.setOnClickListener(searchSubmitClickListener);
        }
    }

    public void setVoiceSearchMode(boolean isVoiceSearchMode,
            OnClickListener searchVoiceClickListener) {
        mIsSearchVoiceMode = isVoiceSearchMode;
        if (mGnSearchVoiceButton == null) {
            return;
        }

        mGnSearchVoiceButton.setVisibility(isVoiceSearchMode ? VISIBLE : GONE);
        if (mIsSearchVoiceMode) {
            mGnSearchVoiceButton.setOnClickListener(searchVoiceClickListener);
        }
    }

    private void updateSearchVoiceButton(boolean isShow) {
        if (mGnSearchVoiceButton == null) {
            return;
        }

        mGnSearchVoiceButton.setVisibility(isShow ? VISIBLE : GONE);
    }

    private void startBeginAnimation() {
        mAnimIndex = 0;
        mIsShowScaleAnimation = true;
        mIsAnimationRunning = true;
        mIsShowPanelAnimation = true;
        new Thread() {
            public void run() {
                while (true) {
                    mAnimIndex++;
                    if (mAnimIndex > SCALE_ANIM_INDEX) {
                        mIsShowScaleAnimation = false;
                        mBackgroundWidth = SEARCHVIEW_BACKGROUND_MINWIDTH
                                + getUnfoldWidth(mAnimIndex - SCALE_ANIM_INDEX);

                        if (mBackgroundWidth >= mBackgroundMaxWidth) {
                            mBackgroundWidth = mBackgroundMaxWidth;
                            mSearchViewInvalidateHandler
                                    .sendEmptyMessage(SEARCHVIEW_INVALIDATE_MESSAGE);
                            mIsAnimationRunning = false;
                            setImeVisibility(true);
                            return;
                        }
                    }

                    try {
                        if (mIsShowScaleAnimation) {
                            sleep(SEARCHVIEW_ANIMTIME_LONG);
                        } else {
                            sleep(SEARCHVIEW_ANIMTIME_SHORT);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mSearchViewInvalidateHandler.sendEmptyMessage(SEARCHVIEW_INVALIDATE_MESSAGE);
                }
            };
        }.start();
    }

    public void startEndAnimation() {

    }

    Handler mSearchViewInvalidateHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == SEARCHVIEW_INVALIDATE_MESSAGE) {
                invalidate();
            }
        };
    };

    private double getUnfoldWidth(int duration) {
        return SEARCHVIEW_UNFOLD_INITIAL_SPEED * duration;
    }

    private boolean isGioneeViewStyle() {
        /*
         * TypedValue outValue = new TypedValue(); boolean rtn =
         * getContext().getTheme
         * ().resolveAttribute(com.android.internal.R.attr.gioneeViewStyle,
         * outValue, true); return outValue.data != 0;
         */
        return false;
    }

    private boolean isGioneeWidget3Support() {
        return false;// /*SystemProperties.get("ro.gn.widget3.support").equals("yes");*/
    }

    // Gionee <zhangxx><2013-05-09> add for CR00811583 end

    /**
     * @author luofu This filter will constrain editText not to make the length
     *         of the text greater than the specified length.
     */
    public static class LengthFilter implements InputFilter {
        public LengthFilter(int max) {
            mMax = max;
        }

        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {
            int keep = mMax - (dest.length() - (dend - dstart));

            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null; // keep original
            } else {
                keep += start;
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    --keep;
                    if (keep == start) {
                        return "";
                    }
                }
                return source.subSequence(start, keep);
            }
        }

        private int mMax;
    }
    
    //aurora add for AutoCompleteTextView start
    public AuroraAutoCompleteTextView getQueryTextView( ) {
    	return mQueryTextView;
    }
}
    
    
