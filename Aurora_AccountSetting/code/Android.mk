LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_STATIC_JAVA_LIBRARIES := \
		android-common \
		android-common-chips \
		android-support-v4 \
        android-support-v13
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_PACKAGE_NAME := Aurora_AccountSetting
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.ex.chips

LOCAL_EMMA_COVERAGE_FILTER := @$(LOCAL_PATH)/emma_filter_classes,--$(LOCAL_PATH)/emma_filter_method

include $(BUILD_PACKAGE)
