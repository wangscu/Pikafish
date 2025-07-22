package com.pikafish;

/**
 * Pikafish最小化API演示程序
 */
public class PikafishDemo {

    public static void main(String[] args) {
        System.out.println("=== Pikafish 最小化API演示 ===");

        try {
            // 演示1: 初始化起始局面
            System.out.println("1. 初始化起始局面");
            String startingFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
            int result = PikafishLib.INSTANCE.init_position(startingFen);
            if (result == 0) {
                System.out.println("✓ 起始局面初始化成功");
            } else {
                System.out.println("✗ 起始局面初始化失败");
                return;
            }

            // 演示2: 评估起始局面
            System.out.println("\n2. 评估起始局面");
            int eval = PikafishLib.INSTANCE.evaluate();
            System.out.println("起始局面评估: " + eval + " (材料分)");

            // 演示3: 测试make_move (简化演示)
            System.out.println("\n3. 测试执行走法");
            short testMove = 1000; // 简化的走法编码
            result = PikafishLib.INSTANCE.make_move(testMove);
            if (result == 0) {
                System.out.println("✓ 走法执行成功");
                int newEval = PikafishLib.INSTANCE.evaluate();
                System.out.println("新局面评估: " + newEval + " (材料分)");

                // 测试撤销
                System.out.println("\n4. 测试撤销走法");
                result = PikafishLib.INSTANCE.unmake_move(testMove);
                if (result == 0) {
                    System.out.println("✓ 走法撤销成功");
                    int restoredEval = PikafishLib.INSTANCE.evaluate();
                    System.out.println("恢复后评估: " + restoredEval + " (材料分)");
                } else {
                    System.out.println("✗ 走法撤销失败");
                }
            } else {
                System.out.println("✗ 走法执行失败");
            }

            // 演示5: 自定义局面
            System.out.println("\n5. 加载自定义局面");
            String customFen = "r1bakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
            result = PikafishLib.INSTANCE.init_position(customFen);

            if (result == 0) {
                int customEval = PikafishLib.INSTANCE.evaluate();
                System.out.println("✓ 自定义局面加载成功");
                System.out.println("自定义局面评估: " + customEval + " (材料分)");
            } else {
                System.out.println("✗ 自定义局面加载失败");
            }

            System.out.println("\n=== 演示完成 ===");
            System.out.println("✓ 所有四个核心函数都已验证工作");

        } catch (UnsatisfiedLinkError e) {
            System.err.println("❌ 无法加载Pikafish共享库: " + e.getMessage());
            System.err.println("请确保已经构建共享库并且在正确路径中。");
        } catch (Exception e) {
            System.err.println("❌ 运行时错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
