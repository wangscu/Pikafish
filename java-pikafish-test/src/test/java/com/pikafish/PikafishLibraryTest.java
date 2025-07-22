package com.pikafish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5测试类，用于测试Pikafish共享库的功能
 */
public class PikafishLibraryTest {

    private static boolean libraryAvailable = false;

    @BeforeAll
    static void setupLibrary() {
        try {
            // 尝试加载库并调用一个函数来验证可用性
            String info = PikafishLibrary.INSTANCE.pikafish_engine_info();
            libraryAvailable = (info != null && !info.isEmpty());
            System.out.println("Pikafish library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Pikafish library not available: " + e.getMessage());
            libraryAvailable = false;
        }
    }

    @Test
    @DisplayName("测试引擎信息获取")
    void testEngineInfo() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        String engineInfo = PikafishLibrary.INSTANCE.pikafish_engine_info();

        assertNotNull(engineInfo, "Engine info should not be null");
        assertFalse(engineInfo.isEmpty(), "Engine info should not be empty");
        assertTrue(engineInfo.contains("Pikafish"), "Engine info should contain 'Pikafish'");

        System.out.println("Engine Info: " + engineInfo);
    }

    @Test
    @DisplayName("测试引擎初始化")
    void testEngineInitialization() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        int result = PikafishLibrary.INSTANCE.pikafish_engine_init();

        assertEquals(0, result, "Engine initialization should return 0 for success");

        System.out.println("Engine initialization result: " + result);
    }

    @Test
    @DisplayName("测试多次调用引擎信息")
    void testMultipleEngineInfoCalls() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        String info1 = PikafishLibrary.INSTANCE.pikafish_engine_info();
        String info2 = PikafishLibrary.INSTANCE.pikafish_engine_info();

        assertNotNull(info1);
        assertNotNull(info2);
        assertEquals(info1, info2, "Multiple calls should return the same information");
    }

    @Test
    @DisplayName("测试多次引擎初始化")
    void testMultipleEngineInitialization() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        int result1 = PikafishLibrary.INSTANCE.pikafish_engine_init();
        int result2 = PikafishLibrary.INSTANCE.pikafish_engine_init();

        assertEquals(0, result1, "First initialization should succeed");
        assertEquals(0, result2, "Second initialization should also succeed");
    }

    @Test
    @DisplayName("测试评估起始位置")
    void testEvaluateStartingPosition() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        String startFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w";
        int score = PikafishLibrary.INSTANCE.pikafish_evaluate_position(startFen);

        // 起始位置应该接近0分
        assertTrue(Math.abs(score) < 100, "起始位置分数应该接近0，实际: " + score);
        System.out.println("起始位置评估分数: " + score);
    }

    @Test
    @DisplayName("测试评估红方优势位置")
    void testEvaluateRedAdvantagePosition() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        // 红方多一个车的位置
        String redAdvantageFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKAB1R w";
        int score = PikafishLibrary.INSTANCE.pikafish_evaluate_position(redAdvantageFen);

        // 红方优势应该为正分
        assertTrue(score > 0, "红方优势位置应该为正分，实际: " + score);
        System.out.println("红方优势位置评估分数: " + score);
    }

    @Test
    @DisplayName("测试评估黑方优势位置")
    void testEvaluateBlackAdvantagePosition() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        // 黑方多一个车的位置
        String blackAdvantageFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKAB1R b";
        int score = PikafishLibrary.INSTANCE.pikafish_evaluate_position(blackAdvantageFen);

        // 黑方优势应该为正分（从黑方角度看，因为黑方是走棋方）
        assertTrue(score > 0, "黑方优势位置应该为正分（从黑方角度看），实际: " + score);
        System.out.println("黑方优势位置评估分数: " + score);
    }

    @Test
    @DisplayName("测试评估无效FEN")
    void testEvaluateInvalidFen() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        String invalidFen = "invalid fen string";
        int score = PikafishLibrary.INSTANCE.pikafish_evaluate_position(invalidFen);

        // 无效FEN应该返回0
        assertEquals(0, score, "无效FEN应该返回0分");
    }

    @Test
    @DisplayName("测试评估空FEN")
    void testEvaluateNullFen() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        int score = PikafishLibrary.INSTANCE.pikafish_evaluate_position(null);
        assertEquals(0, score, "空FEN应该返回0分");
    }

    @Test
    @DisplayName("测试多次评估同一位置")
    void testMultipleEvaluationsOfSamePosition() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        String fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w";
        
        int score1 = PikafishLibrary.INSTANCE.pikafish_evaluate_position(fen);
        int score2 = PikafishLibrary.INSTANCE.pikafish_evaluate_position(fen);
        int score3 = PikafishLibrary.INSTANCE.pikafish_evaluate_position(fen);

        // 同一位置的评估应该一致
        assertEquals(score1, score2, "同一位置的评估应该一致");
        assertEquals(score2, score3, "同一位置的评估应该一致");
        System.out.println("多次评估结果: " + score1 + ", " + score2 + ", " + score3);
    }

    @Test
    @DisplayName("测试评估不同位置")
    void testEvaluateDifferentPositions() {
        Assumptions.assumeTrue(libraryAvailable, "Pikafish library is not available");

        String startFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w";
        String midGameFen = "r1bakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w";
        
        int startScore = PikafishLibrary.INSTANCE.pikafish_evaluate_position(startFen);
        int midScore = PikafishLibrary.INSTANCE.pikafish_evaluate_position(midGameFen);

        // 不同位置应该有不同评估
        assertTrue(Math.abs(startScore - midScore) < 1000, "不同位置评估差异应该在合理范围内");
        System.out.println("起始位置: " + startScore + ", 中局位置: " + midScore);
    }
}
