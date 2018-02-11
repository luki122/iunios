package com.android.packageinstaller;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.TaskProcessListener;
import tmsdk.fg.module.deepclean.rubbish.SdcardScanResultHolder;
import tmsdk.fg.module.deepclean.rubbish.SoftwareCacheModel;
import com.android.data.MyArrayList;
import com.android.utils.FileUtils;
import com.android.packageinstaller.R;

/**
 * 卸载应用后，深度清理模块
 */
public class DeepClearModel{
    static Object sGlobalLock = new Object();
    static DeepClearModel sInstance;
    private Context mApplicationContext;
    private final String TAG = DeepClearModel.class.getName();
    private final Object mLock = new Object();
    private MyArrayList<String> appNameList = new MyArrayList<String>();
    private UIHandler mUIhandler;
    private final HandlerThread mBackgroundThread;
    private final BackgroundHandler mBackgroundHandler;     
    private DeepcleanManager mDeepcleanManager;
    private boolean isInitSucess = false;
    //Aurora <shihao> <2015-03-12> for crash when uninstall app bug 11462 10448 begin
    private static boolean isScanOver = true;
    private static boolean canRestartScan = false;
    //Aurora <shihao> <2015-03-12> for crash when uninstall app bug 11462 10448 end
       
    static public DeepClearModel getInstance(Context context) {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new DeepClearModel(context);
            }
            return sInstance;
        }
    }

    private DeepClearModel(Context context) {	
        mApplicationContext = context.getApplicationContext();
        mUIhandler = new UIHandler(Looper.getMainLooper());
        mBackgroundThread = new HandlerThread(TAG+":Background");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());    
        
        mDeepcleanManager = ManagerCreatorF.getManager(DeepcleanManager.class);
		if(!mDeepcleanManager.init(mListener)) {
			Log.i(TAG,"init error");
			isInitSucess = false;
		}else{
			isInitSucess = true;
		}
    }
	
	/**
	 * 删除一个应用
	 * @param appName
	 */
	public void scanForUnInstall(String appName){
		if(appName == null || !isInitSucess){
			return ;
		}
		synchronized (mLock){
			appNameList.add(appName);	
			//Aurora <shihao> <2015-03-12> for crash when uninstall app bug 11462 10448 begin
//			mBackgroundHandler.sendEmptyMessage(0);
			Log.e(TAG,"scanForUnInstall appName="+appName + "  " +"isScanOver =="+isScanOver+ "  " + " canRestartScan=="+canRestartScan);
			if(isScanOver)
				mBackgroundHandler.sendEmptyMessage(0);
			else
				canRestartScan = true;
			 //Aurora <shihao> <2015-03-12> for crash when uninstall app bug 11462 10448 end
		}
	}
		
    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {       	
        	mDeepcleanManager.startScan();
	    };
    }
    
	TaskProcessListener mListener =  new TaskProcessListener() {		
		@Override
		public void onScanStart() {
			isScanOver = false;   //Aurora <shihao> <2015-03-12> for crash when uninstall app bug 11462 10448 
			Log.i(TAG, "【扫描开始】isScanOver="+isScanOver + " " + "canRestartScan="+canRestartScan);
		}
		
		@Override 
		public void onScanProcessChange(int arg0, String arg1) {
			
		}
		 
		@Override
		public void onScanFound(int arg0, long arg1, long arg2) {
			 
		}
		
		@Override
		public void onScanFinish(SdcardScanResultHolder result) {
			//Aurora <shihao> <2015-03-12> for crash when uninstall app bug 11462 10448 begin
			isScanOver = true;
			Log.i(TAG, "【扫描结束】 isScanover ="+isScanOver  + "  " + " canRestartScan=="+canRestartScan);
			dealResult(result);
			if(canRestartScan){
				canRestartScan = false;
				mBackgroundHandler.sendEmptyMessage(0);
			}
			//Aurora <shihao> <2015-03-12> for crash when uninstall app bug 11462 10448 end
		}
		
		@Override
		public void onScanCancel() {
			
		}
		
		@Override 
		public void onCleanStart() {
			
		}
		
		@Override
		public void onCleanProcessChange(int arg0, long arg1, int arg2) {

		}
		
		@Override
		public void onCleanFinish() {

		}
		
		@Override
		public void onCleanCancel() {
			//未使用
		}
	};
	
	private void dealResult(SdcardScanResultHolder result) {
		String curAppName = null;
		synchronized (mLock) {
			if(appNameList.size() >0){
				curAppName = appNameList.get(0);
				Log.i(TAG,"DeepClearModel dealResult curAppName =="+curAppName);
				appNameList.remove(0);
			}
		}
		if(curAppName == null){
			return ;
		}
        boolean isDelete = false;
		if(result != null) { 			
			//已卸载软件的缓存
			if(result.mUnistallSoftRubbishList != null) {
				for(SoftwareCacheModel model :result.mUnistallSoftRubbishList) {					
					if(model != null) {
						Log.i(TAG, "DeepClearModel dealResult 所属软件名" + model.mApp);
						if(curAppName.equals(model.mApp)){
							deleteRubbish(model);
							isDelete = true;
						}else if(model.mApp.contains(curAppName)){
							deleteRubbish(model);
							isDelete = true;
						}else if(curAppName.contains(model.mApp)){
							deleteRubbish(model);
							isDelete = true;
						}						
					}										
				}
			} 
		}else{
			Log.i(TAG, "没有垃圾文件");
		}
		Log.w(TAG, "DeepClearModel dealResult isDelete==" + isDelete);
    	if(isDelete){
          Message mUIhandlerMsg = mUIhandler.obtainMessage();
          mUIhandlerMsg.obj = mApplicationContext.getString(R.string.clear_sucess);
          mUIhandler.sendMessage(mUIhandlerMsg);
    	}		
	}
	
	private void deleteRubbish(SoftwareCacheModel model){
		for(File f:model.mFiles) {
			if(f.isDirectory()){
				FileUtils.delFolder(f.getAbsolutePath());
			}else{
				FileUtils.delFile(f.getAbsolutePath());
			}
		}
	}
	
    final class UIHandler extends Handler{		
 		public UIHandler(Looper looper){
            super(looper);
        }
 		
 		@Override
 	    public void handleMessage(Message msg) { 
             Toast.makeText(mApplicationContext, (String)msg.obj,Toast.LENGTH_SHORT).show();
 	    }
 	}
}
