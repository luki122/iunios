LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_PACKAGE_NAME := Aurora_Changer
LOCAL_CERTIFICATE := platform


include $(BUILD_PACKAGE)
include $(BUILD_MULTI_PREBUILT)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := universal-image-loader-1.9.3-with-sources:libs/universal-image-loader-1.9.3-with-sources.jar