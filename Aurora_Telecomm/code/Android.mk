LOCAL_PATH:= $(call my-dir)

# Build the Telecom service.
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := telephony-common
# Add for MMI, include the account widget framework
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_STATIC_JAVA_LIBRARIES := \
        guava \
        hmtsdk \
        yulore_helper

res_dirs := res \
            res_ext \
            $(phone_common_dir)/res

LOCAL_RESOURCE_DIR := res
LOCAL_RESOURCE_DIR += res_ext
LOCAL_RESOURCE_DIR += ../PhoneCommon/res

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.phone.common

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES := $(call all-java-files-under, /ext/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../PhoneCommon/src)
                        
LOCAL_PACKAGE_NAME := Telecom

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

# Workaround for "local variable type mismatch" error.
LOCAL_DX_FLAGS += --no-locals

include $(BUILD_PACKAGE)

# Build Plug in jar 
include $(LOCAL_PATH)/ext/Android.mk
