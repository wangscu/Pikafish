# Pikafish Shared Library Makefile

UNAME := $(shell uname)
ifeq ($(UNAME), Darwin)
	SHARED_LIB = libpikafish.dylib
	LDFLAGS = -L. -lpikafish
else
	SHARED_LIB = libpikafish.so
	LDFLAGS = -L. -lpikafish -ldl
endif

CC = gcc
CFLAGS = -Wall -O2

.PHONY: all clean test shared-lib

all: shared-lib test

shared-lib:
	@echo "Building Pikafish shared library..."
	$(MAKE) -C src shared-lib
	cp src/$(SHARED_LIB) .

test: test_pikafish shared-lib
	@echo "Running test..."
	LD_LIBRARY_PATH=. ./test_pikafish

test_pikafish: test_pikafish.c shared-lib
	$(CC) $(CFLAGS) -o test_pikafish test_pikafish.c $(LDFLAGS)

clean:
	$(MAKE) -C src clean
	rm -f test_pikafish $(SHARED_LIB)

help:
	@echo "Available targets:"
	@echo "  all        - Build shared library and test program"
	@echo "  shared-lib - Build only the shared library"
	@echo "  test       - Build and run test program"
	@echo "  clean      - Clean all build artifacts"
	@echo ""
	@echo "Usage examples:"
	@echo "  make all                    # Build everything"
	@echo "  make shared-lib ARCH=x86-64-avx2  # Build with specific architecture"
