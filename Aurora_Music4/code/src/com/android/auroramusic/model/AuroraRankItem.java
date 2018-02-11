package com.android.auroramusic.model;

import com.xiami.sdk.entities.RankType;



public class AuroraRankItem {
	private String rankname;
	private int imgUri;
	private String type;
	private RankType ranktype;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AuroraRankItem(String album,int imguri,String type) {
		this.rankname = album;
		this.imgUri = imguri;
		this.type=type;
	}

	public AuroraRankItem(String album,int imguri,RankType type) {
		this.rankname = album;
		this.imgUri = imguri;
		this.ranktype=type;
	}
	
	public String getRankname() {
		return rankname;
	}

	public void setRankname(String rankname) {
		this.rankname = rankname;
	}

	public int getImgUri() {
		return imgUri;
	}

	public void setImgUri(int imgUri) {
		this.imgUri = imgUri;
	}

	public RankType getRanktype() {
		return ranktype;
	}

	public void setRanktype(RankType ranktype) {
		this.ranktype = ranktype;
	}
	
}
