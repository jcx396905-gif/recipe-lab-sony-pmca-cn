#include <stdexcept>
#include <string>
#include "usbcmd.hpp"

using namespace std;

UsbCmd::UsbCmd(unsigned char feature)
{
    cmd = usbcmd_Create(USBCMD_CODE, feature, NULL, NULL, 0, 0, NULL);
    if (!cmd)
        throw runtime_error("usbcmd_Create failed");

    param = cmd->AllocDataParam();
    if (!param) {
        usbcmd_Destroy(cmd);
        throw runtime_error("usbcmd_t::AllocDataParam failed");
    }
    param->param = &sem;

    sem_init(&sem, 0, 0);
}

UsbCmd::~UsbCmd()
{
    cmd->FreeDataParam(param);
    usbcmd_Destroy(cmd);
    sem_destroy(&sem);
}

void UsbCmd::callback(void *cmd, USBCMD_CALLBACK_ID id, void *param, unsigned int i, int error)
{
    sem_post((sem_t *) ((usbcmd_data_param_t *) param)->param);
}

size_t UsbCmd::read(void *buffer, size_t size, unsigned int timeout)
{
    param->buffer = buffer;
    param->size = size;
    param->timeout = timeout;

    int error = cmd->Receive(param, UsbCmd::callback);
    if (error)
        return error;

    sem_wait(&sem);
    return param->size;
}

size_t UsbCmd::write(void *buffer, size_t size, unsigned int timeout)
{
    param->buffer = buffer;
    param->size = size;
    param->timeout = timeout;

    int error = cmd->Send(param, UsbCmd::callback);
    if (error)
        return error;

    sem_wait(&sem);
    return param->size;
}
