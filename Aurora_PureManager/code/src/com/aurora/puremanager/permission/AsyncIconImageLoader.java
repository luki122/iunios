package com.aurora.puremanager.permission;


import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.annotation.SuppressLint;

public class AsyncIconImageLoader {

    private HashMap<String, SoftReference<Drawable>> imageCache;

    @SuppressLint("SdCardPath")
    private static final String ICON_IMAGE_PATH = Environment.getDataDirectory().toString() + File.separator
            + "data" + File.separator + "com.gionee.softmanager" + File.separator + "iconcache";// "/data/data/com.example.test/iconcache";
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public AsyncIconImageLoader() {
        imageCache = new HashMap<String, SoftReference<Drawable>>();
    }

    public Drawable loadDrawable(final String packageName, final Context context,
            final ImageCallback imageCallback) {
        if (imageCache.containsKey(packageName)) {
            SoftReference<Drawable> softReference = imageCache.get(packageName);
            Drawable drawable = softReference.get();
            if (drawable != null) {
                return drawable;
            } else {
                imageCache.remove(packageName);
            }
        }

        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                imageCallback.imageLoaded((Drawable) message.obj, packageName);
            }
        };

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Drawable drawable = getIconFromSDCard(packageName);
                if (drawable == null) {
                    drawable = loadIconImage(packageName, context);
                    saveIconToSDCard(drawable, packageName);
                }
                imageCache.put(packageName, new SoftReference<Drawable>(drawable));

                Message message = handler.obtainMessage(0, drawable);
                handler.sendMessage(message);
            }
        });
        return null;
    }

    public static Drawable loadIconImage(String pkgName, Context context) {
        ApplicationInfo info = HelperUtils_per.getApplicationInfo(context, pkgName);
        if (info != null) {
            return HelperUtils_per.loadIcon(context, info);
        } else {
            return null;
        }
    }

    public interface ImageCallback {
        public void imageLoaded(Drawable imageDrawable, String pkgName);
    }

    public void saveIconToSDCard(Drawable dw, String packageName) {
        try {
            BitmapDrawable bd = (BitmapDrawable) dw;
            Bitmap bm = bd.getBitmap();
            File file = new File(ICON_IMAGE_PATH + "/" + packageName);
            File iconDir = new File(ICON_IMAGE_PATH);
            if (!iconDir.exists()) {
                boolean blmkdir = iconDir.mkdir();
                Log.i("AsyncIconImageLoader","make dir : "+blmkdir );
            }

            if (!file.exists()) {
                try {
                    boolean blcnf = file.createNewFile();
                    Log.i("AsyncIconImageLoader","create new file : "+blcnf );
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }

            }

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("null")
    public Drawable getIconFromSDCard(String packageName) {
        Bitmap bmpDefaultPic = null;

        String imageSDCardPath = ICON_IMAGE_PATH + "/" + packageName;
        File file = new File(imageSDCardPath);

        if (!file.exists()) {
            return null;
        }
        try {
            bmpDefaultPic = BitmapFactory.decodeFile(imageSDCardPath, null);
            if (bmpDefaultPic != null /*|| bmpDefaultPic.toString().length() > 3*/) {
                @SuppressWarnings("deprecation")
                Drawable drawable = new BitmapDrawable(bmpDefaultPic);
                return drawable;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cleanIcons() {
        File file = new File(ICON_IMAGE_PATH);
        if (!file.exists()) {
           boolean bldelete = file.delete();
           Log.i("AsyncIconImageLoader","delete  file : "+bldelete );
            
        }
    }

}
