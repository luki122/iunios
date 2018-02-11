package com.netmanage.view;

import tmsdkobf.ed;

import com.aurora.netmanage.R;
import com.netmanage.utils.StringUtils;
import com.netmanage.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraEditText;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;

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
	
//	public static void showDialog(Activity activity,
//			int title,
//			int icon,
//			int msg,
//			int PositiBtnText,
//			OnClickListener PositiBtnLis,
//			int NegatiBtnText,
//			OnClickListener NegatiBtnLis,
//			OnKeyListener onKeyListener){
//		
//		if(activity == null || activity.isFinishing()){
//			return ;
//		}
//		
//		AlertDialog.Builder dialog =  new AlertDialog.Builder(activity);
//		dialog.setTitle(title);
//		dialog.setIconAttribute(icon);
//		dialog.setMessage(msg);
//		dialog.setPositiveButton(PositiBtnText,PositiBtnLis) ;	
//		dialog.setNegativeButton(NegatiBtnText,NegatiBtnLis);
//		dialog.setCancelable(true);	  
//		Dialog mDialog = dialog.create();		
//		if(mDialog != null && !mDialog.isShowing()){
//			mDialog.setCanceledOnTouchOutside(true);//点击屏幕外消失
//			mDialog.show();
//		}
//	}
	
	public static void showDialog(Activity activity,
			int title,
			int icon,
			int msg,
			int PositiBtnText,
			OnClickListener PositiBtnLis,
			int NegatiBtnText,
			OnClickListener NegatiBtnLis,
			OnKeyListener onKeyListener){
		
		if(activity == null || activity.isFinishing()){
			return ;
		}		
		AuroraAlertDialog.Builder builder = new Builder(activity);
		builder.setMessage(msg);
		builder.setTitle(title);
		builder.setPositiveButton(PositiBtnText,PositiBtnLis);
		builder.setNegativeButton(NegatiBtnText,NegatiBtnLis);
		builder.create().show();
	}
	
	/**
	 * 自定义带有输入框的弹出框
	 * @param activity
	 * @param title
	 * @param PositiBtnText
	 * @param PositiBtnCallBack 右按钮的回调函数
	 * @param NegatiBtnText
	 * @param NegatiBtnLis
	 * @param onKeyListener
	 */
	public static void showCustomEditDialog(final Activity activity,
			int title,
			int PositiBtnText,
			final EditDialogPosiBtnCallBack positiBtnCallBack,
			int NegatiBtnText){
		
		if(activity == null || activity.isFinishing()){
			return ;
		}		
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.flow_edit_view_of_dialog,null);	
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);
		builder.setView(layout);
		builder.setTitle(title);
		final AuroraEditText edit = (AuroraEditText)layout.findViewById(R.id.edit);
	
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
	

	
//	/**
//	 * 自定义带有输入框的弹出框
//	 * @param activity
//	 * @param title
//	 * @param PositiBtnText
//	 * @param PositiBtnCallBack 右按钮的回调函数
//	 * @param NegatiBtnText
//	 * @param NegatiBtnLis
//	 * @param onKeyListener
//	 */
//	public static void showCustomEditDialog(final Activity activity,
//			int title,
//			int PositiBtnText,
//			final EditDialogPosiBtnCallBack positiBtnCallBack,
//			int NegatiBtnText){
//		
//		if(activity == null || activity.isFinishing()){
//			return ;
//		}
//		
//		final Dialog dialog = new Dialog(activity,R.style.Dialog); 		
//		LayoutInflater inflater = (LayoutInflater) activity  
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		
//		View layout = inflater.inflate(R.layout.aurora_edit_dialog_internal, null); //设定dialog布局 	
//		((TextView)layout.findViewById(R.id.aurora_alertTitle)).setText(title); 	
//		final EditText edit = (EditText)layout.findViewById(R.id.edit);
//
//        if(positiBtnCallBack != null) {  
//        	Button leftBtn = ((Button)layout.findViewById(R.id.button1));  
//        	leftBtn.setText(PositiBtnText); 
//        	leftBtn.setOnClickListener(new View.OnClickListener() {  
//                public void onClick(View v) {
//                	String editText = ""+edit.getText();
//                	if(editText == null || editText.equals("")){
//                		showToast(activity,activity.getString(R.string.can_not_entry_empty)); 
//                	}else{
//                		Utils.hideSoftInput(activity, edit);
//                		positiBtnCallBack.onPositiBtn(editText); 
//                    	dialog.dismiss();                  	
//                	}              	
//                }  
//           }); 
//        } 
//        
//        Button rightBtn = ((Button)layout.findViewById(R.id.button2)); 
//    	rightBtn.setText(NegatiBtnText); 
//    	rightBtn.setOnClickListener(new View.OnClickListener() {  
//            public void onClick(View v) {  
//            	Utils.hideSoftInput(activity, edit);
//            	dialog.dismiss();    
//            }  
//        });  
//	    dialog.setContentView(layout); 
//	    dialog.setCancelable(true);    
//	    if(dialog != null && !dialog.isShowing()){
//	    	dialog.setCanceledOnTouchOutside(true);//点击屏幕外消失
//	    	dialog.show();
//		}
//	}
	
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
	
	private static final String SYS_AUTO_PRE = "sys_auto_sp";
	private static final String SYS_AUTO_SHOW_KEY = "sys_auto_show_key";
	
	public static void showSysAuthDialog(final Activity activity) {
		
		if (activity == null || activity.isFinishing()) {
			return;
		}
		
		final SharedPreferences sp = activity.getSharedPreferences(SYS_AUTO_PRE, 
				Activity.MODE_PRIVATE); 
		boolean showKey = sp.getBoolean(SYS_AUTO_SHOW_KEY, true);	// 是否显示提示框
		if (showKey) {
			LayoutInflater inflater = activity.getLayoutInflater();
			View layout = inflater.inflate(R.layout.alert_dialog_authority_confirm, null);
			final CheckBox cb = (CheckBox) layout.findViewById(R.id.checkbox);
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);
			builder.setView(layout);
			builder.setTitle(activity.getString(R.string.sys_auth_dlg_title));
			builder.setPositiveButton(activity.getString(R.string.sys_auth_dlg_continue), 
					new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					if (cb.isChecked()) {	// 不再提示
						SharedPreferences.Editor editor = sp.edit();
						editor.putBoolean(SYS_AUTO_SHOW_KEY, false);
						editor.commit();
					}
				}
			});
			builder.setNegativeButton(activity.getString(R.string.sys_auth_dlg_exit), 
					new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					activity.finish();
				}
			});
			AuroraAlertDialog dialog = builder.create();
			dialog.setCancelable(false);
			dialog.show();
		}
	}
	
}
