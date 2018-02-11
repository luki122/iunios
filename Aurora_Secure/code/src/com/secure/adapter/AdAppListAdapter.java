package com.secure.adapter;

import java.util.ArrayList;
import java.util.List;

import com.adblock.data.AdProviderData;
import com.adblock.data.AppAdData;
import com.aurora.secure.R;
import com.lbe.security.service.sdkhelper.SDKConstants;
import com.secure.activity.PermissionManageActivity;
import com.secure.data.AppInfo;
import com.secure.data.BaseData;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;
import com.secure.model.AdScanModel;
import com.secure.stickylistheaders.StickyListHeadersAdapter;
import com.secure.utils.ApkUtils;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.viewcache.AdAppListItemCache;
import com.secure.viewcache.PermissionManageItemCache;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * 这个listView悬停会存在一个风险，当两个（或多个）title的第一个字母（或汉字）是一样的时候，
 * 后面的title就会显示不出来，因为title控制是否显示就是根据第一个字符是否一样，如果第一个字符不一样才显示，
 * 如果第一个字符一样的话，就不显示。解决办法：见 PermissionDetailAdapter 或 DefSoftManagelistAdapter
 * @author chengrq
 *
 */
public class AdAppListAdapter extends ArrayAdapter<BaseData> implements 
                           StickyListHeadersAdapter{      	
	private String unBlcokStr,blockedStr;
	private int unBlockAppNum;
	private int BlockAppNum;
	private ArrayList<BlockState> appBlockState = new ArrayList<BlockState>();
	
	public AdAppListAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
		unBlcokStr = getContext().getString(R.string.num_ad_app);
		blockedStr = getContext().getString(R.string.num_app_blocked_ad);	
		updateAppBlcokState();
	}
	
	public void updateAppBlcokState(){
		unBlockAppNum = 0;
		BlockAppNum = 0;
		if(getCount() > 0){
			appBlockState.clear();
			for(int i=0;i<getCount();i++){
				BlockState state = new BlockState(((AppInfo)getItem(i)).getIsBlockAd());
				if(!state.needBlock){
					unBlockAppNum++;
				}else{
					BlockAppNum++;
				}
				appBlockState.add(state);
			}
		}		
	}
	
	private String getLabelText(int position){
		String labelText = "";		
		if(position<appBlockState.size() && !appBlockState.get(position).needBlock){
			labelText = String.format(unBlcokStr,unBlockAppNum);
		}else{
			labelText = String.format(blockedStr,BlockAppNum);
		}	
		return labelText;
	}
	
	private String getLabelFlag(int position){
		String labelText = "";		
		if(position<appBlockState.size() && !appBlockState.get(position).needBlock){
			labelText = "A";
		}else{
			labelText = "B";
		}	
		return labelText;
	}

	@Override 
	public View getView(int position, View convertView, final ViewGroup parent) {	
		AdAppListItemCache holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.ad_app_list_item, parent, false);
			holder = new AdAppListItemCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (AdAppListItemCache)convertView.getTag();
		}
		
		if(getCount()<=position){
			return convertView;
		}
		
		AppInfo item =(AppInfo)getItem(position);
		holder.getAppName().setText(item.getAppName());
		createSubStr(holder.getSubText(),item);
		
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
	 * @param item
	 * @return
	 */
	private void createSubStr(TextView textView, AppInfo item){
		AppAdData appAdData = AdScanModel.getInstance(getContext()).
				getAppAdData(item.getPackageName());
		if(appAdData == null || 
				appAdData.getAdProviderList() == null){
			return ;
		}
		if(item.getIsBlockAd()){
			textView.setText(String.format(getContext().getString(R.string.block_ad_num),
					appAdData.getAdProviderList().size()));
		}else{			
			String str = String.format(getContext().getString(R.string.have_ad_num),
					appAdData.getAdProviderList().size());
			if(appAdData.getIsAdProviderPermRisk()){
				String riskStr = getContext().getString(R.string.risk);
				str = str+getContext().getString(R.string.comma)+riskStr;	
				SpannableStringBuilder builder = new SpannableStringBuilder(str);  
				ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);   			  
				builder.setSpan(redSpan, 
						str.length()-riskStr.length(), 
						str.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
				textView.setText(builder);
			}else{
				textView.setText(str+getContext().getString(R.string.comma)+
						getContext().getString(R.string.no_risk));
			}			 
		}
	}

	@Override 
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {		
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.ad_app_list_header, parent, false);
		}
		
		TextView labelText = (TextView)convertView.findViewById(R.id.labelText);
		labelText.setText(getLabelText(position));
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {		
		String labelText = getLabelFlag(position);
		if(!StringUtils.isEmpty(labelText)){
			return labelText.charAt(0);
		}else{
			return 0;
		}		
	}
	
	public void releaseObject(){
		appBlockState.clear();
	}
	
	final class BlockState{
		public BlockState(boolean needBlock){
			this.needBlock = needBlock;
		}
		boolean needBlock;
	}
}
