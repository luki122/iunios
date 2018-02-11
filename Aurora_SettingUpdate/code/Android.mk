ifeq ("$(GN_SETTING_OTA_UPDATE_SUPPORT)","yes")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_STATIC_JAVA_LIBRARIES := com.android.vcard
LOCAL_STATIC_JAVA_LIBRARIES += YouJuAgent

LOCAL_PACKAGE_NAME := Aurora_SettingUpdate

include $(BUILD_PACKAGE)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
YouJuAgent:libs/YouJuAgent.jar \
YouJuAgent:libs/jsoup-1.7.3.jar
#YouJuAgent:libs/Android_Location_V1.1.0.jar \
#YouJuAgent:libs/jsoup-1.7.3.jar


# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
