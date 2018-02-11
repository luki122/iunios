
package com.aurora.note.ui;

/*
 * @author  zw
 */
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.provider.MediaStore.Images;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.aurora.note.R;
import com.aurora.note.bean.Str2Emo;
import com.aurora.note.util.BitmapUtil;
import com.aurora.note.util.FileLog;
import com.aurora.note.util.Globals;
import com.aurora.note.widget.NoteImageGroupSpan;
import com.aurora.note.widget.NoteImageSpan;
import com.aurora.note.widget.NoteImageSpanBitmapCache;
import com.aurora.note.widget.NoteSoundSpan;
import com.aurora.note.widget.NoteVideoSpan;

import java.util.ArrayList;

public class ChatEmotion {

    private static final String TAG = "ChatEmotion";

    private static int IMAGE_HEIGHT = -1; // 一张图片的高度

    private static final String MARK_CHECKED_TEXT = Globals.ATTACHMENT_START + Globals.ATTACHMENT_TYPE_SIGN
            + Globals.SIGN_CHECKED_ID + Globals.ATTACHMENT_END;

    public static ArrayList<Str2Emo> string2SymbolList(Context context, String str) {
        ArrayList<Str2Emo> s2eLists = new ArrayList<Str2Emo>();
        int pos = 0;
        int start = 0;
        int end = 0;
        int index = -1;
        String tmpstr;
        do {
            index = str.indexOf(Globals.ATTACHMENT_START, pos);
            if (index == -1)
                break;
            start = index;
            index = str.indexOf(Globals.ATTACHMENT_END, start + Globals.ATTACHMENT_START_LENGTH);
            end = index;
            tmpstr = str.substring(start + 10, end);

            pos = end;
            Str2Emo se = new Str2Emo();
            se.setStart(start);
            se.setEnd(end);

            se.setImg_src(tmpstr);
            s2eLists.add(se);
        } while (index != -1);

        return s2eLists;
    }

    // public static int getRowStartIndex2(String text, int selStart) {
    // int rowStartIndex = 0;
    // if (!TextUtils.isEmpty(text)) {
    // int index = text.indexOf(Globals.LINE);
    // if (index == -1 || selStart <= index) {
    // // 只有一行或者光标在第一行
    // rowStartIndex = 0;
    // } else {
    // int lastNewLineIndex = index;
    // index = text.indexOf(Globals.LINE, index + 1);
    // while (index != -1 && index < selStart) {
    // lastNewLineIndex = index;
    // index = text.indexOf(Globals.LINE, index + 1);
    // }
    // rowStartIndex = lastNewLineIndex + 1;
    // }
    // }
    //
    // return rowStartIndex;
    // }

    public static int getRowStartIndex(String text, int selStart) {
        int rowStartIndex = 0;

        if (!TextUtils.isEmpty(text)) {
            rowStartIndex = text.lastIndexOf(Globals.NEW_LINE, selStart);
            if (rowStartIndex < 0) {
                rowStartIndex = 0;
            } else {
                if (rowStartIndex == selStart) {
                    rowStartIndex = text.lastIndexOf(Globals.NEW_LINE, selStart - 1);
                    if (rowStartIndex < 0) {
                        rowStartIndex = 0;
                    } else {
                        rowStartIndex += 1;
                    }
                } else {
                    rowStartIndex += 1;
                }
            }
        }

        return rowStartIndex;
    }

    public static int getRowEndIndex(String text, int selStart) {
        int rowEndIndex = 0;

        if (!TextUtils.isEmpty(text)) {
            rowEndIndex = text.indexOf(Globals.NEW_LINE, selStart);
            if (rowEndIndex < 0) {
                rowEndIndex = text.length();
            }
        }

        return rowEndIndex;
    }

    public static SpannableString string2Symbol(Context context, String str, int width,
            int rightExtraSpace, int leftPadding, int topPadding) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        int pos = 0;
        int start = 0;
        int end = 0;
        int index = -1;
        // 原始字符串
        String tmpstr;
        // 截取后的字符串
        String tmpdes;

        int tmptype;
        do {
            index = str.indexOf(Globals.ATTACHMENT_START, pos);

            if (index == -1) {
                ssb.append(str, pos, str.length());
                break;
            } else {
                ssb.append(str, pos, index);
            }
            start = index;
            index = str.indexOf(Globals.ATTACHMENT_END, start + Globals.ATTACHMENT_START_LENGTH);
            end = index;
            if (end != -1) {
                tmptype = Integer.parseInt(str.substring(start + 10, start + 11));
                tmpdes = str.substring(start + 11, end);
                tmpstr = str.substring(start, end + 5);
                try {
                    if (tmptype == Globals.ATTACHMENT_TYPE_IMAGE || tmptype == Globals.ATTACHMENT_TYPE_IMAGE_GROUP)
                        ssb.append(strpic(context, tmpdes, tmpstr, width, rightExtraSpace, leftPadding, topPadding));
                    else if (tmptype == Globals.ATTACHMENT_TYPE_VIDEO)
                        ssb.append(strvideo(context, tmpdes, tmpstr, width, rightExtraSpace, leftPadding, topPadding));
                    else if (tmptype == Globals.ATTACHMENT_TYPE_RECORD)
                        ssb.append(strsound(context, tmpdes, tmpstr, width, rightExtraSpace, leftPadding, topPadding));
                    else if (tmptype == Globals.ATTACHMENT_TYPE_SIGN)
                        ssb.append(strsign(context, tmpdes, tmpstr, leftPadding, topPadding));
                    else if (tmptype == Globals.ATTACHMENT_TYPE_PRESET_IMAGE)
                    	ssb.append(strPresetImage(context, tmpdes, tmpstr, width, rightExtraSpace, leftPadding, topPadding));
                } catch (Exception e) {
                    FileLog.i(TAG, e.toString());
                }
                pos = end + 5;
                if (MARK_CHECKED_TEXT.equals(tmpstr)) {
                   int lineEndIndex = str.indexOf(Globals.NEW_LINE, pos);
                   if (lineEndIndex == -1) lineEndIndex = str.length();
                   if (lineEndIndex > pos) {
                       ssb.append(strStrikeThrough(context, str.substring(pos, lineEndIndex)));
                       pos = lineEndIndex;
                   }
                }
            } else {
                break;
            }

        } while (index != -1);

        return SpannableString.valueOf(ssb);
    }

    public static SpannableString strpic(Context context, String des,
            String src, int width, int rightExtraSpace, int leftPadding, int topPadding) {
        if (IMAGE_HEIGHT == -1) {
            IMAGE_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.new_note_image_height);
        }
        NoteImageGroupSpan dstSpan = new NoteImageGroupSpan(null, src, NoteImageSpan.Type.Type_Picture,
                width, IMAGE_HEIGHT, rightExtraSpace, leftPadding, topPadding);
        String[] filePaths = des.split(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
        for (String path: filePaths) {
            Bitmap bm = NoteImageSpanBitmapCache.getInstance().getBitmap(path);
            boolean imageNotFound = false;
            if (bm == null) {
                String des_tmp = path.startsWith(Globals.FILE_PROTOCOL) ? path.substring(7) : path;
                boolean doCrop = !des_tmp.substring(0, des_tmp.lastIndexOf("/")).endsWith(Globals.CROP_DIR_NAME);
                try {
                    if (!doCrop) {
                        bm = BitmapUtil.compressImageFromFile(des_tmp, width);
                    } else {
                        bm = BitmapUtil.compressImageFromFile(des_tmp, width, IMAGE_HEIGHT);
                        if (bm != null && (bm.getWidth() > width || bm.getHeight() > IMAGE_HEIGHT)) {
                            bm = BitmapUtil.cropBitmap(bm, width, IMAGE_HEIGHT, false);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception", e);
                    bm = null;
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "OutOfMemoryError", e);
                    bm = null;
                }

                if (null == bm) {
                    imageNotFound = true;
                    bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.forum_loading_default);
                } else {
                    NoteImageSpanBitmapCache.getInstance().putBitmap(path, bm); // 缓存加载过的图片，以便图片合并的时候更快
                }
            }
            Drawable drawable = new BitmapDrawable(context.getResources(), bm);

            Log.d(TAG, "Jim, intrinsic width: " + drawable.getIntrinsicWidth() + ", intrinsic height: " + 
                    drawable.getIntrinsicHeight());

            drawable.setBounds(0, 0, imageNotFound ? width : drawable.getIntrinsicWidth(),
                    imageNotFound ? width * 122 / 155 : drawable.getIntrinsicHeight());
            NoteImageSpan span = new NoteImageSpan(drawable, R.drawable.new_note_image_selected,
                    path, NoteImageSpan.Type.Type_Picture, width, -1, rightExtraSpace,
                    leftPadding, topPadding);

            Log.d(TAG, "Jim, pic width: " + drawable.getBounds().width() + ", height: " + 
                    drawable.getBounds().height());

            dstSpan.addSubSpan(span);
        }

        SpannableString ss = new SpannableString(src);

        ss.setSpan(dstSpan, 0, src.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    public static SpannableString strsound(Context context, String des1,
            String src1, int width, int rightExtraSpace, int leftPadding, int topPadding) {

        String[] tme = des1.split("&");

        String recordTime = tme[1];
        String recordName = tme[2];
        
        final Resources res = context.getResources();
        NinePatchDrawable dra = (NinePatchDrawable) res.getDrawable(R.drawable.sound_bg);
        dra.setBounds(0, 0, width, dra.getIntrinsicHeight());
        NinePatchDrawable playingDra = (NinePatchDrawable) res.getDrawable(R.drawable.sound_playing_bg);
        playingDra.setBounds(0, 0, width, playingDra.getIntrinsicHeight());

        NoteImageSpan span = new NoteSoundSpan(dra, playingDra, des1, NoteImageSpan.Type.Type_Sound,
                rightExtraSpace, leftPadding, topPadding, recordName, recordTime);
        SpannableString ss = new SpannableString(src1);
        ss.setSpan(span, 0, src1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    private static int getSignDrawableResId(String signId) {
        if (Globals.SIGN_INDENT_ID.equals(signId)) {
            return R.drawable.sign_indent;
        }

        if (Globals.SIGN_UNCHECKED_ID.equals(signId)) {
            return R.drawable.sign_mark;
        }

        if (Globals.SIGN_CHECKED_ID.equals(signId)) {
            return R.drawable.sign_mark_expired;
        }

        return R.drawable.sign_noindent;
    }

    public static SpannableString strsign(Context context, String des, String src, int leftPadding, int topPadding) {

        Drawable drawable = context.getResources().getDrawable(getSignDrawableResId(des));
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Log.d(TAG, "Jim, sign width: " + drawable.getBounds().width() + ", height: " +
                drawable.getBounds().height());
        NoteImageSpan span = new NoteImageSpan(drawable, des, NoteImageSpan.Type.Type_Sign, -1, leftPadding, topPadding);
        SpannableString ss = new SpannableString(src);
        ss.setSpan(span, 0, src.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static SpannableString strStrikeThrough(Context context, String src) {
        SpannableString ss = new SpannableString(src);
        ss.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.mark_expired_text_color)),
                0, src.length(), 0);
        // ss.setSpan(new StrikethroughSpan(), 0, src.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static SpannableString strvideo(Context context, String des,
            String src, int width, int rightExtraSpace, int leftPadding, int topPadding) {

        Drawable drawable = null;

        // Bitmap bm = ImageLoader.getInstance().loadImageSync(des);
        String tmp_str = des.substring(7);

        if (IMAGE_HEIGHT == -1) {
            IMAGE_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.new_note_image_height);
        }

        Bitmap bm = BitmapUtil.getVideoThumbnail(tmp_str, width, IMAGE_HEIGHT,
                Images.Thumbnails.MINI_KIND);
        final boolean isVideoImageValid;
        if (null == bm) {
            drawable = context.getResources().getDrawable(R.drawable.forum_loading_failed);
            isVideoImageValid = false;
        } else {
            drawable = new BitmapDrawable(context.getResources(), bm);
            isVideoImageValid = true;
        }

        // drawable.setBounds(0, 0, width, drawable.getIntrinsicHeight());
        drawable.setBounds(0, 0,
                isVideoImageValid ? drawable.getIntrinsicWidth() : width,
                isVideoImageValid ? drawable.getIntrinsicHeight() : IMAGE_HEIGHT);
        NoteImageSpan span = new NoteVideoSpan(drawable, R.drawable.video_card_play,
                R.drawable.video_card_play_pressed, des, NoteImageSpan.Type.Type_Video,
                rightExtraSpace, leftPadding, topPadding, isVideoImageValid);
        Log.d(TAG, "Jim, video width: " + drawable.getBounds().width() + ", height: " +
                drawable.getBounds().height());

        SpannableString ss = new SpannableString(src);

        ss.setSpan(span, 0, src.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    public static SpannableString strPresetImage(Context context, String des, String src,
            int width, int rightExtraSpace, int leftPadding, int topPadding) {

        if (IMAGE_HEIGHT == -1) {
            IMAGE_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.new_note_image_height);
        }

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), getFestivalDrawableResId(des));
        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        drawable.setBounds(0, 0, width, width * 5 / 8);

        NoteImageSpan span = new NoteImageSpan(drawable, R.drawable.new_note_image_selected, des, NoteImageSpan.Type.Type_Preset_Image,
        		width, IMAGE_HEIGHT, rightExtraSpace, leftPadding, topPadding);

        SpannableString ss = new SpannableString(src);
        ss.setSpan(span, 0, src.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    private static int getFestivalDrawableResId(String festival) {
        if (Globals.PRESET_IMAGE_CHUNJIE.equals(festival)) {
            return R.drawable.image_chunjie;
        }

        return R.drawable.image_qingrenjie;
    }

}
