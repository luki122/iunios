LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,$(src_dirs))

# bundled
#LOCAL_STATIC_JAVA_LIBRARIES += \
#		android-common \
#		android-common-chips \
#		calendar-common

# unbundled
LOCAL_STATIC_JAVA_LIBRARIES := \
		android-common \
		android-common-chips \
		android-support-v4 \
        android-support-v13 \
	jackson

LOCAL_JAVA_LIBRARIES += mediatek-framework

#Don't use LOCAL_SDK_VERSION.Because cann't call hide APi
#in framework when has it.
#LOCAL_SDK_VERSION := current

# Gionee lingfen 20121010 merge 6589 start
#LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))
#LOCAL_RESOURCE_DIR := $(chips_dir) $(addprefix $(LOCAL_PATH)/, $(res_dirs))
# # Gionee lingfen 20121010 merge 6589 end

LOCAL_PACKAGE_NAME := Aurora_Market

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.ex.chips

LOCAL_EMMA_COVERAGE_FILTER := @$(LOCAL_PATH)/emma_filter_classes,--$(LOCAL_PATH)/emma_filter_method

include $(BUILD_PACKAGE)
# additionally, build unit tests in a separate .apk
include $(call all-makefiles-under,$(LOCAL_PATH))
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := jackson:libs/jackson-all-1.8.10.jar
