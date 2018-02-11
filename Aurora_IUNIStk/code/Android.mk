# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := telephony-common telephony-msim
LOCAL_SRC_FILES := $(call all-java-files-under, src_before_5)
LOCAL_SRC_FILES += $(call all-java-files-under, src_u3)

LOCAL_PACKAGE_NAME := IUNI_Stk
LOCAL_CERTIFICATE := platform
#LOCAL_AAPT_FLAGS += -c xxhdpi

include $(BUILD_PACKAGE)
