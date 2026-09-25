#pragma once
#include <semaphore.h>
#include "drivers/usbcmd.hpp"

#define USBCMD_CODE 0x7A

class UsbCmd
{
private:
    usbcmd_t *cmd;
    usbcmd_data_param_t *param;
    sem_t sem;
    static void callback(void *cmd, USBCMD_CALLBACK_ID id, void *param, unsigned int i, int error);
public:
    UsbCmd(unsigned char feature);
    ~UsbCmd();
    size_t read(void *buffer, size_t size, unsigned int timeout);
    size_t write(void *buffer, size_t size, unsigned int timeout);
};
