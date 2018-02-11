ifneq ($(findstring IUNI,$(GN_PROJECT)),)
LOCAL_PATH := $(call my-dir)

define all_files_under_subdir
$(foreach files, $(notdir $(wildcard $1)),$(dir $(1))$(files):$(2)/$(files)) 
endef

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := Aurora_Settings
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)


PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lib/arm64/*.so,system/lib64)

PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/desktop/*.jpg,system/iuni/aurora/change/desktop)

PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lockscreen/Building/*.png,system/iuni/aurora/change/lockscreen/Building) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lockscreen/Minimalism/*.png,system/iuni/aurora/change/lockscreen/Minimalism) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lockscreen/Dream/*.png,system/iuni/aurora/change/lockscreen/Dream) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lockscreen/Timely/*.png,system/iuni/aurora/change/lockscreen/Timely)

PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lockscreen/*.xml,system/iuni/aurora/change/lockscreen) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lockscreen/Building/*.xml,system/iuni/aurora/change/lockscreen/Building) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lockscreen/Minimalism/*.xml,system/iuni/aurora/change/lockscreen/Minimalism) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lockscreen/Dream/*.xml,system/iuni/aurora/change/lockscreen/Dream) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lockscreen/Timely/*.xml,system/iuni/aurora/change/lockscreen/Timely)


PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/audio/alarms/*.ogg ,system/media/audio/alarms) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/audio/notifications/*.ogg,system/media/audio/notifications) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/audio/ringtones/*.ogg,system/media/audio/ringtones) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/audio/ui/*.ogg,system/media/audio/ui)


PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/media/bootanimation.zip,system/media) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/media/bootaudio.mp3,system/media)


PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/pic/*.png,system/sdcard/DCIM)


PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/iuni/IUNIOS.png,system/iuni)

PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/mov/*.mp3,system/sdcard/music) \
        $(call all_files_under_subdir,$(LOCAL_PATH)/lrc/*.lrc,system/sdcard/lyric)

PRODUCT_COPY_FILES += \
        $(call all_files_under_subdir,$(LOCAL_PATH)/mov/IUNI_OS_Introduction.mp4,system/sdcard)

PRODUCT_COPY_FILES += \
       $(call all_files_under_subdir,$(LOCAL_PATH)/etc/wakeup_apk_list,system/etc)

PRODUCT_COPY_FILES += \
       $(call all_files_under_subdir,$(LOCAL_PATH)/iuni/com.android.gallery3d/maskfile/*.data,system/iuni/aurora/gallery/maskfile)

PRODUCT_COPY_FILES += \
       $(call all_files_under_subdir,$(LOCAL_PATH)/bin/aurora-data.sh,system/bin)

PRODUCT_COPY_FILES += \
       $(call all_files_under_subdir,$(LOCAL_PATH)/config/default_workspace.xml,system/iuni/aurora/launcher/config)


PRODUCT_COPY_FILES += \
       $(call all_files_under_subdir,$(LOCAL_PATH)/../Aurora_Store/Aurora_Store.apk,system/userapp)


include $(BUILD_PREBUILT)
endif
