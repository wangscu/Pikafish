package com.pikafish;

/**
 * Utility class for encoding and decoding chess moves to/from 16-bit integers.
 * This matches the internal move representation used by the C API.
 */
public class MoveEncoder {
    
    // Move encoding constants (matching Stockfish internal representation)
    private static final int MOVE_TYPE_NORMAL = 0;
    private static final int MOVE_TYPE_PROMOTION = 1 << 12;
    private static final int MOVE_TYPE_ENPASSANT = 2 << 12;
    private static final int MOVE_TYPE_CASTLING = 3 << 12;
    
    // Promotion piece types
    private static final int PROMOTE_KNIGHT = 0 << 14;
    private static final int PROMOTE_BISHOP = 1 << 14;
    private static final int PROMOTE_ROOK = 2 << 14;
    private static final int PROMOTE_QUEEN = 3 << 14;
    
    /**
     * Convert from coordinate notation (e.g., "e2e4") to 16-bit move
     * @param moveStr coordinate notation move
     * @return 16-bit encoded move
     */
    public static short fromCoordinate(String moveStr) {
        if (moveStr == null || moveStr.length() < 4) {
            return 0;
        }
        
        try {
            int fromFile = moveStr.charAt(0) - 'a';
            int fromRank = moveStr.charAt(1) - '1';
            int toFile = moveStr.charAt(2) - 'a';
            int toRank = moveStr.charAt(3) - '1';
            
            if (fromFile < 0 || fromFile > 8 || fromRank < 0 || fromRank > 9 ||
                toFile < 0 || toFile > 8 || toRank < 0 || toRank > 9) {
                return 0;
            }
            
            int fromSquare = fromRank * 9 + fromFile;
            int toSquare = toRank * 9 + toFile;
            
            short move = (short) ((fromSquare << 6) | toSquare);
            
            // Handle promotion
            if (moveStr.length() >= 5) {
                char promote = moveStr.charAt(4);
                int promoteType = 0;
                switch (promote) {
                    case 'n': case 'N': promoteType = PROMOTE_KNIGHT; break;
                    case 'b': case 'B': promoteType = PROMOTE_BISHOP; break;
                    case 'r': case 'R': promoteType = PROMOTE_ROOK; break;
                    case 'q': case 'Q': promoteType = PROMOTE_QUEEN; break;
                    default: return 0;
                }
                move |= MOVE_TYPE_PROMOTION | promoteType;
            }
            
            return move;
            
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Convert from 16-bit move to coordinate notation
     * @param move 16-bit encoded move
     * @return coordinate notation move
     */
    public static String toCoordinate(short move) {
        if (move == 0) {
            return "0000";
        }
        
        int fromSquare = (move >> 6) & 0x3F;
        int toSquare = move & 0x3F;
        
        int fromFile = fromSquare % 9;
        int fromRank = fromSquare / 9;
        int toFile = toSquare % 9;
        int toRank = toSquare / 9;
        
        StringBuilder sb = new StringBuilder();
        sb.append((char)('a' + fromFile));
        sb.append((char)('1' + fromRank));
        sb.append((char)('a' + toFile));
        sb.append((char)('1' + toRank));
        
        // Handle promotion
        int moveType = move & (3 << 12);
        if (moveType == MOVE_TYPE_PROMOTION) {
            int promoteType = move & (3 << 14);
            switch (promoteType) {
                case PROMOTE_KNIGHT: sb.append('n'); break;
                case PROMOTE_BISHOP: sb.append('b'); break;
                case PROMOTE_ROOK: sb.append('r'); break;
                case PROMOTE_QUEEN: sb.append('q'); break;
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Create a simple move from source and destination squares
     * @param fromFile source file (0-8)
     * @param fromRank source rank (0-9)
     * @param toFile destination file (0-8)
     * @param toRank destination rank (0-9)
     * @return 16-bit encoded move
     */
    public static short createMove(int fromFile, int fromRank, int toFile, int toRank) {
        if (fromFile < 0 || fromFile > 8 || fromRank < 0 || fromRank > 9 ||
            toFile < 0 || toFile > 8 || toRank < 0 || toRank > 9) {
            return 0;
        }
        
        int fromSquare = fromRank * 9 + fromFile;
        int toSquare = toRank * 9 + toFile;
        
        return (short) ((fromSquare << 6) | toSquare);
    }
    
    /**
     * Validate if a move is properly encoded
     * @param move 16-bit move
     * @return true if valid, false otherwise
     */
    public static boolean isValidMove(short move) {
        if (move == 0) return false;
        
        int fromSquare = (move >> 6) & 0x3F;
        int toSquare = move & 0x3F;
        
        return fromSquare >= 0 && fromSquare < 90 && 
               toSquare >= 0 && toSquare < 90 &&
               fromSquare != toSquare;
    }
}