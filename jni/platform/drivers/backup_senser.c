#include <stdarg.h>
#include <string.h>
#include "backup_senser.h"
#include "mem.h"
#include "osal_uipc.h"

#define OSAL_MSG_BACKUP_SENSER 0x3E0166

#define MODE_READ 0
#define MODE_WRITE 1

#define BACKUP_SENSER_TYPE_ERROR 0
#define BACKUP_SENSER_TYPE_START 1
#define BACKUP_SENSER_TYPE_WRITE_PTR 2
#define BACKUP_SENSER_TYPE_DO_WRITE 4
#define BACKUP_SENSER_TYPE_DONE 8
#define BACKUP_SENSER_TYPE_FREE 16
#define BACKUP_SENSER_TYPE_FREE_DONE 32

struct backup_senser_msg {
    char type;
    char errno;
    int data_len;
    int res_ptr;
    size_t res_len;
    union {
        struct {
            int write_ptr;
            size_t write_len;
            char data[0x128];
        };
        struct {
            char read_data[0x130];
        };
    };
};

static int backup_senser_send_msg(struct backup_senser_msg *data)
{
    int errno;

    void *msg;
    errno = osal_valloc_msg_wait(OSAL_MSG_BACKUP_SENSER, &msg, sizeof(struct backup_senser_msg), 1);
    if (errno) return errno;

    memcpy(msg, data, sizeof(struct backup_senser_msg));

    errno = osal_snd_sync_msg(OSAL_MSG_BACKUP_SENSER, msg);
    if (errno) return errno;

    memcpy(data, msg, sizeof(struct backup_senser_msg));

    errno = osal_free_msg(OSAL_MSG_BACKUP_SENSER, msg);
    if (errno) return errno;

    return 0;
}

static int backup_senser_sync_msg(int function, int mode, void *data, size_t *data_len, int arg_count, ...)
{
    int errno;

    struct backup_senser_msg msg;
    memset(&msg, 0, sizeof(struct backup_senser_msg));

    msg.type = BACKUP_SENSER_TYPE_START;

    int f = (function << 16) | 0x603;
    memcpy(msg.data, &f, sizeof(int));
    va_list ap;
    va_start(ap, arg_count);
    for (int i = 0; i < arg_count; i++)
        ((int *) msg.data)[i+1] = va_arg(ap, int);
    va_end(ap);
    int write_data_off = (1 + arg_count) * sizeof(int);

    if (mode == MODE_WRITE) {
        if (write_data_off + *data_len < sizeof(msg.data))
            memcpy(msg.data + write_data_off, data, *data_len);
        msg.data_len = write_data_off + *data_len;
    }

    errno = backup_senser_send_msg(&msg);
    if (errno) return errno;

    if (mode == MODE_READ && msg.type == BACKUP_SENSER_TYPE_DONE && msg.res_len <= *data_len) {
        *data_len = msg.res_len;
        if (msg.res_ptr) {
            if (data) {
                errno = mem_read(data, msg.res_ptr, msg.res_len);
                if (errno) return errno;
            }

            msg.type = BACKUP_SENSER_TYPE_FREE;
            errno = backup_senser_send_msg(&msg);
            if (errno) return errno;
            if (msg.type != BACKUP_SENSER_TYPE_FREE_DONE) return msg.errno;
        } else {
            if (data)
                memcpy(data, msg.read_data, msg.res_len);
        }
    } else if (mode == MODE_WRITE && msg.type == BACKUP_SENSER_TYPE_DONE) {
        // nothing to do
    } else if (mode == MODE_WRITE && msg.type == BACKUP_SENSER_TYPE_WRITE_PTR && msg.res_len == *data_len) {
        errno = mem_write(msg.res_ptr, data, msg.res_len);
        if (errno) return errno;

        msg.type = BACKUP_SENSER_TYPE_DO_WRITE;
        msg.write_ptr = msg.res_ptr;
        msg.write_len = msg.res_len;
        errno = backup_senser_send_msg(&msg);
        if (errno) return errno;
        if (msg.type != BACKUP_SENSER_TYPE_DONE) return msg.errno;
    } else if (msg.type == BACKUP_SENSER_TYPE_ERROR) {
        return msg.errno;
    } else {
        return -1;
    }

    return 0;
}

int backup_senser_cmd_preset_data_read(int from_memory, void *data, size_t *len)
{
    return backup_senser_sync_msg(5, MODE_READ, data, len, 1, from_memory);
}

int backup_senser_cmd_ID1(char set_value, char *get_value)
{
    size_t len = sizeof(char);
    return backup_senser_sync_msg(15, MODE_READ, get_value, &len, 1, set_value);
}

#ifndef MODE_ANDROID
// These functions do not work if /version.txt does not exist. version_file_read calls
// backupfile_get_file_datasize which sets task_struct->stack to zero (why???), which has weird
// effects afterwards. Since android runs in a chrooted environment, these two functions are
// disabled in android mode.

int backup_senser_cmd_preset_data_status(backup_senser_preset_data_status *status)
{
    size_t len = sizeof(backup_senser_preset_data_status);
    return backup_senser_sync_msg(6, MODE_READ, status, &len, 0);
}

int backup_senser_cmd_version(backup_senser_version *version)
{
    size_t len = sizeof(backup_senser_version);
    return backup_senser_sync_msg(16, MODE_READ, version, &len, 0);
}
#endif
