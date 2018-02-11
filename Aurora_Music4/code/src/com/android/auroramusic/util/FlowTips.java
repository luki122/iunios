package com.android.auroramusic.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraSwitch;

import com.android.music.MusicUtils;
import com.android.music.R;

public class FlowTips {

	private static final String WIFI_STATE_ETR = "wifi_state";

	public interface OndialogClickListener{
		public void OndialogClick();
	}
	
	public static boolean showPlayFlowTips(final Context context,final OndialogClickListener l) {
		if(!isWifiOpen(context)||!AuroraMusicUtil.isGprsNetActive(context)||!Globals.SWITCH_FOR_ONLINE_MUSIC){
			return false;
		}
		
		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(context)
				.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(l!=null){
							l.OndialogClick();
						}
						closeWifiState(context);
						dialog.dismiss();
					}
				}).setTitleDividerVisible(true)
				.setTitle(R.string.aurora_flow_tips)
				.setMessage(R.string.aurora_continue_play_tips).create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
		return true;
	}

	public static boolean showDownloadFlowTips(final Context context,final OndialogClickListener l) {
		
		if(!isWifiOpen(context)||!AuroraMusicUtil.isGprsNetActive(context)){
			return false;
		}
		
		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(context)
				.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(l!=null){
							l.OndialogClick();
						}
						closeWifiState(context);
						dialog.dismiss();
					}
				}).setTitleDividerVisible(true)
				.setTitle(R.string.aurora_flow_tips)
				.setMessage(R.string.aurora_continue_download_tips).create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		return true;
	}

	public static void showWifiSwitch(final Context context) {
		View layout = LayoutInflater.from(context).inflate(
				R.layout.aurora_net_setting, null);
		AuroraSwitch switch1 = (AuroraSwitch) layout
				.findViewById(R.id.aurora_close_wifi_switch);
		switch1.setChecked(isWifiOpen(context));
		switch1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton compoundbutton,
					boolean flag) {

				if (flag) {
					openWifiState(context);
				} else {
					closeWifiState(context);
				}
			}
		});
		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(context)
				.setTitleDividerVisible(true)
				.setTitle(R.string.aurora_net_setting).setView(layout).create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	private static void closeWifiState(Context context) {
		MusicUtils.setIntPref(context, WIFI_STATE_ETR, 0);
	}

	private static void openWifiState(Context context) {
		MusicUtils.setIntPref(context, WIFI_STATE_ETR, 1);
	}

	private static boolean isWifiOpen(Context context) {

		return MusicUtils.getIntPref(context, WIFI_STATE_ETR, 1) == 1 ? true
				: false;
	}
}
