#!/bin/bash
logFile=~/apps/log.txt

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
    echo "compile app:$1" 
    app_url="http://18.8.5.98/aurora/apps/$1/"
    src_url=${app_url}trunk/$1
    svn checkout ${src_url}
    cd apk_build_env
    local prj="U3"
    local version=$(get_version "../$1/$1_$prj.mk")
#    echo $version
###    if [ -z $version ] ; then
###        echo "Please set version like GN_APK_VERSION=2.1 in Aurora_xxx_prj.mk"
###        exit 1
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
    ./apk ../$1/$1_$prj.mk > compilelog

    if [ $? -ne 0 ]; then
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
    cp apk_build_env/out/$1/*.apk $1
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

. /etc/profile
#svn checkout http://18.8.5.98/apk_build_env
cd apps/U3
cd apk_build_env
svn update

cd ..

rm -rf Aurora_*

if [ "$1" == "all" ]; then
    echo "compile all applications";
    apps=`svn list http://18.8.5.98/aurora/apps`
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
#   apps=${apps/Aurora_Settings/}
     apps=${apps/Aurora_Settings5/}
     apps=${apps/Aurora_Settings51/}
#    apps=${apps/Aurora_SettingsProvider/}
#    apps=${apps/Aurora_SystemUI/}
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
      apps=${apps/Aurora_MMITest5/}
#     apps=${apps/Aurora_AutoMMI/}
      apps=${apps/Aurora_AutoMMI5/}
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
    apps=${apps/Aurora_Bluetooth5/}
#    apps=${apps/Aurora_Community/}
    apps=${apps/Aurora_DownLoadProvider5/}
    apps=${apps/Aurora_MediaScanner5/}
    apps=${apps/TestAuroraImageProcess/}
    apps=${apps/TestAuroraImageProcess/}
    apps=${apps/Aurora_SetupWizard/}

    echo "app log" > $logFile
    echo $apps;
    for app in $apps;
    do
#    echo $app
        trap "" 1 2 3 24
        compile_app $app
    done;
    release_dir="/home/aurora/trunk/alps/alps/aurora_data/system/app/U3/" 
    priv_dir=${release_dir}/priv-app/
    release_dir_2="/home/aurora/trunk/alps/alps/aurora_data/system/app/U3m/" 
    priv_dir_2=${release_dir_2}/priv-app/
    release_dir_3="/home/aurora/trunk/alps/alps/aurora_data/system/app/U2Sea/" 
    priv_dir_3=${release_dir_3}/priv-app/
 
    echo $apps
    priv_apps="Aurora_DownLoadProvider Aurora_Keyguard Aurora_Contacts Aurora_ContactsProvider Aurora_IUNIStk Aurora_Numlocation Aurora_Phone Aurora_TelephonyProvider"
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
            select_apk "$app" "$release_dir"
            select_apk "$app" "$release_dir_2"
            select_apk "$app" "$release_dir_3"
        else
            select_apk "$app" "$priv_dir"
            select_apk "$app" "$priv_dir_2"
            select_apk "$app" "$priv_dir_3"
        fi
    done

    rm /home/aurora/trunk/alps/alps/aurora_data/system/app/U2Sea/Aurora_Store.apk
    rm /home/aurora/trunk/alps/alps/aurora_data/system/app/U3/Aurora_Store.apk
    mv /home/aurora/trunk/alps/alps/aurora_data/system/app/U3m/Aurora_Store.apk /home/aurora/trunk/alps/alps/aurora_data/app/U3m/Aurora_Store.apk
    mv /home/aurora/trunk/alps/alps/aurora_data/system/app/U3m/Aurora_Community.apk /home/aurora/trunk/alps/alps/aurora_data/app/U3m/Aurora_Community.apk
    cd /home/aurora/trunk/alps/alps/aurora_data/app/U3m/
    svn add Aurora_Store.apk
    svn add Aurora_Community.apk
    svn commit -m "update store apps"

    cd $release_dir
    svn add *
    svn commit -m "update apps"

    cd $release_dir_2
    svn add *
    svn commit -m "update apps"
    rm /home/aurora/trunk/alps/alps/aurora_data/system/app/U2Sea/Aurora_WakeUp.apk
    rm /home/aurora/trunk/alps/alps/aurora_data/system/app/U2Sea/Bluetooth.apk
    cd $release_dir_3
    svn add *
    svn commit -m "update apps"


    exit 0
fi

compile_app $1

release_dir="/home/aurora/trunk/alps/alps/aurora_data/system/app/U3/" 
select_apk "$1" "$release_dir"

cd $release_dir
svn commit -m "update apps"
