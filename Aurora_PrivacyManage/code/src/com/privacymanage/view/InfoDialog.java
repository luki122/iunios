package com.privacymanage.view;

import com.aurora.privacymanage.R;
import com.privacymanage.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraEditText;

public class InfoDialog {
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
			int checkboxText,
			OnCheckedChangeListener checkedChangeListener,
			int PositiBtnText,
			OnClickListener PositiBtnLis,
			int NegatiBtnText,
			OnClickListener NegatiBtnLis,
			OnKeyListener onKeyListener){
		
		if(activity == null || activity.isFinishing()){
			return ;
		}		

		View layout = activity.getLayoutInflater().inflate(R.layout.dialog_content_of_checkbox,null);	
		AuroraCheckBox checkBox = (AuroraCheckBox)layout.findViewById(R.id.checkBox);
		checkBox.setText(checkboxText);
		checkBox.setOnCheckedChangeListener(checkedChangeListener);
		
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);
		builder.setView(layout);		
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
	
	/**
	 * 自定义带有输入框的弹出框
	 * @param activity
	 * @param title
	 * @param editHintText
	 * @param PositiBtnText
	 * @param positiBtnCallBack
	 * @param NegatiBtnText
	 */
	public static void showCustomEditDialog(final Activity activity,
			int title,
			String editHintText,
			int PositiBtnText,
			final EditDialogPosiBtnCallBack positiBtnCallBack,
			int NegatiBtnText,
			int maxLength){
		
		if(activity == null || activity.isFinishing()){
			return ;
		}		
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_content_of_edit,null);	
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);
		builder.setView(layout);
		builder.setTitle(title);
		final AuroraEditText edit = (AuroraEditText)layout.findViewById(R.id.edit);
		edit.setHint(""+editHintText);
		if(maxLength>-1){
		    InputFilter[] filters = {new InputFilter.LengthFilter(maxLength)};  
		    edit.setFilters(filters);  
		}

		builder.setPositiveButton(PositiBtnText,
				new OnClickListener() {  
	          public void onClick(DialogInterface dialog, int which){
	          	String editText = ""+edit.getText();
	          	if(editText == null || editText.equals("")){
	          		//showToast(activity,activity.getString(R.string.can_not_entry_empty)); 
	          	}else{
	          		positiBtnCallBack.onPositiBtn(editText);                 	
	          	}              	
	          }  
	     });
		
		builder.setNegativeButton(NegatiBtnText,
				new OnClickListener() {  
			@Override
			public void onClick(DialogInterface dialog, int which) {
	        	dialog.dismiss();  
			}  
	    });
		AuroraAlertDialog dialog = builder.create();
		// show softinput
		dialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				if (edit.hasFocus()) {
					Utils.showSoftInput(activity, edit);
				}
			}
		});
		dialog.show();
	}
	
	/**
	 * 带有"编辑框的对话框",PosiBtn按下时的回调接口
	 * @author Administrator
	 *
	 */
	public interface EditDialogPosiBtnCallBack {
		/**
		 * PosiBtn按下
		 * @param editStr
		 */
		public void onPositiBtn(String editStr);
	}
}
