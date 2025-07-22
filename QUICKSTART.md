# Pikafish 最小化API - 快速开始

## 30秒构建指南

```bash
# 1. 构建共享库
./build_minimal_lib.sh

# 2. 测试API (可选)
make -f Makefile.minimal test
LD_LIBRARY_PATH=. ./test_minimal_api
```

## Java使用示例

```java
// 添加JNA依赖: net.java.dev.jna:jna:5.13.0

import com.pikafish.PikafishLib;
import com.pikafish.PikafishLib.Utils;

// 初始化
Utils.initStartingPosition();

// 走子
short move = Utils.encodeMove("e3e4");
PikafishLib.INSTANCE.make_move(move);

// 评估
int eval = PikafishLib.INSTANCE.evaluate();
System.out.println("评估: " + Utils.formatEvaluation(eval));

// 撤销
PikafishLib.INSTANCE.unmake_move(move);
```

## 核心API

| 函数 | 功能 | 返回值 |
|------|------|--------|
| `init_position(fen)` | 设置局面 | 0=成功, -1=失败 |
| `make_move(move)` | 执行走法 | 0=成功, -1=非法 |
| `unmake_move(move)` | 撤销走法 | 0=成功, -1=错误 |
| `evaluate()` | 评估局面 | 分值(厘兵) |

## 编译选项

**手动编译 (Linux):**
```bash
cd src && make build
g++ -fPIC -shared -O3 -o libpikafish.so pikafish_minimal_api.cpp *.o external/*.o -lpthread
```

**不同平台:**
- Linux: `libpikafish.so`
- macOS: `libpikafish.dylib`
- Windows: `pikafish.dll`

## 文件清单

- `src/pikafish_minimal_api.h` - C头文件
- `src/pikafish_minimal_api.cpp` - C实现
- `PikafishLib.java` - Java接口
- `build_minimal_lib.sh` - 构建脚本
- `README_MINIMAL_API.md` - 详细文档

确保工作目录包含 `pikafish.nnue` 模型文件。
