package com.secure.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import com.aurora.secure.R;
import com.lbe.protocol.LBERecommendPkgPermProtoBuf.PkgPermResponse;
import com.secure.data.AppCategoryData;
import com.secure.data.BaseData;
import com.secure.data.Constants;
import com.secure.data.DefStartAppInfo;
import com.secure.data.MyArrayList;
import com.secure.data.SameCategoryAppInfo;
import com.secure.interfaces.Observer;
import com.secure.utils.ApkUtils;
import com.secure.utils.LogUtils;
import com.secure.utils.StringUtils;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PatternMatcher;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.inputmethod.InputMethodInfo;

/**
 * @author Administrator
 */
public class DefSoftModel {   
	private static DefSoftModel instance;
	private final String TAG = DefSoftModel.class.getName();
	private final int MSG_init = 1;
	private final MyArrayList<BaseData> resultAppCategoryDataList;
	private final Context context;
    private final MyArrayList<Handler> callBackHandlers = new MyArrayList<Handler>();   
	private final HandlerThread mQueueThread;
	private final QueueHandler mQueueHandler;
	private final Object mLock = new Object();
     
     /**
      * 注册观察者对象
      * @param observer
      */
     public void attach(Handler callBackHandler){
    	 try{
    		 callBackHandlers.add(callBackHandler);
    	 }catch(Exception e){
    		 e.printStackTrace();
    	 }   	 
     }
     
     /**
      * 删除观察者对象
      * @param observer
      */
     public void detach(Handler callBackHandler){
    	 try{
    	     callBackHandlers.remove(callBackHandler);
    	 }catch(Exception e){
    		 e.printStackTrace();
    	 } 
     }
	
	private DefSoftModel(Context context) {
		this.context = context.getApplicationContext();
		resultAppCategoryDataList = new MyArrayList<BaseData>();
        mQueueThread = new HandlerThread(TAG+":Background");
        mQueueThread.start();
        mQueueHandler = new QueueHandler(mQueueThread.getLooper()); 
	}
	
	/**
	 * 如果instance为null，不会创建
	 * @return
	 */
	public static synchronized DefSoftModel getInstance() {
		return instance;
	}

	/**
	 * 如果instance为null，则会创建一个
	 * @param context
	 * @return
	 */
	public static synchronized DefSoftModel getInstance(Context context) {
		if (instance == null) {
			instance = new DefSoftModel(context);
		}
		return instance;
	}
	
	/**
	 * 是否需要初始化
	 * @return
	 */
	public boolean isNeedInit(){
		synchronized (resultAppCategoryDataList) {
			if(resultAppCategoryDataList.size()>0){
				return false;
			}else{
				return true;
			}
		}		
	}
	
	/**
	 * 在子线程中初始化或更新数据
	 */
	public void initOrUpdateThread(){		
		synchronized (mLock){
			mQueueHandler.removeMessages(MSG_init);
			
			Message msg = mQueueHandler.obtainMessage();
			msg.what = MSG_init;
			mQueueHandler.sendMessage(msg);	
		}
	}
	
    final class QueueHandler extends Handler {
        public QueueHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
				case MSG_init:
					try{
						Thread.sleep(200);
					}catch(Exception e){
						e.printStackTrace();
					}
					initOrUpdateAppCategoryData();
					findDefSoftOfAllCategory();
	 				break;
	        }
	    };
    }
	
	/**
	 * 初始化或更新应用类别数据
	 */
	private void initOrUpdateAppCategoryData(){		
		MyArrayList<BaseData> appCategoryDataList = new MyArrayList<BaseData>();
		
		setDefForJustSysInputMethod();
		
		//浏览器	
//		Intent intent_browser = new Intent(Intent.ACTION_VIEW);
//		intent_browser.addCategory(Intent.CATEGORY_BROWSABLE);
//		intent_browser.addCategory(Intent.CATEGORY_DEFAULT);
//		intent_browser.setDataAndType(Uri.parse("http://"), null);
		addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_BROWSER),
				context.getString(R.string.browser),appCategoryDataList);
		
		//音乐
//        Intent internt_music = new Intent(Intent.ACTION_VIEW); 
//        internt_music.addCategory(Intent.CATEGORY_DEFAULT);
//		internt_music.setDataAndType(Uri.parse("file://"), "audio/*");
		addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_MUSIC),
				context.getString(R.string.music),appCategoryDataList);
		
		//视频 
//		Intent intent_video = new Intent(Intent.ACTION_VIEW); 
//		intent_video.setDataAndType(Uri.parse("file://"), "video/*"); 
		addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_VIDEO),
				context.getString(R.string.video),appCategoryDataList);
				
		//相机
//		Intent intent_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		intent_camera.addCategory(Intent.CATEGORY_DEFAULT);
		addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_CAMERA),
				context.getString(R.string.camera),appCategoryDataList);
		
		//图库		
//		Intent intent_gallery = new Intent(Intent.ACTION_GET_CONTENT);
//		intent_gallery.setType("image/*");
//		intent_browser.addCategory(Intent.CATEGORY_DEFAULT);
		addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_GALLAY),
				context.getString(R.string.gallery),appCategoryDataList);
		
		//短信 		           
//	    Intent intent_msg = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:10010"));   
//	    intent_msg.addCategory(Intent.CATEGORY_DEFAULT);
//	    intent_msg.putExtra("sms_body", "");     
	    addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_MMS),
	    		context.getString(R.string.msg),appCategoryDataList); 
	    
	    //联系人
//	    Intent intent_contacts = new Intent(Intent.ACTION_VIEW);
//	    intent_contacts.setData(ContactsContract.Contacts.CONTENT_URI);
	    addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_PHONE),
	    		context.getString(R.string.contacts),appCategoryDataList); 
		
		//桌面
//		Intent intent_launcher = new Intent(Intent.ACTION_MAIN);  
//        intent_launcher .addCategory(Intent.CATEGORY_HOME);
//        intent_launcher.addCategory(Intent.CATEGORY_DEFAULT);
        addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_HOME),
        		context.getString(R.string.launcher),appCategoryDataList);
        
        //电子书
//        Intent intent_ebook = new Intent(Intent.ACTION_VIEW);     
//        intent_ebook.addCategory(Intent.CATEGORY_DEFAULT);   
//        intent_ebook.setDataAndType(Uri.parse("file://"), "text/plain"); 
        addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_READER),
        		context.getString(R.string.ebook),appCategoryDataList);
        
        //拨号
        addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_DIAL),
        		context.getString(R.string.dial),appCategoryDataList);
        
        //裁剪
        addCategoryData(DefMrgSoftIntent.getDefIntent(DefMrgSoftIntent.DEF_CROP),
        		context.getString(R.string.crop),appCategoryDataList);
        
		synchronized (resultAppCategoryDataList){
			resultAppCategoryDataList.clear();
			for(int i=0;i<appCategoryDataList.size();i++){
				BaseData tmpData = appCategoryDataList.get(i);
	    		if(tmpData == null){
	    			continue ;
	    		}
	    		resultAppCategoryDataList.add(tmpData);
			}
			appCategoryDataList.clear();
		}
	}
	
	private void addCategoryData(Intent intent,String categoryName,
			MyArrayList<BaseData> appCategoryDataList){
		MyArrayList<BaseData> listData = ApkUtils.querySameCategoryApp(context, intent);
		if(listData == null || listData.size()<=2){
			//注意：第一个为清除某类软件中默认应用的按钮
			//当一个类别中只有一个应用时，不用加入到appCategoryDataList中
			return ;
		}
		
		AppCategoryData appCategory  = new AppCategoryData();
        appCategory.setCategoryName(categoryName);
        appCategory.setIntent(intent);
        appCategory.setAppList(listData);
        appCategoryDataList.add(appCategory);
    	setDefForJustSysApp(appCategory);	
	}
	
	/**
	 * 输入法类别与其他类别处理方式不一样，所以要单独处理，
	 * 输入法类别下只有系统应用，并且该类别没有设置默认应用，并且应用数大于1，
	 * 此时就将IUNI的应用设为默认应用
	 */
	private void setDefForJustSysInputMethod(){
		List<InputMethodInfo> appList = ApkUtils.getEnabledInputMethodList(context);
		if(appList == null || appList.size() == 1){
			return ;
		}
		
		boolean isHaveThirdPartyApp = false;//是否有第三方应用
		InputMethodInfo iuniApp = null;
		
		for(int i=0;i<appList.size();i++){
			InputMethodInfo tmpItem = (InputMethodInfo)appList.get(i);
			if(tmpItem == null){
				continue;
			}
			String pkgName = tmpItem.getPackageName(); 
			if(ApkUtils.isUserApp(ApkUtils.getApplicationInfo(context,pkgName))){
				isHaveThirdPartyApp = true;
				break; 
			}
			if(Constants.isPackageNameInList(Constants.iuniPackageNameList, pkgName)){
				iuniApp = tmpItem;
			}		
		}
		
		if(!isHaveThirdPartyApp && iuniApp != null){			
			ApkUtils.setDefaultInputMethod(context, iuniApp.getId());		
		}		
	}
	
	/**
	 * 在某一个类别下只有系统应用，并且该类别没有设置默认应用，并且应用数大于1，
	 * 此时就将IUNI的应用设为默认应用（例如：在一个三星手机上刷IUNI系统，
	 * 就会有一个IUNI图库和三星图库，此时就把IUNI图库设为图库类别的默认应用）
	 */
	private void setDefForJustSysApp(AppCategoryData appCategory){
		if(appCategory == null){
			return ;
		}
		MyArrayList<BaseData> appList = appCategory.getAppList();
		if(appList == null || appList.size() == 1){
			return ;
		}
		
		boolean isHaveThirdPartyApp = false;//是否有第三方应用
		SameCategoryAppInfo iuniApp = null;
		ArrayList <ResolveInfo> resolveInfoList = new ArrayList<ResolveInfo>();
		
		for(int i=0;i<appList.size();i++){
			SameCategoryAppInfo tmpItem = (SameCategoryAppInfo)appList.get(i);
			if(tmpItem == null || 
					tmpItem.getResolveInfo() == null ||
							tmpItem.getResolveInfo().activityInfo == null){
				continue;
			}
			resolveInfoList.add(tmpItem.getResolveInfo());		
			String pkgName = tmpItem.getResolveInfo().activityInfo.packageName; 
			if(ApkUtils.isUserApp(ApkUtils.getApplicationInfo(context,pkgName))){
				isHaveThirdPartyApp = true;
				break; 
			}
			if(Constants.isPackageNameInList(Constants.iuniPackageNameList, pkgName)){
				iuniApp = tmpItem;
			}		
		}
		
		if(!isHaveThirdPartyApp && iuniApp != null){			
			findDefSoftOfOneCategory(appCategory);		
			if(appCategory.getCurDefStartPackageName() == null){//这个类别在没有设置默认软件	        
		        setDefSoft(iuniApp.getResolveInfo(),appCategory.getIntent(),resolveInfoList, true);	
			}			
		}		
	}
	
	/**
	 * 查找所有类别的默认软件
	 */
	public void findDefSoftOfAllCategory(){	
		for(int i=0;i<resultAppCategoryDataList.size();i++){		
			AppCategoryData appCategoryData = (AppCategoryData)resultAppCategoryDataList.get(i);
			findDefSoftOfOneCategory(appCategoryData);		
		}
			
		for(int i=0;i<callBackHandlers.size();i++){
    		Handler handler = callBackHandlers.get(i);
    		if(handler != null){
    			handler.sendEmptyMessage(0);
    		}
    	}
	}
	
	/**
	 * 查找指定类别的默认软件
	 * @param appCategoryData
	 */
	public void findDefSoftOfOneCategory(AppCategoryData appCategoryData){	
		if(appCategoryData == null ||
				appCategoryData.getAppList() == null){
			return ;
		}
		
		appCategoryData.setCurDefStartPackageName(null);
		appCategoryData.setCurDefStartApkName(null);
		appCategoryData.setCurDefStartClass(null);
		
		ResolveInfo matchResolveInfo = context.getPackageManager().resolveActivity(
				appCategoryData.getIntent(),
                PackageManager.MATCH_DEFAULT_ONLY);
		if(matchResolveInfo == null || 
				matchResolveInfo.activityInfo == null){
			return ;
		}
		ActivityInfo matchActivityInfo = matchResolveInfo.activityInfo;
		
		
		ActivityInfo tmpActivityInfo = null;
		for(int i=0;i<appCategoryData.getAppList().size();i++){
			SameCategoryAppInfo item = (SameCategoryAppInfo)appCategoryData.getAppList().get(i);
			if(item.getResolveInfo() == null || 
					item.getResolveInfo().activityInfo == null){
				continue;
			}
			tmpActivityInfo = item.getResolveInfo().activityInfo;
						
			if(matchActivityInfo.packageName.equals(tmpActivityInfo.packageName) &&
					matchActivityInfo.name.equals(tmpActivityInfo.name)){
				appCategoryData.setCurDefStartClass(tmpActivityInfo.name);
				appCategoryData.setCurDefStartPackageName(tmpActivityInfo.packageName);
				appCategoryData.setCurDefStartApkName(
						(String)item.getResolveInfo().loadLabel(context.getPackageManager()));
				break;
			}
		}
	}
	
    /**
     * 默认软件的个数
     * @return
     */
    public int getDefStartSoftNum(){ 
    	synchronized (resultAppCategoryDataList){
        	int num = 0;   	
        	for(int i=0;i<resultAppCategoryDataList.size();i++){
        		AppCategoryData appCategoryData = (AppCategoryData)resultAppCategoryDataList.get(i);
        		if(appCategoryData != null &&
        				!StringUtils.isEmpty(appCategoryData.getCurDefStartPackageName())){
        			num++;
        		}
        	}
        	return num;
    	}
    }
    
    /**
     * 没有设置默认启动项的个数
     * @return
     */
    public int getNotSetDefSoftNum(){    
    	synchronized (resultAppCategoryDataList){
        	int num = 0;   	
        	for(int i=0;i<resultAppCategoryDataList.size();i++){
        		AppCategoryData appCategoryData = (AppCategoryData)resultAppCategoryDataList.get(i);
        		if(appCategoryData == null || 
        				StringUtils.isEmpty(appCategoryData.getCurDefStartPackageName())){
        			num++;
        		}
        	}
        	return num;
    	}
    }
    
    /**
     * 返回值不可能为null
     * @return
     */
    public MyArrayList<BaseData> getAppCategoryDataList(){ 	
    	synchronized (resultAppCategoryDataList){
    		return resultAppCategoryDataList;
    	}   	
    }
	
	public void setDefSoft(ResolveInfo ri, 
			Intent intent, 
			ArrayList <ResolveInfo> resolveInfoList,
			boolean alwaysCheck) {
        if (alwaysCheck) {
            // Build a reasonable intent filter, based on what matched.
            IntentFilter filter = new IntentFilter();

            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            Set<String> categories = intent.getCategories();
            if (categories != null) {
                for (String cat : categories) {
                    filter.addCategory(cat);
                }
            }
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            int cat = ri.match&IntentFilter.MATCH_CATEGORY_MASK;
            Uri data = intent.getData();
            if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
                String mimeType = intent.resolveType(context);
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        filter = null;
                    }
                }
            }
            if (data != null && data.getScheme() != null) {
                // We need the data specification if there was no type,
                // OR if the scheme is not one of our magical "file:"
                // or "content:" schemes (see IntentFilter for the reason).
                if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                        || (!"file".equals(data.getScheme())
                                && !"content".equals(data.getScheme()))) {
                    filter.addDataScheme(data.getScheme());
    
                    // Look through the resolved filter to determine which part
                    // of it matched the original Intent.
                    Iterator<IntentFilter.AuthorityEntry> aIt = null;
                    if(ri.filter != null){
                    	aIt = ri.filter.authoritiesIterator();
                    }
                    if (aIt != null) {
                        while (aIt.hasNext()) {
                            IntentFilter.AuthorityEntry a = aIt.next();
                            if (a.match(data) >= 0) {
                                int port = a.getPort();
                                filter.addDataAuthority(a.getHost(),
                                        port >= 0 ? Integer.toString(port) : null);
                                break;
                            }
                        }
                    }
                    Iterator<PatternMatcher> pIt = null;
                    if(ri.filter != null){
                    	pIt = ri.filter.pathsIterator();
                    }
                    if (pIt != null) {
                        String path = data.getPath();
                        while (path != null && pIt.hasNext()) {
                            PatternMatcher p = pIt.next();
                            if (p.match(path)) {
                                filter.addDataPath(p.getPath(), p.getType());
                                break;
                            }
                        }
                    }
                }
            }

            if (filter != null) {
                final int N = resolveInfoList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                for (int i=0; i<N; i++) {
                    ResolveInfo r = resolveInfoList.get(i);
                    set[i] = new ComponentName(r.activityInfo.packageName,
                            r.activityInfo.name);
                    if (r.match > bestMatch) bestMatch = r.match;
                }
                
        		ComponentName component = new ComponentName(ri.activityInfo.packageName,
      				  ri.activityInfo.name);
        		
                context.getPackageManager().addPreferredActivity(filter, bestMatch, set,
                		component);
            }
        }
    }
	
	public static void releaseObject(){
		if(instance != null){
			if(instance.resultAppCategoryDataList != null){
				instance.resultAppCategoryDataList.clear();
			}
		}
		instance = null;
	}	
}
