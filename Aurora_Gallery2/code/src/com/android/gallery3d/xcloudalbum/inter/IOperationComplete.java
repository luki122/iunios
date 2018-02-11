package com.android.gallery3d.xcloudalbum.inter;

public interface IOperationComplete {
	void renameComplete(boolean success);

	void delComplete(boolean success);
	
	void moveOrCopyComplete(boolean success,boolean isMove,int errorCode);
	
	void createAlbumComplete(boolean success);
}
