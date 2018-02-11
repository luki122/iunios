package com.aurora.iunivoice.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.widget.app.AuroraAlertDialog;
import com.aurora.iunivoice.widget.app.AuroraProgressDialog;

public class DialogUtil {

	public static Dialog getProgressDialog(Context context, String title,
			String message, String btnStr,
			final IAlertDialogItemClick alertDialogItemClick, boolean isShow) {
		AuroraProgressDialog progressDialog = null;
		progressDialog = new AuroraProgressDialog(context);
		progressDialog.setTitle(title);
		progressDialog.setMessage(message);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(100);
		// auroraProgressDialog.setProgress(value);
		if (isShow) {

			progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, btnStr,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							alertDialogItemClick.onClick(which);
						}
					});

		}
		progressDialog.setCancelable(false);

		return progressDialog;
	}

	public static Dialog getLoadingDialog(Context context, String message) {
		AuroraProgressDialog loadingDialog = new AuroraProgressDialog(context);
		loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		loadingDialog.setMessage(message);
		// View view = LayoutInflater.from(context).inflate(
		// R.layout.alertdialog_loading_layout, null);
		// view.setVisibility(View.VISIBLE);
		// TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
		// tv_msg.setText(message);
		// loadingDialog.setView(view);
		// loadingDialog.setContentView(view);
		// loadingDialog.getWindow().setGravity(Gravity.CENTER);
		// loadingDialog.getWindow().setBackgroundDrawableResource(
		// R.drawable.action_bar_transparent);
		loadingDialog.setCanceledOnTouchOutside(false);
		return loadingDialog;
	}

	public static Dialog getAlertDialog(Context context, String message,
			int cancelId, int sureId, final IAlertDialogClick alertDialogClick) {
		// final Dialog alertDialog = new Dialog(context, R.style.dialog_style);
		//
		// View view = LayoutInflater.from(context).inflate(
		// R.layout.alertdialog_layout, null);
		// TextView aurora_alertTitle = (TextView) view
		// .findViewById(R.id.aurora_alertTitle);
		// TextView dialog_alert_message = (TextView) view
		// .findViewById(R.id.dialog_alert_message);
		// dialog_alert_message.setText(message);
		// Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		// Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		// btn_ok.setText(sureId);
		// btn_cancel.setText(cancelId);
		//
		// btn_ok.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// if (alertDialogClick != null) {
		// alertDialogClick.sureClick();
		// }
		// alertDialog.dismiss();
		// }
		// });
		//
		// btn_cancel.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// if (alertDialogClick != null) {
		// alertDialogClick.cancelClick();
		// }
		// alertDialog.dismiss();
		// }
		// });
		// alertDialog.setContentView(view);
		// alertDialog.getWindow().setGravity(Gravity.CENTER);
		// alertDialog.getWindow().setBackgroundDrawableResource(
		// R.drawable.action_bar_transparent);
		// alertDialog.setCanceledOnTouchOutside(false);
		// WindowManager.LayoutParams lp =
		// alertDialog.getWindow().getAttributes();
		// lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		// lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		AuroraAlertDialog alertDialog = new AuroraAlertDialog.Builder(context)
				.setMessage(message)
				.setNegativeButton(context.getText(cancelId),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (alertDialogClick != null) {
									alertDialogClick.cancelClick();
								}
								dialog.dismiss();
							}
						})
				.setPositiveButton(context.getText(sureId),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (alertDialogClick != null) {
									alertDialogClick.sureClick();
								}
								dialog.dismiss();
							}
						}).create();
		return alertDialog;
	}

	public static Dialog getItemAlertDialog(Context context, int titleId,
			int itemId, final IAlertDialogItemClick alertDialogItemClick) {
		// final Dialog alertDialog = new Dialog(context, R.style.dialog_style);
		//
		// View view = LayoutInflater.from(context).inflate(
		// R.layout.alertdialog_list_layout, null);
		// TextView aurora_alertTitle = (TextView) view
		// .findViewById(R.id.aurora_alertTitle);
		// aurora_alertTitle.setText(context.getString(titleId));
		// TextView dialog_alert_message = (TextView) view
		// .findViewById(R.id.dialog_alert_message);
		// dialog_alert_message.setVisibility(View.GONE);
		//
		// ListView lv = (ListView) view.findViewById(R.id.dialog_alert_list);
		// lv.setVisibility(View.VISIBLE);
		//
		// String[] array = context.getResources().getStringArray(itemId);
		// ItemListAdapter mAdapter = new ItemListAdapter(context, array);
		// lv.setAdapter(mAdapter);
		//
		// lv.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		//
		// if (alertDialogItemClick != null) {
		// alertDialogItemClick.onClick(arg2);
		// }
		//
		// alertDialog.dismiss();
		// }
		// });
		//
		// alertDialog.setContentView(view);
		// alertDialog.getWindow().setGravity(Gravity.CENTER);
		// alertDialog.getWindow().setBackgroundDrawableResource(
		// R.drawable.action_bar_transparent);
		// alertDialog.setCanceledOnTouchOutside(true);
		// WindowManager.LayoutParams lp =
		// alertDialog.getWindow().getAttributes();
		// lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		// lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		AuroraAlertDialog alertDialog = new AuroraAlertDialog.Builder(context)
				.setTitle(context.getText(titleId))
				.setItems(itemId, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (alertDialogItemClick != null) {
							alertDialogItemClick.onClick(which);
						}
					}
				}).create();

		return alertDialog;
	}

	public static Dialog getPromptDialog(Context context, int promptMessage) {
		// final Dialog alertDialog = new Dialog(context, R.style.dialog_style);
		// View view = LayoutInflater.from(context).inflate(
		// R.layout.alertdialog_tip_layout, null);
		// TextView aurora_alertTitle = (TextView) view
		// .findViewById(R.id.aurora_alertTitle);
		// TextView dialog_alert_message = (TextView) view
		// .findViewById(R.id.dialog_alert_message);
		// dialog_alert_message.setText(promptMessage);
		// view.findViewById(R.id.btn_sure).setOnClickListener(
		// new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// alertDialog.dismiss();
		// }
		// });
		// alertDialog.setContentView(view);
		// alertDialog.getWindow().setGravity(Gravity.CENTER);
		// alertDialog.getWindow().setBackgroundDrawableResource(
		// R.drawable.action_bar_transparent);
		// alertDialog.setCanceledOnTouchOutside(false);
		// WindowManager.LayoutParams lp =
		// alertDialog.getWindow().getAttributes();
		// lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		// lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		AuroraAlertDialog alertDialog = new AuroraAlertDialog.Builder(context)
				.setMessage(context.getText(promptMessage))
				.setPositiveButton(R.string.dialog_confirm, null).create();

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
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.alertdialog_item_layout, null);
				holder = new Holder();
				holder.tv = (TextView) convertView.findViewById(R.id.text);
				holder.ll = (LinearLayout) convertView.findViewById(R.id.ll);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}

			holder.tv.setText(list[position]);

			// if (position == list.length - 1) {
			// holder.ll.setBackgroundResource(R.drawable.aurora_alert_dialog_btn_selector_middle);
			// }

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
