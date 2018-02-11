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

LOCAL_SRC_FILES := $(call all-java-files-under,$(src_dirs)) \
     
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
		com.qualcomm.listen.voicewakeup
#and-lbs-3.2.5 \
#commons-codec-1.7 \
#gson-2.2.2 \
#httpmime-4.1.3 \
#locSDK_2.6c \
#sogou-srop \
#sparta \
#yysdk 
		


LOCAL_JAVA_LIBRARIES += mediatek-framework

#Don't use LOCAL_SDK_VERSION.Because cann't call hide APi
#in framework when has it.
#LOCAL_SDK_VERSION := current

# Gionee lingfen 20121010 merge 6589 start
#LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))
#LOCAL_RESOURCE_DIR := $(chips_dir) $(addprefix $(LOCAL_PATH)/, $(res_dirs))
# # Gionee lingfen 20121010 merge 6589 end

LOCAL_PACKAGE_NAME := Aurora_WakeUp

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.ex.chips

LOCAL_EMMA_COVERAGE_FILTER := @$(LOCAL_PATH)/emma_filter_classes,--$(LOCAL_PATH)/emma_filter_method

include $(BUILD_PACKAGE)
# additionally, build unit tests in a separate .apk
include $(call all-makefiles-under,$(LOCAL_PATH))

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := javalib:libs/javalib.jar \

    

