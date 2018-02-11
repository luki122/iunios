package com.aurora.datauiapi.data.bean;

import java.util.List;

public class HomepageData {

	private List<HomepageDataInfo> forum_threadlist;
	private List<HomepageSlideInfo> slides;
	private String newguide;

	public List<HomepageDataInfo> getForum_threadlist() {
		return forum_threadlist;
	}

	public void setForum_threadlist(List<HomepageDataInfo> forum_threadlist) {
		this.forum_threadlist = forum_threadlist;
	}

	public List<HomepageSlideInfo> getSlides() {
		return slides;
	}

	public void setSlides(List<HomepageSlideInfo> slides) {
		this.slides = slides;
	}

	public String getNewguide() {
		return newguide;
	}

	public void setNewguide(String newguide) {
		this.newguide = newguide;
	}

}
