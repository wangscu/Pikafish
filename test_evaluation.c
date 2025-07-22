#include <stdio.h>
#include <stdlib.h>
#include "src/pikafish_c_api.h"

int main() {
    printf("Testing Pikafish evaluation function...\n");
    
    // Test 1: Starting position
    const char* start_fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w";
    int score = pikafish_evaluate_position(start_fen);
    printf("Starting position score: %d\n", score);
    
    // Test 2: Invalid FEN
    int invalid_score = pikafish_evaluate_position("invalid fen");
    printf("Invalid FEN score: %d\n", invalid_score);
    
    // Test 3: Null FEN
    int null_score = pikafish_evaluate_position(NULL);
    printf("Null FEN score: %d\n", null_score);
    
    printf("Evaluation function test completed!\n");
    return 0;
}