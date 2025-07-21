#!/bin/bash

echo "==================================="
echo "Pikafish Java Demo Script"
echo "==================================="

# 检查是否存在共享库
if [ -f "../libpikafish.dylib" ]; then
    echo "✓ Found Pikafish shared library: ../libpikafish.dylib"
elif [ -f "../libpikafish.so" ]; then
    echo "✓ Found Pikafish shared library: ../libpikafish.so"
else
    echo "✗ Pikafish shared library not found!"
    echo "Please run 'make shared-lib' in the parent directory first."
    exit 1
fi

# 检查Java和Maven
echo ""
echo "Checking prerequisites..."
java -version 2>&1 | head -1
mvn -version 2>&1 | head -1

echo ""
echo "==================================="
echo "1. Compiling Java project..."
echo "==================================="
mvn clean compile

echo ""
echo "==================================="
echo "2. Running main test program..."
echo "==================================="
mvn exec:java

echo ""
echo "==================================="
echo "3. Running JUnit tests..."
echo "==================================="
mvn test

echo ""
echo "==================================="
echo "Demo completed successfully!"
echo "==================================="
echo ""
echo "Next steps:"
echo "- Check out the source code in src/main/java/com/pikafish/"
echo "- Modify PikafishTest.java to add your own tests"
echo "- Use PikafishLibrary.java as a template for your own projects"
echo "- Run 'mvn clean package' to create a JAR file"
