package com.aurora.launcher;

import java.util.ArrayList;

import com.aurora.launcher.WidgetsManagerActivity.IconCompound;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class WidgetsListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private Launcher mLauncher;
	private IconCompound multiItems[];
	ContentResolver cr;
	
	public WidgetsListAdapter(Context context , IconCompound iconCompound[]) {
		// TODO Auto-generated constructor stub
		mLauncher =(Launcher) context;
		inflater = LayoutInflater.from(mLauncher);
		multiItems = iconCompound;
		cr = mLauncher.getContentResolver();
	}

	private static class Holder{  
        TextView tv;  
        Switch mSwitch;  
    }  
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		
		return multiItems.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final Holder holder;  
        if(convertView==null){  
              
            convertView=inflater.inflate(R.layout.widget_list_item, null);  
            holder=new Holder();  
            holder.tv = (TextView) convertView.findViewById(R.id.widget_icon_title);  
            holder.mSwitch = (Switch) convertView.findViewById(R.id.widget_item_switch);  
              
            convertView.setTag(holder);  
              
        }else{  
            holder=(Holder) convertView.getTag();  
        }  
        holder.tv.setCompoundDrawablesWithIntrinsicBounds(multiItems[position].icon, null, null, null);
        holder.tv.setText(multiItems[position].title);
        //holder.mSwitch.setOnCheckedChangeListener(null);
        holder.mSwitch.setChecked(multiItems[position].isChecked);        
        /*holder.mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton mSwitch, boolean checked) {
				// TODO Auto-generated method stub
				ArrayList<ShortcutInfo>  itemInfoList = LauncherModel.getAllShortcutsAndApps();
				String pkgName = multiItems[position].pkgName;
				for(ShortcutInfo mInfo : itemInfoList){
				  if(pkgName.equals(mInfo.getPackageName()) ){
					  ContentValues values = new ContentValues();
	            	  values.put(LauncherSettings.Favorites.ENABLE_WIDGETS, checked? "1":"0");
	            	  int tempId = -1;
	            	  if(checked){
	            		  AppWidgetProviderInfo providerInfo = mLauncher.getPkgProviderMap().get(mInfo.getPackageName()).get(mInfo.widgetIndex).providerInfo;
	            		  tempId = mLauncher.mAppWidgetHost.allocateAppWidgetId();
	            		  mLauncher.mAppWidgetManager.bindAppWidgetIdIfAllowed(tempId, providerInfo.provider);
	            	  }
	            	  values.put(LauncherSettings.Favorites.APPWIDGET_ID, tempId);
	            	int count = cr.update(LauncherSettings.Favorites.getContentUri(mInfo.id, false), values, null, null);	
	            	if(count > 0){
	            	multiItems[position].isChecked = checked;
					if( checked ){
					    mInfo.enableWidgets = 1;
					    mInfo.appWidgetId = tempId;
	            	    mInfo.hostView = mLauncher.mAppWidgetHost.createView(mLauncher.getApplicationContext(), mInfo.appWidgetId, mLauncher.mAppWidgetManager.getAppWidgetInfo(mInfo.appWidgetId));   
					}else{
						mInfo.enableWidgets = 0;
						ArrayList<ProviderAndDimen> padEntity = mLauncher.getPkgProviderMap().get(pkgName);
						for(ProviderAndDimen pad: padEntity){
							pad.hostView = null;
						}
						mLauncher.mAppWidgetHost.deleteAppWidgetId(mInfo.appWidgetId);
	            		mInfo.appWidgetId = -1;
						mInfo.hostView = null;
					}
	            	}
	            	else{
	            		Log.e("mywidget", "update launcher.db field 'enableWidgets' error");
	            	}
				  }
				}
				
				
			}});*/
        convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				boolean checked = !(holder.mSwitch.isChecked());
				holder.mSwitch.setChecked(checked);
				ArrayList<ShortcutInfo>  itemInfoList = LauncherModel.getAllShortcutsAndApps();
				String pkgName = multiItems[position].pkgName;
				for(ShortcutInfo mInfo : itemInfoList){
				  if(pkgName.equals(mInfo.getPackageName()) ){
					  ContentValues values = new ContentValues();
	            	  values.put(LauncherSettings.Favorites.ENABLE_WIDGETS, checked? "1":"0");
	            	  int tempId = -1;
	            	  if(checked){
	            		  AppWidgetProviderInfo providerInfo = mLauncher.getPkgProviderMap().get(mInfo.getPackageName()).get(mInfo.widgetIndex).providerInfo;
	            		  tempId = mLauncher.mAppWidgetHost.allocateAppWidgetId();
	            		  mLauncher.mAppWidgetManager.bindAppWidgetIdIfAllowed(tempId, providerInfo.provider);
	            	  }
	            	  values.put(LauncherSettings.Favorites.APPWIDGET_ID, tempId);
	            	int count = cr.update(LauncherSettings.Favorites.getContentUri(mInfo.id, false), values, null, null);	
	            	if(count > 0){
	            	multiItems[position].isChecked = checked;
					if( checked ){
					    mInfo.enableWidgets = 1;
					    mInfo.appWidgetId = tempId;
	            	    mInfo.hostView = mLauncher.mAppWidgetHost.createView(mLauncher.getApplicationContext(), mInfo.appWidgetId, mLauncher.mAppWidgetManager.getAppWidgetInfo(mInfo.appWidgetId));   
					}else{
						mInfo.enableWidgets = 0;
						/*ArrayList<ProviderAndDimen> padEntity = mLauncher.getPkgProviderMap().get(pkgName);
						for(ProviderAndDimen pad: padEntity){
							pad.hostView = null;
						}*/
						mLauncher.mAppWidgetHost.deleteAppWidgetId(mInfo.appWidgetId);
	            		mInfo.appWidgetId = -1;
						mInfo.hostView = null;
					}
	            	}
	            	else{
	            		Log.e("mywidget", "update launcher.db field 'enableWidgets' error");
	            	}
				  }
				}
			}
		});
        convertView.setEnabled(multiItems[position].enable);
        holder.mSwitch.setEnabled(multiItems[position].enable);
        return convertView;  
	}

}
