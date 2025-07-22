#include <iostream>
#include <fstream>
#include "src/pikafish_minimal_api.h"

int main() {
    std::cout << "=== 调试C++测试 ===" << std::endl;

    // 检查NNUE文件
    std::cout << "1. 检查NNUE文件..." << std::endl;
    std::ifstream nnue_file("pikafish.nnue");
    if (nnue_file.good()) {
        nnue_file.seekg(0, std::ios::end);
        auto size = nnue_file.tellg();
        std::cout << "✓ NNUE文件存在，大小: " << size << " 字节" << std::endl;
        nnue_file.close();
    } else {
        std::cout << "✗ NNUE文件不存在于当前目录" << std::endl;
        nnue_file.close();

        // 尝试src目录
        nnue_file.open("src/pikafish.nnue");
        if (nnue_file.good()) {
            std::cout << "✓ 在src目录找到NNUE文件" << std::endl;
            nnue_file.close();
        } else {
            std::cout << "✗ src目录也没有NNUE文件" << std::endl;
            return 1;
        }
    }

    std::cout << "\n2. 测试 init_position..." << std::endl;
    const char* fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
    std::cout << "FEN: " << fen << std::endl;

    int result = init_position(fen);
    std::cout << "init_position 结果: " << result << std::endl;

    if (result == 0) {
        std::cout << "\n3. 测试 evaluate..." << std::endl;
        int eval = evaluate();
        std::cout << "评估结果: " << eval << std::endl;

        std::cout << "\n4. 测试 make_move..." << std::endl;
        // 编码一个简单的走法: e3e4 (从e3到e4)
        // e3 = 文件4(e) + 行3*9 = 4 + 27 = 31
        // e4 = 文件4(e) + 行4*9 = 4 + 36 = 40
        uint16_t move = (31 << 7) | 40;
        std::cout << "尝试走法: e3e4 (编码: " << move << ")" << std::endl;

        int move_result = make_move(move);
        std::cout << "make_move 结果: " << move_result << std::endl;

        if (move_result == 0) {
            std::cout << "✓ 走法成功，评估新局面..." << std::endl;
            int new_eval = evaluate();
            std::cout << "新评估: " << new_eval << std::endl;

            std::cout << "\n5. 测试 unmake_move..." << std::endl;
            int unmake_result = unmake_move(move);
            std::cout << "unmake_move 结果: " << unmake_result << std::endl;

            if (unmake_result == 0) {
                int restored_eval = evaluate();
                std::cout << "恢复后评估: " << restored_eval << std::endl;
                std::cout << "✓ 所有测试通过!" << std::endl;
            }
        }
    } else {
        std::cout << "✗ 初始化失败" << std::endl;
    }

    return 0;
}
