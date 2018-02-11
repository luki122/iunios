package com.aurora.community.bean;

import java.io.Serializable;
import java.util.List;

public class TwitterSelectedObjectSerializable  implements Serializable{

	private static final long serialVersionUID = 2L;

	private List<TwitterSelectedObject> list;
	
	public void setTwitterSelectedObjectList(List<TwitterSelectedObject> l){
		this.list = l;
	}
	
	public List<TwitterSelectedObject> getTwitterSelectedObjectList(){
		return this.list;
	}
}
