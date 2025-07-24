# Pikafish Java Test

这是一个使用JNA (Java Native Access) 调用Pikafish共享库的Java测试程序。该项目使用Maven进行依赖管理和构建。

## 项目结构

```
java-pikafish-test/
├── pom.xml                                    # Maven项目配置
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── pikafish/
│   │               ├── PikafishLibrary.java   # JNA接口定义
│   │               ├── GenerateMovesExample.java # 合法走法生成示例程序
│   │               └── PikafishTest.java      # 主测试程序
│   └── test/
│       └── java/
│           └── com/
│               └── pikafish/
│                   └── PikafishLibraryTest.java # JUnit测试
└── README.md                                  # 本文件
```

## 依赖要求

- Java 11 或更高版本
- Maven 3.6+
- Pikafish共享库（libpikafish.dylib/libpikafish.so）

## 功能特性

### PikafishLibrary.java
JNA接口类，映射了Pikafish C API的以下函数：
- `pikafish_engine_main(int argc, String[] argv)` - 引擎主入口
- `pikafish_engine_init()` - 初始化引擎
- `pikafish_engine_info()` - 获取引擎信息
- `pikafish_generate_legal_moves()` - 生成当前局面的合法走法（指针版本）
- `pikafish_generate_legal_moves_array(short[] moves)` - 生成当前局面的合法走法（数组版本）

### PikafishTest.java
主测试程序，演示如何：
- 加载和使用Pikafish共享库
- 调用各种API函数
- 处理错误情况

### GenerateMovesExample.java
合法走法生成示例程序，演示如何：
- 使用指针版本和数组版本的合法走法生成功能
- 比较两种方法的结果
- 解析和显示生成的走法

### PikafishLibraryTest.java
JUnit 5测试套件，包含：
- 引擎信息获取测试
- 引擎初始化测试
- 多次调用稳定性测试
- 合法走法生成功能测试

## 构建和运行

### 1. 前置条件
确保Pikafish共享库已编译并在上级目录中：
```bash
cd ..
make shared-lib ARCH=apple-silicon  # 或适合您系统的架构
```

### 2. 编译项目
```bash
cd java-pikafish-test
mvn clean compile
```

### 3. 运行主程序
```bash
mvn exec:java
```

或者直接使用java命令：
```bash
mvn clean compile
java -Djava.library.path=.. -cp target/classes com.pikafish.PikafishTest
```

### 4. 运行合法走法生成示例程序
使用默认配置运行：
```bash
mvn exec:java@run-generate-moves-example
```

或者直接使用java命令：
```bash
mvn clean compile
java -Djava.library.path=.. -cp target/classes com.pikafish.GenerateMovesExample
```

### 5. 运行测试
```bash
mvn test
```

### 5. 运行特定测试
```bash
mvn test -Dtest=PikafishLibraryTest
```

## 输出示例

### 合法走法生成示例程序输出
```
Legal moves (pointer version): 20
Legal moves (array version): 20
Both methods returned the same number of moves - SUCCESS

First 5 moves from pointer version:
  0: 1664 -> a0a1
  1: 1728 -> a0b0
  2: 1792 -> a0c0
  3: 1920 -> a0e0
  4: 1984 -> a0f0

First 5 moves from array version:
  0: 1664 -> a0a1
  1: 1728 -> a0b0
  2: 1792 -> a0c0
  3: 1920 -> a0e0
  4: 1984 -> a0f0
```

### 主程序输出
```
Testing Pikafish C API from Java
==================================
Library path set to: /path/to/pikafish

--- Testing Engine Info ---
Engine Info: Pikafish dev-20250720-1f451f43 by the Pikafish developers (see AUTHORS file)

--- Testing Engine Initialization ---
Engine initialization result: 0
✓ Engine initialized successfully

Test completed successfully!
```

### 测试输出
```
[INFO] Running com.pikafish.PikafishLibraryTest
Pikafish library loaded successfully
Engine Info: Pikafish dev-20250720-1f451f43 by the Pikafish developers (see AUTHORS file)
Engine initialization result: 0
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## 故障排除

### 库加载错误
如果遇到 `UnsatisfiedLinkError`：

1. **检查库文件位置**：
   ```bash
   ls -la ../libpikafish.dylib  # macOS
   ls -la ../libpikafish.so     # Linux
   ```

2. **设置库路径**：
   ```bash
   export DYLD_LIBRARY_PATH=..  # macOS
   export LD_LIBRARY_PATH=..    # Linux
   mvn exec:java
   ```

3. **检查架构匹配**：
   ```bash
   file ../libpikafish.dylib    # 应该显示与系统匹配的架构
   ```

### 编译错误
确保Java版本正确：
```bash
java -version  # 应该显示11或更高版本
mvn -version
```

## 扩展使用

### 集成到其他项目
1. 复制 `PikafishLibrary.java` 到您的项目
2. 在 `pom.xml` 中添加JNA依赖
3. 确保共享库在运行时可访问

### 高级用法
- 实现UCI协议通信
- 集成到图形界面象棋程序
- 构建REST API服务
- 创建分析工具

## 注意事项

1. **线程安全**：Pikafish库可能不是线程安全的，在多线程环境中使用时要小心
2. **内存管理**：JNA会自动处理内存管理，但C函数返回的字符串指针指向静态内存
3. **平台兼容性**：确保共享库与Java运行时的架构匹配

## 许可证

本项目遵循与Pikafish相同的GPL v3许可证。
