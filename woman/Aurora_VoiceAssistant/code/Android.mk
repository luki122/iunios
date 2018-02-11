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
                android-support-v13 
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

LOCAL_PACKAGE_NAME := Aurora_VoiceAssistant

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.ex.chips

LOCAL_EMMA_COVERAGE_FILTER := @$(LOCAL_PATH)/emma_filter_classes,--$(LOCAL_PATH)/emma_filter_method

include $(BUILD_PACKAGE)
# additionally, build unit tests in a separate .apk
include $(call all-makefiles-under,$(LOCAL_PATH))

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := and-lbs-3.2.5:libs/and-lbs-3.2.5.jar\
		commons-codec-1.7:libs/commons-codec-1.7.jar \
		gson-2.2.2:libs/gson-2.2.2.jar \
		httpmime-4.1.3:libs/httpmime-4.1.3.jar \
		locSDK_2.6c:libs/locSDK_2.6c.jar \
		sogou-srop:libs/sogou-srop.jar \
		sparta:libs/sparta.jar \
		yysdk:libs/yysdk.jar \
		sogou_tts_v4.0:libs/sogou_tts_v4.0.jar \
		HciCloud_SDK_Java_3.1:libs/HciCloud_SDK_Java_3.1.jar \
		HCI_TTS_PLAYER_VISUALCONTROL_3.1:libs/HCI_TTS_PLAYER_VISUALCONTROL_3.1.jar \
		javalib:libs/core.jar \

LOCAL_JNI_SHARED_LIBRARIES := libspeex_sogou_v42\
				 libencrypt_sogou_v00\
				 liblocSDK_2.4\
				 libhci_tts_local_synth\
				 libhci_tts_local_n6_synth\
				 libhci_tts_jni\
				 libhci_tts\
				 libhci_sys_jni\
				 libhci_sys\
				 libcurl\
				libjtspeex\
				libjtopus\
				libXiaoKun.n6.voclib\
				libLetter_XiaoKun.n6.voclib\
				libENG_Cameal.n6.voclib\
				libsgregtp\
				libsgsegtp\
				libsgsex\
				libsgtegtp\
				libsgtex\
				libsguex\
				libttsoff\
				libgn			
    

