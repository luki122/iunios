package com.secure.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.aurora.secure.R;
import com.secure.activity.CacheManageActivity;
import com.secure.animation.ListItemHorizontalAnimation;
import com.secure.data.AppInfo;
import com.secure.data.BaseData;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;
import com.secure.model.CacheSizeModel;
import com.secure.utils.ApkUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.viewcache.CacheManageListItemCache;
import android.app.Activity;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

/**
 * @author chengrq
 */
public class CacheManageListAdapter extends ArrayAdapter<BaseData> {  
	private final static String VIEW_TAG_OF_CacheText = "@CacheText";
	private final static String VIEW_TAG_OF_CheckBox = "@CheckBox";
	
	/**
	 * 设置cache_manage_list_item.xml中LeftLayout的宽度，
	 * 这个值是相对于320*480（160dpi）屏幕的尺寸值
	 */
	private final int LEFT_LAYOUT_WIDTH = 40;
	private DisplayMetrics metric;
	private int leftLayoutWidth;	
	private int moveDistance;
	private boolean isMidLayoutNearLeft = true;	
	private ArrayList<View> itemViewList;
	private ViewGroup parent;
	private ArrayList<String> needClearList;//存放需要清理软件的packageName
	private ArrayList<CacheManageListItemCache> holderList;
	
	public CacheManageListAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
		metric = Utils.getDisplayMetrics(getContext());
		if(metric != null){
			leftLayoutWidth = LEFT_LAYOUT_WIDTH*metric.densityDpi/160;
			moveDistance = (LEFT_LAYOUT_WIDTH-6)*metric.densityDpi/160;
		}
		itemViewList = new ArrayList<View>();
		needClearList = new ArrayList<String>();
		holderList = new ArrayList<CacheManageListItemCache>();
	}
	
	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {
		this.parent = parent;
		CacheManageListItemCache holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.cache_manage_list_item, parent, false);
			holder = new CacheManageListItemCache(convertView);		
			convertView.setTag(holder);
			holderList.add(holder);
			
			//手动设置LeftLayout，midLayout的长度
			ViewGroup.LayoutParams LeftLayoutParams = holder.getLeftLayout().getLayoutParams();
			LeftLayoutParams.width = leftLayoutWidth;
			holder.getLeftLayout().setLayoutParams(LeftLayoutParams); 
			
			ViewGroup.LayoutParams midLayoutParams  = holder.getMidLayout().getLayoutParams();
			midLayoutParams.width = metric.widthPixels-moveDistance;
			holder.getMidLayout().setLayoutParams(midLayoutParams); 
			
			itemViewList.add(convertView);
		} else {
			holder = (CacheManageListItemCache)convertView.getTag();
		}
				
		if(getCount()<=position){
			return convertView;
		}
		
		AppInfo item =(AppInfo)getItem(position);
		holder.getAppName().setText(item.getAppName());
		holder.getCacheText().setTag(item.getPackageName()+VIEW_TAG_OF_CacheText);
		holder.getCacheText().setText(getContext().getString(R.string.calculating));

		PackageStats packageStats = CacheSizeModel.getInstance(getContext()).
				getPackageStatsMap().get(item.getPackageName());
		if(packageStats != null){
			if(((CacheManageActivity)getContext()).getCurSortWay() ==
					CacheManageActivity.SORT_BY_CACHE_SIZE){
				holder.getCacheText().setText(Utils.dealMemorySize(getContext(), 
						packageStats.cacheSize+packageStats.externalCacheSize));
			}else{
				holder.getCacheText().setText(Utils.dealMemorySize(getContext(), 
						(packageStats.cacheSize+packageStats.externalCacheSize+
								packageStats.codeSize+packageStats.externalCodeSize+
								packageStats.dataSize+packageStats.externalDataSize)));
			}	
		}
		
		//如果当前view没有执行动画，则对view的位置初始化
		if(convertView.getAnimation() == null){
			RelativeLayout.LayoutParams midLayoutParams = 
					(RelativeLayout.LayoutParams)holder.getMidLayout().getLayoutParams();	
			if(isMidLayoutNearLeft){
				midLayoutParams.leftMargin = 0;
				holder.getImgSetGo().setVisibility(View.VISIBLE);
			}else{
				midLayoutParams.leftMargin = moveDistance;
				holder.getImgSetGo().setVisibility(View.INVISIBLE);
			}
			holder.getMidLayout().setLayoutParams(midLayoutParams); 
		}	
		
		holder.getCheckBox().setClickable(false);//设为不允许点击
		holder.getCheckBox().setLongClickable(false);
		holder.getCheckBox().setTag(item.getPackageName()+VIEW_TAG_OF_CheckBox);
		if(isNeedClear(item.getPackageName())){
			holder.getCheckBox().setChecked(true);
		}else{
			holder.getCheckBox().setChecked(false);
		}
		
		String iconViewTag = item.getPackageName()+"@app_icon";
		holder.getAppIcon().setTag(iconViewTag);
		Drawable cachedImage = ImageLoader.getInstance(getContext()).displayImage(
				holder.getAppIcon(),
				item.getPackageName(), 
				iconViewTag, 
			new ImageCallback() {
				public void imageLoaded(Drawable imageDrawable, Object viewTag) {
					if(parent == null || 
							imageDrawable == null || 
									viewTag == null){
						return ;
					}
					ImageView imageViewByTag = (ImageView)parent.findViewWithTag(viewTag);
					if (imageViewByTag != null) {
						imageViewByTag.setImageDrawable(imageDrawable);
					}
				}
		});
		if (cachedImage != null) {
			holder.getAppIcon().setImageDrawable(cachedImage);
		}else{
			holder.getAppIcon().setImageResource(R.drawable.def_app_icon);
		}
		return convertView;
	}
	
	 /**
     * 释放不需要用的对象所占用的堆内存
     */
	public void releaseObject(){		
		if(itemViewList != null){
			itemViewList.clear();
		}
		if(needClearList != null){
			needClearList.clear();
		}
		if(holderList != null){
			holderList.clear();
		}
	}

/**********************************动画相关 begin******************************************/	
	/**
	 * 设置itemView的状态（是勾选状态，非勾选状态）
	 * @param isChoiceState 
	 */
	public void setItemState(boolean isChoiceState){
		if(holderList != null){
			for(int i=0;i<holderList.size();i++){
				CacheManageListItemCache holder = holderList.get(i);
				if(isChoiceState){
					holder.getAppName().setTextColor(
							getContext().getResources().getColor(R.color.black));
					holder.getCacheText().setTextColor(
							getContext().getResources().getColor(R.color.sub_color));
				}else{
					holder.getAppName().setTextColor(
							getContext().getResources().getColorStateList(R.color.list_text_title_selector_color));
					holder.getCacheText().setTextColor(
							getContext().getResources().getColorStateList(R.color.list_text_sub_selector_color));
				}
			}
		}
		
		if(isChoiceState){
			setMidLayoutPlace(false);
		}else{
			setMidLayoutPlace(true);
		}		
	}
	
	/**
	 * 获取itemView是不是勾选状态
	 * @return
	 */
	public boolean isChoiceStateOfItem(){
		if(isMidLayoutNearLeft){
			return false;
		}else{
			return true;
		}
	}
		
	/**
	 * 设置MidLayout的位置
	 * 注意：这个函数必须开线程延时后，再执行动画。
	 * 原因：在弹出底部的menu时候，会缩小listView长度，
	 * 如果listView布局改变未完成，通过convertView.getParent()无法判断某个item是否显示在listView中；  
	 * 只有让listView布局改变完成后，再通过convertView.getParent()就可以判断某个item是否显示在listView中。
	 * 所以让listView布局改变完成后，再在执行动画，就会防止出现有些item无法执行动画的情况，
	 * @param isMidLayoutNearLeft true：表示靠近左边 false 表示靠近右边
	 */
	private void setMidLayoutPlace(final boolean isMidLayoutNearLeft){		
		 final Handler handler = new Handler() {
			   @Override
			   public void handleMessage(Message msg) {
				   CacheManageListAdapter.this.isMidLayoutNearLeft = isMidLayoutNearLeft;	
					if(itemViewList == null){
						return ;
					}
					
					CacheManageListItemCache holder;
					View convertView;
					for(int i =0;i < itemViewList.size(); i++){
						convertView = itemViewList.get(i);
						convertView.clearAnimation();
								
						if(convertView.getParent() == null){
							continue;//表示这个item此时没有添加到listView中
						}
						
						holder = (CacheManageListItemCache)convertView.getTag();				
						if(isMidLayoutNearLeft){
						    animateView(convertView, holder.getLeftLayout(),
						    		holder.getMidLayout(), holder.getImgSetGo(),
						    		ListItemHorizontalAnimation.LEFT);
						}else{
							animateView(convertView, holder.getLeftLayout(),
						    		holder.getMidLayout(), holder.getImgSetGo()
						    		,ListItemHorizontalAnimation.RIGHT);
						}
					}	
			   }
			};
			
			new Thread() {
				@Override
				public void run() {		          	   
		        	   try {
						  Thread.sleep(200);
					   } catch (InterruptedException e) {
						  e.printStackTrace();
					   }
		           handler.sendEmptyMessage(1);   
				}
			}.start(); 
	}
	
	/**
	 * 判断当前的view需不需要执行动画
	 * @param midView
	 * @param type 
	 * ListItemHorizontalAnimation.RIGHT = 0,
	 * ListItemHorizontalAnimation.LEFT = 1;
	 * @return
	 */
    private boolean isNeedAnimate(RelativeLayout midView , int type){
    	boolean needAnimate = true;
    	RelativeLayout.LayoutParams LayoutParams = ((RelativeLayout.LayoutParams) midView.getLayoutParams());
	 
    	if(type == ListItemHorizontalAnimation.LEFT){
    		if(LayoutParams.leftMargin == 0){
    			needAnimate = false;
    		}
    	}else{
    		if(LayoutParams.leftMargin > 0){
    			needAnimate = false;
    		}
    	}
    	return needAnimate;	
    }
    
	private void animateView(View fatherView,
			View leftView,
			RelativeLayout childMidView, 
			View RightView,
			int type) {
		if(isNeedAnimate(childMidView,type)){
			Animation anim = new ListItemHorizontalAnimation(
					childMidView, RightView,
					moveDistance, type
			);
			anim.setDuration(getAnimationDuration());
			anim.setAnimationListener(animationListener);
			fatherView.startAnimation(anim);			
		}		
	}
	
	/**
	 * 动画监听器
	 */
	private AnimationListener animationListener = new Animation.AnimationListener() {
		@Override
		public void onAnimationStart(Animation arg0) {			
		}
		@Override
		public void onAnimationRepeat(Animation arg0) {
			
		}
		@Override
		public void onAnimationEnd(Animation arg0) {
		}
	};
	
	/**
	 * 得到动画执行的时间
	 * @return
	 */
	public int getAnimationDuration() {
		//return 500;
		return AuroraListView.AURORA_ANIM_DURATION;
	}
	
/*************************处理选中事件代码  begin*************************/
	/**
	 * 处理onItemClick事件
	 * @param position
	 */
	public void dealItemClick(int position){
		if(getCount()<=position){
			return ;
		}
		
		AppInfo item =(AppInfo)getItem(position);
		if(item == null){
			return ;
		}
		CheckBox checkView = (CheckBox)parent.findViewWithTag(item.getPackageName()+VIEW_TAG_OF_CheckBox);
		if(isNeedClear(item.getPackageName())){//表示之前是选中状态
			needClearList.remove(item.getPackageName());
			if(checkView != null){
				//checkView.setChecked(false);
				((AuroraCheckBox)checkView).auroraSetChecked(false, true);
			}
		}else{
			needClearList.add(item.getPackageName());	
			if(checkView != null){
				//checkView.setChecked(true);
				((AuroraCheckBox)checkView).auroraSetChecked(true, true);
			}
		}		
	}
	
	/**
	 * 获取存放“需要清理软件的packageName“的lsit
	 * @return
	 */
	public ArrayList<String> getNeedClearList(){
		return needClearList;
	}
	
	/**
	 * 判断当前是不是全选
	 * @return
	 */
	public boolean isAllChoice(){
		if(needClearList == null){
			return false;
		}
		
		if(needClearList.size() == getCount()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 取消已经选择的清理项
	 * @return
	 */
	public void cancelChoiced(){
		if(needClearList == null || itemViewList == null){
			return ;
		}
		needClearList.clear();
		
		CacheManageListItemCache holder;
		View convertView;
		for(int i =0;i < itemViewList.size(); i++){
			convertView = itemViewList.get(i);
			holder = (CacheManageListItemCache)convertView.getTag();		
			holder.getCheckBox().setChecked(false);
		}	
	}
	
	/**
	 * 全选
	 */
	public void checkAllItem(){
		if(needClearList == null){
			needClearList = new ArrayList<String> ();
		}else{
			needClearList.clear();
		}
		for(int i=0;i<getCount();i++){
			AppInfo item =(AppInfo)getItem(i);
			if(item == null){
				continue;
			}
			needClearList.add(item.getPackageName());
		}
		notifyDataSetChanged();
	}
	
	/**
	 * 获取选中的清理项可以节约的空间数
	 * @return
	 */
	public long getNeedClearCanSaveSpace(){
		if(needClearList == null || 
				needClearList.size() == 0){
			return 0;
		}
		long canSaveSpace = 0;
		for(int i=0;i<needClearList.size();i++){
			String packageName = needClearList.get(i);
			PackageStats packageStats = 
					CacheSizeModel.getInstance(getContext()).getPackageStatsMap().get(packageName);
			if(packageStats != null){
				if(((CacheManageActivity)getContext()).getCurSortWay() ==
						CacheManageActivity.SORT_BY_CACHE_SIZE){
					canSaveSpace = canSaveSpace + packageStats.cacheSize+packageStats.externalCacheSize;
				}else{
					canSaveSpace = canSaveSpace + 
							packageStats.cacheSize+packageStats.externalCacheSize+
							packageStats.codeSize+packageStats.externalCodeSize+
							packageStats.dataSize+packageStats.externalDataSize;
				}
			}			
		}
		return canSaveSpace;
	}
	
	/**
	 * 判断当前packageName对应的软件需不需要清理
	 * @param packageName
	 * @return
	 */
	private boolean isNeedClear(String packageName){
		if(needClearList == null || StringUtils.isEmpty(packageName)){
			return false;
		}
		
		for(int i=0;i<needClearList.size();i++){
			String tmpStr = needClearList.get(i);
			if(packageName.equals(tmpStr)){
				return true;
			}
		}
		return false;		
	}
}
