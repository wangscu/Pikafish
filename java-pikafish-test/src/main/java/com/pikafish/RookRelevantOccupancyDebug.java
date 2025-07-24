package com.pikafish;

/**
 * 调试类，用于检查车的相关占领位计算
 */
public class RookRelevantOccupancyDebug {
    public static void main(String[] args) {
        System.out.println("=== 调试车的相关占领位计算 ===");
        
        // 测试几个关键位置的相关占领位
        int[] testSquares = {4, 44, 85}; // a5, i4, i9
        
        for (int square : testSquares) {
            System.out.println("\n=== 测试格子 " + squareToCoordinate(square) + "(" + square + ") ===");
            
            // 计算相关占领位
            Bitboard relevantOccupancy = PrecomputedRookNonAttacks.getRelevantOccupancy(square);
            
            System.out.println("相关占领位:");
            System.out.println(relevantOccupancy.toString());
            
            int maskBits = relevantOccupancy.countBits();
            System.out.println("相关占领位位数: " + maskBits);
            
            // 计算需要的数组大小
            int arraySize = 1 << maskBits;
            System.out.println("需要的数组大小: " + arraySize);
        }
    }
    
    /**
     * 将格子编号转换为坐标表示
     */
    private static String squareToCoordinate(int square) {
        if (square < 0 || square >= 90) {
            return "无效格子";
        }
        
        int file = square % 9;  // 文件 (0-8)
        int rank = square / 9;  // 行 (0-9)
        
        char fileChar = (char) ('a' + file);
        int rankNumber = rank + 1;
        
        return "" + fileChar + rankNumber;
    }
}