#include "../bin/com_android_auroramusic_local_AuroraPlayer.h"

#include <dlfcn.h>
#include "BackportsLog.h"
#define LOG_TAG "AuroraPlayer-common"


// ----------------------------------------------------------------------------
//android::DataSource::getSize(long long*)
#define OFFSET64TAG "_ZN7android10DataSource7getSizeEPx"
//android::DataSource::getSize(long *)
#define OFFSET32TAG "_ZN7android10DataSource7getSizeEPl"
int OffsetTypeSize()
{
    void *stagefright = dlopen("libstagefright.so", RTLD_NOW);
    if(!stagefright)
    {
        LOGE("load libstagefright.so failed");
        return -1;
    }
    int ret = -1;
    if(dlsym(RTLD_DEFAULT, OFFSET64TAG))
        ret = 64;
    else if(dlsym(RTLD_DEFAULT, OFFSET32TAG))
        ret = 32;
    else
        LOGE("cannot find symbol " OFFSET64TAG " or " OFFSET32TAG);
    dlclose(stagefright);
    return ret;
}


jint JNICALL Java_com_android_auroramusic_local_AuroraPlayer_offset_1type
  (JNIEnv *env, jclass jclz)
{
    return OffsetTypeSize();
}
