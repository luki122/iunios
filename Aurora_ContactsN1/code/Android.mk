#Gionee:wangth 20120722 modify begin
#ifeq ($(strip $(GN_QC_MTK_APP_CONTACTS_SUPPORT)), yes)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../yulore_frame_lib/src)

LOCAL_RESOURCE_DIR := res
LOCAL_RESOURCE_DIR += ../yulore_frame_lib/res
LOCAL_AAPT_FLAGS := --auto-add-overlay --extra-packages com.yulore.superyellowpage.lib 

LOCAL_STATIC_JAVA_LIBRARIES := \
    com.android.vcard \
    android-common \
    guava \
    android-support-v13 \
    android-support-v4 \
    android-ex-variablespeed \
    imageloader \
    yulore_helper
#    com.mediatek.CellConnUtil

#LOCAL_JAVA_LIBRARIES += gnframework

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := imageloader:../yulore_frame_lib/libs/universal-image-loader-1.9.3.jar

#Gionee <wangth><2013-05-27> add for CR00819923 begin
#LOCAL_JAVA_LIBRARIES += javax.obex
#Gionee <wangth><2013-05-27> add for CR00819923 end
LOCAL_REQUIRED_MODULES := libvariablespeed

LOCAL_PACKAGE_NAME := Contacts
LOCAL_CERTIFICATE := shared

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)


# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
#endif
#Gionee:wangth 20120722 modify end
