#include "osal_uipc.h"

// Dummy implementation, only used for linking
int osal_free_msg(int type, void *addr) {return -1;}
int osal_snd_msg(int type, void *addr) {return -1;}
int osal_snd_sync_msg(int type, void *addr) {return -1;}
int osal_valloc_msg_wait(int type, void **addr, int len, int flag) {return -1;}
