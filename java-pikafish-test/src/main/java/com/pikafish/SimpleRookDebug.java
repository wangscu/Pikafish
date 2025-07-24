package com.pikafish;

/**
 * 简单的车调试类，不依赖于PrecomputedRookNonAttacks的初始化
 */
public class SimpleRookDebug {
    
    // 棋盘大小常量
    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 10;
    
    // 方向常量
    private static final int NORTH = 9;
    private static final int SOUTH = -9;
    private static final int EAST = 1;
    private static final int WEST = -1;
    
    public static void main(String[] args) {
        System.out.println("=== 简单车调试 ===");
        
        // 直接计算e4格子(索引32)的相关占领位数量
        System.out.println("\n=== 计算e4格子(索引32)的相关占领位 ===");
        int square = 32; // e4
        Bitboard mask = calculateRelevantOccupancy(square);
        int maskBits = mask.countBits();
        
        System.out.printf("e4格子的相关占领位: %s%n", mask);
        System.out.printf("e4格子的相关占领位数量: %d%n", maskBits);
        System.out.printf("数组大小: %d%n", 1 << maskBits);
        
        // 检查其他几个格子
        System.out.println("\n=== 检查其他格子 ===");
        int[] testSquares = {0, 8, 45, 81, 89}; // a0, i0, a5, a9, i9
        for (int sq : testSquares) {
            Bitboard m = calculateRelevantOccupancy(sq);
            int bits = m.countBits();
            System.out.printf("格子 %d (%c%d): 相关占领位数量 = %d, 数组大小 = %d%n", 
                sq, (char)('a' + (sq % 9)), sq / 9, bits, 1 << bits);
        }
    }
    
    /**
     * 计算相关占领位（用于魔法位板）
     * @param square 格子位置
     * @return 相关占领位掩码
     */
    private static Bitboard calculateRelevantOccupancy(int square) {
        if (!isValidSquare(square)) {
            return new Bitboard();
        }
        
        Bitboard mask = new Bitboard();
        
        // 四个方向：北、南、东、西
        int[] directions = {NORTH, SOUTH, EAST, WEST};
        
        for (int direction : directions) {
            int current = square + direction;
            
            // 沿着每个方向直到边界
            while (isValidSquare(current) && !isOppositeEdge(square, current, direction)) {
                mask.setBit(current);
                current += direction;
            }
        }
        
        return mask;
    }
    
    /**
     * 验证格子是否有效
     */
    private static boolean isValidSquare(int square) {
        return square >= 0 && square < 90;
    }
    
    /**
    * 检查是否到达了棋盘边缘
    */
   private static boolean isOppositeEdge(int from, int to, int direction) {
       // 检查是否到达左右边界（文件0和文件8）
       if (direction == EAST || direction == WEST) {
           int toFile = to % BOARD_WIDTH;
           // 如果到达了左边界或右边界，停止移动
           return toFile == 0 || toFile == 8;
       }
       // 检查是否到达上下边界（行0和行9）
       if (direction == NORTH || direction == SOUTH) {
           int toRank = to / BOARD_WIDTH;
           // 如果到达了上边界或下边界，停止移动
           return toRank == 0 || toRank == 9;
       }
       return false;
   }
}