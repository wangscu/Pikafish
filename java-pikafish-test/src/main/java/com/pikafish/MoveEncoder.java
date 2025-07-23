package com.pikafish;

/**
 * Utility class for encoding and decoding chess moves to/from 16-bit integers.
 * This matches the internal move representation used by the C API.
 */
public class MoveEncoder {
    
    /**
     * Convert from coordinate notation (e.g., "e2e4") to 16-bit move
     * @param moveStr coordinate notation move
     * @return 16-bit encoded move
     */
    public static short fromCoordinate(String moveStr) {
        if (moveStr == null || moveStr.length() < 4) {
            return 0;
        }
        
        // Handle special cases
        if ("(none)".equals(moveStr)) {
            return 0;
        }
        
        if ("0000".equals(moveStr)) {
            // Return a special value for null move
            return (short) 129;
        }
        
        try {
            int fromFile = moveStr.charAt(0) - 'a';
            int fromRank = moveStr.charAt(1) - '0';
            int toFile = moveStr.charAt(2) - 'a';
            int toRank = moveStr.charAt(3) - '0';
            
            if (fromFile < 0 || fromFile > 8 || fromRank < 0 || fromRank > 9 ||
                toFile < 0 || toFile > 8 || toRank < 0 || toRank > 9) {
                return 0;
            }
            
            int fromSquare = fromRank * 9 + fromFile;
            int toSquare = toRank * 9 + toFile;
            
            return (short) ((fromSquare << 7) | toSquare);
            
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
        // Handle special cases
        if (move == 0) {
            return "(none)";
        }
        
        if (move == 129) {
            return "0000";
        }
        
        int fromSquare = (move >> 7) & 0x7F;
        int toSquare = move & 0x7F;
        
        int fromFile = fromSquare % 9;
        int fromRank = fromSquare / 9;
        int toFile = toSquare % 9;
        int toRank = toSquare / 9;
        
        StringBuilder sb = new StringBuilder();
        sb.append((char)('a' + fromFile));
        sb.append((char)('0' + fromRank));
        sb.append((char)('a' + toFile));
        sb.append((char)('0' + toRank));
        
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
        
        return (short) ((fromSquare << 7) | toSquare);
    }
    
    /**
     * Validate if a move is properly encoded
     * @param move 16-bit move
     * @return true if valid, false otherwise
     */
    public static boolean isValidMove(short move) {
        if (move == 0 || move == 129) return false;
        
        int fromSquare = (move >> 7) & 0x7F;
        int toSquare = move & 0x7F;
        
        return fromSquare >= 0 && fromSquare < 90 && 
               toSquare >= 0 && toSquare < 90 &&
               fromSquare != toSquare;
    }
}