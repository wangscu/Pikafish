package com.pikafish;

/**
 * 测试DetailedRookDebug类中的isOppositeEdge方法
 */
public class DetailedIsOppositeEdgeTest {
    
    // 棋盘大小常量
    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 10;
    
    // 方向常量
    private static final int NORTH = 9;
    private static final int SOUTH = -9;
    private static final int EAST = 1;
    private static final int WEST = -1;
    
    public static void main(String[] args) {
        System.out.println("=== 测试DetailedRookDebug类中的isOppositeEdge方法 ===");
        
        // 测试从i4(44)向东移动到a5(45)
        System.out.println("\n=== 测试从i4(44)向东移动到a5(45) ===");
        int from = 44; // i4
        int to = 45;   // a5
        int direction = EAST;
        
        System.out.printf("from: %c%d (%d)%n", 
            (char)('a' + (from % 9)), from / 9, from);
        System.out.printf("to: %c%d (%d)%n", 
            (char)('a' + (to % 9)), to / 9, to);
        System.out.printf("isOppositeEdge: %b%n", isOppositeEdge(from, to, direction));
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
            System.out.printf("    检查横向穿越: from=%d(fromFile=%d), to=%d(toFile=%d), direction=%d%n", 
                from, fromFile, to, toFile, direction);
            
            // 如果越过了左边界或右边界，停止移动
            if (toFile < 0 || toFile > 8) {
                System.out.printf("    越过边界: fromFile=%d, toFile=%d%n", fromFile, toFile);
                return true;
            }
            
            // 如果在同一行内发生了横向穿越（文件从8到0或从0到8），停止移动
            if ((fromFile == 8 && toFile == 0) || (fromFile == 0 && toFile == 8)) {
                System.out.printf("    横向穿越: fromFile=%d, toFile=%d%n", fromFile, toFile);
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