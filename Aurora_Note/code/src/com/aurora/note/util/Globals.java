/*
 * @author 张伟
 */
package com.aurora.note.util;

import android.os.Environment;
import java.io.File;

public class Globals {

	public static final String APP_ID = "wx3401c983a4ff9351";

	public static final String QQ_APP_ID = "1103884260";
	
	//图片类型
	public static final String IMAGE_TYPE = "jpg,gif,jpeg,bmp,png";

	public static final String NEW_LINE = "\n"; // 换行符
	public static final char CHAR_NEW_LINE = '\n';
	public static final String FILE_PROTOCOL = "file://";
	public static final String DRAWABLE_PROTOCOL = "drawable://";

	public static final String NEW_LINE_PREFIX = "        "; // 8个空格
	public static final String NEW_LINE_WITH_PREFIX = "\n        ";

	/**
	 * 预置图片
	 */
	public static final int ATTACHMENT_TYPE_PRESET_IMAGE = 6;
	/**
     * 项目符号
     */
    public static final int ATTACHMENT_TYPE_SIGN = 4;
    /**
     * 录音
     */
    public static final int ATTACHMENT_TYPE_RECORD = 3;
    /**
     * 视频
     */
    public static final int ATTACHMENT_TYPE_VIDEO = 2;
    /**
     * 图片
     */
    public static final int ATTACHMENT_TYPE_IMAGE = 1;
    /**
     * 图片组
     */
    public static final int ATTACHMENT_TYPE_IMAGE_GROUP = 5;
    /**
     * 一个分组允许的最多图片数
     */
    public static final int ATTACHMENT_IMAGE_GROUP_MAX_IMAGES = 4;
    /**
     * 图片组里的图片路径的分割符
     */
    public static final String ATTACHMENT_IMAGE_GROUP_PATH_SEP = ";";

	// [image::::1表示要处理的是图片 2：表示要处理的是视频 3：表示要处理的是音频
    public static final String ATTACHMENT_START = "[image::::";
    public static final String ATTACHMENT_END = "::::]";
    public static final int ATTACHMENT_START_LENGTH = ATTACHMENT_START.length();
    public static final int ATTACHMENT_END_LENGTH = ATTACHMENT_END.length();

    public static final String ATTACHMENT_ALL_PATTERN = "\\" + ATTACHMENT_START + "(.*?)::::\\]";
//    public static final String ATTACHMENT_IMAGE_PATTERN = "\\" + ATTACHMENT_START + ATTACHMENT_TYPE_IMAGE + "(.*?)::::\\]";
    public static final String ATTACHMENT_VIDEO_PATTERN = "\\" + ATTACHMENT_START + ATTACHMENT_TYPE_VIDEO + "(.*?)::::\\]";
    public static final String ATTACHMENT_SOUND_PATTERN = "\\" + ATTACHMENT_START + ATTACHMENT_TYPE_RECORD + "(.*?)::::\\]";
//    public static final String ATTACHMENT_IMAGE_GROUP_PATTERN = "\\" + ATTACHMENT_START + ATTACHMENT_TYPE_IMAGE_GROUP + "(.*?)::::\\]";
    public static final String ATTACHMENT_IMAGE_PATTERN = "\\" + ATTACHMENT_START + "[" + ATTACHMENT_TYPE_IMAGE +
            ATTACHMENT_TYPE_IMAGE_GROUP + ATTACHMENT_TYPE_PRESET_IMAGE + "]" + "(.*?)::::\\]";
    public static final String ATTACHMENT_IMAGE_EXCEPT_PRESET_PATTERN = "\\" + ATTACHMENT_START + "[" + ATTACHMENT_TYPE_IMAGE +
            ATTACHMENT_TYPE_IMAGE_GROUP + "]" + "(.*?)::::\\]";

	public final static int REQUEST_CODE_BASE = 1;
	public final static int REQUEST_CODE_GETIMAGE_BYCAMERA = REQUEST_CODE_BASE + 1;
	public final static int REQUEST_CODE_GETVIDEO_BYCAMERA = REQUEST_CODE_BASE + 2;
	public final static int REQUEST_CODE_ALBUM = REQUEST_CODE_BASE + 3;
	public final static int REQUEST_CODE_VIDEO = REQUEST_CODE_BASE + 4;

	public static final String SIGN_NOINDENT_ID = "1";
	public static final String SIGN_INDENT_ID = "2";
	public static final String SIGN_UNCHECKED_ID = "3";
	public static final String SIGN_CHECKED_ID = "4";

	public static final String PRESET_IMAGE_CHUNJIE = DRAWABLE_PROTOCOL + "chunjie";
	public static final String PRESET_IMAGE_QINGRENJIE = DRAWABLE_PROTOCOL + "qingrenjie";
	public static final String PRESET_IMAGE_CHUNJIE_TEXT = ATTACHMENT_START + ATTACHMENT_TYPE_PRESET_IMAGE +
			PRESET_IMAGE_CHUNJIE + ATTACHMENT_END;
	public static final String PRESET_IMAGE_QINGRENJIE_TEXT = ATTACHMENT_START + ATTACHMENT_TYPE_PRESET_IMAGE +
			PRESET_IMAGE_QINGRENJIE + ATTACHMENT_END;

	public static final String NOTE_PAPER_1 = DRAWABLE_PROTOCOL + "note_paper_01";
	public static final String NOTE_PAPER_2 = DRAWABLE_PROTOCOL + "note_paper_02";
	public static final String NOTE_PAPER_3 = DRAWABLE_PROTOCOL + "note_paper_03";
	public static final String NOTE_PAPER_4 = DRAWABLE_PROTOCOL + "note_paper_04";
	public static final String NOTE_PAPER_5 = DRAWABLE_PROTOCOL + "note_paper_05";
	public static final String NOTE_PAPER_6 = DRAWABLE_PROTOCOL + "note_paper_06";

	public static final String[] NOTE_PAPERS = new String[] {
		NOTE_PAPER_1,
		NOTE_PAPER_2,
		NOTE_PAPER_3,
		NOTE_PAPER_4,
		NOTE_PAPER_5,
		NOTE_PAPER_6
	};

	//wifi网络类型标志
	public static final int NETWORK_WIFI = 0;
	//2G网络类型标志
	public static final int NETWORK_2G = 1;
	//3G网络类型标志
	public static final int NETWORK_3G = 2;

	// 相机拍照照片路径
	public static final File PHOTO_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/photo");
	
	// 相机摄像照片路径
	public static final File VIDEO_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/video");
	
	// 生成图片路径
	public static final File PIC_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/picture");
	// 生成录音路径
	public static final File SOUND_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/sound");
	// 备份路径
	public static final File BACKUP_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/backup");
	// 备份路径
	public static final File CROP_DIR = new File(
			Environment.getExternalStorageDirectory() + "/Note/crop");

	public static final String CROP_DIR_NAME = "/Note/crop";
	public static final String CACHE_DIR_NAME = "/Note/cache";
	public static final String LOG_DIR_NAME = "/Note/log";

	// activity result 100
	public static final int REQUEST_CODE_BASE_USER = 100;
	public static final int REQUEST_CODE_ADD_RECORD =  REQUEST_CODE_BASE_USER + 1;
	public static final int REQUEST_CODE_OPEN_NEWNOTE =  REQUEST_CODE_BASE_USER + 2;
	public static final int REQUEST_CODE_MODIFY_LABEL = REQUEST_CODE_BASE_USER + 3;
	public static final int REQUEST_CODE_PLAY_RECORD = REQUEST_CODE_BASE_USER + 4;
	public static final int REQUEST_CODE_ADD_REMINDER = REQUEST_CODE_BASE_USER + 5;
	public static final int REQUEST_CODE_SELECT_PAPER = REQUEST_CODE_BASE_USER + 6;

	public static final String SHARE_PREF_NAME = "note_share_pref";
	public static final String PREF_HAS_INITED = "has_inited";
	public static final String PREF_LABEL_INITED = "label_inited";
	public static final String PREF_FESTIVAL_INITED = "festival_inited";

}
