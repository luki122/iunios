/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.notification;

import android.animation.LayoutTransition;
import android.app.INotificationManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.service.notification.NotificationListenerService;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.AuroraSettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Settings.NotificationAppListActivity;
import com.android.settings.UserSpinnerAdapter;
import com.android.settings.Utils;
import com.aurora.utils.Utils2Icon;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraTextView;

import static com.android.settings.notification.AuroraAppNotificationSettings.EXTRA_HAS_SETTINGS_INTENT;
import static com.android.settings.notification.AuroraAppNotificationSettings.EXTRA_SETTINGS_INTENT;

/**
 * Just a sectioned list of installed applications, nothing else to index
 **/
public class AuroraNotificationAppList extends AuroraSettingsPreferenceFragment
        implements OnItemSelectedListener {

    private static final String TAG = "AuroraNotificationAppList";
    private static final boolean DEBUG = true;// Log.isLoggable(TAG, Log.DEBUG);
    private boolean mIsEditMode = false;

    private static final String EMPTY_SUBTITLE = "";
    private static final String SECTION_BEFORE_A = "*";
    private static final String SECTION_AFTER_Z = "**";
    private static final Intent APP_NOTIFICATION_PREFS_CATEGORY_INTENT

            = new Intent(Intent.ACTION_MAIN)
            .addCategory(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES);

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            mAdapter.setNeedAnim(false);
            super.handleMessage(msg);
        }

    };
    private final ArrayMap<String, AppRow> mRows = new ArrayMap<String, AppRow>();
    private final ArrayList<AppRow> mSortedRows = new ArrayList<AppRow>();
    private final ArrayList<AppRow> mRows1 = new ArrayList<AppRow>();
    private final ArrayList<AppRow> mRows2 = new ArrayList<AppRow>();
    private final ArrayList<String> mSections = new ArrayList<String>();

    private Context mContext;
    private LayoutInflater mInflater;
    private NotificationAppAdapter mAdapter;
    private Signature[] mSystemSignature;
    private Parcelable mListViewState;
    private Backend mBackend = new Backend();
    private UserSpinnerAdapter mProfileSpinnerAdapter;
    private Spinner mSpinner;

    private PackageManager mPM;
    private UserManager mUM;
    private LauncherApps mLauncherApps;

    private TextView mEmptyView;
    private boolean mSecure;

    private static String mSelectAllStr;
    private static String mUnSelectAllStr;
    private AuroraActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        //StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        //       .detectActivityLeaks()
        //       .penaltyLog()
        //       .penaltyDeath()
        //       .build());
        mContext = getActivity();
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAdapter = new NotificationAppAdapter(mContext);
        mUM = UserManager.get(mContext);
        mPM = mContext.getPackageManager();
        mLauncherApps = (LauncherApps) mContext
                .getSystemService(Context.LAUNCHER_APPS_SERVICE);
        mSecure = new LockPatternUtils(getActivity()).isSecure();

        if (getActivity() instanceof AuroraActivity) {
            final AuroraActivity auroraActivity = (AuroraActivity) getActivity();
            mActionBar = auroraActivity.getAuroraActionBar();
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    setDefaultActionBar(auroraActivity);
                }
            }, 50);
            mSelectAllStr = mContext.getResources().getString(R.string.select_all);
            mUnSelectAllStr = mContext.getResources().getString(R.string.unselect_all);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_app_list, container,
                false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProfileSpinnerAdapter = Utils.createUserSpinnerAdapter(mUM, mContext);
        if (mProfileSpinnerAdapter != null) {
            mSpinner = (Spinner) getActivity().getLayoutInflater().inflate(
                    R.layout.spinner_view, null);
            mSpinner.setAdapter(mProfileSpinnerAdapter);
            mSpinner.setOnItemSelectedListener(this);
            //((ViewGroup) getListView().getParent()).addView(mSpinner, 0);
        }

        mEmptyView = (TextView) view.findViewById(android.R.id.empty);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        repositionScrollbar();
        getListView().setAdapter(mAdapter);
        //getListView().setOnScrollListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG)
            Log.d(TAG, "Saving listView state");
        mListViewState = getListView().onSaveInstanceState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListViewState = null; // you're dead to me
        mRows.clear();
        mSortedRows.clear();
        mRows1.clear();
        mRows2.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAppsList();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        UserHandle selectedUser = mProfileSpinnerAdapter.getUserHandle(position);
        if (selectedUser.getIdentifier() != UserHandle.myUserId()) {
            Intent intent = new Intent(getActivity(), NotificationAppListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivityAsUser(intent, selectedUser);
            //Go back to default selection, which is the first one; this makes
            //sure that pressing
            //the back button takes you into a consistent state
            mSpinner.setSelection(0);
        }
    }

    public void setEditMode(boolean flag) {
        mIsEditMode = flag;
    }

    public boolean getEditMode() {
        return mIsEditMode;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void setBackend(Backend backend) {
        mBackend = backend;
    }

    private void loadAppsList() {
        AsyncTask.execute(mCollectAppsRunnable);
    }

    private String getSection(CharSequence label) {
        if (label == null || label.length() == 0)
            return SECTION_BEFORE_A;
        final char c = Character.toUpperCase(label.charAt(0));
        if (c < 'A')
            return SECTION_BEFORE_A;
        if (c > 'Z')
            return SECTION_AFTER_Z;
        return Character.toString(c);
    }

    private void repositionScrollbar() {
        final int sbWidthPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, getListView().getScrollBarSize(),
                getResources().getDisplayMetrics());
        final View parent = (View) getView().getParent();
        final int eat = Math.min(sbWidthPx, parent.getPaddingEnd());
        if (eat <= 0)
            return;
        if (DEBUG)
            Log.d(TAG, String.format(
                    "Eating %dpx into %dpx padding for %dpx scroll, ld=%d",
                    eat, parent.getPaddingEnd(), sbWidthPx, getListView()
                            .getLayoutDirection()));
        parent.setPaddingRelative(parent.getPaddingStart(),
                parent.getPaddingTop(), parent.getPaddingEnd() - eat,
                parent.getPaddingBottom());
    }

    private static class ViewHolder {
        ViewGroup row;
        ImageView icon;
        TextView title;
        TextView subtitle;
        //View rowDivider;
        boolean changefirst;
    }

    private class NotificationAppAdapter extends ArrayAdapter<Row> implements
            SectionIndexer, StickyListHeadersAdapter {
        private boolean mCheckBoxEnable = false;
        private boolean mNeedAnim = false;
        public HashMap<Long, Integer> mCheckedItem = new HashMap<Long, Integer>();

        public NotificationAppAdapter(Context context) {
            super(context, 0, 0);
        }

        public void setCheckBoxEnable(boolean flag) {
            mCheckBoxEnable = flag;
        }

        public boolean getCheckBoxEnable() {
            return mCheckBoxEnable;
        }

        public void setNeedAnim(boolean flag) {
            mNeedAnim = flag;
        }

        public boolean getNeedAnim() {
            return mNeedAnim;
        }

        public void setCheckedItem(long itemId, int position) {
            if (mCheckedItem == null) {
                mCheckedItem = new HashMap<Long, Integer>();
            }

            mCheckedItem.put(itemId, position);
        }

        public HashMap<Long, Integer> getCheckedItem() {
            return mCheckedItem;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Row r = getItem(position);
            return r instanceof AppRow ? 1 : 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Row r = getItem(position);
            View v;
            if (convertView == null || (((ViewHolder) convertView.getTag()).changefirst != ((AppRow) r).first)) {
                v = newView(parent, r, position);
            } else {
                v = convertView;
            }
            bindView(v, r, position, false /* animate */);
            return v;
        }

        public View newView(ViewGroup parent, Row r, int position) {
            if (!(r instanceof AppRow)) {
                return mInflater.inflate(R.layout.notification_app_section,
                        parent, false);
            }
            final View v = mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null); //使用auroralistview布局
            RelativeLayout mainUi = (RelativeLayout) v.findViewById(com.aurora.R.id.aurora_listview_front);

            final View appView = mInflater.inflate(R.layout.notification_app, parent, false);//添加自定义的显示内容到auroralistview中
            mainUi.addView(appView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            ImageView iv = (ImageView) v.findViewById(com.aurora.R.id.aurora_listview_divider); //设置间隔线,无效
            iv.setVisibility(View.GONE);

            LinearLayout contentUi = (LinearLayout) v.findViewById(com.aurora.R.id.content); //设置点击效果
            AuroraListView.auroraGetAuroraStateListDrawableFromIndex(contentUi, position);

            final ViewHolder vh = new ViewHolder();
            vh.row = (ViewGroup) v;
            vh.row.setLayoutTransition(new LayoutTransition());
            vh.row.setLayoutTransition(new LayoutTransition());
            vh.icon = (ImageView) v.findViewById(android.R.id.icon);
            vh.title = (TextView) v.findViewById(android.R.id.title);
            vh.changefirst = ((AppRow) r).first;
            //vh.subtitle = (TextView) v.findViewById(android.R.id.text1);
            v.setTag(vh);
            return v;
        }

        private void enableLayoutTransitions(ViewGroup vg, boolean enabled) {
            if (enabled) {
                vg.getLayoutTransition().enableTransitionType(
                        LayoutTransition.APPEARING);
                vg.getLayoutTransition().enableTransitionType(
                        LayoutTransition.DISAPPEARING);
            } else {
                vg.getLayoutTransition().disableTransitionType(
                        LayoutTransition.APPEARING);
                vg.getLayoutTransition().disableTransitionType(
                        LayoutTransition.DISAPPEARING);
            }
        }

        public void bindView(final View view, Row r, int position, boolean animate) {
            if (!(r instanceof AppRow)) {
                // it's a section row
                final TextView tv = (TextView) view
                        .findViewById(android.R.id.title); //当显示的通知或不显示的通知为空时。可以在此处处理？
                tv.setText(r.section);
                return;
            }
            final int mPosition = position;
            final AppRow row = (AppRow) r;
            final ViewHolder vh = (ViewHolder) view.getTag();
            enableLayoutTransitions(vh.row, animate);

            AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
            RelativeLayout mainUi = (RelativeLayout) view.findViewById(com.aurora.R.id.aurora_listview_front);
            if (getCheckBoxEnable()) {
                if (getNeedAnim()) {
                    AuroraListView.auroraStartCheckBoxAppearingAnim(mainUi, checkBox);
                } else {
                    AuroraListView.auroraSetCheckBoxVisible(mainUi, checkBox, true);
                }
                checkBox.setChecked(row.isChecked);
            } else {
                if (checkBox != null) {
                    if (getNeedAnim()) {
                        AuroraListView.auroraStartCheckBoxDisappearingAnim(mainUi, checkBox);
                    } else {
                        AuroraListView.auroraSetCheckBoxVisible(mainUi, checkBox, false);
                    }

                }
            }
            if (getNeedAnim()) {
                mHandler.sendMessage(mHandler.obtainMessage());
            }

            vh.row.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!getEditMode()) { //普通模式下
                        mContext.startActivity(new Intent(
                                Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, row.pkg)
                                .putExtra(Settings.EXTRA_APP_UID, row.uid)
                                .putExtra(EXTRA_HAS_SETTINGS_INTENT,
                                        row.settingsIntent != null)
                                .putExtra(EXTRA_SETTINGS_INTENT, row.settingsIntent));
                    } else {              //编辑模式下
                        final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                        if (null != checkBox) {
                            boolean checked = checkBox.isChecked();
                            checkBox.auroraSetChecked(!checked, true);
                            row.isChecked = !checked;
                            if (!checked) {
                                mAdapter.setCheckedItem(getItemId(mPosition), mPosition);
                            } else {
                                mAdapter.getCheckedItem().remove(getItemId(mPosition));
                            }

                            if (null != getActivity()) {
                                //updateSelectedItemsView(mItemCount);
                            }
                        }

                    }
                }
            });

            vh.row.setOnLongClickListener(new View.OnLongClickListener() { //长按进入编辑模式

                @Override
                public boolean onLongClick(View v) {
                    if (getEditMode())
                        return false;
                    //TODO Auto-generated method stub
                    mAdapter.setCheckBoxEnable(true);
                    mAdapter.setNeedAnim(true);
                    setEditMode(true);
                    mActionBar.setShowBottomBarMenu(true);
                    mActionBar.showActionBarDashBoard();

                    //返回true 执行完长按事件后不加短按操作
                    return true;
                }
            });
            enableLayoutTransitions(vh.row, animate);
            vh.icon.setImageDrawable(row.icon);
            vh.title.setText(row.label);
            // showHeaderUi(view, row);
        }

        private void showHeaderUi(View view, AppRow row) {
            LinearLayout headerUi = (LinearLayout) view.findViewById(com.aurora.R.id.aurora_list_header);
            if (!row.first) {
                headerUi = null;
                return;
            }
            ViewGroup.LayoutParams params = headerUi.getLayoutParams();
            params.height = 50;

            AuroraTextView tv = new AuroraTextView(mContext);
            if (!row.banned) {
                tv.setText(R.string.lockscreen_show);
            } else {
                tv.setText(R.string.lockscreen_notshow);
            }
            int paddingLeft = 30;
            tv.setPadding(paddingLeft, 0, 0, 0);
            tv.setHeight(params.height);
            tv.setGravity(Gravity.BOTTOM);
            tv.setTextSize(14);
            tv.setTextColor(Color.parseColor("#49aef7"));
            if (headerUi != null) {
                headerUi.removeAllViews();
            }
            headerUi.setEnabled(false);
            headerUi.setClickable(false);
            headerUi.addView(tv);
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.aurora_notification_app_list_header, parent, false);
            }

            TextView labelText = (TextView) convertView.findViewById(R.id.labelText);
            labelText.setText(getLabelText(position));
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            String labelText = getLabelText(position);
            if (labelText == null || labelText.length() == 0 || labelText.equals("null")) {
                return 0;
            } else {
                return labelText.charAt(0);
            }
        }

        private String getLabelText(int position) {
            String labelText = "";

            if (((AppRow) mAdapter.getItem(position)).banned == true) {
                labelText = mContext.getResources().getString(R.string.lockscreen_notshow);
            } else {
                labelText = mContext.getResources().getString(R.string.lockscreen_show);
            }
            return labelText;
        }

        private String getSubtitle(AppRow row) {
            if (row.banned) {
                return mContext.getString(R.string.app_notification_row_banned);
            }
            if (!row.priority && !row.sensitive) {
                return EMPTY_SUBTITLE;
            }
            final String priString = mContext
                    .getString(R.string.app_notification_row_priority);
            final String senString = mContext
                    .getString(R.string.app_notification_row_sensitive);
            if (row.priority != row.sensitive) {
                return row.priority ? priString : senString;
            }
            return priString
                    + mContext.getString(R.string.summary_divider_text)
                    + senString;
        }

        @Override
        public Object[] getSections() {
            return mSections.toArray(new Object[mSections.size()]);
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            final String section = mSections.get(sectionIndex);
            final int n = getCount();
            for (int i = 0; i < n; i++) {
                final Row r = getItem(i);
                if (r.section.equals(section)) {
                    return i;
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            Row row = getItem(position);
            return mSections.indexOf(row.section);
        }
    }

    private static class Row {
        public String section;
    }

    public static class AppRow extends Row {
        public String pkg;
        public int uid;
        public Drawable icon;
        public CharSequence label;
        public Intent settingsIntent;
        public boolean banned;
        public boolean priority;
        public boolean sensitive;
        public boolean first; // first app in section
        public boolean isChecked;
        public String appNamePinYin;
    }

    private static final Comparator<AppRow> mRowComparator = new Comparator<AppRow>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(AppRow lhs, AppRow rhs) {
            return sCollator.compare(lhs.label, rhs.label);
        }
    };

    public static AppRow loadAppRow(PackageManager pm, ApplicationInfo app,
                                    Backend backend) {
        final AppRow row = new AppRow();
        row.pkg = app.packageName;
        row.uid = app.uid;
        try {
            row.label = app.loadLabel(pm);
        } catch (Throwable t) {
            Log.e(TAG, "Error loading application label for " + row.pkg, t);
            row.label = row.pkg;
        }
        row.icon = app.loadIcon(pm);
        row.banned = backend.getNotificationsBanned(row.pkg, row.uid);
        row.priority = backend.getHighPriority(row.pkg, row.uid);
        row.sensitive = backend.getSensitive(row.pkg, row.uid);
        row.appNamePinYin = Utils.getSpell(row.label.toString());
        return row;
    }

    public static List<ResolveInfo> queryNotificationConfigActivities(
            PackageManager pm) {
        if (DEBUG)
            Log.d(TAG, "APP_NOTIFICATION_PREFS_CATEGORY_INTENT is "
                    + APP_NOTIFICATION_PREFS_CATEGORY_INTENT);
        final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(
                APP_NOTIFICATION_PREFS_CATEGORY_INTENT, 0 // PackageManager.MATCH_DEFAULT_ONLY
        );
        return resolveInfos;
    }

    public static void collectConfigActivities(PackageManager pm,
                                               ArrayMap<String, AppRow> rows) {
        final List<ResolveInfo> resolveInfos = queryNotificationConfigActivities(pm);
        applyConfigActivities(pm, rows, resolveInfos);
    }

    public static void applyConfigActivities(PackageManager pm,
                                             ArrayMap<String, AppRow> rows, List<ResolveInfo> resolveInfos) {
        if (DEBUG)
            Log.d(TAG, "Found " + resolveInfos.size()
                    + " preference activities"
                    + (resolveInfos.size() == 0 ? " ;_;" : ""));
        for (ResolveInfo ri : resolveInfos) {
            final ActivityInfo activityInfo = ri.activityInfo;
            final ApplicationInfo appInfo = activityInfo.applicationInfo;
            final AppRow row = rows.get(appInfo.packageName);
            if (row == null) {
                Log.v(TAG, "Ignoring notification preference activity ("
                        + activityInfo.name + ") for unknown package "
                        + activityInfo.packageName);
                continue;
            }
            if (row.settingsIntent != null) {
                Log.v(TAG,
                        "Ignoring duplicate notification preference activity ("
                                + activityInfo.name + ") for package "
                                + activityInfo.packageName);
                continue;
            }
            row.settingsIntent = new Intent(
                    APP_NOTIFICATION_PREFS_CATEGORY_INTENT).setClassName(
                    activityInfo.packageName, activityInfo.name);
        }
    }

    private final Runnable mCollectAppsRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mRows) {
                final long start = SystemClock.uptimeMillis();
                if (DEBUG)
                    Log.d(TAG, "Collecting apps...");
                mRows.clear();
                mSortedRows.clear();
                mRows1.clear();
                mRows2.clear();

                // collect all launchable apps, plus any packages that have
                // notification settings
                final List<ApplicationInfo> appInfos = new ArrayList<ApplicationInfo>();

                final List<LauncherActivityInfo> lais = mLauncherApps
                        .getActivityList(null /* all */,
                                UserHandle.getCallingUserHandle());

                for (LauncherActivityInfo lai : lais) {
                    appInfos.add(lai.getApplicationInfo());
                }

                final List<ResolveInfo> resolvedConfigActivities = queryNotificationConfigActivities(mPM);

                for (ResolveInfo ri : resolvedConfigActivities) {
                    appInfos.add(ri.activityInfo.applicationInfo);
                }

                for (ApplicationInfo info : appInfos) {
                    final String key = info.packageName;
                    if (mRows.containsKey(key)) {
                        // we already have this app, thanks
                        continue;
                    }

                    final AppRow row = loadAppRow(mPM, info, mBackend);
                    row.first = false;
                                        /*aurora, linchunhui 统一系统应用图标显示 20150819 begin*/
                    Drawable mIcon = Utils2Icon.getInstance(mContext).getIconDrawable(info.packageName, Utils2Icon.INTER_SHADOW);
                    if (mIcon != null) {
                        row.icon = mIcon;
                    }
                                        /*aurora, linchunhui 统一系统应用图标显示 20150819 end*/
                    mRows.put(key, row);
                    if (!row.banned) {
                        mRows1.add(row);
                    } else {
                        mRows2.add(row);
                    }
                }

                // add config activities to the list
                applyConfigActivities(mPM, mRows, resolvedConfigActivities);

                // sort rows
                Collections.sort(mRows1, mRowComparator);
                Collections.sort(mRows2, mRowComparator);
                mSortedRows.addAll(mRows1);
                mSortedRows.addAll(mRows2);

                // compute sections
                mSections.clear();
                String section = null;
                for (AppRow r : mSortedRows) {
                    r.section = getSection(r.label);
                    if (!r.section.equals(section)) {
                        section = r.section;
                        mSections.add(section);
                    }
                }
                mHandler.post(mRefreshAppsListRunnable);
                final long elapsed = SystemClock.uptimeMillis() - start;
            }
        }
    };

    private void refreshDisplayedItems() {
        if (DEBUG)
            Log.d(TAG, "Refreshing apps...");
        mAdapter.clear();
        synchronized (mSortedRows) {
            if (mSortedRows == null) {
                return;
            }
            final int N = mSortedRows.size();
            for (int i = 0; i < N; i++) {
                if (mSortedRows.size() < i) {
                    return;
                }

                final AppRow row = mSortedRows.get(i);

                if (isLockScreenModeSecurityAndShowAll()) {

                } else {
                    final PackageInfo info = findPackageInfo(mPM, row.pkg,
                            row.uid);
                    if (info == null || Utils.isSystemPackage(mPM, info)) {
                        continue;
                    }
                    // hide contacts.apk show notification
                    if (row.pkg.equals("com.android.contacts")) {
                        continue;
                    }
                }

                mAdapter.add(row);
            }

            final int M = mAdapter.getCount();
            if (M > 0) {
                ((AppRow) mAdapter.getItem(0)).first = true;
                for (int i = 0; i < M; i++) {
                    if (((AppRow) mAdapter.getItem(0)).banned == true)
                        break;
                    if (((AppRow) mAdapter.getItem(i)).banned != true)
                        continue;
                    if (((AppRow) mAdapter.getItem(0)).banned == false
                            && ((AppRow) mAdapter.getItem(i)).banned == true) {
                        ((AppRow) mAdapter.getItem(i)).first = true;
                        break;
                    }
                }
            }
        }

        if (mListViewState != null) {
            if (DEBUG)
                Log.d(TAG, "Restoring listView state");
            getListView().onRestoreInstanceState(mListViewState);
            mListViewState = null;
        }
        if (DEBUG)
            Log.d(TAG, "Refreshed " + mSortedRows.size() + " displayed items");

        if (mAdapter.getCount() == 0) {
            mEmptyView.setText(R.string.no_third_app);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

	/*
     * 参考AuroraAppNotificationSetting---findPackageInfo
	 */

    public PackageInfo findPackageInfo(PackageManager pm, String pkg, int uid) {
        final String[] packages = pm.getPackagesForUid(uid);
        if (packages != null && pkg != null) {
            final int N = packages.length;
            for (int i = 0; i < N; i++) {
                final String p = packages[i];
                if (pkg.equals(p)) {
                    try {
                        return pm.getPackageInfo(pkg,
                                PackageManager.GET_SIGNATURES);
                    } catch (NameNotFoundException e) {
                        Log.w(TAG, "Failed to load package " + pkg, e);
                    }
                }
            }
        }
        return null;
    }

    /*
     * 图案锁/数字锁 +显示所有通知情况下, return true
     */
    private boolean isLockScreenModeSecurityAndShowAll() {
        int show = 0;
        int value = 0;
        try {
            show = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS);
            value = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (mSecure && (show == 1) && (value == 1)) {
            return true;
        }
        return false;
    }

    private final Runnable mRefreshAppsListRunnable = new Runnable() {
        @Override
        public void run() {
            refreshDisplayedItems();
        }
    };

    public static class Backend {
        static INotificationManager sINM = INotificationManager.Stub
                .asInterface(ServiceManager
                        .getService(Context.NOTIFICATION_SERVICE));

        public boolean setNotificationsBanned(String pkg, int uid,
                                              boolean banned) {
            try {
                sINM.setNotificationsEnabledForPackage(pkg, uid, !banned);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean getNotificationsBanned(String pkg, int uid) {
            try {
                final boolean enabled = sINM.areNotificationsEnabledForPackage(
                        pkg, uid);
                return !enabled;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean getHighPriority(String pkg, int uid) {
            try {
                return sINM.getPackagePriority(pkg, uid) == Notification.PRIORITY_MAX;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean setHighPriority(String pkg, int uid, boolean highPriority) {
            try {
                sINM.setPackagePriority(pkg, uid,
                        highPriority ? Notification.PRIORITY_MAX
                                : Notification.PRIORITY_DEFAULT);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean getSensitive(String pkg, int uid) {
            try {
                return sINM.getPackageVisibilityOverride(pkg, uid) == Notification.VISIBILITY_PRIVATE;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean setSensitive(String pkg, int uid, boolean sensitive) {
            try {
                sINM.setPackageVisibilityOverride(
                        pkg,
                        uid,
                        sensitive ? Notification.VISIBILITY_PRIVATE
                                : NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Error calling NoMan", e);
                return false;
            }
        }
    }

    private int alphaIndexer(BaseAdapter adapter, String s) {
        int position = -1;
        if (s == null || s.isEmpty() || adapter == null) {
            return position;
        }
        AppRow appRow;
        for (int i = 0; i < adapter.getCount(); i++) {
            appRow = (AppRow) adapter.getItem(i);
            if (appRow != null && appRow.appNamePinYin.startsWith(s)) {
                position = i;
                break;
            }
        }
        return position;
    }

    /**
     * 初始化ActionBar编辑按钮，绑定监听事件，设置底部菜单栏，设置编辑模式左上角右上角按钮和功能
     */
    public void setDefaultActionBar(AuroraActivity auroraActivity) {
        if (mActionBar.getVisibility() != View.VISIBLE) {
            mActionBar.setVisibility(View.VISIBLE);
        }
        mActionBar.addItem(AuroraActionBarItem.Type.Edit, 1);
        mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        ((AuroraActivity) getActivity()).setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
        mActionBar.initActionBottomBarMenu(R.menu.aurora_notify_app_bottom_menu, 2); //显示底部菜单栏
        showLeftRight();
    }

    /**
     * 点击ActionBar编辑按钮的监听事件，进入编辑模式
     */
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
                case 1:
                    mAdapter.setCheckBoxEnable(true);
                    mAdapter.setNeedAnim(true);
                    setEditMode(true);
                    mActionBar.setShowBottomBarMenu(true);
                    mActionBar.showActionBarDashBoard();
                    ((TextView) mActionBar.getSelectLeftButton()).setTextColor(getResources().getColor(R.color.white));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 进入普通模式
     */
    public void changeToNormalMode() {
        if (getActivity() == null) {
            return;
        }

        getListView().auroraEnableSelector(true);
        ((TextView) (mActionBar.getSelectRightButton())).setText(mSelectAllStr);

        try {
            onSelectAll(false);
            mAdapter.setCheckBoxEnable(false);
            mAdapter.setNeedAnim(true);
            mAdapter.getCheckedItem().clear();
            setEditMode(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
                case R.id.show: {
                    setToNotify(true);
                    break;
                }

                case R.id.not_show: {
                    setToNotify(false);
                    break;
                }

                default:
                    break;
            }
        }
    };

    /**
     * 定义在编辑模式下，左上角“取消”按钮，右上角“全选/反选”按钮的显示和功能
     */
    private void showLeftRight() {
        if (mActionBar.getSelectLeftButton() != null) {
            mActionBar.getSelectLeftButton().setOnClickListener(
                    new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            //TODO Auto-generated method stub
                            mActionBar.setShowBottomBarMenu(false); //隐藏底部菜单栏
                            mActionBar.showActionBarDashBoard();//退出编辑模式

                            if (getEditMode()) {
                                changeToNormalMode(); //进入普通模式
                            }
                        }
                    });
        }

        if (mActionBar.getSelectRightButton() != null) {
            mActionBar.getSelectRightButton().setOnClickListener(
                    new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            //TODO Auto-generated method stub

                            String selectStr = ((TextView) (mActionBar.getSelectRightButton())).getText().toString();
                            if (selectStr.equals(mSelectAllStr)) { //当按钮显示为“全选”时
                                ((TextView) (mActionBar.getSelectRightButton())).setText(mUnSelectAllStr);
                                onSelectAll(true);
                            } else if (selectStr.equals(mUnSelectAllStr)) {//当按钮显示为“反选”时
                                ((TextView) (mActionBar.getSelectRightButton())).setText(mSelectAllStr);
                                onSelectAll(false);
                            }
                        }
                    });
        }
    }

    /**
     * checked true 全选, false 反选
     */
    public void onSelectAll(boolean check) {
        updateListCheckBoxeState(check);
    }

    /**
     * checked true 全选, false 反选
     */
    private void updateListCheckBoxeState(boolean checked) {
        final int headerCount = getListView().getHeaderViewsCount();
        final int count = mAdapter.getCount() + headerCount;

        for (int position = headerCount; position < count; position++) {
            int adapterPos = position - headerCount;
            AppRow mAppRow = (AppRow) mAdapter.getItem(adapterPos);
            if (checked) {
                mAdapter.setCheckedItem(mAdapter.getItemId(adapterPos), adapterPos);
                mAppRow.isChecked = true;
            } else {
                mAdapter.getCheckedItem().clear();
                mAppRow.isChecked = false;
            }

            int realPos = position - getListView().getFirstVisiblePosition();

            if (realPos >= 0) {
                View view = getListView().getChildAt(realPos);
                if (view != null) {
                    final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                    if (null != checkBox) {
                        checkBox.auroraSetChecked(checked, true);
                    }
                }
            }
        }

        if (null != getActivity()) {
            //    setBottomMenuEnable(checked);
        }
    }

    /**
     * flag true 底部菜单按钮可以点击, flase 底部菜单按钮不可以点击
     */
    public void setBottomMenuEnable(boolean flag) {
        AuroraMenu auroraMenu = mActionBar.getAuroraActionBottomBarMenu();
        auroraMenu.setBottomMenuItemEnable(2, flag);
    }

    /**
     * show true 显示通知, false 不显示通知
     */
    public void setToNotify(boolean show) {
        int selectedCount = mAdapter.getCheckedItem().size();
        if (selectedCount <= 0) {
            return;
        } else {
            AppRow mAppRow = null;
            for (int i = 0; i < mAdapter.getCount(); i++) {
                mAppRow = (AppRow) mAdapter.getItem(i);
                if (mAppRow.isChecked) {
                    mAppRow.banned = !show;
                    mBackend.setNotificationsBanned(mAppRow.pkg, mAppRow.uid, !show);
                }
            }
            loadAppsList();
        }
        mActionBar.setShowBottomBarMenu(false);
        mActionBar.showActionBarDashBoard();
        if (getEditMode()) {
            changeToNormalMode();
        }
    }

}
