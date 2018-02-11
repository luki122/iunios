LOCAL_PATH := $(call my-dir)

my_archs := arm x86 arm64 x86_64
my_src_arch := $(call get-prebuilt-src-arch, $(my_archs))

ifeq ($(my_src_arch),arm)
my_src_abi := armeabi
else ifeq ($(my_src_arch),arm64)
my_src_abi := arm64-v8a
endif

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := YuloreFrameWork
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

LOCAL_PREBUILT_JNI_LIBS := \
    libs/$(my_src_abi)/libposeidon-1.0.0-mfr.so \
    libs/$(my_src_abi)/libTmsdk-2.0.7-mfr.so


include $(BUILD_PREBUILT)

