package com.pikafish;

/**
 * Pikafish Java测试程序
 *
 * 这个程序演示如何在Java中使用JNA调用Pikafish共享库的C接口。
 * 功能与test_pikafish.c相同，但使用Java实现。
 */
public class PikafishTest {

    public static void main(String[] args) {
        System.out.println("Testing Pikafish C API from Java");
        System.out.println("==================================");

        // 检查库是否加载成功
        if (PikafishLibrary.INSTANCE == null) {
            System.err.println("Failed to load Pikafish library!");
            Throwable lastException = PikafishLibrary.Factory.getLastException();
            if (lastException != null) {
                System.err.println("Last error: " + lastException.getMessage());
            }
            System.err.println("Please ensure libpikafish.dylib (macOS) or libpikafish.so (Linux) is available");
            System.exit(1);
        }

        try {
            // 测试引擎信息获取
            testEngineInfo();

            // 测试引擎初始化
            testEngineInitialization();

            System.out.println("\nTest completed successfully!");

        } catch (Exception e) {
            System.err.println("Error during test execution: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 设置JNA库路径
     */
    private static void setupLibraryPath() {
        // 获取当前工作目录的父目录（假设共享库在上级目录）
        String currentDir = System.getProperty("user.dir");
        String parentDir = currentDir + "/..";

        // 设置库路径
        String existingPath = System.getProperty("java.library.path", "");
        String newPath = existingPath.isEmpty() ? parentDir : existingPath + ":" + parentDir;
        System.setProperty("java.library.path", newPath);

        System.out.println("Library path set to: " + newPath);
    }

    /**
     * 测试引擎信息获取
     */
    private static void testEngineInfo() {
        System.out.println("\n--- Testing Engine Info ---");
        try {
            String engineInfo = PikafishLibrary.INSTANCE.pikafish_engine_info();
            System.out.println("Engine Info: " + engineInfo);
        } catch (Exception e) {
            System.err.println("Failed to get engine info: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 测试引擎初始化
     */
    private static void testEngineInitialization() {
        System.out.println("\n--- Testing Engine Initialization ---");
        try {
            int result = PikafishLibrary.INSTANCE.pikafish_engine_init();
            System.out.println("Engine initialization result: " + result);

            if (result == 0) {
                System.out.println("✓ Engine initialized successfully");
            } else {
                System.out.println("✗ Engine initialization failed with code: " + result);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize engine: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 演示如何调用引擎主函数（注释掉因为会启动交互式循环）
     */
    @SuppressWarnings("unused")
    private static void demonstrateEngineMain() {
        System.out.println("\n--- Demonstrating Engine Main (commented out) ---");
        // 注意：这会启动引擎的交互式循环，在实际应用中需要谨慎使用
        // String[] engineArgs = {"pikafish"};
        // int exitCode = PikafishLibrary.INSTANCE.pikafish_engine_main(1, engineArgs);
        // System.out.println("Engine main exit code: " + exitCode);
    }
}
