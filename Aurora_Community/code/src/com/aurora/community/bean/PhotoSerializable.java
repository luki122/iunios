package com.aurora.community.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**    
 */
public class PhotoSerializable implements Serializable {

	private static final long serialVersionUID = 1L;
	private static ArrayList<PhotoInfo> list;
    private ArrayList<AlbumInfo> albumList;
    private ArrayList<String> urlList;
    
	public ArrayList<PhotoInfo> getList() {
		return list;
	}

	public void setList(ArrayList<PhotoInfo> list) {
		this.list = list;
	}
	
	public void setAlbumList(ArrayList<AlbumInfo> list){
		this.albumList = list;
	}
	
	public ArrayList<AlbumInfo> getAlbumList(){
		return albumList;
	}
	
	public void setUrlList(ArrayList<String> l){
		this.urlList = l;
	}
	
	public ArrayList<String> getUrlList(){
		return this.urlList;
	}
	
}
