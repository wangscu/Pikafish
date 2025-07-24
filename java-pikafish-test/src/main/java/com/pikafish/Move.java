package com.pikafish;

/**
 * 象棋移动类
 */
public class Move {
    // 起始位置 (0-89)
    private final int from;
    
    // 目标位置 (0-89)
    private final int to;
    
    // 是否为捕获移动
    private final boolean isCapture;
    
    // 移动类型
    private final MoveType type;
    
    public enum MoveType {
        QUIET,        // 普通移动
        CAPTURE,      // 捕获移动
        CASTLING,     // 王车易位（中国象棋中不适用）
        EN_PASSANT,   // 吃过路兵（中国象棋中不适用）
        PROMOTION     // 升变（中国象棋中不适用）
    }
    
    /**
     * 构造函数
     */
    public Move(int from, int to, boolean isCapture) {
        this.from = from;
        this.to = to;
        this.isCapture = isCapture;
        this.type = isCapture ? MoveType.CAPTURE : MoveType.QUIET;
    }
    
    /**
     * 构造函数（指定移动类型）
     */
    public Move(int from, int to, MoveType type) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.isCapture = (type == MoveType.CAPTURE);
    }
    
    // Getter方法
    public int getFrom() {
        return from;
    }
    
    public int getTo() {
        return to;
    }
    
    public boolean isCapture() {
        return isCapture;
    }
    
    public MoveType getType() {
        return type;
    }
    
    /**
     * 将移动转换为字符串表示
     */
    @Override
    public String toString() {
        // 将数字坐标转换为代数记谱法
        String fromStr = squareToString(from);
        String toStr = squareToString(to);
        return fromStr + (isCapture ? "x" : "-") + toStr;
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
    public static Move fromString(String moveStr) {
        if (moveStr == null || moveStr.length() < 5) {
            throw new IllegalArgumentException("Invalid move string: " + moveStr);
        }
        
        int from = stringToSquare(moveStr.substring(0, 2));
        boolean isCapture = moveStr.charAt(2) == 'x';
        int to = stringToSquare(moveStr.substring(3, 5));
        
        return new Move(from, to, isCapture);
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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return from == move.from && to == move.to && isCapture == move.isCapture;
    }
    
    @Override
    public int hashCode() {
        return from * 1000 + to;
    }
}