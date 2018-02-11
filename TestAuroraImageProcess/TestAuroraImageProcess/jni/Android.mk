LOCAL_PATH:= $(call my-dir)

############################################### SQF ADDED BELOW ###############################################

$(warning $(TARGET_ARCH_ABI))

##############

include $(CLEAR_VARS)
LOCAL_MODULE := libamipengine
LOCAL_MODULE_TAGS := optional
#LOCAL_CFLAGS += -m64
#LOCAL_LDFLAGS += -m64
LOCAL_MULTILIB := 64
LOCAL_SRC_FILES := auroraeffects/lib/libamipengine.a     # or $(so_path)/libthird1.so
#LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)    #or PREBUILT_SHARED_LIBRARY

#####


include $(CLEAR_VARS)
LOCAL_MODULE := libamimageprocess
LOCAL_MODULE_TAGS := optional
#LOCAL_CFLAGS += -m64
#LOCAL_LDFLAGS += -m64
LOCAL_MULTILIB := 64
LOCAL_SRC_FILES := auroraeffects/dependencies/lib/libamimageprocess.a 
include $(PREBUILT_STATIC_LIBRARY)    

#####

include $(CLEAR_VARS)
LOCAL_MODULE := libampostprocess
LOCAL_MODULE_TAGS := optional
#LOCAL_CFLAGS += -m64
#LOCAL_LDFLAGS += -m64
LOCAL_MULTILIB := 64
LOCAL_SRC_FILES := auroraeffects/dependencies/lib/libampostprocess.a 
include $(PREBUILT_STATIC_LIBRARY)    

#####

include $(CLEAR_VARS)
LOCAL_MODULE := libmtpng
LOCAL_MODULE_TAGS := optional
#LOCAL_CFLAGS += -m64
#LOCAL_LDFLAGS += -m64
LOCAL_MULTILIB := 64  
LOCAL_SRC_FILES := auroraeffects/dependencies/lib/libmtpng.a    
include $(PREBUILT_STATIC_LIBRARY)    

#####

include $(CLEAR_VARS)
LOCAL_MODULE := libmtzlib
LOCAL_MODULE_TAGS := optional
#LOCAL_CFLAGS += -m64
#LOCAL_LDFLAGS += -m64
LOCAL_MULTILIB := 64
LOCAL_SRC_FILES := auroraeffects/dependencies/lib/libmtzlib.a    
include $(PREBUILT_STATIC_LIBRARY)

#####

include $(CLEAR_VARS)
LOCAL_MODULE := libmputility
LOCAL_MODULE_TAGS := optional
#LOCAL_CFLAGS += -m64
#LOCAL_LDFLAGS += -m64
LOCAL_MULTILIB := 64
LOCAL_SRC_FILES := auroraeffects/dependencies/lib/libmputility.a 
include $(PREBUILT_STATIC_LIBRARY) 

#####


include $(CLEAR_VARS)
LOCAL_MODULE := libmpkernel
LOCAL_MODULE_TAGS := optional
#LOCAL_CFLAGS += -m64
#LOCAL_LDFLAGS += -m64
LOCAL_MULTILIB := 64
LOCAL_SRC_FILES := auroraeffects/dependencies/lib/libmpkernel.a 
include $(PREBUILT_STATIC_LIBRARY)

#####


include $(CLEAR_VARS)
LOCAL_MODULE := libmpbase
LOCAL_MODULE_TAGS := optional
#LOCAL_CFLAGS += -m64
#LOCAL_LDFLAGS += -m64
LOCAL_MULTILIB := 64
LOCAL_SRC_FILES := auroraeffects/dependencies/lib/libmpbase.a
include $(PREBUILT_STATIC_LIBRARY)


#############





include $(CLEAR_VARS)
LOCAL_MODULE := libAuroraImageProcess
#LOCAL_CFLAGS += -m64
#LOCAL_LDFLAGS += -m64
LOCAL_MULTILIB := 64
LOCAL_MODULE_TAGS := optional

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_LDLIBS += -ljnigraphics  

#LOCAL_LDLIBS += $(LOCAL_PATH)/lib/libamipengine.a \
							$(LOCAL_PATH)/auroraeffects/dependencies/lib/libamimageprocess.a \
							$(LOCAL_PATH)/auroraeffects/dependencies/lib/libampostprocess.a \
							$(LOCAL_PATH)/auroraeffects/dependencies/lib/libmtpng.a \
							$(LOCAL_PATH)/auroraeffects/dependencies/lib/libmtzlib.a \
							$(LOCAL_PATH)/auroraeffects/dependencies/lib/libmputility.a \
							$(LOCAL_PATH)/auroraeffects/dependencies/lib/libmpkernel.a \
							$(LOCAL_PATH)/auroraeffects/dependencies/lib/libmpbase.a

LOCAL_SRC_FILES := auroraeffects/aurora_image_process.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/auroraeffects/inc \
					$(LOCAL_PATH)/auroraeffects/dependencies/inc

LOCAL_STATIC_LIBRARIES := 	libamipengine \
							libamimageprocess \
							libampostprocess \
							libmtpng \
							libmtzlib \
							libmputility \
							libmpkernel \
							libmpbase
							
							

$(warning $(LOCAL_C_INCLUDES))
$(warning $(LOCAL_STATIC_LIBRARIES))

include $(BUILD_SHARED_LIBRARY)


