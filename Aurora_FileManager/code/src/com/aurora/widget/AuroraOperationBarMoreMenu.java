package com.aurora.widget;

import java.io.File;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;

import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.filemanager.FileApplication;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.R;
import com.aurora.filemanager.fragment.FileCategoryFragment;
import com.aurora.filemanager.fragment.PictureCategoryFragment;
import com.aurora.filemanager.fragment.PictureFragment;
import com.aurora.tools.LogUtil;
import com.aurora.tools.OperationAction;

public class AuroraOperationBarMoreMenu extends PopupWindow {
	public View layout;
	private TextView operationRenameTextView;
	private TextView operationInfoTextView;
	// add by Jxh 2014-9-10 添加隐私文件按钮 begin
	private LinearLayout aurora_add_in,button_operation_rename_down_linear,button_operation_info_down_linear;
	private TextView operationaAddTextView;
	// add by Jxh 2014-9-10 添加隐私文件按钮 end
	private OperationAction operationAction;
	private FileExplorerTabActivity activity;

	private void setupAuroraOperationPane() {
		setupClick(operationRenameTextView);
		setupClick(operationInfoTextView);
		setupClick(operationaAddTextView);
	}

	private void setupClick(View view) {
		if (view != null)
			view.setOnClickListener(buttonClick);
	}

	private static final String TAG = "AuroraOperationBarMoreMenu";
	private View.OnClickListener buttonClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button_operation_rename:
				// LogUtil.elog(TAG, "button_operation_rename");
				operationAction.renameOperation();
				break;
			case R.id.button_operation_info:
				// LogUtil.elog(TAG, "button_operation_info");
				operationAction.onOperationInfo();
				activity.beforeDisMissAuroraOperationBarMoreMenu();
				break;
			case R.id.button_operation_add:
				LogUtil.e(TAG, "button_operation_add");
				if ((activity.getCurrentFragment() instanceof PictureFragment)) {
					if (((PictureFragment) activity.getCurrentFragment())
							.isPrivacy()) {
						activity.setPrivacyBackToNormal(true);
						File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
						if(!file.exists()){
							file.mkdirs();
						}
						activity.beforeDisMissAuroraOperationBarMoreMenu();
						operationAction.moveFileOrDir(file.getPath());
					} else {
						
						operationAction.addPrivacyFiles(FileCategory.Picture);
					}
				} else if ((activity.getCurrentFragment() instanceof FileCategoryFragment)
						&& (aurora_add_in.getVisibility() == View.VISIBLE)) {
					if (((FileCategoryFragment) activity.getCurrentFragment())
							.isPrivacyView()) {
						activity.setPrivacyBackToNormal(true);
						File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
						if(!file.exists()){
							file.mkdirs();
						}
						activity.beforeDisMissAuroraOperationBarMoreMenu();
						operationAction.moveFileOrDir(file.getPath());
					} else {
						operationAction.addPrivacyFiles(FileCategory.Video);
					}
				}
				break;
			}

		}

	};

	public AuroraOperationBarMoreMenu(int aniTabMenu, int resId, Context context) {
		super(context);
		activity = (FileExplorerTabActivity) context;
		operationAction = activity.operationAction;
		layout = LayoutInflater.from(context).inflate(resId, null);
		operationRenameTextView = (TextView) layout
				.findViewById(R.id.button_operation_rename);
		operationInfoTextView = (TextView) layout
				.findViewById(R.id.button_operation_info);
		aurora_add_in = (LinearLayout) layout.findViewById(R.id.aurora_add_in);
		operationaAddTextView = (TextView) layout
				.findViewById(R.id.button_operation_add);
		button_operation_rename_down_linear = (LinearLayout)layout.findViewById(R.id.button_operation_rename_down_linear);
		button_operation_info_down_linear=(LinearLayout) layout.findViewById(R.id.button_operation_info_down_linear);
		setupAuroraOperationPane();
		defaultMenu(layout, aniTabMenu);
	}

	// add by Jxh 2014-9-10 begin
	public void showPrivacyView(boolean hide) {
		// if (hide) {
		// aurora_add_in.setVisibility(View.GONE);
		// operationInfoTextView.setVisibility(View.VISIBLE);
		// operationRenameTextView.setVisibility(View.VISIBLE);
		// return;
		// }
		if (activity.isPrivacy()
				&& ((activity.getCurrentFragment() instanceof PictureFragment) || (activity
						.getCurrentFragment() instanceof FileCategoryFragment))) {
			aurora_add_in.setVisibility(View.VISIBLE);
			String text = "";
			if (hide) {
				text = activity.getString(R.string.pri_back);
			} else {
				text = activity.getString(R.string.operation_add);
			}
			operationaAddTextView.setText(text);
			if (activity.getCurrentFragment() instanceof FileCategoryFragment) {
				if (activity.getNowfileCategory() != null
						&& activity.getNowfileCategory() != FileCategory.Video) {
					aurora_add_in.setVisibility(View.GONE);
				} else {
					operationaAddTextView.setText(text);
				}
			}

		} else {
			aurora_add_in.setVisibility(View.GONE);
		}
		if ((aurora_add_in.getVisibility() == View.VISIBLE)
				&& operationAction.getSelectFiles() != null
				&& operationAction.getSelectFiles().size() > 1) {
			operationInfoTextView.setVisibility(View.GONE);
			operationRenameTextView.setVisibility(View.GONE);
			button_operation_rename_down_linear.setVisibility(View.GONE);
			button_operation_info_down_linear.setVisibility(View.GONE);
		} else {
			operationInfoTextView.setVisibility(View.VISIBLE);
			operationRenameTextView.setVisibility(View.VISIBLE);
			button_operation_rename_down_linear.setVisibility(View.VISIBLE);
			button_operation_info_down_linear.setVisibility(View.VISIBLE);
		}
	}

	// add by Jxh 2014-9-10 end

	protected void defaultMenu(View view, int aniTabMenu) {
		// 设置默认项
		this.setContentView(view);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new BitmapDrawable());// 设置Menu菜单背景
		this.setAnimationStyle(aniTabMenu);
		this.setFocusable(true);// 设置点击外部menu不消失
		this.setOutsideTouchable(true);
	}
}
