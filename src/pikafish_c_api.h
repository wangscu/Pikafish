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

#ifdef __cplusplus
}
#endif

#endif /* PIKAFISH_C_API_H */
