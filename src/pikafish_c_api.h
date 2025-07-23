#ifndef PIKAFISH_C_API_H
#define PIKAFISH_C_API_H

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
 * Evaluate current position
 * @return - evaluation score in centipawns (positive favors side to move)
 */
int pikafish_evaluate();

/**
 * Undo the last move
 * @return - 0 on success, non-zero on error
 */
uint64_t pikafish_undo_move(uint16_t move);

#ifdef __cplusplus
}
#endif

#endif /* PIKAFISH_C_API_H */
