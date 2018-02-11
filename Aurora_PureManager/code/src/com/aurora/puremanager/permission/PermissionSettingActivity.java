package com.aurora.puremanager.permission;

import java.util.ArrayList;
import java.util.List;

import com.aurora.puremanager.R;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.content.ContentValues;
import android.os.SystemProperties;
import android.net.Uri;

public class PermissionSettingActivity extends BaseActivity {

    private static final int NONE_TRUST = 2;
    private static final int PARTIAL_TRUST = 1;
    private static final int ALL_TRUST = 0;
    private PermissionSettingAdapter mAdapter;
    private ListView mListview;
    private List<ItemInfo> mData = new ArrayList<ItemInfo>();
    private static final Uri GN_TRUST_URI = Uri
            .parse("content://com.provider.PermissionProvider/whitelist");
    private static final Uri GN_PERM_URI = Uri
            .parse("content://com.provider.PermissionProvider/permissions");
    private ContentValues mContentValues;
//	private ChameleonColorManager mChameleonColorManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_setting);
        setupData(this);
        setupViews(this);
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
    private void setupViews(Context context) {

        mAdapter = new PermissionSettingAdapter(context, mData);
        mListview = (ListView) findViewById(R.id.trust_level_listview);
        mListview.setEnabled(false);
        mListview.setAdapter(mAdapter);
        mListview.setOnItemClickListener(mListListener);
    }

    private void setupData(Context context) {
        mData.clear();
        String[] titles = context.getResources().getStringArray(R.array.listitem_permission_setting);
        String[] summarys = context.getResources()
                .getStringArray(R.array.listitem_permission_setting_summary);
        for (int i = 0; i < titles.length; i++) {
            ItemInfo item = new ItemInfo();
            item.setName(titles[i]);
            item.setSummary(summarys[i]);
            mData.add(item);
        }
        mContentValues = new ContentValues();

    }

    private OnItemClickListener mListListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // TODO Auto-generated method stub
            switch (arg2) {
                case ALL_TRUST:
                    SystemProperties.set("persist.sys.permission.level", PermissionSettingAdapter.ALL_TRUST);
                    mContentValues.put("status", 1);
                    updateRawToDB(mContentValues, GN_PERM_URI);
                    updateRawToDB(mContentValues, GN_TRUST_URI);
                    break;
                case PARTIAL_TRUST:
                    SystemProperties.set("persist.sys.permission.level",
                            PermissionSettingAdapter.PARTIAL_TRUST);
                    mContentValues.put("status", 0);
                    updateRawToDB(mContentValues, GN_PERM_URI);
                    updateRawToDB(mContentValues, GN_TRUST_URI);
                    mContentValues.put("status", 1);
                    updatePermissionInPartialTrust(PermissionSettingActivity.this, mContentValues);
                    break;
                case NONE_TRUST:
                    SystemProperties.set("persist.sys.permission.level", PermissionSettingAdapter.NONE_TRUST);
                    mContentValues.put("status", 0);
                    updateRawToDB(mContentValues, GN_PERM_URI);
                    updateRawToDB(mContentValues, GN_TRUST_URI);
                    break;

                default:
                    break;
            }
            mAdapter.notifyDataSetInvalidated();
        }
    };

    private void updateRawToDB(ContentValues cv, Uri uri) {
        PermissionSettingActivity.this.getContentResolver().update(uri, cv, null, null);
    }

    private boolean updatePermissionInPartialTrust(Context context, ContentValues cv) {
        String temp = HelperUtils_per.mImportantPermissions.toString();
        temp = temp.replace('[', '(');
        temp = temp.replace(']', ')');
        temp = temp.replaceAll(",", "','").replaceAll(" ", "");

        int result = context.getContentResolver().update(GN_PERM_URI, cv,
                "permission not in ('" + temp.substring(1, temp.length() - 1) + "')", null);
        if (result > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void addPackage(String pkgName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePackage(String pkgName) {
        // TODO Auto-generated method stub

    }

}
