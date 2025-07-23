package com.pikafish;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MoveEncoder class.
 */
public class MoveEncoderTest {

    @Test
    public void testFromCoordinate() {
        // Test normal move
        short move = MoveEncoder.fromCoordinate("a0a1");
        // a0 = 0*9 + 0 = 0, a1 = 1*9 + 0 = 9
        assertEquals((short) ((0 << 7) | 9), move);
        
        // Test special case: none
        move = MoveEncoder.fromCoordinate("(none)");
        assertEquals(0, move);
        
        // Test special case: null move
        move = MoveEncoder.fromCoordinate("0000");
        assertEquals(129, move);
        
        // Test invalid input
        move = MoveEncoder.fromCoordinate(null);
        assertEquals(0, move);
        
        move = MoveEncoder.fromCoordinate("a0");
        assertEquals(0, move);
    }

    @Test
    public void testToCoordinate() {
        // Test normal move
        // a0 = 0*9 + 0 = 0, a1 = 1*9 + 0 = 9
        String moveStr = MoveEncoder.toCoordinate((short) ((0 << 7) | 9));
        assertEquals("a0a1", moveStr);
        
        // Test special case: none
        moveStr = MoveEncoder.toCoordinate((short) 0);
        assertEquals("(none)", moveStr);
        // Test special case: null move
        moveStr = MoveEncoder.toCoordinate((short) 129);
        assertEquals("0000", moveStr);
    }

    @Test
    public void testCreateMove() {
        // Test normal move creation
        short move = MoveEncoder.createMove(0, 0, 0, 1); // a0 to a1
        // a0 = 0*9 + 0 = 0, a1 = 1*9 + 0 = 9
        assertEquals((short) ((0 << 7) | 9), move);
        
        // Test invalid moves
        move = MoveEncoder.createMove(-1, 0, 0, 1);
        assertEquals(0, move);
        
        move = MoveEncoder.createMove(0, -1, 0, 1);
        assertEquals(0, move);
        
        move = MoveEncoder.createMove(0, 0, 9, 1);
        assertEquals(0, move);
        
        move = MoveEncoder.createMove(0, 0, 0, 10);
        assertEquals(0, move);
    }

    @Test
    public void testIsValidMove() {
        // Test valid move
        // a0 = 0*9 + 0 = 0, a1 = 1*9 + 0 = 9
        assertTrue(MoveEncoder.isValidMove((short) ((0 << 7) | 9)));
        
        // Test invalid moves
        assertFalse(MoveEncoder.isValidMove((short) 0)); // none
        assertFalse(MoveEncoder.isValidMove((short) 129)); // null move
        assertFalse(MoveEncoder.isValidMove((short) ((0 << 7) | 0))); // same square
    }
}