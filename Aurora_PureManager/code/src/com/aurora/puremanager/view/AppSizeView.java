package com.aurora.puremanager.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.puremanager.R;
import com.aurora.puremanager.model.CacheSizeModel;
import com.aurora.puremanager.totalCount.TotalCount;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.Utils;

public class AppSizeView extends FrameLayout  implements OnClickListener{	
	
	private final int CLEAR_USER_DATA = 2;
    private final int CLEAR_CACHE = 3;
    private final int GET_PKG_SIZE = 4;    
    private final int GET_PKG_SIZE_FOR_MOVE_APP = 5;
    private static final int OP_SUCCESSFUL = 1;
    private static final int OP_FAILED = 2;
  
    private RelativeLayout useDataLayout;
    private RelativeLayout cacheDataLayout;
    private View appSizeTitleLayout;
	private TextView topSizeText;	
	private Button appMemoryText;
	private Button useDataText;
	private TextView cacheDataText;
	private Button clearUseDataBtn;
	private Button clearCacheBtn;
	private PackageStats lastPackageStats;
	
	long cachesize ; 
	long datasize ;  
	long codesize   ;  
	long totalsize  ;
	
	private ClearUserDataObserver mClearDataObserver;
	private ClearCacheObserver mClearCacheObserver;
	
	private String curPkgName = null;
	private DevicePolicyManager mDpm;
	
	public AppSizeView(Context context) {
		super(context); 
	    initView(); 
	    initData();
	}
	
	public AppSizeView(Context context, AttributeSet attrs) {
	    super(context, attrs);       
	    initView(); 
	    initData();
	}
    
    private void initView(){    	 
	    LayoutInflater inflater = LayoutInflater.from(getContext());
	    inflater.inflate(R.layout.app_size_layout, this, true);	    	
    	  
	    useDataLayout = (RelativeLayout)findViewById(R.id.useDataLayout);
	    cacheDataLayout = (RelativeLayout)findViewById(R.id.cacheDataLayout);
	    appSizeTitleLayout = findViewById(R.id.appSizeTitleLayout);
    	topSizeText = (TextView)findViewById(R.id.topSizeText);
    	appMemoryText = (Button)findViewById(R.id.appMemoryText);
    	useDataText = (Button)findViewById(R.id.useDataText);
    	cacheDataText = (TextView)findViewById(R.id.cacheDataText);
    	clearUseDataBtn = (Button)findViewById(R.id.clearUseDataBtn);
    	clearCacheBtn = (Button)findViewById(R.id.clearCacheBtn);
    	clearUseDataBtn.setOnClickListener(this);
    	clearCacheBtn.setOnClickListener(this);
    	clearUseDataBtn.setEnabled(false);
    	clearCacheBtn.setEnabled(false);
    }
    
    private void initData(){  
    	if(mDpm == null){
			mDpm = (DevicePolicyManager)getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
		}
    	
    	if (mClearDataObserver == null) {
            mClearDataObserver = new ClearUserDataObserver();
        }
    	
    	if(mClearCacheObserver == null){
    		mClearCacheObserver = new ClearCacheObserver();
    	}
    }
    
    /**
     * 设置当前的apk信息
     * @param curAppInfo
     */
    public void setCurAppInfo(String curPkgName){
    	this.curPkgName = curPkgName;
    	if(curPkgName == null){
    		throw new IllegalArgumentException("ERROR_OF_curAppInfo_NULL_IN_AppSizeView");
    	}
    	updatePacakgeSize();  	
    }
    
    /**
     * 仅仅显示缓存layout
     */
    public void justShowCacheLayout(){ 
    	if(appSizeTitleLayout != null){
    		appSizeTitleLayout.setVisibility(View.INVISIBLE);
    	}
    	
    	if(appMemoryText != null){
    		appMemoryText.setVisibility(View.GONE);
    	}
    	
    	if(useDataLayout != null){
    		useDataLayout.setVisibility(View.GONE);
    	}
    	
    	if(cacheDataLayout != null){
    		cacheDataLayout.setBackgroundResource(R.drawable.item_of_alone_bg);
    	}    	
    }
    
    /**
     * 获取Android Native App的缓存大小、数据大小、应用程序大小
     * 临时添加 gaoming
     * @param context
     *            Context对象
     * @param pkgName
     *            需要检测的Native App包名
     */
    public void getPkgSize(final Context context, String pkgName){
        Method method = null;
		try {
			method = PackageManager.class.getMethod("getPackageSizeInfo",
			        new Class[] { String.class, IPackageStatsObserver.class });
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {
			method.invoke(context.getPackageManager(), new Object[] {
			        pkgName,
			        new IPackageStatsObserver.Stub() {
			            @Override
			            public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
			                // 子线程中默认无法处理消息循环，自然也就不能显示Toast，所以需要手动Looper一下
			                Looper.prepare();
			                // 从pStats中提取各个所需数据
			                cachesize = pStats.cacheSize;
			                datasize = pStats.dataSize;
			                codesize = pStats.codeSize;
			                totalsize = cachesize + datasize + codesize ;
			                
			                Message msg = mHandler.obtainMessage(GET_PKG_SIZE);
			    			msg.obj = pStats;
			    			mHandler.sendMessage(msg);
			    			
			                /*Toast.makeText(context,
			                        "缓存大小=" + Formatter.formatFileSize(context, cacheSize) +
			                        "\n数据大小=" + Formatter.formatFileSize(context, dataSize) +
			                        "\n程序大小=" + Formatter.formatFileSize(context, codeSize),
			                        Toast.LENGTH_LONG).show();*/
			                // 遍历一次消息队列，弹出Toast
			                Looper.loop();
			            }
			        }
			});
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * 更新应用size
     */
    public void updatePacakgeSize(){
    	ApkUtils.queryPacakgeSize(getContext(),curPkgName,new PkgSizeObserver());	
//    	getPkgSize(getContext(), curPkgName);
    }
    
    /**
     * 更新应用size
     * @param isMoveApp true:表示移动应用后，更新应用的大小
     */
    public void updatePacakgeSize(boolean isMoveApp){
    	ApkUtils.queryPacakgeSize(getContext(),curPkgName,new PkgSizeObserver(isMoveApp));
//    	getPkgSize(getContext(), curPkgName);
    }
    
    /**
     * 禁止清除缓存，清除数据按钮的可点击性
     */
    public void disableAllButton(){
    	if(clearUseDataBtn != null){
    		clearUseDataBtn.setTextColor(getResources().getColor(R.color.head_text));
    		clearUseDataBtn.setEnabled(false);
    	}
    	
    	if(clearCacheBtn != null){
    		clearCacheBtn.setTextColor(getResources().getColor(R.color.head_text));
    		clearCacheBtn.setEnabled(false);
    	}
    }
    
    /**
     * 更新清除缓存，清除数据按钮的可点击性
     */
    public void updateAllButtonState(){
    	if(clearUseDataBtn != null){
    		if(datasize>0){
        		clearUseDataBtn.setEnabled(true);
        		clearUseDataBtn.setTextColor(getResources().getColor(R.color.uninstall_list_item_btn_color));
        		setEnabledOfClearUseDataBtn();
        	}else{
        		clearUseDataBtn.setTextColor(getResources().getColor(R.color.head_text));
        		clearUseDataBtn.setEnabled(false);
        	}
    	}
    	
    	if(clearCacheBtn != null){
    		if(cachesize>0){
        		clearCacheBtn.setEnabled(true);
        		clearCacheBtn.setTextColor(getResources().getColor(R.color.uninstall_list_item_btn_color));
        	}else{
        		clearCacheBtn.setTextColor(getResources().getColor(R.color.head_text));
        		clearCacheBtn.setEnabled(false);
        	}
    	}
    }

    /**
     * 更新应用size视图   
     */
    private void updateSizesLayout(){
    	if(topSizeText == null ||
    			appMemoryText == null ||
    			useDataText == null ||
    			cacheDataText == null ||
    			clearUseDataBtn == null ||
    			clearCacheBtn == null){
    		return ;
    	}  	
    	if(datasize>0){
    		clearUseDataBtn.setEnabled(true);
    		clearUseDataBtn.setTextColor(getResources().getColor(R.color.uninstall_list_item_btn_color));
    		setEnabledOfClearUseDataBtn();
    	}else{
    		clearUseDataBtn.setTextColor(getResources().getColor(R.color.head_text));
    		clearUseDataBtn.setEnabled(false);
    	}
    	
    	if(cachesize>0){
    		clearCacheBtn.setEnabled(true);
    		clearCacheBtn.setTextColor(getResources().getColor(R.color.uninstall_list_item_btn_color));
    	}else{
    		clearCacheBtn.setTextColor(getResources().getColor(R.color.head_text));
    		clearCacheBtn.setEnabled(false);
    	}
    	
		topSizeText.setText(getResources().getString(R.string.total_size)+
				getResources().getString(R.string.colon)+
				Utils.dealMemorySize(getContext(), totalsize));
		
		appMemoryText.setText(getResources().getString(R.string.app_size)+
				getResources().getString(R.string.colon)+
				Utils.dealMemorySize(getContext(), codesize));
		
		useDataText.setText(getResources().getString(R.string.user_data)+
				getResources().getString(R.string.colon)+
				Utils.dealMemorySize(getContext(), datasize));
		
		cacheDataText.setText(getResources().getString(R.string.app_cache)+
				getResources().getString(R.string.colon)+
				Utils.dealMemorySize(getContext(), cachesize));				
    }
    
    private void setEnabledOfClearUseDataBtn(){
    	ApplicationInfo info = ApkUtils.getApplicationInfo(getContext(),curPkgName);
		if(info != null && clearUseDataBtn != null){			
			if ((info.flags&(ApplicationInfo.FLAG_SYSTEM
	                | ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA))
	                == ApplicationInfo.FLAG_SYSTEM
	                || mDpm.packageHasActiveAdmins(curPkgName)){
				clearUseDataBtn.setTextColor(getResources().getColor(R.color.head_text));
				clearUseDataBtn.setEnabled(false);
			} 
		}
    }
    
    
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.clearUseDataBtn:
			InfoDialog.showDialog((Activity)getContext(), 
				R.string.clear_data_dlg_title,
				android.R.attr.alertDialogIcon,
				R.string.clear_data_dlg_text, 
                R.string.sure, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	clearUseDataBtn.setTextColor(getResources().getColor(R.color.head_text));
                    	clearUseDataBtn.setEnabled(false);
                    	new TotalCount(getContext(), "32", 1).CountData();
                        ApkUtils.ClearAppUserData(getContext(),
                        		curPkgName, 
                        		mClearDataObserver);
                        if(curPkgName.equals("com.aurora.puremanager")){
                        	ActivityManager activityManger = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                        	activityManger.killBackgroundProcesses(curPkgName);
                        }
                    }
                }, 
                R.string.cancel,
				null,
				null);
			break;
		case R.id.clearCacheBtn:
			//对话框不需要显示提示内容时，设置为0    gaoming20160315
			InfoDialog.showDialog((Activity)getContext(), 
				R.string.clear_cache,
				android.R.attr.alertDialogIcon,
				0, 
                R.string.sure, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	new TotalCount(getContext(), "22", 1).CountData();
                    	ApkUtils.ClearAppCacheData(getContext(), 
                    			curPkgName, 
                    			mClearCacheObserver);
                    }
                },          
				R.string.cancel,
				null,
				null);
			break;
		}		
	} 
	   
	/**
	 * 获取应用程序包大小的回调
	 */
    private class PkgSizeObserver extends IPackageStatsObserver.Stub{
    	private boolean isMoveApp;
    	public PkgSizeObserver(boolean isMoveApp){
    		this.isMoveApp = isMoveApp;
    	}
    	
    	public PkgSizeObserver(){
    		this.isMoveApp = false;
    	}
    	
		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			ApkUtils.dealPackageStats(pStats);
			long saveMemorySize = 0;
			Message msg = null;
			if(lastPackageStats != null){
				saveMemorySize = (lastPackageStats.cacheSize-pStats.cacheSize)+
						(lastPackageStats.dataSize-pStats.dataSize)+
						(lastPackageStats.codeSize-pStats.codeSize);	
			}
				
			if(isMoveApp){
				msg = mHandler.obtainMessage(GET_PKG_SIZE_FOR_MOVE_APP);
				String toastStr;
				if(saveMemorySize>0){
					toastStr = getContext().getString(R.string.move_sucess)+
							getContext().getString(R.string.comma)+
							getContext().getString(R.string.alread_save)+
							Utils.dealMemorySize(getContext(), saveMemorySize)+
							getContext().getString(R.string.memery_size);
				}else{
					toastStr = getContext().getString(R.string.move_sucess);
				}
				msg.obj = toastStr;
				mHandler.sendMessage(msg);
				
			}
			lastPackageStats = pStats;
			cachesize = pStats.cacheSize+pStats.externalCacheSize; //缓存大小
			datasize = pStats.dataSize+pStats.externalDataSize;  //应用程序大小
			codesize =	pStats.codeSize+pStats.externalCodeSize;  //数据大小
			totalsize = cachesize + datasize + codesize ;
			msg = mHandler.obtainMessage(GET_PKG_SIZE);
			msg.obj = pStats;
			mHandler.sendMessage(msg);
		}
    }
    
    private class ClearUserDataObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            final Message msg = mHandler.obtainMessage(CLEAR_USER_DATA);
            msg.arg1 = succeeded?OP_SUCCESSFUL:OP_FAILED;
            mHandler.sendMessage(msg);
         }
     }
     
    private class ClearCacheObserver extends IPackageDataObserver.Stub {
         public void onRemoveCompleted(final String packageName, final boolean succeeded) {
             final Message msg = mHandler.obtainMessage(CLEAR_CACHE);
             msg.arg1 = succeeded ? OP_SUCCESSFUL:OP_FAILED;
             mHandler.sendMessage(msg);
          }
      }
     
     private Handler mHandler = new Handler() {
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case CLEAR_USER_DATA:
                	 ApkUtils.queryPacakgeSize(getContext(),
                			 curPkgName,
                			 new PkgSizeObserver());
                	 if(msg.arg1 == OP_SUCCESSFUL){
                		 InfoDialog.showToast(getContext(), R.string.clear_sucess); 
                	 }               	 
                     break;
                 case CLEAR_CACHE:
                	 ApkUtils.queryPacakgeSize(getContext(),
                			 curPkgName,
                			 new PkgSizeObserver());	
                	 if(msg.arg1 == OP_SUCCESSFUL){
                		 InfoDialog.showToast(getContext(), R.string.clear_sucess); 
                	 } 
                     break;
                 case GET_PKG_SIZE:
                	 CacheSizeModel.getInstance(getContext()).
                	       updateAppCache(curPkgName, (PackageStats)msg.obj);
                	 updateSizesLayout();
                	 break;
                 case GET_PKG_SIZE_FOR_MOVE_APP:
                	 InfoDialog.showToast(getContext(), (String)msg.obj);
                	 break;
                 default:
                     break;
             }
         }
     };
}
