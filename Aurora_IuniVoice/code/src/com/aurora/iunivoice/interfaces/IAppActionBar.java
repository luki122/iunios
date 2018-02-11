package com.aurora.iunivoice.interfaces;

import android.view.View;

public interface IAppActionBar {
	
	final int BACK_ITEM_ID = 0x1121;
	
	void enableBackItem(boolean isEnable);
	void setBackItemRes(int imageRes);
	void setTitleRes(int titleRes);
	void setTitleText(String titleText);
	void addActionBarItem(int imageRes,int itemId);
	void changeActionBarItemImageRes(int imageRes,int itemId);
	void addActionBarItem(String itemText,int Id);
	void removeActionBarItem(int Id);
	void setTitleSize(float size);
	void setTitleColor(int color);
	View getActionBarItem(int itemId);
	void setActionBarBg(int res);
}
