LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    ../../../ex/carousel/java/com/android/ex/carousel/carousel.rs
    
    
LOCAL_SRC_FILES := $(call all-java-files-under,$(src_dirs)) \
           src/com/privacymanage/service/IPrivacyManageService.aidl \
           src/com/privacymanage/data/AidlAccountData.aidl

LOCAL_JAVA_LIBRARIES := services telephony-common

# Gionee fengjianyi 2013-05-13 modify for CR00800567 start
LOCAL_STATIC_JAVA_LIBRARIES := android-common-carousel \
                               com.mediatek.systemui.ext \
                               CellConnUtil \
                               android-support-v4 \
# Gionee fengjianyi 2013-05-13 modify for CR00800567 end

LOCAL_PACKAGE_NAME := SystemUI
LOCAL_CERTIFICATE := platform
LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
