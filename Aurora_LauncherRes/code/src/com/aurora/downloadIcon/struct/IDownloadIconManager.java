package com.aurora.downloadIcon.struct;

import java.util.ArrayList;
import java.util.List;

import android.app.DownloadManager.Request;
import android.content.Context;
import android.graphics.Bitmap;

import com.aurora.downloadIcon.bean.IconResponseObject;
import com.aurora.downloadIcon.bean.IconResponseProp;

public interface IDownloadIconManager {
	public void getIconResponseProp(final DataResponse<IconResponseObject> response,final Context context);
	public void getIcon(final DataResponse<IconResponseObject> response,final IconResponseObject iconResponseObject ,final IconResponseObject tempIconResponseObject,final Context context);
	public void postActivity();
}
