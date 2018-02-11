LOCAL_PATH := $(call my-dir)

my_archs := arm x86 arm64 x86_64
my_src_arch := $(call get-prebuilt-src-arch, $(my_archs))

ifeq ($(my_src_arch),arm)
my_src_abi := armeabi-v7a
else ifeq ($(my_src_arch),arm64)
my_src_abi := arm64-v8a
endif

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := Aurora_VoiceAssistant
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

LOCAL_PREBUILT_JNI_LIBS := \
    lib/$(my_src_abi)/libdict.so \
    lib/$(my_src_abi)/libencrypt_sogou_v00.so \
    lib/$(my_src_abi)/liblocSDK6.so \
    lib/$(my_src_abi)/libsnd.so \
    lib/$(my_src_abi)/libspeex_sogou_v42.so \
    lib/$(my_src_abi)/libttsoff.so
include $(BUILD_PREBUILT)

