package com.aurora.tools;

import android.os.AsyncTask;
import android.widget.TextView;


public class FileInfoTask extends AsyncTask<FileInfo, Void, Integer> {
	private TextView textView;
	private LruMemoryCacheByInteger cacheByInteger;
	private static final String TAG="FileInfoTask";
	
	public FileInfoTask(TextView textView) {
		super();
		this.textView = textView;
		cacheByInteger = LruMemoryCacheByInteger.getInstance();
	}

	@Override
	protected Integer doInBackground(FileInfo... params) {
		if(isCancelled()){
			return 0;
		}
		FileInfo fileInfo = params[0];
		fileInfo = Util.getAllFileInfo(fileInfo, false);
		cacheByInteger.addBitmapToMemoryCache(fileInfo.filePath+fileInfo.ModifiedDate, fileInfo.Count);
		LogUtil.d(TAG, " fileInfo:"+fileInfo.Count+" fileInfo:"+fileInfo.fileName);
		return fileInfo.Count;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		textView.setText("(" + result + ")");
	}

}
