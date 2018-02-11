package com.aurora.iunivoice.utils;

import java.util.ArrayList;
import java.util.List;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.bean.EmotionInfo;

public class EmotionUtil {
	
	private static EmotionUtil emotionUtil;
	private List<EmotionInfo> emotionList = new ArrayList<EmotionInfo>();
	
	private EmotionUtil() {
		EmotionInfo info;
		for (int i = 0; i < EmotionUtil.faceResId.length; i++) {
			info = new EmotionInfo();
			info.setCode(EmotionUtil.faceCode[i]);
			info.setResId(EmotionUtil.faceResId[i]);
			emotionList.add(info);
		}
	}
	
	public static EmotionUtil getInstence() {
		if (emotionUtil == null) {
			emotionUtil = new EmotionUtil();
		}
		return emotionUtil;
	}
	
	public List<EmotionInfo> getEmotionList() {
		return emotionList;
	}

	public static int[] faceResId = new int[] {
//			R.drawable.face_biggrin,
			R.drawable.face_smile,
			R.drawable.face_cry,
			R.drawable.face_curse,
			R.drawable.face_dizzy,
			R.drawable.face_funk,
//			R.drawable.face_handshake,
			R.drawable.face_huffy,
//			R.drawable.face_hug,
			R.drawable.face_lol,
			R.drawable.face_loveliness,
			R.drawable.face_mad,
			R.drawable.face_sad,
			R.drawable.face_shocked,
			R.drawable.face_shutup,
			R.drawable.face_shy,
			R.drawable.face_sleepy,
			R.drawable.face_sweat,
//			R.drawable.face_time,
			R.drawable.face_titter,
			R.drawable.face_tongue,
			R.drawable.face_kiss,
			R.drawable.face_call,
			R.drawable.face_victory
	};
	
	public static String[] faceCode = new String[] {
//		":D",
		":)",
		":'(",
		":curse:",
		":dizzy:",
		":funk:",
//		":handshake",
		":@",
//		":hug:",
		":lol",
		":loveliness:",
		":Q",
		":(",
		":o",
		":shutup:",
		":$",
		":sleepy:",
		":L",
//		":time:",
		";P",
		":P",
		":kiss:",
		":call:",
		":victory:"
	};

}
