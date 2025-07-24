# Java版中国象棋炮攻击和移动生成器

## 概述

这个项目实现了中国象棋中炮的攻击和移动生成功能，类似于Pikafish引擎中的C++实现。主要特性包括：

1. **Bitboard类** - 用于表示棋盘状态的位板实现
2. **CannonAttackCalculator类** - 炮攻击计算核心算法
3. **CannonMoveGenerator类** - 炮移动列表生成器
4. **PrecomputedCannonAttacks类** - 预计算攻击表（类似魔法位板）
5. **Move类** - 象棋移动表示类

## 类结构说明

### Bitboard类
使用两个long值表示90个格子的中国象棋棋盘（9x10）。

主要方法：
- `getBit(int square)` - 获取指定格子的位值
- `setBit(int square)` - 设置指定格子的位值
- `and(Bitboard other)` - 与操作
- `or(Bitboard other)` - 或操作
- `andNot(Bitboard other)` - 与非操作
- `getFirstSquare()` - 获取第一个设置位的位置
- `getNextSquare(int prevSquare)` - 获取下一个设置位的位置

### CannonAttackCalculator类
计算炮的攻击位图，支持普通移动和攻击移动两种模式。

主要方法：
- `calculateAttacks(int fromSquare, Bitboard occupied, boolean isCapture)` - 计算炮的攻击位图
- `calculateSlidingAttacks(int fromSquare, Bitboard occupied)` - 计算炮的滑动攻击
- `calculateRelevantOccupancy(int square)` - 计算相关占领位

### CannonMoveGenerator类
生成炮的所有合法移动。

主要方法：
- `generateMoves(int fromSquare, Bitboard occupied, Bitboard friendlyPieces, Bitboard enemyPieces)` - 生成所有合法移动
- `generateCaptures(int fromSquare, Bitboard occupied, Bitboard enemyPieces)` - 生成捕获移动
- `generateQuiets(int fromSquare, Bitboard occupied)` - 生成普通移动

### PrecomputedCannonAttacks类
预计算攻击表实现，类似于Pikafish中的魔法位板技术。

主要方法：
- `getAttacks(int square, Bitboard occupied)` - 获取预计算的攻击位图

### Move类
表示象棋移动。

主要方法：
- `Move(int from, int to, boolean isCapture)` - 构造函数
- `toString()` - 转换为字符串表示

## 使用示例

```java
// 创建棋盘状态
Bitboard occupied = new Bitboard();    // 所有棋子
Bitboard redPieces = new Bitboard();   // 红方棋子
Bitboard blackPieces = new Bitboard(); // 黑方棋子

// 设置红方炮在e5位置
int redCannonPos = 40; // e5
redPieces.setBit(redCannonPos);
occupied.setBit(redCannonPos);

// 在e7位置放置红方兵作为炮架
int hurdlePos = 58; // e7
redPieces.setBit(hurdlePos);
occupied.setBit(hurdlePos);

// 在e9位置放置黑方车作为攻击目标
int targetPos = 76; // e9
blackPieces.setBit(targetPos);
occupied.setBit(targetPos);

// 生成红方炮的所有合法移动
List<Move> moves = CannonMoveGenerator.generateMoves(
    redCannonPos, occupied, redPieces, blackPieces);

// 打印移动
for (Move move : moves) {
    System.out.println(move);
}
```

## 炮的移动规则

中国象棋中炮的移动规则：
1. **普通移动**：沿直线（横、竖）移动，不能跳过任何棋子
2. **攻击移动**：沿直线移动，必须跳过一个且仅一个棋子（炮架），然后攻击目标棋子

## 性能优化

1. **位运算优化**：使用long类型进行64位并行计算
2. **预计算表**：对于固定大小的棋盘，预计算所有可能的攻击模式
3. **魔法位板技术**：使用哈希技术快速查找预计算的攻击模式

## 测试

项目包含以下测试类：
- `CannonMoveTest` - 基本功能测试
- `CannonDemo` - 演示程序

运行测试：
```bash
javac com/pikafish/*.java
java com/pikafish/CannonDemo