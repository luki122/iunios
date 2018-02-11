#ifndef _VSEALISTENER_H_
#define _VSEALISTENER_H_

struct ListenerInterface
{
    virtual void sendEvent(int msg, int ext1=0, int ext2=0) = 0;
    virtual ~ListenerInterface() {}
};


#endif
