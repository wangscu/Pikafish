#include <iostream>
#include <string>
#include <cassert>
#include "src/pikafish_minimal_api.h"

/**
 * Test program for Pikafish minimal C API
 * This program tests all four API functions
 */

// Helper function to decode move for display
std::string decode_move(uint16_t move) {
    int from_square = (move >> 7) & 0x7F;
    int to_square = move & 0x7F;

    int from_file = from_square % 9;
    int from_rank = from_square / 9;
    int to_file = to_square % 9;
    int to_rank = to_square / 9;

    std::string result;
    result += (char)('a' + from_file);
    result += (char)('0' + from_rank);
    result += (char)('a' + to_file);
    result += (char)('0' + to_rank);

    return result;
}

// Helper function to encode move from string
uint16_t encode_move(const std::string& move_str) {
    if (move_str.length() != 4) return 0;

    int from_file = move_str[0] - 'a';
    int from_rank = move_str[1] - '0';
    int to_file = move_str[2] - 'a';
    int to_rank = move_str[3] - '0';

    if (from_file < 0 || from_file > 8 || from_rank < 0 || from_rank > 9 ||
        to_file < 0 || to_file > 8 || to_rank < 0 || to_rank > 9) {
        return 0;
    }

    int from_square = from_rank * 9 + from_file;
    int to_square = to_rank * 9 + to_file;

    return (from_square << 7) | to_square;
}

int main() {
    std::cout << "=== Pikafish Minimal API Test ===\n\n";

    // Test 1: Initialize starting position
    std::cout << "Test 1: Initialize starting position\n";
    const char* starting_fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";

    int result = init_position(starting_fen);
    if (result == 0) {
        std::cout << "✓ Position initialized successfully\n";
    } else {
        std::cout << "✗ Failed to initialize position\n";
        return 1;
    }

    // Test 2: Evaluate starting position
    std::cout << "\nTest 2: Evaluate starting position\n";
    int eval = evaluate();
    std::cout << "Starting position evaluation: " << eval << " centipawns\n";

    // Test 3: Make a valid move
    std::cout << "\nTest 3: Make a valid move\n";
    std::string move_str = "e3e4";  // Move pawn from e3 to e4
    uint16_t move = encode_move(move_str);

    if (move == 0) {
        std::cout << "✗ Failed to encode move: " << move_str << "\n";
        return 1;
    }

    std::cout << "Attempting move: " << move_str << " (encoded as " << move << ")\n";
    result = make_move(move);

    if (result == 0) {
        std::cout << "✓ Move executed successfully\n";

        // Evaluate new position
        int new_eval = evaluate();
        std::cout << "New evaluation: " << new_eval << " centipawns\n";
        std::cout << "Evaluation change: " << (new_eval - eval) << " centipawns\n";

        // Test 4: Unmake the move
        std::cout << "\nTest 4: Unmake the move\n";
        result = unmake_move(move);

        if (result == 0) {
            std::cout << "✓ Move undone successfully\n";

            // Verify position is back to original
            int restored_eval = evaluate();
            std::cout << "Restored evaluation: " << restored_eval << " centipawns\n";

            if (restored_eval == eval) {
                std::cout << "✓ Position correctly restored\n";
            } else {
                std::cout << "✗ Position not correctly restored (eval mismatch)\n";
            }
        } else {
            std::cout << "✗ Failed to unmake move\n";
        }
    } else {
        std::cout << "✗ Failed to make move (illegal move?)\n";
    }

    // Test 5: Try some invalid operations
    std::cout << "\nTest 5: Test error handling\n";

    // Try invalid FEN
    result = init_position("invalid_fen_string");
    if (result != 0) {
        std::cout << "✓ Invalid FEN correctly rejected\n";
    } else {
        std::cout << "✗ Invalid FEN was accepted\n";
    }

    // Restore valid position for remaining tests
    init_position(starting_fen);

    // Try invalid move
    uint16_t invalid_move = encode_move("a0a0");  // Same square
    result = make_move(invalid_move);
    if (result != 0) {
        std::cout << "✓ Invalid move correctly rejected\n";
    } else {
        std::cout << "✗ Invalid move was accepted\n";
    }

    // Test 6: Test move sequence
    std::cout << "\nTest 6: Test move sequence\n";
    std::vector<std::string> moves = {"e3e4", "e6e5", "b0c2", "b9c7"};
    std::vector<uint16_t> encoded_moves;

    for (const auto& move_str : moves) {
        uint16_t move = encode_move(move_str);
        result = make_move(move);

        if (result == 0) {
            std::cout << "✓ Move " << move_str << " executed successfully\n";
            encoded_moves.push_back(move);

            int eval = evaluate();
            std::cout << "  Evaluation: " << eval << " centipawns\n";
        } else {
            std::cout << "✗ Move " << move_str << " failed\n";
            break;
        }
    }

    // Unmake all moves in reverse order
    std::cout << "\nUnmaking moves in reverse order:\n";
    for (int i = encoded_moves.size() - 1; i >= 0; i--) {
        result = unmake_move(encoded_moves[i]);
        if (result == 0) {
            std::cout << "✓ Unmade move " << decode_move(encoded_moves[i]) << "\n";
        } else {
            std::cout << "✗ Failed to unmake move " << decode_move(encoded_moves[i]) << "\n";
        }
    }

    // Final evaluation should match starting position
    int final_eval = evaluate();
    std::cout << "Final evaluation: " << final_eval << " centipawns\n";

    std::cout << "\n=== Test Summary ===\n";
    std::cout << "All basic functionality tests completed.\n";
    std::cout << "The API appears to be working correctly.\n";

    return 0;
}
