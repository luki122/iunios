LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

#LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SRC_FILES := $(call all-java-files-under,$(src_dirs)) \

LOCAL_STATIC_JAVA_LIBRARIES := \
		android-support-v4 \
		android-support-v13 \
		libarity

LOCAL_PACKAGE_NAME := Aurora_ThemeManager

include $(BUILD_PACKAGE)
##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
            libarity:libs/ksoap2-android-2.6.5-jar.jar \
            universal-image-loader-1.9.3-with-sources:libs/universal-image-loader-1.9.3-with-sources.jar \
            jackson-all-1.8.10:libs/jackson-all-1.8.10.jar \
            fastjson1:libs/fastjson-1.1.42-sources.jar \
            fastjson2:libs/fastjson-1.1.42.jar
                                                                             

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
