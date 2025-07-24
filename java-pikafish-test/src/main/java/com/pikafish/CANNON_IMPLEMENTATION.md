# Java实现炮攻击和移动列表生成器

## 设计概述

基于Pikafish的C++实现，创建Java版本的炮攻击和移动列表生成器，使用位运算优化性能。

## 核心类设计

### 1. 位板类 (Bitboard)
```java
public class Bitboard {
    private long[] bits; // 使用两个long表示128位棋盘
    
    public Bitboard() {
        this.bits = new long[2];
    }
    
    public boolean getBit(int square) {
        int index = square / 64;
        int bit = square % 64;
        return (bits[index] & (1L << bit)) != 0;
    }
    
    public void setBit(int square) {
        int index = square / 64;
        int bit = square % 64;
        bits[index] |= (1L << bit);
    }
    
    public Bitboard and(Bitboard other) {
        Bitboard result = new Bitboard();
        result.bits[0] = this.bits[0] & other.bits[0];
        result.bits[1] = this.bits[1] & other.bits[1];
        return result;
    }
    
    public int countBits() {
        return Long.bitCount(bits[0]) + Long.bitCount(bits[1]);
    }
    
    public int getFirstSquare() {
        if (bits[0] != 0) return Long.numberOfTrailingZeros(bits[0]);
        if (bits[1] != 0) return 64 + Long.numberOfTrailingZeros(bits[1]);
        return -1;
    }
}
```

### 2. 炮攻击计算器
```java
public class CannonAttackCalculator {
    
    // 棋盘大小常量
    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 10;
    private static final int TOTAL_SQUARES = 90;
    
    // 方向常量
    private static final int NORTH = 9;
    private static final int SOUTH = -9;
    private static final int EAST = 1;
    private static final int WEST = -1;
    
    /**
     * 计算炮的攻击位图
     * @param fromSquare 起始格子
     * @param occupied 所有棋子的位图
     * @param isCapture 是否为攻击模式
     * @return 攻击位图
     */
    public static Bitboard calculateAttacks(int fromSquare, Bitboard occupied, boolean isCapture) {
        Bitboard attacks = new Bitboard();
        
        // 四个方向：北、南、东、西
        int[] directions = {NORTH, SOUTH, EAST, WEST};
        
        for (int direction : directions) {
            boolean hurdleFound = false;
            int current = fromSquare + direction;
            
            while (isValidSquare(current)) {
                // 检查是否超出边界
                if (isOutOfBounds(fromSquare, current, direction)) break;
                
                if (occupied.getBit(current)) {
                    if (!hurdleFound) {
                        // 第一个遇到的棋子作为炮架
                        hurdleFound = true;
                    } else {
                        // 第二个遇到的棋子可以攻击
                        if (isCapture) {
                            attacks.setBit(current);
                        }
                        break;
                    }
                } else {
                    // 空格子
                    if (!isCapture || !hurdleFound) {
                        attacks.setBit(current);
                    }
                }
                
                current += direction;
            }
        }
        
        return attacks;
    }
    
    private static boolean isValidSquare(int square) {
        return square >= 0 && square < TOTAL_SQUARES;
    }
    
    private static boolean isOutOfBounds(int from, int to, int direction) {
        // 检查是否跨越边界
        if (direction == EAST || direction == WEST) {
            int fromFile = from % BOARD_WIDTH;
            int toFile = to % BOARD_WIDTH;
            return Math.abs(toFile - fromFile) > 1;
        }
        return false;
    }
}
```

### 3. 移动列表生成器
```java
public class CannonMoveGenerator {
    
    public static class Move {
        public final int from;
        public final int to;
        public final boolean isCapture;
        
        public Move(int from, int to, boolean isCapture) {
            this.from = from;
            this.to = to;
            this.isCapture = isCapture;
        }
        
        @Override
        public String toString() {
            return String.format("%s%d-%s%d%s", 
                (char)('a' + from % 9), from / 9,
                (char)('a' + to % 9), to / 9,
                isCapture ? "x" : "");
        }
    }
    
    public static List<Move> generateMoves(int fromSquare, Bitboard occupied, 
                                          Bitboard friendlyPieces, Bitboard enemyPieces) {
        List<Move> moves = new ArrayList<>();
        
        // 生成普通移动（非攻击）
        Bitboard quietMoves = CannonAttackCalculator.calculateAttacks(
            fromSquare, occupied, false);
        quietMoves = quietMoves.andNot(occupied); // 移除被占据的格子
        
        // 添加普通移动
        for (int square = quietMoves.getFirstSquare(); 
             square != -1; 
             square = quietMoves.getNextSquare()) {
            moves.add(new Move(fromSquare, square, false));
        }
        
        // 生成攻击移动
        Bitboard captureMoves = CannonAttackCalculator.calculateAttacks(
            fromSquare, occupied, true);
        captureMoves = captureMoves.and(enemyPieces); // 只保留敌方棋子
        
        // 添加攻击移动
        for (int square = captureMoves.getFirstSquare(); 
             square != -1; 
             square = captureMoves.getNextSquare()) {
            moves.add(new Move(fromSquare, square, true));
        }
        
        return moves;
    }
}
```

### 4. 预计算优化版本
```java
public class PrecomputedCannonMoves {
    
    // 预计算攻击表
    private static final Bitboard[][] ATTACK_TABLE = new Bitboard[90][1 << 18];
    
    static {
        precomputeAttacks();
    }
    
    private static void precomputeAttacks() {
        for (int square = 0; square < 90; square++) {
            // 计算相关占领位
            Bitboard mask = calculateRelevantOccupancy(square);
            int maskBits = mask.countBits();
            
            // 生成所有可能的占领组合
            for (int index = 0; index < (1 << maskBits); index++) {
                Bitboard occupancy = indexToOccupancy(index, mask);
                Bitboard attacks = calculateSlidingAttacks(square, occupancy);
                ATTACK_TABLE[square][index] = attacks;
            }
        }
    }
    
    public static Bitboard getAttacks(int square, Bitboard occupied) {
        Bitboard mask = calculateRelevantOccupancy(square);
        int index = calculateMagicIndex(occupied.and(mask));
        return ATTACK_TABLE[square][index];
    }
}
```

## 使用示例

```java
public class CannonExample {
    public static void main(String[] args) {
        // 创建棋盘状态
        Bitboard occupied = new Bitboard();
        Bitboard redPieces = new Bitboard();
        Bitboard blackPieces = new Bitboard();
        
        // 设置炮的位置
        int cannonPos = 4 * 9 + 4; // e5
        
        // 设置一些棋子作为炮架
        occupied.setBit(4 * 9 + 6); // e7
        occupied.setBit(4 * 9 + 2); // e3
        
        // 生成所有可能的移动
        List<CannonMoveGenerator.Move> moves = CannonMoveGenerator.generateMoves(
            cannonPos, occupied, redPieces, blackPieces);
        
        // 打印移动
        for (CannonMoveGenerator.Move move : moves) {
            System.out.println(move);
        }
    }
}
```

## 性能优化建议

1. **位运算优化**：使用`long`类型进行64位并行计算
2. **预计算表**：对于固定大小的棋盘，可以预计算所有可能的攻击模式
3. **内存布局**：使用数组而非对象数组减少内存访问开销
4. **边界检查**：使用位掩码而非条件判断进行边界检查

## 测试用例

```java
public class CannonTest {
    @Test
    public void testCannonAttacks() {
        // 测试炮的基本攻击
        Bitboard occupied = new Bitboard();
        occupied.setBit(20); // 炮架
        occupied.setBit(30); // 目标
        
        Bitboard attacks = CannonAttackCalculator.calculateAttacks(10, occupied, true);
        assertTrue(attacks.getBit(30));
    }
}