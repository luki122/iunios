package com.aurora.filemanager.inter;

import java.util.List;

import com.aurora.tools.FileInfo;

public interface OperationInterfaceLisenter {
	void deleteComplete(List<FileInfo> ids);

	void folderDelet(FileInfo fileInfo);

	void renameComplete(FileInfo old, FileInfo newFileInfo);

	void completeRefresh(List<FileInfo> addFileInfos,
			List<FileInfo> removeInfos);
}
