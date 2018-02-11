package com.secure.activity;

import java.util.ArrayList;
import java.util.List;
import com.aurora.secure.R;
import com.secure.adapter.DefSoftManagelistAdapter;
import com.secure.adapter.InputMethodGridAdapter;
import com.secure.data.AppCategoryData;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.BaseData;
import com.secure.data.DefStartAppInfo;
import com.secure.data.MyArrayList;
import com.secure.data.SameCategoryAppInfo;
import com.secure.model.ConfigModel;
import com.secure.model.DefSoftModel;
import com.secure.utils.ActivityUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.utils.ActivityUtils.LoadCallback;
import com.secure.view.GridViewForEmbed;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.inputmethod.InputMethodInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class DefSoftManageActivity extends AuroraActivity {
	private DefSoftManagelistAdapter adapter;
	private ListView listView;
	private List<BaseData> appCategoryDataList;
	private GridViewForEmbed inputMethodGridView;
    private TextView inputMethodlabelText;
	private InputMethodGridAdapter inputMethodAdapter;
	private List<InputMethodInfo> inputMethodInfos;
	private View inputMethodView;//listView的Header，显示输入法部分
    private int inputMethodViewHeight = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.def_soft_manage_activity);
        }else{
        	setAuroraContentView(R.layout.def_soft_manage_activity,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.def_soft_manage);
        }
        initView(); 
        initInputMethodView();
        initOrUpdateInputMethodData();
        ActivityUtils.sleepForloadScreen(100,new LoadCallback(){
			@Override
			public void loaded() {
				initData(); 	
			}       	
        });       
    }
    
    private void initView(){
    	listView = (ListView)findViewById(R.id.listView);	
    }
    
    /**
     * 初始化输入法相关view
     */
    private void initInputMethodView(){
    	inputMethodView = getLayoutInflater().inflate(R.layout.def_soft_manage_list_item,null,false);
    	listView.addHeaderView(inputMethodView);
    	inputMethodGridView = (GridViewForEmbed)inputMethodView.findViewById(R.id.itemGridView);
    	inputMethodlabelText = (TextView)findViewById(R.id.labelText);
    }
    
    private void initData(){ 
    	DefSoftModel.getInstance(this).findDefSoftOfAllCategory();//每次进入界面则刷新一次
    	DefSoftModel.getInstance(DefSoftManageActivity.this).attach(updateViewHandler);
//    	if(DefSoftModel.getInstance(DefSoftManageActivity.this).isDuringUpdate()){
//    		//wait,由于耗时不太多所以这里不显示wait动画
//    	}else{
//    		updateViewHandler.sendEmptyMessage(0);
//    	}  	
    	updateViewHandler.sendEmptyMessage(0);
    }
    
    private final Handler updateViewHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		    initOrUpdatetAppsData();
	    }
	};
	
    /**
     * 更新Apps数据
     */
    private void initOrUpdatetAppsData(){  
    	if(listView == null){
    		return ;
    	}
    	
    	if(appCategoryDataList == null){
    		appCategoryDataList = new ArrayList<BaseData>();
    	}else{
    		appCategoryDataList.clear();
    	}
    	
    	MyArrayList<BaseData> golAppCategoryDataList = 
    			DefSoftModel.getInstance(this).getAppCategoryDataList();
    	for(int i=0;i<golAppCategoryDataList.size();i++){
    		BaseData tmpData = golAppCategoryDataList.get(i);
    		if(tmpData == null){
    			continue ;
    		}
			appCategoryDataList.add(tmpData);
		}
    	  	
    	if(adapter == null){			
			adapter = new DefSoftManagelistAdapter(this,appCategoryDataList);
			listView.setAdapter(adapter);
		}else{
			adapter.mNotify();
		}
    	
    	if(appCategoryDataList.size() == 0 && 
    			(inputMethodInfos == null ||
    			 inputMethodInfos.size() <= 1)){
    		findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
    		findViewById(R.id.listView).setVisibility(View.GONE);
    	}else{
    		findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
    		findViewById(R.id.listView).setVisibility(View.VISIBLE);
    	}
    }
    
    /**
     * 更新输入法部分数据
     */
    private void initOrUpdateInputMethodData(){
    	if(inputMethodGridView == null ||
    			inputMethodView == null || 
    			listView == null){
    		return ;
    	}
    	
    	List<InputMethodInfo> tmpInputMethodInfos = ApkUtils.getEnabledInputMethodList(this);
    	if(inputMethodAdapter == null){
    		inputMethodInfos = new ArrayList<InputMethodInfo>();
    		int size = tmpInputMethodInfos==null?0:tmpInputMethodInfos.size();
    		for(int i=0;i<size;i++){
    			inputMethodInfos.add(tmpInputMethodInfos.get(i));
    		}
    		inputMethodAdapter = new InputMethodGridAdapter(this,inputMethodInfos);
    		inputMethodGridView.setAdapter(inputMethodAdapter);
    	}else{
    		inputMethodInfos.clear();
    		int size = tmpInputMethodInfos==null?0:tmpInputMethodInfos.size();
    		for(int i=0;i<size;i++){
    			inputMethodInfos.add(tmpInputMethodInfos.get(i));
    		}
    		inputMethodAdapter.notifyDataSetChanged();
    	}  
    	
    	setInputMethodTitle();
    	if(inputMethodInfos.size()>1){
            //显示listView中的HeaderView
    		inputMethodView.setPadding(0, 0, 0, 0);
    	}else{
    		//收起listView中的HeaderView，注意只能用这种方法收起HeaderView。
    		getInputMethodViewHeight();
    		inputMethodView.setPadding(0, -1 * inputMethodViewHeight, 0, 0);
    	}
    }
    
    private void getInputMethodViewHeight(){
    	if(inputMethodView == null || inputMethodViewHeight>0){
    		return ;
    	}
    	Utils.measureView(inputMethodView);
    	inputMethodViewHeight = inputMethodView.getMeasuredHeight();
    }
    
    /**
     * 设置输入法部分当前的title
     */
    public void setInputMethodTitle(){
    	if(inputMethodlabelText == null || 
    			inputMethodInfos == null ){
    		return ;
    	}
    	
    	String labelText = getString(R.string.input_method);
    	String currentInputMethodId = ApkUtils.getCurrentInputMethodId(this);	
		for(int i=0;i<inputMethodInfos.size();i++){
			InputMethodInfo tmp = inputMethodInfos.get(i);
			if(tmp == null){
				continue;
			}
			if(tmp.getId().equals(currentInputMethodId)){
				labelText = getString(R.string.input_method)+
						getString(R.string.left_brackets)+
						tmp.loadLabel(getPackageManager()).toString()+
						getString(R.string.right_brackets);	
				break;
			}
		}  	
    	inputMethodlabelText.setText(labelText);
    }
    	
	@Override
	protected void onRestart() {
		DefSoftModel.getInstance(this).findDefSoftOfAllCategory();
		initOrUpdateInputMethodData();
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		DefSoftModel.getInstance(this).detach(updateViewHandler);
		releaseObject();
		super.onDestroy();
	}
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
	    if(appCategoryDataList != null){
	    	appCategoryDataList.clear();
	    }
				
		if(inputMethodInfos != null){
			inputMethodInfos.clear();
		}
	}
	
	/**
	 * 刷新listView
	 */
	public void updateListView(){
		if(adapter != null){
			adapter.mNotify();
		}
	}
}
