#pragma once

int osal_free_msg(int type, void *addr);
int osal_snd_msg(int type, void *addr);
int osal_snd_sync_msg(int type, void *addr);
int osal_valloc_msg_wait(int type, void **addr, int len, int flag);
