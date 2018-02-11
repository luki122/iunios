#!/system/bin/sh
userdata="/system/userdata"
aurora="/data/aurora"
aurora_ex="/data/aurora/exist"

song_dir="/sdcard/Music/auroramusic/song"
lrc_dir="/sdcard/Music/auroramusic/lyric"

systemuserapp="/system/userapp"
userapp="/data/app"
copyed="/data/app/copyed"

copy=0
if [ ! -d ${aurora_ex} ]; then
        copy=1
fi


while true; do

    echo "copy userapp begin"
    if [ ! -f ${copyed} ] ; then
        cp -rf ${systemuserapp}/Aurora_Store.apk ${userapp}
        echo "copy copyed finish"
        echo 1 > ${copyed}
    fi

    chown -R root:root /system/lbe_oem
    chmod 0755  /system/lbe_oem/*

    chmod 666 ${userapp}/Aurora_Store.apk
    echo 1.copy checking time
    tmpyear=`date +%Y`
    echo ${tmpyear}
    if [ ${tmpyear} -eq "1970" ];then
        echo "sleep 1"
        sleep 1
        continue
    fi   
   
    sdcard_ex="/sdcard/dirtest"
    if [ ! -d ${sdcard_ex} ]; then
        echo "mkdir fail.continue"
        mkdir -p ${sdcard_ex}
        sleep 1
	continue
    fi
    rm -rf ${sdcard_ex}

    echo 2.copy checking dir
    if [ -d ${aurora} ]; then
         
        if [ $copy -eq 0 ]; then
             echo "copy Done."
             break;
        fi
    fi

    mkdir ${aurora}
    mkdir -p ${song_dir}
    mkdir -p ${lrc_dir}

    cp -Rf ${userdata}/data /
    cp /system/sdcard/*.mp4 /sdcard/

    mkdir /sdcard/DCIM/
    chmod 777 /sdcard/DCIM/
    cp /system/sdcard/DCIM/* /sdcard/DCIM/

    #copy mp3 & lrc
    cp system/sdcard/music/*.mp3 ${song_dir}
    cp system/sdcard/lyric/*.lrc ${lrc_dir}
 

    sync
    mkdir -p ${aurora_ex}
    copy=0

done

chmod 777 /data/aurora
chmod 777 /data/aurora/change
chmod 777 /data/aurora/change/lockscreen

echo 1 > /sys/fs/selinux/enforce


