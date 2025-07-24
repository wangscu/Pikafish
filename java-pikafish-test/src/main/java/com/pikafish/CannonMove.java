package com.pikafish;

/**
 * 象棋移动类
 */
public class CannonMove extends Move {
    /**
     * 构造函数
     */
    public CannonMove(int from, int to, boolean isCapture) {
        super(from, to, isCapture);
    }
    
    /**
     * 构造函数（指定移动类型）
     */
    public CannonMove(int from, int to, MoveType type) {
        super(from, to, type);
    }
    
    /**
     * 将移动转换为字符串表示
     */
    @Override
    public String toString() {
        // 将数字坐标转换为代数记谱法
        String fromStr = squareToString(getFrom());
        String toStr = squareToString(getTo());
        return fromStr + (isCapture() ? "x" : "-") + toStr;
    }
    
    /**
     * 将格子转换为字符串
     */
    private String squareToString(int square) {
        if (square < 0 || square >= 90) {
            return "??";
        }
        
        int file = square % 9;  // 文件 (0-8 -> a-i)
        int rank = square / 9;  // 等级 (0-9)
        
        char fileChar = (char)('a' + file);
        return "" + fileChar + rank;
    }
    
    /**
     * 从字符串解析移动
     */
    public static CannonMove fromString(String moveStr) {
        if (moveStr == null || moveStr.length() < 5) {
            throw new IllegalArgumentException("Invalid move string: " + moveStr);
        }
        
        int from = stringToSquare(moveStr.substring(0, 2));
        boolean isCapture = moveStr.charAt(2) == 'x';
        int to = stringToSquare(moveStr.substring(3, 5));
        
        return new CannonMove(from, to, isCapture);
    }
    
    /**
     * 将字符串转换为格子
     */
    private static int stringToSquare(String squareStr) {
        if (squareStr.length() < 2) {
            throw new IllegalArgumentException("Invalid square string: " + squareStr);
        }
        
        char fileChar = squareStr.charAt(0);
        char rankChar = squareStr.charAt(1);
        
        int file = fileChar - 'a';
        int rank = rankChar - '0';
        
        if (file < 0 || file > 8 || rank < 0 || rank > 9) {
            throw new IllegalArgumentException("Invalid square: " + squareStr);
        }
        
        return rank * 9 + file;
    }
}