package com.pikafish;

/**
 * 预计算车非攻击着法表
 */
public class PrecomputedRookNonAttacks {
    
    // 预计算非攻击着法表
    private static final Bitboard[][] NON_ATTACK_TABLE = new Bitboard[90][];
    
    // 相关占领位掩码
    private static final Bitboard[] RELEVANT_OCCUPANCY = new Bitboard[90];
    
    static {
        precomputeNonAttacks();
    }
    
    /**
     * 预计算所有非攻击着法
     */
    private static void precomputeNonAttacks() {
        for (int square = 0; square < 90; square++) {
            // 计算相关占领位
            RELEVANT_OCCUPANCY[square] = calculateRelevantOccupancy(square);
            int maskBits = RELEVANT_OCCUPANCY[square].countBits();
            
            // 初始化非攻击着法表
            NON_ATTACK_TABLE[square] = new Bitboard[1 << maskBits];
            
            // 生成所有可能的占领组合
            for (int index = 0; index < (1 << maskBits); index++) {
                Bitboard occupancy = indexToOccupancy(index, RELEVANT_OCCUPANCY[square]);
                NON_ATTACK_TABLE[square][index] = calculateSlidingNonAttacks(square, occupancy);
            }
        }
    }
    
    /**
     * 计算车的滑动非攻击着法
     * @param fromSquare 起始格子
     * @param occupied 占领位图（所有棋子的位置）
     * @return 非攻击着法位图
     */
    private static Bitboard calculateSlidingNonAttacks(int fromSquare, Bitboard occupied) {
        if (!isValidSquare(fromSquare)) {
            return new Bitboard();
        }
        
        Bitboard nonAttacks = new Bitboard();
        
        // 四个方向：北、南、东、西
        int[] directions = {NORTH, SOUTH, EAST, WEST};
        
        for (int direction : directions) {
            int current = fromSquare + direction;
            
            while (isValidSquare(current) && !isOppositeEdge(fromSquare, current, direction)) {
                // 如果当前位置没有被其他棋子占据，则可以移动到该位置
                if (!occupied.getBit(current)) {
                    nonAttacks.setBit(current);
                } else {
                    // 如果遇到棋子，停止该方向（无论是己方还是对方棋子都不能移动到该位置）
                    break;
                }
                
                current += direction;
            }
        }
        
        return nonAttacks;
    }
    
    /**
     * 计算相关占领位（用于魔法位板）
     * @param square 格子位置
     * @return 相关占领位掩码
     */
    private static Bitboard calculateRelevantOccupancy(int square) {
        if (!isValidSquare(square)) {
            return new Bitboard();
        }
        
        Bitboard mask = new Bitboard();
        
        // 四个方向：北、南、东、西
        int[] directions = {NORTH, SOUTH, EAST, WEST};
        
        for (int direction : directions) {
            int current = square + direction;
            
            // 沿着每个方向直到边界
            while (isValidSquare(current) && !isOppositeEdge(square, current, direction)) {
                mask.setBit(current);
                current += direction;
            }
        }
        
        return mask;
    }
    
    /**
     * 将索引转换为占领位图
     */
    private static Bitboard indexToOccupancy(int index, Bitboard mask) {
        Bitboard occupancy = new Bitboard();
        int maskSquare = mask.getFirstSquare();
        int bitIndex = 0;
        
        while (maskSquare != -1 && bitIndex < 64) { // 支持最多64位
            if ((index & (1L << bitIndex)) != 0) { // 使用1L确保是long类型
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
            // 使用魔法位板技术计算索引
            // 将占领位图与相关占领位掩码进行按位与操作
            Bitboard maskedOccupancy = occupancy.and(RELEVANT_OCCUPANCY[square]);
            
            // 将位图转换为long值
            long occupancyLow = maskedOccupancy.getLow();
            long occupancyHigh = maskedOccupancy.getHigh();
            
            // 使用魔法数计算哈希值（使用高位部分作为主要哈希值）
            long hash = occupancyHigh * MAGIC_NUMBERS[square] + occupancyLow;
            
            // 右移以获得索引
            int maskBits = RELEVANT_OCCUPANCY[square].countBits();
            return (int) (hash >>> (128 - maskBits));
        }
    
    /**
     * 获取预计算的非攻击着法位图
     */
    public static Bitboard getNonAttacks(int square, Bitboard occupied) {
        if (square < 0 || square >= 90) {
            return new Bitboard();
        }
        
        Bitboard maskedOccupancy = occupied.and(RELEVANT_OCCUPANCY[square]);
        int index = calculateMagicIndex(maskedOccupancy, square);
        
        // 边界检查
        if (index >= 0 && index < NON_ATTACK_TABLE[square].length) {
            return NON_ATTACK_TABLE[square][index];
        }
        
        // 如果索引超出范围，回退到动态计算
        return calculateSlidingNonAttacks(square, occupied);
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
                return true;
            }
            
            // 如果在同一行内发生了横向穿越（文件从8到0或从0到8），停止移动
            if ((fromFile == 8 && toFile == 0) || (fromFile == 0 && toFile == 8)) {
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
    
    // 棋盘大小常量
    private static final int BOARD_WIDTH = 9;
    private static final int BOARD_HEIGHT = 10;
    
    // 方向常量
    private static final int NORTH = 9;
    private static final int SOUTH = -9;
    private static final int EAST = 1;
    private static final int WEST = -1;
    
    // 魔法数（复用PrecomputedRookAttacks中的魔法数）
    private static final long[] MAGIC_NUMBERS = new long[90];
    
    // 魔法数移位位数
    private static final int[] MAGIC_SHIFTS = new int[90];
    
    static {
        // 复用PrecomputedRookAttacks中的魔法数初始化代码
        initializeMagicNumbers();
    }
    
    /**
     * 初始化魔法数
     */
    private static void initializeMagicNumbers() {
        // 使用PrecomputedRookAttacks中提供的车的魔法数
        long[] rookMagicsInit = {
            0x4040000414000A40L, 0x0520004802000020L, 0x7040010400065040L, 0x4300008808100040L,
            0x0400200200400100L, 0x4040010001200049L, 0x064002A0C000410BL, 0x0200000900040084L,
            0x0080010400860A02L, 0x50002000085A0000L, 0x028A500012000060L, 0x4000400110002040L,
            0x0440400101000080L, 0x00BA800080818008L, 0x0000800200084010L, 0x184080009024A104L,
            0x0100400440900010L, 0x0C10800001200900L, 0x002060100004C392L, 0x8220008400204000L,
            0x0A18002400004000L, 0x2008201000302020L, 0x008C001000240020L, 0x0000020008008010L,
            0x1080010004108002L, 0x6000008000020214L, 0x2008004000048A18L, 0xA830100008000480L,
            0x000802080C000180L, 0x0291680010002120L, 0x2011020008002040L, 0x10000400B0004000L,
            0x0800900100040008L, 0x0000600008000800L, 0x2002086040001220L, 0x2211008020002101L,
            0x1101242108040004L, 0x2400040084220000L, 0x0280200080840040L, 0x0018000050080010L,
            0x000CA10000240004L, 0x0880100400800200L, 0x0060400481080021L, 0x0000002004200004L,
            0x2A10040440300009L, 0x0E02A42800150200L, 0x8088040004004B00L, 0x2008200142000200L,
            0x4201200200042200L, 0x0100800301A01800L, 0x018C800081016800L, 0x0101200040000200L,
            0x0000A00020000200L, 0x0040400002C28400L, 0x0000200800008021L, 0x8020003100028004L,
            0x0008000080004000L, 0x004A020008008001L, 0x0200800200401004L, 0x0002800200204004L,
            0x2103200040000401L, 0x09200810A0408408L, 0x510000124AA00304L, 0x0122080208004100L,
            0x0010080001000301L, 0x0100040084040084L, 0x0010400A000500A0L, 0x3500022D10085010L,
            0x1006810000140020L, 0x0080010408102004L, 0x6000200040002000L, 0x2280024883004800L,
            0x0220400020041000L, 0x81000010C4000081L, 0x000006004C410409L, 0x0400058000400040L,
            0x00000110041804A2L, 0x2600010000002884L, 0x81001A0204100A02L, 0x0121000000006000L,
            0x000408000C0000A0L, 0x6002600310000000L, 0x0000780020840010L, 0x6000400000440008L,
            0x0230812012001020L, 0x200080008A100080L, 0x6822020000102000L, 0x100C800041002000L,
            0x0000400240012013L, 0x0A04800040002001L, 0x48080204A0022011L, 0x0108000810041000L,
            0x1001842002002200L, 0x0840100008042060L, 0x4002082000040000L, 0x0430C22040410001L,
            0x4410088281000201L, 0x8208002000000208L, 0x4000800034408080L, 0x04280080002004B0L,
            0x0000E00010400004L, 0x0400002000048022L, 0x0000400040000170L, 0x0204414000000408L,
            0x0000004500402202L, 0x1043010000048108L, 0x211A200001280000L, 0x8608902000084008L
        };
        
        // 初始化魔法数和移位位数
        for (int square = 0; square < 90; square++) {
            // 在Java中，我们使用低位部分作为魔法数
            MAGIC_NUMBERS[square] = rookMagicsInit[square];
            
            // 计算移位位数（128 - 相关占领位的位数）
            Bitboard mask = calculateRelevantOccupancy(square);
            int maskBits = mask.countBits();
            MAGIC_SHIFTS[square] = 128 - maskBits;
        }
    }
}