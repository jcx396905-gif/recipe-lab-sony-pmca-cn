#pragma once
#include <stdlib.h>

enum USBCMD_CALLBACK_ID {};

typedef struct {
    void *buffer;
    size_t size;
    int error;
    int unknown;
    unsigned int timeout;
    void *param;
} usbcmd_data_param_t;

class usbcmd_t
{
public:
    usbcmd_data_param_t *AllocDataParam();
    void Cancel();
    int FreeDataParam(usbcmd_data_param_t *param);
    int Receive(usbcmd_data_param_t *param, void (*)(void *, USBCMD_CALLBACK_ID, void *, unsigned int, int));
    int Send(usbcmd_data_param_t *param, void (*)(void *, USBCMD_CALLBACK_ID, void *, unsigned int, int));
};

extern "C"
{
    usbcmd_t *usbcmd_Create(int code, int feature, void (*)(void *, USBCMD_CALLBACK_ID, void *, unsigned int, int), void *ops, unsigned int, unsigned int, int *);
    int usbcmd_Destroy(usbcmd_t *instance);
}
