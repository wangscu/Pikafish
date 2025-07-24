package com.pikafish;

/**
 * 调试车在棋盘边缘时的移动
 */
public class EdgeMovementDebug {
    
    // 棋盘大小常量
    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 10;
    
    // 方向常量
    private static final int NORTH = 9;
    private static final int SOUTH = -9;
    private static final int EAST = 1;
    private static final int WEST = -1;
    
    public static void main(String[] args) {
        System.out.println("=== 调试车在棋盘边缘时的移动 ===");
        
        // 检查车在i4(44)位置向东移动
        System.out.println("\n=== 检查车在i4(44)位置向东移动 ===");
        int fromSquare = 44; // i4
        int direction = EAST;
        
        System.out.printf("起始位置: %c%d (%d)%n", 
            (char)('a' + (fromSquare % 9)), fromSquare / 9, fromSquare);
        
        int current = fromSquare + direction;
        int count = 0;
        
        while (isValidSquare(current) && !isOppositeEdge(fromSquare, current, direction)) {
            System.out.printf("  移动到: %c%d (%d)%n", 
                (char)('a' + (current % 9)), current / 9, current);
            
            current += direction;
            count++;
            
            // 安全检查，防止无限循环
            if (count > 20) {
                System.out.println("  警告: 可能存在无限循环");
                break;
            }
        }
        
        System.out.printf("最终位置: %c%d (%d)%n", 
            (char)('a' + (current % 9)), current / 9, current);
        System.out.printf("是否有效格子: %b%n", isValidSquare(current));
        System.out.printf("是否越界: %b%n", isOppositeEdge(fromSquare, current, direction));
        
        // 检查车在a4(36)位置向西移动
        System.out.println("\n=== 检查车在a4(36)位置向西移动 ===");
        fromSquare = 36; // a4
        direction = WEST;
        
        System.out.printf("起始位置: %c%d (%d)%n", 
            (char)('a' + (fromSquare % 9)), fromSquare / 9, fromSquare);
        
        current = fromSquare + direction;
        count = 0;
        
        while (isValidSquare(current) && !isOppositeEdge(fromSquare, current, direction)) {
            System.out.printf("  移动到: %c%d (%d)%n", 
                (char)('a' + (current % 9)), current / 9, current);
            
            current += direction;
            count++;
            
            // 安全检查，防止无限循环
            if (count > 20) {
                System.out.println("  警告: 可能存在无限循环");
                break;
            }
        }
        
        System.out.printf("最终位置: %c%d (%d)%n", 
            (char)('a' + (current % 9)), current / 9, current);
        System.out.printf("是否有效格子: %b%n", isValidSquare(current));
        System.out.printf("是否越界: %b%n", isOppositeEdge(fromSquare, current, direction));
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