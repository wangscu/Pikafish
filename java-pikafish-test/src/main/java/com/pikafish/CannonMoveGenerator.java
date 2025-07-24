package com.pikafish;

import java.util.ArrayList;
import java.util.List;

/**
 * 炮移动列表生成器
 */
public class CannonMoveGenerator {
    
    /**
     * 生成炮的所有合法移动
     * @param fromSquare 炮的位置 (0-89)
     * @param occupied 所有棋子的位图
     * @param friendlyPieces 己方棋子的位图
     * @param enemyPieces 敌方棋子的位图
     * @return 移动列表
     */
    public static List<CannonMove> generateMoves(int fromSquare, Bitboard occupied,
                                          Bitboard friendlyPieces, Bitboard enemyPieces) {
        List<CannonMove> moves = new ArrayList<>();
        
        // 生成普通移动（非攻击）
        Bitboard quietAttacks = CannonAttackCalculator.calculateAttacks(
            fromSquare, occupied, false);
        // 移除被占据的格子
        Bitboard quietMoves = quietAttacks.andNot(occupied);
        
        // 添加普通移动
        addMovesFromBitboard(moves, fromSquare, quietMoves, false);
        
        // 生成攻击移动
        Bitboard captureAttacks = CannonAttackCalculator.calculateAttacks(
            fromSquare, occupied, true);
        // 只保留敌方棋子
        Bitboard captureMoves = captureAttacks.and(enemyPieces);
        
        // 添加攻击移动
        addMovesFromBitboard(moves, fromSquare, captureMoves, true);
        
        return moves;
    }
    
    /**
     * 从位图添加移动到列表
     */
    private static void addMovesFromBitboard(List<CannonMove> moves, int fromSquare,
                                           Bitboard targetSquares, boolean isCapture) {
        int toSquare = targetSquares.getFirstSquare();
        while (toSquare != -1) {
            moves.add(new CannonMove(fromSquare, toSquare, isCapture));
            toSquare = targetSquares.getNextSquare(toSquare);
        }
    }
    
    /**
     * 生成所有类型的移动（普通和攻击）
     * @param fromSquare 炮的位置
     * @param occupied 所有棋子的位图
     * @param enemyPieces 敌方棋子的位图
     * @return 移动列表
     */
    public static List<CannonMove> generateAllMoves(int fromSquare, Bitboard occupied, Bitboard enemyPieces) {
        List<CannonMove> moves = new ArrayList<>();
        
        // 生成普通移动（非攻击）
        Bitboard quietAttacks = CannonAttackCalculator.calculateAttacks(
            fromSquare, occupied, false);
        // 移除被占据的格子
        Bitboard quietMoves = quietAttacks.andNot(occupied);
        
        // 添加普通移动
        addMovesFromBitboard(moves, fromSquare, quietMoves, false);
        
        // 生成攻击移动
        Bitboard captureAttacks = CannonAttackCalculator.calculateAttacks(
            fromSquare, occupied, true);
        // 只保留敌方棋子
        Bitboard captureMoves = captureAttacks.and(enemyPieces);
        
        // 添加攻击移动
        addMovesFromBitboard(moves, fromSquare, captureMoves, true);
        
        return moves;
    }
    
    /**
     * 生成捕获移动
     * @param fromSquare 炮的位置
     * @param occupied 所有棋子的位图
     * @param enemyPieces 敌方棋子的位图
     * @return 捕获移动列表
     */
    public static List<CannonMove> generateCaptures(int fromSquare, Bitboard occupied, Bitboard enemyPieces) {
        List<CannonMove> moves = new ArrayList<>();
        
        // 生成攻击移动
        Bitboard captureAttacks = CannonAttackCalculator.calculateAttacks(
            fromSquare, occupied, true);
        // 只保留敌方棋子
        Bitboard captureMoves = captureAttacks.and(enemyPieces);
        
        // 添加攻击移动
        addMovesFromBitboard(moves, fromSquare, captureMoves, true);
        
        return moves;
    }
    
    /**
     * 生成普通移动（非捕获）
     * @param fromSquare 炮的位置
     * @param occupied 所有棋子的位图
     * @return 普通移动列表
     */
    public static List<CannonMove> generateQuiets(int fromSquare, Bitboard occupied) {
        List<CannonMove> moves = new ArrayList<>();
        
        // 生成普通移动（非攻击）
        Bitboard quietAttacks = CannonAttackCalculator.calculateAttacks(
            fromSquare, occupied, false);
        // 移除被占据的格子
        Bitboard quietMoves = quietAttacks.andNot(occupied);
        
        // 添加普通移动
        addMovesFromBitboard(moves, fromSquare, quietMoves, false);
        
        return moves;
    }
}