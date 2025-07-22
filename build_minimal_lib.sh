#!/bin/bash

# Build script for Pikafish minimal shared library
# Usage: ./build_minimal_lib.sh [ARCH] [COMP]

set -e

ARCH=${1:-x86-64-avx2}
COMP=${2:-gcc}

echo "Building Pikafish minimal shared library..."
echo "Architecture: $ARCH"
echo "Compiler: $COMP"

# Change to src directory
cd src

# Clean previous builds
make objclean 2>/dev/null || true

# Define compiler flags for shared library
export CXXFLAGS="-std=c++17 -fno-exceptions -Wall -Wcast-qual -fno-rtti -fPIC -DNDEBUG -O3 -flto"
export LDFLAGS="-shared -flto -fwhole-program"

# Platform-specific settings
case "$(uname -s)" in
    Linux*)
        LIB_NAME="libpikafish.so"
        export LDFLAGS="$LDFLAGS -Wl,--no-as-needed -lpthread -lm"
        ;;
    Darwin*)
        LIB_NAME="libpikafish.dylib"
        export LDFLAGS="$LDFLAGS -lpthread -lm"
        ;;
    CYGWIN*|MINGW32*|MSYS*|MINGW*)
        LIB_NAME="pikafish.dll"
        export LDFLAGS="$LDFLAGS -static-libgcc -static-libstdc++ -Wl,--no-undefined"
        ;;
    *)
        echo "Unsupported platform: $(uname -s)"
        exit 1
        ;;
esac

echo "Building for platform: $(uname -s)"
echo "Target library: $LIB_NAME"

# Build all object files (excluding main.cpp equivalents)
echo "Step 1/3: Building object files..."
make ARCH=$ARCH COMP=$COMP CXXFLAGS="$CXXFLAGS" build 2>/dev/null || true

# Verify we have the minimal API files
if [ ! -f "pikafish_minimal_api.h" ] || [ ! -f "pikafish_minimal_api.cpp" ]; then
    echo "Error: Minimal API files not found!"
    exit 1
fi

# Compile the minimal API object
echo "Step 2/3: Compiling minimal API..."
g++ $CXXFLAGS -c pikafish_minimal_api.cpp -o pikafish_minimal_api.o

# Create the shared library - collect only necessary objects
echo "Step 3/3: Linking shared library..."

# Define essential object files (excluding main/UCI interface)
ESSENTIAL_OBJS="bitboard.o position.o evaluate.o misc.o movegen.o"
ESSENTIAL_OBJS="$ESSENTIAL_OBJS tune.o thread.o tt.o timeman.o"
ESSENTIAL_OBJS="$ESSENTIAL_OBJS nnue_misc.o nnue_accumulator.o network.o half_ka_v2_hm.o"
ESSENTIAL_OBJS="$ESSENTIAL_OBJS memory.o score.o search.o movepick.o"

# Add external dependencies
EXTERNAL_OBJS="$(find . -name '*.o' -path './external/*' | tr '\n' ' ')"

# Link the shared library
g++ $LDFLAGS -o $LIB_NAME pikafish_minimal_api.o $ESSENTIAL_OBJS $EXTERNAL_OBJS

# Copy to project root
cp $LIB_NAME ../

echo ""
echo "✓ Successfully built: $LIB_NAME"
echo "✓ Library copied to project root"

# Show library info
if command -v file &> /dev/null; then
    echo ""
    echo "Library info:"
    file ../$LIB_NAME
fi

if command -v ldd &> /dev/null && [[ "$LIB_NAME" == *.so ]]; then
    echo ""
    echo "Dependencies:"
    ldd ../$LIB_NAME 2>/dev/null | head -10 | grep -v "not found" || true
fi

echo ""
echo "To use in Java, ensure NNUE model file 'pikafish.nnue' is in working directory"
echo "Example usage in Java with JNA:"
echo "  int result = PikafishLib.INSTANCE.init_position(\"rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w\");"
