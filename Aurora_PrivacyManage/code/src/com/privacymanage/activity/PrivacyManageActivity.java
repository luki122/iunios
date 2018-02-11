package com.privacymanage.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.privacymanage.R;
import com.privacymanage.adapter.PrivacyManageAdapter;
import com.privacymanage.data.AccountData;
import com.privacymanage.data.BaseData;
import com.privacymanage.data.ModuleInfoData;
import com.privacymanage.data.MyArrayList;
import com.privacymanage.interfaces.AccountObserver;
import com.privacymanage.interfaces.AnimationEndListener;
import com.privacymanage.interfaces.ChildModuleObserver;
import com.privacymanage.interfaces.ChildModuleSubject;
import com.privacymanage.model.AccountModel;
import com.privacymanage.model.ChildModuleModel;
import com.privacymanage.provider.AccountProvider;
import com.privacymanage.service.WatchDogService;
import com.privacymanage.utils.ActivityUtils;
import com.privacymanage.utils.ActivityUtils.LoadCallback;
import com.privacymanage.utils.ApkUtils;
import com.privacymanage.view.PrivacyManageAnimView;

import java.util.ArrayList;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase;

public class PrivacyManageActivity extends AuroraActivity implements
        OnItemClickListener,
        ChildModuleObserver,
        AccountObserver {
    private ArrayList<BaseData> itemList;
    private PrivacyManageAdapter adapter;
    private PrivacyManageAnimView animView;
    private String TAG = PrivacyManageActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AccountModel.getInstance().getCurAccount().getAccountId()
                == AccountData.NOM_ACCOUNT) {
            finish();
            return;
        }
        setAuroraContentView(R.layout.privacy_manage_activity, AuroraActionBar.Type.Empty);
        getAuroraActionBar().setTitle(R.string.app_name);
        getAuroraActionBar().getTitleView().setTextColor(Color.WHITE);
        getAuroraActionBar().setBackgroundResource(R.drawable.aurora_action_bar_top_bg);
        getAuroraActionBar().addItem(R.drawable.header_more_button, R.id.privacy_manage_activity_menu, "");
        getAuroraActionBar().setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener() {
            @Override
            public void onAuroraActionBarItemClicked(int arg0) {
                if (AccountModel.getInstance().getCurAccount().getState() !=
                        AccountData.NO_ENTERED) {
                	//20160304 修改
                    showAuroraMenu(PrivacyManageActivity.this.getAuroraActionBar(),Gravity.TOP | Gravity.RIGHT, 0, 0);
                }
            }
        });
        setAuroraSystemMenuCallBack(auroraMenuCallBack);
        //setAuroraMenuCallBack(auroraMenuCallBack);
        setAuroraMenuItems(R.menu.privacy_manage_act_menu);
        startService(new Intent(this, WatchDogService.class));
        AccountModel.getInstance().attach(this);
        ChildModuleModel.getInstance().attach(this);
        startAnim();
        ActivityUtils.sleepForloadScreen(100, new LoadCallback() {
            @Override
            public void loaded() {
                initOrUpdateView();
                initOrUpdatetListData();
            }
        });
        ActivityMan.killAllActivitiesWithExcept(this);
    }

    @Override
    protected void onResume() {
    	
    		com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                    com.aurora.utils.SystemUtils.STATUS_BAR_MODE_WHITE, this);

        ImageView coverImg = (ImageView) findViewById(R.id.coverImg);
        if (AccountModel.getInstance().getCurAccount().getState() ==
                AccountData.NO_ENTERED) {
            coverImg.setVisibility(View.VISIBLE);
        } else {
            coverImg.setVisibility(View.INVISIBLE);
        }
        super.onResume();
    }

    private void startAnim() {
        animView = (PrivacyManageAnimView) findViewById(R.id.animView);
        animView.setAnimationImageListener(new AnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                Log.i(TAG, "onAnimationEnd");
                if (AccountModel.getInstance().getCurAccount().getState() ==
                        AccountData.NO_ENTERED) {
                    Log.i(TAG, "CUR ACCOUNT IS FIRST ENTER");
                    Intent intentUserGuide = new Intent(PrivacyManageActivity.this, UserGuide.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(UserGuide.LAUNCH_MODE_KEY, UserGuide.LAUNCH_MODE_SHOW_PAGE_HOW_EXIT);
                    intentUserGuide.putExtras(bundle);
                    PrivacyManageActivity.this.startActivity(intentUserGuide);
                    AccountModel.getInstance().getCurAccount().setState(AccountData.ENTERED);
                    AccountProvider.insertOrUpdateDate(PrivacyManageActivity.this,
                            AccountModel.getInstance().getCurAccount());
                }

            }
        });
        animView.start();
    }

    private void initOrUpdateView() {
        TextView textView = (TextView) findViewById(R.id.textView);
        long curTime = System.currentTimeMillis();
        long createTime = AccountModel.getInstance().getCurAccount().getCreateTime();
        long millisOfDay = 60 * 60 * 24 * 1000;
        long days = 1 + (curTime - createTime) / millisOfDay;
        textView.setText(String.format(
                getString(R.string.safe_keeping_privacy_content_days), "" + days));
    }

    /**
     * 更新数据
     */
    private void initOrUpdatetListData() {
        if (itemList == null) {
            itemList = new ArrayList<BaseData>();
        } else {
            itemList.clear();
        }

        MyArrayList<ModuleInfoData> childModuleList = ChildModuleModel.
                getInstance().getChildModuleList();
        for (int i = 0; i < childModuleList.size(); i++) {
            ModuleInfoData moduleInfoData = childModuleList.get(i);
            if (moduleInfoData == null ||
                    !moduleInfoData.getNeedShow() ||
                    null == ApkUtils.getActivityInfo(this,
                            moduleInfoData.getPkgName(),
                            moduleInfoData.getClassName())) {
                continue;
            }
            itemList.add(moduleInfoData);
        }

        if (adapter == null) {
            adapter = new PrivacyManageAdapter(this, itemList);
            GridView gridView = (GridView) findViewById(R.id.gridView);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
    //private AuroraMenuBase.OnAuroraMenuItemClickListener auroraSystemMenuCallBack
    private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack =
            new AuroraMenuBase.OnAuroraMenuItemClickListener() {
                @Override
                public void auroraMenuItemClick(int itemId) {
                    switch (itemId) {
                        case R.id.btn_set:
                            Intent intent = new Intent(PrivacyManageActivity.this, SetActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.btn_help:
                            Intent intent2 = new Intent(PrivacyManageActivity.this, UserGuide.class);
                            startActivity(intent2);
                            break;
                    }
                }
            };

    @Override
    protected void onDestroy() {
        ChildModuleModel.getInstance().detach(this);
        AccountModel.getInstance().detach(this);
        releaseObject();
        super.onDestroy();
    }

    /**
     * 释放不需要用的对象所占用的堆内存
     */
    private void releaseObject() {
        if (animView != null) {
            animView.stop();
        }
        if (itemList != null) {
            itemList.clear();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (adapter != null && arg2 < adapter.getCount()) {
            ModuleInfoData item = (ModuleInfoData) adapter.getItem(arg2);
            if (item == null) {
                return;
            }
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(item.getPkgName(), item.getClassName()));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, R.string.this_function_is_unavailable, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void update(ChildModuleSubject subject, ModuleInfoData moduleInfoData) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void switchAccount(AccountData accountData) {
        if (accountData != null &&
                accountData.getAccountId() == AccountData.NOM_ACCOUNT) {
            finish();
            //ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            //am.forceStopPackage(getPackageName());
        }
    }

    @Override
    public void deleteAccount(AccountData accountData, boolean delete) {
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return myOnKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
    }

    private boolean myOnKeyUp(int keyCode, KeyEvent event) {
        if (!event.hasNoModifiers()) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (event.isTracking() && !event.isCanceled()) {
                    moveTaskToBack(true);
                    finish();
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
