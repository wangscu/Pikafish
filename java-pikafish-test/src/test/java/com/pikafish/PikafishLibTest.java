package com.pikafish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5测试类，用于测试Pikafish最小化API的四个核心函数
 */
public class PikafishLibTest {

    private static boolean libraryAvailable = false;

    @BeforeAll
    static void setupLibrary() {
        try {
            // 尝试初始化起始局面来验证库可用性
            int result = PikafishLib.Utils.initStartingPosition();
            libraryAvailable = (result == 0);
            System.out.println("Pikafish minimal API library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Pikafish library not available: " + e.getMessage());
            System.err.println("Make sure the shared library is built and in the correct path");
            libraryAvailable = false;
        } catch (Exception e) {
            System.err.println("Error testing library: " + e.getMessage());
            libraryAvailable = false;
        }
    }

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(libraryAvailable,
            "Pikafish library is not available. Please build the shared library first.");

        // 重置到起始局面
        PikafishLib.Utils.initStartingPosition();
    }

    @Test
    @DisplayName("测试起始局面初始化")
    void testInitStartingPosition() {
        int result = PikafishLib.Utils.initStartingPosition();
        assertEquals(0, result, "Starting position initialization should succeed");
    }

    @Test
    @DisplayName("测试自定义FEN局面初始化")
    void testInitCustomPosition() {
        String customFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
        int result = PikafishLib.INSTANCE.init_position(customFen);
        assertEquals(0, result, "Custom FEN position initialization should succeed");
    }

    @Test
    @DisplayName("测试无效FEN字符串")
    void testInvalidFEN() {
        int result = PikafishLib.INSTANCE.init_position("invalid_fen_string");
        assertEquals(-1, result, "Invalid FEN should be rejected");

        result = PikafishLib.INSTANCE.init_position(null);
        assertEquals(-1, result, "Null FEN should be rejected");

        result = PikafishLib.INSTANCE.init_position("");
        assertEquals(-1, result, "Empty FEN should be rejected");
    }

    @Test
    @DisplayName("测试起始局面评估")
    void testEvaluateStartingPosition() {
        PikafishLib.Utils.initStartingPosition();
        int eval = PikafishLib.INSTANCE.evaluate();

        // 起始局面的评估应该接近平衡（不会完全是0，但应该在合理范围内）
        assertTrue(Math.abs(eval) < 500,
            "Starting position evaluation should be close to balanced, got: " + eval);

        System.out.println("Starting position evaluation: " +
                         PikafishLib.Utils.formatEvaluation(eval));
    }

    @Test
    @DisplayName("测试走法编码和解码")
    void testMoveEncoding() {
        // 测试从坐标编码
        short move1 = PikafishLib.Utils.encodeMove(4, 3, 4, 4); // e3e4
        assertEquals("e3e4", PikafishLib.Utils.decodeMove(move1));

        // 测试从字符串编码
        short move2 = PikafishLib.Utils.encodeMove("e3e4");
        assertEquals(move1, move2);
        assertEquals("e3e4", PikafishLib.Utils.decodeMove(move2));

        // 测试其他走法
        short move3 = PikafishLib.Utils.encodeMove("b0c2");
        assertEquals("b0c2", PikafishLib.Utils.decodeMove(move3));
    }

    @Test
    @DisplayName("测试无效走法编码")
    void testInvalidMoveEncoding() {
        assertThrows(IllegalArgumentException.class, () -> {
            PikafishLib.Utils.encodeMove("abc");
        }, "Short move string should throw exception");

        assertThrows(IllegalArgumentException.class, () -> {
            PikafishLib.Utils.encodeMove("abcde");
        }, "Long move string should throw exception");

        assertThrows(IllegalArgumentException.class, () -> {
            PikafishLib.Utils.encodeMove("z9a0");
        }, "Invalid file should throw exception");

        assertThrows(IllegalArgumentException.class, () -> {
            PikafishLib.Utils.encodeMove("a9a0");
        }, "Invalid rank should throw exception for rank 9->0 (this specific move might be invalid)");
    }

    @Test
    @DisplayName("测试合法走法执行")
    void testLegalMove() {
        PikafishLib.Utils.initStartingPosition();

        // 执行一个合法的兵走法：e3e4 (兵三进一)
        short move = PikafishLib.Utils.encodeMove("e3e4");
        int result = PikafishLib.INSTANCE.make_move(move);
        assertEquals(0, result, "Legal pawn move should succeed");

        // 评估新局面
        int eval = PikafishLib.INSTANCE.evaluate();
        System.out.println("Evaluation after e3e4: " + PikafishLib.Utils.formatEvaluation(eval));
    }

    @Test
    @DisplayName("测试非法走法执行")
    void testIllegalMove() {
        PikafishLib.Utils.initStartingPosition();

        // 尝试执行非法走法：让兵直接跳两格 e3e5
        short move = PikafishLib.Utils.encodeMove("e3e5");
        int result = PikafishLib.INSTANCE.make_move(move);
        assertEquals(-1, result, "Illegal move should be rejected");

        // 尝试将棋子移动到相同位置
        short sameSquareMove = PikafishLib.Utils.encodeMove("e3e3");
        result = PikafishLib.INSTANCE.make_move(sameSquareMove);
        assertEquals(-1, result, "Same square move should be rejected");
    }

    @Test
    @DisplayName("测试走法撤销")
    void testMoveUndo() {
        PikafishLib.Utils.initStartingPosition();

        // 获取初始评估
        int initialEval = PikafishLib.INSTANCE.evaluate();

        // 执行走法
        short move = PikafishLib.Utils.encodeMove("e3e4");
        int result = PikafishLib.INSTANCE.make_move(move);
        assertEquals(0, result, "Move should succeed");

        // 获取走法后的评估
        int afterMoveEval = PikafishLib.INSTANCE.evaluate();

        // 撤销走法
        result = PikafishLib.INSTANCE.unmake_move(move);
        assertEquals(0, result, "Move undo should succeed");

        // 验证局面恢复
        int restoredEval = PikafishLib.INSTANCE.evaluate();
        assertEquals(initialEval, restoredEval,
            "Position should be restored to initial state after undo");

        System.out.println("Initial eval: " + PikafishLib.Utils.formatEvaluation(initialEval));
        System.out.println("After move eval: " + PikafishLib.Utils.formatEvaluation(afterMoveEval));
        System.out.println("Restored eval: " + PikafishLib.Utils.formatEvaluation(restoredEval));
    }

    @Test
    @DisplayName("测试错误的撤销走法")
    void testWrongMoveUndo() {
        PikafishLib.Utils.initStartingPosition();

        // 执行一个走法
        short move1 = PikafishLib.Utils.encodeMove("e3e4");
        PikafishLib.INSTANCE.make_move(move1);

        // 尝试撤销不同的走法
        short move2 = PikafishLib.Utils.encodeMove("e6e5");
        int result = PikafishLib.INSTANCE.unmake_move(move2);
        assertEquals(-1, result, "Wrong move undo should be rejected");
    }

    @Test
    @DisplayName("测试没有走法时的撤销")
    void testUndoWithoutMove() {
        PikafishLib.Utils.initStartingPosition();

        // 尝试在没有走法的情况下撤销
        short move = PikafishLib.Utils.encodeMove("e3e4");
        int result = PikafishLib.INSTANCE.unmake_move(move);
        assertEquals(-1, result, "Undo without moves should be rejected");
    }

    @Test
    @DisplayName("测试连续走法序列")
    void testMoveSequence() {
        PikafishLib.Utils.initStartingPosition();

        String[] moveSequence = {"e3e4", "e6e5", "b0c2", "b9c7"};
        short[] encodedMoves = new short[moveSequence.length];

        // 执行一系列走法
        for (int i = 0; i < moveSequence.length; i++) {
            encodedMoves[i] = PikafishLib.Utils.encodeMove(moveSequence[i]);
            int result = PikafishLib.INSTANCE.make_move(encodedMoves[i]);
            assertEquals(0, result, "Move " + moveSequence[i] + " should succeed");

            int eval = PikafishLib.INSTANCE.evaluate();
            System.out.println("After " + moveSequence[i] + ": " +
                             PikafishLib.Utils.formatEvaluation(eval));
        }

        // 按相反顺序撤销所有走法
        for (int i = encodedMoves.length - 1; i >= 0; i--) {
            int result = PikafishLib.INSTANCE.unmake_move(encodedMoves[i]);
            assertEquals(0, result, "Undo " + moveSequence[i] + " should succeed");
        }
    }

    @Test
    @DisplayName("测试评估函数的一致性")
    void testEvaluationConsistency() {
        PikafishLib.Utils.initStartingPosition();

        // 多次调用评估函数应该返回相同结果
        int eval1 = PikafishLib.INSTANCE.evaluate();
        int eval2 = PikafishLib.INSTANCE.evaluate();
        int eval3 = PikafishLib.INSTANCE.evaluate();

        assertEquals(eval1, eval2, "Multiple evaluations should be consistent");
        assertEquals(eval2, eval3, "Multiple evaluations should be consistent");
    }

    @Test
    @DisplayName("测试工具类函数")
    void testUtilityFunctions() {
        // 测试评估格式化
        assertEquals("0.00", PikafishLib.Utils.formatEvaluation(0));
        assertEquals("+1.23", PikafishLib.Utils.formatEvaluation(123));
        assertEquals("-2.50", PikafishLib.Utils.formatEvaluation(-250));

        // 测试起始局面FEN
        assertNotNull(PikafishLib.Utils.STARTING_FEN);
        assertFalse(PikafishLib.Utils.STARTING_FEN.isEmpty());
        assertTrue(PikafishLib.Utils.STARTING_FEN.contains("rnbakabnr"));
    }

    @Test
    @DisplayName("压力测试 - 大量走法操作")
    void testStressOperations() {
        PikafishLib.Utils.initStartingPosition();

        // 执行多次相同的走法和撤销操作
        short move = PikafishLib.Utils.encodeMove("e3e4");

        for (int i = 0; i < 100; i++) {
            int makeResult = PikafishLib.INSTANCE.make_move(move);
            assertEquals(0, makeResult, "Make move should succeed on iteration " + i);

            int eval = PikafishLib.INSTANCE.evaluate();
            assertTrue(Math.abs(eval) < 5000, "Evaluation should be reasonable");

            int undoResult = PikafishLib.INSTANCE.unmake_move(move);
            assertEquals(0, undoResult, "Undo move should succeed on iteration " + i);
        }
    }
}
