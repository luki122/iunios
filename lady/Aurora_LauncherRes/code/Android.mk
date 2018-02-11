LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_MODULE_TAGS := optional eng

LOCAL_STATIC_JAVA_LIBRARIES := android-common android-support-v13

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-renderscript-files-under, src)

LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := Aurora_Launcher_Res

LOCAL_OVERRIDES_PACKAGES := Home

LOCAL_CERTIFICATE := shared

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))





