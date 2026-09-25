MODE = UPDATER

BUILD = build
BUILDDIR = $(BUILD)/gen$(GEN)/examples/updater
PLATFORMDIR = ../..

CPPSOURCES = main
MAIN = $(BUILD)/libupdaterbody_gen$(GEN).so

.PHONY: all
all:
	$(MAKE) main GEN=
	$(MAKE) main GEN=1
	$(MAKE) main GEN=2
	$(MAKE) main GEN=3

.PHONY: clean
clean:
	rm -rf $(BUILD)/*

.PHONY: main
main: $(MAIN)

include $(PLATFORMDIR)/build.mk

$(MAIN): $(OBJS) $(LIBOBJS)
	$(CXX) $(LDFLAGS) -L$(BUILDDIR)/$(LIBDIR) $(OBJS) $(LIBS:%=-l%) -shared -o $@
