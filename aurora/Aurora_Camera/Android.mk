LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := Aurora_Camera
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

LOCAL_MULTILIB := 32

LOCAL_PREBUILT_JNI_LIBS := \
    lib/armeabi-v7a/libamui.so \
    lib/armeabi-v7a/libarcimgutilsbase.so \
    lib/armeabi-v7a/libarcimgutils.so \
    lib/armeabi-v7a/libarcplatform.so \
    lib/armeabi-v7a/libarcsoft_panorama_burstcapture.so \
    lib/armeabi-v7a/libati_framework_imagecodec_v2.0.2.so \
    lib/armeabi-v7a/libati_framework_v2.0.1.so \
    lib/armeabi-v7a/libcapsjava.so \
    lib/armeabi-v7a/libJNI_ImageBlur.so \
    lib/armeabi-v7a/libjni_VDMagicFocus.so \
    lib/armeabi-v7a/libPanoramaBCPlugin.so \
    lib/armeabi-v7a/libVDSingleHDRAPI-jni.so

include $(BUILD_PREBUILT)

