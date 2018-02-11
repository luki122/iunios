package com.aurora.account.adapter;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraSwitch;

import com.aurora.account.R;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.receiver.StartSyncReceiver;
import com.aurora.account.service.ExtraFileUpService;
import com.aurora.account.service.ExtraFileUpService.SyncStatus;
import com.aurora.account.totalCount.TotalCount;
import com.aurora.account.util.AppInfo;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;
import com.aurora.account.util.TimeUtils;
import com.aurora.account.widget.SyncProgressView;

public class SyncListAdapter extends BaseAdapter {

	private static final String TAG = "SyncListAdapter";

	private LayoutInflater inflater;
	private List<AppConfigInfo> listData;
	private AppInfo app_info = null;
	private Context m_context;
	private boolean isDisWarn = false;

	public SyncListAdapter(Context context, List<AppConfigInfo> listData,
			boolean isDisWarn) {
		inflater = LayoutInflater.from(context);
		this.listData = listData;
		this.m_context = context;
		this.isDisWarn = isDisWarn;
		app_info = new AppInfo(context);
	}

	@Override
	public int getCount() {
		return listData == null ? 0 : listData.size();
	}

	@Override
	public Object getItem(int position) {
		return this.listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setStatus(boolean isDisWarn) {
		this.isDisWarn = isDisWarn;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		Holder holder = null;
		final AppConfigInfo app = listData.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_sync_module, null);
			holder = new Holder();
			holder.rl_title = (RelativeLayout) convertView.findViewById(R.id.rl_title);
			holder.tv_titlebar_text = (TextView) convertView.findViewById(R.id.tv_titlebar_text);
			holder.tv_name = (TextView) convertView.findViewById(R.id.app_name);
			holder.tv_warn = (TextView) convertView.findViewById(R.id.app_warn);
			holder.spv_app = (SyncProgressView) convertView.findViewById(R.id.spv_app);
			holder.as_switch = (AuroraSwitch) convertView.findViewById(R.id.wlan_switch);
			holder.tv_sync_time = (TextView) convertView.findViewById(R.id.tv_sync_time);
			holder.tv_module_switch  = (View) convertView.findViewById(R.id.module_switch);
			
			holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
			
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		// holder.tv_name.setText(app.getApp_name());

		Log.i(TAG,"zhangwei the getApp_packagename =" + app.getApp_packagename()
						+ "position =" + position);

		if (!app.isSync()
				&& (app.getApp_packagename().equals("com.android.contacts"))
				&& isDisWarn) {
			holder.tv_warn.setVisibility(View.VISIBLE);
		} else {
			holder.tv_warn.setVisibility(View.GONE);
		}

		holder.tv_name.setText(app_info.getAppName(app.getApp_packagename()));
		holder.spv_app.setImage(app_info.getAppIcon(app.getApp_packagename()));
		holder.iv_icon.setImageDrawable(app_info.getAppIcon(app.getApp_packagename()));
		
			
		holder.as_switch.setChecked(app.isSync());
		final AuroraSwitch m_switch = holder.as_switch;
		holder.tv_module_switch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//handleWlanSwitch(m_switch);
				
				
				// 是否应用自身完成同步
				if (!app.isApp_syncself()) {
					
					if (!m_switch.isChecked()) {
						// wlan_switch.setChecked(true);
						m_switch.performClick(); // setCheck没有音效和动画效果
						SystemUtils.updateAppConfigInfo(m_context,
								app.getApp_packagename(), true);
						
						Intent intent = new Intent(); // Itent就是我们要发送的内容
						intent.putExtra(
								StartSyncReceiver.PARAMS_PACKAGE_NAME,
								app.getApp_packagename());
						intent.setAction(StartSyncReceiver.ACTION); // 设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
						m_context.sendBroadcast(intent); // 发送广播
						
					} else {
						new AuroraAlertDialog.Builder(m_context)
						.setTitle(R.string.dialog_prompt)
						.setMessage(R.string.account_sync_turn_on_wlan_confirm)
						.setPositiveButton(R.string.dialog_confirm,
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// wlan_switch.setChecked(false);
								m_switch.performClick();
								SystemUtils.updateAppConfigInfo(m_context,
										app.getApp_packagename(), false);
								
								//add by zw 02-13 关闭联系人同步开关并选择确认按钮时
								if(app.getApp_packagename().equals("com.android.contacts"))
									new TotalCount(m_context, "280", "003", 1).CountData();
								else if(app.getApp_packagename().equals("com.android.mms"))
									new TotalCount(m_context, "280", "004", 1).CountData();	
								//end by zw
								if (position == ExtraFileUpService
										.getModule_index()) {
									ExtraFileUpService.stopCurrentModule(m_context);
								}
							}
						}).setNegativeButton(R.string.dialog_cancel, null)
						.create().show();
					}
					
				} else {
					
					if (!m_switch.isChecked()) {
						m_switch.performClick(); // setCheck没有音效和动画效果
						SystemUtils.updateAppConfigInfo(m_context,
								app.getApp_packagename(), true);
					} else {
						m_switch.performClick();
						SystemUtils.updateAppConfigInfo(m_context,
								app.getApp_packagename(), false);
					}
					
					CommonUtil.checkAndSetAppBackupAlarm();
				}
				
			}
		});
			
		int strId = R.string.account_sync_time;
		if (app.isApp_syncself()) {
			strId = R.string.account_backup_time;
		}
		
		String time = "";
		long timeL = app.getSyncTime();
		if (timeL == 0) {
			time = m_context.getString(strId,
					TimeUtils.getDistanceTime(String.valueOf(timeL)));
		} else {
			time = m_context.getString(strId, TimeUtils
					.getDistanceTime(TimeUtils.getDateTimeFromLong(timeL)));
		}
		holder.tv_sync_time.setText(time);
		
		// 隐藏显示title
		// 是否应用自身完成同步
		if (!app.isApp_syncself()) {
			holder.rl_title.setVisibility(View.GONE);
		} else {
			if (position <= 0) {
				holder.rl_title.setVisibility(View.GONE);
			} else {
				if (!listData.get(position - 1).isApp_syncself()) {
					holder.rl_title.setVisibility(View.VISIBLE);
					holder.tv_titlebar_text.setText(m_context.getString(R.string.account_backup_title));
				} else {
					holder.rl_title.setVisibility(View.GONE);
				}
			}
		}

		/*holder.tv_switch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						Log.i(TAG, "zhangwei the bl =" + isChecked
								+ " the position=" + position);
						List<AppConfigInfo> info = SystemUtils
								.getAppConfigInfo(m_context);

						for (int i = 0, size = info.size(); i < size; i++) {
							if (info.get(i).getApp_packagename()
									.equals(app.getApp_packagename())) {
								if (info.get(i).isSync() == isChecked)
									return;
								else
									break;
							}
						}

						SystemUtils.updateAppConfigInfo(m_context,
								app.getApp_packagename(), isChecked);
						if ((!isChecked)
								&& (position == ExtraFileUpService
										.getModule_index())) {
							ExtraFileUpService.stopCurrentModule(m_context);
						}
						if (isChecked) {
							Intent intent = new Intent(); // Itent就是我们要发送的内容
							intent.putExtra(
									StartSyncReceiver.PARAMS_PACKAGE_NAME,
									app.getApp_packagename());
							intent.setAction(StartSyncReceiver.ACTION); // 设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
							m_context.sendBroadcast(intent); // 发送广播
						}

					}
				});*/

		// =================初始化进度状态================= //
		int module_index = ExtraFileUpService.getModule_index();

		List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(m_context);
		AppConfigInfo appConfigInfo = apps.get(position);
		
		// 是否应用自身完成同步
		if (!appConfigInfo.isApp_syncself()) {
			holder.spv_app.setVisibility(View.VISIBLE);
			holder.iv_icon.setVisibility(View.GONE);
			
			if (module_index == position
					&& appConfigInfo.isSync()
					&& ExtraFileUpService.getSyncStatus() != SyncStatus.SYNC_STATUS_DONE
					&& ExtraFileUpService.getSyncStatus() != SyncStatus.SYNC_STATUS_NOT_STARTED) { // 当前同步模块

				int status = holder.spv_app.getStatus();
				if (status == SyncProgressView.STATUS_NORMAL) {
					holder.spv_app.setStatus(SyncProgressView.STATUS_PROGRESS);
				}

				int currentIndex = 0;
				int totalIndex = 0;
				if (ExtraFileUpService.getSync_type() == 0) {
					currentIndex = ExtraFileUpService.getM_currentcount();
				} else {
					currentIndex = ExtraFileUpService.getM_currentcount()
							+ ExtraFileUpService.getM_uptotalcount();
				}
				totalIndex = ExtraFileUpService.getM_downtotalcount()
						+ ExtraFileUpService.getM_uptotalcount();
				int progress = 0;
				if (totalIndex != 0) {
					progress = currentIndex * 100 / totalIndex;
				}
				Log.i(TAG, "zhangwei the progress=" + progress
						+ " the currentIndex=" + currentIndex + " totalIndex ="
						+ totalIndex);
//				holder.spv_app.setProgressAnim(progress);
				holder.spv_app.setProgress(progress);

			} else { // 非当前同步模块
//				if (appConfigInfo.isSync()) {
//					int status = holder.spv_app.getStatus();
//					
//					holder.spv_app.endProgress();
//				} else {
//					holder.spv_app.setStatus(SyncProgressView.STATUS_NORMAL);
//				}
//				// int status = spv_app.getStatus();
//				// if (status == SyncProgressView.STATUS_PROGRESS) {
//				holder.spv_app.endProgress();
//				// }
				
				holder.spv_app.setStatus(SyncProgressView.STATUS_NORMAL);
			}
		} else {
			holder.spv_app.setVisibility(View.GONE);
			holder.iv_icon.setVisibility(View.VISIBLE);
		}

		
		// =================初始化进度状态=================//

		return convertView;

	}

	static final class Holder {
		TextView tv_name;
		TextView tv_warn;
		AuroraSwitch as_switch;
		SyncProgressView spv_app;
		TextView tv_sync_time;
		View tv_module_switch;
		ImageView iv_icon;
		RelativeLayout rl_title;
		TextView tv_titlebar_text;
	}
	
	private void handleWlanSwitch(final AuroraSwitch v) {
		if (!v.isChecked()) {
			// wlan_switch.setChecked(true);
			v.performClick(); // setCheck没有音效和动画效果
			
			
			
			
		} else {
			new AuroraAlertDialog.Builder(m_context)
					.setTitle(R.string.dialog_prompt)
					.setMessage(R.string.account_sync_turn_off_wlan_confirm)
					.setPositiveButton(R.string.dialog_confirm,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// wlan_switch.setChecked(false);
									v.performClick();
								}
							}).setNegativeButton(R.string.dialog_cancel, null)
					.create().show();
		}
	}


}
