TARGET_OS := arm-linux-gnueabi
include $(SRCROOT)/Makefiles/gmake/sysid.mak
OSNAME := linux

_STATIC_BUILD := 1
_NO_SHARED_BUILD := 1
_NO_DBG_BUILD := 1
_NO_STLDBG_BUILD := 1

EXTRA_FLAGS := -march=armv5te -ffunction-sections -fdata-sections -fno-ident
EXTRA_CFLAGS := $(EXTRA_FLAGS)
EXTRA_CXXFLAGS := $(EXTRA_FLAGS)
