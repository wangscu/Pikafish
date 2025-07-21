#include <stdio.h>
#include <stdlib.h>
#include "src/pikafish_c_api.h"

int main() {
    printf("Testing Pikafish C API\n");

    // Test engine info
    const char* info = pikafish_engine_info();
    printf("Engine Info: %s\n", info);

    // Test engine initialization
    int result = pikafish_engine_init();
    printf("Engine initialization result: %d\n", result);

    printf("Test completed successfully!\n");

    return 0;
}
