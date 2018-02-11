LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# Include res dir from chips
# Gionee lingfen 20121010 merge 6589 start
#chips_dir := ../../../frameworks/ex/chips/res
#res_dirs := $(chips_dir) res
chips_dir := $(PWD)/frameworks/ex/chips/res
res_dirs := res
src_dirs := src
# Gionee lingfen 20121010 merge 6589 end


LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,$(src_dirs))

# unbundled
LOCAL_STATIC_JAVA_LIBRARIES := \
		android-common \
		android-common-chips \
		android-support-v4 \
                android-support-v13 \

LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_RESOURCE_DIR := res
LOCAL_RESOURCE_DIR += navigationbar/res
LOCAL_AAPT_FLAGS += --auto-add-overlay --extra-packages com.android.setupwizard.navigationbar
LOCAL_STATIC_JAVA_LIBRARIES += setup-wizard-navbar

LOCAL_PACKAGE_NAME := Aurora_SetupWizard
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

#include navigationbar/common.mk

include $(BUILD_PACKAGE)
# additionally, build unit tests in a separate .apk
include $(call all-makefiles-under,$(LOCAL_PATH))

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := jackson:libs/jackson-all-1.8.10.jar \

