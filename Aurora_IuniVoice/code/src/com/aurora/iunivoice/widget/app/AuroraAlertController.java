/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.aurora.iunivoice.widget.app;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.utils.DensityUtil;
import com.aurora.iunivoice.widget.AuroraCheckBox;




public class AuroraAlertController {

    private final Context mContext;
    private final DialogInterface mDialogInterface;
    private final Window mWindow;
    
    private CharSequence mTitle;

    private CharSequence mMessage;

    private ListView mListView;
    
    private View mView;

    private int mViewSpacingLeft;
    
    private int mViewSpacingTop;
    
    private int mViewSpacingRight;
    
    private int mViewSpacingBottom;
    
    private boolean mViewSpacingSpecified = false;
    
    private Button mButtonPositive;

    private CharSequence mButtonPositiveText;

    private Message mButtonPositiveMessage;

    private Button mButtonNegative;

    private CharSequence mButtonNegativeText;

    private Message mButtonNegativeMessage;

    private Button mButtonNeutral;

    private CharSequence mButtonNeutralText;

    private Message mButtonNeutralMessage;

    private ScrollView mScrollView;
    
    private int mIconId = -1;
    
    private Drawable mIcon;
    
    private ImageView mIconView;
    
    private AuroraDialogTitle mTitleView;

    private TextView mMessageView;

    private View mCustomTitleView;
    
    private boolean mForceInverseBackground;
    
    private ListAdapter mAdapter;
    
    private int mCheckedItem = -1;

    private int mAlertDialogLayout;
    private int mListLayout;
    private int mMultiChoiceItemLayout;
    private int mSingleChoiceItemLayout;
    private int mListItemLayout;

    private int mListMaxHeight = 313;

    private Handler mHandler;
    
    private View titleDivider;
    
    private boolean mTitleDividerVisible = false;
    
    // Gionee zhangxx 2012-11-01 add for CR00715173 begin
    private boolean mHasCancelIconButton = true;
    private ImageButton mButtonCancel = null;
    // Gionee zhangxx 2012-11-01 add for CR00715173 end
    
    // Gionee <zhangxx><2013-05-15> add for CR00811583 begin
    // gionee widget 3.0 support
    private static boolean mIsGnWidget3Style = false;
    private int mButtonPositiveStyle;
    private int mButtonNeutralStyle;
    // Gionee <zhangxx><2013-05-15> add for CR00811583 end
    //aurora 
    
    private int mButtonCount = 3;
    public int theme_style_id=R.style.AuroraAlertDialogTheme;
    
    
    int mDialogTextColor;
    //aurora
    View.OnClickListener mButtonHandler = new View.OnClickListener() {
        public void onClick(View v) {
            Message m = null;
            if (v == mButtonPositive && mButtonPositiveMessage != null) {
                m = Message.obtain(mButtonPositiveMessage);
            } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
                m = Message.obtain(mButtonNegativeMessage);
            } else if (v == mButtonNeutral && mButtonNeutralMessage != null) {
                m = Message.obtain(mButtonNeutralMessage);
            }
            if (m != null) {
                m.sendToTarget();
            }
            Log.v("AlertController", "onClick");
            // Post a message so we dismiss after the above handlers are executed
            mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialogInterface)
                    .sendToTarget();
        }
    };


    private static final class ButtonHandler extends Handler {
        // Button clicks have Message.what as the BUTTON{1,2,3} constant
        private static final int MSG_DISMISS_DIALOG = 1;
        
        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<DialogInterface>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;
                    
                case MSG_DISMISS_DIALOG:
                	Log.v("AlertController", "MSG_DISMISS_DIALOG");
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }

    private static boolean shouldCenterSingleButton(Context context) {
        //TypedValue outValue = new TypedValue();
        //context.getTheme().resolveAttribute(com.android.R.attr.alertDialogCenterButtons,
                //outValue, true);
        return true;//outValue.data != 0;// modify
    }

    public AuroraAlertController(Context context, DialogInterface di, Window window) {
        mContext = context;
        mDialogInterface = di;
        mWindow = window;
        mHandler = new ButtonHandler(di);

        TypedArray a = context.obtainStyledAttributes(null,
                R.styleable.AuroraAlertDialog,
                0, theme_style_id);

        mDialogTextColor = context.getResources().getColor(R.color.aurora_dialog_content_text_color);
        
        mAlertDialogLayout = a.getResourceId(R.styleable.AuroraAlertDialog_auroralayout,
                R.layout.aurora_alert_dialog);
        mListLayout = a.getResourceId(
                R.styleable.AuroraAlertDialog_auroralistLayout,
                R.layout.aurora_select_dialog);
        mMultiChoiceItemLayout = a.getResourceId(
                R.styleable.AuroraAlertDialog_auroramultiChoiceItemLayout,
                R.layout.aurora_select_dialog_multichoice);
        mSingleChoiceItemLayout = a.getResourceId(
                R.styleable.AuroraAlertDialog_aurorasingleChoiceItemLayout,
                R.layout.aurora_select_dialog_singlechoice);
        mListItemLayout = a.getResourceId(
                R.styleable.AuroraAlertDialog_auroralistItemLayout,
                R.layout.aurora_select_dialog_item);

        a.recycle();
 //aurora       
    }
    
    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        
        ViewGroup vg = (ViewGroup)v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }
        
        return false;
    }
    public void installContent() {
        /* We use a custom title so never request a window title */
        //mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        
        if (mView == null || !canTextInput(mView)) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
            		WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
      
        mWindow.setContentView(mAlertDialogLayout);
        setupView();
    }
    
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    public void setTitleDividerVisible(boolean visible){
        mTitleDividerVisible = visible;
    }
    
    /**
     * @see AuroraAlertDialog.Builder#setCustomTitle(View)
     */
    public void setCustomTitle(View customTitleView) {
        mCustomTitleView = customTitleView;
    }
    
    public void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }

    /**
     * Set the view to display in the dialog.
     */
    public void setView(View view) {
        mView = view;
        mViewSpacingSpecified = false;
    }
    
    /**
     * Set the view to display in the dialog along with the spacing around that view
     */
    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight,
            int viewSpacingBottom) {
        mView = view;
        mViewSpacingSpecified = true;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
    }

    /**
     * Sets a click listener or a message to be sent when the button is clicked.
     * You only need to pass one of {@code listener} or {@code msg}.
     * 
     * @param whichButton Which button, can be one of
     *            {@link DialogInterface#BUTTON_POSITIVE},
     *            {@link DialogInterface#BUTTON_NEGATIVE}, or
     *            {@link DialogInterface#BUTTON_NEUTRAL}
     * @param text The text to display in positive button.
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     * @param msg The {@link Message} to be sent when clicked.
     */
    public void setButton(int whichButton, CharSequence text,
            DialogInterface.OnClickListener listener, Message msg) {

        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }
        
        switch (whichButton) {

            case DialogInterface.BUTTON_POSITIVE:
                mButtonPositiveText = text;
                mButtonPositiveMessage = msg;
                break;
                
            case DialogInterface.BUTTON_NEGATIVE:
                mButtonNegativeText = text;
                mButtonNegativeMessage = msg;
                break;
                
            case DialogInterface.BUTTON_NEUTRAL:
                mButtonNeutralText = text;
                mButtonNeutralMessage = msg;
                break;
                
            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }

    /**
     * Set resId to 0 if you don't want an icon.
     * @param resId the resourceId of the drawable to use as the icon or 0
     * if you don't want an icon.
     */
    public void setIcon(int resId) {
        mIconId = resId;
        if (mIconView != null) {
            if (resId > 0) {
                mIconView.setImageResource(mIconId);
            } else if (resId == 0) {
                mIconView.setVisibility(View.GONE);
            }
        }
    }
    
    public void setIcon(Drawable icon) {
        mIcon = icon;
        if ((mIconView != null) && (mIcon != null)) {
            mIconView.setImageDrawable(icon);
        }
    }

    /**
     * @param attrId the attributeId of the theme-specific drawable
     * to resolve the resourceId for.
     *
     * @return resId the resourceId of the theme-specific drawable
     */
    public int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        mForceInverseBackground = forceInverseBackground;
    }
    
    public ListView getListView() {
        return mListView;
    }
    
    
    
    public Button getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonPositive;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonNegative;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonNeutral;
            default:
                return null;
        }
    }
    
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }
    
    private void setupView() {
        LinearLayout contentPanel = (LinearLayout) mWindow.findViewById(R.id.aurora_contentPanel);
        setupContent(contentPanel);
       
        boolean hasButtons = setupButtons();
        
        LinearLayout topPanel = (LinearLayout) mWindow.findViewById(R.id.aurora_topPanel);
        TypedArray a = mContext.obtainStyledAttributes(
                null, R.styleable.AuroraAlertDialog, 0, theme_style_id);
       
        boolean hasTitle = setupTitle(topPanel);
        View buttonPanel = mWindow.findViewById(R.id.aurora_buttonPanel);
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
            
        }
   
        FrameLayout customPanel = null;
        if (mView != null) {
            customPanel = (FrameLayout) mWindow.findViewById(R.id.aurora_customPanel);
            FrameLayout custom = (FrameLayout) mWindow.findViewById(R.id.aurora_custom);
            int adapterCount = 0;
            int height = MATCH_PARENT;
//            if(mView instanceof ListView){
//                ListAdapter adapter = ((ListView) mView).getAdapter();
//                if(adapter != null){
//                    adapterCount = adapter.getCount();
//                    if(adapterCount >4){
//                        height = DensityUtil.dip2px(mContext, 313);
//                    }
//                }
//                custom.setPadding(0, custom.getPaddingTop(), 0, custom.getBottom());
//            }
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                parent.removeView(mView);
            }
            custom.addView(mView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            if (mViewSpacingSpecified) {
                custom.setPadding(mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                        mViewSpacingBottom);
            }
            if (mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0;
            }
        } else {
            mWindow.findViewById(R.id.aurora_customPanel).setVisibility(View.GONE);
        }
        
        /* Only display the divider if we have a title and a 
         * custom view or a message.
         */
        if (hasTitle) {
            View divider = null;
            if (mMessage != null || mView != null || mListView != null) {
                divider = mWindow.findViewById(R.id.aurora_titleDivider);
            } else {
                divider = mWindow.findViewById(R.id.aurora_titleDividerTop); 
            }

           
            if (divider != null) {
                if(mTitleDividerVisible){
                    divider.setVisibility(View.VISIBLE);
                    topPanel.setPadding(topPanel.getPaddingLeft(), 
                            DensityUtil.dip2px(mContext, 15.0f),
                            topPanel.getPaddingRight(), 
                            topPanel.getPaddingBottom());
                }else{
                    divider.setVisibility(View.GONE);
                }
            }
        }
        
       
        // Aurora <Luofu> <2013-9-27> modify for AlertDialog begin
        
        if(mView != null){
            if(mView instanceof ListView){
                FrameLayout custom = (FrameLayout) mWindow.findViewById(R.id.aurora_custom);
                custom.setPadding(0, customPanel.getPaddingTop(), 0, customPanel.getPaddingRight());
            }
        }
     // Aurora <Luofu> <2013-9-27> modify for AlertDialog end
        setBackground(topPanel, contentPanel, customPanel, hasButtons, a, hasTitle, buttonPanel);
        a.recycle();
    }

    private boolean setupTitle(LinearLayout topPanel) {
        boolean hasTitle = true;
        
        if (mCustomTitleView != null) {
            // Add the custom title view directly to the topPanel layout
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ViewGroup parent = (ViewGroup) mCustomTitleView.getParent();
            if (parent != null) {
                parent.removeView(mCustomTitleView);
            }
            topPanel.addView(mCustomTitleView, 0, lp);
            
            // Hide the title template
            View titleTemplate = mWindow.findViewById(R.id.aurora_title_template);
            titleTemplate.setVisibility(View.GONE);
        } else {
        	
        	if(TextUtils.isEmpty(mTitle)){
        		mTitle = mContext.getResources().getString(R.string.aurora_dialog_default_title);
        	}
            final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
            mIconView = (ImageView) mWindow.findViewById(android.R.id.icon);
            mTitleView = (AuroraDialogTitle) mWindow.findViewById(R.id.aurora_alertTitle);
            mTitleView.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            mTitleView.getPaint().setStrokeWidth(0.6f);
            if (hasTextTitle) {
                /* Display the title if a title is supplied, else hide it */
                
                mTitleView.setText(mTitle);
                
                /* Do this last so that if the user has supplied any
                 * icons we use them instead of the default ones. If the
                 * user has specified 0 then make it disappear.
                 */
                if (mIconId > 0) {
                    mIconView.setImageResource(mIconId);
                } else if (mIcon != null) {
                    mIconView.setImageDrawable(mIcon);
                } else if (mIconId == 0) {
                    
                    /* Apply the padding from the icon to ensure the
                     * title is aligned correctly.
                     */
                    mTitleView.setPadding(mIconView.getPaddingLeft(),
                            mIconView.getPaddingTop(),
                            mIconView.getPaddingRight(),
                            mIconView.getPaddingBottom());
                    mIconView.setVisibility(View.GONE);
                }
            } else {
                
                // Hide the title template
                View titleTemplate = mWindow.findViewById(R.id.aurora_title_template);
                //titleTemplate.setVisibility(View.GONE);
                mIconView.setVisibility(View.GONE);
               // topPanel.setVisibility(View.GONE);
                hasTitle = false;
            }
        }
        return hasTitle;
    }

    private void setupContent(LinearLayout contentPanel) {
        mScrollView = (ScrollView) mWindow.findViewById(R.id.aurora_scrollView);
        mScrollView.setFocusable(false);
        
        // Special case for users that only want to display a String
        mMessageView = (TextView) mWindow.findViewById(android.R.id.message);
        if (mMessageView == null) {
            return;
        }
        
        if (mMessage != null) {
            mMessageView.setText(mMessage);
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);
            
            if (mListView != null) {
                contentPanel.removeView(mWindow.findViewById(R.id.aurora_scrollView));
                contentPanel.addView(mListView,
                        new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
                contentPanel.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1.0f));
            } else {
                contentPanel.setVisibility(View.GONE);
            }
        }
    }

    private boolean setupButtons() {
        int BIT_BUTTON_POSITIVE = 1;
        int BIT_BUTTON_NEGATIVE = 2;
        int BIT_BUTTON_NEUTRAL = 4;
        int whichButtons = 0;
        mButtonPositive = (Button) mWindow.findViewById(android.R.id.button2);
        mButtonPositive.setOnClickListener(mButtonHandler);

        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive.setVisibility(View.GONE);
            mButtonCount--;
        } else {
            mButtonPositive.setText(mButtonPositiveText);
            mButtonPositive.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }

        mButtonNegative = (Button) mWindow.findViewById(android.R.id.button1);
        mButtonNegative.setOnClickListener(mButtonHandler);

        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative.setVisibility(View.GONE);
            mButtonCount--;
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            mButtonNegative.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }

        mButtonNeutral = (Button) mWindow.findViewById(android.R.id.button3);
        mButtonNeutral.setOnClickListener(mButtonHandler);

        if (TextUtils.isEmpty(mButtonNeutralText)) {
            mButtonNeutral.setVisibility(View.GONE);
            mButtonCount--;
        } else {
            mButtonNeutral.setText(mButtonNeutralText);
            mButtonNeutral.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEUTRAL;
        }

        if (shouldCenterSingleButton(mContext)) {
            /*
             * If we only have 1 button it should be centered on the layout and
             * expand to fill 50% of the available space.
             */
            if (whichButtons == BIT_BUTTON_POSITIVE) {
                centerButton(mButtonPositive);
            } else if (whichButtons == BIT_BUTTON_NEGATIVE) {
                centerButton(mButtonNegative);
            } else if (whichButtons == BIT_BUTTON_NEUTRAL) {
                centerButton(mButtonNeutral);
            }
        }
        
        return whichButtons != 0;
    }

    private void centerButton(Button button) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.weight = 0.5f;
        button.setLayoutParams(params);
//        View leftSpacer = mWindow.findViewById(R.id.aurora_leftSpacer);
//        if (leftSpacer != null) {
//            leftSpacer.setVisibility(View.VISIBLE);
//        }
//        View rightSpacer = mWindow.findViewById(R.id.aurora_rightSpacer);
//        if (rightSpacer != null) {
//            rightSpacer.setVisibility(View.VISIBLE);
//        }
    }

    private void setBackground(LinearLayout topPanel, LinearLayout contentPanel,
            View customPanel, boolean hasButtons, TypedArray a, boolean hasTitle, 
            View buttonPanel) {
        
        /* Get all the different background required */
    	 int fullBright = R.drawable.shape;
         int topBright = R.drawable.aurora_dialog_bg_top;
         int centerBright = R.drawable.aurora_dialog_white_bg;
         int bottomBright = R.drawable.aurora_dialog_bg_bottom;
         int bottomMedium = R.drawable.aurora_dialog_bg_bottom;
        View[] views = new View[4];
        boolean[] light = new boolean[4];
        View lastView = null;
        boolean lastLight = false;
      
        int pos = 0;
        if (hasTitle) {
            views[pos] = topPanel;
            light[pos] = false;
            pos++;
        }
        
        /* The contentPanel displays either a custom text message or
         * a ListView. If it's text we should use the dark background
         * for ListView we should use the light background. If neither
         * are there the contentPanel will be hidden so set it as null.
         */
        views[pos] = (contentPanel.getVisibility() == View.GONE) 
                ? null : contentPanel;
        light[pos] = mListView != null;
        pos++;
        if (customPanel != null) {
            views[pos] = customPanel;
            light[pos] = mForceInverseBackground;
            pos++;
        }
        if (hasButtons) {
            views[pos] = buttonPanel;
            light[pos] = true;
        }
        
        boolean setView = false;
        for (pos=0; pos<views.length; pos++) {
            View v = views[pos];
            if (v == null) {
                continue;
            }
            if (lastView != null) {
                if (!setView) {
                    lastView.setBackgroundResource( topBright );
                    lastView.setPadding(lastView.getPaddingLeft(), DensityUtil.dip2px(mContext, 24f), 
                    		lastView.getPaddingRight(), lastView.getPaddingBottom());
                } else {
                    lastView.setBackgroundResource( centerBright );
                }
                setView = true;
            }
            lastView = v;
            lastLight = light[pos];
        }
        
        if (lastView != null) {
            if (setView) {
                
                /* ListViews will use the Bright background but buttons use
                 * the Medium background.
                 */ 
                lastView.setBackgroundResource(
                        lastLight ? (hasButtons ? bottomMedium : bottomBright) : bottomBright);
            } else {
                lastView.setBackgroundResource(lastLight ? fullBright : fullBright);
            }
        }
      
      

      setButtonBackground((LinearLayout)buttonPanel);
      if ((mListView != null) && (mAdapter != null)) {
          mListView.setAdapter(mAdapter);
          if (mCheckedItem > -1) {
              mListView.setItemChecked(mCheckedItem, true);
              mListView.setSelection(mCheckedItem);
          }
      }
      

        
        
    }
    
   
    private void setButtonBackground(LinearLayout buttonPanel){
        View verticalDivider1 = mWindow.findViewById(R.id.aurora_dialog_button_divider1);
        View verticalDivider2 = mWindow.findViewById(R.id.aurora_dialog_button_divider2);
        if(mButtonCount == 1){
            LinearLayout layout = (LinearLayout) buttonPanel.getChildAt(1);
            for(int i = 0;i<layout.getChildCount();i++){
                View child = layout.getChildAt(i);
                if(child instanceof Button){
                    child.setBackgroundResource(R.drawable.aurora_alert_dialog_btn_selector);
                }
            }
        }
        
        if(mButtonCount == 2){
            boolean pos = (mButtonPositive.getVisibility() == View.VISIBLE);
            boolean neg = (mButtonNegative.getVisibility() == View.VISIBLE);
            boolean neu = (mButtonNeutral.getVisibility() == View.VISIBLE);
            
            if(pos && neu){
                mButtonNeutral.setBackgroundResource(R.drawable.aurora_alert_dialog_btn_selector_left);
//                verticalDivider1.setVisibility(View.GONE);
            }
            if(neg && neu){
                mButtonNeutral.setBackgroundResource(R.drawable.aurora_alert_dialog_btn_selector_right);
//                verticalDivider2.setVisibility(View.GONE);
            }
        }
    }

    public static class RecycleListView extends ListView {
        boolean mRecycleOnMeasure = true;
        int mMaxHeight = 313;
        public RecycleListView(Context context) {
            super(context);
            setDividerHeight(1);
            init();
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setDividerHeight(1);
            init();
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            setDividerHeight(1);
            init();
        }

        private void init(){
            mMaxHeight = DensityUtil.dip2px(getContext(), mMaxHeight);
        }
        
        protected boolean recycleOnMeasure() {
            return mRecycleOnMeasure;
        }
        
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // TODO Auto-generated method stub
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int height = getMeasuredHeight();
            //AuroraLog.e("aurora_dialog_list", "height:"+height);
            //AuroraLog.e("aurora_dialog_list", "mMaxHeight:"+mMaxHeight);
            int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
            if (height > mMaxHeight) {
                int specHeightSize = MeasureSpec.getSize(mMaxHeight);
                setMeasuredDimension(specWidthSize, specHeightSize);
            } else
            {
                setMeasuredDimension(specWidthSize, height);

            }
        }
        
    }

    public static class AlertParams {
        public final Context mContext;
        public final LayoutInflater mInflater;
        
        public int mIconId = 0;
        public Drawable mIcon;
        public int mIconAttrId = 0;
        public CharSequence mTitle;
        public View mCustomTitleView;
        public CharSequence mMessage;
        public CharSequence mPositiveButtonText;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public CharSequence mNegativeButtonText;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNeutralButtonText;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public boolean mCancelable;
        public DialogInterface.OnCancelListener mOnCancelListener;
        public DialogInterface.OnDismissListener mOnDismissListener;
        public DialogInterface.OnKeyListener mOnKeyListener;
        public CharSequence[] mItems;
        public ListAdapter mAdapter;
        public DialogInterface.OnClickListener mOnClickListener;
        public boolean mIsMultiChoiceFromAdapter = false;
        public View mView;
        public int mViewSpacingLeft;
        public int mViewSpacingTop;
        public int mViewSpacingRight;
        public int mViewSpacingBottom;
        public boolean mViewSpacingSpecified = false;
        public boolean[] mCheckedItems;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public int mCheckedItem = -1;
        public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
        public Cursor mCursor;
        public String mLabelColumn;
        public String mIsCheckedColumn;
        public boolean mForceInverseBackground;
        public AdapterView.OnItemSelectedListener mOnItemSelectedListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public boolean mRecycleOnMeasure = true;
        
        // Gionee zhangxx 2012-11-01 add for CR00715173 begin 
        public boolean mHasCancelIcon = true;
        public Drawable mCancelIcon = null;
        // Gionee zhangxx 2012-11-01 add for CR00715173 end
        // Gionee <zhangxx> <2013-05-31> add for CR00811583 begin
        public int mPositiveButtonStyle;
        public int mNeutralButtonStyle;
        // Gionee <zhangxx> <2013-05-31> add for CR00811583 end
        public boolean mAuroraStyle;
        
        public boolean mTitileDividerVisible = false;
        
        public boolean mShowTitleDivider = false;
        
        public boolean mShowAddItemView = false;
        
        public int mMaxItemLength;
        
        private int mDialogTextColor;
        
        private boolean mEditMode = false;
        
        
       private ArrayList<CharSequence> mItemArray = new ArrayList<CharSequence>();
       
       private ArrayList<Boolean> mItemCheckedArray = new ArrayList<Boolean>();
        /**
         * Interface definition for a callback to be invoked before the ListView
         * will be bound to an adapter.
         */
        public interface OnPrepareListViewListener {
            
            /**
             * Called before the ListView is bound to an adapter.
             * @param listView The ListView that will be shown in the dialog.
             */
            void onPrepareListView(ListView listView);
        }
        
        public AlertParams(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    
        private final void auroraChangeListTextColor(View view){
            TextView text = (TextView) view.findViewById(android.R.id.text1);
            if(text != null){
                text.setTextColor(Color.RED);
            }
        }
        
        public void setShowTitleDivider(boolean show){
            mShowTitleDivider = show;
        }
        public void apply(AuroraAlertController dialog) {
            mDialogTextColor = dialog.mDialogTextColor;
            if (mCustomTitleView != null) {
                dialog.setCustomTitle(mCustomTitleView);
            } else {
                if (mTitle != null) {
                    dialog.setTitle(mTitle);
                }
                if (mIcon != null) {
                    dialog.setIcon(mIcon);
                }
                if (mIconId >= 0) {
                    dialog.setIcon(mIconId);
                }
                if (mIconAttrId > 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(mIconAttrId));
                }
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            }
            if (mPositiveButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
                        mPositiveButtonListener, null);
            }
            if (mNegativeButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
                        mNegativeButtonListener, null);
            }
            if (mNeutralButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
                        mNeutralButtonListener, null);
            }
            if (mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            // For a list, the client can either supply an array of items or an
            // adapter or a cursor
            if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(dialog);
            }
            if (mView != null) {
                if (mViewSpacingSpecified) {
                    dialog.setView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                            mViewSpacingBottom);
                } else {
                    dialog.setView(mView);
                }
            }
            
            /*
            dialog.setCancelable(mCancelable);
            dialog.setOnCancelListener(mOnCancelListener);
            if (mOnKeyListener != null) {
                dialog.setOnKeyListener(mOnKeyListener);
            }
            */
            dialog.setTitleDividerVisible(mTitileDividerVisible);
            
           
        }
        
        private void createListView(final AuroraAlertController dialog) {
			
			if(mItems != null)
			{
				for(CharSequence item:mItems){
					mItemArray.add(item);
				}
			}
			
			if(mCheckedItems != null){
				for(Boolean checked:mCheckedItems){
					mItemCheckedArray.add(checked);
				}
			}else{
				if(mItems != null){
					for(int i = 0;i<mItems.length;i++){
						mItemCheckedArray.add(false);
					}
				}
			}
			
            final RecycleListView listView = (RecycleListView)
                    mInflater.inflate(dialog.mListLayout, null);
            
            ListAdapter adapter = null;
            if(mIsMultiChoice || mIsSingleChoice){
                listView.setSelector(android.R.color.transparent);
            }
            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = getMultipleChoiceAdapter(listView,dialog);
                } else {
                    adapter = getMultipleChoiceCursorAdapter(listView,dialog);
                }
            } else {
                int layout = mIsSingleChoice 
                        ? dialog.mSingleChoiceItemLayout : dialog.mListItemLayout;
                if (mCursor == null) {
                    if(mIsSingleChoice){
                        adapter = (mAdapter != null) ? mAdapter
                                : new ArrayAdapter<CharSequence>(mContext, layout, android.R.id.text1, mItems);
                    }else{
                        adapter = (mAdapter != null) ? mAdapter
                                :new ItemAdapter(mContext,layout, mItems);
                        listView.setSelector(android.R.color.transparent);
                    }
                        
                } else {
                        adapter = new SimpleCursorAdapter(mContext, layout, 
                                mCursor, new String[]{mLabelColumn}, new int[]{android.R.id.text1});
                }
            }
            
            if (mOnPrepareListViewListener != null) {
                mOnPrepareListViewListener.onPrepareListView(listView);
            }
            
            /* Don't directly set the adapter on the ListView as we might
             * want to add a footer to the ListView later.
             */
            if(adapter != null){
                dialog.mAdapter = adapter;
            }
            
            dialog.mCheckedItem = mCheckedItem;
            
            if (mOnClickListener != null) {
            	setupClickListener(listView,dialog);
            } else if (mOnCheckboxClickListener != null) {
            	setupMultipleChoiceListener(listView,dialog);
            }
            
            // Attach a given OnItemSelectedListener to the ListView
            if (mOnItemSelectedListener != null) {
            	setupSingleChoiceListener(listView,dialog);
            }
            
            if (mIsSingleChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } 
//            else if (mIsMultiChoice) {
//                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//            }
            listView.mRecycleOnMeasure = mRecycleOnMeasure;
            dialog.mListView = listView;
        }
        
        /**
         * create multiple choice adapter with cursor
         * @param listView
         * @param dialog
         * @return
         */
        private ListAdapter getMultipleChoiceCursorAdapter(final ListView listView,final AuroraAlertController dialog){
        	ListAdapter adapter = new CursorAdapter(mContext, mCursor, false) {
                private final int mLabelIndex;
                private final int mIsCheckedIndex;

                {
                    final Cursor cursor = getCursor();
                    mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                    mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                        CheckedTextView text = (CheckedTextView) view.findViewById(android.R.id.text1);
                        text.setText(cursor.getString(mLabelIndex));
                        //AuroraCheckedTextView.setTextColor(mDialogTextColor);
                        listView.setItemChecked(cursor.getPosition(),
                        cursor.getInt(mIsCheckedIndex) == 1);
                }

                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return mInflater.inflate(dialog.mMultiChoiceItemLayout,
                            parent, false);
                }
                
            };
            return adapter;
        }
        
        /**
         * create multiple choice adapter with baseadapter
         * @param listView
         * @return
         */
        private ListAdapter getMultipleChoiceAdapter(final ListView listView,final AuroraAlertController dialog){
        	ListAdapter adapter = new BaseAdapter() {
                
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.aurora_select_dialog_muil,
                          null);
                    boolean isItemChecked =false;
                  if (mItemCheckedArray != null && mItemCheckedArray.size()>0) {
                	  if(!mShowAddItemView){
	                      isItemChecked = mItemCheckedArray.get(position);
	                      if (isItemChecked) {
	                          listView.setItemChecked(position, true);
	                      }
                	  }else{
                		  isItemChecked = mItemCheckedArray.get(position);
                	  }
                  }
                  if(convertView != null){
                   final   TextView item = (TextView) convertView.findViewById(android.R.id.text1);
                      item.setText(mItemArray.get(position));
                      item.setTextColor(mDialogTextColor);
                   final   CheckBox box = (CheckBox)convertView.findViewById(R.id.aurora_select_dialog_checkbox);
                      if(box != null){
                    	  if(!mShowAddItemView){
                    		  box.setChecked(isItemChecked);
                    	  }else{
                    		  if(position < mItemArray.size()-1){
                    			  box.setChecked(isItemChecked);
                    		  }else{
                    			  box.setChecked(mEditMode);
                    		  }
                    	  }
                      }
                     final int pos = position;
                    final  EditText edit = (EditText)convertView.findViewById(android.R.id.text2);
                    final  Button saveBtn = (Button) convertView.findViewById(android.R.id.button1);
                
                      if(edit != null && saveBtn != null && position == mItemArray.size()-1){
                    	    if( mOnCheckboxClickListener != null){
                            	if(mOnCheckboxClickListener instanceof AuroraMultipleChoiceListener){
                            		((AuroraMultipleChoiceListener)mOnCheckboxClickListener).onInput(edit,saveBtn);
                            	}
                    	  saveBtn.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									   item.setVisibility(View.VISIBLE);
			                        	edit.setVisibility(View.GONE);
			                        	saveBtn.setVisibility(View.GONE);
			                        	mEditMode = false;
									 ((InputMethodManager) mContext.getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 
											 InputMethodManager.HIDE_NOT_ALWAYS);
									// TODO Auto-generated method stub
									
		                        	final CharSequence newItemText = edit.getText();
		                        	boolean isEqual = false;//mItemArray.contains(newItemText);
		                        	if(mItemArray.size() > 1){
			                        	for(int i=0;i<mItemArray.size()-1;i++){
			                        		CharSequence text = mItemArray.get(i);
			                        		if(newItemText.toString().equals(text.toString())){
			                        			isEqual = true;
			                        			break;
			                        		}
			                        	}
		                        	}
		                         	/*
		                        	 * callback for button click event
		                        	 */
		                        	if(mOnCheckboxClickListener instanceof AuroraMultipleChoiceListener){
		                        		((AuroraMultipleChoiceListener)mOnCheckboxClickListener).onClick(
		                        				dialog.mDialogInterface, 
		                        				pos,
		                        				isEqual,
		                        				newItemText);//new text for added item,it must give back to caller
		                        	}
		                        	
		                        	if(pos >=0 && !TextUtils.isEmpty(newItemText)){
		                        		if(!isEqual){
				                        		mItemArray.add(pos, newItemText);
				                        		mItemCheckedArray.add(pos, true);
			                        		((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
			                        		
		                        		}
		                        	}
		                       
								}
							});
                    	  if(mEditMode){
                    		  item.setVisibility(View.GONE);
                    		  edit.setVisibility(View.VISIBLE);
                    		  saveBtn.setVisibility(View.VISIBLE);
                    	  }else{
                    		  item.setVisibility(View.VISIBLE);
                    		  edit.setVisibility(View.GONE);
                    		  saveBtn.setVisibility(View.GONE);
                    	  }
                      }
                  }
                  }
               
                  return convertView;
                }
                
                @Override
                public long getItemId(int position) {
                    // TODO Auto-generated method stub
                    return position;
                }
                
                @Override
                public Object getItem(int position) {
                    // TODO Auto-generated method stub
                    return mItemArray.get(position);
                }
                
                @Override
                public int getCount() {
                    // TODO Auto-generated method stub
                    return mItemArray.size();
                }
            };
            return adapter;
        }
        
        /**
         * setup listview onitemclicklistener
         * @param listView
         * @param dialog
         */
        private void setupClickListener(final ListView listView,final AuroraAlertController dialog){
            listView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    mOnClickListener.onClick(dialog.mDialogInterface, position);
                    if(!mIsMultiChoiceFromAdapter){
                        if (!mIsSingleChoice) {
                            dialog.mDialogInterface.dismiss();
                        }
                    }
                    
                }
            });
        }
        
        /**
         * setup listview singlechoice item listener
         * @param listView
         * @param dialog
         */
        private void setupSingleChoiceListener(final ListView listView,final AuroraAlertController dialog){
        	   listView.setOnItemSelectedListener(new OnItemSelectedListener() {

                   @Override
                   public void onItemSelected(AdapterView<?> parent, View view, int position,
                           long id) {
                       // TODO Auto-generated method stub
                       View child = view.findViewById(R.id.aurora_select_dialog_checkbox);
                       if(child != null){
                           if(child instanceof AuroraCheckBox){
                               ((AuroraCheckBox)child).auroraSetChecked(!((AuroraCheckBox)child).isChecked(),true);
                           }
                       }
                       mOnItemSelectedListener.onItemSelected(parent, listView, position, id);
                   }

                   @Override
                   public void onNothingSelected(AdapterView<?> parent) {
                       // TODO Auto-generated method stub
                       mOnItemSelectedListener.onNothingSelected(parent);
                   }
               });
        }
        
        /**
         * deal with multiple choice event
         * @param listView
         * @param dialog
         */
        private void setupMultipleChoiceListener(final ListView listView,final AuroraAlertController dialog){
        	  listView.setOnItemClickListener(new OnItemClickListener() {
                  public void onItemClick(AdapterView parent, View v, int position, long id) {
                	  View child = v.findViewById(R.id.aurora_select_dialog_checkbox);
                    
                      
                     boolean checked = mItemCheckedArray.get(position);
                      if(child != null){
                          if(child instanceof AuroraCheckBox){
                              ((AuroraCheckBox)child).auroraSetChecked(!((AuroraCheckBox)child).isChecked(),true);
                              checked = ((AuroraCheckBox)child).isChecked();
                          }
                      }
                      if ( mItemCheckedArray != null && mItemCheckedArray.size() > 0) {
                    	  mItemCheckedArray.set(position, checked);
                      }
                      final int pos = position;
                      final AdapterView list = parent;
                      /*
                       * 处理多选Dialog最后一个item是否有自动添加数据的功能
                       */
                      if(mShowAddItemView){
	                      if(position == mItemArray.size()-1){
	                      	final EditText edit = (EditText)v.findViewById(android.R.id.text2);
	                      	final Button addBtn = (Button)v.findViewById(android.R.id.button1);
	                      	final TextView itemText = (TextView)v.findViewById(android.R.id.text1);
	                      	final CheckBox box = (CheckBox)v.findViewById(R.id.aurora_select_dialog_checkbox);
	                      	itemText.setVisibility(View.GONE);
	                      	edit.setVisibility(View.VISIBLE);
	                      	edit.requestFocus();
	                      	edit.setText("");
	                      	mEditMode = true;
	                      	if(box != null){
	                      		box.setChecked(true);
	                      	}
//	                      	InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//	  						imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED);
	                        Timer timer = new Timer();
	 	                   timer.schedule(new TimerTask() {

	 	                    @Override
	 	                    public void run() {
	 	                     ((InputMethodManager) mContext.getSystemService("input_method"))
	 	                       .toggleSoftInput(0,
	 	                         InputMethodManager.HIDE_NOT_ALWAYS);
	 	                    }
	 	                   }, 100);
	                      	addBtn.setVisibility(View.VISIBLE);
	                      	dialog.mWindow.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
	                      	
	                      }
	                      if(pos < mItemArray.size() -1){
	                    	  mOnCheckboxClickListener.onClick(
		                              dialog.mDialogInterface, position, mItemCheckedArray.get(pos));
	                      }
                      }else{
                    	   mOnCheckboxClickListener.onClick(
                                   dialog.mDialogInterface, position, mItemCheckedArray.get(pos));
                      }
                      
                   
                  }
              });
        }
        
    }
    
//    private final void auroraChangeListTextColor(AuroraAlertController dialog,View view){
//        dialog.auroraChangeListTextColor(view);
//    }
// 
//    private final void auroraChangeListTextColor(View view){
//        TextView text = (TextView) view.findViewById(android.R.id.text1);
//        if(text != null){
//            text.setTextColor(Color.RED);
//        }
//    }
    
    
    // Gionee zhangxx 2012-11-01 add for CR00715173 end    
    
    // Gionee <zhangxx> <2013-05-31> add for CR00811583 begin
    private void setupButtonStyle() {
        //mButtonPositive.setGnButtonStyle(mButtonPositiveStyle);
        //mButtonNeutral.setGnButtonStyle(mButtonNeutralStyle);
    }
    // Gionee <zhangxx> <2013-05-31> add for CR00811583 end
    
    // Gionee <zhangxx><2013-05-15> add for CR00811583 begin
    public void setButtonStyle(int whichButton, int buttonStyle) {
//        if (whichButton == DialogInterface.BUTTON_POSITIVE) {
//            mButtonPositiveStyle = buttonStyle;
//        } else if (whichButton == DialogInterface.BUTTON_NEUTRAL) {
//            mButtonNeutralStyle = buttonStyle;
//        }
    }
    public void setHasCancelIcon(boolean hasCancelIcon) {
        mHasCancelIconButton = hasCancelIcon;
    }
    
    public void setCancelIcon(Drawable cancelIcon) {
        if (mButtonCancel != null) {
            mButtonCancel.setImageDrawable(cancelIcon);
        }
    }
    public void setGnWidget3Style(boolean isGnWidget3Style) {
        mIsGnWidget3Style = isGnWidget3Style;
    }
    // Gionee <zhangxx><2013-05-15> add for CR00811583 end    
    
    static class ItemAdapter extends BaseAdapter{

        private Context context;
        private int layoutRes;
        private CharSequence[] items;
        public ItemAdapter(Context context,int layoutRes,CharSequence[] items){
            this.context = context;
            this.layoutRes = layoutRes;
            this.items = items;
            
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(context).inflate(layoutRes, null);
            TextView item = (TextView) convertView.findViewById(android.R.id.text1);
            item.setText(items[position]);
            int length = items.length;
            int lastPosition = length - 1;
            if(position == lastPosition || (length == 1)){
                convertView.setBackgroundResource(R.drawable.aurora_frame_list_bottom_background_light);
            }else{
                convertView.setBackgroundResource(R.drawable.aurora_list_selector_light);
            }
            return convertView;
        }
        
    }
}


