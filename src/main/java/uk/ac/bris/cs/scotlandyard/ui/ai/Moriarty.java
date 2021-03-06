package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board;

public class Moriarty implements Ai {

	@Nonnull @Override public String name() { return "Moriarty"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {

		var moves = board.getAvailableMoves().asList();

		//bestNode is now the node with the most optimistic outcome
		if(!moves.get(0).commencedBy().isMrX()) throw new IllegalArgumentException("It's not mrX's turn!");
		final int maxDepth = 3;
		return new Minimax(timeoutPair.left()).getBestMove(new Situation((Board.GameState) board), maxDepth, moves.get(0).source(), true);
	}
}
