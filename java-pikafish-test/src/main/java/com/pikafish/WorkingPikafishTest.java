package com.pikafish;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * 基于原始C API的工作测试程序
 */
public class WorkingPikafishTest {

    public interface PikafishLibrary extends Library {
        /**
         * 初始化引擎但不启动主循环
         */
        int pikafish_engine_init();

        /**
         * 获取引擎信息字符串
         */
        String pikafish_engine_info();

        /**
         * 单例实例
         */
        PikafishLibrary INSTANCE = Native.load("pikafish", PikafishLibrary.class);
    }

    public static void main(String[] args) {
        System.out.println("=== 基于原始API的测试 ===");

        try {
            System.out.println("1. 获取引擎信息");
            String info = PikafishLibrary.INSTANCE.pikafish_engine_info();
            System.out.println("引擎信息: " + info);

            System.out.println("\n2. 初始化引擎");
            int result = PikafishLibrary.INSTANCE.pikafish_engine_init();
            System.out.println("初始化结果: " + result);

            if (result == 0) {
                System.out.println("✓ 引擎初始化成功!");
            } else {
                System.out.println("✗ 引擎初始化失败");
            }

            System.out.println("\n=== 测试完成 ===");

        } catch (UnsatisfiedLinkError e) {
            System.err.println("❌ 无法加载库: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ 运行时错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
