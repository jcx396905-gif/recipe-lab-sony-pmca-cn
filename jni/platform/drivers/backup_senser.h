#pragma once

#define BACKUP_SENSER_PRESET_DATA_MAX_SIZE 0x100000

typedef struct {
    int version;// 16 * ord(data[0xE]) + ord(data[0x24])
    size_t size;
    int unknown1;// data[0x34:0x38]
    int unknown2;// data[0x30:0x34]
    int version_check;// preset_data_version_check(data[0x40:])
    char region[0x20];// data[0xC0:0xE0]
} backup_senser_preset_data_status;

typedef struct {
    int version[3];
} backup_senser_version;

int backup_senser_cmd_preset_data_read(int from_memory, void *data, size_t *len);
int backup_senser_cmd_ID1(char set_value, char *get_value);

#ifndef MODE_ANDROID
int backup_senser_cmd_preset_data_status(backup_senser_preset_data_status *status);
int backup_senser_cmd_version(backup_senser_version *version);
#endif
