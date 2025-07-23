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
#include <deque>
#include <cstring>

#include "bitboard.h"
#include "misc.h"
#include "position.h"
#include "uci.h"
#include "tune.h"
#include "evaluate.h"
#include "nnue/network.h"
#include "pikafish_c_api.h"

using namespace Stockfish;


// Stateful engine implementation
struct EngineState {
    Position pos;
    std::deque<StateInfo> states;
    std::unique_ptr<Eval::NNUE::Networks> networks;
    std::unique_ptr<Eval::NNUE::AccumulatorStack> accumulators;
    std::unique_ptr<Eval::NNUE::AccumulatorCaches> caches;
    bool initialized = false;
    
    EngineState() : states(1) {}
};

static EngineState engine;

// Initialize engine
static void ensure_initialized() {
    if (!engine.initialized) {
        Bitboards::init();
        Position::init();
        
        engine.networks = std::make_unique<Eval::NNUE::Networks>(Eval::NNUE::NetworkBig({EvalFileDefaultNameBig, "None", ""}));
        engine.networks->big.load(".", EvalFileDefaultNameBig);
        
        engine.accumulators = std::make_unique<Eval::NNUE::AccumulatorStack>();
        engine.caches = std::make_unique<Eval::NNUE::AccumulatorCaches>(*engine.networks);
        
        engine.initialized = true;
    }
}

// External C interface for shared library
extern "C" {

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

    // Legacy function for backward compatibility
    int pikafish_evaluate_position(const char* fen) {
        if (!fen) return 0;
        
        ensure_initialized();
        
        Position temp_pos;
        StateInfo temp_si;
        temp_pos.set(std::string(fen), &temp_si);
        
        // Use tempo bonus based on side to move instead of VALUE_ZERO
        // In chess evaluation, tempo is typically a small bonus (around 10-20 centipawns) for the side to move
        Value score = Eval::evaluate(*engine.networks, temp_pos, *engine.accumulators, *engine.caches, VALUE_ZERO);
        // Adjust score based on side to move: positive score means advantage for side to move
        return temp_pos.side_to_move() == WHITE ? static_cast<int>(score) : -static_cast<int>(score);
    }

    // Stateful engine API

    // Initialize position from FEN
    int pikafish_init_position(const char* fen) {
        if (!fen) return -1;
        
        ensure_initialized();
        
        engine.states = std::deque<StateInfo>(1);
        engine.pos.set(std::string(fen), &engine.states.back());
        
        return 0;
    }

    // Apply move and return new hash
    uint64_t pikafish_do_move(uint16_t move) {
        Move m(move);
        ensure_initialized();

        // Validate move is legal
        if (!m.is_ok() || !engine.pos.legal(m)){
            return -1;
        }
        
        engine.states.emplace_back();
        DirtyPiece dp = engine.pos.do_move(m, engine.states.back(), engine.pos.gives_check(m), nullptr);
        engine.accumulators->push(dp); 
       
        return engine.pos.key();
    }

    // Evaluate current position
    int pikafish_evaluate() {
        ensure_initialized();
        
        // For now, use static evaluation
        // TODO: Implement actual search with given depth
        Value score = Eval::evaluate(*engine.networks, engine.pos, *engine.accumulators, *engine.caches, VALUE_ZERO);
        return engine.pos.side_to_move() == WHITE ? static_cast<int>(score) : -static_cast<int>(score);
    }

    // Undo last move
    uint64_t pikafish_undo_move(uint16_t move) {
        if (engine.states.size() <= 1) {
            return -1;
        }
        Move m(move);

        engine.pos.undo_move(m);
        engine.accumulators->pop();
        engine.states.pop_back();
        return 0;
    }
}
