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

package com.aurora.apihook.dialog;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import android.R;

import com.android.internal.app.AlertController.RecycleListView;
import com.android.internal.app.AlertController.AlertParams.OnPrepareListViewListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
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
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.os.Process;
import android.widget.SpinnerAdapter;

import java.lang.ref.WeakReference;

import com.android.internal.app.AlertActivity;
public class AuroraAlertController {

    private final Context mContext;
    private final DialogInterface mDialogInterface;
    private final Window mWindow;

    CharSequence mTitle;

    CharSequence mMessage;

    
    ListView mListView;

    View mView;

    int mViewSpacingLeft;

    int mViewSpacingTop;

    int mViewSpacingRight;

    int mViewSpacingBottom;

    boolean mViewSpacingSpecified = false;

    Button mButtonPositive;

    CharSequence mButtonPositiveText;

    Message mButtonPositiveMessage;

    Button mButtonNegative;

    CharSequence mButtonNegativeText;

    Message mButtonNegativeMessage;

    Button mButtonNeutral;

    CharSequence mButtonNeutralText;

    Message mButtonNeutralMessage;

    ScrollView mScrollView;

    int mIconId = -1;

    Drawable mIcon;

    ImageView mIconView;

    TextView mTitleView;

    TextView mMessageView;

    View mCustomTitleView;

    boolean mForceInverseBackground;

    ListAdapter mAdapter;

    int mCheckedItem = -1;

    private int mAlertDialogLayout;
    private int mListLayout;
    private int mMultiChoiceItemLayout;
    private int mSingleChoiceItemLayout;
    private int mListItemLayout;
    
    private int mAndroidDialogLayout;

    private int mColorWhite = 0xFFFFFFFF;
    private int mColorGrey = 0xFF908E8E;

    private Handler mHandler;

    boolean isCursorAdapte = false;

    private int mProcessUid;

    private boolean mHasAdapter = false;

    private boolean hasTitle = false;
    private boolean hasButton = false;
    private boolean hasView = false;
    private boolean ViewIsList = false;
    private boolean hasSpinner = false;
    private boolean listHasData = false;
    
    private boolean hasMessage = false;
    private int mButtonCount = 3;
    
    private boolean mIsAuroraStyle;
    
    private boolean mFromAlertActivity =false;
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

            // Post a message so we dismiss after the above handlers are
            // executed
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
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }

    private static boolean shouldCenterSingleButton(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(com.android.internal.R.attr.alertDialogCenterButtons,
                outValue, true);
        return outValue.data != 0;
    }

    public AuroraAlertController(Context context, DialogInterface di, Window window) {
        mContext = context;
        mDialogInterface = di;
        mWindow = window;
        mHandler = new ButtonHandler(di);

        TypedArray a = context.obtainStyledAttributes(null,
                com.android.internal.R.styleable.AlertDialog,
                com.android.internal.R.attr.alertDialogStyle, 0);

        mAlertDialogLayout = com.aurora.R.layout.aurora_alert_dialog_internal;
        mListLayout = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_listLayout,
                com.android.internal.R.layout.select_dialog);
        mMultiChoiceItemLayout = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_multiChoiceItemLayout,
                com.android.internal.R.layout.select_dialog_multichoice);
        mSingleChoiceItemLayout = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_singleChoiceItemLayout,
                com.android.internal.R.layout.select_dialog_singlechoice);
        mListItemLayout = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_listItemLayout,
                com.android.internal.R.layout.select_dialog_item);

        
        a.recycle();
        mProcessUid = android.os.Process.myUid();
        mAndroidDialogLayout = com.aurora.R.layout.android_alert_dialog;
        mIsAuroraStyle = getMobilModel().contains("IUNI")||getMobilModel().contains("MI");
    }
    private String getMobilModel(){
        return android.os.Build.MODEL;
    }

    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }

        if (!(v instanceof ViewGroup)) {
            return false;
        }

        ViewGroup vg = (ViewGroup) v;
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
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        if (mView == null || !canTextInput(mView)) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
        // mWindow.closeAllPanels();
        mWindow.setContentView(mAlertDialogLayout);
        auroraSetupView();
        aurorasetupDecor();
    }
    private void aurorasetupDecor() {
        final View decor = mWindow.getDecorView();
        final View parent = mWindow.findViewById(com.aurora.R.id.aurora_parentPanel);
        if (parent != null && decor != null) {
            decor.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                    if (insets.isRound()) {
                        // TODO: Get the padding as a function of the window size.
                        int roundOffset = 50;//mContext.getResources().getDimensionPixelOffset(
//                                R.dimen.alert_dialog_round_padding);
                        parent.setPadding(roundOffset, roundOffset, roundOffset, roundOffset);
                    }
                    return insets.consumeSystemWindowInsets();
                }
            });
            decor.setFitsSystemWindows(true);
            decor.requestApplyInsets();
        }
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
            hasTitle = true;
        }
        if(!TextUtils.isEmpty(title)){
            hasTitle = true;
        }
    }

    /**
     * @see AlertDialog.Builder#setCustomTitle(View)
     */
    public void setCustomTitle(View customTitleView) {
        mCustomTitleView = customTitleView;
        hasTitle = true;
    }

    public void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
        if(!TextUtils.isEmpty(message)){
           hasMessage = true;
        }
    }

    /**
     * Set the view to display in the dialog.
     */
    public void setView(View view) {
        mView = view;
        mViewSpacingSpecified = false;
        hasView = true;
        if (view instanceof AbsListView) {
            ViewIsList = true;
        }
    }

    /**
     * Set the view to display in the dialog along with the spacing around that
     * view
     */
    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight,
            int viewSpacingBottom) {
        mView = view;
        mViewSpacingSpecified = true;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
        hasView = true;
        if (view instanceof AbsListView) {
            ViewIsList = true;
        }
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
        hasButton = true;
    }

    /**
     * Set resId to 0 if you don't want an icon.
     * 
     * @param resId the resourceId of the drawable to use as the icon or 0 if
     *            you don't want an icon.
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
     * @param attrId the attributeId of the theme-specific drawable to resolve
     *            the resourceId for.
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

    @SuppressWarnings({
        "UnusedDeclaration"
    })
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    @SuppressWarnings({
        "UnusedDeclaration"
    })
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    private void auroraSetupView(){
        LinearLayout contentPanel = (LinearLayout) mWindow
                .findViewById(com.aurora.R.id.aurora_contentPanel);
        setupContent(contentPanel);
        boolean hasButtons = setupButtons();

        LinearLayout topPanel = (LinearLayout) mWindow
                .findViewById(com.aurora.R.id.aurora_topPanel);
        TypedArray a = mContext.obtainStyledAttributes(
                null, com.android.internal.R.styleable.AlertDialog,
                com.android.internal.R.attr.alertDialogStyle, 0);
        boolean hasTitle = setupTitle(topPanel);

        View buttonPanel = mWindow.findViewById(com.aurora.R.id.aurora_buttonPanel);
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
            mWindow.setCloseOnTouchOutsideIfNotSet(true);
        }

        FrameLayout customPanel = null;
        if (mView != null) {
            customPanel = (FrameLayout) mWindow.findViewById(com.aurora.R.id.aurora_customPanel);
            FrameLayout custom = (FrameLayout) mWindow.findViewById(com.aurora.R.id.aurora_custom);
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                parent.removeView(mView);
            }
//            if(!isThirdPartApp()){
//                auroraSetAbsList(mView);
//            }
            
            custom.addView(mView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            if (mViewSpacingSpecified) {
                custom.setPadding(mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                        mViewSpacingBottom);
            }
            if (mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0;
            }

        } else {
            mWindow.findViewById(com.aurora.R.id.aurora_customPanel).setVisibility(View.GONE);
        }

        /*
         * Only display the divider if we have a title and a custom view or a
         * message.
         */
        if (hasTitle) {
            View divider = null;
            if (mMessage != null || mView != null || mListView != null) {
                divider = mWindow.findViewById(com.aurora.R.id.aurora_titleDivider);
            } else {
                divider = mWindow.findViewById(com.aurora.R.id.aurora_titleDivider);
            }
            if (divider != null) {
                    divider.setVisibility(View.GONE);
            }
        }
            setBackground(topPanel, contentPanel, customPanel, hasButtons, a, hasTitle, buttonPanel);
        
        a.recycle();
    }
    
    private void setupView(boolean isAndroid) {
        LinearLayout contentPanel = (LinearLayout) mWindow
                .findViewById(com.aurora.R.id.aurora_contentPanel);
        setupContent(contentPanel);
        boolean hasButtons = setupButtons();

        LinearLayout topPanel = (LinearLayout) mWindow
                .findViewById(com.aurora.R.id.aurora_topPanel);
        TypedArray a = mContext.obtainStyledAttributes(
                null, com.android.internal.R.styleable.AlertDialog,
                com.android.internal.R.attr.alertDialogStyle, 0);
        boolean hasTitle = setupTitle(topPanel);

        View buttonPanel = mWindow.findViewById(com.aurora.R.id.aurora_buttonPanel);
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
            mWindow.setCloseOnTouchOutsideIfNotSet(true);
        }

        FrameLayout customPanel = null;
        if (mView != null) {
            customPanel = (FrameLayout) mWindow.findViewById(com.aurora.R.id.aurora_customPanel);
            FrameLayout custom = (FrameLayout) mWindow.findViewById(com.aurora.R.id.aurora_custom);
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                parent.removeView(mView);
            }
//            if(!isThirdPartApp()){
//                auroraSetAbsList(mView);
//            }
            
            custom.addView(mView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            if (mViewSpacingSpecified) {
                custom.setPadding(mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                        mViewSpacingBottom);
            }
            if (mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0;
            }

        } else {
            mWindow.findViewById(com.aurora.R.id.aurora_customPanel).setVisibility(View.GONE);
        }

        /*
         * Only display the divider if we have a title and a custom view or a
         * message.
         */
        if (hasTitle) {
            View divider = null;
            if (mMessage != null || mView != null || mListView != null) {
                divider = mWindow.findViewById(com.aurora.R.id.aurora_titleDivider);
            } else {
                divider = mWindow.findViewById(com.aurora.R.id.aurora_titleDivider);
            }
            if (divider != null) {
                    divider.setVisibility(View.GONE);
            }
        }
            setAndroidBackground(topPanel, contentPanel, customPanel, hasButtons, a, hasTitle, buttonPanel);
        a.recycle();
    }

    private boolean setupTitle(LinearLayout topPanel) {
        boolean hasTitle = true;

        if (mCustomTitleView != null) {
            // Add the custom title view directly to the topPanel layout
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ViewGroup titleParent = (ViewGroup) mCustomTitleView.getParent();
            if (titleParent != null) {
                titleParent.removeView(mCustomTitleView);
            }
            topPanel.addView(mCustomTitleView, 0, lp);

            // Hide the title template
            View titleTemplate = mWindow.findViewById(com.aurora.R.id.aurora_title_template);
            titleTemplate.setVisibility(View.GONE);
        } else {
            final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);

            mIconView = (ImageView) mWindow.findViewById(R.id.icon);
            if (hasTextTitle) {
                /* Display the title if a title is supplied, else hide it */
                mTitleView = (TextView) mWindow.findViewById(com.aurora.R.id.aurora_alertTitle);

                mTitleView.setText(mTitle);
                /*
                 * Do this last so that if the user has supplied any icons we
                 * use them instead of the default ones. If the user has
                 * specified 0 then make it disappear.
                 */
                if (mIconId > 0) {
                    mIconView.setImageResource(mIconId);
                } else if (mIcon != null) {
                    mIconView.setImageDrawable(mIcon);
                } else if (mIconId == 0) {

                    /*
                     * Apply the padding from the icon to ensure the title is
                     * aligned correctly.
                     */
                    mTitleView.setPadding(mIconView.getPaddingLeft(),
                            mIconView.getPaddingTop(),
                            mIconView.getPaddingRight(),
                            mIconView.getPaddingBottom());
                    mIconView.setVisibility(View.GONE);
                }
            } else {

                // Hide the title template
                View titleTemplate = mWindow.findViewById(com.aurora.R.id.aurora_title_template);
                titleTemplate.setVisibility(View.GONE);
                mIconView.setVisibility(View.GONE);
                topPanel.setVisibility(View.GONE);
                hasTitle = false;
            }
        }
        return hasTitle;
    }

    private void setupContent(LinearLayout contentPanel) {
        mScrollView = (ScrollView) mWindow.findViewById(com.aurora.R.id.aurora_scrollView);
        if (mScrollView == null) {
            return;
        }
        mScrollView.setFocusable(false);

        // Special case for users that only want to display a String
        mMessageView = (TextView) mWindow.findViewById(R.id.message);
        if (mMessageView == null) {
            return;
        }

        if (mMessage != null) {
            mMessageView.setText(mMessage);
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);

            if (mListView != null) {
                contentPanel.removeView(mWindow.findViewById(com.aurora.R.id.aurora_scrollView));
                ViewGroup listParent = (ViewGroup) mListView.getParent();
                if (listParent != null) {
                    listParent.removeView(mListView);
                }
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
        mButtonPositive = (Button) mWindow.findViewById(R.id.button1);
        mButtonPositive.setOnClickListener(mButtonHandler);

        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive.setVisibility(View.GONE);
            mButtonCount--;
        } else {
            mButtonPositive.setText(mButtonPositiveText);
            mButtonPositive.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }

        mButtonNegative = (Button) mWindow.findViewById(R.id.button2);
        mButtonNegative.setOnClickListener(mButtonHandler);

        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative.setVisibility(View.GONE);
            mButtonCount--;
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            mButtonNegative.setVisibility(View.VISIBLE);

            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }

        mButtonNeutral = (Button) mWindow.findViewById(R.id.button3);
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
                centerButton(mButtonNeutral);
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
        View leftSpacer = mWindow.findViewById(com.aurora.R.id.aurora_leftSpacer);
        if (leftSpacer != null) {
            leftSpacer.setVisibility(View.VISIBLE);
        }
        View rightSpacer = mWindow.findViewById(com.aurora.R.id.aurora_rightSpacer);
        if (rightSpacer != null) {
            rightSpacer.setVisibility(View.VISIBLE);
        }
    }

    private void setAndroidBackground(LinearLayout topPanel, LinearLayout contentPanel,
            View customPanel, boolean hasButtons, TypedArray a, boolean hasTitle,
            View buttonPanel){
        int[] backgrounds = auroraChangeToAndroid();

        /*
         * com.aurora.R.drawable.aurora_dialog_white_bg We now set the
         * background of all of the sections of the alert. First collect
         * together each section that is being displayed along with whether it
         * is on a light or dark background, then run through them setting their
         * backgrounds. This is complicated because we need to correctly use the
         * full, top, middle, and bottom graphics depending on how many views
         * they are and where they appear.
         */
        if (mAdapter != null && mAdapter.getCount() > 0) {
            listHasData = true;
        }
        
        int fullDark = backgrounds[0];
        int topDark = backgrounds[1];
        int centerDark = backgrounds[2];
        int bottomDark = backgrounds[3];
        int fullBright = backgrounds[4];
        int topBright = backgrounds[5];
        int centerBright = backgrounds[6];
        int bottomBright = backgrounds[7];
        int bottomMedium = backgrounds[8];
        TypedArray array = mContext.obtainStyledAttributes(
                    null, com.android.internal.R.styleable.TextAppearance,
                    android.R.attr.textAppearanceLarge, 0);
            // mTitleView.setTextColor(android.R.a)
            int titleColor = array.getColor(
                    com.android.internal.R.styleable.TextAppearance_textColor, 0x00000000);
            array.recycle();
            array = mContext.obtainStyledAttributes(
                    null, com.android.internal.R.styleable.TextAppearance,
                    android.R.attr.textAppearanceMedium, 0);
            int messageColor = array.getColor(
                    com.android.internal.R.styleable.TextAppearance_textColor, 0x00000000);
            if (mMessageView != null) {
                mMessageView.setTextColor(messageColor);
            }
            if (mTitleView != null) {
                mTitleView.setTextColor(titleColor);
            }
            array.recycle();
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

        /*
         * The contentPanel displays either a custom text message or a ListView.
         * If it's text we should use the dark background for ListView we should
         * use the light background. If neither are there the contentPanel will
         * be hidden so set it as null.
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
        for (pos = 0; pos < views.length; pos++) {
            View v = views[pos];
            if (v == null) {
                continue;
            }
            if (lastView != null) {
                if (!setView) {
                    lastView.setBackgroundResource(lastLight ? topBright : topDark);
                } else {
                    lastView.setBackgroundResource(lastLight ? centerBright : centerDark);
                }
                setView = true;
            }
            lastView = v;
            lastLight = light[pos];
        }

        if (lastView != null) {
            if (setView) {

                /*
                 * ListViews will use the Bright background but buttons use the
                 * Medium background.
                 */
                lastView.setBackgroundResource(
                        lastLight ? (hasButtons ? bottomMedium : bottomBright) : bottomDark);
            } else {
                lastView.setBackgroundResource(lastLight ? fullBright : fullDark);
            }
        }

        /*
         * TODO: uncomment section below. The logic for this should be if it's a
         * Contextual menu being displayed AND only a Cancel button is shown
         * then do this.
         */
        // if (hasButtons && (mListView != null)) {

        /*
         * Yet another *special* case. If there is a ListView with buttons don't
         * put the buttons on the bottom but instead put them in the footer of
         * the ListView this will allow more items to be displayed.
         */

        /*
         * contentPanel.setBackgroundResource(bottomBright);
         * buttonPanel.setBackgroundResource(centerMedium); ViewGroup parent =
         * (ViewGroup) mWindow.findViewById(R.id.parentPanel);
         * parent.removeView(buttonPanel); AbsListView.LayoutParams params = new
         * AbsListView.LayoutParams( AbsListView.LayoutParams.MATCH_PARENT,
         * AbsListView.LayoutParams.MATCH_PARENT);
         * buttonPanel.setLayoutParams(params);
         * mListView.addFooterView(buttonPanel);
         */
        // }
        if ((mListView != null) && (mAdapter != null)) {
            mListView.setAdapter(mAdapter);
            if (mCheckedItem > -1) {
                mListView.setItemChecked(mCheckedItem, true);
                mListView.setSelection(mCheckedItem);
            }

        }

        
        
    }
    public void installAndroidContent() {

        if (mView == null || !canTextInput(mView)) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
        mWindow.closeAllPanels();
        mWindow.setContentView(mAndroidDialogLayout);
        setupView(true);
    }
    private boolean isSystemUID(){
        int systemUID = Process.FIRST_APPLICATION_UID;
        return Process.myUid() < systemUID;
    }
    
    private boolean isSystemApp(Context context){
    	ApplicationInfo info = context.getApplicationInfo();
    	return isSystemApp(info);
    }
    
    
	private  boolean isSystemApp(ApplicationInfo info) {
		if(info == null){
			return false;
		}
        return ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0);  
    }  
  
    public  boolean isSystemUpdateApp(ApplicationInfo info) {  
    	if(info == null){
			return false;
		}
        return ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);  
    }  
  
    public  boolean isUserApp(ApplicationInfo info) {
    	if(info == null){
			return false;
		}
        return (!isSystemApp(info) && !isSystemUpdateApp(info));  
    }
    
    private void setBackground(LinearLayout topPanel, LinearLayout contentPanel,
            View customPanel, boolean hasButtons, TypedArray a, boolean hasTitle,
            View buttonPanel) {
        String voiceAdapter = "class com.sec.android.app.voicerecorder.VoiceListSetasActivity$SetAsDialogAdapter";
        int[] backgrounds = auroraChangeToAndroid();
        /* Get all the different background required */
//        int fullDark = com.aurora.R.drawable.aurora_dialog_white_bg;
//        int topDark = com.aurora.R.drawable.aurora_dialog_white_bg;
//        int centerDark = com.aurora.R.drawable.aurora_dialog_white_bg;
//        int bottomDark = com.aurora.R.drawable.aurora_dialog_white_bg;
        int fullBright = com.aurora.R.drawable.shape;
        int topBright = com.aurora.R.drawable.aurora_dialog_bg_top;
        int centerBright = com.aurora.R.drawable.aurora_dialog_white_bg;
        int bottomBright = com.aurora.R.drawable.aurora_dialog_bg_bottom;
        int bottomMedium = com.aurora.R.drawable.aurora_dialog_bg_bottom;

        /*
         * com.aurora.R.drawable.aurora_dialog_white_bg We now set the
         * background of all of the sections of the alert. First collect
         * together each section that is being displayed along with whether it
         * is on a light or dark background, then run through them setting their
         * backgrounds. This is complicated because we need to correctly use the
         * full, top, middle, and bottom graphics depending on how many views
         * they are and where they appear.
         */
        if (mAdapter != null && mAdapter.getCount() > 0) {
            listHasData = true;
        }
        
        int windowType = 0;
        if(mWindow != null){
         windowType = mWindow.getAttributes().type;
        }
        
        boolean isInputMethodPicker = (windowType == WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG)
        		||(windowType == WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
       
        boolean needChangeToAndroid = false;
//        
        if(hasView){
           needChangeToAndroid = true;
           Log.e("alert", "customView");
        }
        if(mContext instanceof AlertActivity){
        	mFromAlertActivity = true;
        }
        ProgressBar progressBar = (ProgressBar)mWindow.findViewById(android.R.id.progress);
        boolean hasProgressBar = false;
        if(progressBar != null){
            hasProgressBar = true;
        }
        /*mFromAlertActivity ||*/
        if( mFromAlertActivity ||hasProgressBar||isInputMethodPicker
                || mHasAdapter){
            needChangeToAndroid = false;
        }
        Log.e("alert", "customView:"+ViewIsList);
        if (needChangeToAndroid) {
        	if(!mIsAuroraStyle && !isSystemApp(mContext)
        			||mIsAuroraStyle &&!isSystemApp(mContext) || ViewIsList){
        		installAndroidContent();
        		return;
        	}
        }
        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        if(hasProgressBar){
            TextView  proMsg = (TextView)mWindow.findViewById(android.R.id.message);
            if(proMsg != null){
                proMsg.setTextColor(Color.BLACK);
            }
        }
        
        changeInnerTextColor(mView);
        
        View[] views = new View[4];
//        boolean[] light = new boolean[4];
        View lastView = null;
//        boolean lastLight = false;

        int pos = 0;
        if (hasTitle) {
            views[pos] = topPanel;
//            light[pos] = false;
            pos++;
        }

        /*
         * The contentPanel displays either a custom text message or a ListView.
         * If it's text we should use the dark background for ListView we should
         * use the light background. If neither are there the contentPanel will
         * be hidden so set it as null.
         */
        views[pos] = (contentPanel.getVisibility() == View.GONE)
                ? null : contentPanel;
//        light[pos] = mListView != null;
        pos++;
        if (customPanel != null) {
            views[pos] = customPanel;
//            light[pos] = mForceInverseBackground;
            pos++;
        }
        if (hasButtons) {
            views[pos] = buttonPanel;
//            light[pos] = true;
        }

        boolean setView = false;
        for (pos = 0; pos < views.length; pos++) {
            View v = views[pos];
            if (v == null) {
                continue;
            }
           
            if (lastView != null) {
                if (!setView) {
                    if(!hasTitle){
                        lastView.setBackgroundResource(topBright);
                    }
                } else {
                    if(!hasTitle && !hasButtons){
                        lastView.setBackgroundResource(fullBright);
                    }else{
                        lastView.setBackgroundResource(centerBright);
                    }
                    
                }
                setView = true;
            }
            lastView = v;
        }

        if (lastView != null) {
            if (setView) {

                /*
                 * ListViews will use the Bright background but buttons use the
                 * Medium background.
                 */
                lastView.setBackgroundResource(hasButtons ? bottomMedium : bottomBright);
            } else {
                lastView.setBackgroundResource(fullBright);
            }
        }
        
        

        setButtonBackground((LinearLayout)buttonPanel);
        if ((mListView != null) && (mAdapter != null)) {
//                mListView.setAdapter(mAdapter);
                ListAdapter adapter = null;
                if(mAdapter instanceof ArrayAdapter){
                	adapter = new AuroraListAdapter((ArrayAdapter)mAdapter);
                }else if(mAdapter instanceof CursorAdapter){
                	adapter = new AuroraListAdapter((CursorAdapter)mAdapter);
                }else{
                	adapter = new AuroraListAdapter(mAdapter);
                }
                mListView.setAdapter(adapter);
            if (mCheckedItem > -1) {
                mListView.setItemChecked(mCheckedItem, true);
                mListView.setSelection(mCheckedItem);
            }
            
            
        }
        
        if(!hasTitle&&hasMessage){
            if(mMessageView!= null){
                mMessageView.setPadding(mMessageView.getPaddingLeft(), dip2px(mContext,24.0f),
                        mMessageView.getPaddingRight(), mMessageView.getPaddingBottom()); 
            }
            
        }

    }
    
    private void changeInnerTextColor(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof TextView) {
            if(view instanceof Button){
            	 ((Button)view).setTextColor(mContext.getResources().getColorStateList(com.aurora.R.color.aurora_button_text_color));
            }else{
            	((TextView)view).setTextColor(Color.BLACK);
            }
            return;
        }
        if (view instanceof ViewGroup) {
            int childCount = ((ViewGroup) view).getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    changeInnerTextColor(((ViewGroup) view).getChildAt(i));
                }
            }
        }
    }
    
    private void setButtonBackground(LinearLayout buttonPanel){
        int oneButtonSrc = com.aurora.R.drawable.aurora_alert_dialog_btn_selector;
        boolean pos = (mButtonPositive != null)?(mButtonPositive.getVisibility() == View.VISIBLE):false;
        boolean neg = (mButtonNegative != null)?(mButtonNegative.getVisibility() == View.VISIBLE):false;
        boolean neu = (mButtonNeutral != null)?(mButtonNeutral.getVisibility() == View.VISIBLE):false;
        if(mButtonCount == 1){
            if((mButtonNeutral != null) && neu){
                mButtonNeutral.setBackgroundResource(oneButtonSrc);
            }
            
            if((mButtonNegative != null) && neg){
                mButtonNegative.setBackgroundResource(oneButtonSrc);
            }
            if((mButtonPositive != null) && pos){
                mButtonPositive.setBackgroundResource(oneButtonSrc);
            }
        }
        
        if(mButtonCount == 2){
            if(neg && neu){
                if(mButtonNeutral != null){
                    mButtonNeutral.setBackgroundResource(com.aurora.R.drawable.aurora_alert_dialog_btn_selector_right);
                }
               
            }
            if(pos && neu){
                if(mButtonNeutral != null){
                    mButtonNeutral.setBackgroundResource(com.aurora.R.drawable.aurora_alert_dialog_btn_selector_left);
                }
            }
        }
        
    }
    private boolean isGridVoice(String adapterName){
        if(hasGridView(mView)){
            String name = getGridAdapterName(mView);
            if(adapterName.equals(name)){
                return true;
            }
        }
        return false;
    }
    private  int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }
    private boolean isThirdPartApp(){
        return mProcessUid>android.os.Process.FIRST_APPLICATION_UID;
    }

    private boolean hasImageView(View view){
        boolean hasImageView = false;
        if(view == null){
            return false;
        }
        if(view instanceof AbsListView){
            return false;
        }
        if(view instanceof ImageView){
            return true;
        }
        if(view instanceof ViewGroup){
            int childCount = ((ViewGroup)view).getChildCount();
            if(childCount >0){
                for(int i=0;i<childCount;i++){
                    View child = ((ViewGroup)view).getChildAt(i);
                    if(child instanceof ImageView){
                        hasImageView = true;
                        break;
                    }else if(child instanceof ViewGroup){
                        hasImageView = hasImageView(child);
                        break;
                    }else if(child instanceof AbsListView){
                        hasImageView = hasImageView(child);
                        break;
                    }
                }
            }
        }
        
        return hasImageView;
    }
    private boolean hasImageButton(View view){
        boolean hasImageButton = false;
        if(view == null){
            return false;
        }
        if(view instanceof AbsListView){
            return false;
        }
        if(view instanceof ImageButton){
            return true;
        }
        if(view instanceof ViewGroup){
            int childCount = ((ViewGroup)view).getChildCount();
            if(childCount >0){
                for(int i=0;i<childCount;i++){
                    View child = ((ViewGroup)view).getChildAt(i);
                    if(child instanceof ImageButton){
                    	hasImageButton = true;
                        break;
                    }else if(child instanceof ViewGroup){
                    	hasImageButton = hasImageButton(child);
                        break;
                    }else if(child instanceof AbsListView){
                    	hasImageButton = hasImageButton(child);
                        break;
                    }
                }
            }
        }
        
        return hasImageButton;
    }





    private void auroraSetAbsList(View view) {

        if (view == null) {
            return;
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                if (child instanceof GridView) {
                        ListAdapter adapter = ((GridView) child).getAdapter();
                        AuroraListAdapter newAdapter = new AuroraListAdapter(adapter);
                        ((GridView) child).setAdapter(newAdapter);
                } else {
                    auroraSetAbsList(child);
                }
            }
        }
    }
    
    private boolean hasGridView(View view) {
        boolean has = false;
//        int maxLevel = 3;
//        if (view == null) {
//            has = false;
//        }
//        if (view instanceof GridView) {
//            has =  true;
//        }
//        if (view instanceof ViewGroup) {
//            for (int i = 0; i < maxLevel; i++) {
//                View child = ((ViewGroup) view).getChildAt(i);
//                if (child instanceof GridView) {
//                    has =  true;
//                    break;
//                } else {
//                    continue;
//                }
//            }
//        }
        return has;
    }
    
    private boolean hasListView(View view) {
        if (view == null) {
            return false;
        }
        if (view instanceof ListView) {
            ((ListView)view).setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
           return true;
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                if (child instanceof ListView) {
                    ((ListView)child).setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
                   return true;
                } else {
                   return hasListView(child);
                }
            }
        }
        return false;
    }
    
    private String getGridAdapterName(View view){
        if(view == null){
            return "";
        }
        if(view instanceof GridView){
            ListAdapter adapter = ((GridView)view).getAdapter();
            if(adapter != null){
                return adapter.getClass().toString();
            }else{
                return "";
            }
            
        }
        if(view instanceof ViewGroup){
            int childCount = ((ViewGroup)view).getChildCount();
            for(int i = 0;i<childCount;i++){
                View child = ((ViewGroup)view).getChildAt(i);
                if(child instanceof GridView){
                    ListAdapter adapter = ((GridView)child).getAdapter();
                    if(adapter != null){
                        return adapter.getClass().toString();
                    }else{
                        continue;
                    }
                }
            }
        }
        return "";
    }

    private void auroraChangeToAurora2(View group) {
        if (group == null) {
            return;
        }
        if (group instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) group;
            int childCount = parent.getChildCount();
            if (childCount <= 0) {
                return;
            }

            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                if (child instanceof ViewGroup) {

                    auroraChangeToAurora((ViewGroup) child);
                } else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(Color.BLACK);
                }
            }
        }
    }
    private void auroraChangeToAurora(View group){
        if(group == null){
            return;
        }
        if(group instanceof ViewGroup){
            ViewGroup parent = (ViewGroup)group;
            int childCount = parent.getChildCount();
            if(childCount <=0){
                return;
            }
            
            for(int i = 0;i<childCount;i++){
                View child = parent.getChildAt(i);
                if(child instanceof ViewGroup){
                    
                    auroraChangeToAurora((ViewGroup)child);
                }else if(child instanceof TextView){
                    int currentColor = ((TextView)child).getCurrentTextColor();
                        ((TextView)child).setTextColor(Color.BLACK);
                   
                }
            }
        }
        
    }
    private int[] auroraChangeToAndroid() {
        int[] backgrounds = new int[9];
        TypedArray a = mContext.obtainStyledAttributes(
                null, com.android.internal.R.styleable.AlertDialog,
                com.android.internal.R.attr.alertDialogStyle, 0);

        int fullDark = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_fullDark,
                com.aurora.R.drawable.aurora_dialog_white_bg);
        int topDark = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_topDark,
                com.aurora.R.drawable.aurora_dialog_white_bg);
        int centerDark = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_centerDark,
                com.aurora.R.drawable.aurora_dialog_white_bg);
        int bottomDark = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_bottomDark,
                com.aurora.R.drawable.aurora_dialog_white_bg);
        int fullBright = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_fullBright,
                com.aurora.R.drawable.aurora_dialog_white_bg);
        int topBright = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_topBright,
                com.aurora.R.drawable.aurora_dialog_white_bg);
        int centerBright = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_centerBright,
                com.aurora.R.drawable.aurora_dialog_white_bg);
        int bottomBright = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_bottomBright,
                com.aurora.R.drawable.aurora_dialog_white_bg);
        int bottomMedium = a.getResourceId(
                com.android.internal.R.styleable.AlertDialog_bottomMedium,
                com.aurora.R.drawable.aurora_dialog_white_bg);

        backgrounds[0] = fullDark;
        backgrounds[1] = topDark;
        backgrounds[2] = centerDark;
        backgrounds[3] = bottomDark;
        backgrounds[4] = fullBright;
        backgrounds[5] = topBright;
        backgrounds[6] = centerBright;
        backgrounds[7] = bottomBright;
        backgrounds[8] = bottomMedium;

        return backgrounds;

    }

    boolean hasAdapterFromParams = false;
    public static class AuroraAlertParams {
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

        private boolean isCursorAdapter = false;

        
        public boolean mFromAlertActivity = false;
        
        
        public AuroraAlertParams(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void apply(AuroraAlertController dialog) {
            dialog.mFromAlertActivity = mFromAlertActivity;
            if (mAdapter != null) {
                dialog.hasAdapterFromParams = true;
            }

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
            
            if(((mCursor != null) || (mAdapter != null))){
                dialog.mHasAdapter = true;
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
             * dialog.setCancelable(mCancelable);
             * dialog.setOnCancelListener(mOnCancelListener); if (mOnKeyListener
             * != null) { dialog.setOnKeyListener(mOnKeyListener); }
             */

        }

        public static class AuroraRecycleListView extends ListView {
            boolean mRecycleOnMeasure = true;

            public AuroraRecycleListView(Context context) {
                super(context);
            }

            public AuroraRecycleListView(Context context, AttributeSet attrs) {
                super(context, attrs);
            }

            public AuroraRecycleListView(Context context, AttributeSet attrs, int defStyle) {
                super(context, attrs, defStyle);
            }

            @Override
            protected boolean recycleOnMeasure() {
                return mRecycleOnMeasure;
            }
        }


        private void createListView(final AuroraAlertController dialog) {
            int layout = 0;
            int selectDialogSingleChioce = com.aurora.R.layout.aurora_select_dialog_singlechoice_android;
            int listItemLayout = com.aurora.R.layout.aurora_select_dialog_item;
            final AuroraRecycleListView listView = new AuroraRecycleListView(mContext);
            listView.setCacheColorHint(0x00000000);
            listView.setOverScrollMode(ListView.OVER_SCROLL_IF_CONTENT_SCROLLS);
            listView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
           
            ListAdapter adapter;
            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = new ArrayAdapter<CharSequence>(
                            mContext, dialog.mMultiChoiceItemLayout, R.id.text1, mItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            if (mCheckedItems != null) {
                                boolean isItemChecked = mCheckedItems[position];
                                if (isItemChecked) {
                                    listView.setItemChecked(position, true);
                                }
                            }
                            TextView text = (TextView) view.findViewById(R.id.text1);
                            if (text != null) {
                                text.setTextColor(Color.BLACK);
                            }
                            Log.e("alert", "arrayAdapter");
                            return view;
                        }
                    };
                } else {
                    adapter = new CursorAdapter(mContext, mCursor, false) {
                        private final int mLabelIndex;
                        private final int mIsCheckedIndex;

                        {
                            final Cursor cursor = getCursor();
                            mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                            mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            CheckedTextView text = (CheckedTextView) view.findViewById(R.id.text1);
                            text.setText(cursor.getString(mLabelIndex));
                            listView.setItemChecked(cursor.getPosition(),
                                    cursor.getInt(mIsCheckedIndex) == 1);
                            Log.e("alert", "cursor bindView");
                        }

                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return mInflater.inflate(dialog.mMultiChoiceItemLayout,
                                    parent, false);
                        }

                    };

                    dialog.isCursorAdapte = true;
                }
            } else {
                
                if(dialog.mHasAdapter){
                    layout = mIsSingleChoice
                            ? dialog.mSingleChoiceItemLayout : dialog.mListItemLayout;
                }else{
                    layout = mIsSingleChoice
                            ? selectDialogSingleChioce : listItemLayout;
                }
                
                if (mCursor == null) {
                    adapter = (mAdapter != null) ? mAdapter
                            : new ArrayAdapter<CharSequence>(mContext, layout, android.R.id.text1, mItems);
                } else {
                    adapter = new SimpleCursorAdapter(mContext, layout,
                            mCursor, new String[] {
                                mLabelColumn
                            }, new int[] {
                                R.id.text1
                            });
                }
            }

            if (mOnPrepareListViewListener != null) {
                mOnPrepareListViewListener.onPrepareListView(listView);
            }

            /*
             * Don't directly set the adapter on the ListView as we might want
             * to add a footer to the ListView later.
             */
            dialog.mAdapter = adapter;
            dialog.mCheckedItem = mCheckedItem;

            if (mOnClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        mOnClickListener.onClick(dialog.mDialogInterface, position);
                        if (!mIsSingleChoice) {
                            dialog.mDialogInterface.dismiss();
                        }
                    }
                });
            } else if (mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        if (mCheckedItems != null) {
                            mCheckedItems[position] = listView.isItemChecked(position);
                        }
                        mOnCheckboxClickListener.onClick(
                                dialog.mDialogInterface, position, listView.isItemChecked(position));
                    }
                });
            }

            // Attach a given OnItemSelectedListener to the ListView
            if (mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(mOnItemSelectedListener);
            }

            if (mIsSingleChoice) {
                dialog.isSingleAndMult = true;
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } else if (mIsMultiChoice) {
                dialog.isSingleAndMult = true;
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            listView.mRecycleOnMeasure = mRecycleOnMeasure;

            // listView.getChildAt(index)
            dialog.mListView = listView;
        }
    }

    boolean isSingleAndMult = false;
    class AuroraListAdapter extends BaseAdapter{

        int count;
        ListAdapter adapter;
        ArrayAdapter mArrayAdapter;
        CursorAdapter mCursorAdapter;
        View dropDownView;
        
        ListAdapter mTempAdapter;
        public AuroraListAdapter(ListAdapter adapter){
            this.adapter = adapter;
            if(adapter != null){
                count = adapter.getCount();
                
            }
            mTempAdapter = adapter;
            
        }
        public AuroraListAdapter(ArrayAdapter adapter){
            this.mArrayAdapter = adapter;
            if(adapter != null){
                count = adapter.getCount();
                
            }
            mTempAdapter = adapter;
        }
        public AuroraListAdapter(CursorAdapter adapter){
            this.mCursorAdapter = adapter;
            if(adapter != null){
                count = adapter.getCount();
                
            }
            mTempAdapter = adapter;
        }
        
        
        @Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return mTempAdapter.hasStableIds();
		}




		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
			if(mTempAdapter instanceof BaseAdapter){
				((BaseAdapter)mTempAdapter).registerDataSetObserver(observer);
			}
		}




		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
			if(mTempAdapter instanceof BaseAdapter){
				((BaseAdapter)mTempAdapter).unregisterDataSetObserver(observer);
			}
		}




		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			if(mTempAdapter instanceof BaseAdapter){
				((BaseAdapter)mTempAdapter).notifyDataSetChanged();
			}
		}




		@Override
		public void notifyDataSetInvalidated() {
			// TODO Auto-generated method stub
			if(mTempAdapter instanceof BaseAdapter){
				((BaseAdapter)mTempAdapter).notifyDataSetInvalidated();
			}
		}




		@Override
		public boolean areAllItemsEnabled() {
			// TODO Auto-generated method stub
			return mTempAdapter.areAllItemsEnabled();
		}




		@Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			return mTempAdapter.isEnabled(position);
		}




		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			return mTempAdapter.getItemViewType(position);
		}




		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return super.getViewTypeCount();
		}




		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return mTempAdapter.isEmpty();
		}




		@Override
        public int getCount() {
            // TODO Auto-generated method stub
            return count;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mTempAdapter==null?null:mTempAdapter.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return mTempAdapter==null?null:mTempAdapter.getItemId(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if(position == -1){
                return convertView;
            }
            View view = mTempAdapter==null?null:mTempAdapter.getView(position, convertView, parent);
            if(view instanceof TextView){
                ((TextView)view).setTextColor(Color.BLACK);
            }
            if(view != null){
                auroraChangeToAurora2(view);
            }
            return view;
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
        	if(mArrayAdapter != null){
        		return mArrayAdapter.getDropDownView(position, convertView, parent);
        	}
        	
        	return convertView;
        }

        
    }
    

}