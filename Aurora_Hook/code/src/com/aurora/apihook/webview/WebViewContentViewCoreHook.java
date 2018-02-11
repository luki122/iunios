package com.aurora.apihook.webview;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.Browser;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import android.view.View;
import android.view.WindowManager;
public class WebViewContentViewCoreHook  implements AuroraViewToolbar.OnItemClickListener{
	
	private static final String TAG = "WebViewHook";
	
	private AuroraViewToolbar mEditBar;
	
	private int mStartX,mStartY,mEndX,mEndY;
	
	private int mPositionX,mPositionY;
	
	private Object mContentViewCoreObj;
	
	private boolean mAboveCursor = true;
	
	private int mHandleHeight;
	
	private boolean mSelectionEditable = false;
	
	private boolean mSelectAll = false;
	
	public void after_onSelectionBoundsChanged(MethodHookParam params){
			 Object mSelectHandlerController = ClassHelper.callMethod(params.thisObject,"getSelectionHandleController");
			 Object mStartHandle = ClassHelper.getObjectField(mSelectHandlerController,"mStartHandle");
			 Object mEndHandle = ClassHelper.getObjectField(mSelectHandlerController,"mEndHandle");
		 	mStartX =ClassHelper.getIntField(mStartHandle,"mPositionX");// (Float)params.args[0];
			mStartY = ClassHelper.getIntField(mStartHandle,"mPositionY");
			mEndX =ClassHelper.getIntField(mEndHandle,"mPositionX");
			mEndY =ClassHelper.getIntField(mEndHandle,"mPositionY");
		if(mEditBar == null){
			View host = (View)ClassHelper.getObjectField(params.thisObject,"mContainerView");
			mEditBar = new AuroraViewToolbar(host);
			mEditBar.setOnItemClickListener(this);
			mEditBar.show((int)mStartX,(int)mStartY,true);
			Log.e(TAG, "WebViewHook0000");
		}
		
		getPosition(params);
		boolean hasSelection = ClassHelper.getBooleanField(params.thisObject,"mHasSelection");
		if(hasSelection){
			if(!mEditBar.isShowing()){
				mEditBar.show((int)mPositionX,(int)mPositionY,mAboveCursor);
				Log.e(TAG, "WebViewHook1111");
			}else{
				Log.e(TAG, "WebViewHook22222");
				mEditBar.move((int)mPositionX,(int)mPositionY,mAboveCursor);
			}
		}else{
			mEditBar.hide();
			Log.e(TAG, "WebViewHook3333");
		}
	}
	
	public void after_updateImeAdapter(MethodHookParam params){
		mSelectionEditable = ClassHelper.getBooleanField(params.thisObject,"mSelectionEditable");
	
		if(mEditBar != null){
			mEditBar.updateItem(mSelectionEditable);
		}
	}
	
	private void getPosition(MethodHookParam params){
		if(mEditBar == null){
			return;
		}
		int lineHeight = getLineHeight(params);
		int editbarWidth = mEditBar.getWidth();
		int editbarHeight = mEditBar.getHeight();
		int positionOffset = (int)(editbarHeight*1.5f) ;
		int screenWidth = getScreenWidth(mEditBar.getContext());
		mStartY+=lineHeight;
		mEndY+=lineHeight;
		if(mStartY == mEndY){
			mPositionY = mStartY - positionOffset;
			mAboveCursor = true;
			
			if(mEndX - mStartX>mEditBar.getWidth()){
				mPositionX = (mEndX - mStartX)/2 - mEditBar.getWidth()/2;
			}else{
				mPositionX = mStartX;
			}
			if(mPositionY<editbarHeight){
				Log.e(TAG, "single line and mPositionY<editbarHeight mStartY:"+mStartY);
				mPositionY = mStartY+positionOffset/*+ positionOffset*/;
				mAboveCursor = false;
			}
			
			if(getScreenHeight(mEditBar.getContext()) - mPositionY < editbarHeight){
				mPositionY = getScreenHeight(mEditBar.getContext()) - editbarHeight;
				mAboveCursor = true;
			}
			Log.e(TAG, "WebViewHook4444");
			
		}else{
			mPositionX =screenWidth/2 - mEditBar.getWidth()/2;
			if(mEndY - mStartY > editbarHeight*3){
				mPositionY = (mEndY - mStartY)/2+mStartY;// - editbarHeight/2;
				mAboveCursor = true;
			}else{
				if(mStartY > editbarHeight){
					mPositionY = mStartY- positionOffset;
					mAboveCursor = true;
				}else{
					mPositionY = mEndY+ positionOffset;
					mAboveCursor = false;
				}
			}
//			if(mPositionY<editbarHeight){
//				mPositionY = editbarHeight- positionOffset;
//				mAboveCursor = false;
//				Log.e("big", "result2:"+mPositionY);
//			}
			
			if(getScreenHeight(mEditBar.getContext()) - mPositionY < editbarHeight){
				mPositionY = getScreenHeight(mEditBar.getContext()) - editbarHeight;
				mAboveCursor = true;
			}
		}
		if(mPositionX < 0){
			mPositionX = 0;
		}
		int result =(int)(getScreenWidth(mEditBar.getContext())-mPositionX);
		
		boolean outOfRange = result < editbarWidth;
		if(outOfRange){
			mPositionX = getScreenWidth(mEditBar.getContext()) - editbarWidth;
		}
		
		if(mEndY < editbarHeight){
			mPositionY = mEndY+positionOffset  ;
			mAboveCursor = false;
		}
		
	}
	
	 public void before_onTouchEvent(MethodHookParam param) {

		 MotionEvent event = (MotionEvent)param.args[0];
			int action = event.getActionMasked();
			
			
	 
	 }
	
	public void after_updateHandleScreenPositions(MethodHookParam param){
		
	}
	
	
	private void before_hideHandles(MethodHookParam param) {
		
	}
	
	public void before_showSelectActionBar(MethodHookParam param){
		mContentViewCoreObj = param.thisObject;
		
	}
	
	private void unSelect(Object obj){
		if(obj == null)return;
		long mNativeImeAdapterAndroid = ClassHelper.getLongField(obj,"mNativeImeAdapterAndroid");
		 if (mNativeImeAdapterAndroid == 0){ 
			 return;
		 }
		 ClassHelper.callMethod(obj,"nativeUnselect",mNativeImeAdapterAndroid);//nativeUnselect(mNativeImeAdapterAndroid);
	}
	
	@Override
	public void onItemAction(int id){

		if(mContentViewCoreObj != null){
			Context mContext = (Context)ClassHelper.getObjectField(mContentViewCoreObj,"mContext");
			Object mImeAdapter = ClassHelper.getObjectField(mContentViewCoreObj,"mImeAdapter");
			if(mImeAdapter != null){
				Object mSelectHandleObj = ClassHelper.callMethod(mContentViewCoreObj,"getSelectionHandleController");
				
				switch (id) {
				case AuroraViewToolbar.ID_SELECT_ALL:{
					ClassHelper.callMethod(mImeAdapter,"selectAll");
					break;
				}
				case AuroraViewToolbar.ID_COPY:{
					ClassHelper.callMethod(mImeAdapter,"copy");
					if(mSelectHandleObj != null){
						ClassHelper.callMethod(mSelectHandleObj,"hide");
					}
					unSelect(mImeAdapter);
					break;
				}
				case AuroraViewToolbar.ID_SEARCH:{
					if(mSelectHandleObj != null){
						ClassHelper.callMethod(mSelectHandleObj,"hide");
						
					}
					final String query = (String)ClassHelper.getObjectField(mContentViewCoreObj,"mLastSelectedText");//getSelectedText();
	                if (TextUtils.isEmpty(query)) return;


	                Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
	                i.putExtra(SearchManager.EXTRA_NEW_SEARCH, true);
	                i.putExtra(SearchManager.QUERY, query);
	                i.putExtra(Browser.EXTRA_APPLICATION_ID,mContext.getPackageName());
	                if (!(mContext instanceof Activity)) {
	                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                }
	                try {
	                	mContext.startActivity(i);
	                } catch (android.content.ActivityNotFoundException ex) {
	                    // If no app handles it, do nothing.
	                }
	                unSelect(mImeAdapter);
					break;
				}
				case AuroraViewToolbar.ID_SHARE:{
					if(mSelectHandleObj != null){
						ClassHelper.callMethod(mSelectHandleObj,"hide");
					}
					final String query = (String)ClassHelper.getObjectField(mContentViewCoreObj,"mLastSelectedText");//getSelectedText();
	                if (TextUtils.isEmpty(query)) return;

	                Intent send = new Intent(Intent.ACTION_SEND);
	                send.setType("text/plain");
	                send.putExtra(Intent.EXTRA_TEXT, query);
	                try {
	                    Intent i = Intent.createChooser(send, mContext.getString(
	                            com.aurora.R.string.aurora_share));
	                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                    mContext.startActivity(i);
	                } catch (android.content.ActivityNotFoundException ex) {
	                    // If no app handles it, do nothing.
	                }
	                unSelect(mImeAdapter);
					break;
				}
				case AuroraViewToolbar.ID_CUT:{
					ClassHelper.callMethod(mImeAdapter,"cut");
					if(mSelectHandleObj != null){
						ClassHelper.callMethod(mSelectHandleObj,"hide");
					}
					unSelect(mImeAdapter);
					break;
				}
				}
			}
		}
		
	}
	
	private int getScreenWidth(Context context){
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
       return wm.getDefaultDisplay().getWidth();
	}
	
	private int getScreenHeight(Context context){
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight();
	}

	private int getLineHeight(MethodHookParam param) {
		Object coordinateObj = ClassHelper.getObjectField(param.thisObject,"mRenderCoordinates");
		float height = (Float) ClassHelper.callMethod(coordinateObj,"fromLocalCssToPix", 14);
		return (int) Math.ceil(height);
	}
	
	
}
