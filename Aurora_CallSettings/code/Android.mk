LOCAL_PATH:= $(call my-dir)

# Static library with some common classes for the phone apps.
# To use it add this line in your Android.mk
#  LOCAL_STATIC_JAVA_LIBRARIES := com.android.phone.common
include $(CLEAR_VARS)


LOCAL_MODULE := com.android.phone.common
include $(BUILD_STATIC_JAVA_LIBRARY)

# Build the Phone app which includes the emergency dialer. See Contacts
# for the 'other' dialer.
include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES := guava
LOCAL_JAVA_LIBRARIES := telephony-common voip-common ims-common
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_SRC_FILES := $(call all-java-files-under, src_common)
LOCAL_SRC_FILES += \
        src_u2/com/android/phone/EventLogTags.logtags \

LOCAL_SRC_FILES += $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := res

LOCAL_PACKAGE_NAME := CallSettings
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_AAPT_FLAGS += -c xxhdpi
LOCAL_AAPT_FLAGS := --auto-add-overlay

include $(BUILD_PACKAGE)

# Build the test package
include $(call all-makefiles-under,$(LOCAL_PATH))
