LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
	src/com/android/music/IMediaPlaybackService.aidl

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 \
			       wxsdk \
			       xlwbsdk \
			       xiamisdk \
			       imgsdk \
			       fastjson	
			       
					
					
LOCAL_PACKAGE_NAME := Aurora_Music

LOCAL_SDK_VERSION := current

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := wxsdk:libs/libammsdk.jar \
					xlwbsdk:libs/weiboSDKCore_3.1.2.jar \
					xiamisdk:libs/boas-sdk-v0.1.1.jar \
					imgsdk:libs/imageloader-0.1.jar	\
					fastjson:libs/fastjson-1.1.40.jar 			

include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
