package com.aurora.launcher;

import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WidgetsAdapter extends BaseAdapter {
	private ArrayList<AppWidgetProviderInfo> myAppWidgetInfoList;
	private ArrayList<String> myWidgetsDimenList;
	private LayoutInflater inflater;
	private Context mContext;
	public WidgetsAdapter(Context context , ArrayList<AppWidgetProviderInfo> pAdsList, ArrayList<String> widgetsList) {
		// TODO Auto-generated constructor stub
		myAppWidgetInfoList = pAdsList;
		myWidgetsDimenList = widgetsList;
		mContext = context;
		inflater = LayoutInflater.from(mContext);
	}

	private static class Holder{  
        TextView tv;  
        ImageView img;  
    }  
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return myAppWidgetInfoList.size();
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Holder holder;  
        if(convertView==null){  
              
            convertView=inflater.inflate(R.layout.widget_item, null);  
            holder=new Holder();  
            holder.tv=(TextView) convertView.findViewById(R.id.app_widget_dimen);  
            holder.img=(ImageView) convertView.findViewById(R.id.app_widget_preview);  
              
            convertView.setTag(holder);  
              
        }else{  
            holder=(Holder) convertView.getTag();  
        }  
        holder.tv.setText(myWidgetsDimenList.get(position));
        Drawable drawable = mContext.getPackageManager().getDrawable(
        		myAppWidgetInfoList.get(position).provider.getPackageName(), myAppWidgetInfoList.get(position).previewImage, null);
        if(drawable == null){
        	drawable = mContext.getPackageManager().getDrawable(
            		myAppWidgetInfoList.get(position).provider.getPackageName(), myAppWidgetInfoList.get(position).icon, null);
        }
        holder.img.setImageDrawable(drawable);
        return convertView;  
	}

}
