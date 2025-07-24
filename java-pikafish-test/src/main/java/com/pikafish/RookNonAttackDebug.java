package com.pikafish;

/**
 * 车非攻击着法调试类
 */
public class RookNonAttackDebug {
    
    public static void main(String[] args) {
        System.out.println("=== 车非攻击着法调试 ===");
        
        // 测试空棋盘上的车非攻击着法
        testEmptyBoardRookNonAttacks();
    }
    
    /**
     * 测试空棋盘上的车非攻击着法
     */
    public static void testEmptyBoardRookNonAttacks() {
        System.out.println("\n=== 测试空棋盘上的车非攻击着法 ===");
        
        // 创建空棋盘
        Bitboard occupied = new Bitboard();
        
        // 车在e5位置 (4*9+4 = 40)
        int rookPos = 40;
        
        // 手动计算期望的非攻击着法
        System.out.println("手动计算期望的非攻击着法:");
        Bitboard expected = new Bitboard();
        
        // 横向移动 (行4，列0-8，除了列4)
        for (int file = 0; file < 9; file++) {
            if (file != 4) {  // 排除自身位置
                int pos = 4 * 9 + file;
                expected.setBit(pos);
                System.out.println("  横向: " + squareToString(pos) + " (" + pos + ")");
            }
        }
        
        // 纵向移动 (列4，行0-9，除了行4)
        for (int rank = 0; rank < 10; rank++) {
            if (rank != 4) {  // 排除自身位置
                int pos = rank * 9 + 4;
                expected.setBit(pos);
                System.out.println("  纵向: " + squareToString(pos) + " (" + pos + ")");
            }
        }
        
        System.out.println("期望的非攻击着法数量: " + expected.countBits());
        System.out.println("期望的非攻击着法位图: " + expected);
        
        // 获取车的非攻击着法位图
        Bitboard nonAttacks = PrecomputedRookNonAttacks.getNonAttacks(rookPos, occupied);
        
        System.out.println("实际的非攻击着法数量: " + nonAttacks.countBits());
        System.out.println("实际的非攻击着法位图: " + nonAttacks);
        
        // 检查相关占领位
        Bitboard relevantOccupancy = PrecomputedRookNonAttacks.getRelevantOccupancy(rookPos);
        System.out.println("相关占领位数量: " + relevantOccupancy.countBits());
        System.out.println("相关占领位位图: " + relevantOccupancy);
    }
    
    /**
     * 将格子转换为字符串
     */
    private static String squareToString(int square) {
        if (square < 0 || square >= 90) {
            return "??";
        }
        
        int file = square % 9;  // 文件 (0-8 -> a-i)
        int rank = square / 9;  // 等级 (0-9)
        
        char fileChar = (char)('a' + file);
        return "" + fileChar + rank;
    }
}