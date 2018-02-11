package com.aurora.datauiapi.data.bean;

import java.io.Serializable;

public class PhotoInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private int image_id;
	private String path_file;
	private String path_absolute;
	private boolean choose = false;
	
	private int type;
	
	private boolean isCapture = false;
	
	public int getImage_id() {
		return image_id;
	}
	public void setImage_id(int image_id) {
		this.image_id = image_id;
	}
	public String getPath_file() {
		return path_file;
	}
	public void setPath_file(String path_file) {
		this.path_file = path_file;
	}
	public String getPath_absolute() {
		return path_absolute;
	}
	public void setPath_absolute(String path_absolute) {
		this.path_absolute = path_absolute;
	}
	public boolean isChoose() {
		return choose;
	}
	public void setChoose(boolean choose) {
		this.choose = choose;
	}
	
	public boolean  isCapture(){
		return isCapture;
	}
	
	public void setCapture(boolean bCapture){
		isCapture = bCapture;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public int getType(){
		return this.type;
	}
}
