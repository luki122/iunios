package com.aurora.community.bean;

import java.util.ArrayList;
import java.util.List;

public class GalleryHolder {

	public ArrayList<AlbumInfo> albumList = new ArrayList<AlbumInfo>();
	
	public ArrayList<PhotoInfo> photoList = new ArrayList<PhotoInfo>();
	
	public GalleryHolder( ArrayList<AlbumInfo> albumList,ArrayList<PhotoInfo> photoList){
		this.albumList = albumList;
		this.photoList = photoList;
	}
	
	public void setAlbumList(AlbumInfo info){
		albumList.add(info);
	}
	
	public void setPhotoList(PhotoInfo info){
		photoList.add(info);
	}
	
	public ArrayList<AlbumInfo> getAlbumList(){
		return albumList;
	}
	
	public ArrayList<PhotoInfo> getPhotoList(){
		return photoList;
	}
	
}
