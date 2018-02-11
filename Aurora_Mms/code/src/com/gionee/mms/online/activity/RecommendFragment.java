package com.gionee.mms.online.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.gionee.mms.online.IStatisticsBinder;
import com.gionee.mms.online.LogUtils;
import com.gionee.mms.online.OnlineUtils;
import com.gionee.mms.online.OnlineUtils.NETWORK_TPYE;
import com.gionee.mms.online.data.Category;
import com.gionee.mms.online.data.MyContentProvider;
import com.gionee.mms.online.data.RecommendColumns;
import com.gionee.mms.online.transmisson.NetTransmitManager;
import com.gionee.mms.online.transmisson.NetTransmitManager.DownloadListener;
import com.gionee.mms.ui.TabActivity;
import com.gionee.mms.ui.TabActivity.ViewPagerVisibilityListener;
//Gionee <zhouyj> <2013-04-11> modify for CR00796166 start
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
//Gionee <zhouyj> <2013-04-11> modify for CR00796166 end

public class RecommendFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, DownloadListener , ViewPagerVisibilityListener{
    private static final String TAG = "RecommendFragment";
    private Context mContext;
    private ListCursorAdapter mAdapter;
    private NetTransmitManager mNetManager = null;
    private ConnectionStateReceiver mConnectionStateReceiver = new ConnectionStateReceiver();
    private boolean mIsEmpty = true;
    private boolean mIsLoading = false;
    private QueryHandler mQueryHandler = null;
    private IStatisticsBinder mBinder = null;

    private static int QUERY_FIRST_TOKEN = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mNetManager = new NetTransmitManager(mContext, null);
        mNetManager.setDownloadListener(this);
        mQueryHandler = new QueryHandler(mContext.getContentResolver());
        OnlineUtils.startPublicNetworkModule(mContext);
        mBinder = new IStatisticsBinder(mContext);
    }

    @Override
    public void onLoadStateChanged(boolean state) {
        mIsLoading = state;
    }

    @Override
    public void onColumnStateChanged(boolean state) {
        mIsEmpty = state;
    }

    @Override
    public void onDestroy() {
        LogUtils.log(TAG, LogUtils.getThreadName());
        super.onDestroy();
        if (mBinder != null) {
            mBinder.stop();
        }
        OnlineUtils.stopPublicNetworkModule(mContext);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        super.onActivityCreated(savedInstanceState);

        mAdapter = new ListCursorAdapter(mContext, null,
                R.layout.gn_recommend_column_item);
        setListAdapter(mAdapter);
        // setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        View fragmentView = inflater.inflate(R.layout.gn_recom_list, container,
                false);
        return fragmentView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        LogUtils.log(TAG, LogUtils.getThreadName() + "position =" + position
                + ",id =" + id);
        super.onListItemClick(l, v, position, id);

        Cursor cur = mAdapter.getCursor();
        Intent intent = new Intent(getActivity(), RecommendInformation.class);
        intent.putExtra("name", cur.getString(cur.getColumnIndex("name")));
        intent.putExtra("category_ID", cur.getInt(cur.getColumnIndex("cat_id")));
        intent.putExtra("count", cur.getInt(cur.getColumnIndex("count")));
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        startQueryTap(new Category(cur));
    }

    /** Override LoaderManager.LoaderCallbacks<Cursor> start **/
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        CursorLoader mCursorLoader = new CursorLoader(mContext,
                MyContentProvider.COLUMN_URI, new String[] {
                        RecommendColumns._ID, RecommendColumns.CAT_ID,
                        RecommendColumns.NAME, RecommendColumns.COUNT,
                        RecommendColumns.TAP }, null, null, null);
        mCursorLoader.setUpdateThrottle(500);
        return mCursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        mAdapter.swapCursor(arg1);
    }

    public void onLoaderReset(Loader<Cursor> arg0) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        mAdapter.swapCursor(null);
    }

    /** Override LoaderManager.LoaderCallbacks<Cursor> end **/

    public void applyData() {
        LogUtils.log(TAG, LogUtils.getThreadName());
        NETWORK_TPYE state = OnlineUtils.checkNetworkState(mContext);
        if (NETWORK_TPYE.NOT_CONNECTED != state) {
            //Gionee <zhouyj> <2013-06-08> modify for begin for super theme
            Toast.makeText(TabActivity.sTabActivity,
                    getResources().getString(R.string.on_loadding),
                    Toast.LENGTH_SHORT).show();
            //Gionee <zhouyj> <2013-06-08> modify for end
            mNetManager.applyColumnService();
        } else {
            OnlineUtils.showWifiAlert(mContext);
            mConnectionStateReceiver.registerConnectionStateListener();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mBinder.writeStatistisMessage("A");
            }
        }, 1000);
    }

    private MenuItem mMenuBatchOperationItem;
    private MenuItem mMenuChangeEncryptionItem;
    private MenuItem mMenuCancelAllFavoriteItem;
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!MmsApp.sIsExchangeExist) {
            menu.removeItem(R.id.gn_action_exchange);
        }
        if (!MmsApp.isOpenApi()) {
            menu.removeItem(R.id.gn_action_doctoran);
        }
        if (!TabActivity.checkMsgImportExportSms()) {
            menu.removeItem(R.id.gn_action_in_out);
        }
        mMenuCancelAllFavoriteItem = menu.findItem(R.id.gn_action_cancel_all_favorite);
        if (mMenuCancelAllFavoriteItem != null) {
            mMenuCancelAllFavoriteItem.setVisible(false);
        }
        mMenuChangeEncryptionItem = menu.findItem(R.id.gn_action_encryption);
        if (mMenuChangeEncryptionItem != null) {
            mMenuChangeEncryptionItem.setVisible(false);
        }
        mMenuBatchOperationItem = menu.findItem(R.id.gn_action_batch_operation);
        if (mMenuBatchOperationItem != null) {
            mMenuBatchOperationItem.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    public boolean isNeedDownload() {
        LogUtils.log(TAG, LogUtils.getThreadName());
        Date currentData = new Date();
        SimpleDateFormat formattime = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatday = new SimpleDateFormat("yyyy-MM-dd");
        String datatime = formattime.format(currentData);
        String dataday = formatday.format(currentData);

        SharedPreferences Data = mContext.getSharedPreferences("data",
                Context.MODE_WORLD_WRITEABLE);
        String olddatatime = Data.getString("datatime", datatime);
        String olddataday = Data.getString("dataday", dataday);
        LogUtils.log(TAG, "isempty" + mIsEmpty);
        if (mIsLoading) {
            return false;
        } else if (mIsEmpty) {
            LogUtils.log(TAG, "isempty" + mIsEmpty);
            return true;
        } else {
            LogUtils.log(TAG, "isempty11" + mIsEmpty);
            if (datatime.equals(olddatatime)) {
                return true;
            } else if (!dataday.equals(olddataday)) {
                LogUtils.log(TAG, "isempty22" + mIsEmpty);
                return true;
            } else {
                LogUtils.log(TAG, "isempty33" + mIsEmpty);
                return false;
            }
        }
    }

    class ConnectionStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            LogUtils.log(TAG, LogUtils.getThreadName()
                    + "listening net change ok jiajia" + action);
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager cm = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    LogUtils.log(TAG, LogUtils.getThreadName()
                            + "listening net change ok jiajia");
                    mNetManager.applyColumnService();
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
            IntentFilter intentFilter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            // intentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
            if (mConnectionStateReceiver != null) {
                LogUtils.log(TAG, LogUtils.getThreadName()
                        + "do not listen net change jiajia");
                mContext.unregisterReceiver(mConnectionStateReceiver);
            }
        }
    }

    private void startQueryTap(Category c) {
        mQueryHandler.startQuery(QUERY_FIRST_TOKEN, c,
                MyContentProvider.COLUMN_URI, new String[] {
                        RecommendColumns._ID, RecommendColumns.CAT_ID,
                        RecommendColumns.TAP },
                RecommendColumns._ID + "=" + c.getId(), null, null);
    }

    private class ListCursorAdapter extends CursorAdapter {

        private int mLayout;
        private Context mContext;

        public ListCursorAdapter(Context context, Cursor c, int res) {
            super(context, c);
            // TODO Auto-generated constructor stub
            mLayout = res;
            mContext = context;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // TODO Auto-generated method stub
            if (cursor != null) {
                //Gionee <zhouyj> <2013-04-11> modify for CR00796166 start
                TextView catName = (TextView) view
                        .findViewById(R.id.column_name);
                String name = cursor.getString(cursor.getColumnIndex("name")) + " ";
                String count = String.format("(%s)",
                        cursor.getInt(cursor.getColumnIndex("count")));
                String cat = name + count;
                SpannableStringBuilder ssb = new SpannableStringBuilder(cat);
                ssb.setSpan(new TextAppearanceSpan(mContext, android.R.style.TextAppearance_Small), 
                        name.length(), cat.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                catName.setText(ssb);
                //Gionee <zhouyj> <2013-04-11> modify for CR00796166 end
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // TODO Auto-generated method stub
            return LayoutInflater.from(mContext)
                    .inflate(mLayout, parent, false);
        }
    }

    private class QueryHandler extends AsyncQueryHandler {

        private ContentResolver mContentResolver;

        public QueryHandler(ContentResolver cr) {
            super(cr);
            // TODO Auto-generated constructor stub
            mContentResolver = cr;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (QUERY_FIRST_TOKEN == token) {
                if (cursor != null && cursor.getCount() > 0) {
                    /** first click on the item **/
                    cursor.moveToFirst();
                    if (cursor.getInt(cursor
                            .getColumnIndex(RecommendColumns.TAP)) == 0) {
                        String where = "_id="
                                + cursor.getInt(cursor.getColumnIndex("_id"));
                        ContentValues value = new ContentValues(1);
                        value.put(RecommendColumns.TAP, 1);
                        mContentResolver.update(MyContentProvider.COLUMN_URI,
                                value, where, null);
                        mNetManager.postTapCategory(((Category) cookie)
                                .getCategoryId());
                    }
                }

                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        // TODO Auto-generated method stub
        if (visible == false) {
            return;
        }
        if(isNeedDownload()){
            applyData();
        }
    }
}
