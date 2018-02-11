# Copyright (C) 2010 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
JNIPATH := $(call my-dir)
LOCAL_PATH := $(JNIPATH)

include $(call all-subdir-makefiles)

#LOCAL_PATH := $(JNIPATH)

include $(CLEAR_VARS)
LOCAL_MODULE := audiocodec
LOCAL_SRC_FILES := $(JNIPATH)/ffmpeg/android/arm/libaudiocodec.so
include $(PREBUILT_SHARED_LIBRARY) 

include $(CLEAR_VARS)
LOCAL_MODULE := audioutil
LOCAL_SRC_FILES := $(NDKROOT)/sources/cxx-stl/gnu-libstdc++/4.9/libs/armeabi/libgnustl_shared.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := auroraplayer
NDK :=$(NDKROOT) 
LOCAL_C_INCLUDES :=  \
                $(NDK)/sources/cxx-stl/gnu-libstdc++/4.9/include \
                $(NDK)/sources/cxx-stl/gnu-libstdc++/4.9/libs/armeabi/include \
				$(JNIPATH)/ffmpeg \
#				  $(JNIPATH)/../bin \

LOCAL_SRC_FILES := \
                audiotrack/DlAudioTrack.cpp \
                Audio.cpp \
                auroraplayer.cpp \
                EventQueue.cpp \
                auroraplayer_jni.cpp \
                libav/AVDecoder.cpp \
				libav/avhelp.cpp \
				common/exit_interrupt.cpp \
				common/load_help.cpp \
				common/rwlock.c \
				common/jnihelp.cpp \
				common/wrap.cpp \

                
# 注意， 如果不clean之后构建，以下语句不会执行，因此， 在auroraplayer_jni.cpp
# 中打印的该值将是旧的信息
# 正式发布的版本应该在clean之后构建（在auroraplayer_jni.cpp也可以)
BUILD_DATE := $(shell date +'%Y%m%d')

LOCAL_CFLAGS +=-Werror=return-type -Wno-multichar 
LOCAL_CFLAGS +=	-D__STDC_CONSTANT_MACROS=1 \
				-D__STDINT_LIMITS=1 \
				-D__BUILD_DATE=\"$(BUILD_DATE)\" 
				-fvisibility=hidden \
				-Werror=return-type -Wno-psabi \
				
LOCAL_CPPFLAGS +=-std=gnu++11

LOCAL_SHARED_LIBRARIES +=\
		audiocodec \
#		audioutil \
		
LOCAL_LDLIBS +=-llog 
LOCAL_LDLIBS +=-lOpenSLES
include $(BUILD_SHARED_LIBRARY)
