package com.aurora.tools;

import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.filemanager.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;

public class FileIconHelper {

	private static final String TAG = "FileIconHelper";

	private final static HashMap<String, Integer> fileExtToIcons = new HashMap<String, Integer>();
	private FileIconLoader mIconLoader;

	private static FileIconHelper mFileIconHelper = null;
	private static Bitmap picCache;
	private static Bitmap apkCache;
	private static Bitmap videoCache;
	private static Bitmap folderCache;

	public static FileIconHelper getInstance(Context context) {
		context = context.getApplicationContext();// 不能修改 否则导致ICON 图 无法出现
		if (mFileIconHelper == null) {
			synchronized (context) {
				mFileIconHelper = new FileIconHelper(context);
			}
		}
		if (fileExtToIcons.size() == 0) {
			updateFileIcon();
		}
		return mFileIconHelper;
	}

	public FileIconHelper(Context context) {
		mIconLoader = new FileIconLoader(context);
		if (picCache == null) {
			picCache = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.file_icon_picture_light);
		}
		if (apkCache == null) {
			apkCache = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.gn_search_file_icon_apk_light);
		}
		if (videoCache == null) {
			videoCache = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.file_icon_video_light);
		}
		if (folderCache == null) {
			folderCache = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.folder_light);
		}
	}

	public void stopLoading() {
		if (mIconLoader != null) {
			mIconLoader.stop();
		}
	}

	public void clearLoading() {
		if (mIconLoader != null) {
			mIconLoader.pauseClear();
		}
	}

	private static void addItem(String exts, int resId) {
		if (exts != null) {
			fileExtToIcons.put(exts.toLowerCase(), resId);
		}
	}

	public static int getFileIcon(String ext) {
		Integer i = fileExtToIcons.get(ext.toLowerCase());
		if (i != null) {
			return i.intValue();
		} else {
			return 0;
		}

	}

	public void pause() {
		if (mIconLoader != null) {
			mIconLoader.pause();
		}
	}

	public boolean ismPaused() {
		if (mIconLoader != null) {
			return mIconLoader.ismPaused();
		}
		return false;
	}

	public void resume() {
		if (mIconLoader != null) {
			mIconLoader.resume();
		}
	}

	public void setIcon(FileInfo fileInfo, ImageView fileImage) {
		String filePath = fileInfo.filePath;
		long fileId = fileInfo.dbId;
		String extFromFilename = Util.getExtFromFilename(filePath);
		FileCategory fc = Util.getCategoryFromPath(filePath);
		boolean set = false;
		
		if (fileInfo.IsDir) {// 设置文件夹
			fileImage.setImageBitmap(folderCache);
			return;
		}
		int id = getFileIcon(extFromFilename);
//		LogUtil.log(TAG, "fileId==" + fileId);
		if (id != 0) {//
			fileImage.setImageResource(id);
			return;
		}
		mIconLoader.cancelRequest(fileImage);
		switch (fc) {
		case Apk:
			set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
//			LogUtil.log(TAG, "set==" + set);
			if (!set) {
				fileImage.setImageBitmap(apkCache);
				set = true;
			}

			break;
		case Picture:
			if (fileInfo.fileSize != 0) {
				set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
//				LogUtil.log(TAG, fileInfo.fileName + " set=" + set);
				if (!set) {
					fileImage.setImageBitmap(picCache);
					set = true;
				}
			} else {
				fileImage.setImageBitmap(picCache);
				set = true;
			}
			break;
		// case Video:
		//
		// set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
		// if (!set) {
		// fileImage.setImageBitmap(videoCache);
		// set = true;
		// }
		// break;
		// case Music:
		// fileImage.setImageResource(R.drawable.file_icon_music_light);
		// set = true;
		// break;
		default:
			break;
		}

		if (!set) {
			fileImage.setImageResource(R.drawable.file_icon_default_light);
		}
	}

	public static void updateFileIcon() {
		// addItem("apk", R.drawable.gn_search_file_icon_apk_light);
		addItem("flac", R.drawable.file_icon_music_light);
		addItem("aac", R.drawable.file_icon_music_light);
		addItem("mp3", R.drawable.file_icon_music_light);
		addItem("awb", R.drawable.file_icon_music_light);
		addItem("wma", R.drawable.file_icon_music_light);
		addItem("wav", R.drawable.file_icon_music_light);
		addItem("mid", R.drawable.file_icon_music_light);
		addItem("amr", R.drawable.file_icon_music_light);
		addItem("m4a", R.drawable.file_icon_music_light);
		addItem("ogg", R.drawable.file_icon_music_light);
		addItem("3gpp", R.drawable.file_icon_music_light);
		addItem("imy", R.drawable.file_icon_music_light);
		//add by JXH 2014-7-28 begin
		addItem("ape", R.drawable.file_icon_music_light);
		addItem("m4r", R.drawable.file_icon_music_light);
		//add by JXH 2014-7-28 end
		
		addItem("mpga", R.drawable.file_icon_music_light);
		addItem("mp4", R.drawable.file_icon_video_light);
		addItem("wmv", R.drawable.file_icon_video_light);
		addItem("mpeg", R.drawable.file_icon_video_light);
		addItem("mpg", R.drawable.file_icon_video_light);
		addItem("m4v", R.drawable.file_icon_video_light);
		addItem("3gp", R.drawable.file_icon_video_light);
		addItem("3g2", R.drawable.file_icon_video_light);
		addItem("3gpp2", R.drawable.file_icon_video_light);
		addItem("asf", R.drawable.file_icon_video_light);
		addItem("mov", R.drawable.file_icon_video_light);
		addItem("avi", R.drawable.file_icon_video_light);
		addItem("asx", R.drawable.file_icon_video_light);

		addItem("rmvb", R.drawable.file_icon_video_light);
		addItem("rm", R.drawable.file_icon_video_light);
		
		addItem("mkv", R.drawable.file_icon_video_light);
		addItem("flv", R.drawable.file_icon_video_light);
		addItem("tp", R.drawable.file_icon_video_light);
		addItem("vob", R.drawable.file_icon_video_light);

		// addItem("jpg", R.drawable.file_icon_picture_light);
		// addItem("jpeg", R.drawable.file_icon_picture_light);
		// addItem("gif", R.drawable.file_icon_picture_light);
		// addItem("png", R.drawable.file_icon_picture_light);
		// addItem("bmp", R.drawable.file_icon_picture_light);
		// addItem("wbmp", R.drawable.file_icon_picture_light);
		// addItem("webp", R.drawable.file_icon_picture_light);
		addItem("txt", R.drawable.file_icon_txt_light);
		addItem("log", R.drawable.file_icon_txt_light);
		addItem("xml", R.drawable.file_icon_txt_light);
		addItem("ini", R.drawable.file_icon_txt_light);
		addItem("lrc", R.drawable.file_icon_txt_light);
		addItem("doc", R.drawable.file_icon_doc_light);
		addItem("docx", R.drawable.file_icon_doc_light);
		addItem("ppt", R.drawable.file_icon_ppt_light);
		addItem("pptx", R.drawable.file_icon_ppt_light);
		addItem("xls", R.drawable.file_icon_xsl_light);
		addItem("xlsx", R.drawable.file_icon_xsl_light);
		addItem("pdf", R.drawable.file_icon_pdf_light);
		addItem("zip", R.drawable.file_icon_zip_light);
		addItem("gnz", R.drawable.file_icon_zip_light);
		addItem("rar", R.drawable.file_icon_zip_light);
		addItem("vcf", R.drawable.file_icon_vcf_light);
	}

}
