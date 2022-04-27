package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Jack the Ripper"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
//		var moves = board.getAvailableMoves().asList();
//		return moves.get(new Random().nextInt(moves.size()));
		return bestMove(board);
	}

	// Helper function to return the best move among the moves derived from the score
	// Compares score value from a map and returns the corresponding key(Move)
	private Move bestMove(Board board) {
		List<ScoreMap.Score> sm =  new ScoreMap(board).getScoreMap();
		ScoreMap.Score best = sm.get(0) ;
		for(ScoreMap.Score s :sm) {
			if (s.getScore() >= best.getScore()) {
				best = s;
			}
		}
		return best.getMove();
	}


}
