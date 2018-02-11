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

LOCAL_STATIC_JAVA_LIBRARIES := \
    com.android.vcard \
    android-common \
    guava \
    android-support-v13 \
    android-support-v4 \
    android-ex-variablespeed \
    hmtsdk \
#    com.mediatek.CellConnUtil

LOCAL_JAVA_LIBRARIES += gnframework

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
