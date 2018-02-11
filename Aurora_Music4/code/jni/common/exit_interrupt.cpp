#include "mediadef.h"

int __player_quit = 0;

void exit_interrupt_reset()
{
    __player_quit = 0;
}

int exit_interrupt_check()
{
    return __player_quit;
}

int exit_interrupt_set()
{
    int oldVal = __player_quit;
    __player_quit = 1;
    return oldVal;
}
