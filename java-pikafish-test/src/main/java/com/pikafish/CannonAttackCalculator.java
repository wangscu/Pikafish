package com.pikafish;

/**
 * 炮攻击计算器
 */
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
     * @param fromSquare 起始格子 (0-89)
     * @param occupied 所有棋子的位图
     * @param isCapture 是否为攻击模式
     * @return 攻击位图
     */
    public static Bitboard calculateAttacks(int fromSquare, Bitboard occupied, boolean isCapture) {
        if (!isValidSquare(fromSquare)) {
            return new Bitboard();
        }
        
        Bitboard attacks = new Bitboard();
        
        // 四个方向：北、南、东、西
        int[] directions = {NORTH, SOUTH, EAST, WEST};
        
        for (int direction : directions) {
            boolean hurdleFound = false;
            int current = fromSquare + direction;
            
            while (isValidSquare(current) && !isOppositeEdge(fromSquare, current, direction)) {
                if (occupied.getBit(current)) {
                    if (!hurdleFound) {
                        // 第一个遇到的棋子作为炮架
                        hurdleFound = true;
                        if (!isCapture) {
                            // 普通移动不能跳过棋子，停止该方向
                            break;
                        }
                        // 攻击模式下，继续寻找可以攻击的目标
                    } else {
                        // 第二个遇到的棋子可以攻击（如果是攻击模式）
                        if (isCapture) {
                            attacks.setBit(current);
                        }
                        // 无论是否为攻击模式，遇到第二个棋子都停止该方向
                        break;
                    }
                } else {
                    // 空格子
                    if (!isCapture && !hurdleFound) {
                        attacks.setBit(current);
                    }
                }
                
                current += direction;
            }
        }
        
        return attacks;
    }
    
    /**
     * 计算炮的滑动攻击（用于预计算）
     * @param fromSquare 起始格子
     * @param occupied 占领位图
     * @return 攻击位图
     */
    public static Bitboard calculateSlidingAttacks(int fromSquare, Bitboard occupied) {
        if (!isValidSquare(fromSquare)) {
            return new Bitboard();
        }
        
        Bitboard attacks = new Bitboard();
        
        // 四个方向：北、南、东、西
        int[] directions = {NORTH, SOUTH, EAST, WEST};
        
        for (int direction : directions) {
            boolean hurdleFound = false;
            int current = fromSquare + direction;
            
            while (isValidSquare(current) && !isOppositeEdge(fromSquare, current, direction)) {
                if (occupied.getBit(current)) {
                    if (!hurdleFound) {
                        // 第一个遇到的棋子作为炮架
                        hurdleFound = true;
                    } else {
                        // 第二个遇到的棋子可以攻击
                        attacks.setBit(current);
                        break;
                    }
                } else {
                    // 空格子，可以移动但不能攻击
                    if (!hurdleFound) {
                        attacks.setBit(current);
                    }
                }
                
                current += direction;
            }
        }
        
        return attacks;
    }
    
    /**
     * 计算相关占领位（用于魔法位板）
     * @param square 格子位置
     * @return 相关占领位掩码
     */
    public static Bitboard calculateRelevantOccupancy(int square) {
        if (!isValidSquare(square)) {
            return new Bitboard();
        }
        
        Bitboard mask = new Bitboard();
        
        // 四个方向：北、南、东、西
        int[] directions = {NORTH, SOUTH, EAST, WEST};
        
        for (int direction : directions) {
            int current = square + direction;
            int steps = 0;
            
            // 最多考虑每个方向前两个棋子
            while (isValidSquare(current) && !isOppositeEdge(square, current, direction) && steps < 8) {
                mask.setBit(current);
                current += direction;
                steps++;
            }
        }
        
        return mask;
    }
    
    /**
     * 验证格子是否有效
     */
    private static boolean isValidSquare(int square) {
        return square >= 0 && square < TOTAL_SQUARES;
    }
    
    /**
     * 检查是否跨越了边界
     */
    private static boolean isOppositeEdge(int from, int to, int direction) {
        // 检查是否跨越左右边界
        if (direction == EAST || direction == WEST) {
            int fromFile = from % BOARD_WIDTH;
            int toFile = to % BOARD_WIDTH;
            // 如果文件差异大于1，说明跨越了边界
            return Math.abs(toFile - fromFile) > 1;
        }
        return false;
    }
}