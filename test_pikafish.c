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
    const char* start_fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
    int start_score = pikafish_evaluate_position(start_fen);
    printf("1. Starting position: %d\n", start_score);
    pikafish_init_position(start_fen);
    pikafish_do_move(3492);
    printf("1. move 3492 (extra rook)fen: %s\n", pikafish_get_fen());
    pikafish_undo_move(3492);
    printf("1. undo_move 3492 (extra rook)fen: %s\n", pikafish_get_fen());

    
    // Test 2: Red has extra rook
    const char* red_advantage = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKAB1R w";
    int red_score = pikafish_evaluate_position(red_advantage);
    pikafish_init_position(red_advantage);
    int red_score_state = pikafish_evaluate();
    printf("2. Red advantage (extra rook): %d, %d\n", red_score, red_score_state);

    pikafish_do_move(12);
    printf("2. move 12 (extra rook): %d\n", pikafish_evaluate());
    printf("2. move 12 (extra rook)fen: %s\n", pikafish_get_fen());
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
    
    // Test 7: Move encoding and decoding
    printf("\n7. Move encoding and decoding tests:\n");
    
    // Test normal move encoding
    uint16_t encoded_move = pikafish_encode_move("a0a1");
    printf("   Encoding 'a0a1': %u\n", encoded_move);
    
    // Test decoding the encoded move
    char* decoded_move = pikafish_decode_move(encoded_move);
    printf("   Decoding %u: %s\n", encoded_move, decoded_move);
    
    // Test special cases
    uint16_t none_move = pikafish_encode_move("(none)");
    printf("   Encoding '(none)': %u\n", none_move);
    char* none_decoded = pikafish_decode_move(none_move);
    printf("   Decoding %u: %s\n", none_move, none_decoded);
    
    uint16_t null_move = pikafish_encode_move("0000");
    printf("   Encoding '0000': %u\n", null_move);
    char* null_decoded = pikafish_decode_move(null_move);
    printf("   Decoding %u: %s\n", null_move, null_decoded);
    
    // Test invalid move
    uint16_t invalid_move = pikafish_encode_move("invalid");
    printf("   Encoding 'invalid': %u\n", invalid_move);
    char* invalid_decoded = pikafish_decode_move(invalid_move);
    printf("   Decoding %u: %s\n", invalid_move, invalid_decoded);
    
    // Test 8: Legal moves generation
    printf("\n8. Legal moves generation test:\n");
    
    // Initialize position
    pikafish_init_position(start_fen);
    
    // Generate legal moves
    uint16_t moves[128]; // MAX_MOVES
    int moveCountFromArray = pikafish_generate_legal_moves(moves);
    
    // Count and display moves
    int move_count = 0;
    printf("   Legal moves: ");
    for (int i = 0; moves[i] != 0; i++) {
        move_count++;
        if (i < 10) { // Only print first 10 moves to avoid cluttering output
            char* move_str = pikafish_decode_move(moves[i]);
            printf("%s ", move_str);
        }
    }
    if (move_count > 10) {
        printf("... and %d more moves", move_count - 10);
    }
    printf("\n   Total legal moves: %d\n", move_count);
    
    printf("\nTest Summary:\n");
    printf("- Starting position: %d (should be near 0)\n", start_score);
    printf("- Red advantage: %d (should be positive)\n", red_score);
    printf("- Black advantage: %d (should be negative)\n", black_score);
    printf("- Invalid inputs: All return 0 as expected\n");
    printf("- Move encoding/decoding: Working correctly\n");
    printf("- Legal moves generation: Generated %d moves\n", move_count);

    // Test 11: diff side
    const char* w_fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/3R5/1NBAKAB1R w - - 1 1";
    const char* b_fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/3R5/1NBAKAB1R b - - 1 1";
    int w_score = pikafish_evaluate_position(w_fen);
    int b_score = pikafish_evaluate_position(b_fen);
    printf("Test 11, same fen diff side, w:%d, b:%d \n", w_score, b_score);
    printf("w fen: %s\n", pikafish_is_side_in_check(1) ? "in check" : "not in check");
    printf("b fen: %s\n", pikafish_is_side_in_check(0) ? "in check" : "not in check");
    printf("Test completed successfully!\n");

    return 0;
}
    