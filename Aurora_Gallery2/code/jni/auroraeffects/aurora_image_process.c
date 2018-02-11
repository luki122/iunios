#include "amcomdef.h"
#include "amdisplay.h"
#include "merror.h"
#include "asvloffscreen.h"
#include "com_android_gallery3d_filtershow_auroraeffects_ImageProcessor.h"
#include "amipengine.h"


#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#define  LOG_TAG    "libAuroraImageProcess"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


static MRESULT GetOffScreenFromBitmapJObj(JNIEnv *env, jobject srcBitmap, LPASVLOFFSCREEN offscreen, void** addrPtr)
{

	AndroidBitmapInfo info = {0};
	AndroidBitmap_getInfo(env, srcBitmap, &info);

	//void* addrPtr = NULL;
	//AndroidBitmap_lockPixels(env, srcBitmap, addrPtr);

	//LOGI("getOffScreenFromBitmapJObj --> info.width: %d", info.width);
	//LOGI("getOffScreenFromBitmapJObj --> info.height: %d", info.height);
	//LOGI("getOffScreenFromBitmapJObj --> info.stride: %d", info.stride);

	offscreen->i32Width = info.width;
	offscreen->i32Height = info.height;
	offscreen->pi32Pitch[0] = info.stride;
	offscreen->pi32Pitch[1] = 0;
	offscreen->pi32Pitch[2] = 0;

	//set format
	switch (info.format) {
	case ANDROID_BITMAP_FORMAT_NONE:
		//LOGE("GetOffScreenFromBitmapJObj --> unknown ANDROID_BITMAP_FORMAT_NONE");
		//offscreen->u32PixelArrayFormat = ASVL_PAF_RGB32_A8R8G8B8;
		break;
	case ANDROID_BITMAP_FORMAT_RGBA_8888:
		//LOGI("GetOffScreenFromBitmapJObj --> ANDROID_BITMAP_FORMAT_RGBA_8888");
		//offscreen->u32PixelArrayFormat = ASVL_PAF_RGB32_A8R8G8B8;
		offscreen->u32PixelArrayFormat = ASVL_PAF_RGB32_B8G8R8A8;
		break;
	case ANDROID_BITMAP_FORMAT_RGB_565:
		//LOGE("GetOffScreenFromBitmapJObj --> ANDROID_BITMAP_FORMAT_RGB_565");
		offscreen->u32PixelArrayFormat = ASVL_PAF_RGB16_R5G6B5;
		break;
	case ANDROID_BITMAP_FORMAT_RGBA_4444:
		//LOGE("GetOffScreenFromBitmapJObj --> ANDROID_BITMAP_FORMAT_RGBA_4444");
		//out->dwPixelArrayFormat = PAF_R4G4B4;
		break;
	case ANDROID_BITMAP_FORMAT_A_8:
		//out.dwPixelArrayFormat = PAF_R5G6B5;
		//LOGE("GetOffScreenFromBitmapJObj --> unknown ANDROID_BITMAP_FORMAT_A_8");
		break;
	default:
		//LOGE("GetOffScreenFromBitmapJObj --> unknown default...");
		break;
	}

	if (addrPtr == NULL) {
		LOGE("GetOffScreenFromBitmapJObj --> addrPtr == NULL");
	} else {
		LOGI("GetOffScreenFromBitmapJObj --> addrPtr != NULL");
	}

	offscreen->ppu8Plane[0] = *addrPtr;
	offscreen->ppu8Plane[1] = NULL;
	offscreen->ppu8Plane[2] = NULL;
	return MOK;
}


static void unlockBitmap(JNIEnv *env, jobject srcBitmap) {
	AndroidBitmap_unlockPixels(env, srcBitmap);
}

/*
static jobject saveBitmap(JNIEnv *env, jobject srcBitmap, void *bitmapPixels, LPASVLOFFSCREEN offscreen) {
	MInt32* src = (MInt32*) bitmapPixels;
	MInt32* tempPixels = (MInt32*) malloc (sizeof(MInt32)*128);
	int pixelsCount = offscreen->i32Width * offscreen->i32Height;
	memcpy(tempPixels, src, sizeof(MInt32) * pixelsCount);

	//
	//creating a new bitmap to put the pixels into it - using Bitmap Bitmap.createBitmap (int width, int height, Bitmap.Config config) :
	//
	//LOGI("creating new bitmap...");
	jclass bitmapCls = (*env)->GetObjectClass(env, srcBitmap);
	jmethodID createBitmapFunction = (*env)->GetStaticMethodID(env, bitmapCls,
			"createBitmap",
			"(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
	jstring configName = (*env)->NewStringUTF(env, "ARGB_8888");
	jclass bitmapConfigClass =  (*env)->FindClass(env, "android/graphics/Bitmap$Config");
	jmethodID valueOfBitmapConfigFunction = (*env)->GetStaticMethodID(env,
			bitmapConfigClass, "valueOf",
			"(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
	jobject bitmapConfig = (*env)->CallStaticObjectMethod(env, bitmapConfigClass,
			valueOfBitmapConfigFunction, configName);
	jobject newBitmap = (*env)->CallStaticObjectMethod(env, bitmapCls,
			createBitmapFunction, offscreen->i32Height, offscreen->i32Width, bitmapConfig);
	//
	// putting the pixels into the new bitmap:
	//
	if ((AndroidBitmap_lockPixels(env, newBitmap, &bitmapPixels)) < 0) {
		//LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return NULL;
	}
	MInt32* newBitmapPixels = (MInt32*) bitmapPixels;
	int whereToPut = 0;
	int x = offscreen->i32Width - 1;
	int y = 0;
	for (; x >= 0; --x)
		for (; y < offscreen->i32Height; ++y) {
			MInt32 pixel = tempPixels[offscreen->i32Width * y + x];
			newBitmapPixels[whereToPut++] = pixel;
		}
	AndroidBitmap_unlockPixels(env, newBitmap);
	//
	// freeing the native memory used to store the pixels
	//
	free(tempPixels);
	return newBitmap;
}


static void recycleBitmap(JNIEnv *env, jobject srcBitmap)
{
	//
	//recycle bitmap - using bitmap.recycle()
	//
	LOGI("recycling bitmap...");
	jclass bitmapCls = (*env)->GetObjectClass(env, srcBitmap);
	jmethodID recycleFunction = (*env)->GetMethodID(env, bitmapCls, "recycle", "()V");
	if (recycleFunction == 0) {
		LOGE("error recycling!");
		return;
	}
	(*env)->CallVoidMethod(env, srcBitmap, recycleFunction);
}
*/


//JNIEXPORT jobject JNICALL Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap
//  (JNIEnv *env, jobject obj, jobject src)

JNIEXPORT jobject JNICALL Java_com_android_gallery3d_filtershow_auroraeffects_ImageProcessor_generateFilteredBitmap__Landroid_graphics_Bitmap_2
  (JNIEnv *env, jclass obj, jobject src)

{
	MDWord  dwSrcSpaceID = MPAF_RGB24_R8G8B8;
	ASVLOFFSCREEN offscreen = {0};
	void* addrPtr = NULL;// bitmap pixel address pointer

	AndroidBitmap_lockPixels(env, src, &addrPtr);

	MRESULT result = GetOffScreenFromBitmapJObj(env, src, &offscreen, &addrPtr);

	if(addrPtr == NULL) {
		//LOGE("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> addrPtr == null"  );
		return NULL;
	} else {
		//LOGE("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> addrPtr != null" );
	}

	LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> result ---> MOK");

	//MTCHAR *maskPath = "/mnt/sdcard/";
	//char *maskPath = "/system/etc/gn_camera_feature/arcsoft/maskfile/";
	char *maskPath = "/system/iuni/aurora/gallery/maskfile/";

	CosmetologyBackligntParam cosmetologybacklight = {0};
	cosmetologybacklight.dDermabrasionStrength = 100;
	cosmetologybacklight.lDermabrasionSize     = 10;
	cosmetologybacklight.lWhiteningStrength    = 50;

	//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> cosmetologybacklight fill finished ");

	MEffectParam effectPara = {0};

	effectPara.dwEffectID = MEID_SNOWFLAKES;//MEID_LIGHTBEAM;//MEID_SNOWFLAKES;
	effectPara.dwParamSize= sizeof(CosmetologyBackligntParam);
	effectPara.pEffect    = &cosmetologybacklight;

	//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> effectPara fill finished ");

	MPixelInfo pixelinfo = {0};
	pixelinfo.dwPixelArrayFormat = offscreen.u32PixelArrayFormat;
	pixelinfo.lHeight            = offscreen.i32Height;
	pixelinfo.lWidth             = offscreen.i32Width;

	//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MPixelInfo fill finished ");

	MHandle handle = MNull;

	MRESULT res = MOK;
	res = MIPCreateImageEngine(maskPath, &effectPara, &pixelinfo, &handle);

	//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPCreateImageEngine finished ");

	if(handle == NULL) {
		LOGE("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> handle == NULL ");
	} else {
		LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> handle != NULL ");
	}

	if (res != MOK)
	{
		LOGE("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPCreateImageEngine res != MOK ");
		goto EXIT;
	} else {
		LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPCreateImageEngine res == MOK before MIPDoEffect");
	}

	res = MIPDoEffect(handle, &offscreen);

	//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> after MIPDoEffect");

	if(res != 0)
	{
		//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPDoEffect failed !!! ");
		goto EXIT;
	} else {
		//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPDoEffect succeeded !!!");
	}

EXIT:
	if(handle)
	{
		//LOGI("Java_com_example_testimageprocess_ImageProcessor_MIPDestroyImageEngine");
		MIPDestroyImageEngine(handle);
	}

	AndroidBitmap_unlockPixels(env, src);
	//TODO: ......
	return src;
	//return res;
}



JNIEXPORT jobject JNICALL Java_com_android_gallery3d_filtershow_auroraeffects_ImageProcessor_generateFilteredBitmap__Landroid_graphics_Bitmap_2I
  (JNIEnv *env, jclass obj, jobject src, jint type)
{
	MDWord  dwSrcSpaceID = MPAF_RGB24_R8G8B8;
		ASVLOFFSCREEN offscreen = {0};
		void* addrPtr = NULL;// bitmap pixel address pointer

		AndroidBitmap_lockPixels(env, src, &addrPtr);

		MRESULT result = GetOffScreenFromBitmapJObj(env, src, &offscreen, &addrPtr);

		if(addrPtr == NULL) {
			//LOGE("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> addrPtr == null"  );
			return NULL;
		} else {
			//LOGE("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> addrPtr != null" );
		}

		LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> result ---> MOK");

		//MTCHAR *maskPath = "/mnt/sdcard/";
		//char *maskPath = "/system/etc/gn_camera_feature/arcsoft/maskfile/";
		char *maskPath = "/system/iuni/aurora/gallery/maskfile/";

		CosmetologyBackligntParam cosmetologybacklight = {0};
		cosmetologybacklight.dDermabrasionStrength = 100;
		cosmetologybacklight.lDermabrasionSize     = 10;
		cosmetologybacklight.lWhiteningStrength    = 50;

		//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> cosmetologybacklight fill finished ");

		MEffectParam effectPara = {0};

		effectPara.dwEffectID = type;//MEID_SNOWFLAKES;//MEID_LIGHTBEAM;//MEID_SNOWFLAKES;
		effectPara.dwParamSize= sizeof(CosmetologyBackligntParam);
		effectPara.pEffect    = &cosmetologybacklight;

		//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> effectPara fill finished ");

		MPixelInfo pixelinfo = {0};
		pixelinfo.dwPixelArrayFormat = offscreen.u32PixelArrayFormat;
		pixelinfo.lHeight            = offscreen.i32Height;
		pixelinfo.lWidth             = offscreen.i32Width;

		//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MPixelInfo fill finished ");

		MHandle handle = MNull;

		MRESULT res = MOK;
		res = MIPCreateImageEngine(maskPath, &effectPara, &pixelinfo, &handle);

		//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPCreateImageEngine finished ");

		if(handle == NULL) {
			//LOGE("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> handle == NULL ");
		} else {
			//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> handle != NULL ");
		}

		if (res != MOK)
		{
			//LOGE("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPCreateImageEngine res != MOK ");
			goto EXIT;
		} else {
			//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPCreateImageEngine res == MOK before MIPDoEffect");
		}

		res = MIPDoEffect(handle, &offscreen);

		//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> after MIPDoEffect");

		if(res != 0)
		{
			//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPDoEffect failed !!! ");
			goto EXIT;
		} else {
			//LOGI("Java_com_example_testimageprocess_ImageProcessor_generateFilteredBitmap --> MIPDoEffect succeeded !!!");
		}

	EXIT:
		if(handle)
		{
			//LOGI("Java_com_example_testimageprocess_ImageProcessor_MIPDestroyImageEngine");
			MIPDestroyImageEngine(handle);
		}
		AndroidBitmap_unlockPixels(env, src);
		//TODO: ......
		return src;
		//return res;
}

