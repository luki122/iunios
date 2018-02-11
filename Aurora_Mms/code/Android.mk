# Copyright 2007-2008 The Android Open Source Project
ifneq ($(strip $(GN_APP_MMS_SUPPORT)), yes)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
#gionee gaoj 2012-3-22 added for CR00555790 start
#gionee zhouyj 2013-01-17 add for CR00765580 start
LOCAL_SRC_FILES += \
        src/com/gionee/aora/numarea/export/INumAreaManager.aidl \
        src/com/gionee/aora/numarea/export/INumAreaObserver.aidl \
        src/gn/com/android/statistics/aidl/IStatisticsInterface.aidl
#gionee zhouyj 2013-01-17 add for CR00765580 end
#gionee zhouyj 2012-11-03 modify for CR00724293 start
ifneq ($(strip $(GN_CELL_CONN_PLATFORM_4_1_SUPPORT)), yes)
LOCAL_STATIC_JAVA_LIBRARIES := \
    com.android.phone.common \
    com.android.vcard \
    android-common \
    guava \
    android-support-v13 \
    android-support-v4 \
    android-ex-variablespeed \
    com.android.phone.common \
    com.mediatek.CellConnUtil \
    baidumapapi \
    hmtsdk \
    yulore_helper
else
LOCAL_STATIC_JAVA_LIBRARIES := \
    com.android.phone.common \
    com.android.vcard \
    android-common \
    guava \
    android-support-v13 \
    android-support-v4 \
    android-ex-variablespeed \
    com.android.phone.common \
    CellConnUtil \
    baidumapapi \
    hmtsdk \
    yulore_helper
endif
#gionee zhouyj 2012-11-03 modify for CR00724293 end

#gionee gaoj 2012-3-22 added for CR00555790 end
LOCAL_PACKAGE_NAME := Mms
LOCAL_JAVA_LIBRARIES += mediatek-framework
# Builds against the public SDK
#LOCAL_SDK_VERSION := current

#gionee zhouyj 2012-11-03 modify for CR00724293 start
ifneq ($(strip $(GN_CELL_CONN_PLATFORM_4_1_SUPPORT)), yes)
LOCAL_STATIC_JAVA_LIBRARIES += android-common jsr305 com.mediatek.CellConnUtil wappush com.android.vcard
else
LOCAL_STATIC_JAVA_LIBRARIES += android-common CellConnUtil wappush com.android.vcard
endif
#gionee zhouyj 2012-11-03 modify for CR00724293 end

LOCAL_REQUIRED_MODULES := SoundRecorder

LOCAL_PROGUARD_FLAG_FILES := proguard.flags


include $(BUILD_PACKAGE)

#gionee zhouyj 2012-08-16 add for CR00678252 start
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := baidumapapi:./baidu/baidumapapi.jar

PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/baidu/libBMapApiEngine_v1_3_3.so:system/lib/libBMapApiEngine_v1_3_3.so

include $(BUILD_MULTI_PREBUILT)
#gionee zhouyj 2012-08-16 add for CR00678252 end

# This finds and builds the test apk as well, so a single make does both.
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
