package com.pikafish;

/**
 * 详细的车调试类，检查具体是哪些格子没有被包括在内
 */
public class DetailedRookDebug {
    
    // 棋盘大小常量
    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 10;
    
    // 方向常量
    private static final int NORTH = 9;
    private static final int SOUTH = -9;
    private static final int EAST = 1;
    private static final int WEST = -1;
    
    public static void main(String[] args) {
        System.out.println("=== 详细车调试 ===");
        
        // 检查车在e5位置(索引40)的非攻击着法
        System.out.println("\n=== 检查车在e5位置(索引40)的非攻击着法 ===");
        int fromSquare = 40; // e5
        Bitboard occupied = new Bitboard(); // 空棋盘
        
        Bitboard nonAttacks = calculateSlidingNonAttacks(fromSquare, occupied);
        
        System.out.printf("非攻击着法位图: %s%n", nonAttacks);
        System.out.printf("非攻击着法数量: %d%n", nonAttacks.countBits());
        
        // 手动计算期望的非攻击着法
        System.out.println("\n手动计算期望的非攻击着法:");
        Bitboard expected = new Bitboard();
        
        // 横向: a5(45)到i5(53)，除了e5(40)
        System.out.println("  横向格子:");
        for (int file = 0; file < 9; file++) {
            int square = 45 + file; // 第5行的格子
            if (square != fromSquare) {
                expected.setBit(square);
                System.out.printf("    %c5 (%d)%n", (char)('a' + file), square);
            }
        }
        
        // 纵向: e0(4)到e9(85)，除了e5(40)
        System.out.println("  纵向格子:");
        for (int rank = 0; rank < 10; rank++) {
            int square = rank * 9 + 4; // 第4列的格子
            if (square != fromSquare) {
                expected.setBit(square);
                System.out.printf("    e%d (%d)%n", rank, square);
            }
        }
        
        System.out.printf("期望的非攻击着法数量: %d%n", expected.countBits());
        System.out.printf("期望的非攻击着法位图: %s%n", expected);
        
        // 比较差异
        System.out.println("\n比较差异:");
        Bitboard missing = expected.and(nonAttacks.not()); // 期望中有但实际中没有的格子
        System.out.printf("缺少的格子位图: %s%n", missing);
        System.out.printf("缺少的格子数量: %d%n", missing.countBits());
        
        // 打印缺少的具体格子
        System.out.println("缺少的具体格子:");
        int square = missing.getFirstSquare();
        while (square != -1) {
            int file = square % 9;
            int rank = square / 9;
            System.out.printf("  %c%d (%d)%n", (char)('a' + file), rank, square);
            square = missing.getNextSquare(square);
        }
        
        // 检查每个方向的移动
        System.out.println("\n=== 检查每个方向的移动 ===");
        int[] directions = {NORTH, SOUTH, EAST, WEST};
        String[] directionNames = {"北", "南", "东", "西"};
        
        for (int i = 0; i < directions.length; i++) {
            System.out.printf("  %s方向:%n", directionNames[i]);
            int direction = directions[i];
            int current = fromSquare + direction;
            int count = 0;
            
            while (isValidSquare(current) && !isOppositeEdge(fromSquare, current, direction)) {
                System.out.printf("    %c%d (%d)%n", (char)('a' + (current % 9)), current / 9, current);
                current += direction;
                count++;
                
                // 安全检查，防止无限循环
                if (count > 20) {
                    System.out.println("    警告: 可能存在无限循环");
                    break;
                }
            }
            
            // 打印最终位置和是否越界
            if (count <= 20) {
                System.out.printf("    最终位置: %c%d (%d)%n", (char)('a' + (current % 9)), current / 9, current);
                System.out.printf("    是否有效格子: %b%n", isValidSquare(current));
                System.out.printf("    是否越界: %b%n", isOppositeEdge(fromSquare, current, direction));
            }
        }
    }
    
    /**
     * 计算车的滑动非攻击着法
     * @param fromSquare 起始格子
     * @param occupied 占领位图（所有棋子的位置）
     * @return 非攻击着法位图
     */
    private static Bitboard calculateSlidingNonAttacks(int fromSquare, Bitboard occupied) {
        if (!isValidSquare(fromSquare)) {
            return new Bitboard();
        }
        
        Bitboard nonAttacks = new Bitboard();
        
        // 四个方向：北、南、东、西
        int[] directions = {NORTH, SOUTH, EAST, WEST};
        
        for (int direction : directions) {
            int current = fromSquare + direction;
            
            while (isValidSquare(current) && !isOppositeEdge(fromSquare, current, direction)) {
                // 如果当前位置没有被其他棋子占据，则可以移动到该位置
                if (!occupied.getBit(current)) {
                    nonAttacks.setBit(current);
                } else {
                    // 如果遇到棋子，停止该方向（无论是己方还是对方棋子都不能移动到该位置）
                    break;
                }
                
                current += direction;
            }
        }
        
        return nonAttacks;
    }
    
    /**
     * 验证格子是否有效
     */
    private static boolean isValidSquare(int square) {
        return square >= 0 && square < 90;
    }
    
    /**
    * 检查是否越过了棋盘边界或发生了横向穿越
    */
   private static boolean isOppositeEdge(int from, int to, int direction) {
       // 检查是否越过了左右边界（文件0和文件8）
       if (direction == EAST || direction == WEST) {
           int fromFile = from % BOARD_WIDTH;
           int toFile = to % BOARD_WIDTH;
           
           // 添加调试输出
           // System.out.printf("    检查横向穿越: from=%d(fromFile=%d), to=%d(toFile=%d), direction=%d%n",
           //     from, fromFile, to, toFile, direction);
           
           // 如果越过了左边界或右边界，停止移动
           if (toFile < 0 || toFile > 8) {
               // System.out.printf("    越过边界: fromFile=%d, toFile=%d%n", fromFile, toFile);
               return true;
           }
           
           // 如果在同一行内发生了横向穿越（文件从8到0或从0到8），停止移动
           if ((fromFile == 8 && toFile == 0) || (fromFile == 0 && toFile == 8)) {
               // System.out.printf("    横向穿越: fromFile=%d, toFile=%d%n", fromFile, toFile);
               return true;
           }
           
           return false;
       }
       // 检查是否越过了上下边界（行0和行9）
       if (direction == NORTH || direction == SOUTH) {
           int toRank = to / BOARD_WIDTH;
           // 如果越过了上边界或下边界，停止移动
           return toRank < 0 || toRank > 9;
       }
       return false;
   }
}