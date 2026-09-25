#pragma once

#define MEM_DEV "/dev/mem"

int mem_read(void *buffer, int ptr, size_t len);
int mem_write(int ptr, void *buffer, size_t len);
