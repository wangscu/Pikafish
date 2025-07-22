#ifndef PIKAFISH_MINIMAL_API_H
#define PIKAFISH_MINIMAL_API_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Minimal C API for Pikafish Chinese Chess Engine
 * Thread-safe, re-entrant API with zero-overhead design
 * All functions return 0 on success, negative values on error
 */

/**
 * Initialize position from FEN string
 * @param fen - FEN string describing the position
 * @return 0 on success, -1 on invalid FEN
 */
int init_position(const char* fen);

/**
 * Make a move on the current position
 * @param move - 16-bit move encoding (from<<7|to)
 * @return 0 on success, -1 on illegal move
 */
int make_move(uint16_t move);

/**
 * Unmake the last move
 * @param move - 16-bit move encoding (must match last move made)
 * @return 0 on success, -1 on error
 */
int unmake_move(uint16_t move);

/**
 * Evaluate current position
 * @return evaluation score in centipawns (positive for current side to move)
 */
int evaluate(void);

#ifdef __cplusplus
}
#endif

#endif /* PIKAFISH_MINIMAL_API_H */
