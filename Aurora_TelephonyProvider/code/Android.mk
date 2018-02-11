ifeq ($(strip $(GN_APP_MMS_SUPPORT)), yes)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := TelephonyProvider
LOCAL_CERTIFICATE := platform

LOCAL_JAVA_LIBRARIES += gnframework

LOCAL_STATIC_JAVA_LIBRARIES += android-common

include $(BUILD_PACKAGE)
endif
