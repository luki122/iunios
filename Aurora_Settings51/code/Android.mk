LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(strip $(MTK_CLEARMOTION_SUPPORT)),no)
# if not support clearmotion, load a small video for clearmotion
LOCAL_ASSET_DIR:= $(LOCAL_PATH)/no_clearmotion_assets
endif

LOCAL_JAVA_LIBRARIES := bouncycastle \
                        conscrypt \
                        telephony-common \
                        ims-common \
                        mediatek-framework 

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 \
                               android-support-v13 \
                               jsr305 \
                               com.mediatek.lbs.em2.utils \
                               com.mediatek.settings.ext \
                               com.mediatek.keyguard.ext \

LOCAL_SHARED_LIBRARIES :=libMiraVision_jni

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        src/com/android/settings/EventLogTags.logtags

LOCAL_RESOURCE_DIR := res
LOCAL_RESOURCE_DIR += res_ext

LOCAL_RESOURCE_DIR += navigationbar/res
LOCAL_AAPT_FLAGS += --auto-add-overlay --extra-packages com.android.setupwizard.navigationbar
LOCAL_STATIC_JAVA_LIBRARIES += setup-wizard-navbar                               

LOCAL_PACKAGE_NAME := Aurora_Settings
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

#include navigationbar/common.mk

include $(BUILD_PACKAGE)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := svg:libs/androidsvg-1.2.1.jar

# Use the following include to make our test apk.
ifeq (,$(ONE_SHOT_MAKEFILE))
include $(call all-makefiles-under,$(LOCAL_PATH))
endif

