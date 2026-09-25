#pragma once

#define NAND_IOC_NUM_SAFE_PHYS_BLOCKS 1
#define NAND_IOC_NUM_NORMAL_PHYS_BLOCKS 4
#define NAND_IOC_PAGE_SAFE_SIZE 0x600
#define NAND_IOC_PAGE_NORMAL_INFO_SIZE 8

int nand_ioc_get_devinfo(int fd, int *block_size, int *page_size);
int nand_ioc_get_safe_phys_blocks(int fd, int *blocks);
int nand_ioc_get_normal_phys_blocks(int fd, int *blocks);
int nand_ioc_pread_safe(int fd, int block, int page, int n_pages, int n_pages_per_block, void *buffer);
int nand_ioc_pread_normal(int fd, int block, int page, int page_size, int n_pages, void *buffer, void *info);
