package com.android.gallery3d.setting.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraTextView;

import com.android.gallery3d.R;
import com.android.gallery3d.local.BasicActivity;
import com.android.gallery3d.local.GalleryItemActivity;
import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.tools.MediaFileOperationUtil;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;

public class SeletUpdatePopupWindow extends PopupWindow {
	private CheckBox checkBox;
	private AuroraListView listView;
	private AuroraTextView titleView;
	private Button popCancel;
	private View mView;
	private List<MediaFileInfo> fileInfos;
//	private List<MediaFileInfo> selectFileInfos = new ArrayList<MediaFileInfo>();
	private SeletUpdatePopupAdapter adapter;
	private static final String TAG = "AlbumsSelectPopupWindow";
	private MediaFileOperationUtil operationUtil;
	private Context context;

	public SeletUpdatePopupWindow(int aniTabMenu, Context context) {
		super(context);
		this.context = context;
		mView = LayoutInflater.from(context).inflate(R.layout.aurora_cloud_popup, null);
		initView();
		defaultMenu(mView, aniTabMenu);
		operationUtil = MediaFileOperationUtil.getMediaFileOperationUtil(context);

	}

	public void showList(List<MediaFileInfo> fileInfos) {
		this.fileInfos = fileInfos;
		if (this.fileInfos == null) {
			this.fileInfos = new ArrayList<MediaFileInfo>();
		}
		LayoutParams layoutParams = listView.getLayoutParams();
		if (fileInfos.size() > 5) {
			layoutParams.height = (int) context.getResources().getDimension(R.dimen.aurora_album_listview_max_height);
			listView.setLayoutParams(layoutParams);
		}
		adapter = new SeletUpdatePopupAdapter(context, fileInfos);
		listView.setAdapter(adapter);
	}
	
//	public void setSelectFileInfos(List<MediaFileInfo> selectFileInfos){
//		this.selectFileInfos.clear();
//		this.selectFileInfos.addAll(selectFileInfos);
//	}

	public void setTitleText(String title) {
		if (titleView != null) {
			titleView.setText(title);
		}
	}
	
	public void setCheckBoxAndPopCancel(){
		mView.findViewById(R.id.pop_temp_linear).setVisibility(View.GONE);
		if(popCancel!=null){
			popCancel.setVisibility(View.GONE);
		}
	}


	private void initView() {
		if (mView != null) {
			checkBox = (CheckBox) mView.findViewById(R.id.pop_checkbox);
			listView = (AuroraListView) mView.findViewById(R.id.pop_listView);
			titleView = (AuroraTextView) mView.findViewById(R.id.pop_title);
			popCancel = (Button) mView.findViewById(R.id.pop_cancel);
			popCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//					dismiss();
				}
			});
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					LogUtil.d(TAG, "onItemClick::" + position+" context:"+context);
					dismiss();
					if (position == 0) {
						if(context instanceof BasicActivity){
//							((BasicActivity)context).createNewAlbum();
						}
						return;
					}else {
						MediaFileInfo fileInfo = fileInfos.get(position);
						if(fileInfo!=null){
							String path = fileInfo.IsDir?fileInfo.filePath:Utils.getPathFromFilepath(fileInfo.filePath);
							//operationUtil.moveOperation(selectFileInfos, path);
						}
					}
					
				}
			});
		}
	}

	protected void defaultMenu(View view, int aniTabMenu) {
		// 设置默认项
		this.setContentView(view);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new BitmapDrawable());// 设置Menu菜单背景
		this.setAnimationStyle(aniTabMenu);
		this.setFocusable(true);
		this.setOutsideTouchable(false);
	}

	@Override
	public void dismiss() {
		//TODO Auto-generated method stub
		super.dismiss();
		if(adapter != null){
			adapter.dismiss();
		}
	}

}
