# Copyright (C) 2013 The CyanogenMod Project
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# version 3 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.
#		locSDK_5.2:libs/locSDK_5.2.jar \

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES :=  $(call all-java-files-under, src)$(call all-renderscript-files-under, src)
LOCAL_PACKAGE_NAME := Aurora_VoiceAssistant
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := \
		android-common \
		android-common-chips \
		android-support-v4 \
        calendar-common


LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := and-lbs-3.2.5:libs/and-lbs-3.2.5.jar\
		commons-codec-1.7:libs/commons-codec-1.7.jar \
		gson-2.2.2:libs/gson-2.2.2.jar \
		httpmime-4.1.3:libs/httpmime-4.1.3.jar \
		locSDK_6.03:libs/locSDK_6.03.jar \	
		sogou-srop:libs/sogou-srop.jar \
		sparta:libs/sparta.jar \
		yysdk:libs/yysdk.jar \
		SogouTTS_3.3e:libs/SogouTTS_3.3e.jar \
		HciCloud_SDK_Java_3.1:libs/HciCloud_SDK_Java_3.1.jar \
		HCI_TTS_PLAYER_VISUALCONTROL_3.1:libs/HCI_TTS_PLAYER_VISUALCONTROL_3.1.jar \
		javalib:libs/core.jar 


LOCAL_JNI_SHARED_LIBRARIES := libspeex_sogou_v42\
				 libencrypt_sogou_v00\
				 liblocSDK6\
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
				libsnd\
				libdict\
				libttsoff
				
include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))














