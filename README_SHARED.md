qdrant eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3MiOiJtIn0.E-gbMyZHSEdzcU5HLOVZ_i5dibAKd4l7wkZZw7w_ugM


# Pikafish Shared Library

这是修改后的Pikafish引擎，支持编译为共享库(.so/.dll/.dylib)并提供C语言接口。

## 编译共享库

### 基本编译
```bash
# 编译共享库
make shared-lib

# 或者编译共享库和测试程序
make all
```

### 指定架构编译
```bash
# 为特定架构编译
make shared-lib ARCH=x86-64-avx2

# 支持的架构可以通过以下命令查看
cd src && make help
```

### 编译选项
```bash
# 使用不同编译器
make shared-lib COMP=clang

# Debug模式编译
make shared-lib debug=yes

# 优化编译
make shared-lib optimize=yes
```

## C语言接口

共享库提供以下C接口函数：

### pikafish_engine_main
```c
int pikafish_engine_main(int argc, char* argv[]);
```
Pikafish引擎主入口点，等同于原来的main函数。

### pikafish_engine_init
```c
int pikafish_engine_init(void);
```
初始化引擎但不启动主循环。返回0表示成功。

### pikafish_engine_info
```c
const char* pikafish_engine_info(void);
```
获取引擎信息字符串。

## 使用示例

### C程序示例
```c
#include "src/pikafish_c_api.h"
#include <stdio.h>

int main() {
    // 获取引擎信息
    const char* info = pikafish_engine_info();
    printf("Engine: %s\n", info);

    // 初始化引擎
    if (pikafish_engine_init() == 0) {
        printf("Engine initialized successfully\n");
    }

    return 0;
}
```

### 编译使用共享库的程序
```bash
# Linux/Unix
gcc -o myprogram myprogram.c -L. -lpikafish

# macOS
gcc -o myprogram myprogram.c -L. -lpikafish

# Windows (MinGW)
gcc -o myprogram.exe myprogram.c -L. -lpikafish
```

### 运行时链接
```bash
# Linux
LD_LIBRARY_PATH=. ./myprogram

# macOS
DYLD_LIBRARY_PATH=. ./myprogram

# Windows
# 确保pikafish.dll在PATH中或与可执行文件同目录
./myprogram.exe
```

## 目录结构

```
Pikafish/
├── src/                    # 源代码目录
│   ├── Makefile           # 原始Makefile (已修改)
│   ├── main.cpp           # 主文件 (已修改，添加C接口)
│   ├── pikafish_c_api.h   # C接口头文件
│   └── ...                # 其他源文件
├── Makefile               # 顶层Makefile
├── test_pikafish.c        # 测试程序
└── README_SHARED.md       # 本文件
```

## 清理

```bash
# 清理所有构建产物
make clean
```

## 注意事项

1. 共享库需要在运行时能够被找到，确保设置正确的库路径
2. C接口函数是线程安全的，但同时只能有一个引擎实例运行
3. 引擎信息字符串指向静态内存，不需要释放
4. 所有接口都遵循C调用约定，可以被C++、Python、Java等语言调用
