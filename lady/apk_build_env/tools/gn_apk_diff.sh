#!/bin/bash
#
#       DESCRIPTION:    generate apk diff 
#
#       SCRIPT NAME:    gn_apk_diff.sh
#
#       Usage:   gn_apk_diff.sh 
#                
#
#       Input:  stdin
#                       1. alps url
#                       2. apps url
#
#       Output: 
#
#       AUTHOR:         Ling Fen
#
#       EMAIL:          lingfen@gionee.com
#
#       DATE:           2012-11-03
#
#       HISTORY:
#       REVISOR         DATE                    MODIFICATION
#       LingFen         2012-11-03              create

usage(){
printf "
Download the specified code from svn server

Usage : $0 

options:
    [--help] Show help message

example: 
"
}
get_opts(){
    opts=$(getopt -o h --long help -- "$@")     
    if [ $? -ne 0 ];then
        usage 
        exit 1
    fi

    eval set -- "$opts"
    while true 
    do
        case "$1" in 
            -h|--help)
                usage
                exit 0
                ;;
            --)
                shift
                break
                ;;
            *)
                usage
                exit 1
                ;;
        esac
    done
    if [ ${#@} -ne 3 ];then
        echo "argv error! " 
        usage
        exit 1
    fi
    GN_APK_CONFIG=$1
    GN_APK_OLD_ZIP=$2
    GN_APK_NEW_ZIP=$3

    if [ ! -e "$GN_APK_OLD_ZIP" -o ! -e "$GN_APK_NEW_ZIP" ];then
        echo "zip file not exist" 
        exit 2
    fi
    GN_APK_OLD_NAME=${GN_APK_OLD_ZIP%.*}
    GN_APK_OLD_VERSION=${GN_APK_OLD_NAME##*_}
    GN_APK_OLD_PKG_AND_CFG=${GN_APK_OLD_NAME%_*}
    GN_APK_OLD_PKG_NAME=${GN_APK_OLD_PKG_AND_CFG%_*}

    GN_APK_NEW_NAME=${GN_APK_NEW_ZIP%.*}
    GN_APK_NEW_VERSION=${GN_APK_NEW_NAME##*_}
    GN_APK_NEW_PKG_AND_CFG=${GN_APK_NEW_NAME%_*}
    GN_APK_NEW_PKG_NAME=${GN_APK_NEW_PKG_AND_CFG%_*}

    if [ "$GN_APK_OLD_PKG_NAME" != "$GN_APK_NEW_PKG_NAME" ];then
        echo "Warnning: the apk name are not equal"
    fi

}

dump_variable()
{
    echo ============================
    echo    GN_APK_OLD_ZIP          =$GN_APK_OLD_ZIP
    echo    GN_APK_OLD_NAME         =$GN_APK_OLD_NAME
    echo    GN_APK_OLD_VERSION      =$GN_APK_OLD_VERSION
    echo    GN_APK_OLD_PKG_AND_CFG  =$GN_APK_OLD_PKG_AND_CFG
    echo    GN_APK_OLD_PKG_NAME     =$GN_APK_OLD_PKG_NAME
    echo    GN_APK_NEW_ZIP          =$GN_APK_NEW_ZIP
    echo    GN_APK_NEW_NAME         =$GN_APK_NEW_NAME
    echo    GN_APK_NEW_VERSION      =$GN_APK_NEW_VERSION
    echo    GN_APK_NEW_PKG_AND_CFG  =$GN_APK_NEW_PKG_AND_CFG
    echo    GN_APK_NEW_PKG_NAME     =$GN_APK_NEW_PKG_NAME
    echo ============================
}


create_apk_diff_zip(){
    local old_version_apk
    local new_version_apk
    rm -rf $OUT_DIFF_ROOT
    mkdir -p $OUT_DIFF_ROOT/old
    mkdir -p $OUT_DIFF_ROOT/new
    unzip $GN_APK_OLD_ZIP -d  $OUT_DIFF_ROOT/old
    unzip $GN_APK_NEW_ZIP -d  $OUT_DIFF_ROOT/new
    old_version_apk=$(find $OUT_DIFF_ROOT/old/ -name "*.apk" 2>/dev/null)
    new_version_apk=$(find $OUT_DIFF_ROOT/new/ -name "*.apk" 2>/dev/null)
    if [ -z "$old_version_apk" -o -z "$new_version_apk" ];then
        echo "not apk file inside zip package"
        exit 4
    fi
    `dirname $0`/bsdiff  $old_version_apk $new_version_apk \
        $OUT_DIFF_ROOT/${GN_APK_NEW_PKG_AND_CFG}_${GN_APK_NEW_VERSION}_${GN_APK_OLD_VERSION}.p
    pushd  $OUT_DIFF_ROOT/
    md5sum  ${GN_APK_NEW_PKG_AND_CFG}_${GN_APK_NEW_VERSION}_${GN_APK_OLD_VERSION}.p > md5
    >README
    while read line
    do
        if [ ${line:0:5} == "GN_RM" ];then
            echo $line >> README
        fi
    done <$GN_CUR_DIR/$GN_APK_CONFIG
    popd

    find  $OUT_DIFF_ROOT -maxdepth 1 -path .svn -prune -o -type f -print  |\
        zip -jy $OUT_ROOT/${GN_APK_NEW_PKG_AND_CFG}_${GN_APK_NEW_VERSION}_${GN_APK_OLD_VERSION}.zip -@
}

GN_CUR_DIR=`pwd`
OUT_ROOT=$GN_CUR_DIR/out
OUT_DIFF_ROOT=$OUT_ROOT/diff
GN_APK_OLD_ZIP=""
GN_APK_NEW_ZIP=""


main(){
    get_opts "$@" 
    dump_variable
    create_apk_diff_zip
}

main "$@"
