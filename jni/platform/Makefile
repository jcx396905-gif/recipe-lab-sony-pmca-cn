# Build all examples

EXAMPLEDIR = examples
EXAMPLES = android main updater

MAKETARGETS = $(EXAMPLES:%=make_%)
CLEANTARGETS = $(EXAMPLES:%=clean_%)

.PHONY: all
all: $(MAKETARGETS)

.PHONY: clean
clean: $(CLEANTARGETS)

.PHONY: $(MAKETARGETS)
$(MAKETARGETS): make_%:
	$(MAKE) -C $(EXAMPLEDIR)/$*

.PHONY: $(CLEANTARGETS)
$(CLEANTARGETS): clean_%:
	$(MAKE) -C $(EXAMPLEDIR)/$* clean
