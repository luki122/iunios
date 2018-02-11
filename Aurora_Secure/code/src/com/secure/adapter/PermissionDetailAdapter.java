package com.secure.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import aurora.app.AuroraActivity;

import com.aurora.secure.R;
import com.secure.activity.PermissionDetailActivity;
import com.secure.data.AppInfo;
import com.secure.data.Constants;
import com.secure.data.PermissionInfo;
import com.secure.model.LBEmodel;
import com.secure.stickylistheaders.StickyListHeadersAdapter;
import com.secure.utils.ApkUtils;
import com.secure.utils.StringUtils;
import com.secure.viewcache.PermissionDetailItemCache;

public class PermissionDetailAdapter extends ArrayAdapter<PermissionInfo> implements 
                           StickyListHeadersAdapter,
                           SectionIndexer,
                           OnCheckedChangeListener{      
	private AppInfo curAppInfo;
	private int[] sectionIndices;
	private Character[] sectionsLetters;
	private HashMap<String,String> sectionsLetterMap;
	private HashMap<PermissionInfo,TextView> switchViewMap = 
			new HashMap<PermissionInfo,TextView>();
	private Activity mActivity;
	public PermissionDetailAdapter(Activity activity,
			List<PermissionInfo> listData,
			AppInfo curAppInfo) {		
		super(activity, 0, listData);
		this.curAppInfo = curAppInfo;
		initSectionsLetterMap();	
		sectionIndices = getSectionIndices();
		sectionsLetters = getStartingLetters();
		this.mActivity = activity;
	}
	
	private void initSectionsLetterMap(){
		sectionsLetterMap = new HashMap<String,String>();
		sectionsLetterMap.put("A", getContext().getString(R.string.suggest_close));
		sectionsLetterMap.put("B", getContext().getString(R.string.suggest_open));
	}
	
	private int[] getSectionIndices() {		
		ArrayList<Integer> sectionIndices = new ArrayList<Integer>();	
		
		if(getCount()>0){
			PermissionInfo item = (PermissionInfo) getItem(0);
			boolean isDangerOfLastApp,isDangerApp;
			if(ApkUtils.isDangerPermission(getContext(),curAppInfo,item)){
				isDangerOfLastApp = true;
			}else{
				isDangerOfLastApp = false;
			}
			sectionIndices.add(0);
			for (int i = 1; i < getCount(); i++) {
				item = (PermissionInfo)getItem(i);
				if(ApkUtils.isDangerPermission(getContext(),curAppInfo,item)){
					isDangerApp = true;
				}else{
					isDangerApp = false;
				}
				if(isDangerApp != isDangerOfLastApp) {
					isDangerOfLastApp = isDangerApp;
					sectionIndices.add(i);
				}
			}
		}		
		int[] sections = new int[sectionIndices.size()];
		for (int i = 0; i < sectionIndices.size(); i++) {
			sections[i] = sectionIndices.get(i);
		}
		return sections;
	}
	
	private Character[] getStartingLetters() {
		Character[] letters = new Character[sectionIndices.length];
		for (int i = 0; i < sectionIndices.length; i++) {
			int index = sectionIndices[i];		
			String labelText = getLabelTextIndexes(index);
			if(!StringUtils.isEmpty(labelText)){
				letters[i] = labelText.charAt(0);
			}		
		}
		return letters;
	}
	
	private String getLabelTextIndexes(int position){
		String labelText = "";
		if(position >= getCount() || position < 0){
			return labelText;
		}
		PermissionInfo item = (PermissionInfo) getItem(position);
		
		if(item == null){
			return labelText;
		}
		if(ApkUtils.isDangerPermission(getContext(),curAppInfo,item)){
			labelText = "A";
		}else{
			labelText = "B";
		}	
		return labelText;
	}

	@Override 
	public View getView(int position, View convertView, ViewGroup parent) {	
		PermissionDetailItemCache holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.permission_detail_list_item, parent, false);
			holder = new PermissionDetailItemCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (PermissionDetailItemCache)convertView.getTag();
		}
		holder.setPosition(position);
		if(getCount()<=position){
			return convertView;
		}
		
		PermissionInfo item =(PermissionInfo)getItem(position);
		holder.getPermissionName().setText(
				LBEmodel.getInstance(getContext()).getPermName(item.permId));			
		holder.getPermissionIcon().setImageResource(Constants.getLbePermissionIcon(item.permId));	
		
		if(isSectionIndicesHave(position) && position+1<getCount() && !isSectionIndicesHave(position+1)){
			holder.getItemLayout().setBackgroundResource(R.drawable.item_of_up_bg);
		}else if(!isSectionIndicesHave(position) && position+1<getCount() && !isSectionIndicesHave(position+1)){
			holder.getItemLayout().setBackgroundResource(R.drawable.item_of_middle_bg);
		}else if(!isSectionIndicesHave(position) && ( position+1>=getCount() || isSectionIndicesHave(position+1))){
			holder.getItemLayout().setBackgroundResource(R.drawable.item_of_bottom_bg);
		}else{
			holder.getItemLayout().setBackgroundResource(R.drawable.item_of_alone_bg);
		}
		
//		switchViewMap.put(item, holder.getPermissionSwitch());
		holder.getPermissionSwitch().setTag(createSwitchViewTag(position));
		holder.getPermissionSwitch().setText(item.getStateTextRes());
//		if(item.getIsOpen()){
//			holder.getPermissionSwitch().setChecked(true);
//		}else{
//			holder.getPermissionSwitch().setChecked(false);
//		}
		if(getCount()-1 == position){
			convertView.findViewById(R.id.bottomSpace).setVisibility(View.VISIBLE);
		}else{
			convertView.findViewById(R.id.bottomSpace).setVisibility(View.GONE);
		}
		convertView.setOnClickListener(onClickListener);
//		holder.getPermissionSwitch().setOnCheckedChangeListener(this);				
		return convertView;
	}
	
	private int clickPosition = -1;
	
	public int getPosition(){
		return clickPosition;
	}
	
	private OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			PermissionDetailItemCache holder = (PermissionDetailItemCache)arg0.getTag();
			clickPosition = holder.getPosition();
			((AuroraActivity)mActivity).showCustomMenu();
		}
	};
	
	/**
	 * 判断sectionIndices数组中有没有what这个值
	 * @param what
	 * @return
	 */
	private boolean isSectionIndicesHave(int what){
		for(int i=0;i<sectionIndices.length;i++){
			if(what == sectionIndices[i]){
				return true;
			}
		}
		return false;
	}

	@Override 
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {		
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.permission_detail_list_header, parent, false);
		}
		
		TextView labelText = (TextView)convertView.findViewById(R.id.labelText);
		if(sectionsLetterMap != null){
			labelText.setText(sectionsLetterMap.get(getLabelTextIndexes(position)));
		}	
		return convertView;
	}

	//remember that these have to be static, postion=1 should walys return the same Id that is.
	@Override
	public long getHeaderId(int position) {		
		String labelText = getLabelTextIndexes(position);
		if(!StringUtils.isEmpty(labelText)){
			return labelText.charAt(0);
		}else{
			return 0;
		}	
	}

	@Override
	public int getPositionForSection(int section) {
		if(section >= sectionIndices.length) {
			section = sectionIndices.length-1;
		}else if(section < 0){
			section = 0;
		}
		return sectionIndices[section];
	}

	@Override
	public int getSectionForPosition(int position) {
		for (int i = 0; i < sectionIndices.length; i++) {
			if(position < sectionIndices[i]) {
				return i-1;
			}
		}
		return sectionIndices.length-1;
	}

	@Override
	public Object[] getSections() {
		return sectionsLetters;
	}
	
	public void clear(){
		sectionIndices = new int[0];
		sectionsLetters = new Character[0];
		notifyDataSetChanged();
	}
	
	public void restore(){
		sectionIndices = getSectionIndices();
		sectionsLetters = getStartingLetters();
		notifyDataSetChanged();
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
		Object tagObject= buttonView.getTag();
		if(tagObject != null){
			int position = getPositionBySwitchViewTag(tagObject);
			if(getCount()<=position){
				return ;
			}
			
			final PermissionInfo item =(PermissionInfo)getItem(position);
			switch(buttonView.getId()){
			case R.id.permissionSwitch:
				if(curAppInfo == null){
					return ;
				}
				
				if(item.getIsOpen() == isChecked){
					return ;//当前显示的与数据库记录的数据一样的，因此无须修改
				}
				
				new Thread() {
					@Override
					public void run() {
						if(isChecked){
							ApkUtils.openApkAppointPermission(getContext(), curAppInfo, item);
						}else{
							ApkUtils.closeApkAppointPermission(getContext(), curAppInfo, item);
						}
					}
				}.start();	
				break;
			}			
		}	
	}
	
	/**
	 * 更新指定权限的开关状态
	 * @param permissionInfo
	 */
	public void updateOfPermState(PermissionInfo permissionInfo){
		if(permissionInfo == null){
			return ;
		}
		
		int count = getCount();
		for(int i = 0; i<count; i++)
		{
			if(getItem(i).permId == permissionInfo.permId)
			{
				getItem(i).setCurState(permissionInfo.getCurState());
				notifyDataSetChanged();
				break;
			}
		}
//		AuroraSwitch auroraSwitch =switchViewMap.get(permissionInfo);
//		if(auroraSwitch != null && 
//				auroraSwitch.isChecked() != permissionInfo.getIsOpen()){
//			auroraSwitch.setChecked(permissionInfo.getIsOpen());
//		}
	}
	
	private Object createSwitchViewTag(int position){
		return position;
	}
	
	private int getPositionBySwitchViewTag(Object tag){
		return Integer.parseInt(tag.toString());
	}
	
	public void realseObject(){
		if(sectionsLetterMap != null){
			sectionsLetterMap.clear();
		}
		
		if(switchViewMap != null){
			switchViewMap.clear();
		}	
	}
}
