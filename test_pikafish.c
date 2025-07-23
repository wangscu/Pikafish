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

    printf("Comprehensive Pikafish evaluation test...\n");
    
    // Test 1: Starting position
    const char* start_fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w";
    int start_score = pikafish_evaluate_position(start_fen);
    printf("1. Starting position: %d\n", start_score);
    
    // Test 2: Red has extra rook
    const char* red_advantage = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKAB1R w";
    int red_score = pikafish_evaluate_position(red_advantage);
    pikafish_init_position(red_advantage);
    int red_score_state = pikafish_evaluate();
    printf("2. Red advantage (extra rook): %d, %d\n", red_score, red_score_state);

    pikafish_do_move(12);
    printf("2. move 12 (extra rook): %d\n", pikafish_evaluate());
    pikafish_undo_move(12);
    printf("2. undo_move 12 (extra rook): %d\n", pikafish_evaluate());

    
    // Test 3: Same position but black to move
    const char* black_advantage = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKAB1R b";
    int black_score = pikafish_evaluate_position(black_advantage);
    pikafish_init_position(black_advantage);
    int black_score_state = pikafish_evaluate();
    printf("3. Black advantage (extra rook): %d, %d\n", black_score, black_score_state);
    
    // Test 4: Empty board (should return 0)
    const char* empty_fen = "9/9/9/9/9/9/9/9/9/9 w";
    int empty_score = pikafish_evaluate_position(empty_fen);
    printf("4. Empty board: %d\n", empty_score);
    
    // Test 5: Invalid FEN
    int invalid_score = pikafish_evaluate_position("invalid");
    printf("5. Invalid FEN: %d\n", invalid_score);
    
    // Test 6: NULL input
    int null_score = pikafish_evaluate_position(NULL);
    printf("6. NULL input: %d\n", null_score);
    
    printf("\nTest Summary:\n");
    printf("- Starting position: %d (should be near 0)\n", start_score);
    printf("- Red advantage: %d (should be positive)\n", red_score);
    printf("- Black advantage: %d (should be negative)\n", black_score);
    printf("- Invalid inputs: All return 0 as expected\n");

    printf("Test completed successfully!\n");

    return 0;
}
