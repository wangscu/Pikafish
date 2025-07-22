/*
  Pikafish, a UCI chess playing engine derived from Stockfish
  Copyright (C) 2004-2025 The Pikafish developers (see AUTHORS file)

  Pikafish is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Pikafish is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#include "pikafish_minimal_api.h"

#include <mutex>
#include <memory>
#include <deque>
#include <string>
#include <fstream>

#include "bitboard.h"
#include "position.h"
#include "evaluate.h"
#include "misc.h"
#include "nnue/network.h"
#include "nnue/nnue_accumulator.h"

using namespace Stockfish;

namespace {
// Global state protected by mutex
std::mutex                                     g_mutex;
std::unique_ptr<Position>                      g_position;
std::deque<StateInfo>                          g_states;
std::unique_ptr<Eval::NNUE::Networks>          g_networks;
std::unique_ptr<Eval::NNUE::AccumulatorStack>  g_accumulators;
std::unique_ptr<Eval::NNUE::AccumulatorCaches> g_caches;
bool                                           g_initialized = false;

// Move history for undo validation
std::deque<Move> g_move_history;

// Initialize engine components once
bool initialize_engine() {
    if (g_initialized)
        return true;

    // Initialize basic Pikafish components first
    Bitboards::init();
    Position::init();

    // Check if NNUE file exists
    std::ifstream nnue_file("pikafish.nnue");
    if (!nnue_file.good())
    {
        // Try alternative locations
        nnue_file.close();
        nnue_file.open("src/pikafish.nnue");
        if (!nnue_file.good())
        {
            return false;  // NNUE file not found
        }
        nnue_file.close();
    }
    else
    {
        nnue_file.close();
    }

    // Initialize NNUE networks more safely
    Eval::NNUE::EvalFile evalFileInfo;
    evalFileInfo.defaultName = "pikafish.nnue";
    evalFileInfo.current     = "pikafish.nnue";

    // Create network with error handling
    Eval::NNUE::NetworkBig bigNetwork(evalFileInfo);

    // Try to load the network
    std::string   nnue_path = "pikafish.nnue";
    std::ifstream test_file(nnue_path);
    if (!test_file.good())
    {
        nnue_path = "src/pikafish.nnue";
    }
    test_file.close();

    bigNetwork.load("", nnue_path);

    g_networks = std::make_unique<Eval::NNUE::Networks>(std::move(bigNetwork));

    // Initialize NNUE components
    g_accumulators = std::make_unique<Eval::NNUE::AccumulatorStack>();
    g_caches       = std::make_unique<Eval::NNUE::AccumulatorCaches>(*g_networks);

    // Initialize position and state
    g_position = std::make_unique<Position>();
    g_states.clear();
    g_states.emplace_back();
    g_move_history.clear();

    g_initialized = true;
    return true;
}
}

extern "C" {

int init_position(const char* fen) {
    if (!fen)
        return -1;

    std::lock_guard<std::mutex> lock(g_mutex);

    if (!initialize_engine())
    {
        return -1;
    }

    // Clear state history
    g_states.clear();
    g_states.emplace_back();
    g_move_history.clear();

    // Set position from FEN first
    std::string fenStr(fen);
    g_position->set(fenStr, &g_states.back());

    // Reset and properly initialize accumulator stack for the new position
    g_accumulators->reset();

    // Initialize accumulator for the starting position
    // This is crucial for NNUE evaluation to work correctly
    DirtyPiece dp;
    dp.from = SQ_NONE;
    g_accumulators->push(dp);

    return 0;
}

int make_move(uint16_t move) {

    if (!g_initialized || !g_position)
    {
        return -1;
    }

    Move m(move);

    // Validate move is legal
    if (!m.is_ok() || !g_position->legal(m))
    {
        return -1;
    }

    // Add new state info
    g_states.emplace_back();

    // Make the move
    DirtyPiece dp = g_position->do_move(m, g_states.back(), g_position->gives_check(m), nullptr);

    // Update accumulator stack
    g_accumulators->push(dp);

    // Record move for undo validation
    g_move_history.push_back(m);

    return 0;
}

int unmake_move(uint16_t move) {

    if (!g_initialized || !g_position || g_move_history.empty())
    {
        return -1;
    }

    Move m(move);

    // Validate this matches the last move made
    if (g_move_history.back() != m)
    {
        return -1;
    }

    // Unmake the move
    g_position->undo_move(m);

    // Pop accumulator and state
    g_accumulators->pop();
    g_states.pop_back();
    g_move_history.pop_back();

    return 0;
}

int evaluate(void) {
    if (!g_initialized || !g_position)
    {
        return 0;
    }

    // Don't evaluate positions in check (for now, as it needs special handling)
    if (g_position->checkers())
    {
        return 0;
    }

    // Call NNUE evaluation with optimism = 0
    Value eval = Eval::evaluate(*g_networks, *g_position, *g_accumulators, *g_caches, 0);

    // Convert to centipawns and return from current side's perspective
    return static_cast<int>(eval);
}

}  // extern "C"
