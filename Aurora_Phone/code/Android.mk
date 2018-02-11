LOCAL_PATH:= $(call my-dir)

# Static library with some common classes for the phone apps.
# To use it add this line in your Android.mk
#  LOCAL_STATIC_JAVA_LIBRARIES := com.android.phone.common
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	src/com/android/phone/CallLogAsync.java \
	src/com/android/phone/HapticFeedback.java

LOCAL_MODULE := com.android.phone.common
include $(BUILD_STATIC_JAVA_LIBRARY)

# Build the Phone app which includes the emergency dialer. See Contacts
# for the 'other' dialer.
include $(CLEAR_VARS)
# Aurora xuyong 2015-08-29 modified for bug #15926 start
LOCAL_STATIC_JAVA_LIBRARIES := hmtsdk yulore_helper guava android-support-v4
# Aurora xuyong 2015-08-29 modified for bug #15926 end
LOCAL_JAVA_LIBRARIES := telephony-common voip-common ims-common
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
        src_u2/com/android/phone/EventLogTags.logtags \
      #   src/com/android/phone/IPhoneRecorder.aidl\
      #   src/com/android/phone/IPhoneRecordStateListener.aidl
      #  src/com/android/phone/INetworkQueryService.aidl \
     #   src/com/android/phone/INetworkQueryServiceCallback.aidl
LOCAL_RESOURCE_DIR := res

LOCAL_PACKAGE_NAME := Phone
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_AAPT_FLAGS += -c xxhdpi
LOCAL_AAPT_FLAGS := --auto-add-overlay

include $(BUILD_PACKAGE)

# Build the test package
include $(call all-makefiles-under,$(LOCAL_PATH))
