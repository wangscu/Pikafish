package com.pikafish;

/**
 * Example program demonstrating how to use the new array-based legal moves generation function.
 */
public class GenerateMovesExample {
    
    public static void main(String[] args) {
        // Get library instance
        PikafishLibrary library = PikafishLibrary.Factory.getInstance();
        if (library == null) {
            System.err.println("Failed to load Pikafish library");
            return;
        }
        
        // Initialize engine
        int initResult = library.pikafish_engine_init();
        if (initResult != 0) {
            System.err.println("Failed to initialize engine");
            return;
        }
        
        // Initialize starting position
        String startFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
        int positionResult = library.pikafish_init_position(startFen);
        if (positionResult != 0) {
            System.err.println("Failed to initialize position");
            return;
        }
        
        // Generate legal moves using the original pointer-based function
        com.sun.jna.Pointer movesPointer = library.pikafish_generate_legal_moves();
        if (movesPointer == null) {
            System.err.println("Failed to generate legal moves (pointer version)");
            return;
        }
        
        // Read moves from pointer until we hit 0 (end marker)
        short[] movesFromPointer = movesPointer.getShortArray(0, 128); // Read up to 128 moves
        int moveCountFromPointer = 0;
        for (int i = 0; i < movesFromPointer.length; i++) {
            if (movesFromPointer[i] == 0) {
                break;
            }
            moveCountFromPointer++;
        }
        
        System.out.println("Legal moves (pointer version): " + moveCountFromPointer);
        
        // Generate legal moves using the new array-based function
        short[] movesArray = new short[128]; // MAX_MOVES + 1
        int moveCountFromArray = library.pikafish_generate_legal_moves(movesArray);
        
        System.out.println("Legal moves (array version): " + moveCountFromArray);
        
        // Verify both methods return the same number of moves
        if (moveCountFromPointer == moveCountFromArray) {
            System.out.println("Both methods returned the same number of moves - SUCCESS");
        } else {
            System.out.println("Methods returned different move counts - ERROR");
        }
        
        // Display first few moves from both methods
        System.out.println("\nFirst 5 moves from pointer version:");
        for (int i = 0; i < Math.min(5, moveCountFromPointer); i++) {
            short move = movesFromPointer[i];
            String moveStr = library.pikafish_decode_move(move);
            System.out.println("  " + i + ": " + move + " -> " + moveStr);
        }
        
        System.out.println("\nFirst 5 moves from array version:");
        for (int i = 0; i < Math.min(5, moveCountFromArray); i++) {
            short move = movesArray[i];
            String moveStr = library.pikafish_decode_move(move);
            System.out.println("  " + i + ": " + move + " -> " + moveStr);
        }
    }
}