package com.aurora.filemanager.inter;

import java.util.List;

import com.aurora.tools.FileInfo;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

public interface IActionOnClickListener {
	public void onLeftClick();

	public void onRightClick();

	public void auroraDelOnClick(int position);

	public void auroraDeleteItemView(View view, int position);
	
	public void auroraOnItemClick(AdapterView<?> parent, View view,
			int position, long id);
	
	public boolean auroraOnItemLongClick(AdapterView<?> parent,
			View view, int position, long id);
	
	public void auroraOnScrollStateChanged(AbsListView view, int scrollState);
	
	public void onCompleteRefresh(List<FileInfo> addFileInfos,
			List<FileInfo> removeInfos);
	
	public boolean onFragmentBack();
}
