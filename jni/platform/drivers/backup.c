#include <stdarg.h>
#include <string.h>
#include "backup.h"
#include "osal_uipc.h"

#define OSAL_MSG_BACKUP 0x3E014D

struct backup_msg {
    int function;
    int result;
    int arg_count;
    int type;
    int padding[2];
    int args[10];
};

static int backup_sync_msg(int function, int arg_count, ...)
{
    int errno;

    struct backup_msg *msg;
    errno = osal_valloc_msg_wait(OSAL_MSG_BACKUP, (void **) &msg, sizeof(struct backup_msg), 1);
    if (errno) return -1;

    memset(msg, 0, sizeof(struct backup_msg));
    msg->function = function;
    msg->arg_count = arg_count;
    msg->type = OSAL_MSG_BACKUP;

    va_list ap;
    va_start(ap, arg_count);
    for (int i = 0; i < arg_count; i++)
        msg->args[i] = va_arg(ap, int);
    va_end(ap);

    errno = osal_snd_sync_msg(OSAL_MSG_BACKUP, msg);
    if (errno) return -1;

    int result = msg->result;

    errno = osal_free_msg(OSAL_MSG_BACKUP, msg);
    if (errno) return -1;

    return result;
}

int Backup_get_datasize(int id)
{
    return backup_sync_msg(0, 1, id);
}

int Backup_get_attribute(int id)
{
    return backup_sync_msg(2, 1, id);
}

int Backup_read(int id, void *addr)
{
    return backup_sync_msg(3, 2, id, addr);
}

int Backup_write(int subsystem_id, int id, void *addr)
{
    return backup_sync_msg(8, 3, subsystem_id, id, addr);
}

void Backup_sync_all()
{
    backup_sync_msg(15, 0);
}

int Backup_protect(int mode, void *overwrite_data, int size)
{
    return backup_sync_msg(25, 3, mode, overwrite_data, size);
}
