#include <string>

#include "bootloader.hpp"

extern "C"
{
    #include "drivers/mms_ioc.h"
    #include "drivers/nand_ioc.h"
}

#if !defined DRIVER_mms_ioc && !defined DRIVER_nand_ioc
    #error No bootloader driver available
#endif

using namespace std;

vector<bootloader_block> bootloader_get_blocks(int fd)
{
    vector<bootloader_block> blocks;
    size_t block_size, page_size;

#if defined DRIVER_mms_ioc
    if (mms_ioc_getblocksize(fd, (int *) &block_size))
        throw bootloader_error("mms_ioc_getblocksize failed");
    if (mms_ioc_getpagesize(fd, (int *) &page_size))
        throw bootloader_error("mms_ioc_getpagesize failed");
#elif defined DRIVER_nand_ioc
    if (nand_ioc_get_devinfo(fd, (int *) &block_size, (int *) &page_size))
        throw bootloader_error("nand_ioc_get_devinfo failed");
#endif

    size_t n_pages_per_block = block_size / page_size;

#if defined DRIVER_mms_ioc
    blocks.reserve(MMS_IOC_NUM_BOOTLOADER_BLOCKS);

    for (int i = 0; i < MMS_IOC_NUM_BOOTLOADER_BLOCKS; i++) {
        bootloader_block b = {mms_ioc_bootloader_blocks[i], n_pages_per_block, page_size, false};
        blocks.push_back(b);
    }
#elif defined DRIVER_nand_ioc
    blocks.reserve(NAND_IOC_NUM_SAFE_PHYS_BLOCKS + NAND_IOC_NUM_NORMAL_PHYS_BLOCKS);

    int safe_blocks[NAND_IOC_NUM_SAFE_PHYS_BLOCKS];
    if (nand_ioc_get_safe_phys_blocks(fd, safe_blocks))
        throw bootloader_error("nand_ioc_get_safe_phys_blocks failed");
    for (int i = 0; i < NAND_IOC_NUM_SAFE_PHYS_BLOCKS; i++) {
        bootloader_block b = {safe_blocks[i], n_pages_per_block, NAND_IOC_PAGE_SAFE_SIZE, true};
        blocks.push_back(b);
    }

    int normal_blocks[NAND_IOC_NUM_NORMAL_PHYS_BLOCKS];
    if (nand_ioc_get_normal_phys_blocks(fd, normal_blocks))
        throw bootloader_error("nand_ioc_get_normal_phys_blocks failed");
    for (int i = 0; i < NAND_IOC_NUM_NORMAL_PHYS_BLOCKS; i++) {
        bootloader_block b = {normal_blocks[i], n_pages_per_block, page_size, false};
        blocks.push_back(b);
    }
#endif

    return blocks;
}

vector<char> bootloader_read_pages(int fd, bootloader_block &block, size_t page, size_t n)
{
    vector<char> buffer(n * block.page_size);

#if defined DRIVER_mms_ioc
    size_t first_page = block.block * block.num_pages + page;
    for (size_t i = 0; i < n; i++) {
        if (mms_ioc_pread(fd, first_page + i, MMS_IOC_PAGE, block.page_size, &buffer[i * block.page_size]))
            throw bootloader_error("mms_ioc_pread failed");
    }
#elif defined DRIVER_nand_ioc
    if (block.is_safe) {
        if (nand_ioc_pread_safe(fd, block.block, page, n, block.num_pages, &buffer[0]))
            throw bootloader_error("nand_ioc_pread_safe failed");
    } else {
        char info[n * NAND_IOC_PAGE_NORMAL_INFO_SIZE];
        if (nand_ioc_pread_normal(fd, block.block, page, block.page_size, n, &buffer[0], info))
            throw bootloader_error("nand_ioc_pread_normal failed");
    }
#endif

    return buffer;
}

vector<char> bootloader_read_block(int fd, bootloader_block &block)
{
    return bootloader_read_pages(fd, block, 0, block.num_pages);
}
