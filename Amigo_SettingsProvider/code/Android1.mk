#ifeq ($(strip $(GN_APK_AmigoSettingsProvider_SUPPORT)),yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_JAVA_LIBRARIES := telephony-common amigoframework

LOCAL_PACKAGE_NAME := Amigo_SettingsProvider
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

########################
include $(call all-makefiles-under,$(LOCAL_PATH))

#endif
