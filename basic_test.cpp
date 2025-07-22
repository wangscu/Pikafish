#include <iostream>
#include "src/bitboard.h"
#include "src/position.h"

using namespace Stockfish;

int main() {
    std::cout << "=== 基础测试 ===" << std::endl;

    std::cout << "1. 初始化Bitboards..." << std::endl;
    Bitboards::init();
    std::cout << "✓ Bitboards初始化完成" << std::endl;

    std::cout << "2. 初始化Position..." << std::endl;
    Position::init();
    std::cout << "✓ Position初始化完成" << std::endl;

    std::cout << "3. 创建Position对象..." << std::endl;
    Position pos;
    std::cout << "✓ Position对象创建完成" << std::endl;

    std::cout << "4. 创建StateInfo..." << std::endl;
    StateInfo st;
    std::cout << "✓ StateInfo创建完成" << std::endl;

    std::cout << "5. 设置起始局面..." << std::endl;
    std::string fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
    std::cout << "FEN: " << fen << std::endl;

    pos.set(fen, &st);
    std::cout << "✓ 局面设置完成" << std::endl;

    std::cout << "所有基础测试通过!" << std::endl;
    return 0;
}
