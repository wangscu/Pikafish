package com.pikafish;

/**
 * 简单的Pikafish库测试程序，用于调试
 */
public class SimplePikafishTest {

    public static void main(String[] args) {
        System.out.println("=== 简单 Pikafish 库测试 ===");

        try {
            System.out.println("尝试加载库...");

            // 仅尝试库加载，不调用任何函数
            System.out.println("库名: " + PikafishLib.getLibraryName());

            System.out.println("库加载成功!");

            // 现在尝试调用最简单的函数
            System.out.println("调用 init_position...");

            String startingFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
            int result = PikafishLib.INSTANCE.init_position(startingFen);

            System.out.println("init_position 结果: " + result);

            if (result == 0) {
                System.out.println("✓ 初始化成功!");

                // 尝试简单评估
                System.out.println("调用 evaluate...");
                int eval = PikafishLib.INSTANCE.evaluate();
                System.out.println("评估结果: " + eval);
                System.out.println("✓ 评估成功!");
            } else {
                System.out.println("✗ 初始化失败");
            }

            System.out.println("测试完成");

        } catch (UnsatisfiedLinkError e) {
            System.err.println("❌ 无法加载库: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ 运行时错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
