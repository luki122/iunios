#ifndef LISTENER_H
#define LISTENER_H

#include <utils/RefBase.h>

class ListenerInterface : public android::RefBase
{
public:
    virtual void sendEvent(int msg, int ext1=0, int ext2=0) = 0;
};

#endif // LISTENER_H
