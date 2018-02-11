package com.aurora.puremanager.permission;

import java.util.ArrayList;
import java.util.List;
import com.aurora.puremanager.R;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PermissionDetailActivity extends BaseActivity implements LoaderCallbacks<Object> {
    public static final String EXTRA_NAME_POSITION = "position";
    public static final String EXTRA_NAME_TITLE = "title";
    private GNPermissionInfo mPermissionInfo;
    private PermissionAdapter mAdapter;
    private ExpandableListView mListView;
    private LinearLayout mLoader;
    private RelativeLayout mEmpty;
    private TextView mEmptyTextView;
    private boolean mEnableFlag = true;
    private int mPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
//        getAmigoActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_tab_light_bg));
        setContentView(R.layout.softmanager_expand_listview_layout);
        mPos = getIntent().getIntExtra(EXTRA_NAME_POSITION, -1);
        if (mPos < 0) {
            finish();
            return;
        }
        String neme = getIntent().getStringExtra(EXTRA_NAME_TITLE);
        getWindow().setTitle(neme);

        mListView = (ExpandableListView) findViewById(R.id.expand_listview);
        mListView.setGroupIndicator(null);
        mLoader = (LinearLayout) findViewById(R.id.loader);
        mLoader.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmpty = (RelativeLayout) findViewById(R.id.permission_empty_view);
        SoftManagerLoader_per.sCurrentId = mPos;
        getLoaderManager().initLoader(SoftManagerLoader_per.ID_LOADER_PERMISSION, null, this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void prepareData() {
        // PermissionsInfo.sCurrentId = mPos;
        // PermissionsInfo.getInstance().reloadPmisById(this, PermissionsInfo.sCurrentId);
        mAdapter = new PermissionAdapter(this);
        mAdapter.setPosition(mPos);
        mPermissionInfo = PermissionsInfo.getInstance().mPermissions.get(mPos);
    }

    private void initListView() {
        List<ItemInfo> items = new ArrayList<ItemInfo>();
        items.addAll(mPermissionInfo.getList());
        mAdapter.changeItems(items);
        if (mPermissionInfo.getList().size() <= 0 && ApplicationsInfo.getInstance().isReady()) {
            mEmpty.setVisibility(View.VISIBLE);
            mLoader.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
        } else {
            mEmpty.setVisibility(View.GONE);
            mLoader.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        // TODO Auto-generated method stub
        return new SoftManagerLoader_per(this);
    }

    @Override
    public void onLoaderReset(Loader<Object> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLoadFinished(Loader<Object> arg0, Object arg1) {
        prepareData();
        refreshUi();
    }

    @Override
    public void addPackage(String pkgName) {
        // TODO Auto-generated method stub
        ApplicationsInfo.getInstance().loadPermissionsAppEntries(this);
        ApplicationsInfo.getInstance().addPackage(this, pkgName);
        PermissionsInfo.getInstance().addPackage(this, pkgName);
        PermissionsInfo.getInstance().reloadPmisById(this, SoftManagerLoader_per.sCurrentId, true);
        DebugUtil.v("size: " + mPermissionInfo.getEnable().size());
        refreshUi();
    }

    @Override
    public void removePackage(String pkgName) {
        // TODO Auto-generated method stub
        PermissionsInfo.getInstance().removePackage(pkgName);
        ApplicationsInfo.getInstance().removePackage(this, pkgName);
        PermissionsInfo.getInstance().reloadPmisById(this, SoftManagerLoader_per.sCurrentId, true);
        refreshUi();
    }

    private void refreshUi() {
        initListView();
        mListView.setAdapter(mAdapter);
        mListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            public void onGroupExpand(int groupPosition) {
                for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                    if (groupPosition != i) {
                        mListView.collapseGroup(i);
                    }
                }
            }
        });

        if (mPermissionInfo.getList().size() <= 0) {
            mEmpty.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mEmpty.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        }
    }
}
