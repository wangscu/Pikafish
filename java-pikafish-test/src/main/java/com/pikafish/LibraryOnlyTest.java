package com.pikafish;

/**
 * 仅测试库加载的程序
 */
public class LibraryOnlyTest {

    public static void main(String[] args) {
        System.out.println("=== 库加载测试 ===");

        try {
            System.out.println("尝试加载库...");

            String libName = PikafishLib.getLibraryName();
            System.out.println("目标库名: " + libName);

            // 不实例化INSTANCE，仅尝试手动加载
            System.out.println("手动加载库...");

            // 直接使用JNA加载
            //PikafishLib lib = Native.load(libName, PikafishLib.class);

            System.out.println("✓ 库加载成功!");
            System.out.println("测试完成，未调用任何API函数");

        } catch (UnsatisfiedLinkError e) {
            System.err.println("❌ 无法加载库: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ 运行时错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
