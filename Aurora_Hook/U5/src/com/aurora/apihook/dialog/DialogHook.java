package com.aurora.apihook.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aurora.apihook.dialog.AuroraAlertController;
import com.android.internal.app.AlertController.AlertParams;
import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import com.aurora.utils.DensityUtil;


public class DialogHook implements Hook {

	private static final String TAG="DialogHook";
	private AuroraAlertController mAlertController;
	private AuroraAlertController.AuroraAlertParams mAlertParam;
	public DialogHook(){
		
	}
	
	public void after_AlertController(MethodHookParam param){
		Context context = (Context) param.args[0];
		DialogInterface di = (DialogInterface) param.args[1];
		Window window = (Window) param.args[2];
		initController(context, di, window);
	}
	
	private void initController(Context context, DialogInterface di, Window window){
		mAlertController = new AuroraAlertController(context, di, window);
		mAlertParam = new AuroraAlertController.AuroraAlertParams(context);
	}

	public void before_installContent(MethodHookParam param) {
		Log.e("alert", "before_installContent");
		param.setResult(null);
		if(mAlertController != null){
			mAlertController.installContent();
		}
	}

	/**
	 * CharSequence
	 * 
	 * @param title
	 */
	public void before_setTitle(MethodHookParam param) {
		param.setResult(null);
		CharSequence title = (CharSequence) param.args[0];
		if(mAlertController != null){
			mAlertController.setTitle(title);
		}
	}

	/**
	 * View
	 * 
	 * @see AlertDialog.Builder#setCustomTitle(View)
	 */
	public void before_setCustomTitle(MethodHookParam param) {
		param.setResult(null);
		View title = (View) param.args[0];
		if(mAlertController != null){
			mAlertController.setCustomTitle(title);
		}
	}

	/**
	 * CharSequence
	 * 
	 * @param message
	 */
	public void before_setMessage(MethodHookParam param) {
		param.setResult(null);
		CharSequence msg = (CharSequence) param.args[0];
		if(mAlertController != null){
			mAlertController.setMessage(msg);
		}
	}

	/**
	 * View Set the view to display in the dialog.
	 */
	public void before_setView(MethodHookParam param) {
		param.setResult(null);
		View view = (View) param.args[0];
		if(param.args.length > 1){
//			viewSpacingLeft, viewSpacingTop, viewSpacingRight,
			// viewSpacingBottom
			int viewSpacingLeft = (Integer) param.args[1];
			int viewSpacingTop = (Integer) param.args[2];
			int viewSpacingRight = (Integer) param.args[3];
			int viewSpacingBottom = (Integer) param.args[4];
			if(mAlertController != null){
				mAlertController.setView(view, viewSpacingLeft, viewSpacingTop, 
						viewSpacingRight, viewSpacingBottom);
			}
		}else{
			if(mAlertController != null){
				mAlertController.setView(view);
			}
		}
	}


	/**
	 * int  
	 * CharSequence 
	 * DialogInterface.OnClickListener
	 * Message 
	 * 
	 * @param whichButton
	 * @param text
	 * @param listener
	 * @param msg
	 */
	public void before_setButton(MethodHookParam param) {
		param.setResult(null);
		int witch = (Integer) param.args[0];
		CharSequence text = (CharSequence) param.args[1];
		DialogInterface.OnClickListener listener = (OnClickListener) param.args[2];
		Message msg = (Message) param.args[3];
		if(mAlertController != null){
			mAlertController.setButton(witch, text, listener, msg);
		}
	}

	/**
	 * int Set resId to 0 if you don't want an icon.
	 * 
	 * @param resId
	 *            the resourceId of the drawable to use as the icon or 0 if you
	 *            don't want an icon.
	 */
	public void before_setIcon(MethodHookParam param) {
		param.setResult(null);
		if(mAlertController != null){
		if(param.args[0] instanceof Drawable){
			Drawable icon = (Drawable) param.args[0];
		   mAlertController.setIcon(icon);
		}else if(param.args[0] instanceof Integer){
			int resId = (Integer) param.args[0];
				mAlertController.setIcon(resId);
			}
		}
		
	}

	/**
	 * boolean
	 * @param forceInverseBackground
	 */
	public void before_setInverseBackgroundForced(MethodHookParam param) {
		param.setResult(null);
		boolean forceInverseBackground = (Boolean) param.args[0];
		if(mAlertController != null){
			mAlertController.setInverseBackgroundForced(forceInverseBackground);
		}
	}

	public void before_getListView(MethodHookParam param) {
		if(mAlertController !=null){
			param.setResult(mAlertController.getListView());
		}
	}

	/**
	 * int
	 */
	public void before_getButton(MethodHookParam param) {
		int witch = (Integer) param.args[0];
		if(mAlertController != null){
			param.setResult(mAlertController.getButton(witch));
		}
	}

	/**
	 * int keyCode, KeyEvent event
	 * 
	 * @param keyCode
	 * @param event
	 */
	public void before_onKeyDown(MethodHookParam param) {
		int keyCode = (Integer) param.args[0];
		KeyEvent event = (KeyEvent) param.args[1];
		boolean result = false;
		if(mAlertController != null){
			result = mAlertController.onKeyDown(keyCode, event);
		}
		// return mScrollView != null && mScrollView.executeKeyEvent(event);
		param.setResult(result);
	}

	/**
	 * int keyCode, KeyEvent event
	 * 
	 * @param keyCode
	 * @param event
	 */
	public void before_onKeyUp(MethodHookParam param) {
		// return mScrollView != null && mScrollView.executeKeyEvent(event);
		
		int keyCode = (Integer) param.args[0];
		boolean result = false;
		KeyEvent event = (KeyEvent) param.args[1];
		if(mAlertController != null){
			result = mAlertController.onKeyDown(keyCode, event);
		}
		param.setResult(result);
	}
	
	public void before_apply(MethodHookParam param){
		param.setResult(null);
		copyData((com.android.internal.app.AlertController.AlertParams)param.thisObject);
	}
	
	private void copyData(com.android.internal.app.AlertController.AlertParams params){
		if(params != null ){
//          mAlertParam.mFromAlertActivity= params.auroraFromAlertActivity;
          mAlertParam.mAdapter= params.mAdapter;
          mAlertParam.mCancelable= params.mCancelable;
          mAlertParam.mCheckedItem= params.mCheckedItem;
          mAlertParam.mCheckedItems= params.mCheckedItems;
          mAlertParam.mCursor= params.mCursor;
          mAlertParam.mCustomTitleView= params.mCustomTitleView;
          mAlertParam.mForceInverseBackground= params.mForceInverseBackground;
          mAlertParam.mIcon= params.mIcon;
          mAlertParam.mIconAttrId= params.mIconAttrId;
          mAlertParam.mIconId= params.mIconId;
          mAlertParam.mIsCheckedColumn= params.mIsCheckedColumn;
          mAlertParam.mIsMultiChoice= params.mIsMultiChoice;
          mAlertParam.mIsSingleChoice= params.mIsSingleChoice;
          mAlertParam.mItems= params.mItems;
          mAlertParam.mLabelColumn= params.mLabelColumn;
          mAlertParam.mMessage= params.mMessage;
          mAlertParam.mNegativeButtonListener= params.mNegativeButtonListener;
          mAlertParam.mNegativeButtonText= params.mNegativeButtonText;
          mAlertParam.mNeutralButtonListener= params.mNeutralButtonListener;
          
          
          mAlertParam.mNeutralButtonText= params.mNeutralButtonText;
          mAlertParam.mOnCancelListener= params.mOnCancelListener;
          mAlertParam.mOnCheckboxClickListener= params.mOnCheckboxClickListener;
          
          
          mAlertParam.mOnClickListener= params.mOnClickListener;
          mAlertParam.mOnDismissListener= params.mOnDismissListener;
          mAlertParam.mOnItemSelectedListener= params.mOnItemSelectedListener;
          mAlertParam.mOnKeyListener= params.mOnKeyListener;
          
          mAlertParam.mOnPrepareListViewListener= params.mOnPrepareListViewListener;
          mAlertParam.mPositiveButtonListener= params.mPositiveButtonListener;
          mAlertParam.mPositiveButtonText= params.mPositiveButtonText;
          mAlertParam.mRecycleOnMeasure= params.mRecycleOnMeasure;
          mAlertParam.mTitle= params.mTitle;
          mAlertParam.mView= params.mView;
          
          mAlertParam.mViewSpacingBottom= params.mViewSpacingBottom;
          mAlertParam.mViewSpacingLeft= params.mViewSpacingLeft;
          mAlertParam.mViewSpacingRight= params.mViewSpacingRight;
          mAlertParam.mViewSpacingTop= params.mViewSpacingTop;
          mAlertParam.apply(mAlertController);
		}
	}
    
    


}
