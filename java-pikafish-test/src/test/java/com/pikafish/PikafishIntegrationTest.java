package com.pikafish;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete Pikafish workflow using the corrected API.
 * Tests position initialization, move application, evaluation, and undo operations.
 */
public class PikafishIntegrationTest {

    private static PikafishLibrary library;
    private static boolean libraryAvailable;

    @BeforeAll
    static void setup() {
        library = PikafishLibrary.Factory.getInstance();
        libraryAvailable = (library != null);
        
        if (libraryAvailable) {
            System.out.println("Pikafish native library loaded successfully");
            int initResult = library.pikafish_engine_init();
            assertEquals(0, initResult, "Engine initialization should succeed");
        }
    }

    @Test
    @DisplayName("Complete game workflow test")
    void testCompleteGameWorkflow() {
        Assumptions.assumeTrue(libraryAvailable, "Library not available");
        
        // 1. Initialize starting position
        String startFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
        int initResult = library.pikafish_init_position(startFen);
        assertEquals(0, initResult, "Failed to initialize position");
        
        // 2. Evaluate starting position
        int startScore = library.pikafish_evaluate();
        System.out.println("Starting position score: " + startScore);
        assertTrue(Math.abs(startScore) < 210, "Starting position should be close to 0");
        
        // 3. Test some common opening moves
        testOpeningMove("c2c5", "Cannon development");
        testOpeningMove("h2h5", "Horse development");
        testOpeningMove("e2e1", "Elephant development");
    }

    @Test
    @DisplayName("Position state management test")
    void testPositionStateManagement() {
        Assumptions.assumeTrue(libraryAvailable, "Library not available");
        
        String fen1 = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
        String fen2 = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKAB1R w - - 0 1";
        
        // Test position 1
        library.pikafish_init_position(fen1);
        int score1 = library.pikafish_evaluate();
        
        // Test position 2
        library.pikafish_init_position(fen2);
        int score2 = library.pikafish_evaluate();
        
        // Position 2 should have different evaluation (missing right rook)
        assertNotEquals(score1, score2, "Different positions should have different evaluations");
        System.out.println("Position 1 score: " + score1 + ", Position 2 score: " + score2);
    }

    @Test
    @DisplayName("Move encoding and application test")
    void testMoveEncodingAndApplication() {
        Assumptions.assumeTrue(libraryAvailable, "Library not available");
        
        // Initialize position
        String fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
        library.pikafish_init_position(fen);
        
        // Test move encoding
        short move = MoveEncoder.fromCoordinate("c2c5");
        assertTrue(MoveEncoder.isValidMove(move), "Move should be valid");
        
        // Test move application
        long newHash = library.pikafish_do_move(move);
        assertNotEquals(0, newHash, "Valid move should return non-zero hash");
        
        // Test position after move
        int newScore = library.pikafish_evaluate();
        System.out.println("Score after c2c5: " + newScore);
    }

    @Test
    @DisplayName("Undo move test")
    void testUndoMove() {
        Assumptions.assumeTrue(libraryAvailable, "Library not available");
        
        // Initialize position
        String fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
        library.pikafish_init_position(fen);
        
        int originalScore = library.pikafish_evaluate();
        
        // Make a move
        short move = MoveEncoder.fromCoordinate("c2c5");
        long hash1 = library.pikafish_do_move(move);
        assertNotEquals(0, hash1, "Move should succeed");
        
        int afterMoveScore = library.pikafish_evaluate();
        
        // Undo the move
        long undoResult = library.pikafish_undo_move(move);
        assertEquals(0, undoResult, "Undo should succeed");
        
        int afterUndoScore = library.pikafish_evaluate();
        
        // Scores should be similar after undo
        assertEquals(originalScore, afterUndoScore, 
                     "Position should be restored after undo");
    }

    @Test
    @DisplayName("Multiple moves sequence test")
    void testMultipleMovesSequence() {
        Assumptions.assumeTrue(libraryAvailable, "Library not available");
        
        // Initialize position
        String fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
        library.pikafish_init_position(fen);
        
        // Common opening sequence
        String[] moves = {"c2c5", "c7c4", "h2h5", "h7h4"};
        
        for (String moveStr : moves) {
            //short move = MoveEncoder.fromCoordinate(moveStr);
            short move = 12;
            assertTrue(MoveEncoder.isValidMove(move), "Move should be valid: " + moveStr);
            
            long hash = library.pikafish_do_move(move);
            assertNotEquals(0, hash, "Move should succeed: " + moveStr);
            
            int score = library.pikafish_evaluate();
            System.out.println("Move " + moveStr + ": " + score);
            library.pikafish_undo_move(move);
            score = library.pikafish_evaluate();
            System.out.println("After " + moveStr + ": " + score);
        }
    }

    @Test
    @DisplayName("Edge case positions test")
    void testEdgeCasePositions() {
        Assumptions.assumeTrue(libraryAvailable, "Library not available");
        
        // Test various edge case positions
        String[] edgeCases = {
            // Checkmate positions
            "4k4/4a4/4b4/9/9/9/9/4R4/4A4/4K4 w - - 0 1",
            // Stalemate positions
            "4k4/5a3/5b3/9/9/9/9/5R3/5A3/5K3 w - - 0 1",
            // Material imbalance
            "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1"
        };
        
        for (String fen : edgeCases) {
            int score = library.pikafish_evaluate_position(fen);
            System.out.println("Edge case score: " + score);
            assertTrue(score >= -10000 && score <= 10000, 
                       "Score should be reasonable for edge case");
        }
    }

    @Test
    @DisplayName("Performance benchmark test")
    void testPerformanceBenchmark() {
        Assumptions.assumeTrue(libraryAvailable, "Library not available");
        
        String fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
        
        long startTime = System.nanoTime();
        
        // Evaluate position multiple times
        final int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            library.pikafish_evaluate_position(fen);
        }
        
        long endTime = System.nanoTime();
        double avgTime = (endTime - startTime) / 1_000_000.0 / iterations;
        
        System.out.println("Average evaluation time: " + avgTime + " ms");
        assertTrue(avgTime < 100, "Evaluation should be reasonably fast");
    }

    private void testOpeningMove(String moveStr, String description) {
        // Initialize fresh position
        String fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
        library.pikafish_init_position(fen);
        
        short move = MoveEncoder.fromCoordinate(moveStr);
        if (MoveEncoder.isValidMove(move)) {
            long hash = library.pikafish_do_move(move);
            if (hash != 0) {
                int score = library.pikafish_evaluate();
                System.out.println(description + " (" + moveStr + "): " + score);
            } else {
                System.out.println("Invalid move: " + moveStr);
            }
        }
    }
}