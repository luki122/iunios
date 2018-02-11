package com.android.gallery3d.xcloudalbum.inter;

import android.view.View;
import android.widget.AdapterView;

public interface IActionOnClickListener {
	public void onLeftClick();

	public void onRightClick();

	public void auroraDeleteItemView(View view, int position);
	
	public void auroraOnItemClick(AdapterView<?> parent, View view,
			int position, long id);
	
	public boolean auroraOnItemLongClick(AdapterView<?> parent,
			View view, int position, long id);
	
	public boolean onFragmentBack();
}
