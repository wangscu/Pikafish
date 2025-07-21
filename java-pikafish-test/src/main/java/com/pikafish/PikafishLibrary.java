package com.pikafish;

import java.io.File;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * JNA接口，用于调用Pikafish共享库的C函数
 *
 * 这个接口映射了pikafish_c_api.h中定义的C函数：
 * - pikafish_engine_main: 主入口点
 * - pikafish_engine_init: 初始化引擎
 * - pikafish_engine_info: 获取引擎信息
 */
public interface PikafishLibrary extends Library {

    /**
     * Pikafish引擎主入口点，等同于原来的main函数
     *
     * @param argc 命令行参数数量
     * @param argv 命令行参数数组
     * @return 退出代码
     */
    int pikafish_engine_main(int argc, String[] argv);

    /**
     * 初始化Pikafish引擎但不启动主循环
     *
     * @return 0表示成功
     */
    int pikafish_engine_init();

    /**
     * 获取引擎信息字符串
     *
     * @return 指向引擎信息字符串的指针（静态内存）
     */
    String pikafish_engine_info();

    /**
     * 工厂类用于创建PikafishLibrary实例
     */
    class Factory {
        private static PikafishLibrary instance;
        private static Throwable lastException;

        /**
         * 获取PikafishLibrary实例
         * @return PikafishLibrary实例，如果加载失败则返回null
         */
        public static PikafishLibrary getInstance() {
            if (instance == null) {
                instance = createInstance();
            }
            return instance;
        }

        /**
         * 获取最后一次加载异常
         * @return 异常对象，如果没有异常则返回null
         */
        public static Throwable getLastException() {
            return lastException;
        }

        private static PikafishLibrary createInstance() {
            // 尝试多种方式加载库
            String[] possiblePaths = getPossibleLibraryPaths();

            for (String path : possiblePaths) {
                try {
                    System.out.println("Trying to load library from: " + path);
                    if (new File(path).exists()) {
                        return Native.load(path, PikafishLibrary.class);
                    }
                } catch (UnsatisfiedLinkError e) {
                    System.out.println("Failed to load from " + path + ": " + e.getMessage());
                    lastException = e;
                }
            }

            // 尝试使用库名称加载（依赖系统路径）
            try {
                System.out.println("Trying to load library by name: pikafish");
                return Native.load("pikafish", PikafishLibrary.class);
            } catch (UnsatisfiedLinkError e) {
                System.out.println("Failed to load by name: " + e.getMessage());
                lastException = e;
            }

            return null;
        }

        private static String[] getPossibleLibraryPaths() {
            String currentDir = System.getProperty("user.dir");
            String parentDir = new File(currentDir).getParent();

            if (Platform.isMac()) {
                return new String[] {
                    parentDir + "/libpikafish.dylib",
                    currentDir + "/libpikafish.dylib",
                    "./libpikafish.dylib",
                    "../libpikafish.dylib"
                };
            } else if (Platform.isWindows()) {
                return new String[] {
                    parentDir + "/pikafish.dll",
                    parentDir + "/libpikafish.dll",
                    currentDir + "/pikafish.dll",
                    currentDir + "/libpikafish.dll",
                    "./pikafish.dll",
                    "./libpikafish.dll",
                    "../pikafish.dll",
                    "../libpikafish.dll"
                };
            } else {
                // Linux and other Unix-like systems
                return new String[] {
                    parentDir + "/libpikafish.so",
                    currentDir + "/libpikafish.so",
                    "./libpikafish.so",
                    "../libpikafish.so"
                };
            }
        }
    }

    // 静态实例，通过工厂获取
    PikafishLibrary INSTANCE = Factory.getInstance();
}
