#Gionee:wangth 20120722 modify begin
#ifeq ($(strip $(GN_QC_MTK_APP_CONTACTS_SUPPORT)), yes)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq (OP01,$(word 1,$(subst _, ,$(OPTR_SPEC_SEG_DEF))))
ifeq ($(MTK_VT3G324M_SUPPORT), yes)
LOCAL_MANIFEST_FILE := cmcc/AndroidManifest.xml
endif
endif 

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
#LOCAL_SRC_FILES += $(call all-java-files-under, ../yulore_frame_lib/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../ContactsCommon/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../cardview/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../appcompat/src)


LOCAL_RESOURCE_DIR := res
#LOCAL_RESOURCE_DIR += ../yulore_frame_lib/res
LOCAL_RESOURCE_DIR += ../ContactsCommon/res
LOCAL_RESOURCE_DIR += ../cardview/res
LOCAL_RESOURCE_DIR += ../appcompat/res
#LOCAL_AAPT_FLAGS := --auto-add-overlay --extra-packages com.yulore.superyellowpage.lib
LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages com.android.contacts.common 


LOCAL_STATIC_JAVA_LIBRARIES := \
    com.android.vcard \
    android-common \
    guava \
    palette \
    android-support-v13 \
    android-support-v4 \
    android-ex-variablespeed \
    imageloader \
    yulore_helper \
    libphonenumber \
    libgeocoding \
    zxing
#    com.mediatek.CellConnUtil

LOCAL_JAVA_LIBRARIES += gnframework

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := imageloader:../yulore_frame_lib/libs/universal-image-loader-1.9.3.jar \
                                       palette:./libs/palette/javalib.jar \
                                       libphonenumber:./libs/libphonenumber/javalib.jar\
                                       libgeocoding:./libs/libgeocoding/javalib.jar\
                                       zxing:./libs/zxing/zxing.jar


#Gionee <wangth><2013-05-27> add for CR00819923 begin
#LOCAL_JAVA_LIBRARIES += javax.obex
#Gionee <wangth><2013-05-27> add for CR00819923 end
LOCAL_REQUIRED_MODULES := libvariablespeed

LOCAL_PACKAGE_NAME := Contacts
LOCAL_CERTIFICATE := shared

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

#PRODUCT_COPY_FILES += \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo0.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo0.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo1.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo1.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo2.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo2.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo3.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo3.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo4.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo4.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo5.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo5.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo6.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo6.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo7.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo7.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo8.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo8.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo9.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo9.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo10.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo10.jpg \
#    $(LOCAL_PATH)/images/gn_gallery_for_contact_photo11.jpg:system/etc/GN_Contacts/gn_gallery_for_contact_photo11.jpg 

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
#endif
#Gionee:wangth 20120722 modify end
