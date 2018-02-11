package com.aurora.community.bean;

import java.io.Serializable;

public class TwitterSelectedObject  implements Serializable{

	private static final long serialVersionUID = 2L;
	
	public String selectedName;
	public String url;
	
	public boolean isSelected;
	public boolean isAddIcon;
	
	public void setSelectedName(String sel){
		this.selectedName = sel;
	}
	
	public String getSelectedName(){
		return selectedName;
	}
	
	public void setUrl(String uri){
		this.url = uri;
	}
	
	public String getUrl(){
		return this.url;
	}
	
	public void  setSelect(boolean s){
		this.isSelected = s;
	}
	
	public boolean getSelected(){
		return this.isSelected;
	}
	
	public void setAddIcon(boolean a){
		this.isAddIcon = a;
	}
	
	public boolean isAddIcon(){
		return this.isAddIcon;
	}
}
