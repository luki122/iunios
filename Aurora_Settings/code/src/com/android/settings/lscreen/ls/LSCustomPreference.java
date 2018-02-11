package com.android.settings.lscreen.ls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.settings.R;
import com.android.settings.lscreen.AppInfo;
import com.android.settings.lscreen.AuroraLSManageModel;
import com.android.settings.lscreen.BaseData;
import com.android.settings.lscreen.DataArrayList;
import com.android.settings.lscreen.LSAppGridAdapter;
import com.android.settings.lscreen.LSContentProvideImp;
import com.android.settings.lscreen.LSProgressDialog;
import com.android.settings.lscreen.LSAppActivity;
import com.android.settings.lscreen.Utils;

import android.app.Activity;
import android.content.Context;
import aurora.preference.AuroraPreference;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LSCustomPreference extends AuroraPreference{

	private Context mContext;  
    private OnItemClickListener mOnItemClickListener;
    private GridView gridView;
    private LSAppGridAdapter adapter;
    private static List<BaseData> lsAppList;
    private LSProgressDialog lScreenProgressDialog;
    private final AppInfo addItem = new AppInfo();
    public static final String ADD_ITEM_FLAG = "add_privacy_item";
      
    public LSCustomPreference(Context mContext)
    {
    	super(mContext);
    	this.mContext=mContext;
    	setLayoutResource(R.layout.ls_custom_preference);
    }
    
    
    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener){  
        this.mOnItemClickListener = mOnItemClickListener;  
    }  
  
    @Override
	protected void onBindView(View view) {
    	super.onBindView(view);
		gridView = (GridView) view.findViewById(R.id.ls_lscreen);
		if (adapter == null) {
			initOrUpdate();
			adapter = new LSAppGridAdapter((Activity) mContext, lsAppList);
			
		}
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(mOnItemClickListener);
	}
    
    /**
     * 加载数据
     *@param numble
     *@return
     *@exception 
     *@author penggangding
     *@Time
     */
	public void initOrUpdate()
    {
    	if(lsAppList!=null)
    	{
    		lsAppList.clear();
    	}else
    	{
    		lsAppList=new ArrayList<BaseData>();
    		AuroraLSManageModel.getInstance(mContext).getALLLSApp();  // 获取到所有锁屏应用放在系统中
    	}
    	
    	DataArrayList<AppInfo> appDatas=LSContentProvideImp.getLSAppInfo(mContext);
    	
    	if(appDatas!=null && appDatas.size() != 0)
    	{
			for (int i = 0; i < appDatas.size(); i++) 
			{
				Log.d("gd","  packageName="+appDatas.get(i).getPackageName());
				if (appDatas.get(i).getPackageName() == null) 
				{
					continue;
				}
				BaseData appInfo = new AppInfo();
                appInfo=AuroraLSManageModel.getInstance(mContext).setPkgToAppInfo(appDatas.get(i).getPackageName());
                if(((AppInfo)appInfo).getAppName()==null || ((AppInfo)appInfo).getPackageName().equals(LSOperator.IUNI_CONTACTS))
                {
                	continue;
                }
				lsAppList.add(appInfo);
			}
    	}else
    	{
    		Log.d("gd", "  ls datebase is null ");
    	}
    	
        sortList(lsAppList);
        
        if(lsAppList.size()<3)
        {
            addItem.setPackageName(ADD_ITEM_FLAG);
            lsAppList.add(addItem);
        }
        notifyChanged();
    }
    
    private void sortList(List<BaseData> appsList){
		Collections.sort(appsList, new Comparator<BaseData>(){
		   public int compare(BaseData s1, BaseData s2) {
			   return Utils.compare(((AppInfo)s1).getAppNamePinYin(),((AppInfo)s2).getAppNamePinYin());
		   }
		});     
	}
    
    public static List<BaseData> getLSAppList()
    {
    	return lsAppList;
    }
    
    public void dealClickEvent(int position)
    {
    	adapter.dealClickEvent(position);
    }
    
    public void onDestory()
    {
    	if(lScreenProgressDialog!=null)
    	{
    		lScreenProgressDialog.close();
    		lScreenProgressDialog=null;
    	}
    	if(adapter!=null)
    	{
    		adapter.releaseObject();
    		adapter.clear();
    	}
    	if(lsAppList!=null)
    	{
    		lsAppList.clear();
    	}
    }
    
}

/*
class DataCache{
	Map<Integer,Object> map = new HashMap<Integer,Object>();
	GridView gridView;
}*/




