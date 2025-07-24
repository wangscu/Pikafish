package com.pikafish;

import java.util.*;

// 假设的棋盘状态类
class GameState {
    // 实现棋盘表示、走法生成、局面评估等
    public boolean isGameOver() { /* ... */ return false; }
    public int evaluate() { /* 返回相对于当前走子方的分数 */ return 0; }
    public List<Move> generateMoves() { /* ... */ return new ArrayList<>(); }
    public void makeMove(Move move) { /* ... */ }
    public void unmakeMove(Move move) { /* ... */ }
    public long getZobristKey() { /* Zobrist哈希用于置换表 */ return 0L; }
    // 其他必要的方法...
}

// 置换表条目类型
enum TTFlag {
    EXACT,   // 精确值
    LOWER,   // 下边界 (Beta截断)
    UPPER    // 上边界 (Alpha截断)
}

// 置换表条目
class TTEntry {
    long key;       // Zobrist键 (用于校验)
    int depth;      // 搜索深度
    int score;      // 评估分数
    TTFlag flag;    // 条目类型
    Move bestMove;  // 最佳走法

    public TTEntry(long key, int depth, int score, TTFlag flag, Move bestMove) {
        this.key = key;
        this.depth = depth;
        this.score = score;
        this.flag = flag;
        this.bestMove = bestMove;
    }
}

// 置换表
class TranspositionTable {
    private Map<Long, TTEntry> table;

    public TranspositionTable() {
        this.table = new HashMap<>();
    }

    public TTEntry probe(long key) {
        return table.get(key);
    }

    public void store(long key, int depth, int score, TTFlag flag, Move bestMove) {
        // 简单的替换策略，实际可能需要更复杂的逻辑（如深度优先、年龄等）
        table.put(key, new TTEntry(key, depth, score, flag, bestMove));
    }
}

public class ChessEngine {

    private TranspositionTable tt;
    private static final int INFINITY = 1000000;
    private static final int MATE_SCORE = 900000; // 低于无穷大，高于普通评估

    public ChessEngine() {
        this.tt = new TranspositionTable();
    }

    // 主要的Negamax搜索函数 (带PV搜索和置换表)
    public int negamax(GameState state, int depth, int alpha, int beta, boolean isRoot) {
        // 1. 终局检查
        if (state.isGameOver()) {
            // 返回极值分数，可能需要根据具体规则调整
            return -INFINITY; // 简化处理，实际需区分胜负和
        }

        // 2. 叶子节点评估 (Quiescence Search入口)
        if (depth <= 0) {
            // return state.evaluate(); // 简单评估
             return quiescenceSearch(state, alpha, beta); // 静态搜索
        }

        long alphaOriginal = alpha;
        // 3. 查询置换表
        long zobristKey = state.getZobristKey();
        TTEntry ttEntry = tt.probe(zobristKey);
        Move hashMove = null;
        if (ttEntry != null && ttEntry.key == zobristKey) {
            // 检查是否可以利用置换表结果 (深度足够)
            if (ttEntry.depth >= depth) {
                 if (ttEntry.flag == TTFlag.EXACT) {
                    return ttEntry.score;
                } else if (ttEntry.flag == TTFlag.LOWER && ttEntry.score >= beta) {
                    return ttEntry.score; // Beta截断
                } else if (ttEntry.flag == TTFlag.UPPER && ttEntry.score <= alpha) {
                    return ttEntry.score; // Alpha截断
                }
            }
            hashMove = ttEntry.bestMove; // 提取置换表中的最佳走法用于排序
        }


        List<Move> moves = state.generateMoves();
        if (moves.isEmpty()) {
             // 检查是否被将死或 stalemate，根据规则返回分数
             // 这里简化处理
             return -INFINITY + 1000; // 假设是被将死
        }

        // 4. 走法排序 (简化：将置换表走法放在前面)
        if (hashMove != null) {
            moves.remove(hashMove);
            moves.add(0, hashMove);
        }


        Move bestMove = null;
        int bestScore = -INFINITY;
        boolean firstMove = true;

        // 5. 遍历所有走法
        for (Move move : moves) {
            state.makeMove(move);

            int score;
            if (firstMove) {
                // PV Node: 全窗口搜索
                score = -negamax(state, depth - 1, -beta, -alpha, false);
                firstMove = false;
            } else {
                // 非PV Node: 尝试零窗口搜索 (PV Search)
                score = -negamax(state, depth - 1, -alpha - 1, -alpha, false);
                // 如果零窗口搜索失败，进行全窗口重新搜索
                if (score > alpha && score < beta) {
                    score = -negamax(state, depth - 1, -beta, -alpha, false);
                }
            }

            state.unmakeMove(move);

            // 6. 更新最佳分数和Alpha值
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
                if (isRoot) {
                     // 在根节点，你可以存储或更新当前最佳走法
                     System.out.println("info score cp " + score + " pv " + move); // UCI示例输出
                }
            }

            if (score > alpha) {
                alpha = score;
            }

            // 7. Alpha-Beta剪枝
            if (alpha >= beta) {
                break; // Beta截断
            }
        }

        // 8. 存储到置换表
        TTFlag flag;
        if (bestScore <= alphaOriginal) { // 注意：需要保存原始alpha
            flag = TTFlag.UPPER;
        } else if (bestScore >= beta) {
            flag = TTFlag.LOWER;
        } else {
            flag = TTFlag.EXACT;
        }
        tt.store(zobristKey, depth, bestScore, flag, bestMove);

        return bestScore;
    }

    // 静态搜索 (Quiescence Search) - 简化版本
    private int quiescenceSearch(GameState state, int alpha, int beta) {
         int standPat = state.evaluate(); // 静态评估

         if (standPat >= beta) {
             return beta;
         }

         if (alpha < standPat) {
             alpha = standPat;
         }

         // 通常只生成吃子走法或将军走法
         List<Move> captures = generateCapturesOrChecks(state); // 需要实现

         // 简单排序，价值高的先 (MVV-LVA等启发式更佳)
         captures.sort((m1, m2) -> compareCaptures(m1, m2)); // 需要实现比较逻辑

         for (Move capture : captures) {
             state.makeMove(capture);
             int score = -quiescenceSearch(state, -beta, -alpha);
             state.unmakeMove(capture);

             if (score >= beta) {
                 return beta;
             }
             if (score > alpha) {
                 alpha = score;
             }
         }

         return alpha;
    }

    // 辅助方法 - 生成吃子或将军走法 (需根据你的实现填充)
    private List<Move> generateCapturesOrChecks(GameState state) {
        // 实现只生成吃子或将军的走法
        return new ArrayList<>();
    }

    // 辅助方法 - 比较吃子走法价值 (需根据你的实现填充)
    private int compareCaptures(Move m1, Move m2) {
        // 实现吃子价值比较逻辑 (例如 MVV-LVA)
        return 0; // 简化
    }


    // 启动搜索的公共接口 (例如，在根节点调用)
    public Move findBestMove(GameState currentState, int searchDepth) {
        int alphaOriginal = -INFINITY;
        int alpha = alphaOriginal;
        int beta = INFINITY;
        Move bestMove = null;

        // 可以在这里调用 iterative deepening (迭代加深) 循环
        // for (int d = 1; d <= searchDepth; d++) { ... }

        // 或者直接调用指定深度的搜索
        try {
            int score = negamax(currentState, searchDepth, alpha, beta, true);
            // 实际应用中，你需要在negamax或findBestMove中追踪并返回最佳走法
            // 这里假设最佳走法已在negamax中确定或通过其他方式获取
            System.out.println("info score cp " + score);
        } catch (Exception e) {
            e.printStackTrace(); // 处理超时等异常
        }

        // 返回在搜索中确定的最佳走法
        // bestMove = ... ; // 从negamax逻辑或全局变量中获取
        return bestMove;
    }

    public static void main(String[] args) {
        // 示例用法 (需要实例化GameState)
        // GameState initialBoard = new GameState(...);
        // ChessEngine engine = new ChessEngine(1 << 20); // 1M 条目
        // Move bestMove = engine.findBestMove(initialBoard, 6);
        // System.out.println("Best Move: " + bestMove);
    }
}