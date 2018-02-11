package com.aurora.puremanager.permission;

import java.util.ArrayList;
import java.util.List;

import com.aurora.puremanager.R;
import com.aurora.puremanager.data.BaseData;

import android.R.integer;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
//import amigo.widget.AmigoButton;
import android.widget.ImageView;
//import amigo.widget.AmigoTextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.SystemProperties;
import aurora.widget.AuroraSpinner;

public class PermissionAppDetailAdapter extends BaseAdapter implements
		OnItemSelectedListener {
	private static final int UNIMPORTANT_PERMISSION = 0;
	private static final int IMPORTANT_PERMISSION = 1;

	private List<String> mPkgPermLable = new ArrayList<String>();
	private List<String> mPermissions = new ArrayList<String>();
	private int[] mStateList, mImportantList;
	private PackageManager mPm;
	private String mPackageName;

	private Activity activity;
	private SpinnerAdapter _MyAdapter;

	public PermissionAppDetailAdapter(Activity activity) {
		this.activity = activity;
		// 建立数据源
		List<SpinnerInfo> persons = new ArrayList<SpinnerInfo>();
		persons.add(new SpinnerInfo(activity.getResources().getString(
				(R.string.text_allow))));
		persons.add(new SpinnerInfo(activity.getResources().getString(
				(R.string.text_ask))));
		persons.add(new SpinnerInfo(activity.getResources().getString(
				(R.string.text_forbidden))));
		_MyAdapter = new SpinnerAdapter(activity, persons);
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		String info = null;
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.softmanager_permission_adapter_layout, null);
			viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
			viewHolder.mSpinner = (AuroraSpinner) convertView
					.findViewById(R.id.spinner);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		info = getTitle(mPermissions.get(position));
		int permissionSort = PermissionsInfo.getInstance().mPerMissionMap
				.get(mPermissions.get(position));
		if (info != null) {
			viewHolder.mTitle.setText(info);
			viewHolder.mSpinner.setTag(position);
			viewHolder.mSpinner.setAdapter(_MyAdapter);
			viewHolder.mSpinner.setOnItemSelectedListener(this);

			int state = mStateList[position];
			int important = mImportantList[position];
			if (state == 1) {
				viewHolder.mSpinner.setSelection(0);
			} else if (state == 0) {
				viewHolder.mSpinner.setSelection(1);
			} else {
				viewHolder.mSpinner.setSelection(2);
			}
		}
		return convertView;
	}

	/*
	 * public PermissionAppDetailAdapter(Context context) { mContext = context;
	 * mPm = mContext.getPackageManager(); // TODO Auto-generated constructor
	 * stub }
	 * 
	 * @Override public Object getChild(int groupPosition, int childPosition) {
	 * // TODO Auto-generated method stub return null; }
	 * 
	 * @Override public long getChildId(int groupPosition, int childPosition) {
	 * // TODO Auto-generated method stub return childPosition; }
	 * 
	 * @Override public View getChildView(int groupPosition, int childPosition,
	 * boolean isLastChild, View convertView, ViewGroup parent) { final int
	 * position = groupPosition; // TODO Auto-generated method stub if
	 * (convertView == null) { convertView =
	 * LayoutInflater.from(mContext).inflate
	 * (R.layout.softmanager_child_listview, parent, false); }
	 * 
	 * final Button button1 = (Button) convertView.findViewById(R.id.bt1);
	 * button1.setOnClickListener(new Button.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { if (mStateList[position] !=
	 * PermissionsInfo.STATUS_PERMIT) { mStateList[position] =
	 * PermissionsInfo.STATUS_PERMIT; if
	 * (PermissionsInfo.getInstance().updatePermission(mContext,
	 * mPermissions.get(position), mPackageName, mStateList[position])) {
	 * notifyDataSetChanged(); setchecked(); } } } });
	 * 
	 * final Button button2 = (Button) convertView.findViewById(R.id.bt2); if
	 * (PermissionSettingAdapter.PARTIAL_TRUST.equalsIgnoreCase(SystemProperties
	 * .get("persist.sys.permission.level")) ||
	 * PermissionSettingAdapter.ALL_TRUST.equalsIgnoreCase(SystemProperties
	 * .get("persist.sys.permission.level"))) {
	 * button2.setText(R.string.text_ask); } else if
	 * (PermissionSettingAdapter.NONE_TRUST.equalsIgnoreCase(SystemProperties
	 * .get("persist.sys.permission.level"))) {
	 * button2.setText(R.string.text_ask); }
	 * 
	 * button2.setOnClickListener(new Button.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { if (mStateList[position] !=
	 * PermissionsInfo.STATUS_REQUEST) { mStateList[position] =
	 * PermissionsInfo.STATUS_REQUEST; if
	 * (PermissionsInfo.getInstance().updatePermission(mContext,
	 * mPermissions.get(position), mPackageName, mStateList[position])) {
	 * notifyDataSetChanged(); setchecked(); } } } });
	 * 
	 * final Button button3 = (Button) convertView.findViewById(R.id.bt3);
	 * button3.setOnClickListener(new Button.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { if (mStateList[position] !=
	 * PermissionsInfo.STATUS_PROHIBITION) { mStateList[position] =
	 * PermissionsInfo.STATUS_PROHIBITION; if
	 * (PermissionsInfo.getInstance().updatePermission(mContext,
	 * mPermissions.get(position), mPackageName, mStateList[position])) {
	 * notifyDataSetChanged(); setchecked(); } notifyDataSetChanged(); } } });
	 * return convertView; }
	 * 
	 * @Override public int getChildrenCount(int groupPosition) { // TODO
	 * Auto-generated method stub return 1; }
	 * 
	 * @Override public Object getGroup(int groupPosition) { // TODO
	 * Auto-generated method stub return mPkgPermLable.get(groupPosition); }
	 * 
	 * @Override public int getGroupCount() { // TODO Auto-generated method stub
	 * return mPkgPermLable.size(); }
	 * 
	 * @Override public long getGroupId(int groupPosition) { // TODO
	 * Auto-generated method stub return groupPosition; }
	 * 
	 * @Override public View getGroupView(int groupPosition, boolean isExpanded,
	 * View convertView, ViewGroup parent) { // TODO Auto-generated method stub
	 * String info = null; ViewHolder viewHolder; if (convertView == null) {
	 * viewHolder = new ViewHolder(); convertView =
	 * LayoutInflater.from(mContext).inflate(
	 * R.layout.softmanager_permission_adapter_layout, null); viewHolder.mTitle
	 * = (TextView) convertView.findViewById(R.id.title); viewHolder.mSummary =
	 * (TextView) convertView.findViewById(R.id.summary); viewHolder.mIndicator
	 * = (ImageView) convertView.findViewById(R.id.indicator_arrow);
	 * convertView.setTag(viewHolder); } else { viewHolder = (ViewHolder)
	 * convertView.getTag(); }
	 * 
	 * info = getTitle(mPermissions.get(groupPosition)); int permissionSort =
	 * PermissionsInfo.getInstance().mPerMissionMap
	 * .get(mPermissions.get(groupPosition)); Drawable icon =
	 * mContext.getResources().getDrawable(
	 * PermissionsInfo.getInstance().DRAWABLE_ID[permissionSort]); if (info !=
	 * null) { viewHolder.mIndicator.setImageResource(R.drawable.open_holo);
	 * viewHolder.mIndicator.setVisibility(View.VISIBLE);
	 * 
	 * viewHolder.mTitle.setText(info);
	 * 
	 * int state = mStateList[groupPosition]; int important =
	 * mImportantList[groupPosition]; if (state == 1) {
	 * viewHolder.mSummary.setText(R.string.text_allow); } else if (state == 0)
	 * { viewHolder.mSummary.setText(R.string.text_ask);
	 * 
	 * } else { viewHolder.mSummary.setText(R.string.text_forbidden); }
	 * viewHolder.mSummary.setVisibility(View.VISIBLE); } return convertView; }
	 * 
	 * @Override public boolean hasStableIds() { // TODO Auto-generated method
	 * stub return false; }
	 * 
	 * @Override public boolean isChildSelectable(int groupPosition, int
	 * childPosition) { // TODO Auto-generated method stub return false; }
	 */

	public void changeItems(String pkgname) {
		mPackageName = pkgname;
		queryDB(mPackageName);
		if ("".equalsIgnoreCase(SystemProperties
				.get("persist.sys.permission.level"))) {
			SystemProperties.set("persist.sys.permission.level",
					SystemProperties.get("ro.gn.permission.trust.level", "0"));
		}
	}

	private void queryDB(String pkgname) {
		if (pkgname == null) {
			return;
		}
		mPkgPermLable.clear();
		mPermissions.clear();
		mStateList = null;
		mImportantList = null;

		String[] projection = new String[] { "permission", "packagename",
				"status" };
		Cursor cursor = activity.getContentResolver().query(
				Uri.parse(PermissionsInfo.URI), projection, "packagename = ?",
				new String[] { pkgname }, null);

		mStateList = new int[cursor.getCount()];
		mImportantList = new int[cursor.getCount()];
		int i = 0, j = 0;

		while (cursor.moveToNext()) {
			String permission = cursor.getString(0);
			if (NfcAdapter.getDefaultAdapter(activity) == null) {
				if (permission.equalsIgnoreCase("android.permission.NFC")) {
					continue;
				}
			}
			int state = cursor.getInt(2);
			if (HelperUtils_per.mCheckPermissions.contains(permission)) {
				mPkgPermLable.add(getPkgPermLabel(transPermission(permission)));
				mStateList[i++] = state;
				mPermissions.add(permission);
				if (HelperUtils_per.mImportantPermissions.contains(permission)) {
					mImportantList[j++] = IMPORTANT_PERMISSION;
				} else {
					mImportantList[j++] = UNIMPORTANT_PERMISSION;
				}
			}
		}
		cursor.close();
	}

	public boolean haveSecurityPermissions() {
		boolean temp = true;
		if (mPkgPermLable.size() == 0) {
			temp = false;
		}
		return temp;
	}

	public void operatePermissionDB(boolean trust) {
		if (mStateList == null || mImportantList == null) {
			return;
		}
		if (trust) {
			for (int i = 0; i < mStateList.length; i++) {
				mStateList[i] = 1;
			}
		} else {
			for (int i = 0; i < mStateList.length; i++) {
				if (PermissionSettingAdapter.PARTIAL_TRUST
						.equalsIgnoreCase(SystemProperties
								.get("persist.sys.permission.level"))) {
					if (mImportantList[i] == 1) {
						mStateList[i] = 0;
					} else {
						mStateList[i] = 1;
					}
				} else {
					mStateList[i] = 0;
				}
			}
		}

		ContentValues cv = new ContentValues();
		if (trust) {
			cv.put("status", 1);
		} else {
			cv.put("status", 0);
		}
		PermissionsInfo.getInstance().updatePermission(activity, cv,
				mPackageName);
		if (PermissionSettingAdapter.PARTIAL_TRUST
				.equalsIgnoreCase(SystemProperties
						.get("persist.sys.permission.level"))) {
			if (!trust) {
				cv.put("status", 1);
				PermissionsInfo.getInstance().updatePermissionInPartialTrust(
						activity, cv, mPackageName);
			}
		}
		notifyDataSetChanged();
	}

	/*
	 * private int getDrawable(String permission) { int drawableId = 0; for (int
	 * i = 0; i < PermissionsInfo.PERMISSIONS.length; i++) { if
	 * (permission.equalsIgnoreCase(PermissionsInfo.PERMISSIONS[i])) {
	 * drawableId = i; break; } } return
	 * PermissionsInfo.DRAWABLE_ID[drawableId]; }
	 */

	private String getTitle(String permission) {
		String title = null;
		String[] titles = activity.getResources().getStringArray(
				R.array.ui_privacy_policy);
		for (int i = 0; i < PermissionsInfo.PERMISSIONS.length; i++) {
			if (permission.equalsIgnoreCase(PermissionsInfo.PERMISSIONS[i])) {
				title = titles[i];
				break;
			}
		}
		return title;
	}

	private String getPkgPermLabel(final String permission) {

		if (permission == null) {
			return null;
		}
		String tempGrpName = permission;
		if (permission.contains("WRITE_SMS_MMS")) {
			return activity.getResources().getString(R.string.permlab_writeMms);
		}
		if (permission.contains("READ_SMS_MMS")) {
			return activity.getResources().getString(R.string.permlab_readMms);
		}
		if (permission.contains("SEND_SMS_MMS")) {
			return activity.getResources().getString(R.string.perm_send_mms);
		}
		PermissionInfo pgi = null;
		mPm = activity.getPackageManager();
		if (null != mPm) {
			try {
				pgi = mPm.getPermissionInfo(tempGrpName, 0);
			} catch (NameNotFoundException e) {
				if (tempGrpName.equals("DefaultGrp")) {
					tempGrpName = activity.getResources().getString(
							R.string.perm_default);
				}
			}
			if (null != pgi) {
				tempGrpName = pgi.loadLabel(mPm).toString();
			}
		}
		return tempGrpName;
	}

	private String transPermission(String permission) {
		String result = permission;
		if (result.contains("CONTACTS_CALLS")) {
			result = result.replace("_CONTACTS_CALLS", "_CALL_LOG");
		}
		return result;
	}

	private void setchecked() {
		if (PermissionsInfo.mTrust == 1) {
			PermissionsInfo.mTrust = 0;
			PermissionAppDetail.setchecked(PermissionsInfo.mTrust);
		}
	}

	private void showToast(String msg) {
		Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mPkgPermLable.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mPkgPermLable.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		int pos = (Integer) parent.getTag();
		switch (position) {
		case 0:	//允许
			if (mStateList[pos] != PermissionsInfo.STATUS_PERMIT) {
                mStateList[pos] = PermissionsInfo.STATUS_PERMIT;
                if (PermissionsInfo.getInstance().updatePermission(activity, mPermissions.get(pos),
                        mPackageName, mStateList[pos])) {
                    notifyDataSetChanged();
                    setchecked();
                }
            }
			break;
		case 1:	//询问
			if (mStateList[pos] != PermissionsInfo.STATUS_REQUEST) {
                mStateList[pos] = PermissionsInfo.STATUS_REQUEST;
                if (PermissionsInfo.getInstance().updatePermission(activity, mPermissions.get(pos),
                        mPackageName, mStateList[pos])) {
                    notifyDataSetChanged();
                    setchecked();
                }
            }
			break;
		case 2:	//禁止
			if (mStateList[pos] != PermissionsInfo.STATUS_PROHIBITION) {
                mStateList[pos] = PermissionsInfo.STATUS_PROHIBITION;
                if (PermissionsInfo.getInstance().updatePermission(activity, mPermissions.get(pos),
                        mPackageName, mStateList[pos])) {
                    notifyDataSetChanged();
                    setchecked();
                }
                notifyDataSetChanged();
            }
			break;
		default:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
	}
}