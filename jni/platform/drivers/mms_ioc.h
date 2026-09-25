#pragma once

#define MMS_IOC_NUM_BOOTLOADER_BLOCKS 1
#define MMS_IOC_PAGE_INFO_SIZE 0x10

#define MMS_IOC_PAGE 0
#define MMS_IOC_INFO 1

extern const int mms_ioc_bootloader_blocks[MMS_IOC_NUM_BOOTLOADER_BLOCKS];

int mms_ioc_getblocksize(int fd, int *block_size);
int mms_ioc_getpagesize(int fd, int *page_size);
int mms_ioc_pread(int fd, int page, int info_or_page, int size, void *buffer);
