#include <fcntl.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>
#include "mem.h"

int mem_read(void *buffer, int ptr, size_t len)
{
    int errno;

    int fd = open(MEM_DEV, O_RDONLY, 0);
    if (fd == -1) return -1;

    void *mapped = mmap(NULL, len, PROT_READ, MAP_SHARED, fd, ptr);
    if (mapped == MAP_FAILED) return -1;

    memcpy(buffer, mapped, len);

    errno = munmap(mapped, len);
    if (errno) return errno;

    errno = close(fd);
    if (errno) return errno;

    return 0;
}

int mem_write(int ptr, void *buffer, size_t len)
{
    int errno;

    int fd = open(MEM_DEV, O_RDWR, 0);
    if (fd == -1) return -1;

    void *mapped = mmap(NULL, len, PROT_WRITE, MAP_SHARED, fd, ptr);
    if (mapped == MAP_FAILED) return -1;

    memcpy(mapped, buffer, len);

    errno = munmap(mapped, len);
    if (errno) return errno;

    errno = close(fd);
    if (errno) return errno;

    return 0;
}
