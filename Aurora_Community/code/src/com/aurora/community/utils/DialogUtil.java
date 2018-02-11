package com.aurora.community.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.aurora.community.R;

public class DialogUtil {

	public static Dialog getLoadingDialog(Context context, String message) {
		Dialog loadingDialog = new Dialog(context, R.style.dialog_style);

		View view = LayoutInflater.from(context).inflate(
				R.layout.loading_alert_layout, null);
		view.setVisibility(View.VISIBLE);
		TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
		tv_msg.setText(message);
		loadingDialog.setContentView(view);
		loadingDialog.getWindow().setGravity(Gravity.CENTER);
		loadingDialog.getWindow().setBackgroundDrawableResource(
				R.drawable.action_bar_transparent);
		loadingDialog.setCanceledOnTouchOutside(false);
		return loadingDialog;
	}

	public static Dialog getAlertDialog(Context context, String message,
			int cancelId, int sureId, final IAlertDialogClick alertDialogClick) {
		final Dialog alertDialog = new Dialog(context, R.style.dialog_style);

		View view = LayoutInflater.from(context).inflate(
				R.layout.alertdialog_layout, null);
		TextView aurora_alertTitle = (TextView) view
				.findViewById(R.id.aurora_alertTitle);
		TextView dialog_alert_message = (TextView) view
				.findViewById(R.id.dialog_alert_message);
		dialog_alert_message.setText(message);
		Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		btn_ok.setText(sureId);
		btn_cancel.setText(cancelId);

		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (alertDialogClick != null) {
					alertDialogClick.sureClick();
				}
				alertDialog.dismiss();
			}
		});

		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (alertDialogClick != null) {
					alertDialogClick.cancelClick();
				}
				alertDialog.dismiss();
			}
		});

		alertDialog.setContentView(view);
		alertDialog.getWindow().setGravity(Gravity.CENTER);
		alertDialog.getWindow().setBackgroundDrawableResource(
				R.drawable.action_bar_transparent);
		alertDialog.setCanceledOnTouchOutside(false);
		WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
		lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		return alertDialog;
	}

	public static Dialog getItemAlertDialog(Context context, int titleId, 
			int itemId, final IAlertDialogItemClick alertDialogItemClick) {
		final Dialog alertDialog = new Dialog(context, R.style.dialog_style);

		View view = LayoutInflater.from(context).inflate(
				R.layout.alertdialog_list_layout, null);
		TextView aurora_alertTitle = (TextView) view
				.findViewById(R.id.aurora_alertTitle);
		aurora_alertTitle.setText(context.getString(titleId));
		TextView dialog_alert_message = (TextView) view
				.findViewById(R.id.dialog_alert_message);
		dialog_alert_message.setVisibility(View.GONE);
		
		ListView lv = (ListView) view.findViewById(R.id.dialog_alert_list);
		lv.setVisibility(View.VISIBLE);
		
		String[] array = context.getResources().getStringArray(itemId);
		ItemListAdapter mAdapter = new ItemListAdapter(context, array);
		lv.setAdapter(mAdapter);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				if (alertDialogItemClick != null) {
					alertDialogItemClick.onClick(arg2);
				}
				
				alertDialog.dismiss();
			}
		});

		alertDialog.setContentView(view);
		alertDialog.getWindow().setGravity(Gravity.CENTER);
		alertDialog.getWindow().setBackgroundDrawableResource(
				R.drawable.action_bar_transparent);
		alertDialog.setCanceledOnTouchOutside(true);
		WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
		lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		return alertDialog;
	}
	
	public static Dialog getPromptDialog(Context context,int promptMessage){
        final Dialog alertDialog = new Dialog(context,R.style.dialog_style);
		View view = LayoutInflater.from(context).inflate(R.layout.tipdialog_layout, null);
		TextView aurora_alertTitle = (TextView) view.findViewById(R.id.aurora_alertTitle);
		TextView dialog_alert_message = (TextView) view.findViewById(R.id.dialog_alert_message);
		dialog_alert_message.setText(promptMessage);
		view.findViewById(R.id.btn_sure).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
			}
		});
		alertDialog.setContentView(view);
		alertDialog.getWindow().setGravity(Gravity.CENTER);
		alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.action_bar_transparent);
		alertDialog.setCanceledOnTouchOutside(false);
		WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
		lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		return alertDialog;
	}
	
	
	private static class ItemListAdapter extends BaseAdapter {
		
		Context mContext = null;
		String[] list = null;
		
		public ItemListAdapter(Context context, String[] list) {
			mContext = context;
			this.list = list;
		}

		@Override
		public int getCount() {
			return list == null ? 0 : list.length;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			Holder holder = null;
			
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_alert_dialog, null);
				holder = new Holder();
				holder.tv = (TextView) convertView.findViewById(R.id.text);
				holder.ll = (LinearLayout) convertView.findViewById(R.id.ll);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			
			holder.tv.setText(list[position]);
			
//			if (position == list.length - 1) {
//				holder.ll.setBackgroundResource(R.drawable.aurora_alert_dialog_btn_selector_middle);
//			}
			
			return convertView;
		}
		
		private class Holder {
			LinearLayout ll;
			TextView tv;
		}
		
	}

	public static interface IAlertDialogClick {
		void sureClick();

		void cancelClick();
	}
	
	public static interface IAlertDialogItemClick {
		public void onClick(int which);
	}

}
