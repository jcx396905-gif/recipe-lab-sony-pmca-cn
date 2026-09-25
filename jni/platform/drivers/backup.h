#pragma once

#define BACKUP_ERROR_INVALID_ARGUMENT 1
#define BACKUP_ERROR_READ_ONLY 3
#define BACKUP_ERROR_INVALID_SUBSYSTEM 4
#define BACKUP_ERROR_WRONG_SIZE 5

#define BACKUP_PRESET_DATA_OFFSET_VERSION 0x0c
#define BACKUP_PRESET_DATA_OFFSET_REGION 0xc0

#define BACKUP_ATTR_READ_ONLY 1

int Backup_get_datasize(int id);
int Backup_get_attribute(int id);
int Backup_read(int id, void *addr);
int Backup_write(int subsystem_id, int id, void *addr);
void Backup_sync_all();
int Backup_protect(int mode, void *overwrite_data, int size);
