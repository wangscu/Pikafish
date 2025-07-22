package com.pikafish;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * Java Native Access (JNA) interface for Pikafish Chinese Chess Engine
 *
 * This interface provides direct access to the minimal C API of Pikafish engine.
 * All functions are thread-safe and can be called concurrently.
 *
 * Requirements:
 * - JNA library (net.java.dev.jna:jna:5.13.0 or later)
 * - Pikafish shared library (libpikafish.so/pikafish.dll/libpikafish.dylib)
 * - NNUE model file (pikafish.nnue) in working directory
 *
 * Usage Example:
 * <pre>
 * // Initialize starting position
 * int result = PikafishLib.INSTANCE.init_position(
 *     "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1");
 *
 * // Make a move (from e3 to e4, encoded as move)
 * short move = (short)((40 << 7) | 49); // from square 40 to square 49
 * result = PikafishLib.INSTANCE.make_move(move);
 *
 * // Evaluate position
 * int evaluation = PikafishLib.INSTANCE.evaluate();
 *
 * // Unmake the move
 * result = PikafishLib.INSTANCE.unmake_move(move);
 * </pre>
 */
public interface PikafishLib extends Library {

    /**
     * Singleton instance for accessing the library
     */
    PikafishLib INSTANCE = Native.load(getLibraryName(), PikafishLib.class);

    /**
     * Get platform-specific library name
     */
    static String getLibraryName() {
        if (Platform.isWindows()) {
            return "pikafish";  // Will load pikafish.dll
        } else if (Platform.isMac()) {
            return "pikafish";  // Will load libpikafish.dylib
        } else {
            return "pikafish";  // Will load libpikafish.so
        }
    }

    /**
     * Initialize position from FEN string
     *
     * @param fen FEN string describing the chess position
     *            Example: "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1"
     * @return 0 on success, -1 on invalid FEN
     */
    int init_position(String fen);

    /**
     * Make a move on the current position
     *
     * Move encoding: 16-bit value where bits 7-13 are origin square (0-89)
     * and bits 0-6 are destination square (0-89).
     *
     * Chinese chess board squares are numbered 0-89:
     * - Files: a-i (0-8)
     * - Ranks: 0-9 (bottom to top from Red's perspective)
     * - Square = rank * 9 + file
     *
     * @param move Encoded move as 16-bit unsigned integer
     * @return 0 on success, -1 on illegal move
     */
    int make_move(short move);

    /**
     * Unmake the last move
     *
     * @param move Encoded move (must exactly match the last move made)
     * @return 0 on success, -1 on error (move doesn't match or no moves to undo)
     */
    int unmake_move(short move);

    /**
     * Evaluate the current position
     *
     * Returns evaluation from the perspective of the side to move.
     * Positive values favor the current player, negative values favor the opponent.
     *
     * @return Evaluation score in centipawns (typically -3000 to +3000)
     *         Returns 0 if position is in check or evaluation fails
     */
    int evaluate();

    /**
     * Utility class for move encoding/decoding and position management
     */
    class Utils {

        /**
         * Encode a move from coordinates
         *
         * @param fromFile Source file (0-8, representing a-i)
         * @param fromRank Source rank (0-9)
         * @param toFile Destination file (0-8, representing a-i)
         * @param toRank Destination rank (0-9)
         * @return Encoded move as short
         */
        public static short encodeMove(int fromFile, int fromRank, int toFile, int toRank) {
            int fromSquare = fromRank * 9 + fromFile;
            int toSquare = toRank * 9 + toFile;
            return (short)((fromSquare << 7) | toSquare);
        }

        /**
         * Encode a move from algebraic notation (e.g., "e3e4")
         *
         * @param moveStr Move in algebraic notation (e.g., "a0a1", "e3e4")
         * @return Encoded move as short
         * @throws IllegalArgumentException if move string is invalid
         */
        public static short encodeMove(String moveStr) {
            if (moveStr == null || moveStr.length() != 4) {
                throw new IllegalArgumentException("Move string must be 4 characters (e.g., 'e3e4')");
            }

            int fromFile = moveStr.charAt(0) - 'a';
            int fromRank = moveStr.charAt(1) - '0';
            int toFile = moveStr.charAt(2) - 'a';
            int toRank = moveStr.charAt(3) - '0';

            if (fromFile < 0 || fromFile > 8 || fromRank < 0 || fromRank > 9 ||
                toFile < 0 || toFile > 8 || toRank < 0 || toRank > 9) {
                throw new IllegalArgumentException("Invalid coordinates in move: " + moveStr);
            }

            return encodeMove(fromFile, fromRank, toFile, toRank);
        }

        /**
         * Decode a move to algebraic notation
         *
         * @param move Encoded move
         * @return Move string in algebraic notation (e.g., "e3e4")
         */
        public static String decodeMove(short move) {
            int fromSquare = (move >> 7) & 0x7F;
            int toSquare = move & 0x7F;

            int fromFile = fromSquare % 9;
            int fromRank = fromSquare / 9;
            int toFile = toSquare % 9;
            int toRank = toSquare / 9;

            return "" + (char)('a' + fromFile) + fromRank +
                   (char)('a' + toFile) + toRank;
        }

        /**
         * Standard starting position FEN for Chinese Chess
         */
        public static final String STARTING_FEN =
            "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";

        /**
         * Initialize to starting position
         *
         * @return 0 on success, -1 on error
         */
        public static int initStartingPosition() {
            return INSTANCE.init_position(STARTING_FEN);
        }

        /**
         * Convert evaluation to a human-readable string
         *
         * @param eval Evaluation in centipawns
         * @return Formatted evaluation string
         */
        public static String formatEvaluation(int eval) {
            if (eval == 0) return "0.00";

            double value = eval / 100.0;
            return String.format("%+.2f", value);
        }
    }
}
