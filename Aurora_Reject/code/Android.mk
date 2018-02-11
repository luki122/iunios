# Copyright 2007-2008 The Android Open Source Project
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := Aurora_Reject
LOCAL_STATIC_JAVA_LIBRARIES := \
    android-common \
    android-support-v4 \
    hmtsdk \
    yulore_helper

include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))
