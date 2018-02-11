package com.secure.adapter;

import java.util.ArrayList;
import java.util.List;
import com.aurora.secure.R;
import com.secure.data.BaseData;
import com.secure.stickylistheaders.StickyListHeadersAdapter;
import com.secure.utils.ApkUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.viewcache.NetManageItemCache;
import com.secure.data.AppInfo;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.secure.activity.BackgroundNetManageActivity;

public class BackgroundNetManageAdapter extends ArrayAdapter<BaseData> implements 
															StickyListHeadersAdapter,
															OnClickListener{      
	private String netOpenStr,netCloseStr;
	private int netCloseAppNum;	
    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;  
    private ArrayList<NetState> netStateList = new ArrayList<NetState>();
	
	public BackgroundNetManageAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
		updateAppNetSwitchState();
		netOpenStr = getContext().getString(R.string.can_use_2G_3G_network);
		netCloseStr = getContext().getString(R.string.prohibit_use_2G_3G_network_num);
		initData();
	}
	
	private void initData(){
        mBackgroundThread = new HandlerThread("BackgroundNetManageAdapter:Background");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());  
    }
	 
	public void updateAppNetSwitchState(){		
		this.netCloseAppNum = 0;
		if(getCount() > 0){
			netStateList.clear();
			for(int i=0;i<getCount();i++){
				NetState state = new NetState(((AppInfo)getItem(i)).getIsNetPermissionOpen());
				if(!state.isOpen){
					this.netCloseAppNum++;
				}
				netStateList.add(state);
			}
		}	
	}
		
	private String getLabelText(int position){		
		String labelText = "";				
		if(position < netStateList.size() && netStateList.get(position).isOpen){
			labelText = netOpenStr;
		}else{
			labelText = netCloseAppNum+netCloseStr;
		}		
		return labelText;
	}

	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {	
		NetManageItemCache holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.net_manage_list_item, parent, false);
			holder = new NetManageItemCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (NetManageItemCache)convertView.getTag();
		}
		
		if(getCount()<=position){
			return convertView;
		}
		
		AppInfo item =(AppInfo)getItem(position);
		holder.getAppName().setText(item.getAppName());
		
		int progress;
		long totalNetFlow = ((BackgroundNetManageActivity)getContext()).getTotalNetFlow();
		if(totalNetFlow>0){
			progress = (int)(item.getApkTotalBackgroundNetBytes()*100/totalNetFlow);
			//对于百分比不够百分之一的，按照百分之一来显示
			if(progress == 0 && item.getApkTotalBackgroundNetBytes()>0){
				progress = 1;
			}				
			holder.getProgressBar().setProgress(progress);
			holder.getNetworkTraffic().setText(Utils.dealMemorySize(getContext(), 
					item.getApkTotalBackgroundNetBytes()));
		}else{
			holder.getProgressBar().setProgress(0);
			holder.getNetworkTraffic().setText("0.00B");
		}
		
		if(item.getIsNetPermissionOpen()){
			holder.getNetSwitch().setImageResource(R.drawable.net_switch_def_on);
		}else{
			holder.getNetSwitch().setImageResource(R.drawable.net_switch_def_off);
		}	
		holder.getNetSwitch().setTag(position);
		holder.getNetSwitch().setOnClickListener(this);
		
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
	
	@Override 
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {		
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.net_manage_list_header, parent, false);
		}
		
		TextView labelText = (TextView)convertView.findViewById(R.id.labelText);
		labelText.setText(getLabelText(position));
		return convertView;
	}

	//remember that these have to be static, postion=1 should walys return the same Id that is.
	@Override
	public long getHeaderId(int position) {		
		String labelText = getLabelText(position);
		if(!StringUtils.isEmpty(labelText)){
			return labelText.charAt(0);
		}else{
			return 0;
		}		
	}

	@Override
	public void onClick(View v) {
		Object tagObject= v.getTag();
		if(tagObject != null){
			int position = Integer.parseInt(tagObject.toString());
			if(getCount()<=position){
				return ;
			}
			
			AppInfo item =(AppInfo)getItem(position);
			if(item == null){
				return ;
			}
			
			switch(v.getId()){
			case R.id.netSwitch:
				Message msg = mBackgroundHandler.obtainMessage();
				msg.obj = item;
				if(item.getIsNetPermissionOpen()){
					msg.what = 1;
				}else{
					msg.what = 2;			
				}
				mBackgroundHandler.sendMessage(msg);			
				break;
			}			
		}
	}
	
	final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
           switch(msg.what){
           case 1:
        	   ApkUtils.closeApkNetwork(getContext(),(AppInfo)msg.obj);
        	   break;
           case 2:
        	   ApkUtils.openApkNetwork(getContext(),(AppInfo)msg.obj);	
        	   break;
           }
	    };
    }
	    
    public void releaseObject(){
    	netStateList.clear();
	}
	    
    final class NetState{
    	public NetState(boolean isOpen){
    		this.isOpen = isOpen;
    	}
    	boolean isOpen;
    }
}
