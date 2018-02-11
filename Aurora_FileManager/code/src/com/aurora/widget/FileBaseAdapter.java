package com.aurora.widget;

import java.util.ArrayList;
import java.util.List;

import com.aurora.filemanager.R;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;

import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.fragment.base.AuroraFragment;
import com.aurora.tools.FileIconHelper;
import com.aurora.tools.FileInfo;

public class FileBaseAdapter extends BaseAdapter {

	protected List<FileInfo> fileInfos = new ArrayList<FileInfo>();
	private LayoutInflater inflater;
	protected FileIconHelper fileIconHelper;
	private List<FileInfo> selectFiles = new ArrayList<FileInfo>();
	private FileExplorerTabActivity activity;
	private int maxLength, itemHeight;
	private AuroraFragment fragment;
	private boolean isNeedAnim;
	
	public FileBaseAdapter(List<FileInfo> fileInfos,
			FileExplorerTabActivity activity, AuroraFragment fragment) {
		super();
		this.fileInfos = fileInfos;
		this.activity = activity;
		this.fragment = fragment;
		itemHeight = (int)activity.getResources().getDimension(R.dimen.list_item_dp);
		maxLength = (int)activity.getResources().getDimension(R.dimen.list_title_length);
		inflater = LayoutInflater.from(activity);
		fileIconHelper = FileIconHelper.getInstance(activity);
	}

	public int getMaxLength() {
		return maxLength;
	}

	public FileIconHelper getFileIconHelper() {
		return fileIconHelper;
	}

	public List<FileInfo> getFileInfos() {
		return fileInfos;
	}

	public void setFileInfos(List<FileInfo> fileInfos) {
		this.fileInfos = fileInfos;
	}

	public List<FileInfo> getSelectFiles() {
		return selectFiles;
	}

	public void setSelectFiles(List<FileInfo> selectFiles) {
		this.selectFiles = selectFiles;
	}

	public boolean isNeedAnim() {
		return isNeedAnim;
	}

	public void setNeedAnim(boolean isNeedAnim) {
		this.isNeedAnim = isNeedAnim;
	}


	public FileExplorerTabActivity getActivity() {
		return activity;
	}

	@Override
	public int getCount() {
		return fileInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return fileInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	
	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			isNeedAnim = false;
		}
	};
	
	public void isShowAnim() {
		isNeedAnim = true;
		Message message = handler.obtainMessage();
		handler.sendMessage(message);
	}
	
	private boolean isLock;
	public void setLoadIcon(boolean isLock){
		this.isLock = isLock;
		if(isLock){
			fileIconHelper.pause();
		}else {
			fileIconHelper.resume();
		}
		
	}
	
	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}

	/**
	 * 清除选择项
	 */
	public void clearSelectFile() {
		if (selectFiles != null) {
			selectFiles.clear();
		}
	}
	
	
	public void setItemViewData(AuroraFilesItemView itemView,final FileInfo fileInfo){
		
	}
	
	public void setItemTextViewStyle(AuroraFilesItemView itemView){};


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AuroraFilesItemView auroraFilesItemView;
		if (convertView == null) {
			convertView = (View) inflater.inflate(
					com.aurora.R.layout.aurora_slid_listview, null);
			RelativeLayout main = (RelativeLayout) convertView
					.findViewById(com.aurora.R.id.aurora_listview_front);
			inflater.inflate(R.layout.file_browser_item, main);
			auroraFilesItemView = new AuroraFilesItemView(convertView);
			convertView.setTag(auroraFilesItemView);
		} else {
			auroraFilesItemView = (AuroraFilesItemView) convertView.getTag();
		}
		if (convertView.getHeight() >= itemHeight) {
			itemHeight = convertView.getHeight();
		}
		AuroraCheckBox checkbox = auroraFilesItemView.getCheckBox();
		RelativeLayout mainUi = auroraFilesItemView.getLayoutMainUi();
		setItemViewData(auroraFilesItemView, (FileInfo)getItem(position));
		
		ViewGroup.LayoutParams vl = convertView.getLayoutParams();
		if (null != vl) {
			vl.height = itemHeight;
			convertView.setLayoutParams(vl);
			convertView.findViewById(com.aurora.R.id.content).setAlpha(255);
		}

		if (fragment.isOperationFile()) {
			if (isNeedAnim) {
				aurora.widget.AuroraListView.auroraStartCheckBoxAppearingAnim(
						mainUi, checkbox);
			} else {
				aurora.widget.AuroraListView.auroraSetCheckBoxVisible(mainUi,
						checkbox, true);
			}
		} else {
			if (isNeedAnim) {
				aurora.widget.AuroraListView
						.auroraStartCheckBoxDisappearingAnim(mainUi, checkbox);
			} else {
				aurora.widget.AuroraListView.auroraSetCheckBoxVisible(mainUi,
						checkbox, false);
			}
		}
		setItemTextViewStyle(auroraFilesItemView);
		
		return convertView;
	}
	


}
