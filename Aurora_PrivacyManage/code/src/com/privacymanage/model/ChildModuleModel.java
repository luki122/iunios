package com.privacymanage.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.privacymanage.activity.CustomApplication;
import com.privacymanage.data.AccountData;
import com.privacymanage.data.ModuleInfoData;
import com.privacymanage.data.MyArrayList;
import com.privacymanage.interfaces.ChildModuleSubject;
import com.privacymanage.provider.AccountProvider;
import com.privacymanage.provider.ModuleInfoProvider;
import com.privacymanage.utils.LogUtils;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ChildModuleModel extends ChildModuleSubject{
	private final String TAG = ChildModuleModel.class.getName();
	private final int MSG_childModuleUpdate = 1;
	private final static Object sGlobalLock = new Object();
    static ChildModuleModel sInstance;
	private final Context mApplicationContext;
	private final UIHandler mUIhandler;
	/**
	 * 当前账户下各子模块隐私数据
	 */
	private final MyArrayList<ModuleInfoData> childModuleList = 
			new MyArrayList<ModuleInfoData>();
	
    static public ChildModuleModel getInstance() {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new ChildModuleModel();
            }
            return sInstance;
        }
    }

    private ChildModuleModel() {	
        mApplicationContext = CustomApplication.getApplication();
        childModuleList.clear();
        mUIhandler = new UIHandler(Looper.getMainLooper());
        initChildModuleFromAssert();
        initPrivayItemNumFromSqlite();
    }
	
    /**
     * 从assets中初始化子模块列表
     */
	private void initChildModuleFromAssert(){
		LogUtils.printWithLogCat(TAG,"readAssertData()");
		parseJson(getAssert());
	}
	
	/**
	 * 从数据库中初始化隐私条目
	 */
	private void initPrivayItemNumFromSqlite(){
		for(int i=0;i<childModuleList.size();i++){
			ModuleInfoData moduleInfoData = childModuleList.get(i);
			if(moduleInfoData == null){
				continue ;
			}
			int privacyItemNum = ModuleInfoProvider.getPrivacyItemNum(
					mApplicationContext, 
					AccountModel.getInstance().getCurAccount().getAccountId(), 
					moduleInfoData.getPkgName(), 
					moduleInfoData.getClassName() );
			moduleInfoData.setItemNum(privacyItemNum);
		}
	}
	
	/**
	 * 在账户切换时更新隐私条目
	 */
	public void updatePrivayItemNumWhenAccountSwitch(){
		initPrivayItemNumFromSqlite();
	}
	
	/**
	 * 获取子模块列表
	 * @return 返回值不可能为null
	 */
	public MyArrayList<ModuleInfoData> getChildModuleList(){
		return childModuleList;
	}
	
	/**
     * 重置指定模块所有隐私空间下的隐私个数
     * @param pkgName 子模块所在的包名
     * @param className 子模块对应界面的类名
     */
	public void resetPrivacyNumOfAllAccount(String pkgName, String className){
		ModuleInfoProvider.delete(mApplicationContext, pkgName, className);
		
		//更新当前账户，当前模块隐私条数，并刷新界面
		for(int i=0;i<childModuleList.size();i++){
			ModuleInfoData moduleInfoData = childModuleList.get(i);
			if(moduleInfoData == null){
				continue ;
			}
			if(pkgName.equals(moduleInfoData.getPkgName()) &&
					className.equals(moduleInfoData.getClassName())){
				moduleInfoData.setItemNum(0);					
				Message mUIhandlerMsg = mUIhandler.obtainMessage();
		        mUIhandlerMsg.what = MSG_childModuleUpdate;
		        mUIhandlerMsg.obj = moduleInfoData;
		        mUIhandler.sendMessage(mUIhandlerMsg);			        
				break;
			}
		}
	}

	/**
	 * 更新子模块隐私数据
	 * @param pkgName
	 * @param className
	 * @param itemNum
	 * @param accountId
	 */
	public void updateChildModuleItemNum(String pkgName, String className, 
			int itemNum,long accountId){
		if(pkgName == null || className == null){
			return ;
		}
		if(accountId == AccountData.NOM_ACCOUNT || 
				!AccountProvider.isHadAccountId(mApplicationContext,accountId)){
			//如果需要更新的账户是正常账户或者该账户不存在，则直接退出。
			return ;
		}
		//由于不清楚需要更新数据的账户是不是当前账户，所以直接先保存在数据库中
		ModuleInfoData tmpModuleInfoData = new ModuleInfoData();
		tmpModuleInfoData.setClassName(className);
		tmpModuleInfoData.setPkgName(pkgName);
		tmpModuleInfoData.setItemNum(itemNum);
		ModuleInfoProvider.insertOrUpdateDate(mApplicationContext, 
				tmpModuleInfoData,accountId);
		
		if(accountId == AccountModel.getInstance().getCurAccount().getAccountId()){
			//如果需要更新数据的账户是当前账户，则更新内存中的值，并且通知刷新界面
			for(int i=0;i<childModuleList.size();i++){
				ModuleInfoData moduleInfoData = childModuleList.get(i);
				if(moduleInfoData == null){
					continue ;
				}
				if(pkgName.equals(moduleInfoData.getPkgName()) &&
						className.equals(moduleInfoData.getClassName())){
					moduleInfoData.setItemNum(itemNum);					
					Message mUIhandlerMsg = mUIhandler.obtainMessage();
			        mUIhandlerMsg.what = MSG_childModuleUpdate;
			        mUIhandlerMsg.obj = moduleInfoData;
			        mUIhandler.sendMessage(mUIhandlerMsg);			        
					break;
				}
			}
		}		
	}
	
    final class UIHandler extends Handler{		
 		public UIHandler(Looper looper){
            super(looper);
        }
 		@Override
 	    public void handleMessage(Message msg) {  
 			switch (msg.what) {
 			case MSG_childModuleUpdate:
 				notifyOfChildModuleUpdate((ModuleInfoData)msg.obj);
 				break;
 	        }
 	    }
 	}
	
	private InputStream getAssert(){
    	InputStream fs=null;
		try {
			AssetManager am = mApplicationContext.getAssets();
			fs = am.open("module_list.json");			
		}catch (Exception e) {
			e.printStackTrace();
		}return fs;
	}
	
	private void parseJson(InputStream in) {
		try {			
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}
			String str = builder.toString();
			LogUtils.printWithLogCat(TAG,str);
			parseJson(str);
		} catch (Exception e) {
			LogUtils.printWithLogCat(TAG,e.toString());
			e.printStackTrace();
		}
	}
	
	private void parseJson(String str) throws Exception {
//		{
//		    "list":[
//			    {"pkgName":"com.aurora.secure","className":"com.secure.activity.Activity","needShow":1},
//			    {"pkgName":"com.aurora.secure","className":"com.secure.activity.Activity","needShow":1},
//			    {"pkgName":"com.aurora.secure","className":"com.secure.activity.Activity","needShow":1}
//			   ]
//		}
		
		JSONObject json = JSON.parseObject(str);
		if(json == null || json.isEmpty()){
			return ;
		}		
	
		JSONArray listObject = json.getJSONArray("list");
		int size = listObject==null?0:listObject.size();
		for(int i=0;i<size;i++){
			ModuleInfoData moduleInfoData = parseListItem(listObject.getJSONObject(i));
			if(moduleInfoData == null){
				continue ;
			}
			childModuleList.add(moduleInfoData);
		}
	}
	
	private ModuleInfoData parseListItem(JSONObject item) throws Exception {
		if(item == null || item.isEmpty()){
		   return null;	
		}		
		String pkgName = item.getString("pkgName");
		String className = item.getString("className");
		int needShow = item.getIntValue("needShow");
		
		ModuleInfoData moduleInfoData = new ModuleInfoData();
		moduleInfoData.setPkgName(pkgName);
		moduleInfoData.setClassName(className);
		moduleInfoData.setNeedShow((needShow==1)?true:false);
		return moduleInfoData;
	}
}
