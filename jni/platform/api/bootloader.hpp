#pragma once
#include <stdexcept>
#include <vector>

#define BOOTLOADER_DEV "/dev/nflasha"

class bootloader_error : public std::runtime_error
{
public:
    bootloader_error(const std::string& msg) : std::runtime_error(msg) {}
};

typedef struct {
    int block;
    size_t num_pages;
    size_t page_size;
    bool is_safe;
} bootloader_block;

std::vector<bootloader_block> bootloader_get_blocks(int fd);
std::vector<char> bootloader_read_pages(int fd, bootloader_block &block, size_t page, size_t n);
std::vector<char> bootloader_read_block(int fd, bootloader_block &block);
