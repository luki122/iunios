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
    src_url=${app_url}res/lady/$1
    svn ls ${src_url}
#echo $?
    if [ $? -ne 0 ]
    then
        echo "haha"
       return 
    else
        echo "hehe"
        svn checkout ${src_url}
    fi

#svn checkout ${src_url}
    cd apk_build_env
    local prj="U3"
    local version=$(get_version "../$1/$1_$prj.mk")
#    echo $version
    if [ -z $version ] ; then
        echo "Please set version like GN_APK_VERSION=2.1 in Aurora_xxx_prj.mk"
        exit 1
    fi

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
#mutt -s "$1: compile error" jode.peng@iuni.com  < compilelog
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
cd /home/aurora/apps/U3/lady
cd apk_build_env
svn update

cd ..

rm -rf Aurora_*

if [ "$1" == "all" ]; then
    echo "compile all applications";
    apps=`svn list http://18.8.5.98/aurora/apps`
    apps=${apps//\//}
     apps=${apps/Aurora_Gallery2_1.0/}
    apps=${apps/Aurora_LockScreen/}
     apps=${apps/Aurora_Stk/}
     apps=${apps//[^_]Bluetooth/}
    apps=${apps/Aurora_Music/}
    apps=${apps/Aurora_Music2/}
    apps=${apps/Aurora_Music3/}
     apps=${apps%SettingsProvider}
     apps=${apps/Aurora_PhoneInterfaceManager/}
     apps=${apps/Aurora_APIDemo/}
     apps=${apps/Aurora_AppUpdate/}
     apps=${apps/Aurora_SalesTracker/}
     apps=${apps/Aurora_Memo/}
#     apps=${apps/Aurora_Browser/}
     apps=${apps/Aurora_Store/}
#    apps=${apps/Aurora_Bluetooth/}


    echo "app log" > $logFile
    echo $apps;
    for app in $apps;
    do
#    echo $app
        trap "" 1 2 3 24
        compile_app $app
    done;
    release_dir_4="/home/aurora/trunk/alps/alps/aurora_data/system/app/U3m/overlay/lady"
 
    echo $apps
    priv_apps=""
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
            select_apk "$app" "$release_dir_4"
        else
            select_apk "$app" "$release_dir_4"
        fi
    done

    cd $release_dir_4
    svn add *
    svn commit -m "update apps"

    exit 0
fi

compile_app $1

release_dir="/home/aurora/trunk/alps/alps/aurora_data/system/app/U3m/overlay/lady" 
select_apk "$1" "$release_dir"

cd $release_dir
svn commit -m "update apps"
