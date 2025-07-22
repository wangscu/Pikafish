#include <iostream>
#include <fstream>
#include <memory>
#include "src/bitboard.h"
#include "src/position.h"
#include "src/evaluate.h"
#include "src/nnue/network.h"
#include "src/nnue/nnue_accumulator.h"

using namespace Stockfish;

int main() {
    std::cout << "=== NNUE初始化测试 ===" << std::endl;

    std::cout << "1. 基础初始化..." << std::endl;
    Bitboards::init();
    Position::init();
    std::cout << "✓ 基础组件初始化完成" << std::endl;

    std::cout << "2. 检查NNUE文件..." << std::endl;
    std::ifstream nnue_file("pikafish.nnue");
    if (!nnue_file.good()) {
        std::cout << "✗ NNUE文件不存在" << std::endl;
        return 1;
    }
    nnue_file.close();
    std::cout << "✓ NNUE文件存在" << std::endl;

    std::cout << "3. 创建EvalFile..." << std::endl;
    Eval::NNUE::EvalFile evalFileInfo;
    evalFileInfo.defaultName = "pikafish.nnue";
    evalFileInfo.current = "pikafish.nnue";
    std::cout << "✓ EvalFile创建完成" << std::endl;

    std::cout << "4. 创建NetworkBig..." << std::endl;
    try {
        Eval::NNUE::NetworkBig bigNetwork(evalFileInfo);
        std::cout << "✓ NetworkBig创建完成" << std::endl;

        std::cout << "5. 加载网络..." << std::endl;
        bigNetwork.load("", "pikafish.nnue");
        std::cout << "✓ 网络加载完成" << std::endl;

        std::cout << "6. 创建Networks..." << std::endl;
        auto networks = std::make_unique<Eval::NNUE::Networks>(std::move(bigNetwork));
        std::cout << "✓ Networks创建完成" << std::endl;

        std::cout << "7. 创建AccumulatorStack..." << std::endl;
        auto accumulators = std::make_unique<Eval::NNUE::AccumulatorStack>();
        std::cout << "✓ AccumulatorStack创建完成" << std::endl;

        std::cout << "8. 创建AccumulatorCaches..." << std::endl;
        auto caches = std::make_unique<Eval::NNUE::AccumulatorCaches>(*networks);
        std::cout << "✓ AccumulatorCaches创建完成" << std::endl;

        std::cout << "所有NNUE组件初始化成功!" << std::endl;

    } catch (...) {
        std::cout << "✗ NNUE初始化过程中发生异常" << std::endl;
        return 1;
    }

    return 0;
}
