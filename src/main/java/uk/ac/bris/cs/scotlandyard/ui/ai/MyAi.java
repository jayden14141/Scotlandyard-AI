package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Need to be named!"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		var moves = board.getAvailableMoves().asList();
		return moves.get(new Random().nextInt(moves.size()));
//
//		BestMove bestMove = new BestMove(board);
//		return bestMove.getBestMove();
	}

	// Helper function to return the best move among the moves derived from the score
	// Compares score value from a map and returns the corresponding key(Move)
	private Move bestMove(Board board) {
		ScoreMap sc = new ScoreMap(board);
		//TODO make an algorithm to return the move which has the highest score
		return null;
	}


}
