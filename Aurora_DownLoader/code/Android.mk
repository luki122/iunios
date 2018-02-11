#ifeq ("$(GN_DOWNLOAD_PROVIDER_SUPPORT)","yes_1_0_4")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := Aurora_DownLoader
LOCAL_CERTIFICATE := media
LOCAL_STATIC_JAVA_LIBRARIES := umeng

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := umeng:libs/umeng-analytics-v5.5.3.jar

include $(BUILD_MULTI_PREBUILT)
#endif
