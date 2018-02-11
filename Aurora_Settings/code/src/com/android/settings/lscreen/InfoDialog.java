package com.android.settings.lscreen;

import com.android.settings.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraEditText;

public class InfoDialog {

	public static void showToast(Context context, String msg) {
		if (StringUtils.isEmpty(msg) || 
				context == null) {
			return;
		}
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public static void showToast(Context context, int msg) {
		if (msg == 0 || 
				context == null) {
			return;
		}
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		toast.show();
	}

	public static void showLongToast(Activity activity, String msg) {
		if (StringUtils.isEmpty(msg) || 
				activity == null || 
				activity.isFinishing()) {
			return;
		}
		Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
		toast.show();
	}
	
	public static void showDialog(Activity activity,
			int title,
			int msg,
			int PositiBtnText){
		if(activity == null || activity.isFinishing()){
			return ;
		}
		
		AuroraAlertDialog.Builder builder = new Builder(activity);
		builder.setMessage(msg);
		builder.setTitle(title);
		builder.setPositiveButton(PositiBtnText,null);
		try{
			builder.create().show();
		}catch(Exception e){
			e.printStackTrace();
		} 		
	}
	
	public static void showDialog(Activity activity,
			String title,
			String msg,
			String PositiBtnText){
		if(activity == null || activity.isFinishing()){
			return ;
		}
		
		AuroraAlertDialog.Builder builder = new Builder(activity);
		builder.setMessage(msg);
		builder.setTitle(title);
		builder.setPositiveButton(PositiBtnText,null);
		try{
			builder.create().show();
		}catch(Exception e){
			e.printStackTrace();
		} 
	}
	
	public static void showDialog(Activity activity,
			int title,
			int icon,
			int msg,
			int PositiBtnText,
			OnClickListener PositiBtnLis,
			int NegatiBtnText,
			OnClickListener NegatiBtnLis,
			OnKeyListener onKeyListener){
		
		showDialog(activity,
				title,
				icon,
				msg,
				PositiBtnText,
				PositiBtnLis,
				NegatiBtnText,
				NegatiBtnLis,
				onKeyListener,null);
	}
	
	public static void showDialog(Activity activity,
			int title,
			int icon,
			int msg,
			int PositiBtnText,
			OnClickListener PositiBtnLis,
			int NegatiBtnText,
			OnClickListener NegatiBtnLis,
			OnKeyListener onKeyListener,
			OnCancelListener onCancelListener){
		
		if(activity == null || activity.isFinishing()){
			return ;
		}
				
		AuroraAlertDialog.Builder builder = new Builder(activity);
		builder.setMessage(msg);
		builder.setTitle(title);
		builder.setPositiveButton(PositiBtnText,PositiBtnLis);
		builder.setNegativeButton(NegatiBtnText,NegatiBtnLis);
		builder.setOnCancelListener(onCancelListener);
		try{
			builder.create().show();
		}catch(Exception e){
			e.printStackTrace();
		} 
	}
	
	/**
	 * @param activity
	 * @param checkItem
	 * @param listener
	 */
	public static void showSingleChoiceDialog(Activity activity,
			int items,
			int checkItem,
			DialogInterface.OnClickListener listener){	
		if(activity == null || activity.isFinishing()){
			return ;
		}

		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);
		builder.setTitle(R.string.choice_sort_way);
		builder.setPositiveButton(R.string.cancel,null);
		builder.setTitleDividerVisible(true);	
		builder.setSingleChoiceItems(items,checkItem,listener);
	    try{
			builder.create().show();
		}catch(Exception e){
			e.printStackTrace();
		} 
	}
	
	/**
	 * 带有checkbox的对话框
	 * @param activity
	 * @param title
	 * @param icon
	 * @param msg
	 * @param checkboxText
	 * @param checkedChangeListener
	 * @param PositiBtnText
	 * @param PositiBtnLis
	 * @param NegatiBtnText
	 * @param NegatiBtnLis
	 * @param onKeyListener
	 */
	public static void showDialogWithCheckbox(Activity activity,
			int title,
			int icon,
			int msg,
//			int checkboxText,
			boolean isChecked,
//			OnCheckedChangeListener checkedChangeListener,
			int PositiBtnText,
			OnClickListener PositiBtnLis,
			int NegatiBtnText,
			OnClickListener NegatiBtnLis,
			OnKeyListener onKeyListener){
		
		if(activity == null || activity.isFinishing()){
			return ;
		}		

//		View layout = activity.getLayoutInflater().inflate(R.layout.dialog_content_of_checkbox,null);	
//		AuroraCheckBox checkBox = (AuroraCheckBox)layout.findViewById(R.id.checkBox);
//		checkBox.setText(checkboxText);
//		checkBox.setOnCheckedChangeListener(checkedChangeListener);
//		checkBox.setChecked(isChecked);
		
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);
//		builder.setView(layout);		
		builder.setMessage(msg);
		builder.setTitle(title);
		builder.setPositiveButton(PositiBtnText,PositiBtnLis);
		builder.setNegativeButton(NegatiBtnText,NegatiBtnLis);		
		try{
			builder.create().show();
		}catch(Exception e){
			e.printStackTrace();
		} 
	}
}
