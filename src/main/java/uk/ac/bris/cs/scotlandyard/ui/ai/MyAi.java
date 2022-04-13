package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;


import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Need to be named!"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
//		var moves = board.getAvailableMoves().asList();
//		return moves.get(new Random().nextInt(moves.size()));
		
		// score -> getScoreMap -> bestMove
		return bestMove(getScoreMap(board.getAvailableMoves()));
	}

	// Helper function to return the node value from a Move
	public Integer getDestination(Move move) {
		return move.accept(new Move.Visitor<>() {
			@Override
			public Integer visit(Move.SingleMove move) {
				return move.destination;
			}

			@Override
			public Integer visit(Move.DoubleMove move) {
				return move.destination2;
			}
		});
	}

	// Helper function to return the best move among the moves derived from the score
	// Compares score value from a map and returns the corresponding key(Move)
	public Move bestMove(Map<Move,Integer> scoreMap) {
		return Collections.max(scoreMap.entrySet(), Map.Entry.comparingByValue())
				.getKey();
	}


	// Reference 'https://boardgamegeek.com/thread/102272/scotland-yard-basic-strategy' for basic strategy for mrX
	// Helper function to map scores with moves
	// Used Map<Move, Int> as a return value; Indicating Move(Move) and score(Int) respectively
	public Map<Move, Integer> getScoreMap(ImmutableSet<Move> mv) {
		Map<Move, Integer> scoreMap = new HashMap<>();
		for(Move m : mv) {
			scoreMap.put(m, score(m));
		}
		return scoreMap;
	}

	// TODO Basic points to be considered in the scoring function
	// Score scales: -100 <= x <= 100 potentially ? (Can be mutated)
	// 1. Places which are near detectives get a low score
	// 2. Place where has various choices of transportation gets high score
	public Integer score(Move m) {
		return 0;
	}

}
