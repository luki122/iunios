ifneq ($(findstring IUNI,$(GN_PROJECT)),)
LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := Aurora_Music
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

LOCAL_DEX_PREOPT := false

PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/lib/arm64/libaudiocodec.so:system/lib64/libaudiocodec.so \
    $(LOCAL_PATH)/lib/arm64/libauroraplayer.so:system/lib64/libauroraplayer.so \
    $(LOCAL_PATH)/lib/arm64/libencryptor.so:system/lib64/libencryptor.so \
    $(LOCAL_PATH)/lib/arm64/libgnustl_shared.so:system/lib64/libgnustl_shared.so



PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/lib/arm/libencryptor.so:system/app/Aurora_Music/lib/arm/libencryptor.so \
    $(LOCAL_PATH)/lib/arm/libxiamitag.so:system/app/Aurora_Music/lib/arm/libxiamitag.so \
    $(LOCAL_PATH)/lib/arm/libaudiocodec.so:system/app/Aurora_Music/lib/arm/libaudiocodec.so \
    $(LOCAL_PATH)/lib/arm/libauroraplayer.so:system/app/Aurora_Music/lib/arm/libauroraplayer.so \
    $(LOCAL_PATH)/lib/arm/libgnustl_shared.so:system/app/Aurora_Music/lib/arm/libgnustl_shared.so \
    $(LOCAL_PATH)/lib/arm/libweibosdkcore.so:system/app/Aurora_Music/lib/arm/libweibosdkcore.so

include $(BUILD_PREBUILT)
endif
