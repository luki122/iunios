package com.gionee.mms.online.activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraListActivity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import aurora.widget.AuroraListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;
import com.gionee.mms.online.IStatisticsBinder;
import com.gionee.mms.online.LogUtils;
import com.gionee.mms.online.OnlineUtils;
import com.gionee.mms.online.OnlineUtils.NETWORK_TPYE;
import com.gionee.mms.online.data.InformationColumns;
import com.gionee.mms.online.data.MyContentProvider;
import com.gionee.mms.online.data.RecommendColumns;
import com.gionee.mms.online.transmisson.NetTransmitManager;
import com.gionee.mms.online.transmisson.NetTransmitManager.DownloadListener;

public class RecommendInformation extends AuroraListActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, DownloadListener {

    private static final String TAG = "RecommendInformation";
    public final static int MMS_ONLINE_NO_INFORMATION = 1;
    public final static int MMS_ONLINE_NO_MORE_INFORMATION = 2;
    public final static int MMS_ONLINE_DISPLAY_MORE = 3;
    public final static int MMS_ONLINE_DISPLAY_LOADING_FAILD = 4;
    public final static int MMS_ONLINE_NO_NET = 5;
    public final static int MMS_ONLINE_REFRESH_UNABLE = 6;
    public final static int MMS_ONLINE_ONLOADING = 7;
    public final static int MMS_ONLINE_CHECK_DONE = 8;
    public final static int MMS_ONLINE_LOAD_MORE = 9;
    public final static int MMS_ONLINE_REQUEST_TIME_OUT = 10;
    public final static int MMS_ONLINE_DOWNLOADING = 11;
    public final static int MMS_ONLINE_DOWNLOAD_STATEMENT_INTERVAL = 7 * 24
            * 60 * 60 * 1000;

    private final static int MENU_FORWARD = 0;
    private final static int MENU_CPOY = 1;

    private AuroraListView mListview;
    private TextView mCategory;
//    private ImageView mIconChangeKey;
    private MenuItem mMenuRefresh;

    private MyCursorAdapter mListAdapter;
    private NetTransmitManager mNetManager = null;

    private View mTitleActionBar;
    private ImageView mCategoryBackSymble;
    private ImageView mNoNetWorks;
    private View mFoot;
    private View mMoreTextButton;
    private View mLoading;
    private View mNoNet;
    private int mCategoryid = 0;
    private int mServerCount = 0;
    private int mMaxMsgId = 0;
    private int mMinMsgId = 0;
    private int mInforamtionCount = 0;
    private boolean mIsTableEmpty = true;
    private boolean sIsLoading = false;
    private boolean sIsLoadingStatement = false;
    private Context mContext;
    private Notityhandler mHandler;
    private ProgressBar mProgressbar;
    private static final String SEPERATE = "|";
    private IStatisticsBinder mBinder = null;
    private ConnectionStateReceiver mConnectionStateReceiver = new ConnectionStateReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        if (MmsApp.mTransparent) {
            setTheme(R.style.TabActivityTheme);
        } else if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkTheme) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        Intent intent = getIntent();
        String category = intent.getStringExtra("name");
        mCategoryid = intent.getIntExtra("category_ID", 0);
        mServerCount = intent.getIntExtra("count", 0);
        Log.d("net1", "category_id:" + mCategoryid);
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setCustomView(
                R.layout.gn_recommond_information_custom_title);
        getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        mTitleActionBar = getActionBar().getCustomView();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end

        mCategory = (TextView) mTitleActionBar.findViewById(R.id.category);
        mCategory.setText(category);

        mCategoryBackSymble = (ImageView) mTitleActionBar
                .findViewById(R.id.symble);
        mCategoryBackSymble.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*mIconChangeKey = (ImageView) mTitleActionBar
                .findViewById(R.id.try_luck);
        mIconChangeKey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("newResortFuction",
                        "onClick: " + System.currentTimeMillis());
                try {
                    mIconChangeKey.setClickable(false);
                    mIconChangeKey.setImageResource(R.drawable.gn_try_luck);
                    resort();
                } catch (Exception e) {
                    Log.d(TAG, " onClick()");
                    e.getCause();
                    e.printStackTrace();
                }

            }
        });*/
        setContentView(R.layout.gn_recommend_information);
        mNoNet = (View) findViewById(R.id.no_net);
        mNoNetWorks = (ImageView) findViewById(R.id.no_networks);
         // Aurora liugj 2013-11-16 modified for bug-820 start
        mListview = (AuroraListView) getListView();
         // Aurora liugj 2013-11-16 modified for bug-820 end

        mFoot = ((LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.gn_recom_information_foot, null, false);
        mListview.addFooterView(mFoot);
        mMoreTextButton = (View) mFoot.findViewById(R.id.loadMore);

        mMoreTextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "more click");
                try {
                    addMoredata();
                } catch (Exception e) {
                    Log.d(TAG, " onClick()");
                    e.getCause();
                    e.printStackTrace();
                }
            }
        });
        mProgressbar = (ProgressBar) mFoot.findViewById(R.id.progressbar);
        mLoading = (View) mFoot.findViewById(R.id.loading);
        mLoading.setClickable(false);
       /* if (MmsApp.mTransparent) {
            mListview.setDivider(this.getResources().getDrawable(
                    android.R.drawable.divider_horizontal_dark));
            mProgressbar.setProgressDrawable(this.getResources().getDrawable(
                    R.drawable.gn_progressbar_black));
            mNoNetWorks.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.gn_no_networks_black));
        } else if (MmsApp.mLightTheme) {
            mListview.setDivider(this.getResources().getDrawable(
                    R.drawable.gn_mms_online_divider));
            mProgressbar.setProgressDrawable(this.getResources().getDrawable(
                    R.drawable.gn_progressbar_white));
            mNoNetWorks.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.gn_no_networks_white));
            mMoreTextButton.setBackgroundDrawable(this.getResources()
                    .getDrawable(R.drawable.gn_more_text_background_light));
        } else if (MmsApp.mDarkTheme) {
            // mIconChangeKey.setImageResource(R.drawable.gn_try_luck);
            mListview.setDivider(this.getResources().getDrawable(
                    android.R.drawable.divider_horizontal_dark));
            mProgressbar.setProgressDrawable(this.getResources().getDrawable(
                    R.drawable.gn_progressbar_black));
            mNoNetWorks.setImageDrawable(this.getResources().getDrawable(
                    R.drawable.gn_no_networks_black));
            // moretext.setBackgroundColor(this.getResources().getColor(android.R.color.background_dark));
        }*/
        mListAdapter = new MyCursorAdapter(this, mCategoryid,
                R.layout.gn_recommend_information_item, R.id.information_cotent);
        mListview.setAdapter(mListAdapter);
        mListview.setOnCreateContextMenuListener(this);

        mListview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                LogUtils.log(TAG, LogUtils.getThreadName());
                LogUtils.log(TAG, LogUtils.getThreadName() + "position  ???"
                        + position + "parent.getCount" + parent.getCount());
                // if (position != parent.getCount() - 1) {
                if (position < mListAdapter.getCount()) {
                    view.showContextMenu();
                }
            }
        });
        mHandler = new Notityhandler();
        mNetManager = new NetTransmitManager(this, mHandler);
        mNetManager.setDownloadListener(this);

        getLoaderManager().initLoader(0, null, this);
        mBinder = new IStatisticsBinder(mContext);
    }

    @Override
    protected void onResume() {
        LogUtils.log(TAG, LogUtils.getThreadName());
        super.onResume();
        if (mServerCount == 0) {
            mHandler.sendEmptyMessage(MMS_ONLINE_NO_INFORMATION);

        }
        isInformationEmpty(mCategoryid, false);
        if (isNeedDownloadStatement()) {
            mNetManager.applyStatement();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mBinder != null) {
            mBinder.stop();
        }
        super.onDestroy();
    }

    @Override
    public void onLoadStateChanged(boolean state) {
        sIsLoadingStatement = state;
    }

    @Override
    public void onColumnStateChanged(boolean state) {
    }

    public void addMoredata() {
        LogUtils.log(TAG, LogUtils.getThreadName());
        isInformationEmpty(mCategoryid, true);
    }

    public void resort() {
        LogUtils.log(TAG, LogUtils.getThreadName());
        mListAdapter.resort();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        CursorLoader mCursorLoader = new CursorLoader(this,
                MyContentProvider.INFORMATION_URI, null,
                InformationColumns.CATEGORY_ID + " = ?",
                new String[] { String.valueOf(mCategoryid) }, null);

        mCursorLoader.setUpdateThrottle(50);
        return mCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        mListAdapter.swapCursor(arg1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        LogUtils.log(TAG, LogUtils.getThreadName());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LogUtils.log(TAG, LogUtils.getThreadName() + "change ");
        }
        if (sIsLoading) {
            mHandler.sendEmptyMessage(MMS_ONLINE_DOWNLOADING);
            // isLoading = false;
        }
        super.onConfigurationChanged(newConfig);
    }

    public void applyData(int id) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        NETWORK_TPYE state = OnlineUtils.checkNetworkState(mContext);
        if (NETWORK_TPYE.NOT_CONNECTED != state) {
            /*mIconChangeKey.setClickable(false);
            mIconChangeKey.setImageResource(R.drawable.gn_try_luck);*/
            setRefreshMenuState(false);
            
            mNetManager.applyInformationService(id, mMaxMsgId, mMinMsgId,
                    mIsTableEmpty);
            mBinder.writeStatistisMessage("C" + mCategoryid);
        } else {
            if (mInforamtionCount == 0) {
                mHandler.sendEmptyMessage(MMS_ONLINE_NO_NET);
            }
            OnlineUtils.showWifiAlert(mContext);
            mBinder.writeStatistisMessage("B" + mCategoryid);
            mConnectionStateReceiver.registerConnectionStateListener();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = (int) info.position;
        LogUtils.log(TAG, LogUtils.getThreadName() + "id ??????" + position
                + "mListAdapter.getCount() " + mListAdapter.getCount());
        if (position < mListAdapter.getCount()) {
            menu.setHeaderTitle(mListAdapter.getItem(position).toString());
            menu.add(0, MENU_FORWARD, 0, R.string.quick_text_menu_forward);
            menu.add(0, MENU_CPOY, 0, R.string.gn_copy_text);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int position = info.position;
        String mText = mListAdapter.getItem(position).toString();
        switch (item.getItemId()) {
        case MENU_FORWARD:
            Intent intent = new Intent(this, ComposeMessageActivity.class);
            intent.putExtra("sms_body", mText);
            startActivity(intent);
            mBinder.writeStatistisMessage("D" + mCategoryid + SEPERATE
                    + mListAdapter.getMsgId(position));
            break;
        case MENU_CPOY:
            try {
                ClipboardManager cm = (ClipboardManager) this
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", mText);
                cm.setPrimaryClip(clip);
                mBinder.writeStatistisMessage("E" + mCategoryid + SEPERATE
                        + mListAdapter.getMsgId(position));
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        default:
            break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.gn_mms_online_statement, menu);
        mMenuRefresh = menu.findItem(R.id.refresh_menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        switch (item.getItemId()) {
        case R.id.statement:
            SharedPreferences Data = mContext.getSharedPreferences("data",
                    Context.MODE_WORLD_WRITEABLE);
            String statement = Data.getString("statement",
                    getString(R.string.gn_statement_content));
            LogUtils.log(TAG, LogUtils.getThreadName() + "statement"
                    + statement);
            LogUtils.log(TAG, LogUtils.getThreadName()
                    + getString(R.string.gn_statement_content));
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
            //Gionee <zhouyj> <2013-06-13> modify for CR00822081 begin
            builder.setTitle(R.string.gn_statement);
            //Gionee <zhouyj> <2013-06-13> modify for CR00822081 end
            builder.setMessage(statement);
            builder.create().show();
            break;
            
        case R.id.refresh_menu:
            Log.d("newResortFuction",
                    "onClick: " + System.currentTimeMillis());
            try {
                /*mIconChangeKey.setClickable(false);
                mIconChangeKey.setImageResource(R.drawable.gn_try_luck);*/
                setRefreshMenuState(false);
                resort();
            } catch (Exception e) {
                Log.d(TAG, " onClick()");
                e.getCause();
                e.printStackTrace();
            }
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isNeedDownloadStatement() {
        LogUtils.log(TAG, LogUtils.getThreadName());
        Long currentTime = System.currentTimeMillis();
        SharedPreferences Data = mContext.getSharedPreferences("data",
                Context.MODE_WORLD_WRITEABLE);
        String statement = Data.getString("statement",
                getString(R.string.gn_statement_content));
        long oldloadtime = Data.getLong("loadStatementTime", currentTime);
        if (sIsLoadingStatement) {
            return false;
        } else if (statement == null || statement.length() <= 0) {
            return true;
        } else if (currentTime - oldloadtime >= MMS_ONLINE_DOWNLOAD_STATEMENT_INTERVAL) {
            return true;
        } else {
            return false;
        }
    }

    public void isInformationEmpty(final int id, final Boolean ismustload) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        final Runnable checkInformation = new Runnable() {
            public void run() {
                LogUtils.log(TAG, LogUtils.getThreadName());

                final ContentResolver cr = mContext.getContentResolver();
                if (cr != null) {
                    // gionee zhouyj 2012-12-10 modify for CR00739014 start
                    Cursor cur = cr.query(MyContentProvider.INFORMATION_URI,
                            null, InformationColumns.CATEGORY_ID + " = ?",
                            new String[] { String.valueOf(id) },
                            " msg_id DESC ");
                    try {
                        if (cur != null && cur.getCount() > 0) {
                            Log.d("MMS_ONLINE",
                                    "cur.getCount( : " + cur.getCount());
                            mIsTableEmpty = false;
                            mInforamtionCount = cur.getCount();
                            LogUtils.log(TAG, LogUtils.getThreadName()
                                    + "mInforamtionCount" + mInforamtionCount);
                            cur.moveToFirst();
                            mMaxMsgId = cur
                                    .getInt(cur.getColumnIndex("msg_id"));
                            cur.moveToLast();
                            mMinMsgId = cur
                                    .getInt(cur.getColumnIndex("msg_id"));
                        } else {
                            mIsTableEmpty = true;
                            mInforamtionCount = 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mIsTableEmpty = true;
                        mInforamtionCount = 0;
                    } finally {
                        if ((null != cur) && !(cur.isClosed())) {
                            cur.close();
                        }
                    }
                    // gionee zhouyj 2012-12-10 modify for CR00739014 end
                } else {
                    Log.d("net1", "can not find the db file");
                    mIsTableEmpty = true;
                    mInforamtionCount = 0;
                }
                if (ismustload) {
                    mHandler.sendEmptyMessage(MMS_ONLINE_LOAD_MORE);
                } else {
                    mHandler.sendEmptyMessage(MMS_ONLINE_CHECK_DONE);
                }
            }
        };
        new Thread(checkInformation).start();
    }

    class ConnectionStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.log(TAG, LogUtils.getThreadName()
                    + "listening net change ok jiajia" + action);
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager cm = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    LogUtils.log(TAG, LogUtils.getThreadName()
                            + "listening net change ok jiajia"
                            + "mInforamtionCount" + mInforamtionCount);
                    if (mInforamtionCount == 0) {
                        mHandler.sendEmptyMessage(MMS_ONLINE_ONLOADING);
                        mNetManager.applyInformationService(mCategoryid,
                                mMaxMsgId, mMinMsgId, mIsTableEmpty);
                    } else {
                        mHandler.sendEmptyMessage(MMS_ONLINE_LOAD_MORE);
                    }
                    mConnectionStateReceiver
                            .unregisterConnectionStateListener();
                }
            }
        }

        public void registerConnectionStateListener() {
            LogUtils.log(TAG, LogUtils.getThreadName()
                    + "listening net change jiajia");
            IntentFilter intentFilter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            // intentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
            if (mConnectionStateReceiver != null) {
                mContext.registerReceiver(mConnectionStateReceiver,
                        intentFilter);
            }
        }

        public void unregisterConnectionStateListener() {
            if (mConnectionStateReceiver != null) {
                LogUtils.log(TAG, LogUtils.getThreadName()
                        + "do not listen net change jiajia");
                mContext.unregisterReceiver(mConnectionStateReceiver);
            }
        }
    }

    class Notityhandler extends Handler {
        public Notityhandler(Looper looper) {
            super(looper);
        }

        public Notityhandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            LogUtils.log(TAG, LogUtils.getThreadName());
            super.handleMessage(msg);
            LogUtils.log(TAG, LogUtils.getThreadName() + "msg.what" + msg.what);
            switch (msg.what) {

            case MMS_ONLINE_NO_INFORMATION:
                /*mIconChangeKey.setClickable(false);
                mIconChangeKey.setImageResource(R.drawable.gn_try_luck);*/
                setRefreshMenuState(false);
                
                mNoNet.setVisibility(View.GONE);
                mListview.setVisibility(View.VISIBLE);
                Toast.makeText(mContext,
                        getResources().getString(R.string.no_information),
                        Toast.LENGTH_SHORT).show();
                break;

            case MMS_ONLINE_NO_MORE_INFORMATION:
                mNoNet.setVisibility(View.GONE);
                mListview.setVisibility(View.VISIBLE);
                /*if (mInforamtionCount > 1) {
                    mIconChangeKey.setClickable(true);
                    mIconChangeKey
                            .setImageResource(R.drawable.gn_try_luck_blue);
                }*/
                setRefreshMenuState(mInforamtionCount > 1 ? true : false);
                
                mLoading.setVisibility(View.GONE);
                mMoreTextButton.setVisibility(View.GONE);
                sIsLoading = false;
                Toast.makeText(mContext,
                        getResources().getString(R.string.no_more_information),
                        Toast.LENGTH_SHORT).show();
                break;
            case MMS_ONLINE_DISPLAY_MORE:
                mNoNet.setVisibility(View.GONE);
                mListview.setVisibility(View.VISIBLE);
                /*if (mInforamtionCount > 1) {
                    mIconChangeKey.setClickable(true);
                    mIconChangeKey
                            .setImageResource(R.drawable.gn_try_luck_blue);
                } else {
                    mIconChangeKey.setClickable(false);
                    mIconChangeKey.setImageResource(R.drawable.gn_try_luck);
                }*/
                setRefreshMenuState(mInforamtionCount > 1 ? true : false);
                
                mLoading.setVisibility(View.GONE);
                mMoreTextButton.setVisibility(View.VISIBLE);
                sIsLoading = false;
                if (msg.obj != null) {
                    ArrayList<Map<Integer, String>> list = (ArrayList<Map<Integer, String>>) msg.obj;
                    mListAdapter.mTexts = new String[list.size()];
                    mListAdapter.mMsgIds = new int[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        Map<Integer, String> map = list.get(i);
                        Iterator it = map.keySet().iterator();
                        while (it.hasNext()) {
                            Integer key = (Integer) it.next();
                            mListAdapter.mTexts[i] = (String) map.get(key);
                            mListAdapter.mMsgIds[i] = key;
                        }
                    }
                }
                boolean visiable = mListAdapter.getCount() != mServerCount;
                mMoreTextButton.setVisibility(visiable ? View.VISIBLE
                        : View.GONE);
                mListAdapter.notifyDataSetChanged();
                break;
            case MMS_ONLINE_DISPLAY_LOADING_FAILD:
                mNoNet.setVisibility(View.GONE);
                mListview.setVisibility(View.VISIBLE);
                /*if (mInforamtionCount > 1) {
                    mIconChangeKey.setClickable(true);
                    mIconChangeKey
                            .setImageResource(R.drawable.gn_try_luck_blue);
                    mLoading.setVisibility(View.GONE);
                } else {
                    mIconChangeKey.setClickable(false);
                    mIconChangeKey.setImageResource(R.drawable.gn_try_luck);
                }*/
                setRefreshMenuState(mInforamtionCount > 1 ? true : false);

                sIsLoading = false;
                Toast.makeText(mContext,
                        getResources().getString(R.string.download_failed),
                        Toast.LENGTH_SHORT).show();
                break;
            case MMS_ONLINE_NO_NET:
                mNoNet.setVisibility(View.VISIBLE);
                mListview.setVisibility(View.INVISIBLE);
                sIsLoading = false;
                break;
            case MMS_ONLINE_REFRESH_UNABLE:
                /*mIconChangeKey.setClickable(false);
                mIconChangeKey.setImageResource(R.drawable.gn_try_luck);*/
                setRefreshMenuState(false);
                break;
            case MMS_ONLINE_ONLOADING:
                mNoNet.setVisibility(View.GONE);
                mListview.setVisibility(View.VISIBLE);
                /*mIconChangeKey.setClickable(false);
                mIconChangeKey.setImageResource(R.drawable.gn_try_luck);*/
                setRefreshMenuState(false);
                
                Toast.makeText(mContext,
                        getResources().getString(R.string.on_loadding),
                        Toast.LENGTH_SHORT).show();
                break;
            case MMS_ONLINE_CHECK_DONE:
                if (mServerCount != 0 && mIsTableEmpty) {
                    /*mIconChangeKey.setClickable(false);
                    mIconChangeKey.setImageResource(R.drawable.gn_try_luck);*/
                    setRefreshMenuState(false);
                    
                    applyData(mCategoryid);
                } else if (!mIsTableEmpty) {
                    mBinder.writeStatistisMessage("B" + mCategoryid);
                }
                break;
            case MMS_ONLINE_LOAD_MORE:
                NETWORK_TPYE state = OnlineUtils.checkNetworkState(mContext);
                if (NETWORK_TPYE.NOT_CONNECTED != state) {
                    mNoNet.setVisibility(View.GONE);
                    mListview.setVisibility(View.VISIBLE);
                    /*mIconChangeKey.setClickable(false);
                    mIconChangeKey.setImageResource(R.drawable.gn_try_luck);*/
                    setRefreshMenuState(false);
                    
                    mMoreTextButton.setVisibility(View.GONE);
                    mLoading.setVisibility(View.VISIBLE);
                    mNetManager.applyInformationService(mCategoryid, mMaxMsgId,
                            mMinMsgId, mIsTableEmpty);
                    sIsLoading = true;
                    mBinder.writeStatistisMessage("C" + mCategoryid);
                } else {
                    if (mInforamtionCount == 0) {
                        mHandler.sendEmptyMessage(MMS_ONLINE_NO_NET);
                    }
                    OnlineUtils.showWifiAlert(mContext);
                    mConnectionStateReceiver.registerConnectionStateListener();
                }
                break;
            case MMS_ONLINE_REQUEST_TIME_OUT:
                if (mInforamtionCount > 1) {
                    /*mIconChangeKey.setClickable(true);
                    mIconChangeKey
                            .setImageResource(R.drawable.gn_try_luck_blue);*/
                    setRefreshMenuState(true);
                    
                    mLoading.setVisibility(View.GONE);
                    mMoreTextButton.setVisibility(View.VISIBLE);
                }
                sIsLoading = false;
                Toast.makeText(
                        mContext,
                        getResources().getString(
                                R.string.request_timed_out_mms_online),
                        Toast.LENGTH_SHORT).show();
                break;
            case MMS_ONLINE_DOWNLOADING:
                /*mIconChangeKey.setClickable(false);
                mIconChangeKey.setImageResource(R.drawable.gn_try_luck);*/
                setRefreshMenuState(false);
                
                mMoreTextButton.setVisibility(View.GONE);
                mLoading.setVisibility(View.VISIBLE);
                break;
            default:
                break;
            }
        }
    }

    class MyCursorAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private int mLayout;
        private int mViewId;
        private String[] mTexts;
        private int[] mMsgIds;
        private SortDataManager mData;

        private Runnable mResort = new Runnable() {
            @Override
            public void run() {
                mData.resort();
                mHandler.sendEmptyMessage(MMS_ONLINE_DISPLAY_MORE);
            }
        };

        public MyCursorAdapter(Context context, int categoryId, int layout,
                int to) {
            this.mData = new SortDataManager(context, categoryId);
            this.mInflater = LayoutInflater.from(context);
            this.mLayout = layout;
            this.mViewId = to;
        }

        public void swapCursor(final Cursor cursor) {
            LogUtils.log(TAG, LogUtils.getThreadName());
            //Gionee <zhouyj> <2013-05-23> modify for CR00818556 begin
            if (cursor != null && !cursor.isClosed()) {
            //Gionee <zhouyj> <2013-05-23> modify for CR00818556 end
                Runnable swap = new Runnable() {
                    public void run() {
                        try {
                            int count = cursor.getCount();
                            mInforamtionCount = count;
                            cursor.moveToFirst();

                            ArrayList<Map<Integer, String>> list = new ArrayList<Map<Integer, String>>();
                            int columnId = cursor
                                    .getColumnIndex(InformationColumns.MSG);
                            int msgId = cursor
                                    .getColumnIndex(InformationColumns.MSG_ID);
                            for (int i = 0; i < count; i++) {
                                Map<Integer, String> map = new HashMap<Integer, String>();
                                map.put(cursor.getInt(msgId),
                                        cursor.getString(columnId));
                                list.add(map);
                                cursor.moveToNext();
                            }
                            if (mData.isInit()) {
                                mData.appendArray(count);
                            } else {
                                mData.initArray(count);
                            }
                            mIsTableEmpty = !(count > 0);
                            if (!mIsTableEmpty) {
                                Message msg = mHandler.obtainMessage(
                                        MMS_ONLINE_DISPLAY_MORE, list);
                                mHandler.sendMessage(msg);
                            }
                        } catch (SQLException ex) {
                        }
                    }
                };
                startThread(swap);
            }
        }

        @Override
        public int getCount() {
            if (mTexts != null) {
                return mTexts.length;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mTexts != null) {
                return mTexts[mData.getItem(position)];
            }
            return "";
        }

        public int getMsgId(int pos) {
            if (mMsgIds != null && pos < mMsgIds.length) {
                return mMsgIds[mData.getItem(pos)];
            }
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(mLayout, null);
            }
            TextView tv = (TextView) convertView.findViewById(mViewId);
            tv.setText((String) getItem(position));
            return convertView;
        }

        public void resort() {
            LogUtils.log(TAG, LogUtils.getThreadName());
            startThread(mResort);
        }

        private void startThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            // thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
    }

    class SortDataManager {

        private static final String FOLDER_NAME = "resort";

        private int mCategoryId;
        private String mPath;
        private Integer[] mSortArray;

        private SortDataManager(Context context, int categoryId) {
            this.mPath = "/data/data/" + context.getPackageName()
                    + File.separator + FOLDER_NAME + File.separator;
            this.mCategoryId = categoryId;
        }

        int getItem(int index) {
            if (mSortArray == null || index >= mSortArray.length) {
                return index;
            }
            return mSortArray[index];
        }

        boolean isInit() {
            return mSortArray != null;
        }

        void initArray(int count) {
            mSortArray = readSortArray(mCategoryId, count);
        }

        void writeArray(Integer[] array) {
            mSortArray = writeSortArray(mCategoryId, array);
        }

        void appendArray(int length) {
            mSortArray = appendSortArray(mCategoryId, length);
        }

        void resort() {
            Log.d("newResortFuction", "new sort count: " + mSortArray.length);
            Integer[] temp = new Integer[mSortArray.length];
            System.arraycopy(mSortArray, 0, temp, 0, mSortArray.length);
            List<Integer> list = Arrays.asList(temp);
            Collections.shuffle(list);
            mSortArray = temp;
            writeSortArray(mCategoryId, temp);
        }

        private Integer[] appendSortArray(int categoryId, int length) {
            Integer[] array = new Integer[length];
            int start = 0;
            if (mSortArray != null && length > mSortArray.length) {
                System.arraycopy(mSortArray, 0, array, 0, mSortArray.length);
                start = mSortArray.length;
            }
            for (int i = start; i < length; i++) {
                array[i] = i;
            }
            return writeFile(categoryId, array);
        }

        private Integer[] writeSortArray(int categoryId, Integer[] array) {
            return writeFile(categoryId, array);
        }

        private Integer[] writeFile(int categoryId, Integer[] array) {
            File file = getFile(categoryId);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = null;
            DataOutputStream dos = null;
            try {
                new File(mPath).mkdirs();
                file.createNewFile();
                fos = new FileOutputStream(file);
                dos = new DataOutputStream(fos);
                for (int i : array) {
                    dos.writeInt(i);
                }
                dos.flush();
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return array;
        }

        private File getFile(int categoryId) {
            File file = new File(mPath + categoryId);
            return file;
        }

        private Integer[] readSortArray(int categoryId, int count) {
            File file = getFile(categoryId);
            if (file.exists()) {
                int length = (int) file.length();
                if (length == count * 4) {

                    FileInputStream fis = null;
                    DataInputStream dis = null;
                    try {
                        fis = new FileInputStream(file);
                        dis = new DataInputStream(fis);
                        Integer[] array = new Integer[count];

                        for (int i = 0; i < count; i++) {
                            array[i] = dis.readInt();
                        }
                        return array;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (dis != null) {
                            try {
                                dis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    file.delete();
                }
            }
            Integer[] array = new Integer[count];
            for (int i = 0; i < count; i++) {
                array[i] = i;
            }
            return array;
        }
    }
    
    private void setRefreshMenuState(boolean enable) {
        if (mMenuRefresh == null) {
            return;
        }
        if (enable) {
            mMenuRefresh.setEnabled(true);
            /*mMenuRefresh.setIcon(R.drawable.gn_try_luck_blue);*/
        } else {
            mMenuRefresh.setEnabled(false);
            /*mMenuRefresh.setIcon(R.drawable.gn_try_luck);*/
        }
    }
}