package com.aurora.puremanager.activity;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.adapter.PureBackgroundAppAdapter;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.BaseData;
import com.aurora.puremanager.data.Constants;
import com.aurora.puremanager.interfaces.Observer;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.loader.PureBackgroundLoader;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.utils.LogUtils;
import com.aurora.puremanager.utils.StringUtils;
import com.aurora.puremanager.utils.mConfig;

import java.util.ArrayList;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

/**
 * 纯净后台主界面
 */
public class PureBackgroundActivity extends AuroraActivity implements
        AdapterView.OnItemClickListener, Observer, LoaderManager.LoaderCallbacks<Object> {
    private static final String TAG = "PureBackground";

    private int pureAppCount = 0;
    private TextView tvPureAppCount;

    private ListView ListView;
    private PureBackgroundAppAdapter adapter;
    private List<BaseData> AppList = new ArrayList<BaseData>();

    private int clickPosition = -1;
    private static final int REQUEST_CODE_PUREBACKGROUND_DETAIL = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mConfig.isNative) {
            setContentView(R.layout.activity_pure_background);
        } else {
            setAuroraContentView(R.layout.activity_pure_background, AuroraActionBar.Type.Normal);
            setAuroraActionbarSplitLineVisibility(View.GONE);
            getAuroraActionBar().setTitle(R.string.pure_manage);
            getAuroraActionBar().setBackgroundResource(R.color.pure_background_title_color);
        }

        initViews();
        initData();
    }

    private void initViews() {
        tvPureAppCount = (TextView) findViewById(R.id.tv_pure_app_count);
        ListView = (ListView) findViewById(R.id.list);
        ListView.setOnItemClickListener(this);
    }

    private void initData() {
        tvPureAppCount.setText(pureAppCount + "");
        getLoaderManager().initLoader(PureBackgroundLoader.ID_LOADER_PURE_APP_COUNT, null, this);
        getLoaderManager().initLoader(PureBackgroundLoader.ID_LOADER_USER_APP_LIST, null, this);
        ConfigModel.getInstance(this).getAppInfoModel().attach(this);
    }

    public void initOrUpdateListData() {
        if (ListView == null) {
            return;
        }

        LogUtils.printWithLogCat(TAG, "initOrUpdateListData");

        if (adapter == null) {
            adapter = new PureBackgroundAppAdapter(this, AppList);
            ListView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        if (AppList == null || AppList.size() == 0) {
            findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.list).setVisibility(View.GONE);
        } else {
            findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
            findViewById(R.id.list).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ListView != null) {
            ((AuroraListView) ListView).auroraOnResume();
        }
    }

    @Override
    protected void onPause() {
        if (ListView != null) {
            ((AuroraListView) ListView).auroraOnPause();
        }
        super.onPause();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            LogUtils.printWithLogCat(TAG, "RESULT_OK");
            switch (requestCode) {
                case REQUEST_CODE_PUREBACKGROUND_DETAIL:
                    AppInfo appInfo = (AppInfo) adapter.getItem(clickPosition);
                    if (appInfo != null) {
                        appInfo.setIsStopAutoStart(data.getBooleanExtra("StopAutoStart", true));
                        appInfo.setIsStopWakeup(data.getBooleanExtra("StopWake", true));
                        appInfo.setIsAutoSleep(data.getBooleanExtra("AutoSleep", true));
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

    public void exitSelf() {
        finish();
    }

    private void releaseObject() {
        if (AppList != null) {
            AppList.clear();
        }
    }

    @Override
    public void onBackPressed() {
        exitSelf();
    }

    @Override
    protected void onDestroy() {
        releaseObject();
        ConfigModel.getInstance(this).getAppInfoModel().detach(this);
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (adapter == null) {
            return;
        }

        if (position < adapter.getCount()) {
            AppInfo appInfo = (AppInfo) adapter.getItem(position);
            clickPosition = position;
            if (appInfo != null && !StringUtils.isEmpty(appInfo.getPackageName())) {
                Intent intent = new Intent(this, PureBackgroundDetailActivity.class);
                intent.setData(Uri.fromParts(Constants.SCHEME, appInfo.getPackageName(), null));
                startActivityForResult(intent, REQUEST_CODE_PUREBACKGROUND_DETAIL);
            }
        }
    }

    @Override
    public void updateOfInit(Subject subject) {
    }

    @Override
    public void updateOfInStall(Subject subject, String pkgName) {
        getLoaderManager().restartLoader(PureBackgroundLoader.ID_LOADER_USER_APP_LIST, null, this);
        getLoaderManager().restartLoader(PureBackgroundLoader.ID_LOADER_PURE_APP_COUNT, null, this);
    }

    @Override
    public void updateOfCoverInStall(Subject subject, String pkgName) {
    }

    @Override
    public void updateOfUnInstall(Subject subject, String pkgName) {
        getLoaderManager().restartLoader(PureBackgroundLoader.ID_LOADER_USER_APP_LIST, null, this);
        getLoaderManager().restartLoader(PureBackgroundLoader.ID_LOADER_PURE_APP_COUNT, null, this);
    }

    @Override
    public void updateOfRecomPermsChange(Subject subject) {
    }

    @Override
    public void updateOfExternalAppAvailable(Subject subject, List<String> pkgList) {
    }

    @Override
    public void updateOfExternalAppUnAvailable(Subject subject, List<String> pkgList) {
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        return new PureBackgroundLoader(PureBackgroundActivity.this);
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        if (loader.getId() == PureBackgroundLoader.ID_LOADER_PURE_APP_COUNT) {
            LogUtils.printWithLogCat(TAG, "onLoadFinished -> ID_LOADER_PURE_APP_COUNT");
            pureAppCount = (int) data;
            tvPureAppCount.setText(pureAppCount + "");
        } else if (loader.getId() == PureBackgroundLoader.ID_LOADER_USER_APP_LIST) {
            LogUtils.printWithLogCat(TAG, "onLoadFinished -> ID_LOADER_USER_APP_LIST");
            if (data != null && AppList != null) {
                AppList.clear();
                AppList.addAll((List<BaseData>) data);
            }
            initOrUpdateListData();
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
    }
}