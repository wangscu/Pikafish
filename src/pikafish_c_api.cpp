/*
  Stockfish, a UCI chess playing engine derived from Glaurung 2.1
  Copyright (C) 2004-2025 The Stockfish developers (see AUTHORS file)

  Stockfish is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Stockfish is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <iostream>
#include <string>

#include "bitboard.h"
#include "misc.h"
#include "position.h"
#include "uci.h"
#include "tune.h"
#include "evaluate.h"
#include "nnue/network.h"
#include "pikafish_c_api.h"

using namespace Stockfish;

// Internal main function
int pikafish_main(int argc, char* argv[]) {

    std::cout << engine_info() << std::endl;

    Bitboards::init();
    Position::init();

    UCIEngine uci(argc, argv);

    Tune::init(uci.engine_options());

    uci.loop();

    return 0;
}

// Standard main function for executable
int main(int argc, char* argv[]) {
    return pikafish_main(argc, argv);
}

// External C interface for shared library
extern "C" {
    int pikafish_engine_main(int argc, char* argv[]) {
        return pikafish_main(argc, argv);
    }

    // Initialize the engine without starting the main loop
    int pikafish_engine_init(void) {
        Bitboards::init();
        Position::init();
        return 0;
    }

    // Get engine info as C string
    const char* pikafish_engine_info(void) {
        static std::string info = engine_info();
        return info.c_str();
    }

    // Evaluate a position from FEN string
    int pikafish_evaluate_position(const char* fen) {
        if (!fen) {
            std::cerr << "Invalid FEN string" << std::endl;
            return 0;
        }
        std::cerr << "Valid FEN string" << fen<< std::endl;


        // Initialize if not already done
        static bool initialized = false;
        if (!initialized) {
            Bitboards::init();
            Position::init();
            initialized = true;
        }

        // Create position from FEN
        Position pos;
        StateInfo si;
        pos.set(std::string(fen), &si);

        // Initialize networks (correct approach)
        static std::unique_ptr<Eval::NNUE::Networks> networks;
        static bool network_loaded = false;
        
        if (!network_loaded) {
            Eval::NNUE::NetworkBig network({EvalFileDefaultNameBig, "None", ""});
            network.load(".", EvalFileDefaultNameBig);
            networks = std::make_unique<Eval::NNUE::Networks>(std::move(network));
            network_loaded = true;
        }

        // Create accumulator stack and caches
        Eval::NNUE::AccumulatorStack accumulators;
        auto caches = std::make_unique<Eval::NNUE::AccumulatorCaches>(*networks);

        // Evaluate position
        Value score = Eval::evaluate(*networks, pos, accumulators, *caches, VALUE_ZERO);

        // Convert to centipawns and return
        return static_cast<int>(score);
    }
}
