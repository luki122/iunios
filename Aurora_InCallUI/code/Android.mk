LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

contacts_common_dir := ../ContactsCommon
phone_common_dir := ../PhoneCommon
contacts_ext_dir := ../ContactsCommon/ext
# M: add mtk-ex
chips_dir := ../chips

src_dirs := src \
    $(contacts_common_dir)/src \
    $(phone_common_dir)/src \
    $(contacts_ext_dir)/src

res_dirs := res \
    res_ext \
    $(contacts_common_dir)/res \
    $(contacts_common_dir)/res_ext \
    $(phone_common_dir)/res \
    $(chips_dir)/res

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES := $(call all-java-files-under, ext/src)
LOCAL_SRC_FILES := $(call all-java-files-under, common/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../ContactsCommon/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../PhoneCommon/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../ContactsCommon/ext/src)
LOCAL_RESOURCE_DIR := res
LOCAL_RESOURCE_DIR += res_ext
LOCAL_RESOURCE_DIR += ../ContactsCommon/res
LOCAL_RESOURCE_DIR += ../ContactsCommon/res_ext
LOCAL_RESOURCE_DIR += ../PhoneCommon/res
LOCAL_RESOURCE_DIR += ../chips/res

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.contacts.common

#LOCAL_AAPT_FLAGS += --extra-packages com.android.phone.common

# M: add mtk-ex
#LOCAL_AAPT_FLAGS += --extra-packages com.android.mtkex.chips

LOCAL_JAVA_LIBRARIES := telephony-common mediatek-framework voip-common 

LOCAL_STATIC_JAVA_LIBRARIES := com.android.vcard android-common \
    guava \
    android-support-v13 \
    android-support-v4 \
    android-ex-variablespeed \
    libphonenumber \
    libgeocoding \
    hmtsdk \
    yulore_helper

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libphonenumber:../ContactsCommon/libs/libphonenumber-6.2.jar \
    libgeocoding:../ContactsCommon/libs/geocoder-2.9.jar

# M: add mtk-ex
LOCAL_STATIC_JAVA_LIBRARIES += android-common-chips

LOCAL_REQUIRED_MODULES := libvariablespeed

LOCAL_PACKAGE_NAME := InCallUI
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

# Uncomment the following line to build against the current SDK
# This is required for building an unbundled app.
# LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)
