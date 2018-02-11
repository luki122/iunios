package com.xy.smartsms.manager;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager;
import cn.com.xy.sms.sdk.ui.menu.PopMenus;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseBubbleManager;
import cn.com.xy.sms.util.ParseManager;

// Aurora xuyong 2016-03-03 added for bug #20540 start
import com.android.mms.MmsApp;
// Aurora xuyong 2016-03-03 added for bug #20540 end
import com.android.mms.R;
// Aurora xuyong 2016-03-03 added for bug #20540 start
import com.aurora.mms.util.Utils;
// Aurora xuyong 2016-03-03 added for bug #20540 end
import com.xy.smartsms.iface.IXYSmartSmsHolder;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
// Aurora xuyong 2016-03-03 added for bug #20540 start
import android.widget.Toast;
// Aurora xuyong 2016-03-03 added for bug #20540 end

public class XYComponseManager {
    public static final short SMART_SMS_DUOQU_EVENT_SHOW_EDIT_LAYOUT = 1;
    public static final short SMART_SMS_DUOQU_EVENT_HIDE_EDIT_LAYOUT = 4;
    public static final short SMART_SMS_DUOQU_EVENT_SHOW_VIEW_MENU = 5;

    private final String TAG = "XIAOYUAN";
    private static final String SECONDMENU = "secondmenu";
    private static final String ACTION_DATA = "action_data";
    private ViewGroup mXYMenuContent;
    private View mButtonToEditMenu;
    private View mShowXyMenuDivider;
    private ImageButton mAddButton;
    private View mEditRootLayout=null;
    private PopMenus mPopupWindowCustommenu;
    private ImageButton mButtonToXYMenu;
    private IXYSmartSmsHolder mXYSmsHolder;
    private LayoutInflater mLayoutInflater = null;
    private View mXYMenuRootLayout = null;
    private String mNumber = null;
    private Activity mCtx;
    private boolean mIsNotifyComposeMessage = true;
    private boolean mIsLoad = false;

    private boolean mhasMenu = false;
    private boolean mMenuIsShowing =false;

    private Animation mEnterAnim = null;
    private Animation mExitAnim = null;
    /**
     * get SmartSmsMenu root-view
     */
    public View getMenuRootView() {
        return mXYMenuRootLayout;
    }

    public XYComponseManager(IXYSmartSmsHolder xiaoYuanSmsHolder) {
        this.mXYSmsHolder = xiaoYuanSmsHolder;
        mCtx = xiaoYuanSmsHolder.getActivityContext();
        mEnterAnim = AnimationUtils.loadAnimation(mCtx, R.anim.aurora_xymenu_enter_anim);
        mExitAnim = AnimationUtils.loadAnimation(mCtx, R.anim.aurora_xymenu_exit_anim);
    }

    /**
     * query Menu data by recipientNumber
     */
    public void loadMenu(IXYSmartSmsHolder xiaoYuanSmsHolder, String recipientNumber) {

        if (recipientNumber == null) {
            Log.w(TAG, XYComponseManager.class.getName()
                    + "  queryMenu recipientNumber is null");
            return;
        }
        if (mCtx == null) {
            return;
        }
        // Aurora xuyong 2016-03-09 modified for bug #20806 start
        if (mNumber == null || !mNumber.equals(recipientNumber) || mIsLoad) {
        // Aurora xuyong 2016-03-09 modified for bug #20806 end
            mNumber = recipientNumber;
            // Aurora xuyong 2016-03-09 deleted for bug #20806 start
            //mIsLoad = false;
            // Aurora xuyong 2016-03-09 deleted for bug #20806 end
            queryMenu(recipientNumber, mCtx, xiaoYuanSmsHolder);
        }
    }

    // Aurora xuyong 2016-03-09 added for bug #20806 start
    private boolean ismMenuIsShowing = false;
    // Aurora xuyong 2016-03-09 added for bug #20806 end
    // Aurora xuyong 2016-03-15 added for xy-smartsms start
    public void showNone() {
        if (mXYMenuRootLayout != null) {
            mXYMenuRootLayout.setVisibility(View.GONE);
        }
        if (mEditRootLayout != null) {
            mEditRootLayout.setVisibility(View.GONE);
        }
    }
    // Aurora xuyong 2016-03-15 added for xy-smartsms end
    public void showMenu() {
        // Aurora xuyong 2016-03-09 added for bug #20806 start
        ismMenuIsShowing = true;
        // Aurora xuyong 2016-03-09 added for bug #20806 end
        if (mXYMenuRootLayout != null) {
            mXYMenuRootLayout.setVisibility(View.VISIBLE);
            mXYMenuRootLayout.startAnimation(mEnterAnim);
        }
        mhasMenu = true;
        mMenuIsShowing = true;
        mXYSmsHolder.setHasXyMenu(mhasMenu);
        if (mEditRootLayout != null) {
            mEditRootLayout.setVisibility(View.GONE);
            mEditRootLayout.startAnimation(mExitAnim);
        }
    }

    public void hideMenu() {
        // Aurora xuyong 2016-03-09 added for bug #20806 start
        ismMenuIsShowing = false;
        // Aurora xuyong 2016-03-09 added for bug #20806 end
        if (mAddButton != null) {
            mAddButton.setVisibility(View.GONE);
        }
        if (mXYMenuRootLayout != null) {
            mXYMenuRootLayout.setVisibility(View.GONE);
            mXYMenuRootLayout.startAnimation(mExitAnim);
        }
        mMenuIsShowing = false;
        if (mEditRootLayout != null) {
            mEditRootLayout.setVisibility(View.VISIBLE);
            mEditRootLayout.startAnimation(mEnterAnim);
        }
    }
    // Aurora xuyong 2016-01-28 added for xy-smartsms start
    public void changeMenuLayoutVisibility(int status) {
        if (mXYMenuRootLayout != null) {
            if (status == View.VISIBLE && !mXYSmsHolder.isRejectMode()) {
                if (mMenuIsShowing) {
                    mXYMenuRootLayout.setVisibility(View.VISIBLE);
                    mEditRootLayout.setVisibility(View.GONE);
                } else {
                    mXYMenuRootLayout.setVisibility(View.GONE);
                    mEditRootLayout.setVisibility(View.VISIBLE);
                }
            } else {
                mXYMenuRootLayout.setVisibility(View.GONE);
                mEditRootLayout.setVisibility(View.GONE);
            }
        }
    }
    // Aurora xuyong 2016-01-28 added for xy-smartsms end

    public boolean getIsNotifyComposeMessage() {
        return mIsNotifyComposeMessage;
    }

    private void initMenu() {
        if (mXYSmsHolder == null) {
            Log.w(TAG, XYComponseManager.class.getName()+ "  initSmartSmsMenuManager iSmartSmsUIHolder is null");
            return;
        }
        if (mCtx == null) {
            Log.w(TAG,
                    XYComponseManager.class.getName()+ "  initSmartSmsMenuManager iSmartSmsUIHolder.getActivityContext() is null");
            return;
        }
        mLayoutInflater = (LayoutInflater) mCtx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initBottomMenuView(mCtx, mXYSmsHolder);

    }

    /**
     * initialization SmartSmsMenu all views
     */
    private void initBottomMenuView(Activity ctx,
            final IXYSmartSmsHolder xiaoYuanSmsHolder) {

        if (mXYMenuRootLayout == null) {
            ViewStub mMenuRootStub = (ViewStub) xiaoYuanSmsHolder.findViewById(R.id.duoqu_menu_layout_stub);
            if (mMenuRootStub == null) {
                Log.w(TAG, XYComponseManager.class.getName()+ " initBottomMenu menuRootStub is null.");
                return;
            }
            mXYMenuRootLayout = mMenuRootStub.inflate();
        }
        if (mXYMenuRootLayout == null) {
            return;
        }
        mButtonToXYMenu = (ImageButton)xiaoYuanSmsHolder.findViewById(R.id.duoqu_button_menu);
        mShowXyMenuDivider = xiaoYuanSmsHolder.findViewById(R.id.aurora_show_menu_divider);
        mAddButton = (ImageButton)xiaoYuanSmsHolder.findViewById(R.id.gn_insert_attach_btn);
        mEditRootLayout = xiaoYuanSmsHolder.findViewById(R.id.bottom_panel);
        mXYMenuContent = (LinearLayout) mXYMenuRootLayout.findViewById(R.id.layout_menu);

        mButtonToEditMenu = (ImageButton) mXYMenuRootLayout.findViewById(R.id.layout_exchange);
        mButtonToEditMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();

            }
        });
    }

    private void queryMenu(final String recipientNumber, final Activity ctx,
            final IXYSmartSmsHolder xiaoYuanSmsHolder) {
        // Aurora xuyong 2016-03-09 modified for bug #20806 start
        if (mIsLoad) {
            // Aurora xuyong 2016-03-15 added for xy-smartsms start
            if (xiaoYuanSmsHolder != null && xiaoYuanSmsHolder.isBatchMode() || xiaoYuanSmsHolder.isRejectMode()) {
                showNone();
                return;
            }
            // Aurora xuyong 2016-03-15 added for xy-smartsms end
            if (ismMenuIsShowing) {
                showMenu();
            } else {
                hideMenu();
            }
            return;
        }
        mIsLoad = false;
        // Aurora xuyong 2016-03-09 modified for bug #20806 end
        if (TextUtils.isEmpty(recipientNumber)) {
            return;
        }
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... arg0) {
                // DuoquBubbleViewManager.clearCacheData();
                loadBubbleData(recipientNumber);
                String formatNumber = StringUtils
                        .getPhoneNumberNo86(recipientNumber);
                String json = ParseManager.queryMenuByPhoneNum(ctx,
                        formatNumber, 1, null, null);
               android.util.Log.e("xiaoyuan_menu", "menu json: "+json);
                return json;
            }

            @Override
            protected void onPostExecute(Object result) {
                String json = (String) result;
                if (TextUtils.isEmpty(json)) {
                    mIsLoad = true;
                    return;
                }
                try {
                    /**
                     * json is not empty, show the SmartSmsMenu
                     */
                    initMenu();
                    beforeInitBubbleView();
                    if (null == mXYMenuRootLayout || null == mButtonToXYMenu
                            || null == mXYMenuContent
                            || null == mLayoutInflater) {
                        return;
                    }
                    bindMenuView(new JSONArray(json), ctx);
                    mButtonToXYMenu.setVisibility(View.VISIBLE);
                    mButtonToXYMenu
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    showMenu();
                                }
                            });
                    mIsLoad = true;
                    // Aurora xuyong 2016-03-15 modified for xy-smartsms start
                    if (xiaoYuanSmsHolder.isRejectMode()) {
                        showNone();
                    } else {
                        if (!xiaoYuanSmsHolder.isBatchMode()) {
                            showMenu();
                        } else {
                            hideMenu();
                        }
                    }
                    // Aurora xuyong 2016-03-15 modified for xy-smartsms end
                    mButtonToXYMenu.setVisibility(View.VISIBLE);
                    mShowXyMenuDivider.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    mIsLoad = false;
                    Log.w(TAG,
                            "SmartSmsMenuManager queryMenu onPostExecute error: "
                                    + e.getMessage());
                }
            }
        };
        task.execute();

    }

    private void beforeInitBubbleView() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DuoquBubbleViewManager.beforeInitBubbleView(mCtx, mNumber);
            }
        }, 2000);
    }

    /**
     * load bubble data by recipientNumber
     * 
     * @param recipientNumber
     */
    private void loadBubbleData(final String recipientNumber) {
        if (mCtx == null) {
            return;
        }
        Thread thread = new Thread() {
            public void run() {
                ParseBubbleManager.loadBubbleDataByPhoneNum(recipientNumber,
                        true);
                mIsNotifyComposeMessage = ParseManager.isEnterpriseSms(mCtx,
                        recipientNumber, null, null);
            }
        };
        thread.start();
    }

    private void bindMenuView(JSONArray jsonCustomMenu, final Activity ctx)
            throws JSONException {
        if (jsonCustomMenu != null && jsonCustomMenu.length() > 0) {
            mXYMenuContent.removeAllViews();
            JSONArray btnJson = jsonCustomMenu;
            Drawable drawable = mXYMenuContent.getResources().getDrawable(R.drawable.duoqu_menu_item_logo);
            int drawableParam = (int)ViewUtil.getDimension(R.dimen.duoqu_menu_logo_param);
            drawable.setBounds(0,0,drawableParam,drawableParam);
//            Drawable drawable = mXYMenuContent.getResources().getDrawable(R.drawable.ic_launcher);
//            int drawableParam = (int) ViewUtil.getDimension(R.dimen.duoqu_menu_logo_param);
//            int drawableParam =10;
//            Drawable Drawable =null;
//            drawable.setBounds(0, 0, drawableParam, drawableParam);
            for (int i = 0; i < btnJson.length(); i++) {

                final JSONObject ob = btnJson.getJSONObject(i);
                final LinearLayout layout = (LinearLayout) mLayoutInflater
                        .inflate(R.layout.duoqu_item_custommenu, null);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                        1.0f);
                layout.setLayoutParams(lp);
                TextView tvCustommenuName = (TextView) layout
                        .findViewById(R.id.duoqu_custommenu_name);
                tvCustommenuName.setText(ob.getString("name"));
                if (ob.has(SECONDMENU)
                        && ob.getJSONArray(SECONDMENU).length() > 0) {
                    tvCustommenuName.setCompoundDrawables(drawable, null, null,null);
                }
                layout.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                             // Aurora xuyong 2016-03-03 modified for bug #20540 start
                             if (Utils.isSuperMode(ctx)) {
                                 return;
                             }
                             if (ob.has(SECONDMENU)) {
                                 if (ob.getJSONArray(SECONDMENU).length() == 0) {
                                     Map<String, String> extend = new HashMap<String, String>();
                                     ParseManager.doAction(ctx, ob.get(ACTION_DATA).toString(), extend);
                                 } else {
                                     mPopupWindowCustommenu = new PopMenus(ctx, ob.getJSONArray(SECONDMENU), 0, 0);
                                     mPopupWindowCustommenu.showAtLocation(layout);
                                 }
                             } else {
                                 Map<String, String> extend = new HashMap<String, String>();
                                 ParseManager.doAction(ctx, ob.get(ACTION_DATA).toString(), extend);
                             }
                             // Aurora xuyong 2016-03-03 modified for bug #20540 end
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }

                });
                mXYMenuContent.addView(layout);
            }
        } else {
            mXYMenuRootLayout.setVisibility(View.GONE);
        }
    }

}
