LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_PACKAGE_NAME := Aurora_Changer
LOCAL_CERTIFICATE := platform


include $(BUILD_PACKAGE)
include $(BUILD_MULTI_PREBUILT)

