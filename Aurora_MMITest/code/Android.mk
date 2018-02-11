#ifeq ("$(GN_MMI_TEST_SUPPORT)","yes")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_CERTIFICATE := platform

LOCAL_SRC_FILES := $(call all-java-files-under, src)\
                    src/com/caf/fmradio/IFMRadioService.aidl\
                    src/com/caf/fmradio/IFMRadioServiceCallbacks.aidl\

LOCAL_JAVA_LIBRARIES := qcnvitems qcrilhook telephony-common

LOCAL_PACKAGE_NAME := Aurora_MMITest
LOCAL_PROGUARD_ENABLED := disabled
include $(BUILD_PACKAGE)
#endif
