#include "usbcmd.hpp"

// Dummy implementation, only used for linking
usbcmd_t *usbcmd_Create(int, int, void (*)(void *, USBCMD_CALLBACK_ID, void *, unsigned int, int), void *, unsigned int, unsigned int, int *) {return NULL;}
int usbcmd_Destroy(usbcmd_t *) {return -1;}
usbcmd_data_param_t *usbcmd_t::AllocDataParam() {return NULL;}
void usbcmd_t::Cancel() {}
int usbcmd_t::FreeDataParam(usbcmd_data_param_t *) {return -1;}
int usbcmd_t::Receive(usbcmd_data_param_t *, void (*)(void *, USBCMD_CALLBACK_ID, void *, unsigned int, int)) {return -1;}
int usbcmd_t::Send(usbcmd_data_param_t *, void (*)(void *, USBCMD_CALLBACK_ID, void *, unsigned int, int)) {return -1;}
