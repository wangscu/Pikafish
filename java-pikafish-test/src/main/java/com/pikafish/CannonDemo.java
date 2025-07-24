package com.pikafish;

import java.util.List;

/**
 * 炮攻击和移动生成演示类
 */
public class CannonDemo {
    
    public static void main(String[] args) {
        System.out.println("=== 中国象棋炮攻击和移动生成演示 ===\n");
        
        // 创建棋盘状态
        Bitboard occupied = new Bitboard();    // 所有棋子
        Bitboard redPieces = new Bitboard();   // 红方棋子
        Bitboard blackPieces = new Bitboard(); // 黑方棋子
        
        // 设置红方炮在e5位置 (4*9+4 = 40)
        int redCannonPos = 40;
        redPieces.setBit(redCannonPos);
        occupied.setBit(redCannonPos);
        
        // 设置黑方炮在e6位置 (5*9+4 = 49)
        int blackCannonPos = 49;
        blackPieces.setBit(blackCannonPos);
        occupied.setBit(blackCannonPos);
        
        // 在e7位置放置红方兵作为炮架 (6*9+4 = 58)
        int hurdlePos = 58;
        redPieces.setBit(hurdlePos);
        occupied.setBit(hurdlePos);
        
        // 在e9位置放置黑方车作为攻击目标 (8*9+4 = 76)
        int targetPos = 76;
        blackPieces.setBit(targetPos);
        occupied.setBit(targetPos);
        
        System.out.println("棋盘状态:");
        System.out.println("  红方炮位置: " + squareToString(redCannonPos) + " (e5)");
        System.out.println("  黑方炮位置: " + squareToString(blackCannonPos) + " (e6)");
        System.out.println("  炮架位置: " + squareToString(hurdlePos) + " (e7)");
        System.out.println("  攻击目标: " + squareToString(targetPos) + " (e9)");
        System.out.println();
        
        // 演示红方炮的移动
        demonstrateCannonMoves("红方炮", redCannonPos, occupied, redPieces, blackPieces);
        
        // 演示黑方炮的移动
        demonstrateCannonMoves("黑方炮", blackCannonPos, occupied, blackPieces, redPieces);
        
        // 演示预计算攻击表
        demonstratePrecomputedAttacks(redCannonPos, occupied);
    }
    
    /**
     * 演示炮的移动
     */
    private static void demonstrateCannonMoves(String cannonName, int cannonPos,
                                                 Bitboard occupied, Bitboard friendlyPieces, Bitboard enemyPieces) {
            System.out.println("--- " + cannonName + "在" + squareToString(cannonPos) + "的移动 ---");
            
            // 生成所有移动
            List<CannonMove> allMoves = CannonMoveGenerator.generateMoves(cannonPos, occupied, friendlyPieces, enemyPieces);
            
            // 分类移动
            List<CannonMove> quietMoves = CannonMoveGenerator.generateQuiets(cannonPos, occupied);
            List<CannonMove> captureMoves = CannonMoveGenerator.generateCaptures(cannonPos, occupied, enemyPieces);
            
            System.out.println("普通移动数量: " + quietMoves.size());
            if (quietMoves.size() > 0) {
                System.out.print("  ");
                for (int i = 0; i < Math.min(5, quietMoves.size()); i++) {
                    System.out.print(quietMoves.get(i) + " ");
                }
                if (quietMoves.size() > 5) {
                    System.out.print("... (" + (quietMoves.size() - 5) + " more)");
                }
                System.out.println();
            }
            
            System.out.println("攻击移动数量: " + captureMoves.size());
            if (captureMoves.size() > 0) {
                System.out.print("  ");
                for (CannonMove move : captureMoves) {
                    System.out.print(move + " ");
                }
                System.out.println();
            }
            
            System.out.println("总移动数量: " + allMoves.size());
            System.out.println();
        }
    
    /**
     * 演示预计算攻击表
     */
    private static void demonstratePrecomputedAttacks(int cannonPos, Bitboard occupied) {
        System.out.println("--- 预计算攻击表演示 ---");
        
        // 使用动态计算
        long startTime = System.nanoTime();
        Bitboard dynamicAttacks = CannonAttackCalculator.calculateSlidingAttacks(cannonPos, occupied);
        long dynamicTime = System.nanoTime() - startTime;
        
        // 使用预计算表
        startTime = System.nanoTime();
        Bitboard precomputedAttacks = PrecomputedCannonAttacks.getAttacks(cannonPos, occupied);
        long precomputedTime = System.nanoTime() - startTime;
        
        System.out.println("动态计算结果: " + dynamicAttacks.countBits() + " 个攻击位置");
        System.out.println("预计算结果: " + precomputedAttacks.countBits() + " 个攻击位置");
        System.out.println("结果一致性: " + dynamicAttacks.equals(precomputedAttacks));
        System.out.println("动态计算耗时: " + dynamicTime + " 纳秒");
        System.out.println("预计算耗时: " + precomputedTime + " 纳秒");
        System.out.println("性能提升: " + (dynamicTime > 0 ? String.format("%.2f", (double) dynamicTime / precomputedTime) : "N/A") + "x");
        System.out.println();
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