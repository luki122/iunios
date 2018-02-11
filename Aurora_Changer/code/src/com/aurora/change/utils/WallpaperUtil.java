package com.aurora.change.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.aurora.change.R;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.receiver.ChangeReceiver;

public class WallpaperUtil {

    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");

    private static String[] sTimeArea = {"6:00-8:00", "8:00-10:00", "10:00-12:00", "12:00-14:00",
            "14:00-16:00", "16:00-18:00", "18:00-20:00", "20:00-22:00", "22:00-24:00", "0:00-2:00",
            "2:00-4:00", "4:00-6:00"};
    private static String[] sModifyTimeArea = {"8:00-8:02", "10:00-10:02", "12:00-12:02", "14:00-14:02",
        "16:00-16:02", "18:00-18:02", "20:00-20:02", "22:00-22:02", "0:00-0:02", "2:00-2:02",
        "4:00-4:02", "6:00-6:02"};

    public static Drawable getLockScreenPre(Context context) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        File file = new File(Consts.LOCKSCREEN_WALLPAPER_PATH);
        try {
            fis = new FileInputStream(Consts.LOCKSCREEN_WALLPAPER_PATH);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPurgeable = true;  
            opt.inInputShareable = true; 
            opt.inSampleSize = 3;
            bitmap = BitmapFactory.decodeStream(fis, null, opt);
            BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            return drawable;
        } catch (FileNotFoundException e) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.default_lockpaper);
            return drawable;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            if (!bitmap.isRecycled()) {
//                bitmap.recycle();
//            }
        }
    }

    public static long getFileModifyTime(File file) {
        if (file.exists()) {
            return file.lastModified();
        }
        return -1;
    }

    /**
     * 读取文件修改时间的方法1
     */
    /*@SuppressWarnings("deprecation")
    public static void getModifiedTime_1(File file) {
        if (file.exists()) {
            Calendar cal = Calendar.getInstance();
            long time = file.lastModified();
            cal.setTimeInMillis(time);
            // 此处toLocalString()方法是不推荐的，但是仍可输出
            System.out.println("修改时间[1] " + cal.getTime().toLocaleString());
            // 输出：修改时间[1] 2009-8-17 10:32:38
        }
    }*/

    /**
     * 读取修改时间的方法
     */
    public static double getModifiedTime(File file) {
        if (file.exists()) {
            long fileTime = file.lastModified();
            long nowTime = new Date().getTime();
            long useTime = Math.abs(nowTime - fileTime);
            double result = useTime * 1.0 / (1000 * 60 * 60);
            Log.d(ChangeReceiver.LOCK_TAG, "getModifiedTime: time=" + result + ",nowTime = " + nowTime * 1.0 / (1000 * 60 * 60)
                    + ",fileTime = " + fileTime * 1.0 / (1000 * 60 * 60));
            return result;
        }
        return -1;
    }

    public static boolean isReallyModify() {
        File file = new File(Consts.LOCKSCREEN_WALLPAPER_PATH);
        boolean bool = false;
        double useTime = getModifiedTime(file);
        boolean isModifyTime = false;
        try {
            isModifyTime = isModifyTimeArea(sModifyTimeArea);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(ChangeReceiver.LOCK_TAG, isModifyTime+"-->isReallyModify: useTime = " + useTime);
        if (isModifyTime || useTime > 1.9 || useTime == -1) {
            bool = true;
        }
        return bool;
    }

    public static String getCurrentLockPaperPath(Context context) {
        String path = "";
        String group_name = DataOperation.getStringPreference(context, Consts.CURRENT_LOCKPAPER_GROUP,
        		Consts.DEFAULT_LOCKPAPER_GROUP);
        Log.d(ChangeReceiver.LOCK_TAG, "getCurrentLockPaperPath: group_name = " + group_name);
        path = getCurrentLockPaperPath(context, group_name);
        File file = new File(path);
        if (!file.exists()) {
            DataOperation.setStringPreference(context, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
            path = getCurrentLockPaperPath(context, Consts.DEFAULT_LOCKPAPER_GROUP);
        }
        return path;
    }

    public static String getCurrentLockPaperPath(Context context, String currentGroup) {
        String path = "";
        DbControl dbControl = new DbControl(context);
        PictureGroupInfo groupInfo = dbControl.queryGroupByName(currentGroup);
        List<PictureInfo> list = dbControl.queryAllItemsByGroupId(groupInfo.getId());
        dbControl.close();
        path = getCurrentLockPaperPath(list);
        Log.d(ChangeReceiver.LOCK_TAG, currentGroup+"-->getCurrentLockPaperPath: path=" + path);
        return path;
    }

    private static String getCurrentLockPaperPath(List<PictureInfo> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        int listIndex = 0;
        int index = 0;
        String path = "";
        try {
            index = getTimeArea(sTimeArea);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int size = list.size();
        if (index >= size && size != 0) {
            listIndex = index % size;
        } else {
            listIndex = index;
        }
        path = String.valueOf(list.get(listIndex).getBigIcon());
        return path;
    }

    public static String getNextLockPaperPath(Context context) {
        String path = "";
        String group_name = DataOperation.getStringPreference(context, Consts.CURRENT_LOCKPAPER_GROUP,
        		Consts.DEFAULT_LOCKPAPER_GROUP);
        path = getNextLockPaperPath(context, group_name);
        File file = new File(path);
        if (!file.exists()) {
            DataOperation.setStringPreference(context, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
            path = getNextLockPaperPath(context, Consts.DEFAULT_LOCKPAPER_GROUP);
        }
        return path;
    }

    private static String getNextLockPaperPath(Context context, String group_name){
        String path = "";
        DbControl dbControl = new DbControl(context);
        PictureGroupInfo groupInfo = dbControl.queryGroupByName(group_name);
        List<PictureInfo> list = dbControl.queryAllItemsByGroupId(groupInfo.getId());
        dbControl.close();
        path = getNextLockPaperPath(list);
        Log.d(ChangeReceiver.LOCK_TAG, "getNextLockPaperPath: NextPath=" + path);
        return path;
    }

    private static String getNextLockPaperPath(List<PictureInfo> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        int listIndex = 0;
        int index = 0;
        String path = "";
        try {
            index = getTimeArea(sTimeArea);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        index++;
        int size = list.size();
        if (index >= size  && size != 0) {
            listIndex = index % size;
        } else {
            listIndex = index;
        }
        path = String.valueOf(list.get(listIndex).getBigIcon());
        return path;
    }

    private static boolean isModifyTimeArea(String time[]) throws ParseException {
        Date now = new Date();
        now = sDateFormat.parse(sDateFormat.format(now));
        for (int i = 0; i < time.length; i++) {
            if (isTimeArea(now, time[i]))
                return true;
        }
        return false;
    }

    private static int getTimeArea(String time[]) throws ParseException {
//        String[] arr = {"6:00-8:00", "8:00-10:00", "10:00-12:00", "12:00-14:00", "14:00-16:00",
//                "16:00-18:00", "18:00-20:00", "20:00-22:00", "22:00-23:59", "0:00-2:00", "2:00-4:00",
//                "4:00-6:00"};
		  // Aurora liugj 2014-09-09 modified for lockpaper change by time error start
        Date now = new Date(System.currentTimeMillis());
		  // Aurora liugj 2014-09-09 modified for lockpaper change by time error end
        now = sDateFormat.parse(sDateFormat.format(now));
        int i = 0;
        for (; i < time.length; i++) {
            if (isTimeArea(now, time[i]))
                break;
        }
        int index = i;
        Log.d(ChangeReceiver.LOCK_TAG, index + "=index -- getTimeArea: now=" + now);
        return index;
    }

    //获取距离下一次更换壁纸的时间
    public static long getNextTime() {
        String time = "";
        int index = 0;
        try {
            index = getTimeArea(sTimeArea);
        } catch (ParseException e) {
            index = 0;
            e.printStackTrace();
        }
        if (index > (sModifyTimeArea.length - 1)) {
            index = sModifyTimeArea.length - 1;
        }
//        if (index >= 11) {
//            index = 0;
//        } else {
//            index++;
//        }
        time = sModifyTimeArea[index];
        String[] s = time.split("-");
        Date start = null;
        try {
            start = sDateFormat.parse(s[0]);
        } catch (ParseException e) {
            start = new Date(System.currentTimeMillis());
            e.printStackTrace();
        }
        Date now = new Date(System.currentTimeMillis());
        start.setYear(now.getYear());
        start.setMonth(now.getMonth());
        if (s[0].equals("0:00")) {
        	Log.d(ChangeReceiver.LOCK_TAG, "now.getDate(): " + now.getDate());
        	start.setDate(now.getDate() + 1);
		}else {
			start.setDate(now.getDate());
		}
        long nowTime = now.getTime();
        long intervalTime = start.getTime() - nowTime;
        Log.d(ChangeReceiver.LOCK_TAG, "getNextTime: time=" + start.toString() + ",date=" + start.getTime() + ",intervalTime=" + intervalTime);
        return start.getTime();
    }

    public static boolean isTimeArea(Date now, String arg) throws ParseException {
        String[] s = arg.split("-");
        Date start = sDateFormat.parse(s[0]);
        Date end = sDateFormat.parse(s[1]);
        return start.getTime() <= now.getTime() && end.getTime() > now.getTime();
    }

    public static class PictureInfoComparator implements Comparator<PictureInfo> {
        @Override
        public int compare(PictureInfo lhs, PictureInfo rhs) {
//            int lhsNum = 0;
//            int rhsNum = 0;
//            try {
//                lhsNum = Integer.valueOf(lhs.getIdentify().substring(4));
//                rhsNum = Integer.valueOf(rhs.getIdentify().substring(4));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if (lhsNum < rhsNum) {
//                return -1;
//            }
//            if (lhsNum > rhsNum) {
//                return 1;
//            }
//            return 0;
            return lhs.getIdentify().compareTo(rhs.getIdentify());
        }
    }
}
