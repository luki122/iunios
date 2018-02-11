#ifeq ("$(GN_DOWNLOAD_PROVIDER_SUPPORT)","yes_1_0_4")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := Aurora_DownLoader
LOCAL_CERTIFICATE := media

include $(BUILD_PACKAGE)
#endif
