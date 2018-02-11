#ifeq ("$(GN_APP_NUMAREA_SUPPORT)","gionee")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := 12345 



LOCAL_SRC_FILES := $(call all-java-files-under, src) \
	        src/com/gionee/aora/numarea/export/INumAreaManager.aidl \
		src/com/gionee/aora/numarea/export/INumAreaObserver.aidl 

LOCAL_PACKAGE_NAME := NumArea
		
		
		
		
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
                                        

include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
#endif
