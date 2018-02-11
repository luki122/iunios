package com.aurora.puremanager.permission;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

import com.aurora.puremanager.R;
import com.aurora.puremanager.utils.mConfig;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-4-26 Change List:
 */
public class PermissionMrgActivity extends AuroraActivity implements
		LoaderCallbacks<Object>, OnItemClickListener, TabListener {

	private static final String TAG = "PermissionMrgActivity-->";
	private List<ItemInfo> mData = new ArrayList<ItemInfo>();
	private List<ItemInfo> mDataApp = new ArrayList<ItemInfo>();
	private PermissionsInfo mPermissionsInfo;
	private LinearLayout mLoader;
	private RelativeLayout mEmptyView;
	private CompositeAdapter mPermissionAppMrgAdapter;
	private int mPermissionCount = 0;
	private ListView mListView;
	private CompositeAdapter mAdapter;
	private Resources mRes;

	MyPagerAdapter mMypagerAdapter;
	ViewPager mViewPager;
	private int mNotNeedRefresh = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.softmanager_activity_permission_mrg);
//		setContentView(R.layout.gn_actionbar_tab);
		if(mConfig.isNative){
        	setContentView(R.layout.gn_actionbar_tab);
        }else{
        	setAuroraContentView(R.layout.gn_actionbar_tab,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.jurisdiction_manage);
            getAuroraActionBar().setBackgroundResource(R.color.per_activity_title_color);
        }
		/*
		 * final ActionBar actionBar = getActionBar();
		 * 
		 * actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		 * actionBar.setDisplayShowTitleEnabled(true);
		 * actionBar.setDisplayHomeAsUpEnabled(true);
		 * actionBar.setDisplayShowHomeEnabled(true);
		 */
		mRes = getResources();
		mPermissionsInfo = PermissionsInfo.getInstance();
		SoftManagerLoader_per.sCurrentId = -1;
		getLoaderManager().initLoader(
				SoftManagerLoader_per.ID_LOADER_PERMISSION, null, this);
		PermissionsInfo.isPermissionApp = true;

		createPage1View();
		// createPage2View();

		mMypagerAdapter = new MyPagerAdapter();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mMypagerAdapter);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		if (tab.getPosition() == 0) {
			PermissionsInfo.isPermissionApp = true;
			SoftManagerLoader_per.sCurrentId = -1;
			getLoaderManager().initLoader(
					SoftManagerLoader_per.ID_LOADER_PERMISSION, null,
					PermissionMrgActivity.this);
		} else {
			PermissionsInfo.isPermissionApp = false;

		}
		if (mViewPager.getCurrentItem() != tab.getPosition()) {
			mViewPager.setCurrentItem(tab.getPosition(), true);
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	private void refreshUI() {
		if (PermissionsInfo.isPermissionApp) {
			/*
			 * if(mDataApp.size() == mPermissionsApplist.size()){
			 * mNotNeedRefresh = 0; }
			 */
			if (mDataApp.size() == ApplicationsInfo.getInstance().mPermissionsAppEntries
					.size()) {
				mNotNeedRefresh = 0;
			}
			refreshPermissionAppList();
		} else {
			refreshPermissionList();
		}
		mMypagerAdapter.notifyDataSetChanged();

	}

	// Gionee <lihq> <2013-05-28> modify add for CR00819921 begin
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		SoftManagerLoader_per.sCurrentId = -1;
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNotNeedRefresh = -1;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		releasRes();
		if (mData != null) {
			mData.clear();
		}
		if (mDataApp != null) {
			mDataApp.clear();
		}
		super.onDestroy();
	}

	private void releasRes() {
		HelperUtils_per.unbindDrawables(findViewById(R.id.root));
		System.gc();
	}

	private void refreshPermissionList() {
		mAdapter = getAdapter();
		if (mPermissionsInfo.mPermissions.size() < mPermissionCount
				|| mPermissionsInfo.mPermissions.size() == 0) {
			return;
		}
		String[] summaries = mRes.getStringArray(R.array.permission_summary);
		for (int i = 0; i < mPermissionCount; i++) {
			GNPermissionInfo pmisInfo = mPermissionsInfo.mPermissions.get(i);
			if (pmisInfo.getNumbers() > 0) {
				mData.get(i).setSummary(pmisInfo.getNumbers() + summaries[i]);
			} else {
				mData.get(i).setSummary(
						mRes.getString(R.string.text_no_app_has_permission));
			}
		}
		mListView.setVisibility(View.VISIBLE);
		mLoader.setVisibility(View.GONE);
		mEmptyView.setVisibility(View.GONE);
		mListView.setAdapter(mAdapter);
		// mAdapter.notifyDataSetChanged();
	}

	private void refreshPermissionAppList() {
		mDataApp.clear();
		mDataApp.addAll(ApplicationsInfo.getInstance().mPermissionsAppEntries);
		if (mDataApp.size() > 0) {
			mPermissionAppMrgAdapter = new CompositeAdapter(
					PermissionMrgActivity.this, mDataApp, mListView);
			mListView.setVisibility(View.VISIBLE);
			mLoader.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.GONE);
			mListView.setAdapter(mPermissionAppMrgAdapter);
		} else {
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
			mLoader.setVisibility(View.GONE);
		}
		// mPermissionAppMrgAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub

		Intent intent;
		if (PermissionsInfo.isPermissionApp) {
			/*
			 * if (position >= mPermissionsApplist.size()) { return; }
			 */
			if (position >= ApplicationsInfo.getInstance().mPermissionsAppEntries
					.size()) {
				return;
			}
			intent = new Intent(this, PermissionAppDetail.class);
			/*
			 * intent.putExtra( PermissionAppDetail.PACKAGENAME,
			 * mPermissionsApplist.get(position).mPackageName);
			 */
			intent.putExtra(PermissionAppDetail.PACKAGENAME,
					ApplicationsInfo.getInstance().mPermissionsAppEntries
							.get(position).mApplicationInfo.packageName);
			intent.putExtra(PermissionAppDetail.EXTRA_NAME_TITLE,
					mDataApp.get(position).getName());
			intent.putExtra(PermissionAppDetail.FROM_MYSELF, true);
			// intent.putExtra(PermissionAppDetail.POSITION,
			// String.valueOf(position));
			startActivity(intent);

		} else {
			intent = new Intent(this, PermissionDetailActivity.class);
			intent.putExtra(PermissionDetailActivity.EXTRA_NAME_POSITION,
					position);
			intent.putExtra(PermissionDetailActivity.EXTRA_NAME_TITLE, mData
					.get(position).getName());
			startActivity(intent);
		}
	}

	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return new SoftManagerLoader_per(this);
	}

	@Override
	public void onLoaderReset(Loader<Object> arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onLoadFinished(Loader<Object> arg0, Object arg1) {
		refreshUI();
	}

	public void addPackage(String pkgName) {
		// TODO Auto-generated method stub
		ApplicationsInfo.getInstance().loadPermissionsAppEntries(this);
		ApplicationsInfo.getInstance().addPackage(this, pkgName);
		PermissionsInfo.getInstance().addPackage(this, pkgName);
		PermissionsInfo.getInstance().init(this);
		mNotNeedRefresh = -1;
		refreshUI();
	}

	public void removePackage(String pkgName) {
		// TODO Auto-generated method stub
		PermissionsInfo.getInstance().removePackage(pkgName);
		ApplicationsInfo.getInstance().removePackage(this, pkgName);
		PermissionsInfo.getInstance().init(this);
		mNotNeedRefresh = -1;
		refreshUI();
	}

	protected CompositeAdapter getAdapter() {
		// TODO Auto-generated method stub
		mData.clear();
		String[] titles = mRes.getStringArray(R.array.ui_privacy_policy);
		if (NfcAdapter.getDefaultAdapter(PermissionMrgActivity.this) == null) {
			mPermissionCount = titles.length - 1;
		} else {
			mPermissionCount = titles.length;
		}
		for (int i = 0; i < mPermissionCount; i++) {
			ItemInfo item = new ItemInfo();
			item.setName(titles[i]);
//			item.setIcon(mRes.getDrawable(PermissionsInfo.DRAWABLE_ID[i]));
			item.setImportantStatus(PermissionsInfo.IMPORTANT_PERMISSION_STATE[i]);
			mData.add(item);
		}
		return new CompositeAdapter(this, mData);
	}

	private View createPage1View() {

		View rootView = LayoutInflater.from(this).inflate(
				R.layout.softmanager_activity_permission_mrg, null);

		mListView = (ListView) rootView.findViewById(R.id.listview);
		mListView.setOnItemClickListener(this);

		mEmptyView = (RelativeLayout) rootView
				.findViewById(R.id.permission_app_empty_view);

		mLoader = (LinearLayout) rootView.findViewById(R.id.loader);

		mDataApp.clear();
		mDataApp.addAll(ApplicationsInfo.getInstance().mPermissionsAppEntries);
		if (mDataApp.size() > 0) {
			mPermissionAppMrgAdapter = new CompositeAdapter(
					PermissionMrgActivity.this, mDataApp, mListView);
			mListView.setVisibility(View.VISIBLE);
			mLoader.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.GONE);
			mListView.setAdapter(mPermissionAppMrgAdapter);
		} else {
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
			mLoader.setVisibility(View.GONE);
		}

		rootView.setTag(0);
		return rootView;

	}

	public class MyPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;
			if (position == 0) {
				view = createPage1View();
			} 
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getItemPosition(Object object) {
			View view = (View) object;
			if (mNotNeedRefresh == (Integer) view.getTag()) {
				return POSITION_UNCHANGED;
			} else {
				return POSITION_NONE;
			}
		}
	}
}
