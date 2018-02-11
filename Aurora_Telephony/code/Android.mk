LOCAL_PATH:= $(call my-dir)

# Build the Phone app which includes the emergency dialer. See Contacts
# for the 'other' dialer.
include $(CLEAR_VARS)


src_dirs := ../PhoneCommon/src sip/src ext/src common/src src
res_dirs := res res_ext ../PhoneCommon/res sip/res

LOCAL_JAVA_LIBRARIES := telephony-common voip-common ims-common

# Add for Plug-in, include the plug-in framework
LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_STATIC_JAVA_LIBRARIES := guava

#LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, common/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ext/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../PhoneCommon/src)
LOCAL_SRC_FILES += $(call all-java-files-under, sip/src)
LOCAL_SRC_FILES += \
        src/com/android/phone/EventLogTags.logtags 
    #    src/com/android/phone/INetworkQueryService.aidl \
     #   src/com/android/phone/INetworkQueryServiceCallback.aidl
#LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))
LOCAL_RESOURCE_DIR := res
LOCAL_RESOURCE_DIR += res_ext
LOCAL_RESOURCE_DIR += ../PhoneCommon/res
LOCAL_RESOURCE_DIR += sip/res

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.phone.common 
    #--extra-packages com.android.services.telephony.sip


LOCAL_PACKAGE_NAME := TeleService

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags sip/proguard.flags

include $(BUILD_PACKAGE)

