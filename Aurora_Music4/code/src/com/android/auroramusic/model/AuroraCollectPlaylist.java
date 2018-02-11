package com.android.auroramusic.model;

public class AuroraCollectPlaylist {

	private String playlistname;
	private String playlistid;
	private String imgUrl;
	private int songSize;
	private String type;
	private int listType;
	private String info;
	
	public AuroraCollectPlaylist(){
		
	}
	
	public AuroraCollectPlaylist(String name,String id,String url,int size,String info,int listtype,String type){
		this.playlistid=id;
		this.playlistname=name;
		this.songSize=size;
		this.imgUrl=url;
		this.type=type;
		this.info=info;
		this.listType=listtype;
	}
	
	public String getPlaylistname() {
		return playlistname;
	}

	public void setPlaylistname(String playlistname) {
		this.playlistname = playlistname;
	}

	public String getPlaylistid() {
		return playlistid;
	}

	public int getListType() {
		return listType;
	}

	public void setListType(int listType) {
		this.listType = listType;
	}

	public void setPlaylistid(String playlistid) {
		this.playlistid = playlistid;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public int getSongSize() {
		return songSize;
	}

	public void setSongSize(int songSize) {
		this.songSize = songSize;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "AuroraCollectPlaylist [playlistname=" + playlistname
				+ ", playlistid=" + playlistid + ", imgUrl=" + imgUrl
				+ ", songSize=" + songSize + ", type=" + type + ", listType="
				+ listType + ", info=" + info + "]";
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

}
