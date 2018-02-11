LOCAL_PATH := $(call my-dir)

my_archs := arm x86 arm64 x86_64
my_src_arch := $(call get-prebuilt-src-arch, $(my_archs))


include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := Aurora_Gallery
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

LOCAL_MULTILIB := 32

LOCAL_PREBUILT_JNI_LIBS := \
    lib/arm/libAuroraImageProcess.so


include $(BUILD_PREBUILT)

