package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

import com.aurora.iunivoice.bean.PostInfo;

public class PostListObject {

	private ArrayList<PostInfo> forum_threadlist;

	public ArrayList<PostInfo> getForum_threadlist() {
		return forum_threadlist;
	}

	public void setForum_threadlist(ArrayList<PostInfo> forum_threadlist) {
		this.forum_threadlist = forum_threadlist;
	}

}
