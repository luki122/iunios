package com.android.browser;

import java.util.ArrayList;

import com.android.browser.BookmarksFolderActivity.FolderInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


public class BookmarksFolderAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater layoutInflater;
	private ArrayList<FolderInfo> folderList;
	private long selectedId;
	private int defaultSpaceDp;
	
	public BookmarksFolderAdapter(Context context, ArrayList<FolderInfo> folderList, long selectedId) {
		this.mContext = context;
		this.layoutInflater = LayoutInflater.from(context);
		this.folderList = folderList;
		this.selectedId = selectedId;
	}
	
	@Override
	public int getCount() {
		return folderList.size();
	}

	@Override
	public FolderInfo getItem(int arg0) {
		return folderList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	class ViewHolder {
		ImageView ivFolder;
		TextView tvFolderName;
		ImageView ivSelectedFolder;
	}
	
	@Override
	public View getView(int position, View converView, ViewGroup arg2) {
		FolderInfo info = folderList.get(position);
		ViewHolder holder;
		if(converView == null) {
			converView = layoutInflater.inflate(R.layout.bookmarks_folder_list_item, null);
			holder = new ViewHolder();
			holder.ivFolder = (ImageView)converView.findViewById(R.id.iv_bookmarks_folder_list_item);
			holder.tvFolderName = (TextView)converView.findViewById(R.id.tv_bookmarks_folder_list_item_foldername);
			holder.ivSelectedFolder = (ImageView)converView.findViewById(R.id.iv_bookmarks_folder_list_item_selected);
			converView.setTag(holder);
		}else {
			holder = (ViewHolder)converView.getTag();
		}
		int eachSpace = BaseUi.dip2px(mContext, defaultSpaceDp);
		int origSpace = BaseUi.dip2px(mContext, BookmarksFolderActivity.rootFolderLeftMargin);
		for(int i=0;i<info.level;i++) {
			origSpace += eachSpace;
		}
		LinearLayout.LayoutParams params = (LayoutParams) holder.ivFolder.getLayoutParams();
		params.leftMargin = origSpace;
		holder.ivFolder.setLayoutParams(params);
		holder.tvFolderName.setText(info.title.equals("Bookmarks") ? mContext.getResources().getString(R.string.bookmarks) : info.title);
		if(info.id == selectedId) {
			holder.ivSelectedFolder.setVisibility(View.VISIBLE);
		}else {
			holder.ivSelectedFolder.setVisibility(View.INVISIBLE);
			
		}
		
		return converView;
	}
	
	public void setDefaultSpaceDp(int defaultSpaceDp) {
		this.defaultSpaceDp = defaultSpaceDp;
	}

	public void setSelectedId(long selectedId) {
		this.selectedId = selectedId;
	}
	
}
