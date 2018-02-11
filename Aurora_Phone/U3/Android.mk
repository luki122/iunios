LOCAL_PATH:= $(call my-dir)

# Build the Phone app which includes the emergency dialer. See Contacts
# for the 'other' dialer.
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := telephony-common voip-common telephony-msim
LOCAL_STATIC_JAVA_LIBRARIES := com.android.phone.shared hmtsdk yulore_helper
LOCAL_SRC_FILES := $(call all-java-files-under, src_common)
LOCAL_SRC_FILES += \
        src_u3/com/android/phone/EventLogTags.logtags \
     #    src_u3/com/android/phone/INetworkQueryService.aidl \
     #    src_u3/com/android/phone/INetworkQueryServiceCallback.aidl \
      #   src_u3/org/codeaurora/ims/IImsService.aidl \
      #   src_u3/org/codeaurora/ims/IImsServiceListener.aidl \
      #   src_u3/com/android/phone/IPhoneRecorder.aidl\
     #    src_u3/com/android/phone/IPhoneRecordStateListener.aidl
LOCAL_SRC_FILES += $(call all-java-files-under, src_u3)
LOCAL_SRC_FILES += $(call all-java-files-under, src_common_before_5)
LOCAL_RESOURCE_DIR := res
LOCAL_RESOURCE_DIR += res-v19

LOCAL_PACKAGE_NAME := Phone
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_AAPT_FLAGS += -c xxxhdpi -c xxhdpi

include $(BUILD_PACKAGE)

# Build the test package
include $(call all-makefiles-under,$(LOCAL_PATH))
