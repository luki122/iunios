package com.aurora.feedback.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import com.aurora.android.mms.pdu.Base64;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FeedBackUtil 
{
	private static final String TAG="FeedBackUtil";
	public static Bitmap convertToBitmap(String path, int w, int h) 
	{
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 设置为ture只获取图片大小
		opts.inJustDecodeBounds = true;
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		int width = opts.outWidth;
		int height = opts.outHeight;
		float scaleWidth = 0.f, scaleHeight = 0.f;
		if (width > w || height > h)
		{
			scaleWidth = ((float) width) / w;
			scaleHeight = ((float) height) / h;
		}
		opts.inJustDecodeBounds = false;
		float scale = Math.max(scaleWidth, scaleHeight);
		opts.inSampleSize = (int) scale;
		opts.inDither=false;    /*不进行图片抖动处理*/
		opts.inInputShareable=true;
		opts.inPreferredConfig=null;  /*设置让解码器以最佳方式解码*/
//		WeakReference<Bitmap> weak = new WeakReference<Bitmap>(
//				BitmapFactory.decodeFile(path, opts));
		Bitmap tempbitmap=BitmapFactory.decodeFile(path, opts);
		Bitmap cutBitmap=null;
		if(tempbitmap!=null)
		{
			cutBitmap=ImageCrop(tempbitmap);
		}
		WeakReference<Bitmap> weak = new WeakReference<Bitmap>(
				/*BitmapFactory.decodeFile(path, opts)*/cutBitmap);
		Bitmap bitmap=Bitmap.createScaledBitmap(weak.get(), w, h, true);
		return createFramedPhoto(bitmap.getWidth(),bitmap.getHeight(),bitmap,(float) 15.0);
	}
	
    /**
     * 按正方形裁切图片
     */
    public static Bitmap ImageCrop(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长
        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;
        //下面这句是关键
        return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
    }
	
    /**
    *
    * @param x 图像的宽度
    * @param y 图像的高度
    * @param image 源图片
    * @param outerRadiusRat 圆角的大小
    * @return 圆角图片
    */
   static Bitmap createFramedPhoto(int x, int y, Bitmap image, float outerRadiusRat) {
       //根据源文件新建一个darwable对象
       Drawable imageDrawable = new BitmapDrawable(image);

       // 新建一个新的输出图片
       Bitmap output = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
       Canvas canvas = new Canvas(output);

       // 新建一个矩形
       RectF outerRect = new RectF(0, 0, x, y);

       // 产生一个红色的圆角矩形
       Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
       paint.setColor(Color.RED);
       canvas.drawRoundRect(outerRect, outerRadiusRat, outerRadiusRat, paint);

       // 将源图片绘制到这个圆角矩形上
       paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
       imageDrawable.setBounds(0, 0, x, y);
       canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
       imageDrawable.draw(canvas);
       canvas.restore();

       return output;
   }
   
   public static String storePath(String mCurrentPhotoPath)
   {
		File f = new File(mCurrentPhotoPath);

		Bitmap bm = getSmallBitmap(mCurrentPhotoPath);

		FileOutputStream fos=null;
		final File file=new File(getAlbumDir(), "small_" + f.getName());
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bm.compress(Bitmap.CompressFormat.JPEG, 20, fos);
		Log.d("gd", "path="+file.getAbsolutePath());
		return file.getAbsolutePath();
   }
   
	/**
	 * 根据路径获得突破并压缩返回bitmap用于显示
	 * 
	 * @param imagesrc
	 * @return
	 */
	public static Bitmap getSmallBitmap(String filePath) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, 1280, 720);
//		options.inSampleSize = calculateInSampleSize(options, 800, 480);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}
	
	/**
	 * 计算图片的缩放值
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
	
	/**
	 * 获取保存图片的目录
	 * 
	 * @return
	 */
	public static File getAlbumDir() {
		File dir = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),getAlbumName());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
   
	/**
	 * 获取保存 隐患检查的图片文件夹名称
	 * @return
	 */
	public static String getAlbumName() {
		return "penggangding";
	}
   
	/**
	 * 将bitmap 转化成 file 文件
	 * @param bmp
	 * @param filename
	 * @return
	 */
/*	public static File saveBitmapTofile(Bitmap bmp, String filename) {
		CompressFormat format = Bitmap.CompressFormat.PNG;
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File sdCardDir = Environment.getExternalStorageDirectory();//获取SDCard目录
			}
		Log.d(TAG,"State="+Environment.getExternalStorageState());
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(Environment.getExternalStorageDirectory() + "NN");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(stream==null)
		{
			Log.d(TAG, "stream is null }}}}}}}}}");
		}
		if (bmp.compress(format, quality, stream)) {
			
			return new File(Environment.getExternalStorageDirectory() + filename);
		}
		return null;
	}*/
	
	static String getDeviceRelease()
	{
		return Build.MODEL;
	}
	
	static String getDeviceModel()
	{
		return Build.VERSION.RELEASE;
	}
	
	/**
	 * 获取CPU信号和频率
	 * @return
	 */
	public static  String[] getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String[] cpuInfo = { "", "" }; // 1-cpu型号 //2-cpu频率
		String[] arrayOfString;
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
			}
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			cpuInfo[1] += arrayOfString[2];
			localBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//1-cpu型号  //2-cpu频率  
		Log.i(TAG, "cpuinfo:" + cpuInfo[0] + " " + cpuInfo[1]);
		return cpuInfo;
	}
	
    static String getIMEIID(Context mContext)
    {
    	TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    	return tm.getDeviceId();
    }
    
	public static boolean isNetworkAvailable(Context context) {   
        ConnectivityManager cm = (ConnectivityManager) context   
                .getSystemService(Context.CONNECTIVITY_SERVICE);   
        if (cm == null) {   
        } else {
            NetworkInfo[] info = cm.getAllNetworkInfo();   
            if (info != null) {   
                for (int i = 0; i < info.length; i++) {   
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {   
                        return true;   
                    }   
                }   
            }   
        }   
        return false;
	}
	
}


