package com.pikafish;

import java.io.File;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * JNA接口，用于调用Pikafish共享库的C函数
 *
 * 这个接口映射了pikafish_c_api.h中定义的C函数：
 * - pikafish_engine_init: 初始化引擎并获取引擎信息
 * - pikafish_evaluate_position: 评估FEN棋局位置
 * - pikafish_is_side_in_check: 检查指定方是否被将军
 * - pikafish_init_position: 从FEN字符串初始化棋局位置
 * - pikafish_do_move: 应用合法移动
 * - pikafish_generate_legal_moves: 生成所有合法移动
 * - pikafish_evaluate: 评估当前位置
 * - pikafish_get_fen: 获取当前局面的FEN表示
 * - pikafish_undo_move: 撤销最后一步移动
 * - pikafish_encode_move: 将坐标表示的移动编码为内部表示
 * - pikafish_decode_move: 将内部表示的移动解码为坐标表示
 */
public interface PikafishLibrary extends Library {



    /**
     * 获取引擎信息字符串
     *
     * @return 指向引擎信息字符串的指针（静态内存）
     */
    String pikafish_engine_init();

    /**
     * 评估FEN棋局位置
     *
     * @param index 引擎实例索引
     * @param fen FEN字符串表示的棋局位置
     * @return 评估分数（以分为单位，正值表示对当前走棋方有利）
     */
    int pikafish_evaluate_position(int index, String fen);

    /**
     * 从FEN字符串初始化棋局位置
     *
     * @param index 引擎实例索引
     * @param fen FEN字符串表示的棋局位置
     * @return 0表示成功，非0表示失败
     */
    int pikafish_init_position(int index, String fen);

    /**
     * 应用合法移动
     *
     * @param index 引擎实例索引
     * @param move 16位无符号整数表示的移动
     * @return 新位置的哈希值，0表示失败
     */
    long pikafish_do_move(int index, short move);

    /**
     * 评估当前位置
     *
     * @param index 引擎实例索引
     * @return 评估分数（以分为单位）
     */
    int pikafish_evaluate(int index);

    /**
     * 获取当前局面的FEN表示
     *
     * @param index 引擎实例索引
     * @return 当前局面的FEN字符串
     */
    String pikafish_get_fen(int index);

    /**
     * 将坐标表示的移动编码为内部表示
     * @param move_str - 坐标表示的移动 (例如 "e2e4")
     * @return - 编码后的移动(uint16_t)，如果无效则返回0
     */
    short pikafish_encode_move(String move_str);

    /**
     * 将内部表示的移动解码为坐标表示
     * @param move - 编码后的移动(uint16_t)
     * @return - 坐标表示的移动 (例如 "e2e4")，如果无效则返回null
     */
    String pikafish_decode_move(short move);
    
   /**
    * Generate a list of legal moves for the current position (array version)
    * @param index - 引擎实例索引
    * @param moves - array to store legal moves (must be at least MAX_MOVES + 1 in size)
    * @return - number of legal moves generated
    */
   int pikafish_generate_legal_moves(int index, short[] moves);

   /**
    * 撤销最后一步移动
    *
    * @param index 引擎实例索引
    * @param move 16位无符号整数表示的要撤销的移动
    * @return 新位置的哈希值，0表示失败
    */
   long pikafish_undo_move(int index, short move);

    /**
     * 检查指定方是否被将军
     *
     * @param index 引擎实例索引
     * @param is_white 1表示检查白方，0表示检查黑方
     * @return 1表示被将军，0表示未被将军
     */
    int pikafish_is_side_in_check(int index, int is_white);
    
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
                    "libpikafish.dylib",
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
