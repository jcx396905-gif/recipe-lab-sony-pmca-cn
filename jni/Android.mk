LOCAL_PATH := $(call my-dir)

MODE = ANDROID
PLATFORMDIR = platform
include $(LOCAL_PATH)/$(PLATFORMDIR)/vars.mk

# Compile stub shared libraries which are needed to link librecipelab.so. These
# files are already present on the camera.
$(foreach lib, $(LIBS), \
    $(eval include $(CLEAR_VARS)) \
    $(eval LOCAL_MODULE := $(lib)) \
    $(eval LOCAL_SRC_FILES := $(wildcard $(addprefix $(LOCAL_PATH)/$(PLATFORMDIR)/$(DRIVERDIR)/$(lib), .c .cpp))) \
    $(eval LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(PLATFORMDIR)) \
    $(eval LOCAL_CFLAGS += $(DEFS) $(WFLAGS) -std=c11) \
    $(eval LOCAL_LDFLAGS += $(LFLAGS)) \
    $(eval include $(BUILD_SHARED_LIBRARY)) \
)

# Compile librecipelab.so (generic settings-store access)
include $(CLEAR_VARS)
LOCAL_MODULE := recipelab
LOCAL_SRC_FILES := jni.cpp $(foreach source, $(SOURCES), $(wildcard $(addprefix $(LOCAL_PATH)/$(source), .c .cpp)))
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(PLATFORMDIR)
LOCAL_CFLAGS += $(DEFS) $(WFLAGS) -fvisibility=hidden
LOCAL_CONLYFLAGS += -std=c11
LOCAL_CPPFLAGS += -std=c++98 -Wno-vla -Wno-variadic-macros -fexceptions
LOCAL_LDFLAGS += -Wl,--gc-sections -Wl,--exclude-libs,ALL -Wl,--no-undefined
LOCAL_LDLIBS := -lgcc
LOCAL_SHARED_LIBRARIES := $(LIBS)
include $(BUILD_SHARED_LIBRARY)

