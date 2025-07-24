package com.pikafish;

import java.util.*;

// 假设的棋盘状态和走法类 (需要根据实际象棋规则实现)
class Board {
    // 表示当前棋盘状态
    // ... 字段 ...

    public boolean isGameOver() { /* 检查是否将军或无子可动 */ return false; }
    public int evaluate() { /* 返回当前局面对于红方的评估分 */ return 0; }
    public List<Move> generateMoves() { /* 生成所有合法走法 */ return new ArrayList<>(); }
    public void makeMove(Move move) { /* 执行走法 */ }
    public void undoMove(Move move) { /* 撤销走法 */ }
    public long getZobristKey() { /* 返回当前局面的 Zobrist 哈希值 */ return 0L; }
}

public class ChineseChessNegamax {

    private static final int INFINITY = 999999;
    private static final int MATE_SCORE = 99999;
    private static final int MAX_DEPTH = 64;
    private com.pikafish.TranspositionTable tt = new com.pikafish.TranspositionTable();

    /**
     * 主要的 Negamax 搜索函数 (带 Alpha-Beta 剪枝, PV 搜索, 置换表)
     * @param board 当前棋盘状态
     * @param depth 剩余搜索深度
     * @param alpha Alpha 窗口边界
     * @param beta Beta 窗口边界
     * @param ply 当前搜索层数 (根节点为0)
     * @return 最佳评估分
     */
    public int negamax(Board board, int depth, int alpha, int beta, int ply) {
        // 检查游戏是否结束 (终止节点)
        if (board.isGameOver()) {
            // 根据谁被将死返回分数
            return -MATE_SCORE + ply; // ply 用于调整杀棋步数
        }

        // 初始化最佳走法
        Move bestMove = null;

        // 查询置换表 [[2]]
        com.pikafish.TTEntry ttEntry = tt.probe(board.getZobristKey());
        if (ttEntry != null && ttEntry.depth >= depth) { // 根节点不使用置换表截断
            int ttScore = ttEntry.score;
            // 边界检查和截断
            if (ttEntry.flag == com.pikafish.TTFlag.EXACT) { // 精确值
                return ttScore;
            } else if (ttEntry.flag == com.pikafish.TTFlag.LOWER && ttScore >= beta) { // Beta 边界 (下边界) [[7]]
                return ttScore; // Beta 截断
            } else if (ttEntry.flag == com.pikafish.TTFlag.UPPER && ttScore <= alpha) { // Alpha 边界 (上边界) [[7]]
                return ttScore; // Alpha 截断
            }
            // 如果是 PV 节点且有置换表着法，则优先考虑 [[4]]
            if (ttEntry.bestMove != null) {
                bestMove = ttEntry.bestMove; // 置换表启发 [[2]]
            }
        }


        // 叶节点：到达深度限制或需要进入静态搜索
        if (depth <= 0) {
            // 进入静态搜索以稳定评估 [[5]]
            return quiescence(board, alpha, beta, ply);
        }

        List<Move> moves = board.generateMoves();
        if (moves.isEmpty()) {
            // 检查是否被将死或困毙已在 isGameOver 中处理
            // 如果走到这里，可能是特殊局面，返回评估
            return board.evaluate();
        }

        int moveCount = 0;
        int bestScore = -INFINITY;
        com.pikafish.TTFlag flag = com.pikafish.TTFlag.UPPER; // 初始化为 Alpha 边界

        // 将置换表中的最佳走法移到最前面 (启发式)
        if (bestMove != null) {
            moves.remove(bestMove);
            moves.add(0, bestMove);
        }

        for (Move move : moves) {
            board.makeMove(move);
            int score;
            moveCount++;

            if (moveCount == 1) {
                // 对第一个走法进行全窗口搜索
                score = -negamax(board, depth - 1, -beta, -alpha, ply + 1);
            } else {
                // 对后续走法进行零窗口搜索 (Null Window Search)
                score = -negamax(board, depth - 1, -alpha - 1, -alpha, ply + 1);
                // 如果零窗口搜索结果表明第一个走法可能不是最佳的，则进行重新搜索 (PVS)
                if (score > alpha && score < beta) {
                    // 重新进行全窗口搜索以获得准确分数
                    score = -negamax(board, depth - 1, -beta, -alpha, ply + 1);
                }
            }
            board.undoMove(move);

            if (score > bestScore) {
                bestScore = score;

                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                    flag = TTFlag.EXACT; // 更新为精确值标志
                }
            }

            // Alpha-Beta 剪枝
            if (alpha >= beta) {
                flag = TTFlag.LOWER; // Beta 截断，更新为 Beta 边界标志
                break; // 发生截断
            }
        }

        // 将结果存入置换表 [[2]]
        tt.store(board.getZobristKey(), depth, bestScore, flag, bestMove);

        return bestScore;
    }

    /**
     * 静态搜索 (Quiescence Search) - 用于稳定评估，避免水平线效应
     * @param board 当前棋盘状态
     * @param alpha Alpha 窗口边界
     * @param beta Beta 窗口边界
     * @param ply 当前搜索层数
     * @return 稳定后的评估分
     */
    private int quiescence(Board board, int alpha, int beta, int ply) {
        // 防止搜索过深
        if (ply >= MAX_DEPTH) {
            return board.evaluate();
        }

        // 站立点评估 (Stand pat)
        int standPat = board.evaluate();
        if (standPat >= beta) {
            return beta; // 站立点就足够好，可以截断
        }
        if (alpha < standPat) {
            alpha = standPat; // 提升 Alpha
        }

        // 只生成吃子走法或将军走法等“不稳定”走法
        List<Move> captures = generateCapturesOrChecks(board); // 需要实现
        for (Move move : captures) {
            board.makeMove(move);
            int score = -quiescence(board, -beta, -alpha, ply + 1);
            board.undoMove(move);

            if (score >= beta) {
                return beta; // Beta 截断
            }
            if (score > alpha) {
                alpha = score; // 更新 Alpha
            }
        }
        return alpha;
    }

    // 辅助函数：生成吃子或将军走法 (需要根据规则实现)
    private List<Move> generateCapturesOrChecks(Board board) {
        // 实际实现中，可能需要专门的生成器
        // 这里简化为生成所有走法并筛选
        List<Move> allMoves = board.generateMoves();
        List<Move> capturesOrChecks = new ArrayList<>();
        // ... 筛选逻辑 ...
        return capturesOrChecks;
    }

    // 启动搜索的公共接口 (例如，迭代加深)
    public Move findBestMove(Board board, int maxDepth) {
        Move bestMove = null;
        int bestScore = -INFINITY;

        for (int depth = 1; depth <= maxDepth; depth++) {
            // 每次深度增加时，重置 Alpha 和 Beta
            int score = negamax(board, depth, -INFINITY, INFINITY, 0);
            // 在实际应用中，会从根节点的 PV 线获取最佳走法
            // 这里简化处理
            System.out.println("info depth " + depth + " score cp " + score);
            // bestMove = getFromPV(); // 从 PV 线获取
        }
        // bestMove = getFromPV(); // 最终从 PV 线获取
        return bestMove; // 需要根据 PV 线逻辑完善
    }

    public static void main(String[] args) {
        // 示例用法 (需要完整的 Board 和 Move 实现)
        /*
        Board board = new Board(); // 初始化棋盘
        ChineseChessNegamax engine = new ChineseChessNegamax();
        Move bestMove = engine.findBestMove(board, 6); // 搜索深度为6
        System.out.println("Best Move Found: " + bestMove);
        */
    }
}