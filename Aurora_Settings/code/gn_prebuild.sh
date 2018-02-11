#!/bin/bash

#CURRENT_DIR IS gn_prebuild.sh local dir
CURENT_DIR=$(dirname $0)

pushd $CURENT_DIR >/dev/null

#if [ "$GN_APK_PLATFORM_VENDOR" == "mtk" ];then
#    GN_VENDOR_SRC=vendor/mtk
#elif [ "$GN_APK_PLATFORM_VENDOR" == "qcom" ];then
#    GN_VENDOR_SRC=vendor/qcom
#else
#    GN_VENDOR_SRC=""
#fi

#if [ ! -z "$GN_VENDOR_SRC" ];then
#    rsync -a $GN_VENDOR_SRC/ .
#fi

if [ ! -z "$GN_APK_PLATFORM_VENDOR" ];then
    rm -rf src/com/android/settings/bluetooth
fi

popd >/dev/null
