#pragma once
#include <errno.h>

volatile int *__aeabi_errno_addr();
#undef errno
#define errno (*__aeabi_errno_addr())
