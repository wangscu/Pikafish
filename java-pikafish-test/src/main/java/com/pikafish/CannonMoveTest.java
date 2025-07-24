package com.pikafish;

import java.util.List;

/**
 * 炮移动生成器测试类
 */
public class CannonMoveTest {
    
    public static void main(String[] args) {
        // 创建测试棋盘
        testBasicCannonMoves();
        testCannonWithHurdle();
        testCannonCaptures();
        testPrecomputedAttacks();
        testFullMoveGeneration();
    }
    
    /**
     * 测试基本炮移动
     */
    public static void testBasicCannonMoves() {
        System.out.println("=== 测试基本炮移动 ===");
        
        // 创建空棋盘
        Bitboard occupied = new Bitboard();
        Bitboard enemyPieces = new Bitboard();
        
        // 炮在e5位置 (4*9+4 = 40)
        int cannonPos = 40;
                
                // 生成所有移动
                List<CannonMove> moves = CannonMoveGenerator.generateQuiets(cannonPos, occupied);
                
                System.out.println("炮在e5位置的普通移动数量: " + moves.size());
                for (CannonMove move : moves) {
                    System.out.println("  " + move);
                }
            }
    
    /**
     * 测试有炮架的情况
     */
    public static void testCannonWithHurdle() {
        System.out.println("\n=== 测试有炮架的情况 ===");
        
        // 创建棋盘状态
        Bitboard occupied = new Bitboard();
        Bitboard enemyPieces = new Bitboard();
        
        // 炮在e5位置 (4*9+4 = 40)
        int cannonPos = 40;
        
        // 在e7位置放置炮架 (6*9+4 = 58)
        occupied.setBit(58);
                
                // 生成普通移动
                List<CannonMove> quietMoves = CannonMoveGenerator.generateQuiets(cannonPos, occupied);
                
                System.out.println("炮在e5位置，e7有炮架时的普通移动数量: " + quietMoves.size());
                for (CannonMove move : quietMoves) {
                    System.out.println("  " + move);
                }
            }
    
    /**
     * 测试炮的攻击移动
     */
    public static void testCannonCaptures() {
        System.out.println("\n=== 测试炮的攻击移动 ===");
        
        // 创建棋盘状态
        Bitboard occupied = new Bitboard();
        Bitboard enemyPieces = new Bitboard();
        
        // 炮在e5位置 (4*9+4 = 40)
        int cannonPos = 40;
        
        // 在e7位置放置炮架 (6*9+4 = 58)
        occupied.setBit(58);
        
        // 在e9位置放置敌方棋子 (8*9+4 = 76)
        occupied.setBit(76);
        enemyPieces.setBit(76);
                
                // 生成攻击移动
                List<CannonMove> captureMoves = CannonMoveGenerator.generateCaptures(cannonPos, occupied, enemyPieces);
                
                System.out.println("炮在e5位置，e7有炮架，e9有敌子时的攻击移动数量: " + captureMoves.size());
                for (CannonMove move : captureMoves) {
                    System.out.println("  " + move);
                }
            }
    
    /**
     * 测试预计算攻击表
     */
    public static void testPrecomputedAttacks() {
        System.out.println("\n=== 测试预计算攻击表 ===");
        
        // 创建棋盘状态
        Bitboard occupied = new Bitboard();
        
        // 炮在e5位置 (4*9+4 = 40)
        int cannonPos = 40;
        
        // 在e7位置放置炮架 (6*9+4 = 58)
        occupied.setBit(58);
        
        // 在e9位置放置棋子 (8*9+4 = 76)
        occupied.setBit(76);
        
        // 使用动态计算
        Bitboard dynamicAttacks = CannonAttackCalculator.calculateSlidingAttacks(cannonPos, occupied);
        
        // 使用预计算表
        Bitboard precomputedAttacks = PrecomputedCannonAttacks.getAttacks(cannonPos, occupied);
        
        System.out.println("动态计算攻击位数: " + dynamicAttacks.countBits());
        System.out.println("预计算攻击位数: " + precomputedAttacks.countBits());
        System.out.println("结果是否一致: " + dynamicAttacks.equals(precomputedAttacks));
    }
    
    /**
     * 测试完整移动生成
     */
    public static void testFullMoveGeneration() {
        System.out.println("\n=== 测试完整移动生成 ===");
        
        // 创建棋盘状态
        Bitboard occupied = new Bitboard();
        Bitboard friendlyPieces = new Bitboard();
        Bitboard enemyPieces = new Bitboard();
        
        // 炮在e5位置 (4*9+4 = 40)
        int cannonPos = 40;
        friendlyPieces.setBit(cannonPos);
        occupied.setBit(cannonPos);
        
        // 在e7位置放置炮架 (6*9+4 = 58)
        occupied.setBit(58);
        
        // 在e9位置放置敌方棋子 (8*9+4 = 76)
        occupied.setBit(76);
        enemyPieces.setBit(76);
                
                // 生成所有移动
                List<CannonMove> moves = CannonMoveGenerator.generateMoves(cannonPos, occupied, friendlyPieces, enemyPieces);
                
                System.out.println("炮在e5位置的所有合法移动数量: " + moves.size());
                for (CannonMove move : moves) {
                    System.out.println("  " + move);
                }
            }
        }