package com.android.gallery3d.xcloudalbum.widget;

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
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

public class CloudSelectPopupWindow extends PopupWindow {
	private CheckBox checkBox;
	private AuroraListView listView;
	private AuroraTextView titleView;
	private Button popCancel;
	private View mView;
	private List<CommonFileInfo> fileInfos;
	private SelectPopupAdapter adapter;
	private static final String TAG = "CloudSelectPopupWindow";
	private OperationUtil operationUtil;
	private Context context;

	public CloudSelectPopupWindow(int aniTabMenu, Context context) {
		super(context);
		this.context = context;
		mView = LayoutInflater.from(context).inflate(
				R.layout.aurora_cloud_popup, null);
		initView();
		defaultMenu(mView, aniTabMenu);

		if (context instanceof CloudActivity) {
			operationUtil = ((CloudActivity) context).getOperationUtil();
		}

	}

	public void showList(List<CommonFileInfo> fileInfos) {
		this.fileInfos = fileInfos;
		if (this.fileInfos == null) {
			this.fileInfos = new ArrayList<CommonFileInfo>();
		}
		LayoutParams layoutParams = listView.getLayoutParams();
		if(fileInfos.size()>3){
			layoutParams.height = (int)context.getResources().getDimension(R.dimen.aurora_album_listview_max_height);
			listView.setLayoutParams(layoutParams);
		}
		adapter = new SelectPopupAdapter(context, fileInfos);
		listView.setAdapter(adapter);
	}

	public void setTitleText(String title) {
		if (titleView != null) {
			titleView.setText(title);
		}
	}

	public void setOperationUtil(OperationUtil operationUtil) {
		this.operationUtil = operationUtil;
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
					dismiss();
				}
			});
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					LogUtil.d(TAG, "onItemClick::" + position);
					if (position == 0) {
						operationUtil.setMoveCreateAlbum(true);
						operationUtil.setMoveCheckBox(!checkBox.isChecked());
						operationUtil
								.createAlbum(R.string.aurora_move_new_album,(CloudActivity)context);
						return;
					}
					CommonFileInfo fileInfo = (CommonFileInfo) adapter
							.getItem(position);
					LogUtil.d(TAG, !checkBox.isChecked() + " fileInfo::"
							+ fileInfo.toString());
					operationUtil.setMoveOrCopyTarget(fileInfo);
					operationUtil.moveOrCopyPhoto(fileInfo,
							!checkBox.isChecked());
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

}
