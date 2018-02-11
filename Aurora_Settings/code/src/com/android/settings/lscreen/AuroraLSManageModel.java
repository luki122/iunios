package com.android.settings.lscreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.android.settings.lscreen.ls.LSOperator;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AuroraLSManageModel extends LSAppSubject {
	private static final String TAG=AuroraLSManageModel.class.getName();
	
	private static final int MSG_ADDLSAPP=1;
	private static final int MSG_DELLSAPP=2;
	private static final int MSG_ALLLSAPP=3;
	
	private Context mContext;
	private static AuroraLSManageModel manageModel;
	private static Object lock = new Object();
	
	private DataArrayList<AppInfo> allLSAppList; // 所有锁屏应用程序列表
	private List<BaseData> allAppInfoList;  // 所有应用程序的列表
    private List<BaseData> lsFilterAllApp;  // 获取所有过滤之后的所有应用程序
	private final UIHandler mUIHandle;
	private final HandlerThread handlerThread;
	private final QueueHandler mQueueHandler;
	
	public static AuroraLSManageModel getInstance(Context mContext)
	{
		if(manageModel==null)
		{
			manageModel=new AuroraLSManageModel(mContext);
		}
		return manageModel;
	}
	
	public AuroraLSManageModel(Context mContext)
	{
		this.mContext = mContext.getApplicationContext();
		allLSAppList=new DataArrayList<AppInfo>();
		allAppInfoList= new ArrayList<BaseData>();
		lsFilterAllApp=new ArrayList<BaseData>();
		handlerThread=new HandlerThread(TAG+":Background");
		handlerThread.start();
		mQueueHandler=new QueueHandler(handlerThread.getLooper());
		mUIHandle=new UIHandler(Looper.getMainLooper());
	}
	
	public void addLSApp(DataArrayList<String> appDatas)
	{
		synchronized (lock) {
			Message msg=mQueueHandler.obtainMessage();
			msg.what=MSG_ADDLSAPP;
			msg.obj=appDatas;
			msg.sendToTarget();
		}
	}
	
	public void delLSApp(DataArrayList<String> appDatas)
	{
		synchronized (lock) {
		    Message msg=mQueueHandler.obtainMessage();
		    msg.what=MSG_DELLSAPP;
		    msg.obj=appDatas;
		    msg.sendToTarget();
		}
	}
	
	public void getALLLSApp()
	{
		synchronized (lock) {
			Message msg= mQueueHandler.obtainMessage();
			msg.what=MSG_ALLLSAPP;
            msg.sendToTarget();
		}
	}
	
	/*
	 * 应用程序安装触发的操作
	 */
	public void AddLSOrSqllist(String packageName)
	{
		synchronized (lock) {
			if(packageName==null || mContext==null || allLSAppList == null)
			{
				return;
			}
			
	    	if(LSContentProvideImp.insertOrUpdateDate(mContext, packageName))
	    	{
	    		AppInfo lsAppData = findLSAppData(packageName);
	    		if(lsAppData==null)
	    		{
	    			lsAppData=new AppInfo();
		    	    lsAppData.setPackageName(packageName);
		    	    allLSAppList.add(lsAppData);
	    		}else
	    		{
	    			return ;
	    		}
	    	}
		}
	}
	
	/*
	 * 应用程序卸载时候触发的操作
	 */
	public void delLSOrSqllist(String packageName)
	{
		synchronized (lock) {
			if(packageName==null || mContext==null || allLSAppList == null)
			{
				return;
			}
			
			
			LSContentProvideImp.deleteData(mContext, packageName);
			
			AppInfo lsdata= findLSAppData(packageName);
		    
			if(lsdata!=null)
			{
				allLSAppList.removeDate(lsdata);
			}
		}
	}
	
	/*
	 *  通过设置packageName 生成对应的AppInfo信息
	 *  主要目的是获取ICON 和名字信息并显示
	 */
	public BaseData setPkgToAppInfo(String packageName)
	{
		AppInfo info = new AppInfo();
        if(allAppInfoList == null || allAppInfoList.size()==0)
        {
        	Log.d("gd", " allAppInfoList size == 0");
        	return info;
        }
        for(BaseData data: allAppInfoList)
        {
        	if(((AppInfo)data).getPackageName()==null)
        	{
        		continue;
        	}
        	if(packageName.equals(((AppInfo)data).getPackageName()))
        	{
        		info.setAppName(((AppInfo)data).getAppName());
        		info.setAppNamePinYin(((AppInfo)data).getAppNamePinYin());
        		info.setPackageName(packageName);
        	}
        }
        return info;        
	}
	
	public final class QueueHandler extends Handler
	{
		
		public QueueHandler(Looper looper)
		{
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			case MSG_ADDLSAPP:
				addLSAppFunc((DataArrayList<String>)msg.obj);
				break;
			case MSG_DELLSAPP:
				delLSAppFunc((DataArrayList<String>)msg.obj);
				break;
			case MSG_ALLLSAPP:
				getAllLSAppFunc(mContext);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	private void addLSAppFunc(DataArrayList<String> packageList)
	{
		if(packageList==null)
		{
			return;
		}
	    ArrayList<AppInfo> addLSDatas=new ArrayList<AppInfo>();
        for(int i=0;i<packageList.size();i++)
        {
        	String packageName=packageList.get(i);
        	if(packageName==null)
        	{
        		continue;
        	}
        	
        	if(LSContentProvideImp.insertOrUpdateDate(mContext, packageName))
        	{
        		AppInfo lsAppData = new AppInfo();
        	    lsAppData.setPackageName(packageName);
        	    addLSDatas.add(lsAppData);
        	    allLSAppList.add(lsAppData);
        	}
        }
        
        if(addLSDatas.size()>0)
        {
        	Message mUIhandlerMsg=mUIHandle.obtainMessage();
        	mUIhandlerMsg.what=MSG_ADDLSAPP;
        	mUIhandlerMsg.obj=addLSDatas;
        	mUIhandlerMsg.sendToTarget();
        }
	}
	
	
	
	private void delLSAppFunc(DataArrayList<String> packageList)
	{
		
		if(packageList==null)
		{
			return;
		}
		
	    ArrayList<AppInfo> delLSAppList=new ArrayList<AppInfo>();
		for(int i=0;i<packageList.size();i++)
		{
			String packageName=packageList.get(i);
			if(packageName==null)
			{
				continue;
			}
			LSContentProvideImp.deleteData(mContext, packageName);
			/*
			 * 查看锁屏列表中是否存在此packageName
			 */
			AppInfo lsdata= findLSAppData(packageName);
			if(lsdata!=null)
			{
				allLSAppList.removeDate(lsdata);
				delLSAppList.add(lsdata);
			}
		}
		Log.d("gd", "size="+delLSAppList.size());
		if(delLSAppList.size()>=0)
		{
			Message mUIhandlerMsg=mUIHandle.obtainMessage();
			mUIhandlerMsg.what=MSG_DELLSAPP;
			mUIhandlerMsg.obj=delLSAppList;
			mUIhandlerMsg.sendToTarget();
		}
	}
	
	/*
	 * 获取所有锁屏应用程序
	 */
	private void getAllLSAppFunc(Context mContext)
	{
		if(mContext==null)
		{
			return ;
		}
		if(allLSAppList!=null)
		{
			allLSAppList.clear();
		}
		try {
			allLSAppList=LSContentProvideImp.getLSAppInfo(mContext);
		} catch (Exception e) {
		}
		if(allLSAppList.size()==0)
		{
			return;
		}else
		{
/*			for(int i=0;i<allLSAppList.size();i++)
			{
				if(allLSAppList.get(i)==null)
				{
					continue;
				}
			}*/
		}
	}
	
	private AppInfo findLSAppData(String packageName)
	{
		
		if(packageName==null || allLSAppList.size()==0)
		{
			return null;
		}
		
        for(int i=0;i<allLSAppList.size();i++)
        {
        	AppInfo appData=allLSAppList.get(i);
        	if(appData==null)
        	{
        		continue;
        	}
        	if(packageName.equals(appData.getPackageName()))
        	{
                return appData;
        	}
        }
        return null;
	}
	
	
    public final class UIHandler extends Handler
    {

    	public UIHandler(Looper looper)
    	{
    		super(looper);
    	}
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ADDLSAPP:
				notifyObserverAdd((ArrayList<AppInfo>)msg.obj);
				break;
			case MSG_DELLSAPP:
				notifyObserserDel((ArrayList<AppInfo>)msg.obj);
				break;
			case MSG_ALLLSAPP:
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
    	
    }
    
    private AppInfo getAppInfo(ApplicationInfo app,PackageManager pm) {  
        AppInfo appInfo = new AppInfo();  
        appInfo.setAppName((String) app.loadLabel(pm));
        appInfo.setAppNamePinYin(Utils.getSpell((String) app.loadLabel(pm)));
        appInfo.setPackageName(app.packageName);  
        return appInfo;  
    }
    
    /*
     * 获取所有应用程序并以未加入到锁屏应用返回
     */
    
    public List<BaseData> queryAppInfo()
    {
		PackageManager pm=mContext.getPackageManager();
        // 查询所有已经安装的第三方的应用程序  
        List<ApplicationInfo> listAppcations = pm  
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(listAppcations,new ApplicationInfo.DisplayNameComparator(pm));// 排序  
        List<AppInfo> appInfos = new ArrayList<AppInfo>(); // 保存过滤查到的AppInfo  
		allAppInfoList.clear();
		lsFilterAllApp.clear();
		for(int i=0;i<LSOperator.specialApp.length;i++)
		{
			ApplicationInfo app=null;
			try {
				app = pm.getApplicationInfo(LSOperator.specialApp[i],
						PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			AppInfo appInfo=getAppInfo(app,pm);
			allAppInfoList.add(appInfo);
            if(findLSAppData(appInfo.getPackageName())!=null)
            {
            	continue;
            }
            lsFilterAllApp.add(appInfo);
		}
		
        for (ApplicationInfo app : listAppcations) 
        {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) 
            {
            	AppInfo appInfo=getAppInfo(app,pm);
            	allAppInfoList.add(appInfo);
	            if(findLSAppData(appInfo.getPackageName())!=null)
	            {
	            	continue;
	            }
	            lsFilterAllApp.add(appInfo);
            }   
        }
        // 获取所有应用程序
//		Intent mainIntent=new Intent(Intent.ACTION_MAIN,null);
//		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//		List<ResolveInfo> resolveInfos=pm.queryIntentActivities(mainIntent, 0);
//		Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
//		if(allAppInfoList!=null && lsFilterAllApp!=null)
//		{
//			allAppInfoList.clear();
//			lsFilterAllApp.clear();
//			for(ResolveInfo info : resolveInfos)
//			{
//	            String pkgName = info.activityInfo.packageName; // 获得应用程序的包名
//	            String appLabel = (String) info.loadLabel(pm); // 获得应用程序的Label
//                String appPinYin = Utils.getSpell(appLabel);  // 获取应用程序的拼音名字
//                //    pkgName=com.android.contacts appLabel=拨号 appPinYin=BOHAO
//                //    pkgName=com.aurora.note appLabel=快速录音 appPinYin=KUAISULUYIN
//                if(appPinYin.equals("KUAISULUYIN"))
//                {
////                	Log.d("gd", "pkgName="+pkgName+" appLabel="+appLabel+" appPinYin="+appPinYin);
//                	continue;
//                }
//	            AppInfo appInfo = new AppInfo();
//	            appInfo.setAppName(appLabel);
//	            appInfo.setAppNamePinYin(appPinYin);
//	            appInfo.setPackageName(pkgName);
//	            allAppInfoList.add(appInfo);
//	            /*
//	             * 根据锁屏的列表提取应用 并返回
//	             */
//	            if(findLSAppData(pkgName)!=null)
//	            {
//	            	continue;
//	            }
//	            lsFilterAllApp.add(appInfo);
//			}
//		}
		Log.d("gd", "lsFilter size="+lsFilterAllApp.size()+"  allApp"+allAppInfoList.size());
		return lsFilterAllApp;
    } 

    
    
    
    
    
}
