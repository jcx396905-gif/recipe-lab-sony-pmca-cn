# Apis are high level code
APIDIR = api

# Drivers are low level code
DRIVERDIR = drivers

# Libs are drivers which are already present as .so files on the device
LIBDIR = libs


# Functions available in all modes:
APIS_GEN_ = properties tweaks util
DRIVERS_GEN_ = mem
DRIVERS_GEN2 = backup_senser
DRIVERS_GEN3 = backup_senser
LIBS_GEN2 = osal_uipc
LIBS_GEN3 = osal_uipc

# Functions available in MAIN mode:
APIS_MAIN_GEN_ = usbcmd
APIS_MAIN_GEN2 = backup bootloader
APIS_MAIN_GEN3 = backup bootloader
DRIVERS_MAIN_GEN2 = mms_ioc
DRIVERS_MAIN_GEN3 = nand_ioc
LIBS_MAIN_GEN_ = usbcmd
LIBS_MAIN_GEN2 = backup
LIBS_MAIN_GEN3 = backup
LFLAGS_MAIN_GEN3 = $(STDLIBS:%=-l%)

# Functions available in UPDATER mode:
APIS_UPDATER_GEN_ = usbcmd
APIS_UPDATER_GEN1 = bootloader
APIS_UPDATER_GEN2 = backup bootloader
APIS_UPDATER_GEN3 = backup bootloader
DRIVERS_UPDATER_GEN1 = mms_ioc
DRIVERS_UPDATER_GEN2 = mms_ioc
DRIVERS_UPDATER_GEN3 = nand_ioc
LIBS_UPDATER_GEN_ = usbcmd
LIBS_UPDATER_GEN1 = $(filter-out sonystdc++,$(STDLIBS:%=sony%)) sonysupc++ stlport_static
LIBS_UPDATER_GEN2 = $(STDLIBS:%=sony%) backup
LIBS_UPDATER_GEN3 = backup
FLAGS_UPDATER_GEN1 = -isystem$(PLATFORMDIR)/stlport/stlport
LFLAGS_UPDATER_GEN1 = -nostdlib
LFLAGS_UPDATER_GEN2 = -nostdlib
LFLAGS_UPDATER_GEN3 = -nostdlib $(STDLIBS:%=-l%)

# Functions available in ANDROID mode:
APIS_ANDROID_GEN_ = backup shell
DRIVERS_ANDROID_GEN_ = backup backup_senser
LIBS_ANDROID_GEN_ = osal_uipc


# Collect available functions:
APIS = $(sort $(APIS_GEN_) $(APIS_GEN$(GEN)) $(APIS_$(MODE)_GEN_) $(APIS_$(MODE)_GEN$(GEN)))
DRIVERS = $(sort $(DRIVERS_GEN_) $(DRIVERS_GEN$(GEN)) $(DRIVERS_$(MODE)_GEN_) $(DRIVERS_$(MODE)_GEN$(GEN)))
LIBS = $(sort $(LIBS_GEN_) $(LIBS_GEN$(GEN)) $(LIBS_$(MODE)_GEN_) $(LIBS_$(MODE)_GEN$(GEN)))

# Sony standard libraries
STDLIBS = c dl gcc_s m pthread rt stdc++

# All source files in the main executable
SOURCES = $(APIS:%=$(PLATFORMDIR)/$(APIDIR)/%) $(DRIVERS:%=$(PLATFORMDIR)/$(DRIVERDIR)/%) $(CSOURCES) $(CPPSOURCES) $(PLATFORMDIR)/gccstuff


# GCC:
# Get it here: https://releases.linaro.org/components/toolchain/binaries/4.9-2017.01/arm-linux-gnueabi/
CC = arm-linux-gnueabi-gcc
CXX = arm-linux-gnueabi-g++

# Compiler flags
DEFS = -DMODE_$(MODE) -DGEN$(GEN) $(APIS:%=-DAPI_%) $(DRIVERS:%=-DDRIVER_%) $(subst +,p,$(LIBS:%=-DDRIVER_%))
WFLAGS = -Wall -Werror -Wundef -pedantic
FLGS = $(FLAGS_$(MODE)_GEN$(GEN))
LFLAGS = -s $(LFLAGS_$(MODE)_GEN$(GEN))
