#include <sys/ioctl.h>
#include "mms_ioc.h"

const int mms_ioc_bootloader_blocks[MMS_IOC_NUM_BOOTLOADER_BLOCKS] = {0};

int mms_ioc_getblocksize(int fd, int *block_size)
{
    return ioctl(fd, 0x80046D01, block_size);
}

int mms_ioc_getpagesize(int fd, int *page_size)
{
    return ioctl(fd, 0x80046D02, page_size);
}

struct mms_ioc_page {
    int page;
    int info_or_page;
    int page_size;
    void *page_buffer;
    int info_size;
    void *info_buffer;
    int errno;
};
int mms_ioc_pread(int fd, int page, int info_or_page, int size, void *buffer)
{
    struct mms_ioc_page args = {page, info_or_page, size, buffer, size, buffer, -1};
    int errno = ioctl(fd, 0xC01C6D81, &args);
    if (errno) return errno;
    return args.errno;
}
