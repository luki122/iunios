package com.aurora.downloadIcon.bean;

import java.util.List;

import android.R.integer;

public class IconResponseObject {
	private List<IconResponseProp> iconItems; 

	public List<IconResponseProp> getIconItems() {
		return iconItems;
	}

	public void setIconItems(List<IconResponseProp> iconItems) {
		this.iconItems = iconItems;
	}
	
	public void copy2List(List<IconResponseProp> items){
		for(IconResponseProp prop : iconItems){
			items.add(prop);
		}
	}
	
	public String toJson(){
		if(iconItems.size()<=0)return null;
		String result = "{\"iconItems\":[";
		int i = 0;
		for(IconResponseProp prop:iconItems){
			result +=prop.toString();
			i++;
			if(i!=(iconItems.size())){
				result+=",";
			}
		}
		result += "]}";
		return result;
	}
}
