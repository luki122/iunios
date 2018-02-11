#ifeq ("$(GN_AUTO_MMI_SUPPORT)", "yes")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
                    src/com/caf/fmradio/IFMRadioService.aidl\
                    src/com/caf/fmradio/IFMRadioServiceCallbacks.aidl\

LOCAL_JAVA_LIBRARIES := qcnvitems qcrilhook telephony-common
LOCAL_PACKAGE_NAME := AutoMMI
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)
#endif
