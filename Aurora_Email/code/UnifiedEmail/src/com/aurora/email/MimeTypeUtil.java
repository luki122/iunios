package com.aurora.email;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.text.TextUtils;
import android.widget.ImageView;

import com.android.mail.R;
import com.android.mail.utils.MyLog;

public class MimeTypeUtil {

	private static final Map<String, Integer> EXTENSIONTOIMGIDMAP_MAP = new HashMap<String, Integer>();

	static {
		addItem("flac", R.drawable.aurora_file_icon_music_light);
		addItem("aac", R.drawable.aurora_file_icon_music_light);
		addItem("mp3", R.drawable.aurora_file_icon_music_light);
		addItem("awb", R.drawable.aurora_file_icon_music_light);
		addItem("wma", R.drawable.aurora_file_icon_music_light);
		addItem("wav", R.drawable.aurora_file_icon_music_light);
		addItem("mid", R.drawable.aurora_file_icon_music_light);
		addItem("amr", R.drawable.aurora_file_icon_music_light);
		addItem("m4a", R.drawable.aurora_file_icon_music_light);
		addItem("ogg", R.drawable.aurora_file_icon_music_light);
		addItem("3gpp", R.drawable.aurora_file_icon_music_light);
		addItem("imy", R.drawable.aurora_file_icon_music_light);
		// add by JXH 2014-7-28 begin
		addItem("ape", R.drawable.aurora_file_icon_music_light);
		addItem("m4r", R.drawable.aurora_file_icon_music_light);
		// add by JXH 2014-7-28 end

		addItem("mpga", R.drawable.aurora_file_icon_music_light);
		addItem("mp4", R.drawable.aurora_file_icon_video_light);
		addItem("wmv", R.drawable.aurora_file_icon_video_light);
		addItem("mpeg", R.drawable.aurora_file_icon_video_light);
		addItem("mpg", R.drawable.aurora_file_icon_video_light);
		addItem("m4v", R.drawable.aurora_file_icon_video_light);
		addItem("3gp", R.drawable.aurora_file_icon_video_light);
		addItem("3g2", R.drawable.aurora_file_icon_video_light);
		addItem("3gpp2", R.drawable.aurora_file_icon_video_light);
		addItem("asf", R.drawable.aurora_file_icon_video_light);
		addItem("mov", R.drawable.aurora_file_icon_video_light);
		addItem("avi", R.drawable.aurora_file_icon_video_light);
		addItem("asx", R.drawable.aurora_file_icon_video_light);

		addItem("rmvb", R.drawable.aurora_file_icon_video_light);
		addItem("rm", R.drawable.aurora_file_icon_video_light);

		addItem("mkv", R.drawable.aurora_file_icon_video_light);
		addItem("flv", R.drawable.aurora_file_icon_video_light);
		addItem("tp", R.drawable.aurora_file_icon_video_light);
		addItem("vob", R.drawable.aurora_file_icon_video_light);

		addItem("txt", R.drawable.aurora_file_icon_txt_light);
		addItem("log", R.drawable.aurora_file_icon_txt_light);
		addItem("xml", R.drawable.aurora_file_icon_txt_light);
		addItem("ini", R.drawable.aurora_file_icon_txt_light);
		addItem("lrc", R.drawable.aurora_file_icon_txt_light);
		addItem("doc", R.drawable.aurora_file_icon_doc_light);
		addItem("docx", R.drawable.aurora_file_icon_doc_light);
		addItem("ppt", R.drawable.aurora_file_icon_ppt_light);
		addItem("pptx", R.drawable.aurora_file_icon_ppt_light);
		addItem("xls", R.drawable.aurora_file_icon_xsl_light);
		addItem("xlsx", R.drawable.aurora_file_icon_xsl_light);
		addItem("pdf", R.drawable.aurora_file_icon_pdf_light);
		addItem("zip", R.drawable.aurora_file_icon_zip_light);
		addItem("gnz", R.drawable.aurora_file_icon_zip_light);
		addItem("rar", R.drawable.aurora_file_icon_zip_light);
		addItem("vcf", R.drawable.aurora_file_icon_vcf_light);
	}

	public static void addItem(String extension, int res) {
		if (!EXTENSIONTOIMGIDMAP_MAP.containsKey(extension)) {
			EXTENSIONTOIMGIDMAP_MAP.put(extension, res);
		}
	}

	public static int getImgId(String extension) {
		if (extension == null || extension.isEmpty()) {
			return -1;
		}
		
		Integer id=EXTENSIONTOIMGIDMAP_MAP.get(extension);
		
		return id==null?-1:id.intValue();
	}

	private static String getFilenameExtension(String fileName) {
		String extension = null;
		if (!TextUtils.isEmpty(fileName)) {
			int lastDot = fileName.lastIndexOf('.');
			if ((lastDot > 0) && (lastDot < fileName.length() - 1)) {
				extension = fileName.substring(lastDot + 1).toLowerCase();
			}
		}
		return extension;
	}

	public static void showDefualtbyExtension(String fileName, ImageView view) {
		final String extension = getFilenameExtension(fileName);
		if (extension == null) {
			return;
		}
		int id = MimeTypeUtil.getImgId(extension);
		if (id != -1) {
			view.setImageResource(id);
		}
	}

	public static boolean isApkMimeType(String mimeType) {

		return mimeType != null
				&& mimeType.equals("application/vnd.android.package-archive");
	}
}
