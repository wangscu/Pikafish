package com.pikafish;

/**
 * 预计算炮攻击表（类似魔法位板）
 */
public class PrecomputedCannonAttacks {
    
    // 预计算攻击表
    private static final Bitboard[][] ATTACK_TABLE = new Bitboard[90][];
    
    // 相关占领位掩码
    private static final Bitboard[] RELEVANT_OCCUPANCY = new Bitboard[90];
    
    // 魔法数（简化版）
    private static final long[] MAGIC_NUMBERS = new long[90];
    
    static {
        initializeMagicNumbers();
        precomputeAttacks();
    }
    
    /**
     * 初始化魔法数
     */
    private static void initializeMagicNumbers() {
        // 简化的魔法数初始化
        for (int square = 0; square < 90; square++) {
            MAGIC_NUMBERS[square] = 0x0101010101010101L * (square + 1);
        }
    }
    
    /**
     * 预计算所有攻击模式
     */
    private static void precomputeAttacks() {
        for (int square = 0; square < 90; square++) {
            // 计算相关占领位
            RELEVANT_OCCUPANCY[square] = CannonAttackCalculator.calculateRelevantOccupancy(square);
            int maskBits = RELEVANT_OCCUPANCY[square].countBits();
            
            // 初始化攻击表
            ATTACK_TABLE[square] = new Bitboard[1 << maskBits];
            
            // 生成所有可能的占领组合
            for (int index = 0; index < (1 << maskBits); index++) {
                Bitboard occupancy = indexToOccupancy(index, RELEVANT_OCCUPANCY[square]);
                ATTACK_TABLE[square][index] = CannonAttackCalculator.calculateSlidingAttacks(square, occupancy);
            }
        }
    }
    
    /**
     * 将索引转换为占领位图
     */
    private static Bitboard indexToOccupancy(int index, Bitboard mask) {
        Bitboard occupancy = new Bitboard();
        int maskSquare = mask.getFirstSquare();
        int bitIndex = 0;
        
        while (maskSquare != -1 && bitIndex < 32) {
            if ((index & (1 << bitIndex)) != 0) {
                occupancy.setBit(maskSquare);
            }
            maskSquare = mask.getNextSquare(maskSquare);
            bitIndex++;
        }
        
        return occupancy;
    }
    
    /**
     * 计算魔法索引
     */
    private static int calculateMagicIndex(Bitboard occupancy, int square) {
        // 简化版魔法索引计算
        long hash = 0;
        int maskSquare = RELEVANT_OCCUPANCY[square].getFirstSquare();
        int bitIndex = 0;
        
        while (maskSquare != -1 && bitIndex < 32) {
            if (occupancy.getBit(maskSquare)) {
                hash ^= MAGIC_NUMBERS[square] << (bitIndex % 7);
            }
            maskSquare = RELEVANT_OCCUPANCY[square].getNextSquare(maskSquare);
            bitIndex++;
        }
        
        int maskBits = RELEVANT_OCCUPANCY[square].countBits();
        return (int) ((hash >> (64 - maskBits)) % (1 << maskBits));
    }
    
    /**
     * 获取预计算的攻击位图
     */
    public static Bitboard getAttacks(int square, Bitboard occupied) {
        if (square < 0 || square >= 90) {
            return new Bitboard();
        }
        
        Bitboard maskedOccupancy = occupied.and(RELEVANT_OCCUPANCY[square]);
        int index = calculateMagicIndex(maskedOccupancy, square);
        
        // 边界检查
        if (index >= 0 && index < ATTACK_TABLE[square].length) {
            return ATTACK_TABLE[square][index];
        }
        
        // 如果索引超出范围，回退到动态计算
        return CannonAttackCalculator.calculateSlidingAttacks(square, occupied);
    }
    
    /**
     * 获取相关占领位掩码
     */
    public static Bitboard getRelevantOccupancy(int square) {
        if (square < 0 || square >= 90) {
            return new Bitboard();
        }
        return RELEVANT_OCCUPANCY[square];
    }
}