package com.pikafish;

/**
 * 中国象棋位板类，用于表示棋盘状态
 */
public class Bitboard {
    // 使用两个long值表示90个格子的位板（中国象棋棋盘9x10）
    private long low;
    private long high;

    /**
     * 默认构造函数，创建空的位板
     */
    public Bitboard() {
        this.low = 0L;
        this.high = 0L;
    }

    /**
     * 从两个long值构造位板
     */
    public Bitboard(long low, long high) {
        this.low = low;
        this.high = high;
    }

    /**
     * 拷贝构造函数
     */
    public Bitboard(Bitboard other) {
        this.low = other.low;
        this.high = other.high;
    }

    /**
     * 获取指定格子的位值
     */
    public boolean getBit(int square) {
        if (square < 0 || square >= 90) {
            return false;
        }
        
        if (square < 64) {
            return (low & (1L << square)) != 0;
        } else {
            return (high & (1L << (square - 64))) != 0;
        }
    }

    /**
     * 设置指定格子的位值
     */
    public void setBit(int square) {
        if (square < 0 || square >= 90) {
            return;
        }
        
        if (square < 64) {
            low |= (1L << square);
        } else {
            high |= (1L << (square - 64));
        }
    }

    /**
     * 清除指定格子的位值
     */
    public void clearBit(int square) {
        if (square < 0 || square >= 90) {
            return;
        }
        
        if (square < 64) {
            low &= ~(1L << square);
        } else {
            high &= ~(1L << (square - 64));
        }
    }

    /**
     * 与操作
     */
    public Bitboard and(Bitboard other) {
        return new Bitboard(this.low & other.low, this.high & other.high);
    }

    /**
     * 或操作
     */
    public Bitboard or(Bitboard other) {
        return new Bitboard(this.low | other.low, this.high | other.high);
    }

    /**
     * 异或操作
     */
    public Bitboard xor(Bitboard other) {
        return new Bitboard(this.low ^ other.low, this.high ^ other.high);
    }

    /**
     * 非操作
     */
    public Bitboard not() {
        return new Bitboard(~this.low, ~this.high);
    }

    /**
     * 与非操作
     */
    public Bitboard andNot(Bitboard other) {
        return new Bitboard(this.low & ~other.low, this.high & ~other.high);
    }

    /**
     * 统计位数
     */
    public int countBits() {
        return Long.bitCount(low) + Long.bitCount(high);
    }

    /**
     * 获取第一个设置位的位置
     */
    public int getFirstSquare() {
        if (low != 0) {
            return Long.numberOfTrailingZeros(low);
        }
        if (high != 0) {
            return Long.numberOfTrailingZeros(high) + 64;
        }
        return -1; // 没有设置位
    }

    /**
     * 获取下一个设置位的位置
     */
    public int getNextSquare(int prevSquare) {
        if (prevSquare < 0) {
            return getFirstSquare();
        }

        if (prevSquare < 63) {
            long mask = (1L << (prevSquare + 1)) - 1;
            long maskedLow = low & ~mask;
            if (maskedLow != 0) {
                return Long.numberOfTrailingZeros(maskedLow);
            }
            if (high != 0) {
                return Long.numberOfTrailingZeros(high) + 64;
            }
        } else if (prevSquare == 63) {
            if (high != 0) {
                return Long.numberOfTrailingZeros(high) + 64;
            }
        } else {
            long mask = (1L << (prevSquare - 64 + 1)) - 1;
            long maskedHigh = high & ~mask;
            if (maskedHigh != 0) {
                return Long.numberOfTrailingZeros(maskedHigh) + 64;
            }
        }
        return -1;
    }

    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return low == 0 && high == 0;
    }

    /**
     * 清空位板
     */
    public void clear() {
        low = 0;
        high = 0;
    }

    /**
     * 获取低位部分
     */
    public long getLow() {
        return low;
    }
    
    /**
     * 获取高位部分
     */
    public long getHigh() {
        return high;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bitboard: low=").append(Long.toHexString(low))
          .append(", high=").append(Long.toHexString(high))
          .append(", count=").append(countBits());
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Bitboard bitboard = (Bitboard) obj;
        return low == bitboard.low && high == bitboard.high;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(low) * 31 + Long.hashCode(high);
    }
}