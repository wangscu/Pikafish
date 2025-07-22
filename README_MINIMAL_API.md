# Pikafish Minimal C/C++ Shared Library API

专为Java通过JNA直接调用而设计的最小化、零开销C/C++共享库。

## 概述

本API提供四个严格定义的函数，用于Pikafish中国象棋引擎的核心功能：

1. `init_position(const char* fen)` - 从FEN字符串初始化局面
2. `make_move(uint16_t move)` - 执行走法
3. `unmake_move(uint16_t move)` - 撤销走法
4. `evaluate()` - 评估当前局面

所有函数都是线程安全、可重入的，除全局Position对象外无其他副作用。

## 构建共享库

### 前提条件

- GCC 9.3+ 或 Clang 10.0+
- 支持C++17标准
- pthread库
- Pikafish NNUE模型文件 (`pikafish.nnue`)

### 编译命令

#### 自动构建 (推荐)
```bash
# 赋予执行权限
chmod +x build_minimal_lib.sh

# 构建共享库 (默认x86-64-avx2架构)
./build_minimal_lib.sh

# 或指定架构和编译器
./build_minimal_lib.sh x86-64-sse41-popcnt gcc
```

#### 手动构建

**Linux (.so):**
```bash
cd src
make ARCH=x86-64-avx2 COMP=gcc build
g++ -std=c++17 -fPIC -DNDEBUG -O3 -flto -c pikafish_minimal_api.cpp -o pikafish_minimal_api.o
g++ -shared -flto -o libpikafish.so pikafish_minimal_api.o \
    bitboard.o position.o evaluate.o misc.o movegen.o tune.o thread.o tt.o \
    nnue_misc.o nnue_accumulator.o network.o half_ka_v2_hm.o \
    $(find ./external -name '*.o') -lpthread -lm
```

**macOS (.dylib):**
```bash
cd src
make ARCH=x86-64-avx2 COMP=gcc build
g++ -std=c++17 -fPIC -DNDEBUG -O3 -flto -c pikafish_minimal_api.cpp -o pikafish_minimal_api.o
g++ -shared -flto -o libpikafish.dylib pikafish_minimal_api.o \
    [... 同上的对象文件 ...] -lpthread -lm
```

**Windows (.dll):**
```bash
cd src
make ARCH=x86-64-avx2 COMP=mingw build
g++ -std=c++17 -fPIC -DNDEBUG -O3 -flto -c pikafish_minimal_api.cpp -o pikafish_minimal_api.o
g++ -shared -flto -static-libgcc -static-libstdc++ -Wl,--no-undefined \
    -o pikafish.dll pikafish_minimal_api.o [... 对象文件 ...]
```

## Java接口使用

### Maven依赖

```xml
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>5.13.0</version>
</dependency>
```

### 基本使用示例

```java
package com.example;

import com.pikafish.PikafishLib;
import com.pikafish.PikafishLib.Utils;

public class ChessExample {
    public static void main(String[] args) {
        try {
            // 1. 初始化起始局面
            int result = Utils.initStartingPosition();
            if (result != 0) {
                System.err.println("Failed to initialize position");
                return;
            }

            // 2. 评估起始局面
            int eval = PikafishLib.INSTANCE.evaluate();
            System.out.println("Starting position evaluation: " +
                             Utils.formatEvaluation(eval));

            // 3. 执行走法 "e3e4" (兵三进一)
            short move = Utils.encodeMove("e3e4");
            result = PikafishLib.INSTANCE.make_move(move);
            if (result == 0) {
                System.out.println("Move executed: " + Utils.decodeMove(move));

                // 4. 评估新局面
                eval = PikafishLib.INSTANCE.evaluate();
                System.out.println("New evaluation: " +
                                 Utils.formatEvaluation(eval));

                // 5. 撤销走法
                result = PikafishLib.INSTANCE.unmake_move(move);
                if (result == 0) {
                    System.out.println("Move undone successfully");
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

### 高级使用示例

```java
// 从自定义FEN初始化
String customFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
int result = PikafishLib.INSTANCE.init_position(customFen);

// 批量执行走法
String[] moveSequence = {"e3e4", "e6e5", "b0c2", "b9c7"};
for (String moveStr : moveSequence) {
    short move = Utils.encodeMove(moveStr);
    if (PikafishLib.INSTANCE.make_move(move) != 0) {
        System.err.println("Illegal move: " + moveStr);
        break;
    }
    int eval = PikafishLib.INSTANCE.evaluate();
    System.out.println(moveStr + " -> " + Utils.formatEvaluation(eval));
}

// 撤销所有走法
for (int i = moveSequence.length - 1; i >= 0; i--) {
    short move = Utils.encodeMove(moveSequence[i]);
    PikafishLib.INSTANCE.unmake_move(move);
}
```

## API参考

### C函数签名

```c
// 初始化局面
int init_position(const char* fen);

// 执行走法 (move编码: (from_square << 7) | to_square)
int make_move(uint16_t move);

// 撤销走法
int unmake_move(uint16_t move);

// 评估局面 (返回分值，单位：厘兵)
int evaluate(void);
```

### 走法编码

中国象棋棋盘格子编号 (0-89):
- 纵线: a-i (0-8)
- 横线: 0-9 (红方视角从下到上)
- 格子编号 = 横线 × 9 + 纵线

走法编码为16位值:
- 位 7-13: 起始格子 (0-89)
- 位 0-6: 目标格子 (0-89)

### 错误代码

- `0`: 成功
- `-1`: 错误 (无效FEN、非法走法等)

## 线程安全

所有函数都使用互斥锁保护全局状态，可以安全地从多个线程调用。但是，同一时间只有一个线程可以操作引擎状态。

## 性能特点

- **零开销设计**: 直接调用原生C++代码，无JNI封装开销
- **最小依赖**: 仅需要pthread和数学库
- **高效评估**: 使用NNUE神经网络，评估速度极快
- **内存效率**: 全局状态管理，避免频繁内存分配

## 故障排除

### 常见问题

1. **库加载失败**
   - 确保共享库在系统路径或当前目录
   - 检查库文件权限和架构匹配

2. **NNUE模型未找到**
   - 确保 `pikafish.nnue` 在工作目录
   - 检查文件权限

3. **编译错误**
   - 验证GCC/Clang版本
   - 确保C++17支持
   - 检查依赖库安装

### 调试建议

```java
// 启用JNA调试信息
System.setProperty("jna.debug_load", "true");
System.setProperty("jna.debug_load.jna", "true");

// 检查库加载
try {
    PikafishLib lib = PikafishLib.INSTANCE;
    System.out.println("Library loaded successfully");
} catch (UnsatisfiedLinkError e) {
    System.err.println("Failed to load library: " + e.getMessage());
}
```

## 许可证

本软件基于GNU General Public License v3.0发布。详见[LICENSE](LICENSE)文件。

## 贡献

欢迎提交问题和拉取请求到Pikafish项目仓库。
