#include <string.h>
#include <sys/ioctl.h>
#include "nand_ioc.h"

struct nand_ioc_devinfo {
    int errno;
    int unknown1[1];
    int n_pages_per_block;
    int page_size;
    int unknown2[4];
    int block_size;// = n_pages_per_block * page_size
    int unknown3[1];
};
int nand_ioc_get_devinfo(int fd, int *block_size, int *page_size)
{
    struct nand_ioc_devinfo args;
    int errno = ioctl(fd, 0x6E09, &args);
    if(errno) return errno;
    *block_size = args.block_size;
    *page_size = args.page_size;
    return args.errno;
}

struct nand_ioc_safe_phys_blocks {
    int errno;
    int blocks[NAND_IOC_NUM_SAFE_PHYS_BLOCKS];
};
int nand_ioc_get_safe_phys_blocks(int fd, int *blocks)
{
    struct nand_ioc_safe_phys_blocks args;
    int errno = ioctl(fd, 0x6E0B, &args);
    if (errno) return errno;
    memcpy(blocks, args.blocks, sizeof(args.blocks));
    return args.errno;
}

struct nand_ioc_normal_phys_blocks {
    int errno;
    int blocks[NAND_IOC_NUM_NORMAL_PHYS_BLOCKS];
};
int nand_ioc_get_normal_phys_blocks(int fd, int *blocks)
{
    struct nand_ioc_normal_phys_blocks args;
    int errno = ioctl(fd, 0x6E0C, &args);
    if (errno) return errno;
    memcpy(blocks, args.blocks, sizeof(args.blocks));
    return args.errno;
}

struct nand_ioc_page_safe {
    int errno;
    int block;
    int page;
    int n_pages;
    int n_pages_per_block_log2;
    void *buffer;// len: n_pages * NAND_IOC_PAGE_SAFE_SIZE
};
int nand_ioc_pread_safe(int fd, int block, int page, int n_pages, int n_pages_per_block, void *buffer)
{
    int n_pages_per_block_log2 = 0;
    while (n_pages_per_block >>= 1)
        n_pages_per_block_log2++;
    struct nand_ioc_page_safe args = {-1, block, page, n_pages, n_pages_per_block_log2, buffer};
    int errno = ioctl(fd, 0x6E02, &args);
    if (errno) return errno;
    return args.errno;
}

struct nand_ioc_page_normal {
    int errno;
    int die;
    int plane;
    int block;
    int page;
    int page_size;
    int n_pages;
    void *page_buffer;// len: n_pages * page_size
    void *info_buffer;// len: n_pages * NAND_IOC_PAGE_NORMAL_INFO_SIZE
};
int nand_ioc_pread_normal(int fd, int block, int page, int page_size, int n_pages, void *buffer, void *info)
{
    struct nand_ioc_page_normal args = {-1, 0, 0, block, page, page_size, n_pages, buffer, info};
    int errno = ioctl(fd, 0x6E05, &args);
    if (errno) return errno;
    return args.errno;
}
