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
}
