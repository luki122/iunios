package com.aurora.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;


/**
 * 图片调色处理
 * 
 * @author xiexiujie
 * 
 */
public class PictureTools {

	/**
	 * 饱和度标识
	 */
	public static final int FLAG_SATURATION = 0x0;

	/**
	 * 亮度标识
	 */
	public static final int FLAG_LUM = 0x1;

	/**
	 * 色相标识
	 */
	public static final int FLAG_HUE = 0x2;

	

	private ColorMatrix mLightnessMatrix;
	private ColorMatrix mSaturationMatrix;
	private ColorMatrix mHueMatrix;
	private ColorMatrix mAllMatrix;

	/**
	 * 亮度
	 */
	private float mLumValue = 1F;

	/**
	 * 饱和度
	 */
	private float mSaturationValue = 0F;

	/**
	 * 色相
	 */
	private float mHueValue = 0F;

	

	public PictureTools(Context context) {

	}

	

	/**
	 * 设置饱和度值
	 * 
	 * @param saturation
	 */
	public void setSaturation(int saturation) {
		mSaturationValue = saturation * 1.0F / 127;
	}

	/**
	 * 设置色相值
	 * 
	 * @param hue
	 */
	public void setHue(int hue) {
		mHueValue = hue * 1.0F / 127;
	}

	/**
	 * 设置亮度值
	 * 
	 * @param lum
	 */
	public void setLum(int lum) {
		mLumValue = (lum - 127) * 1.0F / 127 * 180;
	}

	

	/**
	 * 
	 * @param flag
	 *            比特位0 表示是否改变色相，比位1表示是否改变饱和度,比特位2表示是否改变明亮度
	 */
	public Bitmap handleImage(Bitmap bm, int flag) {
		Bitmap bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(),
				Bitmap.Config.ARGB_8888);
		// 创建一个相同尺寸的可变的位图区,用于绘制调色后的图片
		Canvas canvas = new Canvas(bmp); // 得到画笔对象
		Paint paint = new Paint(); // 新建paint
		paint.setAntiAlias(true); // 设置抗锯齿,也即是边缘做平滑处理
		if (null == mAllMatrix) {
			mAllMatrix = new ColorMatrix();
		}

		if (null == mLightnessMatrix) {
			mLightnessMatrix = new ColorMatrix(); // 用于颜色变换的矩阵，android位图颜色变化处理主要是靠该对象完成
		}

		if (null == mSaturationMatrix) {
			mSaturationMatrix = new ColorMatrix();
		}

		if (null == mHueMatrix) {
			mHueMatrix = new ColorMatrix();
		}

		switch (flag) {
		case FLAG_HUE: // 需要改变色相
			mHueMatrix.reset();
			mHueMatrix.setScale(mHueValue, mHueValue, mHueValue, 1); // 红、绿、蓝三分量按相同的比例,最后一个参数1表示透明度不做变化，此函数详细说明参考
			// // android
			// doc
			break;
		case FLAG_SATURATION: // 需要改变饱和度
			// saturation 饱和度值，最小可设为0，此时对应的是灰度图(也就是俗话的“黑白图”)，
			// 为1表示饱和度不变，设置大于1，就显示过饱和
			mSaturationMatrix.reset();
			mSaturationMatrix.setSaturation(mSaturationValue);
			break;
		case FLAG_LUM: // 亮度
			// hueColor就是色轮旋转的角度,正值表示顺时针旋转，负值表示逆时针旋转
			mLightnessMatrix.reset(); // 设为默认值
			mLightnessMatrix.setRotate(0, mLumValue); // 控制让红色区在色轮上旋转的角度
			mLightnessMatrix.setRotate(1, mLumValue); // 控制让绿红色区在色轮上旋转的角度
			mLightnessMatrix.setRotate(2, mLumValue); // 控制让蓝色区在色轮上旋转的角度
			// 这里相当于改变的是全图的色相
			break;
		}
		mAllMatrix.reset();
		mAllMatrix.postConcat(mHueMatrix);
		mAllMatrix.postConcat(mSaturationMatrix); // 效果叠加
		mAllMatrix.postConcat(mLightnessMatrix); // 效果叠加

		paint.setColorFilter(new ColorMatrixColorFilter(mAllMatrix));// 设置颜色变换效果
		canvas.drawBitmap(bm, 0, 0, paint); // 将颜色变化后的图片输出到新创建的位图区
		// 返回新的位图，也即调色处理后的图片
		return bmp;
	}

}
