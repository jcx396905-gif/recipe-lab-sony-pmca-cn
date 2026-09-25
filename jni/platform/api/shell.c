#include <string.h>
#include "shell.h"
#include "drivers/osal_uipc.h"

#define OSAL_MSG_BOOTIN 0x94020A

int shell_exec_async(const char *cmd)
{
    int errno;

    if (strlen(cmd) > 255)
        return -1;

    char *msg;
    errno = osal_valloc_msg_wait(OSAL_MSG_BOOTIN, (void **) &msg, sizeof(int) + strlen(cmd) + 1, 1);
    if (errno) return errno;

    ((int *) msg)[0] = 0x940005;
    strcpy(msg + sizeof(int), cmd);

    errno = osal_snd_msg(OSAL_MSG_BOOTIN, msg);
    if (errno) return errno;

    // Do not free async messages
    return 0;
}
