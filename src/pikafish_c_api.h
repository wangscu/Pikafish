#ifndef PIKAFISH_C_API_H
#define PIKAFISH_C_API_H

#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Main entry point for Pikafish engine
 * @param argc - number of command line arguments
 * @param argv - array of command line arguments
 * @return - exit code
 */
int pikafish_engine_main(int argc, char* argv[]);

/**
 * Initialize the Pikafish engine without starting the main loop
 * @return - 0 on success
 */
int pikafish_engine_init(void);

/**
 * Get engine information string
 * @return - pointer to engine info string (static memory)
 */
const char* pikafish_engine_info(void);

/**
 * Evaluate a chess position from FEN string
 * @param fen - FEN string representing the chess position
 * @return - evaluation score in centipawns (positive favors side to move)
 */
int pikafish_evaluate_position(const char* fen);

/**
 * Check if the given side is in check
 * @param is_white - 1 for white side, 0 for black side
 * @return - 1 if the specified side is in check, 0 otherwise
 */
int pikafish_is_side_in_check(int is_white);

/**
 * Initialize position from FEN string
 * @param fen - FEN string representing the chess position
 * @return - 0 on success, non-zero on error
 */
int pikafish_init_position(const char* fen);

/**
 * Apply a legal move to the current position
 * @param move - move in coordinate notation (e.g., "e2e4")
 * @return - new position hash, 0 on error
 */
uint64_t pikafish_do_move(uint16_t move);


/**
 * Generate a list of legal moves for the current position (array version)
 * @param moves - array to store legal moves (must be at least MAX_MOVES + 1 in size)
 * @return - number of legal moves generated
 */
int pikafish_generate_legal_moves(uint16_t moves[]);

/**
 * Evaluate current position
 * @return - evaluation score in centipawns (positive favors side to move)
 */
int pikafish_evaluate();

/**
 * Get current position in FEN format
 * @return - pointer to FEN string (static memory)
 */
char* pikafish_get_fen();

/**
 * Undo the last move
 * @return - 0 on success, non-zero on error
 */
uint64_t pikafish_undo_move(uint16_t move);

/**
 * Encode a move from coordinate notation to internal representation
 * @param move_str - move in coordinate notation (e.g., "e2e4")
 * @return - encoded move as uint16_t, 0 if invalid
 */
uint16_t pikafish_encode_move(const char* move_str);

/**
 * Decode a move from internal representation to coordinate notation
 * @param move - encoded move as uint16_t
 * @return - move in coordinate notation (e.g., "e2e4"), NULL if invalid
 */
char* pikafish_decode_move(uint16_t move);

#ifdef __cplusplus
}
#endif

#endif /* PIKAFISH_C_API_H */
