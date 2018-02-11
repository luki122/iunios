#!/bin/bash

PATH_ROOT="/home/aurora/apps_AlphaOS2.0/5.1/N1"
URL_BUILD_EVN="http://18.8.5.98/apk_build_env"
URL_AURORA_PACKAGE="http://18.8.5.98/aurora2/7503_rom_2.0/trunk/packages/apps/mtk/aurora"
URL_AURORA_APPS="http://18.8.5.98/aurora/apps_rom_2.0"
logFile=${PATH_ROOT}/log.txt

cd ${PATH_ROOT}

if [ ! -d apk_build_env ];then
    svn co ${URL_BUILD_EVN}
fi

if [ ! -d aurora ];then
    svn co ${URL_AURORA_PACKAGE}
fi

./clean_svn_new_file.sh apk_build_env && svn revert -R apk_build_env && svn update apk_build_env

./clean_svn_new_file.sh aurora && svn revert -R aurora && svn update aurora

get_version() {
    while read line
        do
        {
            [ -z "$line" -o "${line:0:1}" == "#" ] && continue
                local key=$(echo `echo $line | cut -d "=" -f 1`)
                local value=$(echo `echo $line | cut -d "=" -f 2`)
                if [ "$key" == "GN_APK_VERSION" ] ; then
                    echo $value
                    return
                 fi
        }
        done < <(cat $1)
}

get_time() {
   time=`date -Iseconds`
   time=${time%+0800}
   time=${time//-/}
   time=${time//:/}
   time=${time//T/}
   time=${time:0:(${#time}-2)}
   echo $time
}

make_svn_tag() {
   svn copy $1 $2 -m "make tags automatically" 
}

compile_app() {
    if [ ! -d ${1} ];then
       echo "update app:$1" 
       app_url="${URL_AURORA_APPS}/$1/"
       src_url=${app_url}trunk/$1
       svn checkout ${src_url}
    else
       echo "update ${1}"
       ./clean_svn_new_file.sh  ${1} && svn revert -R ${1} && svn update ${1}
    fi

    cd apk_build_env
    local prj="N1"
    local version=$(get_version "../$1/$1_$prj.mk")

    if [ ! -f ../$1/$1_$prj.mk ];then
         rm -rf ../${1}
         echo "no U5 config.return"
         cd ..
         return;
    fi

    pwd
    echo ../$1/$1_$prj.mk $version
###    if [ -z $version ] ; then
###        echo "Please set version like GN_APK_VERSION=2.1 in Aurora_xxx_prj.mk"
###        cd ..
###        return
###    fi

    cd ../$1
    echo -ne "\n\n\n">> $logFile
    echo $1 >> $logFile
    hour=`date '+%k'`
    if [ $hour -lt 10 ]; then
        today=`date -d yesterday '+%F'`
    else
        today=`date "+%F"`
    fi
    svn log -v -r {$today}:HEAD >> $logFile
    cd ../apk_build_env

    ###./apk -v $version ../$1/$1_$prj.mk
    #not increment version num
    echo "./apk ../$1/$1_$prj.mk"
    ./apk ../$1/$1_$prj.mk > compilelog
    if [ $? -ne 0 ]; then
        echo ppppppp
        echo -ne "\n\n\n">> $logFile
        echo "compile error ???????????????????????????????????????????" >>$logFile
        echo >>$logFile
        cat compilelog >> $logFile
        mutt -s "$1: compile error" sparky.wang@iuni.com  < compilelog
    fi
    cat compilelog
    rm compilelog
    
    cd ../$1
    cd ..
    time=$(get_time)
    dst_url=${app_url}tags/$version-$prj-T$time
#ls apk_build_env/out/$1/*.apk
    cp -v apk_build_env/out/$1/*.apk $1
    cd $1
#    svn add *.apk
#    svn commit -m "update apk"
    cd ..
#    svn mkdir ${dst_url} -m "make new ${dst_url}"
#    make_svn_tag $src_url $dst_url
}

select_apk() {
#    tags_url="http://18.8.5.98/aurora/apps/$1/tags"
#    apk_list=`svn list $tags_url`
#    apk_list=${apk_list//\//}
#    apk_list=$(echo `for s in $apk_list; do echo $s; done| sort -u`)
#    for apk in $apk_list;
#    do
#         select=$apk
#    done
#    echo $apk_list
#    echo $select
#  svn checkout $tags_url/$select
#    echo $2
#    cp $select/$1/*.apk $2
    cp -v $1/*.apk $2
}

export set PATH=/home/aurora/jdk1.7.0_75/bin:/home/aurora/jdk1.7.0_75/jre/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games

cd ${PATH_ROOT}
cd apk_build_env
cd ..

if [ "$1" == "all" ]; then
    echo "compile all applications";
    apps=`svn list ${URL_AURORA_APPS}`
    apps=${apps//\//}
#    apps=${apps/Aurora_Calendar/}
#    apps=${apps/Aurora_CalendarProvider/}
#    apps=${apps/Aurora_AccountSetting/}
#    apps=${apps/Aurora_AudioProfile/}
#    apps=${apps/Aurora_Contacts/}
#    apps=${apps/Aurora_ContactsProvider/}
#    apps=${apps/Aurora_DeskClock/}
#    apps=${apps/Aurora_Secure/}
#    apps=${apps/Aurora_FileManager/}
#    apps=${apps/Aurora_Gallery2/}
     apps=${apps/Aurora_Gallery2_1.0/}
#    apps=${apps/Aurora_Contacts/}
#    apps=${apps/Aurora_ContactsProvider/}
#    apps=${apps/Aurora_Launcher/}
    apps=${apps/Aurora_LockScreen/}
#    apps=${apps/Aurora_DeskClock/}
#    apps=${apps/Aurora_Phone/}
#   apps=${apps/Aurora_SettingUpdate/}
     apps=${apps/Aurora_Settings/}
     apps=${apps/Aurora_Settings5/}
#    apps=${apps/Aurora_SettingsProvider/}
    apps=${apps/Aurora_SystemUI/}
#    apps=${apps/Aurora_TelephonyProvider/}
#    apps=${apps/Aurora_Changer/}
#    apps=${apps/Aurora_Calculator/}
     apps=${apps/Aurora_Stk/}
     apps=${apps//[^_]Bluetooth/}
#     apps=${apps/Aurora_IUNIStk/}
    apps=${apps/Aurora_Music/}
    apps=${apps/Aurora_Music2/}
    apps=${apps/Aurora_Music3/}
#     apps=${apps/Aurora_VoiceAssistant/}
#     apps=${apps/Aurora_Note/}    
     apps=${apps%SettingsProvider}
#     apps=${apps/Aurora_MediaScanner/}
#    apps=${apps/Aurora_MMITest/}
#      apps=${apps/Aurora_MMITest5/}
#     apps=${apps/Aurora_AutoMMI/}
#      apps=${apps/Aurora_AutoMMI5/}
#     apps=${apps/Aurora_Market/}
#     apps=${apps/Aurora_Reject/}
     apps=${apps/Aurora_PhoneInterfaceManager/}
     apps=${apps/Aurora_APIDemo/}
#     apps=${apps/Aurora_Account/}
     apps=${apps/Aurora_AppUpdate/}
#     apps=${apps/Aurora_Email/}
     apps=${apps/Aurora_SalesTracker/}
#     apps=${apps/Aurora_Weather/}
#     apps=${apps/Aurora_Hook/}
     apps=${apps/Aurora_Memo/}
#     apps=${apps/Aurora_Browser/}
#     apps=${apps/Aurora_Store/}
#     apps=${apps/Aurora_Torch/}
#     apps=${apps/Aurora_WakeUp/}
#    apps=${apps/Aurora_Bluetooth/}
    apps=${apps/Aurora_Community/}
#    apps=${apps/Aurora_PureManager/}
#    apps=${apps/Aurora_DualWechat/}
    apps=${apps/TestAuroraImageProcess/}
    apps=${apps/Aurora_ContactsProviderN1/}


    echo "app log" > $logFile
    echo $apps;
    for app in $apps;
    do
#    echo $app
#        trap "" 1 2 3 24
        compile_app $app
    done;
     release_dir="aurora"
    priv_dir=${release_dir}/priv-app/

    echo $apps
#    priv_apps="Aurora_DownLoadProvider5 Aurora_Keyguard Aurora_Contacts Aurora_ContactsProvider Aurora_IUNIStk Aurora_Numlocation Aurora_Phone Aurora_TelephonyProvider"
    for app in $apps;
    do
        priv=""
        for priv_app in $priv_apps;
        do
# echo $priv_app
            if [ "$app" == "$priv_app" ]; then
                  priv=$app
                  break
            fi
        done
#        echo $priv
        if [ "$priv" == "" ]; then        
            mkdir $release_dir/$app
            select_apk "$app" "$release_dir/$app"
       else
            mkdir $priv_dir/$app
            select_apk "$app" "$priv_dir/$app"
       fi
    done

    
    cd $release_dir
#    svn add *
    svn commit -m "update apps"
   
    exit 0
fi
